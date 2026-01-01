package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.rbac.entity.Role
import com.vgerbot.rbac.entity.Roles
import org.springframework.stereotype.Repository

@Repository
class RoleDaoImpl : AuditableDaoImpl<Role, Roles>(Roles), RoleDao


