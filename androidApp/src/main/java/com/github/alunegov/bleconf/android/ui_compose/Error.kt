package com.github.alunegov.bleconf.android.ui_compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

@Preview
@Composable
fun ErrorPreview() {
    Error("e1", "", "e3", "e4")
}
