package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Текст ошибки.
 */
@Composable
fun Error(vararg strs: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        strs.forEach {
            if (it.isNotEmpty()) {
                Text(
                    text = it,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colors.error,
                )
            }
        }
    }
}
