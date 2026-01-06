package com.vgerbot.dynamictable.core

import com.vgerbot.dynamictable.dto.ColumnInfo
import com.vgerbot.dynamictable.dto.DiscoveredTable
import com.vgerbot.dynamictable.dto.TableMetadata
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

/**
 * Dynamic Table Manager - Runtime management of dynamic tables
 * 
 * This manager provides runtime registration and unregistration of database tables,
 * similar to Supabase's table management functionality.
 * 
 * Features:
 * - Runtime table registration/unregistration (no restart required)
 * - Discover available tables from database
 * - Column exclusion for sensitive data
 * - Table metadata caching
 * 
 * Usage:
 * ```kotlin
 * // Register a table at runtime
 * manager.registerTable("products")
 * 
 * // Unregister a table
 * manager.unregisterTable("products")
 * 
 * // Get all available tables from database
 * val availableTables = manager.discoverTables()
 * 
 * // Get registered tables
 * val registeredTables = manager.getRegisteredTables()
 * ```
 */
@Component
class DynamicTableManager(
    private val dataSource: DataSource
) {
    
    private val logger = LoggerFactory.getLogger(DynamicTableManager::class.java)
    
    /**
     * Lazy-initialized table builder
     */
    private val tableBuilder: DynamicTableBuilder by lazy {
        DynamicTableBuilder(dataSource)
    }
    
    /**
     * Registered dynamic tables (table name -> DynamicTable)
     */
    private val registeredTables = ConcurrentHashMap<String, DynamicTable>()
    
    /**
     * Table metadata cache (table name -> TableMetadata)
     */
    private val tableMetadataCache = ConcurrentHashMap<String, TableMetadata>()
    
    /**
     * Global excluded columns (applied to all tables)
     */
    private val globalExcludedColumns = ConcurrentHashMap.newKeySet<String>().apply {
        // Default excluded columns for security
        addAll(listOf("password", "secret", "token", "api_key", "private_key"))
    }
    
    /**
     * Per-table excluded columns
     */
    private val tableExcludedColumns = ConcurrentHashMap<String, MutableSet<String>>()
    
    /**
     * Register a table by name
     * 
     * @param tableName The database table name
     * @param schema The schema name (optional)
     * @param alias Alternative name for the table (optional)
     * @return The registered DynamicTable, or null if registration failed
     */
    fun registerTable(
        tableName: String, 
        schema: String? = null,
        alias: String? = null
    ): DynamicTable? {
        val normalizedName = (alias ?: tableName).lowercase()
        
        // Check if already registered
        if (registeredTables.containsKey(normalizedName)) {
            logger.info("Table '{}' is already registered", normalizedName)
            return registeredTables[normalizedName]
        }
        
        return try {
            val dynamicTable = tableBuilder.buildFromDatabase(tableName, schema)
            registeredTables[normalizedName] = dynamicTable
            
            // Cache metadata
            val columnInfo = tableBuilder.getColumnInfo(tableName, schema)
            tableMetadataCache[normalizedName] = TableMetadata(
                name = tableName,
                alias = alias,
                schema = schema,
                columns = columnInfo,
                registeredAt = System.currentTimeMillis()
            )
            
            logger.info("Registered dynamic table '{}' with {} columns", 
                normalizedName, dynamicTable.columns.size)
            dynamicTable
        } catch (e: Exception) {
            logger.error("Failed to register table '{}': {}", tableName, e.message)
            null
        }
    }
    
    /**
     * Unregister a table
     * 
     * @param tableName The table name or alias
     * @return true if the table was unregistered, false if it wasn't registered
     */
    fun unregisterTable(tableName: String): Boolean {
        val normalizedName = tableName.lowercase()
        val removed = registeredTables.remove(normalizedName)
        tableMetadataCache.remove(normalizedName)
        tableExcludedColumns.remove(normalizedName)
        
        if (removed != null) {
            logger.info("Unregistered dynamic table '{}'", normalizedName)
            return true
        }
        return false
    }
    
    /**
     * Get a registered table
     * 
     * @param tableName The table name or alias
     * @return The DynamicTable, or null if not registered
     */
    fun getTable(tableName: String): DynamicTable? {
        return registeredTables[tableName.lowercase()]
    }
    
    /**
     * Check if a table is registered
     */
    fun isTableRegistered(tableName: String): Boolean {
        return registeredTables.containsKey(tableName.lowercase())
    }
    
    /**
     * Get all registered table names
     */
    fun getRegisteredTableNames(): Set<String> {
        return registeredTables.keys.toSet()
    }
    
    /**
     * Get all registered tables with metadata
     */
    fun getRegisteredTables(): List<TableMetadata> {
        return tableMetadataCache.values.toList()
    }
    
    /**
     * Discover all tables in the database
     * 
     * @param schema The schema to scan (optional)
     * @return List of discovered table information
     */
    fun discoverTables(schema: String? = null): List<DiscoveredTable> {
        val tableNames = tableBuilder.getAllTableNames(schema)
        return tableNames.map { tableName ->
            val isRegistered = isTableRegistered(tableName)
            val columnCount = if (isRegistered) {
                registeredTables[tableName.lowercase()]?.columns?.size ?: 0
            } else {
                try {
                    tableBuilder.getColumnInfo(tableName, schema).size
                } catch (e: Exception) {
                    0
                }
            }
            
            DiscoveredTable(
                name = tableName,
                schema = schema,
                isRegistered = isRegistered,
                columnCount = columnCount
            )
        }
    }
    
    /**
     * Get column information for a table (doesn't require registration)
     */
    fun getTableColumns(tableName: String, schema: String? = null): List<ColumnInfo> {
        // Check cache first
        val cached = tableMetadataCache[tableName.lowercase()]
        if (cached != null) {
            return cached.columns
        }
        
        return tableBuilder.getColumnInfo(tableName, schema)
    }
    
    /**
     * Register multiple tables at once
     * 
     * @param tableNames List of table names
     * @param schema The schema name (optional)
     * @return Map of table name to registration success
     */
    fun registerTables(tableNames: List<String>, schema: String? = null): Map<String, Boolean> {
        return tableNames.associateWith { tableName ->
            registerTable(tableName, schema) != null
        }
    }
    
    /**
     * Register all tables in the database
     * 
     * @param schema The schema to scan (optional)
     * @param excludeTables Tables to exclude
     * @return Number of tables registered
     */
    fun registerAllTables(
        schema: String? = null, 
        excludeTables: Set<String> = emptySet()
    ): Int {
        val tableNames = tableBuilder.getAllTableNames(schema)
            .filter { it.lowercase() !in excludeTables.map { e -> e.lowercase() } }
        
        var count = 0
        for (tableName in tableNames) {
            if (registerTable(tableName, schema) != null) {
                count++
            }
        }
        
        logger.info("Registered {} tables from database", count)
        return count
    }
    
    /**
     * Add global excluded columns
     */
    fun addGlobalExcludedColumns(columns: Collection<String>) {
        globalExcludedColumns.addAll(columns.map { it.lowercase() })
        logger.info("Added global excluded columns: {}", columns)
    }
    
    /**
     * Remove global excluded columns
     */
    fun removeGlobalExcludedColumns(columns: Collection<String>) {
        globalExcludedColumns.removeAll(columns.map { it.lowercase() }.toSet())
    }
    
    /**
     * Get global excluded columns
     */
    fun getGlobalExcludedColumns(): Set<String> {
        return globalExcludedColumns.toSet()
    }
    
    /**
     * Add excluded columns for a specific table
     */
    fun addTableExcludedColumns(tableName: String, columns: Collection<String>) {
        val normalizedName = tableName.lowercase()
        tableExcludedColumns.computeIfAbsent(normalizedName) { 
            ConcurrentHashMap.newKeySet() 
        }.addAll(columns.map { it.lowercase() })
    }
    
    /**
     * Get excluded columns for a table (global + table-specific)
     */
    fun getExcludedColumns(tableName: String): Set<String> {
        val tableSpecific = tableExcludedColumns[tableName.lowercase()] ?: emptySet()
        return globalExcludedColumns + tableSpecific
    }
    
    /**
     * Check if a column should be excluded
     */
    fun isColumnExcluded(tableName: String, columnName: String): Boolean {
        val normalizedColumn = columnName.lowercase()
        if (normalizedColumn in globalExcludedColumns) return true
        
        val tableSpecific = tableExcludedColumns[tableName.lowercase()]
        return tableSpecific?.contains(normalizedColumn) == true
    }
    
    /**
     * Refresh a registered table (re-read from database)
     */
    fun refreshTable(tableName: String, schema: String? = null): DynamicTable? {
        val normalizedName = tableName.lowercase()
        if (!registeredTables.containsKey(normalizedName)) {
            logger.warn("Table '{}' is not registered, cannot refresh", tableName)
            return null
        }
        
        // Unregister and re-register
        unregisterTable(tableName)
        return registerTable(tableName, schema)
    }
    
    /**
     * Clear all registered tables
     */
    fun clearAll() {
        registeredTables.clear()
        tableMetadataCache.clear()
        tableExcludedColumns.clear()
        logger.info("Cleared all registered dynamic tables")
    }
}

