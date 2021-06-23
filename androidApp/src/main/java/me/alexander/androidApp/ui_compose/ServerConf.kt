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
import me.alexander.androidApp.domain.Conf
import java.util.*

@Composable
fun ServerConf(
    viewModel: ServerViewModel,
    navController: NavController,
) {
    val confState = viewModel.conf.collectAsState()
    val timeState = viewModel.time.collectAsState()

    DisposableEffect(true) {
        //Log.d(TAG, "ServerConf DisposableEffect")
        viewModel.reloadConf()
        viewModel.reloadTime()
        onDispose {
            //Log.d(TAG, "ServerConf DisposableEffect onDispose")
        }
    }

    Column {
        ServerAppBar(viewModel.serverName, navController)

        val confModel = confState.value
        val timeModel = timeState.value

        if (confModel.errorText.isNotEmpty() || timeModel.errorText.isNotEmpty()) {
            Column {
                if (confModel.errorText.isNotEmpty()) {
                    Text(
                        text = confModel.errorText,
                        color = Color.Red,
                    )
                }

                if (timeModel.errorText.isNotEmpty()) {
                    Text(
                        text = timeModel.errorText,
                        color = Color.Red,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1.0f)
                .padding(8.dp, 8.dp),
        ) {
            Text(Date(timeModel.time * 1000).toString())

            Button(onClick = { viewModel.setConf(Conf()) }) {
                Text("SetConf")
            }
        }

        ServerBottomBar(navController)
    }
}
