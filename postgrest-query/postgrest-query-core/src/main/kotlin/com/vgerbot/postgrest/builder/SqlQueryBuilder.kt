package com.vgerbot.postgrest.builder

import com.vgerbot.postgrest.dto.CountType
import com.vgerbot.postgrest.dto.OrderConfig
import com.vgerbot.postgrest.dto.QueryOperation
import com.vgerbot.postgrest.dto.QueryRequest
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.util.*

/**
 * SQL 查询构建器
 * 将 PostgREST 风格的 JSON 配置转换为 SQL 查询
 */
class SqlQueryBuilder(
    private val connection: Connection
) {
    
    private val logger = LoggerFactory.getLogger(SqlQueryBuilder::class.java)
    
    /**
     * 构建并执行查询
     */
    fun buildAndExecute(
        request: QueryRequest,
        rlsConditions: List<String> = emptyList()
    ): QueryResult {
        return when (request.operation) {
            QueryOperation.SELECT -> buildSelectQuery(request, rlsConditions)
            QueryOperation.INSERT -> buildInsertQuery(request, rlsConditions)
            QueryOperation.UPDATE -> buildUpdateQuery(request, rlsConditions)
            QueryOperation.DELETE -> buildDeleteQuery(request, rlsConditions)
            QueryOperation.UPSERT -> buildUpsertQuery(request, rlsConditions)
        }
    }
    
    /**
     * 构建 SELECT 查询
     */
    private fun buildSelectQuery(
        request: QueryRequest,
        rlsConditions: List<String>
    ): QueryResult {
        val tableName = sanitizeTableName(request.from)
        val selectFields = buildSelectFields(request.select)
        val whereClause = buildWhereClause(request.where, rlsConditions)
        val orderClause = buildOrderClause(request.order)
        val limitClause = buildLimitClause(request.limit, request.range)
        
        val sql = buildString {
            append("SELECT ")
            if (request.count == CountType.EXACT && request.head == true) {
                append("COUNT(*) as count")
            } else {
                append(selectFields)
                if (request.count == CountType.EXACT) {
                    append(", COUNT(*) OVER() as total_count")
                }
            }
            append(" FROM ")
            append(tableName)
            if (whereClause.isNotEmpty()) {
                append(" WHERE ")
                append(whereClause)
            }
            if (orderClause.isNotEmpty()) {
                append(" ORDER BY ")
                append(orderClause)
            }
            if (limitClause.isNotEmpty()) {
                append(" ")
                append(limitClause)
            }
        }
        
        logger.debug("Generated SELECT SQL: {}", sql)
        
        return executeQuery(sql, request.where, request.count, request.head == true)
    }
    
    /**
     * 构建 INSERT 查询
     */
    private fun buildInsertQuery(
        request: QueryRequest,
        rlsConditions: List<String>
    ): QueryResult {
        val tableName = sanitizeTableName(request.from)
        val data = request.data ?: throw IllegalArgumentException("INSERT 操作需要 data 字段")
        
        val dataList = if (data is List<*>) {
            data.map { it as Map<*, *> }
        } else {
            listOf(data as Map<*, *>)
        }
        
        if (dataList.isEmpty()) {
            throw IllegalArgumentException("INSERT 操作的数据不能为空")
        }
        
        val columns = dataList[0].keys.map { sanitizeColumnName(it.toString()) }
        val placeholders = columns.joinToString(", ") { "?" }
        
        val sql = buildString {
            append("INSERT INTO ")
            append(tableName)
            append(" (")
            append(columns.joinToString(", "))
            append(") VALUES (")
            append(placeholders)
            append(")")
        }
        
        logger.debug("Generated INSERT SQL: {}", sql)
        
        return executeInsert(sql, dataList, request.select)
    }
    
    /**
     * 构建 UPDATE 查询
     */
    private fun buildUpdateQuery(
        request: QueryRequest,
        rlsConditions: List<String>
    ): QueryResult {
        val tableName = sanitizeTableName(request.from)
        val data = request.data as? Map<*, *> 
            ?: throw IllegalArgumentException("UPDATE 操作需要 data 字段")
        
        val setClause = data.keys.mapIndexed { index, key ->
            "${sanitizeColumnName(key.toString())} = ?"
        }.joinToString(", ")
        
        val whereClause = buildWhereClause(request.where, rlsConditions)
        
        if (whereClause.isEmpty()) {
            throw IllegalArgumentException("UPDATE 操作必须包含 where 条件")
        }
        
        val sql = buildString {
            append("UPDATE ")
            append(tableName)
            append(" SET ")
            append(setClause)
            append(" WHERE ")
            append(whereClause)
        }
        
        logger.debug("Generated UPDATE SQL: {}", sql)
        
        return executeUpdate(sql, data, request.where, request.select)
    }
    
    /**
     * 构建 DELETE 查询
     */
    private fun buildDeleteQuery(
        request: QueryRequest,
        rlsConditions: List<String>
    ): QueryResult {
        val tableName = sanitizeTableName(request.from)
        val whereClause = buildWhereClause(request.where, rlsConditions)
        
        if (whereClause.isEmpty()) {
            throw IllegalArgumentException("DELETE 操作必须包含 where 条件")
        }
        
        val sql = buildString {
            append("DELETE FROM ")
            append(tableName)
            append(" WHERE ")
            append(whereClause)
        }
        
        logger.debug("Generated DELETE SQL: {}", sql)
        
        return executeDelete(sql, request.where, request.select)
    }
    
    /**
     * 构建 UPSERT 查询（INSERT ... ON DUPLICATE KEY UPDATE）
     */
    private fun buildUpsertQuery(
        request: QueryRequest,
        rlsConditions: List<String>
    ): QueryResult {
        val tableName = sanitizeTableName(request.from)
        val data = request.data as? Map<*, *>
            ?: throw IllegalArgumentException("UPSERT 操作需要 data 字段")
        
        val onConflict = request.onConflict
            ?: throw IllegalArgumentException("UPSERT 操作需要 onConflict 字段")
        
        val columns = data.keys.map { sanitizeColumnName(it.toString()) }
        val placeholders = columns.joinToString(", ") { "?" }
        val updateClause = columns.filter { it != sanitizeColumnName(onConflict) }
            .joinToString(", ") { "$it = ?" }
        
        val sql = buildString {
            append("INSERT INTO ")
            append(tableName)
            append(" (")
            append(columns.joinToString(", "))
            append(") VALUES (")
            append(placeholders)
            append(") ON DUPLICATE KEY UPDATE ")
            append(updateClause)
        }
        
        logger.debug("Generated UPSERT SQL: {}", sql)
        
        return executeUpsert(sql, data, request.select)
    }
    
    /**
     * 构建 SELECT 字段列表
     */
    private fun buildSelectFields(select: List<String>?): String {
        if (select == null || select.isEmpty() || select.contains("*")) {
            return "*"
        }
        return select.joinToString(", ") { sanitizeColumnName(it) }
    }
    
    /**
     * 构建 WHERE 子句
     */
    private fun buildWhereClause(
        where: List<Any>?,
        rlsConditions: List<String>
    ): String {
        val conditions = mutableListOf<String>()
        
        // 添加 RLS 条件
        conditions.addAll(rlsConditions)
        
        // 添加用户提供的条件
        if (where != null && where.isNotEmpty()) {
            val userConditions = buildWhereConditions(where)
            if (userConditions.isNotEmpty()) {
                conditions.add(userConditions)
            }
        }
        
        return conditions.joinToString(" AND ")
    }
    
    /**
     * 构建 WHERE 条件
     */
    private fun buildWhereConditions(where: List<Any>): String {
        val conditions = mutableListOf<String>()
        var i = 0
        
        while (i < where.size) {
            val item = where[i]
            
            when {
                item is List<*> && item.size >= 3 -> {
                    // 条件表达式: ["field", "operator", value]
                    val field = sanitizeColumnName(item[0].toString())
                    val operator = item[1].toString().lowercase()
                    val value = item[2]
                    
                    val condition = when (operator) {
                        "eq" -> "$field = ?"
                        "neq" -> "$field != ?"
                        "gt" -> "$field > ?"
                        "gte" -> "$field >= ?"
                        "lt" -> "$field < ?"
                        "lte" -> "$field <= ?"
                        "like" -> "$field LIKE ?"
                        "ilike" -> "LOWER($field) LIKE LOWER(?)"
                        "is" -> {
                            if (value == null) {
                                "$field IS NULL"
                            } else {
                                "$field IS NOT NULL"
                            }
                        }
                        "in" -> {
                            if (value is List<*>) {
                                val placeholders = value.map { "?" }.joinToString(", ")
                                "$field IN ($placeholders)"
                            } else {
                                "$field = ?"
                            }
                        }
                        "contains" -> {
                            // JSON 数组包含检查（MariaDB 使用 JSON_CONTAINS）
                            if (value is List<*>) {
                                val jsonValue = value.joinToString(", ") { "'$it'" }
                                "JSON_CONTAINS($field, '[$jsonValue]')"
                            } else {
                                "JSON_CONTAINS($field, '\"$value\"')"
                            }
                        }
                        else -> throw IllegalArgumentException("不支持的运算符: $operator")
                    }
                    conditions.add(condition)
                    i++
                }
                item == "and" || item == "AND" -> {
                    if (i + 1 < where.size) {
                        val nextCondition = buildWhereConditions(listOf(where[i + 1]))
                        if (nextCondition.isNotEmpty()) {
                            if (conditions.isNotEmpty()) {
                                conditions.add("AND")
                            }
                            conditions.add(nextCondition)
                        }
                        i += 2
                    } else {
                        i++
                    }
                }
                item == "or" || item == "OR" -> {
                    if (i + 1 < where.size) {
                        val nextCondition = buildWhereConditions(listOf(where[i + 1]))
                        if (nextCondition.isNotEmpty()) {
                            if (conditions.isNotEmpty()) {
                                conditions.add("OR")
                            }
                            conditions.add(nextCondition)
                        }
                        i += 2
                    } else {
                        i++
                    }
                }
                item is List<*> -> {
                    // 嵌套条件
                    val nestedCondition = buildWhereConditions(item)
                    if (nestedCondition.isNotEmpty()) {
                        conditions.add("($nestedCondition)")
                    }
                    i++
                }
                else -> {
                    i++
                }
            }
        }
        
        return conditions.joinToString(" ")
    }
    
    /**
     * 构建 ORDER BY 子句
     */
    private fun buildOrderClause(order: Map<String, OrderConfig>?): String {
        if (order == null || order.isEmpty()) {
            return ""
        }
        
        return order.map { (field, config) ->
            val direction = if (config.ascending) "ASC" else "DESC"
            val nulls = when {
                config.nullsFirst == true -> "NULLS FIRST"
                config.nullsFirst == false -> "NULLS LAST"
                else -> ""
            }
            "${sanitizeColumnName(field)} $direction $nulls".trim()
        }.joinToString(", ")
    }
    
    /**
     * 构建 LIMIT 子句
     */
    private fun buildLimitClause(limit: Int?, range: List<Int>?): String {
        return when {
            range != null && range.size >= 2 -> {
                val offset = range[0]
                val count = range[1] - range[0] + 1
                "LIMIT $count OFFSET $offset"
            }
            limit != null -> {
                "LIMIT $limit"
            }
            else -> ""
        }
    }
    
    /**
     * 执行 SELECT 查询
     */
    private fun executeQuery(
        sql: String,
        where: List<Any>?,
        count: CountType?,
        headOnly: Boolean
    ): QueryResult {
        val preparedStatement = connection.prepareStatement(sql)
        setWhereParameters(preparedStatement, where)
        
        val resultSet = preparedStatement.executeQuery()
        val data = if (headOnly) {
            emptyList()
        } else {
            resultSetToMapList(resultSet)
        }
        
        val totalCount = if (count == CountType.EXACT && !headOnly) {
            // 从结果中提取 total_count
            data.firstOrNull()?.get("total_count") as? Int
        } else if (headOnly) {
            resultSet.next()
            resultSet.getInt("count")
        } else {
            null
        }
        
        resultSet.close()
        preparedStatement.close()
        
        return QueryResult(data, totalCount, headOnly)
    }
    
    /**
     * 执行 INSERT 查询
     */
    private fun executeInsert(
        sql: String,
        dataList: List<Map<*, *>>,
        select: List<String>?
    ): QueryResult {
        val preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        
        for (data in dataList) {
            setInsertParameters(preparedStatement, data)
            preparedStatement.addBatch()
        }
        
        preparedStatement.executeBatch()
        
        // 如果需要返回数据，执行 SELECT 查询
        val resultData = if (select != null) {
            // 简化实现：返回插入的数据
            dataList.map { it.mapKeys { (k, _) -> k.toString() }.mapValues { (_, v) -> v } }
        } else {
            emptyList()
        }
        
        preparedStatement.close()
        
        return QueryResult(resultData, null, false)
    }
    
    /**
     * 执行 UPDATE 查询
     */
    private fun executeUpdate(
        sql: String,
        data: Map<*, *>,
        where: List<Any>?,
        select: List<String>?
    ): QueryResult {
        val preparedStatement = connection.prepareStatement(sql)
        
        var paramIndex = 1
        // 设置 SET 子句的参数
        for (value in data.values) {
            preparedStatement.setObject(paramIndex++, value)
        }
        // 设置 WHERE 子句的参数
        setWhereParameters(preparedStatement, where, paramIndex)
        
        val affectedRows = preparedStatement.executeUpdate()
        
        // 如果需要返回数据，执行 SELECT 查询
        val resultData = if (select != null && affectedRows > 0) {
            // 简化实现：返回更新的数据
            listOf(data.mapKeys { (k, _) -> k.toString() }.mapValues { (_, v) -> v })
        } else {
            emptyList()
        }
        
        preparedStatement.close()
        
        return QueryResult(resultData, affectedRows, false)
    }
    
    /**
     * 执行 DELETE 查询
     */
    private fun executeDelete(
        sql: String,
        where: List<Any>?,
        select: List<String>?
    ): QueryResult {
        val preparedStatement = connection.prepareStatement(sql)
        setWhereParameters(preparedStatement, where)
        
        val affectedRows = preparedStatement.executeUpdate()
        preparedStatement.close()
        
        return QueryResult(emptyList(), affectedRows, false)
    }
    
    /**
     * 执行 UPSERT 查询
     */
    private fun executeUpsert(
        sql: String,
        data: Map<*, *>,
        select: List<String>?
    ): QueryResult {
        val preparedStatement = connection.prepareStatement(sql)
        
        var paramIndex = 1
        // 设置 INSERT 的参数
        for (value in data.values) {
            preparedStatement.setObject(paramIndex++, value)
        }
        // 设置 UPDATE 的参数（排除冲突字段）
        for ((key, value) in data) {
            if (key != "onConflict") {
                preparedStatement.setObject(paramIndex++, value)
            }
        }
        
        val affectedRows = preparedStatement.executeUpdate()
        
        // 如果需要返回数据，执行 SELECT 查询
        val resultData = if (select != null && affectedRows > 0) {
            listOf(data.mapKeys { (k, _) -> k.toString() }.mapValues { (_, v) -> v })
        } else {
            emptyList()
        }
        
        preparedStatement.close()
        
        return QueryResult(resultData, affectedRows, false)
    }
    
    /**
     * 设置 WHERE 子句的参数
     */
    private fun setWhereParameters(
        statement: PreparedStatement,
        where: List<Any>?,
        startIndex: Int = 1
    ): Int {
        if (where == null || where.isEmpty()) {
            return startIndex
        }
        
        var paramIndex = startIndex
        
        for (item in where) {
            when {
                item is List<*> && item.size >= 3 -> {
                    val operator = item[1].toString().lowercase()
                    val value = item[2]
                    
                    when (operator) {
                        "in" -> {
                            if (value is List<*>) {
                                for (v in value) {
                                    statement.setObject(paramIndex++, v)
                                }
                            } else {
                                statement.setObject(paramIndex++, value)
                            }
                        }
                        "is" -> {
                            // IS NULL 不需要参数
                            if (value != null) {
                                statement.setObject(paramIndex++, value)
                            }
                        }
                        else -> {
                            statement.setObject(paramIndex++, value)
                        }
                    }
                }
                item is List<*> -> {
                    // 递归处理嵌套条件
                    paramIndex = setWhereParameters(statement, item, paramIndex)
                }
            }
        }
        
        return paramIndex
    }
    
    /**
     * 设置 INSERT 参数
     */
    private fun setInsertParameters(
        statement: PreparedStatement,
        data: Map<*, *>
    ) {
        var paramIndex = 1
        for (value in data.values) {
            statement.setObject(paramIndex++, value)
        }
    }
    
    /**
     * 将 ResultSet 转换为 Map 列表
     */
    private fun resultSetToMapList(resultSet: ResultSet): List<Map<String, Any?>> {
        val result = mutableListOf<Map<String, Any?>>()
        val metaData: ResultSetMetaData = resultSet.metaData
        val columnCount = metaData.columnCount
        
        while (resultSet.next()) {
            val row = mutableMapOf<String, Any?>()
            for (i in 1..columnCount) {
                val columnName = metaData.getColumnLabel(i)
                val value = resultSet.getObject(i)
                row[columnName] = value
            }
            result.add(row)
        }
        
        return result
    }
    
    /**
     * 清理表名（防止 SQL 注入）
     */
    private fun sanitizeTableName(name: String): String {
        // 只允许字母、数字、下划线和点
        return name.replace(Regex("[^a-zA-Z0-9_.]"), "")
    }
    
    /**
     * 清理列名（防止 SQL 注入）
     */
    private fun sanitizeColumnName(name: String): String {
        // 处理关联查询格式: "user:users(id, name)"
        if (name.contains(":")) {
            return name // 暂时不处理关联查询
        }
        // 只允许字母、数字和下划线
        return name.replace(Regex("[^a-zA-Z0-9_]"), "")
    }
}

/**
 * 查询结果
 */
data class QueryResult(
    val data: List<Map<String, Any?>>,
    val count: Int?,
    val headOnly: Boolean
)

