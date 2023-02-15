package com.github.alunegov.bleconf.android.domain

/**
 * Системные настройки сервера
 *
 * Аналог структуры BleApp::Server::Services::Conf в проекте сервера.
 */
data class ConfOld(
    val adcCoeff: Float = 1.0f,
    val adcEmonNum: Int = 2048,
    val adcAverNum: Int = 11,
    val adcImbaNum: Int = 30,
    val adcImbaMinCurrent: Float = 0.5f,
    val adcImbaMinSwing: Float = 0.2f,
    val adcImbaThreshold: Float = 0.1f,
    val modbusSlaveAddr: UByte = 1u,
    val modbusBaudrate: UInt = 9600u,
)

open class ConfBase

data class ConfV1(
    val adcCoeff: Float = 1.0f,
    val adcEmonNum: Int = 2048,
    val adcAverNum: Int = 11,
    val adcImbaNum: Int = 30,
    val adcImbaMinCurrent: Float = 0.5f,
    val adcImbaMinSwing: Float = 0.2f,
    val adcImbaThreshold: Float = 0.1f,
) : ConfBase()

data class ConfV2(
    val adcCoeff: Float = 1.0f,
    val adcEmonNum: Int = 2048,
    val adcAverNum: Int = 11,
    val adcImbaNum: Int = 30,
    val adcImbaMinCurrent: Float = 0.5f,
    val adcImbaMinSwing: Float = 0.2f,
    val adcImbaThreshold: Float = 0.1f,
    val modbusSlaveAddr: UByte = 1u,
    val modbusBaudrate: UInt = 9600u,
) : ConfBase()

enum class AdcImbaMode {
    Imba1,
    Imba2,
    Imba3;

    companion object {
        fun fromInt(value: Int) = values().first { it.ordinal == value }
    }
}

data class ConfV3(
    val adcCoeff: Float = 1.0f,
    val adcEmonNum: Int = 2048,
    val adcAverNum: Int = 11,
    val imbaN: Float = 5.0f,
    val adcImbaMinCurrent: Float = 0.5f,
    val adcImbaMinSwing: Float = 0.2f,
    val adcImbaThreshold: Float = 10.0f,
    val imbaMode: AdcImbaMode = AdcImbaMode.Imba1,
    val modbusSlaveAddr: UByte = 1u,
    val modbusBaudrate: UInt = 9600u,
) : ConfBase()
