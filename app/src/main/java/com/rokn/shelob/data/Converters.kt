package com.rokn.shelob.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toValueType(value: String) = enumValueOf<ValueType>(value)

    @TypeConverter
    fun fromValueType(value: ValueType) = value.name
}