package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.StatusAuditableDaoImpl
import com.vgerbot.rbac.entity.Permission
import com.vgerbot.rbac.entity.Permissions
import org.springframework.stereotype.Repository

@Repository
class PermissionDaoImpl : StatusAuditableDaoImpl<Permission, Permissions>(Permissions), PermissionDao


