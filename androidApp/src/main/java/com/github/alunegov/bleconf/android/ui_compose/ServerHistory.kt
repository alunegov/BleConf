package com.github.alunegov.bleconf.android.ui_compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
 * @param onBackClicked Обработчик навигации назад.
 * @param currentRoute Текущий роут.
 * @param onRouteClicked Обработчик навигации между окнами сервера.
 */
@Composable
fun ServerHistoryScreen(
    serverName: String,
    model: HistoryModel,
    onBackClicked: () -> Unit,
    currentRoute: String?,
    onRouteClicked: (String) -> Unit,
) {
    Column {
        ServerAppBar(serverName, onBackClicked)

        if (model.errorText.isNotEmpty()) {
            Error(model.errorText)
        }

        if (model.loading) {
            EmptyPlaceHolder(stringResource(R.string.loading))
        } else {
            if (model.events.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1.0f),
                ) {
                    items(model.events) { event ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        ) {
                            Text(
                                text = Date(event.time * 1000).toLocaleString(),
                                style = MaterialTheme.typography.h5,
                            )

                            Text(stringResource(R.string.sensors_enability))

                            Spacer(Modifier.size(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                val enabledColor = SwitchDefaults.colors().trackColor(
                                    enabled = true,
                                    checked = true
                                ).value
                                val disabledColor = SwitchDefaults.colors().trackColor(
                                    enabled = true,
                                    checked = false
                                ).value

                                for (i in 0..7) {
                                    Spacer(Modifier.size(8.dp))

                                    Column(
                                        modifier = Modifier.weight(1.0f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        val en = (event.en and (1 shl i)) != 0

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
                    }
                }
            } else {
                EmptyPlaceHolder(stringResource(R.string.no_events))
            }
        }

        ServerBottomBar(currentRoute, onRouteClicked)
    }
}

@Preview
@Composable
fun ServerHistoryScreenPreview() {
    ServerHistoryScreen(
        serverName = "Server",
        model = HistoryModel(
            events = listOf(
                HistoryEvent(2, 0b00000000),
                HistoryEvent(1, 0b00100100),
            ),
            errorText = "Error",
        ),
        onBackClicked = {},
        currentRoute = ServerScreen.History.route,
        onRouteClicked = {},
    )
}

@Preview
@Composable
fun ServerHistoryScreenPreview_NoEvents() {
    ServerHistoryScreen(
        serverName = "Server",
        model = HistoryModel(
            events = listOf(),
            errorText = "Error",
        ),
        onBackClicked = {},
        currentRoute = ServerScreen.History.route,
        onRouteClicked = {},
    )
}
