package com.scriptrunner.completion

data class CompletionContext(
    val code: String,
    val cursorOffset: Int,
    val prefix: String,
    val isInStringOrComment: Boolean
)
