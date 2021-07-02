package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.alexander.androidApp.RootScreen
import me.alexander.androidApp.ServersListViewModel
import me.alexander.androidApp.address
import me.alexander.androidApp.domain.Server
import me.alexander.androidApp.domain.ServersModel

private const val TAG = "ServersList"

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

    ServersListScreen(
        model = state.value,
        onServerClicked = { serverAddress ->
            address = serverAddress

            navController.navigate(RootScreen.Server.route) {
                //launchSingleTop = true
                //restoreState = true
            }
        }
    )
}

@Composable
fun ServersListScreen(
    model: ServersModel,
    onServerClicked: (String) -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Text("BleConn")
            },
        )

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
                            onServerClicked(server.address)
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

@Preview
@Composable
fun ServersListScreenPreview() {
    ServersListScreen(
        model = ServersModel(
            servers = listOf(
                Server("1", "Server 1", -10),
                Server("2", "Server 2", -20),
            ),
            errorText = "Error",
        ),
        onServerClicked = {},
    )
}
