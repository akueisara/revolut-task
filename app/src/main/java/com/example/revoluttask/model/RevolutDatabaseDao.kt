package com.example.revoluttask.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.Deferred

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

    @Query("SELECT * FROM latest_rate_table ORDER BY CASE WHEN currency_rate = 1.0 THEN 0 ELSE 1 END, currency_rate")
    suspend fun getAllRates(): List<Rate>

//    @Query("SELECT * FROM latest_rate_table ORDER BY rateId")
//    fun getAllRates(): List<Rate>
}

