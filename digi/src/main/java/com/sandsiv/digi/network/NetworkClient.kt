package com.sandsiv.digi.network

import com.sandsiv.digi.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY

class NetworkClient {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor().apply { setLevel(BODY) }
                addInterceptor(loggingInterceptor)
            }
        }
        .build()

    fun head(url: String, callback: okhttp3.Callback) {
        val request = Request.Builder()
            .url(url)
            .head()
            .build()
        client.newCall(request).enqueue(callback)
    }

    fun get(url: String, callback: okhttp3.Callback) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        client.newCall(request).enqueue(callback)
    }
}