package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.common.dao.BaseDao
import com.vgerbot.rbac.model.Permission
import com.vgerbot.rbac.model.Permissions
import org.springframework.stereotype.Component

@Component
class PermissionDao : AbstractBaseDao<Permission, Permissions>(Permissions)

