package com.example.myapplication.model

data class MapPoints (
    val id: Int,
    val icon: String,
    val title: String,
    val title_en: String,
    val subtitle: String,
    val subtitle_en: String,
    val description: String,
    val description_en: String,
    val position: List<Double>,
    val created_at: String,
    val updated_at: String,
    val type: String,
        )