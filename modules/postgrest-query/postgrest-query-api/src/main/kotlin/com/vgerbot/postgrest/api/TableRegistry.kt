package com.vgerbot.postgrest.api

import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column

/**
 * 表注册表接口
 * 用于注册和查找可通过 PostgREST 查询的表
 */
interface TableRegistry {
    
    /**
     * 注册表
     * 
     * @param tableName 表名（用于 JSON 配置中的 from 字段）
     * @param table Ktorm Table 对象
     */
    fun registerTable(tableName: String, table: BaseTable<*>)
    
    /**
     * 根据表名查找表
     * 
     * @param tableName 表名
     * @return Ktorm Table 对象，如果未找到返回 null
     */
    fun findTable(tableName: String): BaseTable<*>?
    
    /**
     * 根据表名和列名查找列
     * 
     * @param tableName 表名
     * @param columnName 列名
     * @return Ktorm Column 对象，如果未找到返回 null
     */
    fun findColumn(tableName: String, columnName: String): Column<*>?
    
    /**
     * 获取表的所有列
     * 
     * @param tableName 表名
     * @return 列名到 Column 对象的映射
     */
    fun getColumns(tableName: String): Map<String, Column<*>>?
    
    /**
     * 获取所有已注册的表名
     */
    fun getRegisteredTableNames(): Set<String>
    
    /**
     * 检查表是否已注册
     */
    fun isTableRegistered(tableName: String): Boolean
}

