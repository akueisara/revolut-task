package com.example.revoluttask.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RevolutDatabaseDao {

    @Insert
    fun insert(rate: Rate): Long

    @Update
    fun update(rate: Rate): Int

    @Query("SELECT * from latest_rate_table WHERE currency_name LIKE :code")
    fun get(code: String): Rate?

    @Query("DELETE FROM latest_rate_table")
    fun clearAllRates()

    @Query("SELECT * FROM latest_rate_table ORDER BY CASE WHEN currency_rate = 1.00 THEN 0 ELSE 1 END")
    fun getAllRates(): List<Rate>
}

