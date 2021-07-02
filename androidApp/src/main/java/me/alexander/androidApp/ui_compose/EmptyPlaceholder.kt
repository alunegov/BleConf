package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ColumnScope.EmptyPlaceHolder(text: String) {
    Column(
        modifier = Modifier
            .weight(1.0f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.h5,
        )
    }
}
