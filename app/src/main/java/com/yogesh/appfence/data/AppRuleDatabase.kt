package com.yogesh.appfence.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for AppFence.
 * Single table: app_rules storing per-app Wi-Fi and mobile data access rules.
 */
@Database(entities = [AppRule::class], version = 1, exportSchema = false)
abstract class AppRuleDatabase : RoomDatabase() {

    abstract fun appRuleDao(): AppRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppRuleDatabase? = null

        fun getInstance(context: Context): AppRuleDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppRuleDatabase::class.java,
                    "netguard_lite_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
