package com.scriptrunner.lexer

data class Token(
    val type: TokenType,
    val text: String,
    val startOffset: Int,
    val endOffset: Int,
    val line: Int,
    val column: Int
) {
    val length: Int get() = endOffset - startOffset
}
