package com.vgerbot.dict.dao

import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.dict.entity.DictData
import com.vgerbot.dict.entity.DictDatas

interface DictDataDao : SoftDeleteDao<DictData, DictDatas>


