package me.alexander.androidApp.domain

/**
 * Системные настройки сервера
 */
data class Conf(
    val adcCoeff: Float = 1.0f,
    val adcEmonNum: Int = 2048,
    val adcAverNum: Int = 11,
    val adcImbaNum: Int = 30,
    val adcImbaMinCurrent: Float = 0.5f,
    val adcImbaMinSwing: Float = 0.2f,
    val adcImbaThreshold: Float = 0.1f,
)
