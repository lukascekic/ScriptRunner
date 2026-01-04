package com.scriptrunner.model

/** Events emitted during script execution. */
sealed class ExecutionEvent {
    /** A line of output from the script. */
    data class Output(val line: OutputLine) : ExecutionEvent()
    /** Script completed with exit code. */
    data class Finished(val exitCode: Int) : ExecutionEvent()
    /** Script execution failed. */
    data class Failed(val error: String) : ExecutionEvent()
}
