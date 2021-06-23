package me.alexander.androidApp.services

import com.benasher44.uuid.uuidFrom
import com.juul.kable.*
import kotlinx.coroutines.runBlocking
import me.alexander.androidApp.domain.Conf
import me.alexander.androidApp.domain.Sensor
import org.mockito.kotlin.*
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

internal const val STATES_CHANN1_CH_UUID = "4834cad6-5043-4ed9-0000-000000000001"

class BleServerConnImplTest {
    private val periphMock = mock<Peripheral>()
    private val sut = BleServerConnImpl("dummy", periphMock)

    @Test
    fun getSensorsTest() {
        val service1Ch1Mock = mock<DiscoveredCharacteristic> {
            on { serviceUuid } doReturn uuidFrom(STATES_SERVICE_UUID)
            on { characteristicUuid } doReturn uuidFrom(STATES_CHANN1_CH_UUID)
        }

        val service1Mock = mock<DiscoveredService> {
            on { serviceUuid } doReturn uuidFrom(STATES_SERVICE_UUID)
            on { characteristics } doReturn listOf(
                service1Ch1Mock,
            )
        }

        val service2Mock = mock<DiscoveredService> {
            on { serviceUuid } doReturn uuidFrom(CONF_SERVICE_UUID)
            on { characteristics } doReturn listOf()
        }

        periphMock.stub {
            on { services } doReturn listOf(
                service1Mock,
                service2Mock,
            )
            onBlocking {
                read(argThat<Characteristic> { serviceUuid == uuidFrom(CONF_SERVICE_UUID) } )
            } doReturn byteArrayOf(0b11000001.toByte())
            onBlocking {
                read(argThat<Characteristic> { serviceUuid == uuidFrom(STATES_SERVICE_UUID) } )
            } doReturn byteArrayOf(2)
        }

        val sensors = runBlocking {
            sut.getSensors()
        }

        assertEquals(1, sensors.size)
        assertEquals(STATES_CHANN1_CH_UUID, sensors[0].id)
        assertEquals("1", sensors[0].name)
        assertEquals(true, sensors[0].enabled)
        assertEquals(2, sensors[0].state)
        assertEquals(0.0f, sensors[0].coeff ?: 0.0f)
    }

    @Test
    fun setSensorsEnabilityTest() {
        val sensors = listOf<Sensor>(
            Sensor("", "", true, 0),
            Sensor("", "", false, 0),
            Sensor("", "", false, 0),
            Sensor("", "", false, 0),
            Sensor("", "", false, 0),
            Sensor("", "", false, 0),
            Sensor("", "", true, 0),
            Sensor("", "", true, 0),
        )

        runBlocking {
            sut.setSensorsEnability(sensors)
        }

        verifyBlocking(periphMock) {
            write(
                any(),
                argThat { this contentEquals byteArrayOf(0b11000001.toByte()) },
                any(),
            )
        }
    }

    @Test
    fun getHistoryTest() {
        periphMock.stub {
            onBlocking { read(any<Characteristic>()) } doReturn "time:1,en:255;".toByteArray()
        }

        val history = runBlocking {
            sut.getHistory()
        }

        assertEquals(1, history.size)
        assertEquals(1, history[0].time)
        assertEquals(255, history[0].en)
    }

    @Test
    fun getConfTest() {
        val testTime = System.currentTimeMillis() / 1000
        val testTimeRaw = ByteBuffer.allocate(java.lang.Long.BYTES).putLong(testTime).array().reversedArray()

        periphMock.stub {
            onBlocking {
                read(argThat<Characteristic> { characteristicUuid == uuidFrom(CONF_TIME_CH_UUID) } )
            } doReturn testTimeRaw
        }

        val conf = runBlocking {
            sut.getConf()
        }

        assertEquals(testTime, conf.time)
    }

    @Test
    fun setTimeTest() {
        val testTime = System.currentTimeMillis() / 1000
        val testTimeRaw = ByteBuffer.allocate(java.lang.Long.BYTES).putLong(testTime).array().reversedArray()

        runBlocking {
            sut.setTime(Conf(time = testTime))
        }

        verifyBlocking(periphMock) {
            write(
                any(),
                argThat { this contentEquals testTimeRaw },
                any(),
            )
        }
    }
}
