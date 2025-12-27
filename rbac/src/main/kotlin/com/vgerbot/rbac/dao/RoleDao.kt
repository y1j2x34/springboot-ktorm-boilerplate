package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.rbac.model.Role
import com.vgerbot.rbac.model.Roles
import org.springframework.stereotype.Component

@Component
class RoleDao : BaseDao<Role, Roles>(Roles)

