package com.scriptrunner

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scriptrunner.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Script Runner",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                App()
            }
        }
    }
}
