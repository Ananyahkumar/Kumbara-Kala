package com.example.kumbarakala.data

import com.example.kumbarakala.R
import com.example.kumbarakala.model.Product

object ProductData {
    val products = listOf(
        Product(
            id = "1",
            name = "Traditional Curd Pot",
            description = "A beautifully crafted clay pot perfect for setting thick, sweet curd.",
            imageResId = R.drawable.curd_pot,
            healthBenefitTitle = "Maintains pH Balance",
            healthBenefitDescription = "Clay is alkaline in nature, which neutralizes the acidity of the milk, leading to sweeter and healthier curd.",
            ecoBenefitTitle = "100% Biodegradable",
            ecoBenefitDescription = "Returns to the earth without leaving a trace, unlike plastic containers."
        ),
        Product(
            id = "2",
            name = "Eco-Friendly Clay Lamp",
            description = "A classic Diya to light up your home with a warm, natural glow.",
            imageResId = R.drawable.clay_lamp,
            healthBenefitTitle = "Purifies the Air",
            healthBenefitDescription = "Lighting lamps with pure oil/ghee in clay diyas can help purify the surrounding air.",
            ecoBenefitTitle = "Sustainable Lighting",
            ecoBenefitDescription = "A perfect, reusable alternative to wax candles that release toxins."
        ),
        Product(
            id = "3",
            name = "Authentic Cooking Pan",
            description = "Traditional clay kadai that enhances the flavor of your curries.",
            imageResId = R.drawable.clay_pan,
            healthBenefitTitle = "Non-Toxic Cooking",
            healthBenefitDescription = "No Teflon, no microplastics. 100% natural cooking surface that retains the food's natural oils and moisture.",
            ecoBenefitTitle = "Low Carbon Footprint",
            ecoBenefitDescription = "Locally sourced and crafted with minimal energy consumption."
        )
    )
}
