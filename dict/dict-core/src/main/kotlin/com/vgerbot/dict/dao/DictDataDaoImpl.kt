package com.vgerbot.dict.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.dict.entity.DictData
import com.vgerbot.dict.entity.DictDatas
import org.springframework.stereotype.Repository

@Repository
class DictDataDaoImpl : AuditableDaoImpl<DictData, DictDatas>(DictDatas), DictDataDao


