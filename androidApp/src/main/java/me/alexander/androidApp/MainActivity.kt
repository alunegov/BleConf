package me.alexander.androidApp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val _isGranted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            _isGranted.value = it
        }

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> _isGranted.value = true
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        //val bleConn = BleConnImpl(logger = LoggerImpl)
        val bleConn = BleConnStub

        setContent {
            MaterialTheme(
                //colors = darkColors(),
            ) {
                Root(_isGranted, bleConn)
            }
        }
    }
}

sealed class RootScreen(val route: String) {
    object Servers : RootScreen("A")
    object Server : RootScreen("B")
}

sealed class ServerScreen(val route: String, val caption: String, val IconId: Int) {
    object Sensors : ServerScreen("B1", "Sensors", 1)
    object History : ServerScreen("B2", "History", 2)
    object Conf : ServerScreen("B3", "Conf", 3)
}

val serverScreenItems = listOf(
    ServerScreen.Sensors,
    ServerScreen.History,
    ServerScreen.Conf,
)

@Composable
fun Root(isGranted: State<Boolean>, bleConn: BleConn) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = RootScreen.Servers.route) {
        composable(RootScreen.Servers.route) {
            val viewModel: ServersListViewModel = viewModel(factory = serversListViewModelFactory(bleConn))
            Log.d(TAG, viewModel.toString())
            ServersList(isGranted, viewModel, navController)
        }
        navigation(route = RootScreen.Server.route, startDestination = ServerScreen.Sensors.route) {
            composable(ServerScreen.Sensors.route) {
                val viewModel: ServerViewModel = it.parentViewModel(navController = navController, factory = serverViewModelFactory(bleConn, address))
                Log.d(TAG, viewModel.toString())
                SensorsList(viewModel, navController)
            }
            composable(ServerScreen.History.route) {
                val viewModel: ServerViewModel = it.parentViewModel(navController = navController, factory = serverViewModelFactory(bleConn, address))
                Log.d(TAG, viewModel.toString())
                ServerHistory(viewModel, navController)
            }
            composable(ServerScreen.Conf.route) {
                val viewModel: ServerViewModel = it.parentViewModel(navController = navController, factory = serverViewModelFactory(bleConn, address))
                Log.d(TAG, viewModel.toString())
                ServerConf(viewModel, navController)
            }
        }
    }
}

// https://stackoverflow.com/questions/64955859/scoping-states-in-jetpack-compose
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

    // And since we can't use viewModel(), we use ViewModelProvider directly to get the ViewModel instance, using the
    // lifecycle-viewmodel-ktx extension
    return ViewModelProvider(parentBackStackEntry, factory).get()
}

// TODO: pass via nav args
var address: String = ""

fun serversListViewModelFactory(bleConn: BleConn): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ServersListViewModel(bleConn) as T
        }
    }
}

@Composable
fun ServersList(
    isGranted: State<Boolean>,
    viewModel: ServersListViewModel,
    navController: NavController,
) {
    val state = viewModel.servers.collectAsState()

    DisposableEffect(isGranted.value) {
        //Log.d(TAG, "ServersList DisposableEffect")
        if (isGranted.value) {
            viewModel.startScan()
        }
        onDispose {
            //Log.d(TAG, "ServersList DisposableEffect onDispose")
            viewModel.stopScan()
        }
    }

    Column {
        TopAppBar(
            title = {
                Text("BleConn")
            },
        )

        val model = state.value

        if (model.errorText.isNotEmpty()) {
            Text(
                text = model.errorText,
                color = Color.Red,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1.0f),
        ) {
            items(model.servers) { server ->
                Row(
                    modifier = Modifier
                        .clickable {
                            address = server.address

                            navController.navigate(RootScreen.Server.route) {
                                //launchSingleTop = true
                                //restoreState = true
                            }
                            //logNavBackQueue(navController)
                        }
                        .fillMaxWidth()
                        .padding(8.dp, 8.dp),
                ) {
                    Text(server.address)

                    Spacer(Modifier.size(16.dp))

                    Text(server.name ?: "unk")

                    Spacer(Modifier.size(16.dp))

                    Text(String.format("%d dB", server.rssi))
                }
            }
        }
    }
}

fun serverViewModelFactory(bleConn: BleConn, address: String): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ServerViewModel(bleConn, address) as T
        }
    }
}

@Composable
fun SensorsList(
    viewModel: ServerViewModel,
    navController: NavController,
) {
    val state = viewModel.sensors.collectAsState()

    DisposableEffect(true) {
        //Log.d(TAG, "SensorsList DisposableEffect")
        viewModel.reloadSensors()
        onDispose {
            //Log.d(TAG, "SensorsList DisposableEffect onDispose")
        }
    }

    Column {
        ServerTopAppBar(viewModel.serverName, navController)

        val model = state.value

        if (model.errorText.isNotEmpty()) {
            Text(
                text = model.errorText,
                color = Color.Red,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1.0f),
        ) {
            items(model.sensors) { sensor ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp, 8.dp),
                ) {
                    Checkbox(
                        checked = sensor.enabled,
                        onCheckedChange = { checked -> viewModel.toggleEnabled(sensor.id, checked) },
                    )

                    Spacer(Modifier.size(16.dp))

                    Text(sensor.name)

                    Spacer(Modifier.size(16.dp))

                    Text(
                        when (sensor.state) {
                            0 -> "bad"
                            1 -> "good"
                            2 -> "unk"
                            else -> "unsupp ${sensor.state}"
                        }
                    )

                    if (sensor.coeff != null) {
                        Spacer(Modifier.size(16.dp))

                        Text(sensor.coeff.toString())
                    }
                }
            }
        }

        ServerBottomBar(navController)
    }
}

@Composable
fun ServerHistory(
    viewModel: ServerViewModel,
    navController: NavController,
) {
    val state = viewModel.history.collectAsState()

    DisposableEffect(true) {
        //Log.d(TAG, "ServerHistory DisposableEffect")
        viewModel.reloadHistory()
        onDispose {
            //Log.d(TAG, "ServerHistory DisposableEffect onDispose")
        }
    }

    Column {
        ServerTopAppBar(viewModel.serverName, navController)

        val model = state.value

        if (model.errorText.isNotEmpty()) {
            Text(
                text = model.errorText,
                color = Color.Red,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1.0f),
        ) {
            items(model.servers) { event ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp, 8.dp),
                ) {
                    Text(Date(event.time * 1000).toString())

                    //Spacer(Modifier.size(16.dp))

                    //Text(event.en.toString())

                    Row {
                        for (i in 0..7) {
                            val en = (event.en and (1 shl i)) != 0

                            Spacer(Modifier.size(16.dp))

                            Column {
                                Text((i + 1).toString(), color = if (en) Color.Unspecified else Color.Gray)

                                if (en) {
                                    Text("on")
                                } else {
                                    Text("off", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        ServerBottomBar(navController)
    }
}

@Composable
fun ServerConf(
    viewModel: ServerViewModel,
    navController: NavController,
) {
    val state = viewModel.conf.collectAsState()

    DisposableEffect(true) {
        //Log.d(TAG, "ServerConf DisposableEffect")
        viewModel.reloadConf()
        onDispose {
            //Log.d(TAG, "ServerConf DisposableEffect onDispose")
        }
    }

    Column {
        ServerTopAppBar(viewModel.serverName, navController)

        val model = state.value

        if (model.errorText.isNotEmpty()) {
            Text(
                text = model.errorText,
                color = Color.Red,
            )
        }

        Column(
            modifier = Modifier.weight(1.0f).padding(8.dp, 8.dp),
        ) {
            Text(Date(model.conf.time * 1000).toString())

            Button(onClick = { viewModel.syncTime() }) {
                Text("SyncTime")
            }

            /*Button(onClick = { viewModel.setConfOnly() }) {
                Text("SetConf")
            }*/
        }

        ServerBottomBar(navController)
    }
}

@Composable
fun ServerTopAppBar(
    title: String,
    navController: NavController,
) {
    TopAppBar(
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, null)
            }
        },
    )
}

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

/*fun logNavBackQueue(navController: NavController) {
    navController.backQueue.forEach {
        Log.d(TAG, """destination = ${it.destination}
            arguments = ${it.arguments}
            id = ${it.id}
            savedStateHandle = ${it.savedStateHandle.keys()}
            getLifecycle = ${it.getLifecycle()}
            maxLifecycle = ${it.maxLifecycle}
            getViewModelStore = ${it.getViewModelStore()}
            getDefaultViewModelProviderFactory = ${it.getDefaultViewModelProviderFactory()}
            getSavedStateRegistry = ${it.getSavedStateRegistry()}""")
    }
}*/
