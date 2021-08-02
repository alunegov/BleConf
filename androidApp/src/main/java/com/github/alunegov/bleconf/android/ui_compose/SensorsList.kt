package com.github.alunegov.bleconf.android.ui_compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.alunegov.bleconf.android.R
import com.github.alunegov.bleconf.android.SensorsModel
import com.github.alunegov.bleconf.android.ServerScreen
import com.github.alunegov.bleconf.android.ServerViewModel
import com.github.alunegov.bleconf.android.TimeModel
import com.github.alunegov.bleconf.android.domain.Sensor
import java.text.NumberFormat

//private const val TAG = "SensorsList"

/**
 * Окно Список датчиков.
 *
 * @param viewModel VM.
 * @param navController Navigation.
 */
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

/**
 * Окно Список датчиков (preview-friendly).
 *
 * @param serverName Имя сервера.
 * @param sensorsModel Модель списка датчиков.
 * @param timeModel Модель системного времени сервера.
 * @param onSensorChecked Обработчик включения/выключения датчика.
 * @param onBackClicked Обработчик навигации назад.
 * @param currentRoute Текущий роут.
 * @param onRouteClicked Обработчик навигации между окнами сервера.
 */
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
            Error(sensorsModel.errorText, timeModel.errorText)
        }

        if (sensorsModel.loading) {
            EmptyPlaceHolder(stringResource(R.string.loading))
        } else {
            if (sensorsModel.sensors.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1.0f),
                ) {
                    items(sensorsModel.sensors) { sensor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Switch(
                                checked = sensor.enabled,
                                onCheckedChange = { checked -> onSensorChecked(sensor.id, checked) },
                            )

                            Spacer(Modifier.size(16.dp))

                            Column {
                                Text(
                                    text = sensor.name,
                                    style = MaterialTheme.typography.h5,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.weight(1.0f)) {
                                        Text(
                                            text = stringResource(R.string.sensor_state),
                                            style = MaterialTheme.typography.body1,
                                        )

                                        Text(
                                            text = when (sensor.state) {
                                                0 -> stringResource(R.string.sensor_state_bad)
                                                1 -> stringResource(R.string.sensor_state_good)
                                                2 -> stringResource(R.string.sensor_state_unknown)
                                                else -> stringResource(R.string.sensor_state_unsopported, sensor.state)
                                            },
                                            style = MaterialTheme.typography.body1,
                                        )
                                    }

                                    if (sensor.coeff != null) {
                                        val formattedCoeff = NumberFormat.getInstance().format(sensor.coeff)
                                        Text(
                                            text = stringResource(R.string.sensor_coeff, formattedCoeff),
                                            modifier = Modifier.weight(1.0f),
                                            style = MaterialTheme.typography.body1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                EmptyPlaceHolder(stringResource(R.string.no_sensors))
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

@Preview
@Composable
fun SensorsListScreenPreview_NoSensors() {
    SensorsListScreen(
        serverName = "Server",
        sensorsModel = SensorsModel(
            sensors = listOf(),
            errorText = "Sensors error",
        ),
        timeModel = TimeModel(errorText = "Time error"),
        onSensorChecked = { _, _ -> },
        onBackClicked = {},
        currentRoute = ServerScreen.Sensors.route,
        onRouteClicked = {},
    )
}
