package com.scriptrunner.model

data class OutputLine(
    val text: String,
    val isError: Boolean = false,
    val errorLocation: ErrorLocation? = null
)
