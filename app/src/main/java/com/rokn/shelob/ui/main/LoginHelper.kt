package com.rokn.shelob.ui.main

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.google.gson.Gson
import com.rokn.shelob.R
import com.rokn.shelob.ui.main.data.TokenResponse
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object LoginHelper {
    var token: String? = null

    fun login(context: Context): Boolean {
        Log.d(MainViewModel.TAG, "login: ")
        val apiKey = context.getSharedPreferences(MainViewModel.SHARED_PREFS, Context.MODE_PRIVATE).getString(
            MainViewModel.API_KEY, null
        )
        if (apiKey != null) {
            val client = OkHttpClient().newBuilder()
                .build()
            val mediaType: MediaType = "text/plain".toMediaType()
            val body: RequestBody = "".toRequestBody(mediaType)
            val request = Request.Builder()
                .url(MainViewModel.TOKEN_URL)
                .method("POST", body)
                .addHeader(MainViewModel.API_KEY_HEADER, apiKey)
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let {
                    val gson = Gson()
                    token =
                        gson.fromJson<TokenResponse>(
                            it, TokenResponse::class.java
                        ).token ?: ""
                    context.getSharedPreferences(MainViewModel.SHARED_PREFS, Context.MODE_PRIVATE)
                        .edit(commit = true) {
                            putString(MainViewModel.TOKEN, token)
                        }
                    return true

                }

            } else {
                Log.d(MainViewModel.TAG, "login: failed to login")

            }
        } else {
            Toast.makeText(context, R.string.no_api_key_set, Toast.LENGTH_SHORT).show()
        }
        return false
    }
}