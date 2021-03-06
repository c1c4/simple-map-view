package com.example.myapplication.map

import androidx.lifecycle.MutableLiveData
import com.example.myapplication.network.MapApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel {
    private val _response = MutableLiveData<String>()

    private fun getMapInformationProperties() {
        MapApi.retrofitService.getInformation().enqueue(
            object: Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    _response.value = response.body()
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    _response.value = "Failure" + t.message
                }
            }
        )
    }
}