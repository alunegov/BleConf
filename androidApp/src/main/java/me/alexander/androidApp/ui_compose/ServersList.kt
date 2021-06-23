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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.alexander.androidApp.RootScreen
import me.alexander.androidApp.ServersListViewModel
import me.alexander.androidApp.address

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
