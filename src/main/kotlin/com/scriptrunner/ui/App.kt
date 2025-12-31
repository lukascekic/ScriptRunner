package com.scriptrunner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.scriptrunner.model.ExecutionState
import com.scriptrunner.ui.components.EditorPane
import com.scriptrunner.ui.components.OutputPane
import com.scriptrunner.ui.components.StatusBar
import com.scriptrunner.ui.components.Toolbar
import com.scriptrunner.viewmodel.MainViewModel

@Composable
fun App() {
    val viewModel = remember { MainViewModel() }
    val scope = rememberCoroutineScope()
    val editorFocusRequester = remember { FocusRequester() }

    val isRunning = viewModel.executionState.value is ExecutionState.Running

    Column(modifier = Modifier.fillMaxSize()) {
        Toolbar(
            isRunning = isRunning,
            onRun = { viewModel.runScript(scope) },
            onStop = viewModel::stopScript
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            EditorPane(
                value = viewModel.scriptContent.value,
                onValueChange = viewModel::updateScript,
                focusRequester = editorFocusRequester,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 4.dp)
            )

            OutputPane(
                lines = viewModel.outputLines,
                clearGeneration = viewModel.clearGeneration.value,
                onClear = viewModel::clearOutput,
                onErrorClick = { error ->
                    viewModel.navigateToError(error)
                    editorFocusRequester.requestFocus()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 4.dp)
            )
        }

        StatusBar(executionState = viewModel.executionState.value)
    }
}
