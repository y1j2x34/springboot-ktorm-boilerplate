package com.vgerbot.dict.dao

import com.vgerbot.common.dao.StatusAuditableDaoImpl
import com.vgerbot.dict.entity.DictType
import com.vgerbot.dict.entity.DictTypes
import org.springframework.stereotype.Repository

@Repository
class DictTypeDaoImpl : StatusAuditableDaoImpl<DictType, DictTypes>(DictTypes), DictTypeDao


