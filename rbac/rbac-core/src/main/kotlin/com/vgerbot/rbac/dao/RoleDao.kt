package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.rbac.entity.Role
import com.vgerbot.rbac.entity.Roles

interface RoleDao : SoftDeleteDao<Role, Roles>


