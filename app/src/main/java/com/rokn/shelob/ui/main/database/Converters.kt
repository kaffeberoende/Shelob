package com.rokn.shelob.ui.main.database

import androidx.room.TypeConverter
import com.rokn.shelob.ui.main.ValueType

class Converters {
    @TypeConverter
    fun toValueType(value: String) = enumValueOf<ValueType>(value)

    @TypeConverter
    fun fromValueType(value: ValueType) = value.name
}