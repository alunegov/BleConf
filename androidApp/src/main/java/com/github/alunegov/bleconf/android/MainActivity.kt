package com.github.alunegov.bleconf.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.SideEffect
import com.github.alunegov.bleconf.android.services.BleConnImpl
import com.github.alunegov.bleconf.android.services.BleConnStub
import com.github.alunegov.bleconf.android.ui_compose.RootWithLocationPermission
import com.github.alunegov.bleconf.android.ui_compose.theme.BleConfTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.graphics.Color

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
        // apply the actual theme (instead of Theme.BleConf.Splash)
        setTheme(R.style.Theme_BleConf_NoActionBar)

        super.onCreate(savedInstanceState)

        setContent {
            BleConfTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = MaterialTheme.colors.isLight

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons,
                    )
                }

                // to force content color to be onSurface
                Surface {
                    RootWithLocationPermission(
                        bleConn = gBleConn,
                        navigateToSettingsScreen = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null),
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}
