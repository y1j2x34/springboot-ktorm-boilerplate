package com.vgerbot.dynamictable.core

import com.vgerbot.dynamictable.dto.ColumnInfo
import org.slf4j.LoggerFactory
import java.sql.DatabaseMetaData
import java.sql.Types
import javax.sql.DataSource

/**
 * Dynamic Table Builder - Builds DynamicTable objects from database metadata at runtime.
 * 
 * This builder reads table structure information from the database using JDBC DatabaseMetaData
 * and automatically creates DynamicTable objects with all columns properly typed.
 * 
 * Usage:
 * ```kotlin
 * val builder = DynamicTableBuilder(dataSource)
 * 
 * // Build a single table
 * val userTable = builder.buildFromDatabase("user")
 * 
 * // Build all tables in the database
 * val allTables = builder.buildAllTables()
 * ```
 * 
 * @param dataSource The JDBC DataSource to read metadata from
 */
class DynamicTableBuilder(private val dataSource: DataSource) {
    
    private val logger = LoggerFactory.getLogger(DynamicTableBuilder::class.java)
    
    /**
     * Build a DynamicTable from database metadata for a specific table
     * 
     * @param tableName The name of the table to build
     * @param schema The schema name (optional, uses default schema if not specified)
     * @return A DynamicTable with all columns from the database table
     * @throws IllegalArgumentException if the table doesn't exist or has no columns
     */
    fun buildFromDatabase(tableName: String, schema: String? = null): DynamicTable {
        dataSource.connection.use { conn ->
            val metaData = conn.metaData
            val catalog = conn.catalog
            val actualSchema = schema ?: getDefaultSchema(metaData)
            
            logger.debug("Building dynamic table for '{}' in schema '{}'", tableName, actualSchema)
            
            val dynamicTable = DynamicTable(tableName, actualSchema, catalog)
            
            // Get primary key columns
            val primaryKeys = mutableSetOf<String>()
            metaData.getPrimaryKeys(catalog, actualSchema, tableName).use { rs ->
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"))
                }
            }
            
            // Get and add all columns
            var columnCount = 0
            metaData.getColumns(catalog, actualSchema, tableName, null).use { rs ->
                while (rs.next()) {
                    val columnName = rs.getString("COLUMN_NAME")
                    val dataType = rs.getInt("DATA_TYPE")
                    val typeName = rs.getString("TYPE_NAME")
                    val isPK = primaryKeys.contains(columnName)
                    
                    addColumnByType(dynamicTable, columnName, dataType, typeName, isPK)
                    columnCount++
                }
            }
            
            if (columnCount == 0) {
                throw IllegalArgumentException("Table '$tableName' not found or has no columns in schema '$actualSchema'")
            }
            
            logger.info("Built dynamic table '{}' with {} columns, primary keys: {}", 
                tableName, columnCount, primaryKeys)
            
            return dynamicTable
        }
    }
    
    /**
     * Build a DynamicTable if the table exists, returns null otherwise
     * 
     * @param tableName The name of the table to build
     * @param schema The schema name (optional)
     * @return A DynamicTable, or null if the table doesn't exist
     */
    fun buildFromDatabaseOrNull(tableName: String, schema: String? = null): DynamicTable? {
        return try {
            buildFromDatabase(tableName, schema)
        } catch (e: IllegalArgumentException) {
            logger.debug("Table '{}' not found: {}", tableName, e.message)
            null
        }
    }
    
    /**
     * Get all table names in the database
     * 
     * @param schema The schema name (optional)
     * @return List of table names
     */
    fun getAllTableNames(schema: String? = null): List<String> {
        dataSource.connection.use { conn ->
            val metaData = conn.metaData
            val catalog = conn.catalog
            val actualSchema = schema ?: getDefaultSchema(metaData)
            
            val tables = mutableListOf<String>()
            metaData.getTables(catalog, actualSchema, null, arrayOf("TABLE")).use { rs ->
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"))
                }
            }
            
            logger.debug("Found {} tables in schema '{}'", tables.size, actualSchema)
            return tables
        }
    }
    
    /**
     * Build DynamicTable objects for all tables in the database
     * 
     * @param schema The schema name (optional)
     * @return Map of table name to DynamicTable
     */
    fun buildAllTables(schema: String? = null): Map<String, DynamicTable> {
        val tableNames = getAllTableNames(schema)
        val tables = mutableMapOf<String, DynamicTable>()
        
        for (tableName in tableNames) {
            try {
                tables[tableName] = buildFromDatabase(tableName, schema)
            } catch (e: Exception) {
                logger.warn("Failed to build dynamic table for '{}': {}", tableName, e.message)
            }
        }
        
        logger.info("Built {} dynamic tables", tables.size)
        return tables
    }
    
    /**
     * Build DynamicTable objects for specified tables
     * 
     * @param tableNames List of table names to build
     * @param schema The schema name (optional)
     * @return Map of table name to DynamicTable
     */
    fun buildTables(tableNames: List<String>, schema: String? = null): Map<String, DynamicTable> {
        return tableNames.mapNotNull { tableName ->
            try {
                tableName to buildFromDatabase(tableName, schema)
            } catch (e: Exception) {
                logger.warn("Failed to build dynamic table for '{}': {}", tableName, e.message)
                null
            }
        }.toMap()
    }
    
    /**
     * Check if a table exists in the database
     */
    fun tableExists(tableName: String, schema: String? = null): Boolean {
        dataSource.connection.use { conn ->
            val metaData = conn.metaData
            val catalog = conn.catalog
            val actualSchema = schema ?: getDefaultSchema(metaData)
            
            metaData.getTables(catalog, actualSchema, tableName, arrayOf("TABLE")).use { rs ->
                return rs.next()
            }
        }
    }
    
    /**
     * Get column information for a table (useful for debugging)
     */
    fun getColumnInfo(tableName: String, schema: String? = null): List<ColumnInfo> {
        dataSource.connection.use { conn ->
            val metaData = conn.metaData
            val catalog = conn.catalog
            val actualSchema = schema ?: getDefaultSchema(metaData)
            
            val primaryKeys = mutableSetOf<String>()
            metaData.getPrimaryKeys(catalog, actualSchema, tableName).use { rs ->
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"))
                }
            }
            
            val columns = mutableListOf<ColumnInfo>()
            metaData.getColumns(catalog, actualSchema, tableName, null).use { rs ->
                while (rs.next()) {
                    columns.add(ColumnInfo(
                        name = rs.getString("COLUMN_NAME"),
                        sqlType = rs.getInt("DATA_TYPE"),
                        typeName = rs.getString("TYPE_NAME"),
                        size = rs.getInt("COLUMN_SIZE"),
                        nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                        isPrimaryKey = primaryKeys.contains(rs.getString("COLUMN_NAME")),
                        defaultValue = rs.getString("COLUMN_DEF"),
                        remarks = rs.getString("REMARKS")
                    ))
                }
            }
            return columns
        }
    }
    
    /**
     * Get the default schema from the connection
     */
    private fun getDefaultSchema(metaData: DatabaseMetaData): String? {
        return try {
            metaData.connection.schema
        } catch (e: Exception) {
            logger.debug("Could not get default schema: {}", e.message)
            null
        }
    }
    
    /**
     * Add a column to the DynamicTable based on SQL type
     */
    private fun addColumnByType(
        table: DynamicTable, 
        columnName: String, 
        sqlType: Int, 
        typeName: String,
        isPrimaryKey: Boolean
    ) {
        when (sqlType) {
            // Integer types
            Types.INTEGER, Types.SMALLINT, Types.TINYINT -> 
                table.addIntColumn(columnName, isPrimaryKey)
            
            // Long/BigInt type
            Types.BIGINT -> 
                table.addLongColumn(columnName, isPrimaryKey)
            
            // String types
            Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR, 
            Types.NVARCHAR, Types.NCHAR, Types.LONGNVARCHAR -> 
                table.addVarcharColumn(columnName, isPrimaryKey)
            
            // Text/CLOB types
            Types.CLOB, Types.NCLOB -> 
                table.addTextColumn(columnName)
            
            // Boolean type
            Types.BOOLEAN, Types.BIT -> 
                table.addBooleanColumn(columnName)
            
            // Timestamp types
            Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> 
                table.addTimestampColumn(columnName)
            
            // Date type
            Types.DATE -> 
                table.addDateColumn(columnName)
            
            // Time types
            Types.TIME, Types.TIME_WITH_TIMEZONE -> 
                table.addTimeColumn(columnName)
            
            // Decimal/Numeric types
            Types.DECIMAL, Types.NUMERIC -> 
                table.addDecimalColumn(columnName)
            
            // Double type
            Types.DOUBLE -> 
                table.addDoubleColumn(columnName)
            
            // Float/Real type
            Types.REAL, Types.FLOAT -> 
                table.addFloatColumn(columnName)
            
            // Binary/Blob types
            Types.BLOB, Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> 
                table.addBlobColumn(columnName)
            
            // JSON type (treat as varchar/text)
            Types.OTHER -> {
                // Check type name for JSON support
                if (typeName.uppercase().contains("JSON")) {
                    table.addTextColumn(columnName)
                } else {
                    logger.warn("Unknown SQL type {} ({}) for column '{}', defaulting to VARCHAR", 
                        sqlType, typeName, columnName)
                    table.addVarcharColumn(columnName, isPrimaryKey)
                }
            }
            
            // Default to VARCHAR for unknown types
            else -> {
                logger.warn("Unhandled SQL type {} ({}) for column '{}', defaulting to VARCHAR", 
                    sqlType, typeName, columnName)
                table.addVarcharColumn(columnName, isPrimaryKey)
            }
        }
    }
}

