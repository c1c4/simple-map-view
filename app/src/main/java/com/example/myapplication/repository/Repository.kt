package com.example.myapplication.repository

import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.model.MapPoints
import retrofit2.Response

class Repository {
    suspend fun getMapInfoPoints(): Response<List<MapPoints>> {
        return RetrofitInstance.api.getMapPointsInfo()
    }
}