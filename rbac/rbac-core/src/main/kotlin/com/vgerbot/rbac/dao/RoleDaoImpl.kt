package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.rbac.entity.Role
import com.vgerbot.rbac.entity.Roles
import org.springframework.stereotype.Repository

@Repository
class RoleDaoImpl : AbstractBaseDao<Role, Roles>(Roles), RoleDao

