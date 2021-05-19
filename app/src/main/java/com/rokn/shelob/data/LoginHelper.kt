package com.rokn.shelob.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.google.gson.Gson
import com.rokn.shelob.R
import com.rokn.shelob.graphview.GraphViewModel
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object LoginHelper {
    const val TAG = "SPINDEL LOGIN"
    var token: String? = null

    fun login(context: Context): Boolean {
        Log.d(TAG, "login: ")
        val apiKey = context.getSharedPreferences(GraphViewModel.SHARED_PREFS, Context.MODE_PRIVATE).getString(
            GraphViewModel.API_KEY, null
        )
        if (apiKey != null) {
            val client = OkHttpClient().newBuilder()
                .build()
            val mediaType: MediaType = "text/plain".toMediaType()
            val body: RequestBody = "".toRequestBody(mediaType)
            val request = Request.Builder()
                .url(GraphViewModel.TOKEN_URL)
                .method("POST", body)
                .addHeader(GraphViewModel.API_KEY_HEADER, apiKey)
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let {
                    val gson = Gson()
                    token =
                        gson.fromJson<TokenResponse>(
                            it, TokenResponse::class.java
                        ).token ?: ""
                    context.getSharedPreferences(GraphViewModel.SHARED_PREFS, Context.MODE_PRIVATE)
                        .edit(commit = true) {
                            putString(GraphViewModel.TOKEN, token)
                        }
                    return true

                }

            } else {
                Log.d(TAG, "login: failed to login")

            }
        } else {
            Toast.makeText(context, R.string.no_api_key_set, Toast.LENGTH_SHORT).show()
        }
        return false
    }

}