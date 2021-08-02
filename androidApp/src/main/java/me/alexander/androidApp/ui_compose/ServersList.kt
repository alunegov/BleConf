package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.alexander.androidApp.R
import me.alexander.androidApp.ServersListViewModel
import me.alexander.androidApp.domain.Server
import me.alexander.androidApp.domain.ServersModel

//private const val TAG = "ServersList"

/**
 * Окно Список серверов.
 *
 * @param viewModel VM.
 * @param onServerClicked Обработчик события выбора сервера.
 */
@Composable
fun ServersList(
    viewModel: ServersListViewModel,
    onServerClicked: (String) -> Unit,
) {
    val state = viewModel.servers.collectAsState()

    DisposableEffect(true) {
        //Log.d(TAG, "ServersList DisposableEffect")
        viewModel.startScan()
        onDispose {
            //Log.d(TAG, "ServersList DisposableEffect onDispose")
            viewModel.stopScan()
        }
    }

    ServersListScreen(
        model = state.value,
        onServerClicked = onServerClicked,
    )
}

/**
 * Окно Список серверов (preview-friendly).
 *
 * @param model Модель списка серверов.
 * @param onServerClicked Обработчик выбора сервера.
 */
@Composable
fun ServersListScreen(
    model: ServersModel,
    onServerClicked: (String) -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Text(stringResource(R.string.app_name))
            },
        )

        if (model.errorText.isNotEmpty()) {
            Error(model.errorText)
        }

        if (model.servers.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1.0f),
            ) {
                items(model.servers) { server ->
                    Column(
                        modifier = Modifier
                            .clickable { onServerClicked(server.address) }
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Text(
                            text = server.name ?: stringResource(R.string.unknown_server_name),
                            style = MaterialTheme.typography.h5,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = server.address,
                                modifier = Modifier.weight(1.0f),
                                style = MaterialTheme.typography.body1,
                            )

                            Text(
                                text = stringResource(R.string.server_rssi, server.rssi),
                                modifier = Modifier.weight(1.0f),
                                style = MaterialTheme.typography.body1,
                            )
                        }
                    }
                }
            }
        } else {
            EmptyPlaceHolder(stringResource(R.string.no_servers))
        }
    }
}

@Preview
@Composable
fun ServersListScreenPreview() {
    ServersListScreen(
        model = ServersModel(
            servers = listOf(
                Server("10:20:30:40:50:61", "Server 1", -10),
                Server("10:20:30:40:50:62", "Server 2", -20),
            ),
            errorText = "Error",
        ),
        onServerClicked = {},
    )
}

@Preview
@Composable
fun ServersListScreenPreview_NoServers() {
    ServersListScreen(
        model = ServersModel(
            servers = listOf(),
            errorText = "Error",
        ),
        onServerClicked = {},
    )
}
