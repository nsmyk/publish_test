package com.sandsiv.digi.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

//import retrofit2.Callback
//import retrofit2.Retrofit
//import retrofit2.converter.scalars.ScalarsConverterFactory

// It's required to add a base url to the retrofit builder, so we've added the "localhost" like a placeholder
private const val BASE_URL = "http://localhost/"

class NetworkClient {

    //    private val service: NetworkService
    private val service: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor()
                .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        )
        .build()


    init {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .build()
//        service = retrofit.create(NetworkService::class.java)
    }

//    fun testHead(url: String, callback: Callback<Void>) {
//        val call = service.headScript(url)
//        call.enqueue(callback)
//    }

    fun head(url: String, callback: okhttp3.Callback) {
        val request = Request.Builder()
            .url(url)
            .head()
            .build()
        service.newCall(request).enqueue(callback)
    }

    fun get(url: String, callback: okhttp3.Callback) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        service.newCall(request).enqueue(callback)
    }

//    fun testGet(url: String, callback: Callback<String>) {
//        val call = service.getScript(url)
//        call.enqueue(callback)
//    }
}