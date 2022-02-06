package com.github.alunegov.bleconf.android.ui_compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
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
        onSensorsRefresh = { viewModel.reloadSensors() },
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
 * @param onSensorsRefresh Обработчик обновления/загрузки списка датчиков.
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
    onSensorsRefresh: () -> Unit,
    onSensorChecked: (String, Boolean) -> Unit,
    onBackClicked: () -> Unit,
    currentRoute: String?,
    onRouteClicked: (String) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()

    if (sensorsModel.errorText.isNotEmpty() or timeModel.errorText.isNotEmpty()) {
        LaunchedEffect(scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = sensorsModel.errorText + timeModel.errorText,
                //actionLabel = "Reload",
            )
            //onSensorsRefresh()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { ServerAppBar(serverName, onBackClicked) },
        bottomBar = { ServerBottomBar(currentRoute, onRouteClicked) },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding()),
        ) {
            val swipeRefreshState = rememberSwipeRefreshState(sensorsModel.loading or timeModel.loading)

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = onSensorsRefresh,
            ) {
                if (sensorsModel.sensors.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(sensorsModel.sensors) {
                            SensorItem(it, onSensorChecked, swipeRefreshState.isRefreshing)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        EmptyPlaceHolder(stringResource(R.string.no_sensors))
                    }
                }
            }
        }
    }
}

@Preview
@Preview(locale = "ru")
@Composable
fun SensorsListScreenPreview() {
    SensorsListScreen(
        serverName = "Server",
        sensorsModel = SensorsModel(
            sensors = listOf(
                Sensor("1", "Sensor 1", true, 0, null, false, 0),
                Sensor("2", "Sensor 2", true, 1, null, false, 1),
                Sensor("3", "Sensor 3", true, 2, null, false, 2),
                Sensor("4", "Sensor 4", false, 0, null, false, 3),
                Sensor("5", "Sensor 5", false, 1, null, false, 4),
                Sensor("6", "Sensor 6", false, 2, null, false, 5),
                Sensor("7", "Sensor 7", true, 0, null, false, 6),
                Sensor("8", "Sensor 8", true, 0, 1.13f, false, 7),
                Sensor("16", "Relay", true, 0, null, true, 15),
            ),
            errorText = "Sensors error",
        ),
        timeModel = TimeModel(errorText = "Time error"),
        onSensorsRefresh = {},
        onSensorChecked = { _, _ -> },
        onBackClicked = {},
        currentRoute = ServerScreen.Sensors.route,
        onRouteClicked = {},
    )
}

@Preview
@Preview(locale = "ru")
@Composable
fun SensorsListScreenPreview_NoSensors() {
    SensorsListScreen(
        serverName = "Server",
        sensorsModel = SensorsModel(
            sensors = listOf(),
            errorText = "Sensors error",
        ),
        timeModel = TimeModel(errorText = "Time error"),
        onSensorsRefresh = {},
        onSensorChecked = { _, _ -> },
        onBackClicked = {},
        currentRoute = ServerScreen.Sensors.route,
        onRouteClicked = {},
    )
}

/**
 * Датчик (элемент списка датчиков).
 *
 * @param sensor Датчик.
 * @param onSensorChecked Обработчик включения/выключения датчика.
 * @param isRefreshing Флаг: В процессе обновления списка датчиков.
 */
@Composable
fun SensorItem(
    sensor: Sensor,
    onSensorChecked: (String, Boolean) -> Unit,
    isRefreshing: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = sensor.enabled,
            onCheckedChange = { checked -> onSensorChecked(sensor.id, checked) },
            enabled = !isRefreshing,
        )

        Spacer(Modifier.size(16.dp))

        Column {
            if (sensor.isRelay) {
                Divider(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp))
            }

            Text(
                text = sensor.name,
                style = MaterialTheme.typography.h5,
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val stateValueColor = when (sensor.state) {
                    0, 4 -> MaterialTheme.colors.error
                    1 -> Color.Companion.Unspecified
                    2 -> Color.Gray
                    else -> Color.Gray
                }

                Row(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = stringResource(R.string.sensor_state),
                        style = MaterialTheme.typography.body1,
                    )

                    if (!sensor.isRelay) {
                        Text(
                            text = when (sensor.state) {
                                0 -> stringResource(R.string.sensor_state_bad)
                                1 -> stringResource(R.string.sensor_state_good)
                                2 -> stringResource(R.string.sensor_state_unknown)
                                4 -> stringResource(R.string.sensor_state_timeout)
                                else -> stringResource(R.string.sensor_state_unsupported, sensor.state)
                            },
                            color = stateValueColor,
                            style = MaterialTheme.typography.body1,
                        )
                    } else {
                        Text(
                            text = when (sensor.state) {
                                0, 4 -> stringResource(R.string.sensor_state_bad_relay)
                                1, 5 -> stringResource(R.string.sensor_state_good_relay)
                                else -> stringResource(R.string.sensor_state_unsupported, sensor.state)
                            },
                            color = when (sensor.state) {
                                0, 4 -> MaterialTheme.colors.error
                                1, 5 -> Color.Companion.Unspecified
                                else -> Color.Gray
                            },
                            style = MaterialTheme.typography.body1,
                        )
                    }
                }

                if (sensor.coeff != null) {
                    Row(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = stringResource(R.string.sensor_coeff),
                            style = MaterialTheme.typography.body1,
                        )

                        Text(
                            text = NumberFormat.getInstance().format(sensor.coeff),
                            color = stateValueColor,
                            style = MaterialTheme.typography.body1,
                        )
                    }
                }
            }
        }
    }
}
