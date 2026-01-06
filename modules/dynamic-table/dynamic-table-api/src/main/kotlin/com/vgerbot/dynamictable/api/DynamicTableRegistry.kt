package com.vgerbot.dynamictable.api

import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column

/**
 * Dynamic Table Registry Interface
 * 
 * Provides registration and lookup of dynamically created tables.
 * This is similar to TableRegistry but specifically designed for dynamic tables.
 */
interface DynamicTableRegistry {
    
    /**
     * Register a table
     * 
     * @param tableName The name to register the table under
     * @param table The Ktorm BaseTable object
     */
    fun registerTable(tableName: String, table: BaseTable<*>)
    
    /**
     * Unregister a table
     * 
     * @param tableName The table name
     * @return true if the table was unregistered, false if it wasn't registered
     */
    fun unregisterTable(tableName: String): Boolean
    
    /**
     * Find a table by name
     * 
     * @param tableName The table name
     * @return The BaseTable object, or null if not found
     */
    fun findTable(tableName: String): BaseTable<*>?
    
    /**
     * Find a column by table name and column name
     * 
     * @param tableName The table name
     * @param columnName The column name
     * @return The Column object, or null if not found
     */
    fun findColumn(tableName: String, columnName: String): Column<*>?
    
    /**
     * Get all columns for a table
     * 
     * @param tableName The table name
     * @return Map of column name to Column object
     */
    fun getColumns(tableName: String): Map<String, Column<*>>?
    
    /**
     * Get all registered table names
     */
    fun getRegisteredTableNames(): Set<String>
    
    /**
     * Check if a table is registered
     */
    fun isTableRegistered(tableName: String): Boolean
}

