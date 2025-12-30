package com.scriptrunner.model

data class ErrorLocation(
    val line: Int,
    val column: Int,
    val message: String
)
