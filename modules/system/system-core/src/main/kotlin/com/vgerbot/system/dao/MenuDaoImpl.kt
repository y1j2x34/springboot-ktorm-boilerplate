package com.vgerbot.system.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.system.entity.Menu
import com.vgerbot.system.entity.Menus
import org.springframework.stereotype.Repository

@Repository
class MenuDaoImpl : AuditableDaoImpl<Menu, Menus>(Menus), MenuDao
