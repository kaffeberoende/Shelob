package com.rokn.shelob.ui.main.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rokn.shelob.ui.main.ValueType

@Dao
interface ValueDao {

    @Query("SELECT * FROM value")
    fun getAll(): List<Value>

    @Query("SELECT * FROM value WHERE type = :type")
    fun getAll(type: ValueType): List<Value>

    @Query("SELECT * FROM value WHERE timestamp > :timestamp")
    fun getAllAfter(timestamp: Long): List<Value>

    @Query("SELECT * FROM value WHERE timestamp > :timestamp AND type = :type")
    fun getAllAfter(timestamp: Long, type: ValueType): List<Value>

    @Query("SELECT * FROM value WHERE timestamp > :start AND timestamp < :end")
    fun getAllBetween(start: Long, end: Long): List<Value>

    @Insert
    fun insert(values: List<Value>)

    @Delete
    fun delete(value: Value)
}