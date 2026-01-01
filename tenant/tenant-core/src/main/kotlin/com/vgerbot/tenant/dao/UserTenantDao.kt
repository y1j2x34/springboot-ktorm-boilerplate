package com.vgerbot.tenant.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.common.dao.SimpleAuditableDaoImpl
import com.vgerbot.tenant.entity.UserTenant
import com.vgerbot.tenant.entity.UserTenants
import org.springframework.stereotype.Repository

interface UserTenantDao: BaseDao<UserTenant, UserTenants>

@Repository
class UserTenantDaoImpl: SimpleAuditableDaoImpl<UserTenant, UserTenants>(UserTenants), UserTenantDao

