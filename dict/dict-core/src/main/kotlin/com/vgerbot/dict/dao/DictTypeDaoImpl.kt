package com.vgerbot.dict.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.dict.entity.DictType
import com.vgerbot.dict.entity.DictTypes
import org.springframework.stereotype.Repository

@Repository
class DictTypeDaoImpl : AbstractBaseDao<DictType, DictTypes>(DictTypes), DictTypeDao

