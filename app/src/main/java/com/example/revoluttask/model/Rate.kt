package com.example.revoluttask.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "latest_rate_table")
data class Rate(
    @PrimaryKey(autoGenerate = true)
    val rateId: Long = 0L,
    @ColumnInfo(name = "currency_name")
    val code: String,
    @ColumnInfo(name = "currency_rate")
    var rate: Double,
    @ColumnInfo(name = "flag_image_res_id")
    val flagImageResId: Int
)
