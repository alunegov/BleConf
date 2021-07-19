package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.alexander.androidApp.*
import me.alexander.androidApp.R
import me.alexander.androidApp.domain.Conf
import java.util.*

//private const val TAG = "ServerConf"

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
            Error(confModel.errorText, timeModel.errorText)
        }

        Column(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth(),
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
fun ServerConfEdit(
    confModel: ConfModel,
    timeModel: TimeModel,
    onSetConfClicked: (Conf) -> Unit,
) {
    val adcCoeff = remember(confModel.conf.adcCoeff) { mutableStateOf(confModel.conf.adcCoeff.toString()) }
    val adcEmonNum = remember(confModel.conf.adcEmonNum) { mutableStateOf(confModel.conf.adcEmonNum.toString()) }
    val adcAverNum = remember(confModel.conf.adcAverNum) { mutableStateOf(confModel.conf.adcAverNum.toString()) }
    val adcImbaNum = remember(confModel.conf.adcImbaNum) { mutableStateOf(confModel.conf.adcImbaNum.toString()) }
    val adcImbaMinCurrent = remember(confModel.conf.adcImbaMinCurrent) { mutableStateOf(confModel.conf.adcImbaMinCurrent.toString()) }
    val adcImbaMinSwing = remember(confModel.conf.adcImbaMinSwing) { mutableStateOf(confModel.conf.adcImbaMinSwing.toString()) }
    val adcImbaThreshold = remember(confModel.conf.adcImbaThreshold) { mutableStateOf(confModel.conf.adcImbaThreshold.toString()) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.system_time),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.h5
        )

        Divider()

        Text(
            text = Date(timeModel.time * 1000).toLocaleString(),
            modifier = Modifier.padding(8.dp),
        )

        Text(
            text = stringResource(R.string.system_conf),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.h5
        )

        Divider()

        listOf(
            ConfItem(adcCoeff, stringResource(R.string.adc_coeff)),
            ConfItem(adcEmonNum, stringResource(R.string.adc_emon_num)),
            ConfItem(adcAverNum, stringResource(R.string.adc_aver_num)),
            ConfItem(adcImbaNum, stringResource(R.string.adc_imba_num)),
            ConfItem(adcImbaMinCurrent, stringResource(R.string.adc_imba_min_current)),
            ConfItem(adcImbaMinSwing, stringResource(R.string.adc_imba_min_swing)),
            ConfItem(adcImbaThreshold, stringResource(R.string.adc_imba_threshold)),
        ).forEach { confItem ->
            OutlinedTextField(
                value = confItem.item.value,
                onValueChange = { confItem.item.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text(confItem.label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        }

        Button(
            onClick = {
                // TODO: convert error
                Conf(
                    adcCoeff.value.toFloat(),
                    adcEmonNum.value.toInt(),
                    adcAverNum.value.toInt(),
                    adcImbaNum.value.toInt(),
                    adcImbaMinCurrent.value.toFloat(),
                    adcImbaMinSwing.value.toFloat(),
                    adcImbaThreshold.value.toFloat(),
                ).also { onSetConfClicked(it) }
            },
            modifier = Modifier
                .align(Alignment.End)
                .widthIn(100.dp)
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.apply_conf))
        }
    }
}

@Composable
fun ServerConfAuth(
    onAuthClicked: (String) -> Unit,
) {
    var pwd by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.authorization),
            modifier = Modifier
                //.fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5,
        )

        OutlinedTextField(
            value = pwd,
            onValueChange = { pwd = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
        )

        Button(
            onClick = { onAuthClicked(pwd) },
            modifier = Modifier
                .align(Alignment.End)
                .widthIn(100.dp)
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.do_auth))
        }
    }
}

data class ConfItem(
    val item: MutableState<String>,
    val label: String,
)
