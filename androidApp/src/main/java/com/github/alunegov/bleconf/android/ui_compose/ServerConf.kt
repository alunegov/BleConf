package com.github.alunegov.bleconf.android.ui_compose

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.alunegov.bleconf.android.*
import com.github.alunegov.bleconf.android.R
import com.github.alunegov.bleconf.android.domain.Conf
import java.util.*

private const val TAG = "ServerConf"

/**
 * Окно История.
 *
 * @param viewModel VM.
 * @param navController Navigation.
 */
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

/**
 * Окно История (preview-friendly).
 *
 * @param serverName Имя сервера.
 * @param confModel Модель настроек.
 * @param timeModel Модель системного времени сервера.
 * @param onAuthClicked Обработчик авторизации.
 * @param onSetConfClicked Обработчик задания настроек.
 * @param onBackClicked Обработчик навигации назад.
 * @param currentRoute Текущий роут.
 * @param onRouteClicked Обработчик навигации между окнами сервера.
 */
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

@Preview(locale = "ru")
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

/**
 * Настройки сервера.
 *
 * @param confModel Модель настроек.
 * @param timeModel Модель системного времени сервера.
 * @param onSetConfClicked Обработчик задания настроек.
 */
@Composable
fun ServerConfEdit(
    confModel: ConfModel,
    timeModel: TimeModel,
    onSetConfClicked: (Conf) -> Unit,
) {
    //val conf = remember(confModel.conf) { mutableStateOf(confModel.conf.toValidatableConf()) }

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
            ConfItem(adcCoeff, stringResource(R.string.adc_coeff), stringResource(R.string.adc_coeff_helper)),
            ConfItem(adcEmonNum, stringResource(R.string.adc_emon_num), stringResource(R.string.adc_emon_num_helper)),
            ConfItem(adcAverNum, stringResource(R.string.adc_aver_num), stringResource(R.string.adc_aver_num_helper)),
            ConfItem(adcImbaNum, stringResource(R.string.adc_imba_num), stringResource(R.string.adc_imba_num_helper)),
            ConfItem(adcImbaMinCurrent, stringResource(R.string.adc_imba_min_current), stringResource(R.string.adc_imba_min_current_helper)),
            ConfItem(adcImbaMinSwing, stringResource(R.string.adc_imba_min_swing), stringResource(R.string.adc_imba_min_swing_helper)),
            ConfItem(adcImbaThreshold, stringResource(R.string.adc_imba_threshold), stringResource(R.string.adc_imba_threshold_helper)),
            /*ConfItem(conf.value.adcCoeff, stringResource(R.string.adc_coeff), stringResource(R.string.adc_coeff_helper)),
            ConfItem(conf.value.adcEmonNum, stringResource(R.string.adc_emon_num), stringResource(R.string.adc_emon_num_helper)),
            ConfItem(conf.value.adcAverNum, stringResource(R.string.adc_aver_num), stringResource(R.string.adc_aver_num_helper)),
            ConfItem(conf.value.adcImbaNum, stringResource(R.string.adc_imba_num), stringResource(R.string.adc_imba_num_helper)),
            ConfItem(conf.value.adcImbaMinCurrent, stringResource(R.string.adc_imba_min_current), stringResource(R.string.adc_imba_min_current_helper)),
            ConfItem(conf.value.adcImbaMinSwing, stringResource(R.string.adc_imba_min_swing), stringResource(R.string.adc_imba_min_swing_helper)),
            ConfItem(conf.value.adcImbaThreshold, stringResource(R.string.adc_imba_threshold), stringResource(R.string.adc_imba_threshold_helper)),*/
        ).forEach { confItem ->
            val helperText: @Composable (() -> Unit)? =
                if (confItem.helper.isNotEmpty()) {
                    @Composable { Text(confItem.helper) }
                }
                else null

            OutlinedTextFieldWithHelper(
                value = confItem.item.value,
                onValueChange = { confItem.item.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text(confItem.label) },
                helper = helperText,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        }

        Button(
            onClick = {
                // TODO: pre validate Conf data
                try {
                    Conf(
                        adcCoeff.value.toFloat(),
                        adcEmonNum.value.toInt(),
                        adcAverNum.value.toInt(),
                        adcImbaNum.value.toInt(),
                        adcImbaMinCurrent.value.toFloat(),
                        adcImbaMinSwing.value.toFloat(),
                        adcImbaThreshold.value.toFloat(),
                    ).also { onSetConfClicked(it) }
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .widthIn(150.dp)
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.apply_conf))
        }
    }
}

/*data class ValidatableConf(
    var adcCoeff: MutableState<String>,
    var adcEmonNum: MutableState<String>,
    var adcAverNum: MutableState<String>,
    var adcImbaNum: MutableState<String>,
    var adcImbaMinCurrent: MutableState<String>,
    var adcImbaMinSwing: MutableState<String>,
    var adcImbaThreshold: MutableState<String>,
)

fun Conf.toValidatableConf(): ValidatableConf {
    return ValidatableConf()
}*/

data class ConfItem(
    val item: MutableState<String>,
    val label: String,
    val helper: String = "",
)

/**
 * Авторизация
 *
 * @param onAuthClicked Обработчик авторизации.
 */
@Composable
fun ServerConfAuth(
    onAuthClicked: (String) -> Unit,
) {
    var pwd by remember { mutableStateOf("") }
    var pwdVisible by remember { mutableStateOf(false) }

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
            trailingIcon = {
                IconButton(onClick = { pwdVisible = !pwdVisible }) {
                    Icon(if (pwdVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                }
            },
            visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
        )

        Button(
            onClick = { onAuthClicked(pwd) },
            modifier = Modifier
                .align(Alignment.End)
                .widthIn(150.dp)
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.do_auth))
        }
    }
}
