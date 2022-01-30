package com.github.alunegov.bleconf.android.domain

/**
 * Описание датчика на сервере.
 *
 * @property id Id датчика.
 * @property name Имя датчика.
 * @property enabled Флаг: Активный датчик.
 * @property state Состояние датчика (0/1/2/4(0+4)-таймаут/5(1+4)-таймаут).
 * @property coeff Коэффициент (только у adc-датчика).
 * @property isRelay Флаг: Реле (не датчик).
 * @property enabledEncIndex Индекс датчика при кодировании активности датчиков.
  */
data class Sensor(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val state: Int,
    val coeff: Float?,
    val isRelay: Boolean,
    val enabledEncIndex: Int,
)
