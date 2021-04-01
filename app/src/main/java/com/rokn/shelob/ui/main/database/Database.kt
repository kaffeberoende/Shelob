package com.rokn.shelob.ui.main.database

import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.TypeConverters

@Database(entities = [Value::class], version = 1)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun valueDao(): ValueDao
}