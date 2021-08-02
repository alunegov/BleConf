package com.github.alunegov.bleconf.android.services

import com.juul.kable.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.mockito.kotlin.mock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test

class BleConnImplTest {
    private val scannerMock = mock<Scanner>()
    private val sut = BleConnImpl(scannerMock)

    @Test
    fun scanAndGetTest() {
        val scope = CoroutineScope(EmptyCoroutineContext)

        sut.startScan(scope)

        sut.stopScan()

        //val serverConn = sut.getServerConn("", scope)
    }

    @Test
    fun stopScanTest_StopViaScope() {
        val scope = CoroutineScope(EmptyCoroutineContext)
        sut.startScan(scope)
        scope.cancel()
    }
}
