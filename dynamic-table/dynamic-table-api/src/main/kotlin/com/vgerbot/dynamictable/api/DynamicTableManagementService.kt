package com.vgerbot.dynamictable.api

import com.vgerbot.dynamictable.dto.*

/**
 * Dynamic Table Management Service Interface
 * 
 * Provides runtime management of database tables.
 * Similar to Supabase's table management functionality.
 */
interface DynamicTableManagementService {
    
    /**
     * Discover all tables in the database
     * 
     * @param schema The schema to scan (optional)
     * @return List of discovered tables with registration status
     */
    fun discoverTables(schema: String? = null): List<TableInfo>
    
    /**
     * Get all registered tables
     * 
     * @return List of registered tables with metadata
     */
    fun getRegisteredTables(): List<RegisteredTableInfo>
    
    /**
     * Register a table for API access
     * 
     * @param request Registration request
     * @return Registration result
     */
    fun registerTable(request: TableRegistrationRequest): TableRegistrationResult
    
    /**
     * Register multiple tables at once
     * 
     * @param request Batch registration request
     * @return Results for each table
     */
    fun registerTables(request: BatchTableRegistrationRequest): BatchTableRegistrationResult
    
    /**
     * Unregister a table (remove from API access)
     * 
     * @param tableName The table name or alias
     * @return true if successful
     */
    fun unregisterTable(tableName: String): Boolean
    
    /**
     * Get column information for a table
     * 
     * @param tableName The table name
     * @param schema The schema (optional)
     * @return List of column information
     */
    fun getTableColumns(tableName: String, schema: String? = null): List<TableColumnInfo>
    
    /**
     * Refresh a table's schema (re-read from database)
     * 
     * @param tableName The table name
     * @return true if successful
     */
    fun refreshTable(tableName: String): Boolean
    
    /**
     * Configure excluded columns for a table
     * 
     * @param tableName The table name
     * @param columns Columns to exclude
     */
    fun setExcludedColumns(tableName: String, columns: Set<String>)
    
    /**
     * Get excluded columns for a table
     * 
     * @param tableName The table name
     * @return Set of excluded column names
     */
    fun getExcludedColumns(tableName: String): Set<String>
    
    /**
     * Configure global excluded columns
     * 
     * @param columns Columns to exclude from all tables
     */
    fun setGlobalExcludedColumns(columns: Set<String>)
    
    /**
     * Get global excluded columns
     * 
     * @return Set of globally excluded column names
     */
    fun getGlobalExcludedColumns(): Set<String>
}

