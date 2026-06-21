package com.skyline.model

import androidx.annotation.DrawableRes

data class Promotion(
    val title: String,
    val date: String,
    @DrawableRes val imageRes: Int
)

data class Destination(
    val country: String,
    val title: String,
    @DrawableRes val imageRes: Int
)

data class Experience(
    val tag: String,
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int
)