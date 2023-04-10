package com.vgerbot.app.foundation.dto

data class PageDTO<T>(
    val modelList: List<T>,
    val totalRecordsInAllPages: Int,
    val startIndex: Int,
    val pageSize: Int,
    val pageCount: Int =
        if (totalRecordsInAllPages % pageSize == 0) {
            totalRecordsInAllPages / pageSize
        } else {
            totalRecordsInAllPages / pageSize + 1
        }
)