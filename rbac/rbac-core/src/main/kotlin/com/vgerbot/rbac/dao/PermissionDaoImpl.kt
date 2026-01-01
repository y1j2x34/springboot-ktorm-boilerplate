package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.rbac.entity.Permission
import com.vgerbot.rbac.entity.Permissions
import org.springframework.stereotype.Repository

@Repository
class PermissionDaoImpl : AuditableDaoImpl<Permission, Permissions>(Permissions), PermissionDao


