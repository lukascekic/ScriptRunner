package com.scriptrunner.completion

data class CompletionItem(
    val text: String,
    val displayText: String = text,
    val type: CompletionType
)
