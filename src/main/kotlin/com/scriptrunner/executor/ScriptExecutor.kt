package com.scriptrunner.executor

import com.scriptrunner.model.ExecutionEvent
import com.scriptrunner.model.Script
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ScriptExecutor {
    val isRunning: StateFlow<Boolean>
    fun execute(script: Script): Flow<ExecutionEvent>
    fun cancel()
}
