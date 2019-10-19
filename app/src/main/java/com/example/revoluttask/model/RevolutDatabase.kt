package com.example.revoluttask.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Rate::class], version = 1, exportSchema = false)
abstract class RevolutDatabase : RoomDatabase() {

    abstract val revolutDatabaseDao: RevolutDatabaseDao

    companion object {

        const val DATABASE_NAME = "revolut_database"

        @Volatile
        private var INSTANCE: RevolutDatabase? = null

        fun getInstance(context: Context): RevolutDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RevolutDatabase::class.java,
                        DATABASE_NAME
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}