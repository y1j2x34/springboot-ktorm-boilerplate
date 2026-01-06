package com.vgerbot.dynamictable.service

import com.vgerbot.dynamictable.api.DynamicTableManagementService
import com.vgerbot.dynamictable.api.DynamicTableRegistry
import com.vgerbot.dynamictable.core.DynamicTableManager
import com.vgerbot.dynamictable.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Implementation of DynamicTableManagementService
 * 
 * Provides runtime table management capabilities, integrating with
 * DynamicTableManager and DynamicTableRegistry.
 */
@Service
class DynamicTableManagementServiceImpl(
    private val dynamicTableManager: DynamicTableManager,
    private val dynamicTableRegistry: DynamicTableRegistry
) : DynamicTableManagementService {
    
    private val logger = LoggerFactory.getLogger(DynamicTableManagementServiceImpl::class.java)
    
    override fun discoverTables(schema: String?): List<TableInfo> {
        return dynamicTableManager.discoverTables(schema).map { discovered ->
            TableInfo(
                name = discovered.name,
                schema = discovered.schema,
                isRegistered = discovered.isRegistered,
                columnCount = discovered.columnCount
            )
        }
    }
    
    override fun getRegisteredTables(): List<RegisteredTableInfo> {
        return dynamicTableManager.getRegisteredTables().map { metadata ->
            RegisteredTableInfo(
                name = metadata.name,
                alias = metadata.alias,
                schema = metadata.schema,
                columnCount = metadata.columnCount,
                primaryKeys = metadata.primaryKeys,
                registeredAt = metadata.registeredAt,
                excludedColumns = dynamicTableManager.getExcludedColumns(metadata.name)
            )
        }
    }
    
    override fun registerTable(request: TableRegistrationRequest): TableRegistrationResult {
        logger.info("Registering table: {}", request.tableName)
        
        val dynamicTable = dynamicTableManager.registerTable(
            tableName = request.tableName,
            schema = request.schema,
            alias = request.alias
        )
        
        if (dynamicTable == null) {
            return TableRegistrationResult(
                success = false,
                tableName = request.tableName,
                alias = request.alias,
                columnCount = null,
                message = "Failed to read table metadata from database"
            )
        }
        
        // Register with DynamicTableRegistry for queries
        val registrationName = request.alias ?: request.tableName
        dynamicTableRegistry.registerTable(registrationName, dynamicTable)
        
        // Configure excluded columns if provided
        if (request.excludedColumns != null && request.excludedColumns.isNotEmpty()) {
            dynamicTableManager.addTableExcludedColumns(registrationName, request.excludedColumns)
        }
        
        logger.info("Successfully registered table '{}' with {} columns", 
            registrationName, dynamicTable.columns.size)
        
        return TableRegistrationResult(
            success = true,
            tableName = request.tableName,
            alias = request.alias,
            columnCount = dynamicTable.columns.size,
            message = "Table registered successfully"
        )
    }
    
    override fun registerTables(request: BatchTableRegistrationRequest): BatchTableRegistrationResult {
        val results = mutableListOf<TableRegistrationResult>()
        
        if (request.registerAll) {
            // Register all tables from database
            val discoveredTables = dynamicTableManager.discoverTables(request.schema)
            val tablesToRegister = discoveredTables
                .filter { !it.isRegistered }
                .filter { request.excludeTables?.contains(it.name) != true }
            
            for (table in tablesToRegister) {
                val result = registerTable(TableRegistrationRequest(
                    tableName = table.name,
                    schema = request.schema
                ))
                results.add(result)
            }
        } else if (request.tables != null) {
            // Register specified tables
            for (tableRequest in request.tables) {
                val result = registerTable(tableRequest)
                results.add(result)
            }
        }
        
        val successCount = results.count { it.success }
        val failedCount = results.count { !it.success }
        
        return BatchTableRegistrationResult(
            totalRequested = results.size,
            successCount = successCount,
            failedCount = failedCount,
            results = results
        )
    }
    
    override fun unregisterTable(tableName: String): Boolean {
        logger.info("Unregistering table: {}", tableName)
        
        val success = dynamicTableManager.unregisterTable(tableName)
        if (success) {
            dynamicTableRegistry.unregisterTable(tableName)
        }
        
        return success
    }
    
    override fun getTableColumns(tableName: String, schema: String?): List<TableColumnInfo> {
        val columns = dynamicTableManager.getTableColumns(tableName, schema)
        val excludedColumns = dynamicTableManager.getExcludedColumns(tableName)
        
        return columns.map { col ->
            TableColumnInfo(
                name = col.name,
                type = col.typeName,
                sqlType = col.sqlType,
                nullable = col.nullable,
                isPrimaryKey = col.isPrimaryKey,
                defaultValue = col.defaultValue,
                isExcluded = col.name.lowercase() in excludedColumns.map { it.lowercase() }
            )
        }
    }
    
    override fun refreshTable(tableName: String): Boolean {
        logger.info("Refreshing table: {}", tableName)
        
        val dynamicTable = dynamicTableManager.refreshTable(tableName)
        if (dynamicTable != null) {
            // Re-register with DynamicTableRegistry
            dynamicTableRegistry.registerTable(tableName, dynamicTable)
            return true
        }
        return false
    }
    
    override fun setExcludedColumns(tableName: String, columns: Set<String>) {
        dynamicTableManager.addTableExcludedColumns(tableName, columns)
        logger.info("Set excluded columns for '{}': {}", tableName, columns)
    }
    
    override fun getExcludedColumns(tableName: String): Set<String> {
        return dynamicTableManager.getExcludedColumns(tableName)
    }
    
    override fun setGlobalExcludedColumns(columns: Set<String>) {
        // Clear existing and add new
        val existing = dynamicTableManager.getGlobalExcludedColumns()
        dynamicTableManager.removeGlobalExcludedColumns(existing)
        dynamicTableManager.addGlobalExcludedColumns(columns)
        logger.info("Set global excluded columns: {}", columns)
    }
    
    override fun getGlobalExcludedColumns(): Set<String> {
        return dynamicTableManager.getGlobalExcludedColumns()
    }
}

