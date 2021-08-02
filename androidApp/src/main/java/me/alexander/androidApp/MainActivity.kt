package me.alexander.androidApp

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import me.alexander.androidApp.services.BleConn
import me.alexander.androidApp.services.BleConnImpl
//import me.alexander.androidApp.services.BleConnStub
import me.alexander.androidApp.ui_compose.SensorsList
import me.alexander.androidApp.ui_compose.ServerConfEntry
import me.alexander.androidApp.ui_compose.ServerHistory
import me.alexander.androidApp.ui_compose.ServersList

private const val TAG = "MainActivity"

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
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RootWithLocationPermission(
    bleConn: BleConn,
    navigateToSettingsScreen: () -> Unit,
) {
    // Track if the user doesn't want to see the rationale any more.
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)

    PermissionRequired(
        permissionState = locationPermissionState,
        permissionNotGrantedContent = {
            if (doNotShowRationale) {
                Text("Feature not available")
            } else {
                Rationale(
                    onDoNotShowRationale = { doNotShowRationale = true },
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() }
                )
            }
        },
        permissionNotAvailableContent = {
            PermissionDenied(navigateToSettingsScreen)
        }
    ) {
        RootWithNavigation(bleConn)
    }
}

@Composable
private fun Rationale(
    onDoNotShowRationale: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    Column {
        Text("The camera is important for this app. Please grant the permission.")
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = onRequestPermission) {
                Text("Request permission")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onDoNotShowRationale) {
                Text("Don't show rationale again")
            }
        }
    }
}

@Composable
private fun PermissionDenied(
    navigateToSettingsScreen: () -> Unit,
) {
    Column {
        Text(
            "Camera permission denied. See this FAQ with information about why we " +
                    "need this permission. Please, grant us access on the Settings screen."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = navigateToSettingsScreen) {
            Text("Open Settings")
        }
    }
}

/**
 * Основное окно приложения. Реализует точку навигации.
 *
 * @param bleConn Реализация [BleConn].
 */
@Composable
fun RootWithNavigation(bleConn: BleConn) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = RootScreen.Servers.route) {
        composable(RootScreen.Servers.route) {
            val viewModel: ServersListViewModel = viewModel(factory = serversListViewModelFactory(bleConn))
            Log.d(TAG, viewModel.toString())
            ServersList(viewModel) { serverAddress ->
                gAddress = serverAddress

                navController.navigate(RootScreen.Server.route) {
                    //launchSingleTop = true
                    //restoreState = true
                }
            }
        }
        navigation(route = RootScreen.Server.route, startDestination = ServerScreen.Sensors.route) {
            composable(ServerScreen.Sensors.route) {
                val viewModel: ServerViewModel = it.parentViewModel(navController = navController, factory = serverViewModelFactory(bleConn, gAddress))
                Log.d(TAG, viewModel.toString())
                SensorsList(viewModel, navController)
            }
            composable(ServerScreen.History.route) {
                val viewModel: ServerViewModel = it.parentViewModel(navController = navController, factory = serverViewModelFactory(bleConn, gAddress))
                Log.d(TAG, viewModel.toString())
                ServerHistory(viewModel, navController)
            }
            composable(ServerScreen.Conf.route) {
                val viewModel: ServerViewModel = it.parentViewModel(navController = navController, factory = serverViewModelFactory(bleConn, gAddress))
                Log.d(TAG, viewModel.toString())
                ServerConfEntry(viewModel, navController)
            }
        }
    }
}

/**
 * Возвращает VM родительского роута.
 *
 * https://stackoverflow.com/questions/64955859/scoping-states-in-jetpack-compose
 */
@Composable
inline fun <reified VM: ViewModel> NavBackStackEntry.parentViewModel(
    navController: NavController,
    factory: ViewModelProvider.Factory,
): VM {
    // First, get the parent of the current destination
    // This always exists since every destination in your graph has a parent
    val parentId = destination.parent!!.id

    // Now get the NavBackStackEntry associated with the parent
    val parentBackStackEntry = navController.getBackStackEntry(parentId)

    return viewModel(viewModelStoreOwner = parentBackStackEntry, factory = factory)
}
