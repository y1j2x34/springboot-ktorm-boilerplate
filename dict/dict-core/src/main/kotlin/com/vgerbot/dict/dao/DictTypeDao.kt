package com.vgerbot.dict.dao

import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.dict.entity.DictType
import com.vgerbot.dict.entity.DictTypes

interface DictTypeDao : SoftDeleteDao<DictType, DictTypes>


