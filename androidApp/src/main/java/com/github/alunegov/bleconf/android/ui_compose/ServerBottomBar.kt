package com.github.alunegov.bleconf.android.ui_compose

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.alunegov.bleconf.android.RootScreen
import com.github.alunegov.bleconf.android.ServerScreen

//private const val TAG = "ServerBottomBar"

/**
 * Окна сервера.
 */
val serverScreenItems = listOf(
    ServerScreen.Sensors,
    ServerScreen.History,
    ServerScreen.Conf,
)

/**
 * Нижняя панель окон сервера с кнопками навигации между окнами сервера.
 */
@Composable
fun ServerBottomBar(
    currentRoute: String?,
    onRouteClicked: (String) -> Unit,
) {
    BottomNavigation {
        serverScreenItems.forEach {
            BottomNavigationItem(
                selected = currentRoute == it.route,
                onClick = { onRouteClicked(it.route) },
                icon = {
                    when (it.IconId) {
                        1 -> Icon(Icons.Filled.Sensors, null)
                        2 -> Icon(Icons.Filled.History, null)
                        3 -> Icon(Icons.Filled.Settings, null)
                    }
                },
                label = { Text(stringResource(it.captionResId)) },
            )
        }
    }
}

@Preview
@Preview(locale = "ru")
@Composable
fun ServerBottomBarPreview() {
    ServerBottomBar(ServerScreen.Sensors.route, {})
}

@Composable
fun getCurrentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

fun createNavigateToRouteClicked(navController: NavController): (String) -> Unit {
    return { route: String ->
        //Log.d(TAG, String.format("nav to %s, popUpTo %s", it.route, navController.graph.startDestinationRoute ?: ""))
        navController.navigate(route) {
            popUpTo(RootScreen.Server.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
