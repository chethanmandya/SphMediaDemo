package com.sphmedia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breweries")
data class Brewery(
    @PrimaryKey val id: String,
    val name: String,
    val brewery_type: String,
    val address_1: String?,
    val address_2: String?,
    val address_3: String?,
    val city: String,
    val state_province: String?,
    val postal_code: String,
    val country: String,
    val longitude: String?,
    val latitude: String?,
    val phone: String?,
    val website_url: String?,
    val state: String,
    val street: String?
)
