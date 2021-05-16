package me.alexander.androidApp.domain

data class Sensor(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val state: Int,
    val coeff: Float? = null,
)
