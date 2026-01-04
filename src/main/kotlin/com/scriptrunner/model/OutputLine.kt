package com.scriptrunner.model

/** A single line of script output. */
data class OutputLine(
    val text: String,
    val isError: Boolean = false,
    val errorLocation: ErrorLocation? = null
)
