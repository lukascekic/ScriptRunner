package com.scriptrunner.model

import kotlin.time.Duration

/** Current state of script execution. */
sealed class ExecutionState {
    /** No script running. */
    data object Idle : ExecutionState()
    /** Script is currently executing. */
    data object Running : ExecutionState()
    /** Script finished with exit code and duration. */
    data class Completed(val exitCode: Int, val duration: Duration) : ExecutionState()
    /** Script failed to start. */
    data class Error(val message: String) : ExecutionState()
}
