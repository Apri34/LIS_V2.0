package com.lis.lis.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lis.lis.database.daos.CardDao
import com.lis.lis.database.daos.StackDao
import com.lis.lis.database.entities.Card
import com.lis.lis.database.entities.Stack

@Database(entities = [Card::class, Stack::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun stackDao(): StackDao
    abstract fun cardDao(): CardDao

    companion object {
        private var database: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if(database == null) {
                database = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                ).build()
            }
            return database!!
        }
    }
}