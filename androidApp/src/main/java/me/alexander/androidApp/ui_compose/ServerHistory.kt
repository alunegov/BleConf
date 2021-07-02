package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.alexander.androidApp.HistoryModel
import me.alexander.androidApp.ServerScreen
import me.alexander.androidApp.ServerViewModel
import me.alexander.androidApp.domain.HistoryEvent
import java.util.*

//private const val TAG = "ServerHistory"

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
            EmptyPlaceHolder("Loading...")
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

                            Text("Включенность датчиков:")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                for (i in 0..7) {
                                    val en = (event.en and (1 shl i)) != 0

                                    Spacer(Modifier.size(16.dp))

                                    Column(
                                        modifier = Modifier.weight(1.0f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text((i + 1).toString(), color = if (en) Color.Unspecified else Color.Gray)

                                        if (en) {
                                            Text("on")
                                        } else {
                                            Text("off", color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            // TODO: Text("Изменение включенности:")
                        }
                    }
                }
            } else {
                EmptyPlaceHolder("No events")
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
