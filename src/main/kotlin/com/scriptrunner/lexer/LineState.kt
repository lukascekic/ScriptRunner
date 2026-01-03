package com.scriptrunner.lexer

/**
 * Stores the cached tokenization state for a single line.
 *
 * @property tokens The tokens found on this line
 * @property lineContent The original line content (for dirty detection)
 * @property startState The lexer state at the beginning of this line
 * @property endState The lexer state at the end of this line (for propagation to next line)
 */
data class LineState(
    val tokens: List<Token>,
    val lineContent: String,
    val startState: Int,
    val endState: Int
)
