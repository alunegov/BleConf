package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.alexander.androidApp.*
import me.alexander.androidApp.domain.Conf
import java.util.*

private const val TAG = "ServerConf"

@Composable
fun ServerConfEntry(
    viewModel: ServerViewModel,
    navController: NavController,
) {
    val confState = viewModel.conf.collectAsState()
    val timeState = viewModel.time.collectAsState()

    DisposableEffect(confState.value.isAuthed) {
        //Log.d(TAG, "ServerConf DisposableEffect")
        if (confState.value.isAuthed) {
            viewModel.reloadConf()
            viewModel.reloadTime()
        }
        onDispose {
            //Log.d(TAG, "ServerConf DisposableEffect onDispose")
        }
    }

    ServerConfEntryScreen(
        serverName = viewModel.serverName,
        confModel = confState.value,
        timeModel = timeState.value,
        onAuthClicked = { viewModel.authConf(it) },
        onSetConfClicked = { viewModel.setConf(it) },
        onBackClicked = { navController.popBackStack() },
        currentRoute = getCurrentRoute(navController),
        onRouteClicked = createNavigateToRouteClicked(navController),
    )
}

@Composable
fun ServerConfEntryScreen(
    serverName: String,
    confModel: ConfModel,
    timeModel: TimeModel,
    onAuthClicked: (String) -> Unit,
    onSetConfClicked: (Conf) -> Unit,
    onBackClicked: () -> Unit,
    currentRoute: String?,
    onRouteClicked: (String) -> Unit,
) {
    Column {
        ServerAppBar(serverName, onBackClicked)

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
            if (confModel.isAuthed) {
                ServerConfEdit(confModel, timeModel, onSetConfClicked)
            } else {
                ServerConfAuth(onAuthClicked)
            }
        }

        ServerBottomBar(currentRoute, onRouteClicked)
    }
}

@Preview
@Composable
fun ServerConfEntryScreenPreview_Authed() {
    ServerConfEntryScreen(
        serverName = "Server",
        confModel = ConfModel(
            isAuthed = true,
            conf = Conf(),
            errorText = "Conf error",
        ),
        timeModel = TimeModel(
            time = 1,
            errorText = "Time error",
        ),
        onAuthClicked = {},
        onSetConfClicked = {},
        onBackClicked = {},
        currentRoute = ServerScreen.Conf.route,
        onRouteClicked = {},
    )
}

@Preview
@Composable
fun ServerConfEntryScreenPreview_NotAuthed() {
    ServerConfEntryScreen(
        serverName = "Server",
        confModel = ConfModel(
            isAuthed = false,
            conf = Conf(),
            errorText = "Conf error",
        ),
        timeModel = TimeModel(
            time = 1,
            errorText = "Time error",
        ),
        onAuthClicked = {},
        onSetConfClicked = {},
        onBackClicked = {},
        currentRoute = ServerScreen.Conf.route,
        onRouteClicked = {},
    )
}

@Composable
fun ServerConfAuth(
    onAuthClicked: (String) -> Unit,
) {
    Column {
        var pwd by remember { mutableStateOf("") }

        OutlinedTextField(
            value = pwd,
            onValueChange = { pwd = it },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
        )

        Button(
            onClick = { onAuthClicked(pwd) },
        ) {
            Text("Auth")
        }
    }
}

@Composable
fun ServerConfEdit(
    confModel: ConfModel,
    timeModel: TimeModel,
    onSetConfClicked: (Conf) -> Unit,
) {
    Column {
        Text(Date(timeModel.time * 1000).toString())

        var adcCoeff by remember(confModel.conf.adcCoeff) { mutableStateOf(confModel.conf.adcCoeff.toString()) }
        var adcEmonNum by remember(confModel.conf.adcEmonNum) { mutableStateOf(confModel.conf.adcEmonNum.toString()) }
        var adcAverNum by remember(confModel.conf.adcAverNum) { mutableStateOf(confModel.conf.adcAverNum.toString()) }
        var adcImbaNum by remember(confModel.conf.adcImbaNum) { mutableStateOf(confModel.conf.adcImbaNum.toString()) }
        var adcImbaMinCurrent by remember(confModel.conf.adcImbaMinCurrent) { mutableStateOf(confModel.conf.adcImbaMinCurrent.toString()) }
        var adcImbaMinSwing by remember(confModel.conf.adcImbaMinSwing) { mutableStateOf(confModel.conf.adcImbaMinSwing.toString()) }
        var adcImbaThreshold by remember(confModel.conf.adcImbaThreshold) { mutableStateOf(confModel.conf.adcImbaThreshold.toString()) }

        OutlinedTextField(
            value = adcCoeff,
            onValueChange = { adcCoeff = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        OutlinedTextField(
            value = adcEmonNum,
            onValueChange = { adcEmonNum = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        OutlinedTextField(
            value = adcAverNum,
            onValueChange = { adcAverNum = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        OutlinedTextField(
            value = adcImbaNum,
            onValueChange = { adcImbaNum = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        OutlinedTextField(
            value = adcImbaMinCurrent,
            onValueChange = { adcImbaMinCurrent = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        OutlinedTextField(
            value = adcImbaMinSwing,
            onValueChange = { adcImbaMinSwing = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        OutlinedTextField(
            value = adcImbaThreshold,
            onValueChange = { adcImbaThreshold = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        Button(
            onClick = {
                Conf(
                    adcCoeff.toFloat(),
                    adcEmonNum.toInt(),
                    adcAverNum.toInt(),
                    adcImbaNum.toInt(),
                    adcImbaMinCurrent.toFloat(),
                    adcImbaMinSwing.toFloat(),
                    adcImbaThreshold.toFloat(),
                ).also { onSetConfClicked(it) }
            },
        ) {
            Text("SetConf")
        }
    }
}
