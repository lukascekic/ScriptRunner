package com.scriptrunner.highlighting

import androidx.compose.ui.text.AnnotatedString
import com.scriptrunner.model.ScriptLanguage

interface SyntaxHighlighter {
    val language: ScriptLanguage
    fun highlight(code: String): AnnotatedString
    fun highlight(code: String, cursorOffset: Int): AnnotatedString = highlight(code)
}
