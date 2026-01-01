package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.rbac.entity.Permission
import com.vgerbot.rbac.entity.Permissions

interface PermissionDao : SoftDeleteDao<Permission, Permissions>


