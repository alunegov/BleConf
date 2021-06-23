package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.alexander.androidApp.ServerViewModel

@Composable
fun SensorsList(
    viewModel: ServerViewModel,
    navController: NavController,
) {
    val state = viewModel.sensors.collectAsState()

    DisposableEffect(true) {
        //Log.d(TAG, "SensorsList DisposableEffect")
        viewModel.reloadSensors()
        onDispose {
            //Log.d(TAG, "SensorsList DisposableEffect onDispose")
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
            items(model.sensors) { sensor ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp, 8.dp),
                ) {
                    Checkbox(
                        checked = sensor.enabled,
                        onCheckedChange = { checked -> viewModel.toggleEnabled(sensor.id, checked) },
                    )

                    Spacer(Modifier.size(16.dp))

                    Text(sensor.name)

                    Spacer(Modifier.size(16.dp))

                    Text(
                        when (sensor.state) {
                            0 -> "bad"
                            1 -> "good"
                            2 -> "unk"
                            else -> "unsupp ${sensor.state}"
                        }
                    )

                    if (sensor.coeff != null) {
                        Spacer(Modifier.size(16.dp))

                        Text(sensor.coeff.toString())
                    }
                }
            }
        }

        ServerBottomBar(navController)
    }
}
