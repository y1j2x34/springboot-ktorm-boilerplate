package com.vgerbot.postgrest.registry

import com.vgerbot.postgrest.api.TableRegistry
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 默认表注册表实现
 * 使用 ConcurrentHashMap 存储表信息，支持并发访问
 */
@Component
class DefaultTableRegistry : TableRegistry {
    
    private val logger = LoggerFactory.getLogger(DefaultTableRegistry::class.java)
    
    /**
     * 表名 -> Table 对象映射
     */
    private val tables = ConcurrentHashMap<String, BaseTable<*>>()
    
    /**
     * 表名 -> (列名 -> Column 对象) 映射
     * 缓存列信息以提高查找效率
     */
    private val columnCache = ConcurrentHashMap<String, Map<String, Column<*>>>()
    
    override fun registerTable(tableName: String, table: BaseTable<*>) {
        val normalizedName = tableName.lowercase()
        tables[normalizedName] = table
        
        // 构建列缓存
        val columns = buildColumnMap(table)
        columnCache[normalizedName] = columns
        
        logger.info("Registered table '{}' with {} columns: {}", 
            normalizedName, columns.size, columns.keys.joinToString(", "))
    }
    
    override fun findTable(tableName: String): BaseTable<*>? {
        return tables[tableName.lowercase()]
    }
    
    override fun findColumn(tableName: String, columnName: String): Column<*>? {
        val columns = columnCache[tableName.lowercase()] ?: return null
        // 尝试精确匹配
        columns[columnName]?.let { return it }
        // 尝试小写匹配
        columns[columnName.lowercase()]?.let { return it }
        // 尝试下划线转换匹配（camelCase -> snake_case）
        val snakeCaseName = camelToSnakeCase(columnName)
        return columns[snakeCaseName]
    }
    
    override fun getColumns(tableName: String): Map<String, Column<*>>? {
        return columnCache[tableName.lowercase()]
    }
    
    override fun getRegisteredTableNames(): Set<String> {
        return tables.keys.toSet()
    }
    
    override fun isTableRegistered(tableName: String): Boolean {
        return tables.containsKey(tableName.lowercase())
    }
    
    /**
     * 构建列名到 Column 对象的映射
     */
    private fun buildColumnMap(table: BaseTable<*>): Map<String, Column<*>> {
        val columnMap = mutableMapOf<String, Column<*>>()
        
        for (column in table.columns) {
            // 使用列的数据库名称（通常是 snake_case）
            columnMap[column.name] = column
            // 也添加小写版本
            columnMap[column.name.lowercase()] = column
        }
        
        return columnMap
    }
    
    /**
     * 将 camelCase 转换为 snake_case
     */
    private fun camelToSnakeCase(str: String): String {
        return str.replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }
            .lowercase()
    }
}

