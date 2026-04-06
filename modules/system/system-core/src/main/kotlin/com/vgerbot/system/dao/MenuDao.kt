package com.vgerbot.system.dao

import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.system.entity.Menu
import com.vgerbot.system.entity.Menus

interface MenuDao : SoftDeleteDao<Menu, Menus>
