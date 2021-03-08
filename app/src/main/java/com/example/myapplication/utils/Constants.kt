package com.example.myapplication.utils

class Constants {
    companion object{
        const val BASE_URL = "https://midgard.netzmap.com"
        const val GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID"
        const val MARKER_IMAGE_ID = "MARKER_IMAGE_ID"
        const val MARKER_LAYER_ID = "MARKER_LAYER_ID"
        const val CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID"
        const val ID_SELECTED = "id"
        const val TITLE_SELECTED = "title"
        const val SUBTITLE_SELECTED = "subtitle"
        const val DESCRIPTION_SELECTED = "description"

        fun treatNullValue(value: String?): String {
            return value ?: ""
        }
    }


}