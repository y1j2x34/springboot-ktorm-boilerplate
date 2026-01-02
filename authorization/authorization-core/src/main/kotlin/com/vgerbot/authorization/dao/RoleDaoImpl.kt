package com.vgerbot.authorization.dao

import com.vgerbot.authorization.entity.Role
import com.vgerbot.authorization.entity.Roles
import com.vgerbot.common.dao.StatusAuditableDaoImpl
import org.springframework.stereotype.Repository

@Repository
class RoleDaoImpl : StatusAuditableDaoImpl<Role, Roles>(Roles), RoleDao

