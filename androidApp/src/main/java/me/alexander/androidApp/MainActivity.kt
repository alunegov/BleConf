package me.alexander.androidApp

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.composable
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Root()
            }
        }
    }
}

sealed class RootScreen(val route: String) {
    object Servers : RootScreen("A")
    object Server : RootScreen("B")
}

sealed class ServerScreen(val route: String, val caption: String) {
    object Sensors : ServerScreen("B1", "Sensors")
    object History : ServerScreen("B2", "History")
    object Conf : ServerScreen("B3", "Conf")
}

val serverScreenItems = listOf(
    ServerScreen.Sensors,
    ServerScreen.History,
    ServerScreen.Conf,
)

@Composable
fun Root() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = RootScreen.Servers.route) {
        composable(RootScreen.Servers.route) { ServersList(navController) }
        navigation(route = RootScreen.Server.route, startDestination = ServerScreen.Sensors.route) {
            composable(ServerScreen.Sensors.route) { SensorsList(navController) }
            composable(ServerScreen.History.route) { ServerHistory(navController) }
            composable(ServerScreen.Conf.route) { ServerConf(navController) }
        }
    }
}

@Preview
@Composable
fun RootPreview() {
    Root()
}

var address: String = ""

@Composable
fun ServersList(navController: NavController) {
    val viewModel: ServersListViewModel = viewModel()
    val state = viewModel.model.collectAsState()

    LazyColumn {
        items(state.value) { server ->
            Row(
                modifier = Modifier
                    .clickable {
                        address = server.address
                        navController.navigate(RootScreen.Server.route)
                    }
                    .fillMaxWidth()
            ) {
                Text(server.address)

                Spacer(Modifier.size(16.dp))

                Text(server.name)

                Spacer(Modifier.size(16.dp))

                Text(String.format("%d dB", server.rssi))
            }
        }
    }
}

fun serverViewModelFactory(address: String): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ServerViewModel(address) as T
        }
    }
}

@Composable
fun SensorsList(navController: NavController) {
    val viewModel: ServerViewModel = viewModel(
        factory = serverViewModelFactory(address),
    )
    val state = viewModel.sensors.collectAsState()

    Column {
        LazyColumn(
            modifier = Modifier.weight(1.0f)
        ) {
            items(state.value) { sensor ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = sensor.enabled,
                        onCheckedChange = { viewModel.toggleEnabled(sensor.name, it) },
                    )

                    Spacer(Modifier.size(16.dp))

                    Text(sensor.name)

                    Spacer(Modifier.size(16.dp))

                    Text(sensor.state.toString())
                }
            }
        }

        Button(onClick = { viewModel.reloadSensors() }) {
            Text("Reload")
        }

        ServerBottomBar(navController)
    }
}

@Composable
fun ServerHistory(navController: NavController) {
    val viewModel: ServerViewModel = viewModel(
        factory = serverViewModelFactory(address),
    )
    val state = viewModel.history.collectAsState()

    Column {
        Text(
            text = "ServerHistory",
            modifier = Modifier.weight(1.0f),
        )

        ServerBottomBar(navController)
    }
}

@Composable
fun ServerConf(navController: NavController) {
    val viewModel: ServerViewModel = viewModel(
        factory = serverViewModelFactory(address),
    )
    val state = viewModel.conf.collectAsState()

    Column {
        Text(
            text = "ServerConf",
            modifier = Modifier.weight(1.0f),
        )

        ServerBottomBar(navController)
    }
}

@Composable
fun ServerBottomBar(navController: NavController) {
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)
        serverScreenItems.forEach {
            BottomNavigationItem(
                selected = currentRoute == it.route,
                onClick = {
                    navController.navigate(it.route) {
                        popUpTo = navController.graph.startDestination
                        launchSingleTop = true
                    }
                },
                icon = {},
                label = { Text(it.caption) },
            )
        }
    }
}
