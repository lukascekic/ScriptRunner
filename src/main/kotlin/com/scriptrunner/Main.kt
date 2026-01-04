package com.scriptrunner

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scriptrunner.ui.App
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.scriptrunner.Main")

fun main() = application {
    logger.info("Script Runner application started")
    Window(
        onCloseRequest = ::exitApplication,
        title = "Script Runner",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        App()
    }
}
