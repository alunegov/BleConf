package me.alexander.shared.sensors

data class Sensor(
    val name: String,
    val state: Byte,
    val enabled: Boolean,
    val Coeff: Double?,
)
