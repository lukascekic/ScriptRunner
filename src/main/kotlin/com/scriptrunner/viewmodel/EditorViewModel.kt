package com.scriptrunner.viewmodel

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import com.scriptrunner.highlighting.KotlinSyntaxHighlighter
import com.scriptrunner.highlighting.SyntaxHighlighter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditorViewModel(
    private val highlighter: SyntaxHighlighter = KotlinSyntaxHighlighter()
) {
    private val _text = MutableStateFlow(TextFieldValue(""))
    val text: StateFlow<TextFieldValue> = _text.asStateFlow()

    val highlightedText: StateFlow<AnnotatedString> = highlighter.highlightedText

    fun onTextChanged(value: TextFieldValue) {
        _text.value = value
        highlighter.requestHighlight(value.text, value.selection.start)
    }

    fun setText(value: TextFieldValue) {
        _text.value = value
        highlighter.requestHighlight(value.text, value.selection.start)
    }
}
