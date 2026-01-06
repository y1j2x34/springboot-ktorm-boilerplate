package com.vgerbot.dict.dao

import com.vgerbot.common.dao.StatusAuditableDaoImpl
import com.vgerbot.dict.entity.DictData
import com.vgerbot.dict.entity.DictDatas
import org.springframework.stereotype.Repository

@Repository
class DictDataDaoImpl : StatusAuditableDaoImpl<DictData, DictDatas>(DictDatas), DictDataDao


