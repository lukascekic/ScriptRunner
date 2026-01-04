package com.scriptrunner.highlighting

import androidx.compose.ui.text.AnnotatedString
import com.scriptrunner.model.ScriptLanguage
import kotlinx.coroutines.flow.StateFlow

interface SyntaxHighlighter {
    val language: ScriptLanguage
    val highlightedText: StateFlow<AnnotatedString>
    fun requestHighlight(code: String, cursorOffset: Int)
}
