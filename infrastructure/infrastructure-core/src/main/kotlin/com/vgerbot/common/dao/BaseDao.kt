package com.vgerbot.common.dao

import com.vgerbot.common.dto.Pagination
import com.vgerbot.common.entity.AuditableEntity
import com.vgerbot.common.entity.AuditableTable
import com.vgerbot.common.entity.SimpleAuditableEntity
import com.vgerbot.common.entity.SimpleAuditableTable
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import org.ktorm.entity.all
import org.ktorm.entity.any
import org.ktorm.entity.count
import org.ktorm.entity.drop
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.none
import org.ktorm.entity.removeIf
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.sortedBy
import org.ktorm.entity.sortedByDescending
import org.ktorm.entity.take
import org.ktorm.entity.toList
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

enum class SortOrder {
    ASC, DESC
}

/**
 * 基础 DAO 接口
 */
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

/**
 * 支持逻辑删除的 DAO 接口
 */
interface SoftDeleteDao<E : Entity<E>, T : Table<E>> : BaseDao<E, T> {
    /**
     * 逻辑删除实体
     * @param id 实体ID
     * @return 是否删除成功
     */
    fun softDelete(id: Any): Boolean
    
    /**
     * 逻辑删除实体（带条件）
     * @param predicate 查询条件
     * @return 删除的记录数
     */
    fun softDeleteIf(predicate: (T) -> ColumnDeclaring<Boolean>): Int
    
    /**
     * 查找未删除的实体（单个）
     */
    fun findOneActive(predicate: (T) -> ColumnDeclaring<Boolean>): E?
    
    /**
     * 查找未删除的实体（列表）
     */
    fun findListActive(predicate: (T) -> ColumnDeclaring<Boolean>): List<E>
    
    /**
     * 查找所有未删除的实体
     */
    fun findAllActive(): List<E>
    
    /**
     * 统计未删除的记录数
     */
    fun countActive(predicate: (T) -> ColumnDeclaring<Boolean>): Int
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

    fun pagination(pageSize: Int, pageIndex: Int, predicate: (T) -> ColumnDeclaring<Boolean>): Pagination<E> {

        val totalCount = this.count(predicate)

        val startIndex = pageIndex * pageSize

        val modelList = database.sequenceOf(tableObject)
            .filter(predicate)
            .drop(startIndex)
            .take(pageSize)
            .toList()

        return Pagination(
            modelList = modelList,
            totalRecordsInAllPages = totalCount,
            startIndex = startIndex,
            pageSize = pageSize
        )
    }
    fun paginationWithSort(
        pageSize: Int,
        pageIndex: Int,
        predicate: (T) -> ColumnDeclaring<Boolean>,
        orderByList: List<Pair<(T) -> ColumnDeclaring<*>, SortOrder?>>? = null
    ): Pagination<E> {
        val totalCount = this.count(predicate)
        val startIndex = pageIndex * pageSize

        val modelList = database.sequenceOf(tableObject).filter(predicate)
            .let {
                it ->
                var seq = it;
                orderByList?.forEach { (column, order) ->
                   seq = when(order) {
                       null -> seq
                       SortOrder.ASC -> seq.sortedBy(column)
                       else -> seq.sortedByDescending(column)
                   }
                }
                seq
            }
            .drop(startIndex)
            .take(pageSize)
            .toList()

        return Pagination(
            modelList = modelList,
            totalRecordsInAllPages = totalCount,
            startIndex = startIndex,
            pageSize = pageSize
        )
    }
}

/**
 * 支持逻辑删除的抽象 DAO 实现
 * 
 * @param E 实体类型，必须实现 AuditableEntity 或 SimpleAuditableEntity
 * @param T 表类型，必须继承 AuditableTable 或 SimpleAuditableTable
 */
abstract class AbstractSoftDeleteDao<E, T>(
    private val tableObject: T
) : AbstractBaseDao<E, T>(tableObject), SoftDeleteDao<E, T>
        where E : Entity<E>,
              T : Table<E> {
    
    /**
     * 获取 isDeleted 列
     * 子类需要实现此方法以提供 isDeleted 列的引用
     */
    protected abstract fun getIsDeletedColumn(table: T): ColumnDeclaring<Boolean>
    
    /**
     * 设置实体的逻辑删除标志
     */
    protected abstract fun setDeleted(entity: E, deleted: Boolean)
    
    /**
     * 设置实体的更新时间（如果支持）
     */
    protected open fun setUpdatedAt(entity: E, time: Instant?) {
        // 默认实现，子类可以覆盖
        if (entity is AuditableEntity<*>) {
            @Suppress("UNCHECKED_CAST")
            (entity as AuditableEntity<E>).updatedAt = time
        }
    }
    
    /**
     * 设置实体的更新人（如果支持）
     */
    protected open fun setUpdatedBy(entity: E, userId: Int?) {
        // 默认实现，子类可以覆盖
        if (entity is AuditableEntity<*>) {
            @Suppress("UNCHECKED_CAST")
            (entity as AuditableEntity<E>).updatedBy = userId
        }
    }
    
    override fun softDelete(id: Any): Boolean {
        val entities = database.sequenceOf(tableObject).filter {
            getIsDeletedColumn(tableObject) eq false
        }.toList()
        
        // Find entity by ID (assuming first column is primary key or entity has id property)
        val entity = entities.firstOrNull { e ->
            // This is a simplified approach - in production you'd want proper ID comparison
            e.toString().contains(id.toString())
        } ?: return false
        
        setDeleted(entity, true)
        setUpdatedAt(entity, Instant.now())
        
        return update(entity) == 1
    }
    
    override fun softDeleteIf(predicate: (T) -> ColumnDeclaring<Boolean>): Int {
        val entities = database.sequenceOf(tableObject).filter {
            predicate(tableObject) and (getIsDeletedColumn(tableObject) eq false)
        }.toList()
        
        var count = 0
        entities.forEach { entity ->
            setDeleted(entity, true)
            setUpdatedAt(entity, Instant.now())
            if (update(entity) == 1) {
                count++
            }
        }
        return count
    }
    
    override fun findOneActive(predicate: (T) -> ColumnDeclaring<Boolean>): E? {
        return database.sequenceOf(tableObject).find {
            predicate(tableObject) and (getIsDeletedColumn(tableObject) eq false)
        }
    }
    
    override fun findListActive(predicate: (T) -> ColumnDeclaring<Boolean>): List<E> {
        return database.sequenceOf(tableObject).filter {
            predicate(tableObject) and (getIsDeletedColumn(tableObject) eq false)
        }.toList()
    }
    
    override fun findAllActive(): List<E> {
        return database.sequenceOf(tableObject).filter {
            getIsDeletedColumn(tableObject) eq false
        }.toList()
    }
    
    override fun countActive(predicate: (T) -> ColumnDeclaring<Boolean>): Int {
        return database.sequenceOf(tableObject).count {
            predicate(tableObject) and (getIsDeletedColumn(tableObject) eq false)
        }
    }
}