package com.vgerbot.authorization.dao

import com.vgerbot.authorization.entity.Permission
import com.vgerbot.authorization.entity.Permissions
import com.vgerbot.common.dao.StatusAuditableDaoImpl
import org.springframework.stereotype.Repository

@Repository
class PermissionDaoImpl : StatusAuditableDaoImpl<Permission, Permissions>(Permissions), PermissionDao

