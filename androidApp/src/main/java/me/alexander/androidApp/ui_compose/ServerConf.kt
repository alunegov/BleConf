package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.alexander.androidApp.ServerViewModel
import java.util.*

@Composable
fun ServerConf(
    viewModel: ServerViewModel,
    navController: NavController,
) {
    val state = viewModel.conf.collectAsState()

    DisposableEffect(true) {
        //Log.d(TAG, "ServerConf DisposableEffect")
        viewModel.reloadConf()
        onDispose {
            //Log.d(TAG, "ServerConf DisposableEffect onDispose")
        }
    }

    Column {
        ServerAppBar(viewModel.serverName, navController)

        val model = state.value

        if (model.errorText.isNotEmpty()) {
            Text(
                text = model.errorText,
                color = Color.Red,
            )
        }

        Column(
            modifier = Modifier.weight(1.0f).padding(8.dp, 8.dp),
        ) {
            Text(Date(model.conf.time * 1000).toString())

            Button(onClick = { viewModel.syncTime() }) {
                Text("SyncTime")
            }

            /*Button(onClick = { viewModel.setConfOnly() }) {
                Text("SetConf")
            }*/
        }

        ServerBottomBar(navController)
    }
}
