package com.vgerbot.dynamictable.core

import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.Column

/**
 * Dynamic Entity - A wrapper around Map<String, Any?> that provides convenient access to row data.
 * 
 * This class is used to represent query results from DynamicTable queries,
 * providing type-safe accessors and property-style access to column values.
 * 
 * Usage:
 * ```kotlin
 * // Create from query result
 * val entity = DynamicEntity.fromRow(row, dynamicTable.columns)
 * 
 * // Access values
 * val id = entity.getInt("id")
 * val name = entity.getString("username")
 * val createdAt = entity.getInstant("created_at")
 * 
 * // Or use operator syntax
 * val value = entity["column_name"]
 * ```
 */
class DynamicEntity(
    private val data: MutableMap<String, Any?> = mutableMapOf()
) {
    
    /**
     * Get a value by column name
     */
    operator fun get(key: String): Any? = data[key] ?: data[key.lowercase()]
    
    /**
     * Set a value by column name
     */
    operator fun set(key: String, value: Any?) {
        data[key] = value
    }
    
    /**
     * Check if a column exists
     */
    fun has(key: String): Boolean = data.containsKey(key) || data.containsKey(key.lowercase())
    
    /**
     * Get value as String
     */
    fun getString(key: String): String? = get(key)?.toString()
    
    /**
     * Get value as String with default
     */
    fun getString(key: String, default: String): String = getString(key) ?: default
    
    /**
     * Get value as Int
     */
    fun getInt(key: String): Int? {
        return when (val value = get(key)) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }
    
    /**
     * Get value as Int with default
     */
    fun getInt(key: String, default: Int): Int = getInt(key) ?: default
    
    /**
     * Get value as Long
     */
    fun getLong(key: String): Long? {
        return when (val value = get(key)) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }
    
    /**
     * Get value as Long with default
     */
    fun getLong(key: String, default: Long): Long = getLong(key) ?: default
    
    /**
     * Get value as Double
     */
    fun getDouble(key: String): Double? {
        return when (val value = get(key)) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }
    
    /**
     * Get value as Double with default
     */
    fun getDouble(key: String, default: Double): Double = getDouble(key) ?: default
    
    /**
     * Get value as Boolean
     */
    fun getBoolean(key: String): Boolean? {
        return when (val value = get(key)) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value.lowercase() in listOf("true", "1", "yes")
            else -> null
        }
    }
    
    /**
     * Get value as Boolean with default
     */
    fun getBoolean(key: String, default: Boolean): Boolean = getBoolean(key) ?: default
    
    /**
     * Get value as java.time.Instant
     */
    fun getInstant(key: String): java.time.Instant? {
        return when (val value = get(key)) {
            is java.time.Instant -> value
            is java.sql.Timestamp -> value.toInstant()
            is java.util.Date -> value.toInstant()
            is String -> try { java.time.Instant.parse(value) } catch (e: Exception) { null }
            else -> null
        }
    }
    
    /**
     * Get value as java.time.LocalDateTime
     */
    fun getLocalDateTime(key: String): java.time.LocalDateTime? {
        return when (val value = get(key)) {
            is java.time.LocalDateTime -> value
            is java.sql.Timestamp -> value.toLocalDateTime()
            is String -> try { java.time.LocalDateTime.parse(value) } catch (e: Exception) { null }
            else -> null
        }
    }
    
    /**
     * Get value as java.time.LocalDate
     */
    fun getLocalDate(key: String): java.time.LocalDate? {
        return when (val value = get(key)) {
            is java.time.LocalDate -> value
            is java.sql.Date -> value.toLocalDate()
            is String -> try { java.time.LocalDate.parse(value) } catch (e: Exception) { null }
            else -> null
        }
    }
    
    /**
     * Get value as java.math.BigDecimal
     */
    fun getBigDecimal(key: String): java.math.BigDecimal? {
        return when (val value = get(key)) {
            is java.math.BigDecimal -> value
            is Number -> java.math.BigDecimal(value.toString())
            is String -> try { java.math.BigDecimal(value) } catch (e: Exception) { null }
            else -> null
        }
    }
    
    /**
     * Get all column names
     */
    fun keys(): Set<String> = data.keys
    
    /**
     * Get all values
     */
    fun values(): Collection<Any?> = data.values
    
    /**
     * Convert to immutable Map
     */
    fun toMap(): Map<String, Any?> = data.toMap()
    
    /**
     * Convert to mutable Map
     */
    fun toMutableMap(): MutableMap<String, Any?> = data.toMutableMap()
    
    /**
     * Get the number of columns
     */
    val size: Int get() = data.size
    
    /**
     * Check if empty
     */
    fun isEmpty(): Boolean = data.isEmpty()
    
    /**
     * Check if not empty
     */
    fun isNotEmpty(): Boolean = data.isNotEmpty()
    
    override fun toString(): String {
        return "DynamicEntity($data)"
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DynamicEntity) return false
        return data == other.data
    }
    
    override fun hashCode(): Int {
        return data.hashCode()
    }
    
    companion object {
        /**
         * Create a DynamicEntity from a QueryRowSet
         * 
         * @param row The query row
         * @param columns The columns to extract
         * @return A new DynamicEntity with the row data
         */
        fun fromRow(row: QueryRowSet, columns: List<Column<*>>): DynamicEntity {
            val data = columns.associate { col -> col.name to row[col] }
            return DynamicEntity(data.toMutableMap())
        }
        
        /**
         * Create a DynamicEntity from a QueryRowSet using a DynamicTable
         * 
         * @param row The query row
         * @param table The DynamicTable
         * @return A new DynamicEntity with the row data
         */
        fun fromRow(row: QueryRowSet, table: DynamicTable): DynamicEntity {
            return fromRow(row, table.columns)
        }
        
        /**
         * Create a DynamicEntity from a Map
         */
        fun fromMap(map: Map<String, Any?>): DynamicEntity {
            return DynamicEntity(map.toMutableMap())
        }
        
        /**
         * Create an empty DynamicEntity
         */
        fun empty(): DynamicEntity {
            return DynamicEntity()
        }
    }
}

/**
 * Extension function to convert QueryRowSet to DynamicEntity
 */
fun QueryRowSet.toDynamicEntity(columns: List<Column<*>>): DynamicEntity {
    return DynamicEntity.fromRow(this, columns)
}

/**
 * Extension function to convert QueryRowSet to DynamicEntity using DynamicTable
 */
fun QueryRowSet.toDynamicEntity(table: DynamicTable): DynamicEntity {
    return DynamicEntity.fromRow(this, table)
}

