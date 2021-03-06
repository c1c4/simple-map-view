package com.example.myapplication.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

private const val BASE_URL = "https://midgard.netzmap.com"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .build()

interface MapApiService {
    @GET("features.json?app_mode=swh-mein-halle-mobil")
    fun getInformation(): Call<String>
}

object MapApi {
    val retrofitService: MapApiService by lazy {
        retrofit.create(MapApiService::class.java)
    }
}
