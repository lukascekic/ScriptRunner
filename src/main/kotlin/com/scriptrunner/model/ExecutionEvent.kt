package com.scriptrunner.model

sealed class ExecutionEvent {
    data class Output(val line: OutputLine) : ExecutionEvent()
    data class Finished(val exitCode: Int) : ExecutionEvent()
    data class Failed(val error: String) : ExecutionEvent()
}
