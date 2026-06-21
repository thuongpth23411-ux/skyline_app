package com.skyline.model

import androidx.annotation.DrawableRes

data class Promotion(
    val title: String,
    val date: String,
    @param:DrawableRes val imageRes: Int
)

data class Destination(
    val country: String,
    val title: String,
    @param:DrawableRes val imageRes: Int
)

data class Experience(
    val tag: String,
    val title: String,
    val description: String,
    @param:DrawableRes val imageRes: Int
)