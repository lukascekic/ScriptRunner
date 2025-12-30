package com.scriptrunner.model

import kotlin.time.Duration

sealed class ExecutionState {
    data object Idle : ExecutionState()
    data object Running : ExecutionState()
    data class Completed(val exitCode: Int, val duration: Duration) : ExecutionState()
    data class Error(val message: String) : ExecutionState()
}
