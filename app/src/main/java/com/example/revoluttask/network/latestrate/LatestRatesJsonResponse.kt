package com.example.revoluttask.network.latestrate

data class LatestRatesJsonResponse (
    val base: String,
    val date: String,
    val rates: Rates
)