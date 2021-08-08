package com.github.alunegov.bleconf.android.ui_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.alunegov.bleconf.android.R
import com.github.alunegov.bleconf.android.ServersListViewModel
import com.github.alunegov.bleconf.android.domain.Server
import com.github.alunegov.bleconf.android.domain.ServersModel
import kotlinx.coroutines.launch

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
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
            )
        },
    ) { contentPadding ->
        if (model.errorText.isNotEmpty()) {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(model.errorText)
            }
        }

        Column(
            modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding()),
        ) {
            if (model.servers.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(model.servers) {
                        ServerItem(it, onServerClicked)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                ) {
                    EmptyPlaceHolder(stringResource(R.string.no_servers))
                }
            }
        }
    }
}

@Preview
@Preview(locale = "ru")
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
@Preview(locale = "ru")
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

/**
 * Сервер (элемент списка серверов).
 *
 * @param server Сервер.
 * @param onServerClicked Обработчик выбора сервера.
 */
@Composable
fun ServerItem(
    server: Server,
    onServerClicked: (String) -> Unit,
) {
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
            modifier = Modifier.fillMaxWidth(),
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
