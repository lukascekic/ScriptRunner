package com.scriptrunner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scriptrunner.highlighting.SyntaxColors
import com.scriptrunner.viewmodel.EditorViewModel

/**
 * Adjusts highlighting styles from old text to new text by shifting positions.
 * This allows showing approximate highlighting while async computation runs.
 */
private fun adjustHighlighting(oldHighlighted: AnnotatedString, newText: String): AnnotatedString {
    val oldText = oldHighlighted.text
    if (oldText == newText) return oldHighlighted
    if (oldText.isEmpty()) return AnnotatedString(newText)

    // Find first difference position
    val diffStart = oldText.indices.firstOrNull { i ->
        i >= newText.length || oldText[i] != newText[i]
    } ?: oldText.length

    val lengthDiff = newText.length - oldText.length

    return buildAnnotatedString {
        append(newText)
        oldHighlighted.spanStyles.forEach { span ->
            val newStart = if (span.start >= diffStart) span.start + lengthDiff else span.start
            val newEnd = if (span.end > diffStart) span.end + lengthDiff else span.end
            if (newStart >= 0 && newEnd <= newText.length && newStart < newEnd) {
                addStyle(span.item, newStart, newEnd)
            }
        }
    }
}

@Composable
fun EditorPane(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val scrollState = rememberScrollState()
    val text by viewModel.text.collectAsState()
    val highlightedText by viewModel.highlightedText.collectAsState()

    // Keep track of last valid highlighted text to avoid gray flashes
    var lastHighlightedText by remember { mutableStateOf(AnnotatedString("")) }

    // Update lastHighlightedText when we get a matching highlight
    LaunchedEffect(highlightedText, text.text) {
        if (highlightedText.text == text.text && highlightedText.text.isNotEmpty()) {
            lastHighlightedText = highlightedText
        }
    }

    val syntaxTransformation = VisualTransformation { _ ->
        val displayText = when {
            // Perfect match - use current highlighting
            highlightedText.text == text.text -> highlightedText
            // Adjust old highlighting to new text (shift style positions)
            lastHighlightedText.text.isNotEmpty() -> adjustHighlighting(lastHighlightedText, text.text)
            // Fallback - plain text (only for initial state)
            else -> AnnotatedString(text.text)
        }
        TransformedText(displayText, OffsetMapping.Identity)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        BasicTextField(
            value = text,
            onValueChange = viewModel::onTextChanged,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .focusRequester(focusRequester)
                .verticalScroll(scrollState),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = SyntaxColors.default
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = syntaxTransformation,
            decorationBox = { innerTextField ->
                if (text.text.isEmpty()) {
                    Text(
                        text = "// Write your Kotlin script here...",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = SyntaxColors.default.copy(alpha = 0.5f)
                        )
                    )
                }
                innerTextField()
            }
        )
    }
}
