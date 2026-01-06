package com.vgerbot.postgrest.builder

import com.vgerbot.common.exception.BusinessException
import com.vgerbot.postgrest.api.TableRegistry
import com.vgerbot.postgrest.dto.CountType
import com.vgerbot.postgrest.dto.OrderConfig
import com.vgerbot.postgrest.dto.QueryOperation
import com.vgerbot.postgrest.dto.QueryRequest
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.expression.OrderType
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 基于 Ktorm 的查询构建器
 * 使用 Ktorm DSL 构建查询，确保数据库兼容性
 */
@Component
class KtormQueryBuilder(
    private val database: Database,
    private val tableRegistry: TableRegistry
) {
    
    private val logger = LoggerFactory.getLogger(KtormQueryBuilder::class.java)
    
    /**
     * 构建并执行查询
     */
    fun buildAndExecute(
        request: QueryRequest,
        rlsConditions: List<RlsCondition> = emptyList()
    ): QueryResult {
        // 验证表是否已注册
        val table = tableRegistry.findTable(request.from)
            ?: throw BusinessException("表 '${request.from}' 未注册，无法查询", code = 400)
        
        return when (request.operation) {
            QueryOperation.SELECT -> executeSelect(request, table, rlsConditions)
            QueryOperation.INSERT -> executeInsert(request, table)
            QueryOperation.UPDATE -> executeUpdate(request, table, rlsConditions)
            QueryOperation.DELETE -> executeDelete(request, table, rlsConditions)
            QueryOperation.UPSERT -> executeUpsert(request, table)
        }
    }
    
    /**
     * 执行 SELECT 查询
     */
    private fun executeSelect(
        request: QueryRequest,
        table: BaseTable<*>,
        rlsConditions: List<RlsCondition>
    ): QueryResult {
        val columns = resolveSelectColumns(request.select, request.from)
        
        // 构建查询
        var query = database.from(table).select(columns)
        
        // 添加 WHERE 条件
        val whereCondition = buildWhereCondition(request.where, request.from, rlsConditions)
        if (whereCondition != null) {
            query = query.where { whereCondition }
        }
        
        // 添加排序
        if (request.order != null) {
            query = applyOrder(query, request.order, request.from)
        }
        
        // 添加分页
        if (request.range != null && request.range.size >= 2) {
            val offset = request.range[0]
            val limit = request.range[1] - request.range[0] + 1
            query = query.limit(offset, limit)
        } else if (request.limit != null) {
            query = query.limit(0, request.limit)
        }
        
        logger.debug("Generated Ktorm query for SELECT on table: {}", request.from)
        
        // 执行查询
        val data = if (request.head == true) {
            emptyList()
        } else {
            query.map { row ->
                columns.associate { col ->
                    col.name to row[col]
                }
            }
        }
        
        // 计算总数（如果需要）
        val count = if (request.count == CountType.EXACT) {
            val countQuery = database.from(table).select(org.ktorm.dsl.count())
            val countCondition = buildWhereCondition(request.where, request.from, rlsConditions)
            val finalCountQuery = if (countCondition != null) {
                countQuery.where { countCondition }
            } else {
                countQuery
            }
            finalCountQuery.map { it.getInt(1) }.firstOrNull() ?: 0
        } else {
            null
        }
        
        return QueryResult(data, count, request.head == true)
    }
    
    /**
     * 执行 INSERT 查询
     */
    private fun executeInsert(
        request: QueryRequest,
        table: BaseTable<*>
    ): QueryResult {
        val data = request.data ?: throw BusinessException("INSERT 操作需要 data 字段", code = 400)
        
        val dataList = if (data is List<*>) {
            data.map { it as Map<*, *> }
        } else {
            listOf(data as Map<*, *>)
        }
        
        if (dataList.isEmpty()) {
            throw BusinessException("INSERT 操作的数据不能为空", code = 400)
        }
        
        var insertedCount = 0
        val resultData = mutableListOf<Map<String, Any?>>()
        
        for (rowData in dataList) {
            val assignments = buildAssignments(rowData, request.from)
            
            val affectedRows = database.insert(table) {
                for ((column, value) in assignments) {
                    @Suppress("UNCHECKED_CAST")
                    set(column as Column<Any?>, value)
                }
            }
            
            insertedCount += affectedRows
            
            if (request.select != null) {
                resultData.add(rowData.mapKeys { (k, _) -> k.toString() }.mapValues { (_, v) -> v })
            }
        }
        
        logger.debug("Inserted {} rows into table: {}", insertedCount, request.from)
        
        return QueryResult(resultData, insertedCount, false)
    }
    
    /**
     * 执行 UPDATE 查询
     */
    private fun executeUpdate(
        request: QueryRequest,
        table: BaseTable<*>,
        rlsConditions: List<RlsCondition>
    ): QueryResult {
        val data = request.data as? Map<*, *>
            ?: throw BusinessException("UPDATE 操作需要 data 字段", code = 400)
        
        val whereCondition = buildWhereCondition(request.where, request.from, rlsConditions)
            ?: throw BusinessException("UPDATE 操作必须包含 where 条件", code = 400)
        
        val assignments = buildAssignments(data, request.from)
        
        val affectedRows = database.update(table) {
            for ((column, value) in assignments) {
                @Suppress("UNCHECKED_CAST")
                set(column as Column<Any?>, value)
            }
            where { whereCondition }
        }
        
        logger.debug("Updated {} rows in table: {}", affectedRows, request.from)
        
        val resultData = if (request.select != null && affectedRows > 0) {
            listOf(data.mapKeys { (k, _) -> k.toString() }.mapValues { (_, v) -> v })
        } else {
            emptyList()
        }
        
        return QueryResult(resultData, affectedRows, false)
    }
    
    /**
     * 执行 DELETE 查询
     */
    private fun executeDelete(
        request: QueryRequest,
        table: BaseTable<*>,
        rlsConditions: List<RlsCondition>
    ): QueryResult {
        val whereCondition = buildWhereCondition(request.where, request.from, rlsConditions)
            ?: throw BusinessException("DELETE 操作必须包含 where 条件", code = 400)
        
        val affectedRows = database.delete(table) {
            where { whereCondition }
        }
        
        logger.debug("Deleted {} rows from table: {}", affectedRows, request.from)
        
        return QueryResult(emptyList(), affectedRows, false)
    }
    
    /**
     * 执行 UPSERT 查询（INSERT ... ON DUPLICATE KEY UPDATE）
     */
    private fun executeUpsert(
        request: QueryRequest,
        table: BaseTable<*>
    ): QueryResult {
        val data = request.data as? Map<*, *>
            ?: throw BusinessException("UPSERT 操作需要 data 字段", code = 400)
        
        val onConflict = request.onConflict
            ?: throw BusinessException("UPSERT 操作需要 onConflict 字段", code = 400)
        
        val assignments = buildAssignments(data, request.from)
        val conflictColumn = tableRegistry.findColumn(request.from, onConflict)
            ?: throw BusinessException("冲突字段 '$onConflict' 不存在", code = 400)
        
        val affectedRows = database.insertOrUpdate(table) {
            for ((column, value) in assignments) {
                @Suppress("UNCHECKED_CAST")
                set(column as Column<Any?>, value)
            }
            onDuplicateKey {
                for ((column, value) in assignments) {
                    if (column != conflictColumn) {
                        @Suppress("UNCHECKED_CAST")
                        set(column as Column<Any?>, value)
                    }
                }
            }
        }
        
        logger.debug("Upserted {} rows into table: {}", affectedRows, request.from)
        
        val resultData = if (request.select != null && affectedRows > 0) {
            listOf(data.mapKeys { (k, _) -> k.toString() }.mapValues { (_, v) -> v })
        } else {
            emptyList()
        }
        
        return QueryResult(resultData, affectedRows, false)
    }
    
    /**
     * 解析 SELECT 字段
     */
    private fun resolveSelectColumns(select: List<String>?, tableName: String): List<Column<*>> {
        val allColumns = tableRegistry.getColumns(tableName)
            ?: throw BusinessException("表 '$tableName' 未注册", code = 400)
        
        if (select == null || select.isEmpty() || select.contains("*")) {
            return allColumns.values.distinctBy { it.name }
        }
        
        return select.mapNotNull { columnName ->
            // 跳过关联查询（暂不支持）
            if (columnName.contains(":") || columnName.contains("(")) {
                logger.warn("关联查询暂不支持: {}", columnName)
                return@mapNotNull null
            }
            
            tableRegistry.findColumn(tableName, columnName)
                ?: throw BusinessException("列 '$columnName' 在表 '$tableName' 中不存在", code = 400)
        }
    }
    
    /**
     * 构建 WHERE 条件
     */
    private fun buildWhereCondition(
        where: List<Any>?,
        tableName: String,
        rlsConditions: List<RlsCondition>
    ): ColumnDeclaring<Boolean>? {
        val conditions = mutableListOf<ColumnDeclaring<Boolean>>()
        
        // 添加 RLS 条件
        for (rls in rlsConditions) {
            val column = tableRegistry.findColumn(tableName, rls.columnName)
            if (column != null) {
                val condition = buildCondition(column, rls.operator, rls.value)
                if (condition != null) {
                    conditions.add(condition)
                }
            }
        }
        
        // 添加用户提供的条件
        if (where != null && where.isNotEmpty()) {
            val userCondition = parseWhereConditions(where, tableName)
            if (userCondition != null) {
                conditions.add(userCondition)
            }
        }
        
        return if (conditions.isEmpty()) {
            null
        } else {
            conditions.reduce { a, b -> a and b }
        }
    }
    
    /**
     * 解析 WHERE 条件列表
     */
    private fun parseWhereConditions(where: List<Any>, tableName: String): ColumnDeclaring<Boolean>? {
        if (where.isEmpty()) return null
        
        val conditions = mutableListOf<ColumnDeclaring<Boolean>>()
        var currentOperator = "and"
        var i = 0
        
        while (i < where.size) {
            val item = where[i]
            
            when {
                item is String && (item.lowercase() == "and" || item.lowercase() == "or") -> {
                    currentOperator = item.lowercase()
                    i++
                }
                item is List<*> -> {
                    val condition = if (item.size >= 3 && item[0] is String && item[1] is String) {
                        // 简单条件: ["field", "operator", value]
                        val field = item[0] as String
                        val operator = item[1] as String
                        val value = item[2]
                        
                        val column = tableRegistry.findColumn(tableName, field)
                            ?: throw BusinessException("列 '$field' 在表 '$tableName' 中不存在", code = 400)
                        
                        buildCondition(column, operator, value)
                    } else {
                        // 嵌套条件
                        @Suppress("UNCHECKED_CAST")
                        parseWhereConditions(item as List<Any>, tableName)
                    }
                    
                    if (condition != null) {
                        if (conditions.isEmpty()) {
                            conditions.add(condition)
                        } else {
                            val last = conditions.removeLast()
                            if (currentOperator == "or") {
                                conditions.add(last or condition)
                            } else {
                                conditions.add(last and condition)
                            }
                        }
                    }
                    i++
                }
                else -> {
                    i++
                }
            }
        }
        
        return if (conditions.isEmpty()) null else conditions.reduce { a, b -> a and b }
    }
    
    /**
     * 构建单个条件
     */
    @Suppress("UNCHECKED_CAST")
    private fun buildCondition(column: Column<*>, operator: String, value: Any?): ColumnDeclaring<Boolean>? {
        return when (operator.lowercase()) {
            "eq" -> (column as Column<Any?>) eq value
            "neq" -> (column as Column<Any?>) notEq value
            "gt" -> {
                when (value) {
                    is Number -> (column as Column<Number>) greater value
                    is String -> (column as Column<String>) greater value
                    is Comparable<*> -> (column as Column<Comparable<Any>>) greater (value as Comparable<Any>)
                    else -> null
                }
            }
            "gte" -> {
                when (value) {
                    is Number -> (column as Column<Number>) greaterEq value
                    is String -> (column as Column<String>) greaterEq value
                    is Comparable<*> -> (column as Column<Comparable<Any>>) greaterEq (value as Comparable<Any>)
                    else -> null
                }
            }
            "lt" -> {
                when (value) {
                    is Number -> (column as Column<Number>) less value
                    is String -> (column as Column<String>) less value
                    is Comparable<*> -> (column as Column<Comparable<Any>>) less (value as Comparable<Any>)
                    else -> null
                }
            }
            "lte" -> {
                when (value) {
                    is Number -> (column as Column<Number>) lessEq value
                    is String -> (column as Column<String>) lessEq value
                    is Comparable<*> -> (column as Column<Comparable<Any>>) lessEq (value as Comparable<Any>)
                    else -> null
                }
            }
            "like" -> (column as Column<String>) like (value as String)
            "ilike" -> (column as Column<String>) ilike (value as String)
            "is" -> {
                if (value == null) {
                    column.isNull()
                } else {
                    column.isNotNull()
                }
            }
            "in" -> {
                if (value is List<*>) {
                    (column as Column<Any?>) inList (value as List<Any?>)
                } else {
                    (column as Column<Any?>) eq value
                }
            }
            else -> {
                logger.warn("不支持的运算符: {}", operator)
                null
            }
        }
    }
    
    /**
     * 应用排序
     */
    private fun applyOrder(
        query: Query,
        order: Map<String, OrderConfig>,
        tableName: String
    ): Query {
        val orderByList = order.mapNotNull { (columnName, config) ->
            val column = tableRegistry.findColumn(tableName, columnName)
            if (column == null) {
                logger.warn("排序列 '{}' 在表 '{}' 中不存在", columnName, tableName)
                return@mapNotNull null
            }
            
            val orderType = if (config.ascending) OrderType.ASCENDING else OrderType.DESCENDING
            OrderByExpression(column.asExpression(), orderType)
        }
        
        return if (orderByList.isNotEmpty()) {
            query.orderBy(*orderByList.toTypedArray())
        } else {
            query
        }
    }
    
    /**
     * 构建赋值列表（用于 INSERT 和 UPDATE）
     */
    private fun buildAssignments(data: Map<*, *>, tableName: String): List<Pair<Column<*>, Any?>> {
        return data.mapNotNull { (key, value) ->
            val columnName = key.toString()
            val column = tableRegistry.findColumn(tableName, columnName)
            if (column == null) {
                logger.warn("列 '{}' 在表 '{}' 中不存在，跳过", columnName, tableName)
                return@mapNotNull null
            }
            column to value
        }
    }
}

/**
 * RLS 条件
 */
data class RlsCondition(
    val columnName: String,
    val operator: String,
    val value: Any?
)

/**
 * 查询结果
 */
data class QueryResult(
    val data: List<Map<String, Any?>>,
    val count: Int?,
    val headOnly: Boolean
)

/**
 * 排序表达式包装类
 */
private data class OrderByExpression(
    val column: org.ktorm.expression.ColumnExpression<*>,
    val orderType: OrderType
)

/**
 * 扩展函数：将 OrderByExpression 列表应用到 Query
 */
private fun Query.orderBy(vararg orders: OrderByExpression): Query {
    var query = this
    for (order in orders) {
        query = if (order.orderType == OrderType.ASCENDING) {
            query.orderBy(order.column.asc())
        } else {
            query.orderBy(order.column.desc())
        }
    }
    return query
}

