package com.scriptrunner.highlighting

import androidx.compose.ui.text.AnnotatedString
import com.scriptrunner.model.ScriptLanguage

/**
 * Applies syntax highlighting to source code.
 */
interface SyntaxHighlighter {
    /** The language this highlighter supports. */
    val language: ScriptLanguage

    /** Returns highlighted code as an AnnotatedString. */
    fun highlight(code: String): AnnotatedString

    /** Returns highlighted code with bracket matching at cursor position. */
    fun highlight(code: String, cursorOffset: Int): AnnotatedString = highlight(code)
}
