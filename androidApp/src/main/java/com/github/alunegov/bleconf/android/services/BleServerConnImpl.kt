package com.github.alunegov.bleconf.android.services

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.bytes
import com.benasher44.uuid.uuidFrom
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.github.alunegov.bleconf.android.domain.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "BleServerConnImpl"

// код ble-сервиса настройки
internal const val CONF_SERVICE_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f07"
// код ble-характеристики набора активных датчиков (g...Sensors[].enabled кодируется в uint8_t)
internal const val CONF_ENABLED_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f17"
// код ble-характеристики истории изменений (кодируется в строку, только чтение)
internal const val CONF_UPDATES_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f27"
internal const val CONF_TIME_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f37"
internal const val CONF_CONF_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f47"
internal const val CONF_COEFF_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4ff7"

// код ble-сервиса оповещения
internal const val STATES_SERVICE_UUID = "4834cad6-5043-4ed9-8d85-a277e72c8178"  // ref sensor
//internal const val STATES_SENSORx_CH_UUID

// Characteristic User Description
internal const val CUD_DSC_UUID = "00002901-0000-1000-8000-00805f9b34fb"
// Characteristic Presentation Format
//internal const val CPF_DSC_UUID = "00002904-0000-1000-8000-00805f9b34fb"
// Код дескриптора, "говорящего" что у датчика есть коэффициент
internal const val WITHCOEFF_DSC_UUID = "0000c01d-0000-1000-8000-00805f9b34fb"

private val CoeffChr = characteristicOf(
    service = CONF_SERVICE_UUID,
    characteristic = CONF_COEFF_CH_UUID,
)

/**
 * Реализация подключения к серверу (BLE-серверу) через Kable.
 *
 * Коды ble-сервисов/характеристик/дескрипторов и форматы данных задаются в модуле BleConnImpl.cpp в проекте сервера.
 *
 * @property periph Реализация [Peripheral].
 * @property logger Реализация [Logger].
 */
class BleServerConnImpl(
    override val serverName: String,
    private val periph: Peripheral,
    private val logger: Logger? = null,
) : BleServerConn {
    override val coeff: Flow<Float> = periph.observe(CoeffChr).map { it.coeff }

    override suspend fun connect() {
        logger?.d(TAG, "connect")
        // TODO: withTimeoutOrNull
        periph.connect()
    }

    override suspend fun disconnect() {
        logger?.d(TAG, "disconnect")
        withTimeoutOrNull(5000) {
            logger?.d(TAG, "disconnecting...")
            periph.disconnect()
            logger?.d(TAG, "disconnected")
        }
    }

    override suspend fun getSensors(): List<Sensor> {
        logger?.d(TAG, "getSensors")

        val statesService = periph.services?.firstOrNull { it.serviceUuid == uuidFrom(STATES_SERVICE_UUID) } ?: return emptyList()

        val enabledEncRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_ENABLED_CH_UUID))
        logger?.d(TAG, "enabledEnc = " + enabledEncRaw[0].toString())

        val sensors = mutableListOf<Sensor>()

        statesService.characteristics.forEachIndexed { i, it ->
            sensors += loadSensor(it, i, enabledEncRaw)
        }

        return sensors
    }

    override suspend fun getSensor(id: String): Sensor? {
        logger?.d(TAG, "getSensor $id")

        val statesService = periph.services?.firstOrNull { it.serviceUuid == uuidFrom(STATES_SERVICE_UUID) } ?: return null

        val enabledEncRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_ENABLED_CH_UUID))
        logger?.d(TAG, "enabledEnc = " + enabledEncRaw[0].toString())

        val sensorChara = statesService.characteristics.firstOrNull { it.characteristicUuid.toString() == id } ?: return null
        val sensorCharaIndex = statesService.characteristics.indexOf(sensorChara)

        return loadSensor(sensorChara, sensorCharaIndex, enabledEncRaw)
    }

    /**
     * Загружает датчик.
     *
     * @param chara Ble-характеристика датчика.
     * @param charaIndex Индекс ble-характеристики датчика.
     * @param enabledEncRaw "Включенность" датчиков.
     * @return Датчик.
     */
    private suspend fun loadSensor(chara: DiscoveredCharacteristic, charaIndex: Int, enabledEncRaw: ByteArray): Sensor {
        val nameDsc = chara.descriptors.firstOrNull { it.descriptorUuid.toString() == CUD_DSC_UUID }
        val name = if (nameDsc != null) {
            // иногда телефон читает значение несуществующего дескриптора (из кэша, был удалён на сервере) - после этого
            // портятся все последующие чтения
            val nameRaw = periph.read(nameDsc)
            // если нуль-терминатор, то используем номер из характеристики. Нуль-терминатор м.б. из-за ошибок на
            // сервере (длинное имя) или пустого имени.
            if (nameRaw.size == 1 && nameRaw[0].toInt() == 0) {
                extractName(chara.characteristicUuid)
            } else {
                nameRaw.decodeToString()
            }
        } else {
            extractName(chara.characteristicUuid)
        }

        // TODO: unstable index charaIndex
        val enabled = (enabledEncRaw[0].toInt() and (1 shl charaIndex)) != 0

        val stateRaw = periph.read(chara)
        val state = stateRaw[0].toInt()

        val withCoeffDsc = chara.descriptors.firstOrNull { it.descriptorUuid.toString() == WITHCOEFF_DSC_UUID }
        val coeff: Float? = if (withCoeffDsc != null) {
            // не читаем значение дескриптора - там всегда 1
            val coeffRaw = periph.read(CoeffChr)
            coeffRaw.coeff
        } else {
            null
        }

        return Sensor(
            chara.characteristicUuid.toString(),
            name,
            enabled,
            state,
            coeff,
        )
    }

    /**
     * Извлекает имя датчика из кода ble-характеристики.
     *
     * Имя хранится в виде 8-байтного значения в последней части GUID (4834cad6-5043-4ed9-0000-000000000001 -> 1). По
     * аналогии с функцией BleApp::Shared::BleHelper::ExtractId в проекте сервера.
     */
    private fun extractName(id: Uuid): String {
        // TODO: ByteOrder.LITTLE_ENDIAN?
        return ByteBuffer.wrap(id.bytes).getLong(8).toString()
    }

    override suspend fun setSensorsEnability(sensors: List<Sensor>) {
        logger?.d(TAG, "setSensorsEnability")

        var enabledEnc = 0
        sensors.forEachIndexed { i, it ->
            // TODO: unstable index i
            if (it.enabled) enabledEnc = enabledEnc or (1 shl i)
        }

        periph.write(characteristicOf(CONF_SERVICE_UUID, CONF_ENABLED_CH_UUID), byteArrayOf(enabledEnc.toByte()), WriteType.WithResponse)
    }

    override suspend fun getHistory(): List<HistoryEvent> {
        logger?.d(TAG, "getHistory")

        val historyEncRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_UPDATES_CH_UUID))
        logger?.d(TAG, "historyEnc = " + historyEncRaw.decodeToString())

        return decodeHistory(historyEncRaw)
    }

    /**
     * Раскодирует закодированную строку с историей. Кодирование происходит в функции BleApp::Server::Domain::CoreImpl::GetUpdates
     * на сервере. Пример сторки: "time: 587093, en: 193; time: 587088, en: 225; time: 518671, en: 193; time: 518670, en: 195; time: 511483, en: 193;".
     */
    private fun decodeHistory(historyEncRaw: ByteArray): List<HistoryEvent> {
        val asStr = historyEncRaw.decodeToString()
        val events = mutableListOf<HistoryEvent>()
        val eventsAsStr = asStr.split(';')
        for (eventAsStr in eventsAsStr) {
            val propsAsStr = eventAsStr.split(',')
            if (propsAsStr.size != 2) break  // last one

            // TODO: check bounds
            val time = propsAsStr[0].split(':')[1].trim().toLong()
            val en = propsAsStr[1].split(':')[1].trim().toInt()

            events += HistoryEvent(time, en)
        }
        return events
    }

    override suspend fun getConf(): Conf {
        logger?.d(TAG, "getConf")

        val confRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_CONF_CH_UUID))

        return ByteBuffer.wrap(confRaw).order(ByteOrder.LITTLE_ENDIAN).let {
            val adcCoeff = it.getFloat()
            val adcEmonNum = it.getInt()
            val adcAverNum = it.getInt()
            val adcImbaNum = it.getInt()
            val adcImbaMinCurrent = it.getFloat()
            val adcImbaMinSwing = it.getFloat()
            val adcImbaThreshold = it.getFloat()

            Conf(adcCoeff, adcEmonNum, adcAverNum, adcImbaNum, adcImbaMinCurrent, adcImbaMinSwing, adcImbaThreshold)
        }
    }

    override suspend fun setConf(conf: Conf) {
        logger?.d(TAG, "setConf")

        val confRaw = ByteBuffer.allocate(4 + 4 + 4 + 4 + 4 + 4 + 4).order(ByteOrder.LITTLE_ENDIAN)
            .putFloat(conf.adcCoeff)
            .putInt(conf.adcEmonNum)
            .putInt(conf.adcAverNum)
            .putInt(conf.adcImbaNum)
            .putFloat(conf.adcImbaMinCurrent)
            .putFloat(conf.adcImbaMinSwing)
            .putFloat(conf.adcImbaThreshold)
            .array()

        periph.write(characteristicOf(CONF_SERVICE_UUID, CONF_CONF_CH_UUID), confRaw, WriteType.WithResponse)
    }

    override suspend fun getTime(): Long {
        logger?.d(TAG, "getTime")

        val timeRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_TIME_CH_UUID))

        return ByteBuffer.wrap(timeRaw).order(ByteOrder.LITTLE_ENDIAN).getLong()
    }

    override suspend fun setTime(time: Long) {
        logger?.d(TAG, "setTime to $time")

        val timeRaw = ByteBuffer.allocate(java.lang.Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(time).array()

        periph.write(characteristicOf(CONF_SERVICE_UUID, CONF_TIME_CH_UUID), timeRaw, WriteType.WithResponse)
    }
}

/**
 * Извлекает коэффициент.
 */
private inline val ByteArray.coeff: Float
    get() = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).getFloat()
