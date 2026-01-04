package com.scriptrunner.executor

import com.scriptrunner.model.ExecutionEvent
import com.scriptrunner.model.Script
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Executes scripts and streams output events.
 */
interface ScriptExecutor {
    /** Whether a script is currently running. */
    val isRunning: StateFlow<Boolean>

    /** Executes the script and emits output/completion events. */
    fun execute(script: Script): Flow<ExecutionEvent>

    /** Cancels the currently running script. */
    fun cancel()
}
