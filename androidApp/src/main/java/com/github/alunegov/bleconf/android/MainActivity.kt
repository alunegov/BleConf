package com.github.alunegov.bleconf.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import com.github.alunegov.bleconf.android.services.BleConnImpl
import com.github.alunegov.bleconf.android.ui_compose.RootWithLocationPermission
//import com.github.alunegov.bleconf.android.services.BleConnStub
import com.github.alunegov.bleconf.android.ui_compose.theme.BleConfTheme

//private const val TAG = "MainActivity"

/**
 * MAC-адрес выбранного сервера в окне Список серверов.
 */
// TODO: pass via nav args
var gAddress: String = ""

/**
 * Объект для работы с BT-адаптером.
 */
val gBleConn = BleConnImpl(logger = LoggerImpl)
//val gBleConn = BleConnStub

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // apply the actual theme (instead of SplashTheme)
        setTheme(R.style.Theme_BleConf_NoActionBar)

        super.onCreate(savedInstanceState)

        setContent {
            BleConfTheme {
                // to force content color to be onSurface
                Surface {
                    RootWithLocationPermission(
                        bleConn = gBleConn,
                        navigateToSettingsScreen = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}
