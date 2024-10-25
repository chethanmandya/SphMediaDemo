package com.sphmedia.data.model

import androidx.room.Entity

@Entity(tableName = "brewery_cache", primaryKeys = ["pageNumber", "breweryType"])
data class BreweryCache(
    val pageNumber: Int,
    val breweryType: String,
    val lastUpdated: Long
)
