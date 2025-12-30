package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.rbac.model.Permission
import com.vgerbot.rbac.model.Permissions
import org.springframework.stereotype.Repository

@Repository
class PermissionDaoImpl : AbstractBaseDao<Permission, Permissions>(Permissions), PermissionDao

