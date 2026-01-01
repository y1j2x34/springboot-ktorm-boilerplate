package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.StatusAuditableDaoImpl
import com.vgerbot.rbac.entity.Role
import com.vgerbot.rbac.entity.Roles
import org.springframework.stereotype.Repository

@Repository
class RoleDaoImpl : StatusAuditableDaoImpl<Role, Roles>(Roles), RoleDao


