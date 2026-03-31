package com.abdelrahman.accountpromax.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abdelrahman.accountpromax.models.ClientEntity
import com.abdelrahman.accountpromax.models.ProjectEntity
import com.abdelrahman.accountpromax.models.TransactionEntity

@Database(
    entities = [ProjectEntity::class, ClientEntity::class, TransactionEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "account_pro_max.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
