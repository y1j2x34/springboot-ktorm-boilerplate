package com.vgerbot.dynamictable.controller

import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.dynamictable.api.DynamicTableManagementService
import com.vgerbot.dynamictable.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Dynamic Table Management Controller
 * 
 * Provides REST API for runtime table management, similar to Supabase's table editor.
 * 
 * API Endpoints:
 * - GET  /api/tables/discover          - Discover all tables in database
 * - GET  /api/tables                   - Get all registered tables
 * - POST /api/tables/register          - Register a single table
 * - POST /api/tables/register/batch    - Register multiple tables
 * - DELETE /api/tables/{tableName}     - Unregister a table
 * - GET  /api/tables/{tableName}/columns - Get table columns
 * - POST /api/tables/{tableName}/refresh - Refresh table schema
 * - PUT  /api/tables/{tableName}/excluded-columns - Set excluded columns
 * - GET  /api/tables/excluded-columns  - Get global excluded columns
 * - PUT  /api/tables/excluded-columns  - Set global excluded columns
 */
@Tag(name = "Table Management", description = "Runtime table management API (Supabase-style)")
@RestController
@RequestMapping("/api/tables")
class DynamicTableManagementController(
    private val tableManagementService: DynamicTableManagementService
) {
    
    private val logger = LoggerFactory.getLogger(DynamicTableManagementController::class.java)
    
    /**
     * Discover all tables in the database
     */
    @Operation(
        summary = "Discover database tables",
        description = "Returns all tables in the database with their registration status"
    )
    @GetMapping("/discover")
    fun discoverTables(
        @Parameter(description = "Database schema to scan")
        @RequestParam(required = false) schema: String?
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Discovering tables in schema: {}", schema ?: "default")
        val tables = tableManagementService.discoverTables(schema)
        return tables.ok()
    }
    
    /**
     * Get all registered tables
     */
    @Operation(
        summary = "Get registered tables",
        description = "Returns all tables that are registered for API access"
    )
    @GetMapping
    fun getRegisteredTables(): ResponseEntity<Map<String, Any>> {
        val tables = tableManagementService.getRegisteredTables()
        return tables.ok()
    }
    
    /**
     * Register a single table
     */
    @Operation(
        summary = "Register a table",
        description = "Register a database table for API access"
    )
    @PostMapping("/register")
    fun registerTable(
        @Valid @RequestBody request: TableRegistrationRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Registering table: {}", request.tableName)
        val result = tableManagementService.registerTable(request)
        
        if (!result.success) {
            throw com.vgerbot.common.exception.BusinessException(
                result.message ?: "注册失败",
                code = 400
            )
        }
        
        return result.created("表注册成功")
    }
    
    /**
     * Register multiple tables at once
     */
    @Operation(
        summary = "Batch register tables",
        description = "Register multiple tables at once, or register all tables from database"
    )
    @PostMapping("/register/batch")
    fun registerTables(
        @Valid @RequestBody request: BatchTableRegistrationRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Batch registering tables, registerAll={}", request.registerAll)
        val result = tableManagementService.registerTables(request)
        return result.ok()
    }
    
    /**
     * Unregister a table
     */
    @Operation(
        summary = "Unregister a table",
        description = "Remove a table from API access"
    )
    @DeleteMapping("/{tableName}")
    fun unregisterTable(
        @Parameter(description = "Table name or alias")
        @PathVariable tableName: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Unregistering table: {}", tableName)
        val success = tableManagementService.unregisterTable(tableName)
        
        if (!success) {
            throw NotFoundException("表 '$tableName' 未注册")
        }
        
        return ok("表取消注册成功")
    }
    
    /**
     * Get column information for a table
     */
    @Operation(
        summary = "Get table columns",
        description = "Returns column information for a table"
    )
    @GetMapping("/{tableName}/columns")
    fun getTableColumns(
        @Parameter(description = "Table name")
        @PathVariable tableName: String,
        @Parameter(description = "Database schema")
        @RequestParam(required = false) schema: String?
    ): ResponseEntity<Map<String, Any>> {
        val columns = tableManagementService.getTableColumns(tableName, schema)
        return columns.ok()
    }
    
    /**
     * Refresh a table's schema
     */
    @Operation(
        summary = "Refresh table schema",
        description = "Re-read table schema from database (useful after schema changes)"
    )
    @PostMapping("/{tableName}/refresh")
    fun refreshTable(
        @Parameter(description = "Table name")
        @PathVariable tableName: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Refreshing table: {}", tableName)
        val success = tableManagementService.refreshTable(tableName)
        
        if (!success) {
            throw com.vgerbot.common.exception.BusinessException(
                "刷新表 '$tableName' 失败",
                code = 400
            )
        }
        
        return ok("表刷新成功")
    }
    
    /**
     * Set excluded columns for a table
     */
    @Operation(
        summary = "Set table excluded columns",
        description = "Configure columns to exclude from API responses for a specific table"
    )
    @PutMapping("/{tableName}/excluded-columns")
    fun setTableExcludedColumns(
        @Parameter(description = "Table name")
        @PathVariable tableName: String,
        @RequestBody columns: Set<String>
    ): ResponseEntity<Map<String, Any>> {
        tableManagementService.setExcludedColumns(tableName, columns)
        return mapOf(
            "table" to tableName,
            "excludedColumns" to columns
        ).ok()
    }
    
    /**
     * Get excluded columns for a table
     */
    @Operation(
        summary = "Get table excluded columns",
        description = "Get columns that are excluded from API responses for a specific table"
    )
    @GetMapping("/{tableName}/excluded-columns")
    fun getTableExcludedColumns(
        @Parameter(description = "Table name")
        @PathVariable tableName: String
    ): ResponseEntity<Map<String, Any>> {
        val columns = tableManagementService.getExcludedColumns(tableName)
        return mapOf(
            "table" to tableName,
            "excludedColumns" to columns
        ).ok()
    }
    
    /**
     * Set global excluded columns
     */
    @Operation(
        summary = "Set global excluded columns",
        description = "Configure columns to exclude from API responses for all tables"
    )
    @PutMapping("/excluded-columns")
    fun setGlobalExcludedColumns(
        @RequestBody columns: Set<String>
    ): ResponseEntity<Map<String, Any>> {
        tableManagementService.setGlobalExcludedColumns(columns)
        return mapOf(
            "globalExcludedColumns" to columns
        ).ok()
    }
    
    /**
     * Get global excluded columns
     */
    @Operation(
        summary = "Get global excluded columns",
        description = "Get columns that are excluded from API responses for all tables"
    )
    @GetMapping("/excluded-columns")
    fun getGlobalExcludedColumns(): ResponseEntity<Map<String, Any>> {
        val columns = tableManagementService.getGlobalExcludedColumns()
        return mapOf(
            "globalExcludedColumns" to columns
        ).ok()
    }
}

