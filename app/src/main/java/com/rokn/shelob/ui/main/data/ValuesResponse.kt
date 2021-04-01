package com.rokn.shelob.ui.main.data

import com.rokn.shelob.ui.main.database.Value

data class ValuesResponse(
    val next: String?,
    val previous: String?,
    val count: Boolean?,
    val results: List<Value>?)

