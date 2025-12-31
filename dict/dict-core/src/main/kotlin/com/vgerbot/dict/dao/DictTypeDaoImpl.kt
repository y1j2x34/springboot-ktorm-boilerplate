package com.vgerbot.dict.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.dict.model.DictType
import com.vgerbot.dict.model.DictTypes
import org.springframework.stereotype.Repository

@Repository
class DictTypeDaoImpl : AbstractBaseDao<DictType, DictTypes>(DictTypes), DictTypeDao

