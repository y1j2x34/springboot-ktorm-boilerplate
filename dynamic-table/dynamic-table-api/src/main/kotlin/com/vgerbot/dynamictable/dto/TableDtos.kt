package com.vgerbot.dynamictable.dto

/**
 * Basic table information (discovered from database)
 */
data class TableInfo(
    val name: String,
    val schema: String?,
    val isRegistered: Boolean,
    val columnCount: Int
)

/**
 * Registered table information with metadata
 */
data class RegisteredTableInfo(
    val name: String,
    val alias: String?,
    val schema: String?,
    val columnCount: Int,
    val primaryKeys: List<String>,
    val registeredAt: Long,
    val excludedColumns: Set<String>
)

/**
 * Table column information
 */
data class TableColumnInfo(
    val name: String,
    val type: String,
    val sqlType: Int,
    val nullable: Boolean,
    val isPrimaryKey: Boolean,
    val defaultValue: String?,
    val isExcluded: Boolean
)

/**
 * Request to register a single table
 */
data class TableRegistrationRequest(
    val tableName: String,
    val schema: String? = null,
    val alias: String? = null,
    val excludedColumns: Set<String>? = null
)

/**
 * Result of table registration
 */
data class TableRegistrationResult(
    val success: Boolean,
    val tableName: String,
    val alias: String?,
    val columnCount: Int?,
    val message: String?
)

/**
 * Request to register multiple tables
 */
data class BatchTableRegistrationRequest(
    val tables: List<TableRegistrationRequest>? = null,
    val registerAll: Boolean = false,
    val schema: String? = null,
    val excludeTables: Set<String>? = null
)

/**
 * Result of batch table registration
 */
data class BatchTableRegistrationResult(
    val totalRequested: Int,
    val successCount: Int,
    val failedCount: Int,
    val results: List<TableRegistrationResult>
)

/**
 * Column metadata from database
 */
data class ColumnInfo(
    val name: String,
    val sqlType: Int,
    val typeName: String,
    val size: Int,
    val nullable: Boolean,
    val isPrimaryKey: Boolean,
    val defaultValue: String?,
    val remarks: String?
)

/**
 * Metadata for a registered table
 */
data class TableMetadata(
    val name: String,
    val alias: String?,
    val schema: String?,
    val columns: List<ColumnInfo>,
    val registeredAt: Long
) {
    val columnCount: Int get() = columns.size
    val primaryKeys: List<String> get() = columns.filter { it.isPrimaryKey }.map { it.name }
}

/**
 * Information about a discovered (but not necessarily registered) table
 */
data class DiscoveredTable(
    val name: String,
    val schema: String?,
    val isRegistered: Boolean,
    val columnCount: Int
)

