package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.alexander.androidApp.SensorsModel
import me.alexander.androidApp.ServerScreen
import me.alexander.androidApp.ServerViewModel
import me.alexander.androidApp.TimeModel
import me.alexander.androidApp.domain.Sensor

private const val TAG = "SensorsList"

@Composable
fun SensorsList(
    viewModel: ServerViewModel,
    navController: NavController,
) {
    val sensorsState = viewModel.sensors.collectAsState()
    val timeState = viewModel.time.collectAsState()

    DisposableEffect(true) {
        //Log.d(TAG, "SensorsList DisposableEffect")
        viewModel.reloadSensors()
        // синхронизируем время. на остальных экранах сервера не делаем, п.ч. этот экран будет загружен в любом случае
        viewModel.syncTime()
        onDispose {
            //Log.d(TAG, "SensorsList DisposableEffect onDispose")
            viewModel.stopObserveSensors()
        }
    }

    SensorsListScreen(
        serverName = viewModel.serverName,
        sensorsModel = sensorsState.value,
        timeModel = timeState.value,
        onSensorChecked = { sensorId, checked -> viewModel.setEnabled(sensorId, checked) },
        onBackClicked = { navController.popBackStack() },
        currentRoute = getCurrentRoute(navController),
        onRouteClicked = createNavigateToRouteClicked(navController),
    )
}

@Composable
fun SensorsListScreen(
    serverName: String,
    sensorsModel: SensorsModel,
    timeModel: TimeModel,
    onSensorChecked: (String, Boolean) -> Unit,
    onBackClicked: () -> Unit,
    currentRoute: String?,
    onRouteClicked: (String) -> Unit,
) {
    Column {
        ServerAppBar(serverName, onBackClicked)

        if (sensorsModel.errorText.isNotEmpty() || timeModel.errorText.isNotEmpty()) {
            Column {
                if (sensorsModel.errorText.isNotEmpty()) {
                    Text(
                        text = sensorsModel.errorText,
                        color = Color.Red,
                    )
                }

                if (timeModel.errorText.isNotEmpty()) {
                    Text(
                        text = timeModel.errorText,
                        color = Color.Red,
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1.0f),
        ) {
            items(sensorsModel.sensors) { sensor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 8.dp),
                ) {
                    Switch(
                        checked = sensor.enabled,
                        onCheckedChange = { checked -> onSensorChecked(sensor.id, checked) },
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

        ServerBottomBar(currentRoute, onRouteClicked)
    }
}

@Preview
@Composable
fun SensorsListScreenPreview() {
    SensorsListScreen(
        serverName = "Server",
        sensorsModel = SensorsModel(
            sensors = listOf(
                Sensor("1", "Sensor 1", true, 0, null),
                Sensor("2", "Sensor 2", true, 1, null),
                Sensor("3", "Sensor 3", true, 2, null),
                Sensor("4", "Sensor 4", false, 0, null),
                Sensor("5", "Sensor 5", false, 1, null),
                Sensor("6", "Sensor 6", false, 2, null),
                Sensor("7", "Sensor 7", true, 0, null),
                Sensor("8", "Sensor 8", true, 0, 1.13f),
            ),
            errorText = "Sensors error",
        ),
        timeModel = TimeModel(errorText = "Time error"),
        onSensorChecked = { _, _ -> },
        onBackClicked = {},
        currentRoute = ServerScreen.Sensors.route,
        onRouteClicked = {},
    )
}
