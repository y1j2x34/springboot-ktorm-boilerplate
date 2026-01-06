package com.vgerbot.dynamictable.core

import org.ktorm.schema.*
import org.ktorm.dsl.QueryRowSet
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Dynamic Table - A Table object created at runtime based on database metadata.
 * 
 * This table is not bound to any Entity type, query results are returned as Map<String, Any?>.
 * 
 * Usage:
 * ```kotlin
 * val dynamicTable = DynamicTable("user")
 * dynamicTable.addIntColumn("id", isPrimaryKey = true)
 * dynamicTable.addVarcharColumn("username")
 * dynamicTable.addTimestampColumn("created_at")
 * 
 * // Query using Ktorm DSL
 * database.from(dynamicTable)
 *     .select()
 *     .map { row ->
 *         dynamicTable.columns.associate { col -> col.name to row[col] }
 *     }
 * ```
 * 
 * @param tableName The name of the database table
 * @param schema The schema name (optional)
 * @param catalog The catalog name (optional)
 */
class DynamicTable(
    tableName: String,
    schema: String? = null,
    catalog: String? = null
) : BaseTable<Nothing>(tableName, schema = schema ?: "", catalog = catalog ?: "") {
    
    /**
     * Map of column name to property name (snake_case -> camelCase)
     */
    private val columnPropertyMap = mutableMapOf<String, String>()
    
    /**
     * Map of column name to Column object for quick lookup
     */
    private val columnMap = mutableMapOf<String, Column<*>>()
    
    /**
     * Add an Int type column
     * 
     * @param name Column name in database
     * @param isPrimaryKey Whether this column is a primary key
     * @return The created Column object
     */
    fun addIntColumn(name: String, isPrimaryKey: Boolean = false): Column<Int> {
        val col = int(name)
        val result = if (isPrimaryKey) col.primaryKey() else col
        registerDynamicColumn(name, result)
        return result
    }
    
    /**
     * Add a Long type column
     */
    fun addLongColumn(name: String, isPrimaryKey: Boolean = false): Column<Long> {
        val col = long(name)
        val result = if (isPrimaryKey) col.primaryKey() else col
        registerDynamicColumn(name, result)
        return result
    }
    
    /**
     * Add a String (VARCHAR) type column
     */
    fun addVarcharColumn(name: String, isPrimaryKey: Boolean = false): Column<String> {
        val col = varchar(name)
        val result = if (isPrimaryKey) col.primaryKey() else col
        registerDynamicColumn(name, result)
        return result
    }
    
    /**
     * Add a Text type column (for long text content)
     */
    fun addTextColumn(name: String): Column<String> {
        val col = text(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a Boolean type column
     */
    fun addBooleanColumn(name: String): Column<Boolean> {
        val col = boolean(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a Timestamp type column (maps to java.time.Instant)
     */
    fun addTimestampColumn(name: String): Column<Instant> {
        val col = timestamp(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a DateTime type column (maps to java.time.LocalDateTime)
     */
    fun addDateTimeColumn(name: String): Column<LocalDateTime> {
        val col = datetime(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a Date type column (maps to java.time.LocalDate)
     */
    fun addDateColumn(name: String): Column<LocalDate> {
        val col = date(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a Time type column (maps to java.time.LocalTime)
     */
    fun addTimeColumn(name: String): Column<LocalTime> {
        val col = time(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a Decimal type column (maps to java.math.BigDecimal)
     */
    fun addDecimalColumn(name: String): Column<BigDecimal> {
        val col = decimal(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a Double type column
     */
    fun addDoubleColumn(name: String): Column<Double> {
        val col = double(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a Float type column
     */
    fun addFloatColumn(name: String): Column<Float> {
        val col = float(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Add a Blob/Bytes type column
     */
    fun addBlobColumn(name: String): Column<ByteArray> {
        val col = bytes(name)
        registerDynamicColumn(name, col)
        return col
    }
    
    /**
     * Get a column by name
     * 
     * @param columnName The column name (supports both snake_case and camelCase)
     * @return The Column object, or null if not found
     */
    fun getColumn(columnName: String): Column<*>? {
        return columnMap[columnName] 
            ?: columnMap[columnName.lowercase()]
            ?: columnMap[camelToSnakeCase(columnName)]
    }
    
    /**
     * Get a column by name, throws exception if not found
     * Note: Cannot use 'get' operator due to conflict with parent class BaseTable.get()
     */
    fun column(columnName: String): Column<*> {
        return getColumn(columnName)
            ?: throw IllegalArgumentException("Column '$columnName' not found in table '$tableName'")
    }
    
    /**
     * Check if a column exists
     */
    fun hasColumn(columnName: String): Boolean {
        return getColumn(columnName) != null
    }
    
    /**
     * Get the property name (camelCase) for a column
     */
    fun getPropertyName(columnName: String): String {
        return columnPropertyMap[columnName] ?: snakeToCamelCase(columnName)
    }
    
    /**
     * Get all column names
     */
    fun getColumnNames(): Set<String> {
        return columnMap.keys.toSet()
    }
    
    /**
     * Register a column in the internal maps
     */
    private fun registerDynamicColumn(name: String, column: Column<*>) {
        val propertyName = snakeToCamelCase(name)
        columnPropertyMap[name] = propertyName
        columnMap[name] = column
        columnMap[name.lowercase()] = column
    }
    
    /**
     * Convert snake_case to camelCase
     */
    private fun snakeToCamelCase(snake: String): String {
        val parts = snake.split("_")
        return parts[0].lowercase() + parts.drop(1).joinToString("") { 
            it.lowercase().replaceFirstChar { char -> char.uppercaseChar() } 
        }
    }
    
    /**
     * Convert camelCase to snake_case
     */
    private fun camelToSnakeCase(camel: String): String {
        return camel.replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }
            .lowercase()
    }

    /**
     * DynamicTable doesn't bind to any Entity type, so this method should not be called.
     * This implementation throws an exception to indicate improper usage.
     */
    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean): Nothing {
        throw UnsupportedOperationException("DynamicTable doesn't support entity creation. Use row mapping instead.")
    }
}

