package com.rokn.shelob.ui.main.data

data class ValuesResponse(
    val next: String?,
    val previous: String?,
    val count: Boolean?,
    val results: List<Value>?)

data class Value(
    val timestamp: Long?,
    val value: String?,
    val created_at: Long)