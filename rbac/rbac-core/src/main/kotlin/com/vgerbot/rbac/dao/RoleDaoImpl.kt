package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.rbac.model.Role
import com.vgerbot.rbac.model.Roles
import org.springframework.stereotype.Repository

@Repository
class RoleDaoImpl : AbstractBaseDao<Role, Roles>(Roles), RoleDao

