package com.scriptrunner.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.scriptrunner.executor.KotlinScriptExecutor
import com.scriptrunner.executor.ScriptExecutor
import com.scriptrunner.model.ErrorLocation
import com.scriptrunner.model.ExecutionEvent
import com.scriptrunner.model.ExecutionState
import com.scriptrunner.model.OutputLine
import com.scriptrunner.model.Script
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.TimeSource

/** Main application state and business logic. */
class MainViewModel {

    private val executor: ScriptExecutor = KotlinScriptExecutor()
    private var executionJob: Job? = null
    private var startTime: TimeSource.Monotonic.ValueTimeMark? = null

    val scriptContent = mutableStateOf(TextFieldValue(""))
    val outputLines = mutableStateListOf<OutputLine>()
    val executionState = mutableStateOf<ExecutionState>(ExecutionState.Idle)
    val clearGeneration = mutableStateOf(0)
    val isDarkTheme = mutableStateOf(false)

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    /** Executes the current script content. */
    fun runScript(scope: CoroutineScope) {
        if (executionState.value is ExecutionState.Running) return

        outputLines.clear()
        clearGeneration.value++
        executionState.value = ExecutionState.Running
        startTime = TimeSource.Monotonic.markNow()

        val script = Script(scriptContent.value.text)

        executionJob = scope.launch {
            executor.execute(script).collect { event ->
                when (event) {
                    is ExecutionEvent.Output -> {
                        outputLines.add(event.line)
                    }
                    is ExecutionEvent.Finished -> {
                        val duration = startTime?.elapsedNow() ?: Duration.ZERO
                        executionState.value = ExecutionState.Completed(event.exitCode, duration)
                    }
                    is ExecutionEvent.Failed -> {
                        executionState.value = ExecutionState.Error(event.error)
                    }
                }
            }
        }
    }

    /** Stops the currently running script. */
    fun stopScript() {
        executor.cancel()
        executionJob?.cancel()
        executionJob = null
        val duration = startTime?.elapsedNow() ?: Duration.ZERO
        executionState.value = ExecutionState.Completed(-1, duration)
    }

    fun clearOutput() {
        outputLines.clear()
        clearGeneration.value++
    }

    fun updateScript(value: TextFieldValue) {
        scriptContent.value = value
    }

    /** Moves cursor to the error location in the editor. */
    fun navigateToError(error: ErrorLocation) {
        val text = scriptContent.value.text
        val lines = text.lines()

        // Calculate character offset for line:column
        val lineIndex = (error.line - 1).coerceIn(0, lines.size - 1)
        var offset = 0
        for (i in 0 until lineIndex) {
            offset += lines[i].length + 1 // +1 for newline
        }
        offset += (error.column - 1).coerceIn(0, lines.getOrNull(lineIndex)?.length ?: 0)
        offset = offset.coerceIn(0, text.length)

        scriptContent.value = scriptContent.value.copy(
            selection = TextRange(offset)
        )
    }
}
