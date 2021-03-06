package com.github.alunegov.bleconf.android.ui_compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.alunegov.bleconf.android.HistoryModel
import com.github.alunegov.bleconf.android.R
import com.github.alunegov.bleconf.android.ServerScreen
import com.github.alunegov.bleconf.android.ServerViewModel
import com.github.alunegov.bleconf.android.domain.HistoryEvent
import com.github.alunegov.bleconf.android.domain.SensorsMaskV1
import com.github.alunegov.bleconf.android.domain.SensorsMaskV2
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.DateFormat
import java.util.*

//private const val TAG = "ServerHistory"

/**
 * Окно Настройки.
 *
 * @param viewModel VM.
 * @param navController Navigation.
 */
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

    ServerHistoryScreen(
        serverName = viewModel.serverName,
        model = state.value,
        onHistoryRefresh = { viewModel.reloadHistory() },
        onBackClicked = { navController.popBackStack() },
        currentRoute = getCurrentRoute(navController),
        onRouteClicked = createNavigateToRouteClicked(navController),
    )
}

/**
 * Окно Настройки (preview-friendly).
 *
 * @param serverName Имя сервера.
 * @param model Модель истории.
 * @param onHistoryRefresh Обработчик обновления/загрузки истории.
 * @param onBackClicked Обработчик навигации назад.
 * @param currentRoute Текущий роут.
 * @param onRouteClicked Обработчик навигации между окнами сервера.
 */
@Composable
fun ServerHistoryScreen(
    serverName: String,
    model: HistoryModel,
    onHistoryRefresh: () -> Unit,
    onBackClicked: () -> Unit,
    currentRoute: String?,
    onRouteClicked: (String) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()

    if (model.errorText.isNotEmpty()) {
        LaunchedEffect(scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar(
                model.errorText,
                //actionLabel = "Reload",
            )
            //onHistoryRefresh()
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
            val swipeRefreshState = rememberSwipeRefreshState(model.loading)

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = onHistoryRefresh,
            ) {
                if (model.events.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(model.events) {
                            ServerHistoryItem(it)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        EmptyPlaceHolder(stringResource(R.string.no_events))
                    }
                }
            }
        }
    }
}

@Preview
@Preview(locale = "ru")
@Composable
fun ServerHistoryScreenPreview() {
    ServerHistoryScreen(
        serverName = "Server",
        model = HistoryModel(
            events = listOf(
                HistoryEvent(2, 0b0000000000000000, SensorsMaskV2),
                HistoryEvent(1, 0b1000000000100100, SensorsMaskV1),
            ),
            errorText = "Error",
        ),
        onHistoryRefresh = {},
        onBackClicked = {},
        currentRoute = ServerScreen.History.route,
        onRouteClicked = {},
    )
}

@Preview
@Preview(locale = "ru")
@Composable
fun ServerHistoryScreenPreview_NoEvents() {
    ServerHistoryScreen(
        serverName = "Server",
        model = HistoryModel(
            events = listOf(),
            errorText = "Error",
        ),
        onHistoryRefresh = {},
        onBackClicked = {},
        currentRoute = ServerScreen.History.route,
        onRouteClicked = {},
    )
}

/**
 * Событие истории (элемент списка событий - истории).
 *
 * @param event Событие истории.
 */
@Composable
fun ServerHistoryItem(event: HistoryEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        val formattedTime = DateFormat.getDateTimeInstance().format(Date(event.time * 1000))
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.h5,
        )

        Text(stringResource(R.string.sensors_enability))

        Spacer(Modifier.size(8.dp))

        val enabledColor = SwitchDefaults.colors().trackColor(
            enabled = true,
            checked = true
        ).value
        val disabledColor = SwitchDefaults.colors().trackColor(
            enabled = true,
            checked = false
        ).value

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            for (i in 0..7) {
                if ((event.sensorsMask and (1 shl i)) != 0) {
                    Spacer(Modifier.size(8.dp))

                    Column(
                        modifier = Modifier.weight(1.0f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val en = (event.sensorsEnability and (1 shl i)) != 0

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (en) enabledColor else disabledColor),
                        ) {
                            Text(
                                text = (i + 1).toString(),
                                modifier = Modifier.defaultMinSize(24.dp),
                                color = if (en) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.sensors_enability_relay))

            val en = if ((event.sensorsMask and (1 shl 15)) != 0) {
                (event.sensorsEnability and (1 shl 15)) != 0
            } else {
                true
            }
            Text(
                text = stringResource(if (en) R.string.sensor_state_good_relay else R.string.sensor_state_bad_relay),
                color = if (en) enabledColor else disabledColor,
            )
        }
    }
}
