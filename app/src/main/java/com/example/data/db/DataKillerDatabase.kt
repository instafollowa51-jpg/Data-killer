package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StressSessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DataKillerDatabase : RoomDatabase() {
    abstract fun stressSessionDao(): StressSessionDao

    companion object {
        @Volatile
        private var INSTANCE: DataKillerDatabase? = null

        fun getDatabase(context: Context): DataKillerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataKillerDatabase::class.java,
                    "data_killer_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
