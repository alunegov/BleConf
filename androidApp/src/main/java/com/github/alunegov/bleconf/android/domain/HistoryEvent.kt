package com.github.alunegov.bleconf.android.domain

/**
 * Событие истории сервера (изменение активных датчиков).
 *
 * Аналог структуры BleApp::Server::Services::UpdateData в проекте сервера.
 *
 * @property time Время изменения, UNIX-time.
 * @property sensorsEnability Активные датчики, в виде битовой маски (0b1000000000100001 -> включены 1-ый и 6-ой датчики и реле).
 * @property sensorsMask sensorsMask.
 */
data class HistoryEvent(
    val time: Long,
    val sensorsEnability: Int,
    val sensorsMask: Int,
)

// 8 датчиков
const val SensorsMaskV1 = 0b0000000011111111
const val SensorsMaskV2Pre = 0b1000000011111111
// 7 датчиков (убрали gpio-датчик с сохранением id=8 у adc-датчика) и реле
const val SensorsMaskV2 = 0b1000000010111111
