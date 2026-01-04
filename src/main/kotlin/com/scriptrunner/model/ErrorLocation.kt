package com.scriptrunner.model

/** Location of an error in the script (1-based line and column). */
data class ErrorLocation(
    val line: Int,
    val column: Int,
    val message: String
)
