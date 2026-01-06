package com.vgerbot.dynamictable.registry

import com.vgerbot.dynamictable.api.DynamicTableRegistry
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Default implementation of DynamicTableRegistry
 * 
 * Uses ConcurrentHashMap for thread-safe table storage and lookup.
 */
@Component
class DefaultDynamicTableRegistry : DynamicTableRegistry {
    
    private val logger = LoggerFactory.getLogger(DefaultDynamicTableRegistry::class.java)
    
    /**
     * Table name -> Table object mapping
     */
    private val tables = ConcurrentHashMap<String, BaseTable<*>>()
    
    /**
     * Table name -> (Column name -> Column object) mapping
     * Cached for efficient column lookup
     */
    private val columnCache = ConcurrentHashMap<String, Map<String, Column<*>>>()
    
    override fun registerTable(tableName: String, table: BaseTable<*>) {
        val normalizedName = tableName.lowercase()
        tables[normalizedName] = table
        
        // Build column cache
        val columns = buildColumnMap(table)
        columnCache[normalizedName] = columns
        
        logger.info("Registered table '{}' with {} columns: {}", 
            normalizedName, columns.size, columns.keys.joinToString(", "))
    }
    
    override fun unregisterTable(tableName: String): Boolean {
        val normalizedName = tableName.lowercase()
        val removed = tables.remove(normalizedName)
        columnCache.remove(normalizedName)
        
        if (removed != null) {
            logger.info("Unregistered table '{}'", normalizedName)
            return true
        }
        return false
    }
    
    override fun findTable(tableName: String): BaseTable<*>? {
        return tables[tableName.lowercase()]
    }
    
    override fun findColumn(tableName: String, columnName: String): Column<*>? {
        val columns = columnCache[tableName.lowercase()] ?: return null
        // Try exact match
        columns[columnName]?.let { return it }
        // Try lowercase match
        columns[columnName.lowercase()]?.let { return it }
        // Try snake_case conversion (camelCase -> snake_case)
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
     * Build column name to Column object mapping
     */
    private fun buildColumnMap(table: BaseTable<*>): Map<String, Column<*>> {
        val columnMap = mutableMapOf<String, Column<*>>()
        
        for (column in table.columns) {
            // Use database column name (usually snake_case)
            columnMap[column.name] = column
            // Also add lowercase version
            columnMap[column.name.lowercase()] = column
        }
        
        return columnMap
    }
    
    /**
     * Convert camelCase to snake_case
     */
    private fun camelToSnakeCase(str: String): String {
        return str.replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }
            .lowercase()
    }
}

