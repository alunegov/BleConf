package me.alexander.androidApp.ui_compose

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
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import me.alexander.androidApp.RootScreen
import me.alexander.androidApp.ServerScreen

private const val TAG = "ServerBottomBar"

val serverScreenItems = listOf(
    ServerScreen.Sensors,
    ServerScreen.History,
    ServerScreen.Conf,
)

@Composable
fun ServerBottomBar(navController: NavController) {
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        //val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)
        val currentRoute = navBackStackEntry?.destination?.route
        serverScreenItems.forEach {
            BottomNavigationItem(
                selected = currentRoute == it.route,
                onClick = {
                    //Log.d(TAG, String.format("nav to %s, popUpTo %s", it.route, navController.graph.startDestinationRoute ?: ""))
                    navController.navigate(it.route) {
                        launchSingleTop = true
                        //restoreState = true
                        //popUpTo(navController.graph.startDestinationId) {
                        //saveState = true
                        //}
                        popUpTo(RootScreen.Server.route) {
                            //saveState = true
                        }
                    }
                    //logNavBackQueue(navController)
                },
                icon = {
                    when (it.IconId) {
                        1 -> Icon(Icons.Filled.Sensors, null)
                        2 -> Icon(Icons.Filled.History, null)
                        3 -> Icon(Icons.Filled.Settings, null)
                    }
                },
                label = { Text(it.caption) },
            )
        }
    }
}
