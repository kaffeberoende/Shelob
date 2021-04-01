package com.rokn.shelob.ui.main.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rokn.shelob.ui.main.ValueType

@Entity
data class Value(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "type") val type: ValueType,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "value") val value: String?,
    @ColumnInfo(name = "created_at") val created_at: Long?
)