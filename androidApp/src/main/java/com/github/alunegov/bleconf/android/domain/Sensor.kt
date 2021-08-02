package com.github.alunegov.bleconf.android.domain

/**
 * Описание датчика на сервере.
 *
 * @property id Id датчика.
 * @property name Имя датчика.
 * @property enabled Флаг: Активный датчик.
 * @property state Состояние датчика (0/1/2).
 * @property coeff Коэффициент (только у adc-датчика).
  */
data class Sensor(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val state: Int,
    val coeff: Float? = null,
)
