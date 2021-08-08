package com.github.alunegov.bleconf.android.ui_compose

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.github.alunegov.bleconf.android.*
import com.github.alunegov.bleconf.android.R
import com.github.alunegov.bleconf.android.services.BleConn
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState

private const val TAG = "Root"

/**
 * Основное окно приложения. Реализует работу с разрешениями.
 *
 * @param bleConn Реализация [BleConn].
 * @param navigateToSettingsScreen Обработчик перехода к настройкам приложения.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RootWithLocationPermission(
    bleConn: BleConn,
    navigateToSettingsScreen: () -> Unit,
) {
    // не используем doNotShowRationale, п.ч. ble/location основа всей нашей функциональности

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    PermissionRequired(
        permissionState = locationPermissionState,
        permissionNotGrantedContent = {
            if (locationPermissionState.shouldShowRationale) {
                PermissionRationale(
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() },
                )
            } else {
                Log.d(TAG, "not shouldShowRationale")
                SideEffect {
                    locationPermissionState.launchPermissionRequest()
                }
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
private fun PermissionRationale(
    onRequestPermission: () -> Unit,
) {
    // to force content color to be onSurface
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.permission_rationale),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h5,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.widthIn(250.dp),
            ) {
                Text(stringResource(R.string.request_permission))
            }
        }
    }
}

@Preview
@Preview(locale = "ru")
@Composable
fun PermissionRationalePreview() {
    PermissionRationale({})
}

@Composable
private fun PermissionDenied(
    navigateToSettingsScreen: () -> Unit,
) {
    // to force content color to be onSurface
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.permission_denied),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h5,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = navigateToSettingsScreen,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(250.dp),
            ) {
                Text(stringResource(R.string.open_settings))
            }
        }
    }
}

@Preview
@Preview(locale = "ru")
@Composable
fun PermissionDeniedPreview() {
    PermissionDenied({})
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
                val viewModel: ServerViewModel = it.parentViewModel(
                    navController = navController,
                    factory = serverViewModelFactory(bleConn, gAddress),
                )
                Log.d(TAG, viewModel.toString())
                SensorsList(viewModel, navController)
            }
            composable(ServerScreen.History.route) {
                val viewModel: ServerViewModel = it.parentViewModel(
                    navController = navController,
                    factory = serverViewModelFactory(bleConn, gAddress),
                )
                Log.d(TAG, viewModel.toString())
                ServerHistory(viewModel, navController)
            }
            composable(ServerScreen.Conf.route) {
                val viewModel: ServerViewModel = it.parentViewModel(
                    navController = navController,
                    factory = serverViewModelFactory(bleConn, gAddress),
                )
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
