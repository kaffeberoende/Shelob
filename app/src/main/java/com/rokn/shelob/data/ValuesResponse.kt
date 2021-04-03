package com.rokn.shelob.data

data class ValuesResponse(
    val next: String?,
    val previous: String?,
    val count: Boolean?,
    val results: List<Value>?)

