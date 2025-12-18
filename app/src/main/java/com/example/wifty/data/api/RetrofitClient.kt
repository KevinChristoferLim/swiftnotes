package com.example.wifty.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 is the special alias to your host loopback interface (localhost on your development machine)
    // Updated port to 3000 to match your backend server logs
    private const val BASE_URL = "http://10.0.2.2:3000/api/" 

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
