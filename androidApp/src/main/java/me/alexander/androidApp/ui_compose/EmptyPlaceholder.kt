package me.alexander.androidApp.ui_compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.EmptyPlaceHolder(text: String) {
    Column(
        modifier = Modifier
            .weight(1.0f)
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5,
        )
    }
}
