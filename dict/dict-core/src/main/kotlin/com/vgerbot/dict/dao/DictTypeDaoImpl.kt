package com.vgerbot.dict.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.dict.entity.DictType
import com.vgerbot.dict.entity.DictTypes
import org.springframework.stereotype.Repository

@Repository
class DictTypeDaoImpl : AuditableDaoImpl<DictType, DictTypes>(DictTypes), DictTypeDao


