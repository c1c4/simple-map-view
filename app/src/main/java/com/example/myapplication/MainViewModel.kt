package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.MapPoints
import com.example.myapplication.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(private val repository: Repository): ViewModel() {

    val myResponse: MutableLiveData<Response<List<MapPoints>>> = MutableLiveData()

    fun getMapInfoPoints() {
        viewModelScope.launch {
            val response = repository.getMapInfoPoints()
            myResponse.value = response
        }
    }
}