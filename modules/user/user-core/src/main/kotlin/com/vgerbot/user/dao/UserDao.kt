package com.vgerbot.user.dao

import com.vgerbot.common.dao.AbstractSoftDeleteDao
import com.vgerbot.common.dao.BaseDao
import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.user.entity.User
import com.vgerbot.user.entity.Users
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.schema.ColumnDeclaring
import org.springframework.stereotype.Repository

interface UserDao: SoftDeleteDao<User, Users> {
    fun findByUsername(username: String): User?
    fun findByExternalIdentity(authProvider: String, externalId: String): User?
}

@Repository
class UserDaoImpl: AbstractSoftDeleteDao<User, Users>(Users), UserDao {
    override fun getIsDeletedColumn(table: Users): ColumnDeclaring<Boolean> = table.isDeleted

    override fun setDeleted(entity: User, deleted: Boolean) {
        entity.isDeleted = deleted
    }

    override fun findByUsername(username: String): User? = findOneActive { it.username eq username }

    override fun findByExternalIdentity(authProvider: String, externalId: String): User? = findOneActive {
        (it.authProvider eq authProvider) and (it.externalId eq externalId)
    }
}
