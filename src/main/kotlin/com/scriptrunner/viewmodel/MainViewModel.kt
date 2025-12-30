package com.scriptrunner.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.scriptrunner.executor.KotlinScriptExecutor
import com.scriptrunner.executor.ScriptExecutor
import com.scriptrunner.model.ExecutionEvent
import com.scriptrunner.model.ExecutionState
import com.scriptrunner.model.OutputLine
import com.scriptrunner.model.Script
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.TimeSource

class MainViewModel {

    private val executor: ScriptExecutor = KotlinScriptExecutor()
    private var executionJob: Job? = null
    private var startTime: TimeSource.Monotonic.ValueTimeMark? = null

    val scriptContent = mutableStateOf(TextFieldValue(""))
    val outputLines = mutableStateListOf<OutputLine>()
    val executionState = mutableStateOf<ExecutionState>(ExecutionState.Idle)

    fun runScript(scope: CoroutineScope) {
        if (executionState.value is ExecutionState.Running) return

        outputLines.clear()
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

    fun stopScript() {
        executor.cancel()
        executionJob?.cancel()
        executionJob = null
        val duration = startTime?.elapsedNow() ?: Duration.ZERO
        executionState.value = ExecutionState.Completed(-1, duration)
    }

    fun clearOutput() {
        outputLines.clear()
    }

    fun updateScript(value: TextFieldValue) {
        scriptContent.value = value
    }
}
