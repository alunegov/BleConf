package com.github.alunegov.bleconf.android.ui_compose

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.text.DateFormat
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
            if (!confState.value.isAuthed) {
                viewModel.resetAuthError()
            }
        }
    }

    ServerConfEntryScreen(
        serverName = viewModel.serverName,
        confModel = confState.value,
        timeModel = timeState.value,
        onAuthClicked = { viewModel.authConf(it) },
        onPwdChanged = { viewModel.resetAuthError() },
        onConfRefresh = {
            viewModel.reloadConf()
            viewModel.reloadTime()
        },
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
 * @param onPwdChanged Обработчик изменения пароля.
 * @param onConfRefresh Обработчик обновления/загрузки настроек.
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
    onPwdChanged: () -> Unit,
    onConfRefresh: () -> Unit,
    onSetConfClicked: (Conf) -> Unit,
    onBackClicked: () -> Unit,
    currentRoute: String?,
    onRouteClicked: (String) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    if (confModel.errorText.isNotEmpty() or timeModel.errorText.isNotEmpty()) {
        LaunchedEffect(scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar(
                confModel.errorText + timeModel.errorText,
                //actionLabel = "Reload",
            )
            //onConfRefresh()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { ServerAppBar(serverName, onBackClicked) },
        bottomBar = { ServerBottomBar(currentRoute, onRouteClicked) },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding()),
        ) {
            if (confModel.isAuthed) {
                ServerConfEdit(
                    confModel,
                    timeModel,
                    onConfRefresh,
                    onSetConfClicked,
                    onConfValidateError = {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(it)
                        }
                    },
                )
            } else {
                ServerConfAuth(
                    onAuthClicked,
                    onPwdChanged,
                )
            }
        }
    }
}

@Preview
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
        onPwdChanged = {},
        onConfRefresh = {},
        onSetConfClicked = {},
        onBackClicked = {},
        currentRoute = ServerScreen.Conf.route,
        onRouteClicked = {},
    )
}

@Preview
@Preview(locale = "ru")
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
        onPwdChanged = {},
        onConfRefresh = {},
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
 * @param onConfRefresh Обработчик обновления/загрузки настроек.
 * @param onSetConfClicked Обработчик задания настроек.
 * @param onConfValidateError Обработчик ошибки валидации значений.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServerConfEdit(
    confModel: ConfModel,
    timeModel: TimeModel,
    onConfRefresh: () -> Unit,
    onSetConfClicked: (Conf) -> Unit,
    onConfValidateError: (String) -> Unit,
) {
    val adcCoeff = remember(confModel.conf.adcCoeff) { mutableStateOf(confModel.conf.adcCoeff.toString()) }
    val adcEmonNum = remember(confModel.conf.adcEmonNum) { mutableStateOf(confModel.conf.adcEmonNum.toString()) }
    val adcAverNum = remember(confModel.conf.adcAverNum) { mutableStateOf(confModel.conf.adcAverNum.toString()) }
    val adcImbaNum = remember(confModel.conf.adcImbaNum) { mutableStateOf(confModel.conf.adcImbaNum.toString()) }
    val adcImbaMinCurrent = remember(confModel.conf.adcImbaMinCurrent) { mutableStateOf(confModel.conf.adcImbaMinCurrent.toString()) }
    val adcImbaMinSwing = remember(confModel.conf.adcImbaMinSwing) { mutableStateOf(confModel.conf.adcImbaMinSwing.toString()) }
    val adcImbaThreshold = remember(confModel.conf.adcImbaThreshold) { mutableStateOf(confModel.conf.adcImbaThreshold.toString()) }

    val (
        adcCoeffFocus,
        adcEmonNumFocus,
        adcAverNumFocus,
        adcImbaNumFocus,
        adcImbaMinCurrentFocus,
        adcImbaMinSwingFocus,
        adcImbaThresholdFocus,
        applyFocus,
    ) = remember { FocusRequester.createRefs() }

    val keyboardController = LocalSoftwareKeyboardController.current
    //val focusManager = LocalFocusManager.current

    val swipeRefreshState = rememberSwipeRefreshState(confModel.loading or timeModel.loading)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = onConfRefresh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.system_time),
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.h5,
            )

            Divider()

            val formattedTime = DateFormat.getDateTimeInstance().format(Date(timeModel.time * 1000))
            Text(
                text = formattedTime,
                modifier = Modifier.padding(8.dp),
            )

            Text(
                text = stringResource(R.string.system_conf),
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.h5,
            )

            Divider()

            listOf(
                ConfItem(
                    adcCoeff,
                    stringResource(R.string.adc_coeff),
                    stringResource(R.string.adc_coeff_helper),
                    adcCoeffFocus,
                    adcEmonNumFocus,
                ),
                ConfItem(
                    adcEmonNum,
                    stringResource(R.string.adc_emon_num),
                    stringResource(R.string.adc_emon_num_helper),
                    adcEmonNumFocus,
                    adcAverNumFocus,
                ),
                ConfItem(
                    adcAverNum,
                    stringResource(R.string.adc_aver_num),
                    stringResource(R.string.adc_aver_num_helper),
                    adcAverNumFocus,
                    adcImbaNumFocus,
                ),
                ConfItem(
                    adcImbaNum,
                    stringResource(R.string.adc_imba_num),
                    stringResource(R.string.adc_imba_num_helper),
                    adcImbaNumFocus,
                    adcImbaMinCurrentFocus,
                ),
                ConfItem(
                    adcImbaMinCurrent,
                    stringResource(R.string.adc_imba_min_current),
                    stringResource(R.string.adc_imba_min_current_helper),
                    adcImbaMinCurrentFocus,
                    adcImbaMinSwingFocus,
                ),
                ConfItem(
                    adcImbaMinSwing,
                    stringResource(R.string.adc_imba_min_swing),
                    stringResource(R.string.adc_imba_min_swing_helper),
                    adcImbaMinSwingFocus,
                    adcImbaThresholdFocus
                ),
                ConfItem(
                    adcImbaThreshold,
                    stringResource(R.string.adc_imba_threshold),
                    stringResource(R.string.adc_imba_threshold_helper),
                    adcImbaThresholdFocus,
                    applyFocus
                ),
            ).forEach { confItem ->
                val helperText: @Composable (() -> Unit)? =
                    if (confItem.helper.isNotEmpty()) {
                        @Composable { Text(confItem.helper) }
                    } else null

                OutlinedTextFieldWithHelper(
                    value = confItem.item.value,
                    onValueChange = { confItem.item.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .focusRequester(confItem.focusRequester),
                    label = { Text(confItem.label) },
                    helper = helperText,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (confItem.nextFocusRequester == applyFocus) {
                                keyboardController?.hide()
                                //focusManager.clearFocus()
                            }
                            confItem.nextFocusRequester.requestFocus()
                        },
                    ),
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
                        onConfValidateError(e.message ?: e.toString())
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(250.dp)
                    .padding(8.dp)
                    .focusRequester(applyFocus),
            ) {
                Text(stringResource(R.string.apply_conf))
            }
        }
    }
}

data class ConfItem(
    val item: MutableState<String>,
    val label: String,
    val helper: String,
    val focusRequester: FocusRequester,
    val nextFocusRequester: FocusRequester,
)

/**
 * Авторизация.
 *
 * @param onAuthClicked Обработчик авторизации.
 * @param onPwdChanged Обработчик изменения пароля.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServerConfAuth(
    onAuthClicked: (String) -> Unit,
    onPwdChanged: () -> Unit,
) {
    var pwd by remember { mutableStateOf("") }
    var pwdVisible by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    //val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.authorization),
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5,
        )

        OutlinedTextField(
            value = pwd,
            onValueChange = {
                pwd = it
                onPwdChanged()
            },
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
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    //focusManager.clearFocus()
                    onAuthClicked(pwd)
                },
            ),
            singleLine = true,
        )

        Button(
            onClick = {
                keyboardController?.hide()
                //focusManager.clearFocus()
                onAuthClicked(pwd)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(250.dp)
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.do_auth))
        }
    }
}
