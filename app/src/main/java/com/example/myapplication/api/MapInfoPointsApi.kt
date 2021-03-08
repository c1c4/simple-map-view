package com.example.myapplication.api

import com.example.myapplication.model.MapPoints
import retrofit2.Response
import retrofit2.http.GET

interface MapInfoPointsApi {
    @GET("features.json?app_mode=swh-mein-halle-mobil")
    suspend fun getMapPointsInfo(): Response<List<MapPoints>>
}