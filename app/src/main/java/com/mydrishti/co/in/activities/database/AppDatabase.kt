package com.mydrishti.co.`in`.activities.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mydrishti.co.`in`.activities.dao.ChartDao
import com.mydrishti.co.`in`.activities.dao.Converters
import com.mydrishti.co.`in`.activities.dao.ParameterDao
import com.mydrishti.co.`in`.activities.database.converters.DateRangeConverter
import com.mydrishti.co.`in`.activities.database.converters.IntListConverter
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ParameterEntity

@Database(entities = [ChartConfig::class, ParameterEntity::class], version = 4, exportSchema = false)
@TypeConverters(value = [Converters::class, DateRangeConverter::class, IntListConverter::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun chartDao(): ChartDao
    abstract fun parameterDao(): ParameterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_drishti_database_v4" // Change database name to force recreation
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}