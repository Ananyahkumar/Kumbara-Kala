package com.example.kumbarakala.model

import androidx.annotation.DrawableRes

data class Product(
    val id: String,
    val name: String,
    val description: String,
    @DrawableRes val imageResId: Int,
    val healthBenefitTitle: String,
    val healthBenefitDescription: String,
    val ecoBenefitTitle: String,
    val ecoBenefitDescription: String
)
