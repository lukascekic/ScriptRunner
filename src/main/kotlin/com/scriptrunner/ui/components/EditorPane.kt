package com.scriptrunner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scriptrunner.highlighting.KotlinSyntaxHighlighter
import com.scriptrunner.highlighting.SyntaxColors
import com.scriptrunner.highlighting.SyntaxHighlighter

@Composable
fun EditorPane(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    highlighter: SyntaxHighlighter = remember { KotlinSyntaxHighlighter() }
) {
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Use cursor position for bracket matching
    val cursorOffset = value.selection.start

    val syntaxTransformation = remember(highlighter, value.text, cursorOffset) {
        VisualTransformation { text ->
            TransformedText(highlighter.highlight(text.text, cursorOffset), OffsetMapping.Identity)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        val viewportHeight = constraints.maxHeight.toFloat()

        // Auto-scroll to keep cursor visible
        LaunchedEffect(value.text.length, value.selection.start) {
            delay(50)
            textLayoutResult?.let { layout ->
                val cursorPos = value.selection.start.coerceAtMost(layout.layoutInput.text.length)
                val cursorRect = layout.getCursorRect(cursorPos)
                val targetScroll = (cursorRect.bottom - viewportHeight + 100).coerceAtLeast(0f).toInt()
                scrollState.scrollTo(targetScroll)
            }
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
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
            onTextLayout = { textLayoutResult = it },
            visualTransformation = syntaxTransformation,
            decorationBox = { innerTextField ->
                if (value.text.isEmpty()) {
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
