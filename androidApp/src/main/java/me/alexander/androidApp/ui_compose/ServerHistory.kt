package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private const val TAG = "ServerHistory"

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

    Column {
        ServerAppBar(viewModel.serverName, navController)

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
            items(model.events) { event ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp, 8.dp),
                ) {
                    Text(Date(event.time * 1000).toString())

                    //Spacer(Modifier.size(16.dp))

                    //Text(event.en.toString())

                    Row {
                        for (i in 0..7) {
                            val en = (event.en and (1 shl i)) != 0

                            Spacer(Modifier.size(16.dp))

                            Column {
                                Text((i + 1).toString(), color = if (en) Color.Unspecified else Color.Gray)

                                if (en) {
                                    Text("on")
                                } else {
                                    Text("off", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        ServerBottomBar(navController)
    }
}
