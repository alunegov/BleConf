package me.alexander.androidApp.ui_compose

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable

/**
 * Верхняя панель окон сервера с именем сервера и кнопкой Назад.
 */
@Composable
fun ServerAppBar(
    title: String,
    onBackClicked: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.Filled.ArrowBack, null)
            }
        },
    )
}
