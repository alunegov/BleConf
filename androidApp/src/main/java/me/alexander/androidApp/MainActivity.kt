package me.alexander.androidApp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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
    /**
     * Флаг: Разрешения для BT получены.
     */
    private val _isGranted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        // apply the actual theme (instead of SplashTheme)
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            _isGranted.value = it
        }

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> _isGranted.value = true
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setContent {
            MaterialTheme {
                Root(_isGranted, gBleConn)
            }
        }
    }
}

/**
 * Основное окно приложения. Реализует точку навигации.
 *
 * @param isGranted Флаг: Разрешения для BT получены.
 * @param bleConn Реализация [BleConn].
 */
@Composable
fun Root(isGranted: State<Boolean>, bleConn: BleConn) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = RootScreen.Servers.route) {
        composable(RootScreen.Servers.route) {
            val viewModel: ServersListViewModel = viewModel(factory = serversListViewModelFactory(bleConn))
            Log.d(TAG, viewModel.toString())
            ServersList(isGranted, viewModel) { serverAddress ->
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
