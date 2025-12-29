package com.vgerbot.common.dao

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import org.ktorm.entity.all
import org.ktorm.entity.any
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.none
import org.ktorm.entity.removeIf
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table
import org.springframework.beans.factory.annotation.Autowired

interface BaseDao<E: Entity<E>, T: Table<E>> {
    fun add(entity: E): Int
    fun update(entity: E): Int
    fun deleteIf(predicate: (T) -> ColumnDeclaring<Boolean>): Int
    fun allMatched(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean
    fun anyMatched(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean
    fun noneMatched(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean
    /**
     * Return the number of records in the table.
     */
    fun count(): Int

    /**
     * Return the number of records matching the given [predicate] in the table.
     */
    fun count(predicate: (T) -> ColumnDeclaring<Boolean>): Int

    /**
     * Return an entity object matching the given [predicate].
     */
    fun findOne(predicate: (T) -> ColumnDeclaring<Boolean>): E?

    /**
     * Return a list of entities matching the given [predicate].
     */
    fun findList(predicate: (T) -> ColumnDeclaring<Boolean>): List<E>

    fun findAll(): List<E>
}

abstract class AbstractBaseDao<E : Entity<E>, T : Table<E>>(private val tableObject: T) : BaseDao<E, T> {
    @Autowired
    protected lateinit var database: Database

    /**
     * Insert the given entity into the table and return the effected record number.
     */
    override fun add(entity: E): Int {
        return database.sequenceOf(tableObject).add(entity)
    }

    /**
     * Update properties of the given entity to the table and return the effected record number.
     */
    override fun update(entity: E): Int {
        return database.sequenceOf(tableObject).update(entity)
    }

    /**
     * Delete records that satisfy the given [predicate].
     */
    override fun deleteIf(predicate: (T) -> ColumnDeclaring<Boolean>): Int {
        return database.sequenceOf(tableObject).removeIf(predicate)
    }

    /**
     * Return true if all records match the given [predicate].
     */
    override fun allMatched(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean {
        return database.sequenceOf(tableObject).all(predicate)
    }

    /**
     * Return true if at least one record match the given [predicate].
     */
    override fun anyMatched(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean {
        return database.sequenceOf(tableObject).any(predicate)
    }

    /**
     * Return true if no records match the given [predicate].
     */
    override fun noneMatched(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean {
        return database.sequenceOf(tableObject).none(predicate)
    }

    /**
     * Return the number of records in the table.
     */
    override fun count(): Int {
        return database.sequenceOf(tableObject).count()
    }

    /**
     * Return the number of records matching the given [predicate] in the table.
     */
    override fun count(predicate: (T) -> ColumnDeclaring<Boolean>): Int {
        return database.sequenceOf(tableObject).count(predicate)
    }

    /**
     * Return an entity object matching the given [predicate].
     */
    override fun findOne(predicate: (T) -> ColumnDeclaring<Boolean>): E? {
        return database.sequenceOf(tableObject).find(predicate)
    }

    /**
     * Return a list of entities matching the given [predicate].
     */
    override fun findList(predicate: (T) -> ColumnDeclaring<Boolean>): List<E> {
        return database.sequenceOf(tableObject).filter(predicate).toList()
    }

    /**
     * Return all entities in the table.
     */
    override fun findAll(): List<E> {
        return database.sequenceOf(tableObject).toList()
    }
}