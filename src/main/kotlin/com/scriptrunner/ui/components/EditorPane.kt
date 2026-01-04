package com.scriptrunner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scriptrunner.completion.CompletionItem
import com.scriptrunner.highlighting.KotlinSyntaxHighlighter
import com.scriptrunner.highlighting.SyntaxColors
import com.scriptrunner.highlighting.SyntaxHighlighter

@Composable
fun EditorPane(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    isDarkTheme: Boolean = false,
    // Completion parameters
    completionItems: List<CompletionItem> = emptyList(),
    completionVisible: Boolean = false,
    selectedCompletionIndex: Int = 0,
    onCompletionAccept: (CompletionItem) -> Unit = {},
    onCompletionDismiss: () -> Unit = {},
    onCompletionUp: () -> Unit = {},
    onCompletionDown: () -> Unit = {},
    onCompletionTrigger: () -> Unit = {}
) {
    val syntaxColors = if (isDarkTheme) SyntaxColors.dark else SyntaxColors.light
    val highlighter: SyntaxHighlighter = remember(isDarkTheme) {
        KotlinSyntaxHighlighter(syntaxColors)
    }

    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Use cursor position for bracket matching
    val cursorOffset = value.selection.start

    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

    val syntaxTransformation = remember(highlighter, value.text, cursorOffset) {
        VisualTransformation { text ->
            TransformedText(highlighter.highlight(text.text, cursorOffset), OffsetMapping.Identity)
        }
    }

    val density = LocalDensity.current

    // Calculate popup position based on cursor
    var popupOffset by remember { mutableStateOf(IntOffset.Zero) }
    LaunchedEffect(textLayoutResult, cursorOffset, scrollState.value) {
        textLayoutResult?.let { layout ->
            if (cursorOffset <= layout.layoutInput.text.length) {
                val cursorRect = layout.getCursorRect(cursorOffset)
                with(density) {
                    popupOffset = IntOffset(
                        x = (cursorRect.left + 12.dp.toPx()).toInt(),
                        y = (cursorRect.bottom - scrollState.value + 12.dp.toPx()).toInt()
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val viewportHeight = constraints.maxHeight.toFloat()

            // Auto-scroll to keep cursor visible - only if cursor is outside visible area
            LaunchedEffect(value.text.length, value.selection.start) {
                delay(50)
                textLayoutResult?.let { layout ->
                    val cursorPos = value.selection.start.coerceAtMost(layout.layoutInput.text.length)
                    val cursorRect = layout.getCursorRect(cursorPos)

                    val currentScroll = scrollState.value.toFloat()
                    val cursorTop = cursorRect.top
                    val cursorBottom = cursorRect.bottom

                    // Define visible area with some padding
                    val visibleTop = currentScroll
                    val visibleBottom = currentScroll + viewportHeight
                    val padding = 50f

                    // Only scroll if cursor is outside visible area
                    when {
                        cursorTop < visibleTop + padding -> {
                            scrollState.scrollTo((cursorTop - padding).coerceAtLeast(0f).toInt())
                        }
                        cursorBottom > visibleBottom - padding -> {
                            scrollState.scrollTo((cursorBottom - viewportHeight + padding).coerceAtLeast(0f).toInt())
                        }
                    }
                }
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .focusRequester(focusRequester)
                    .verticalScroll(scrollState)
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                        when {
                            // Ctrl+Space - trigger completion
                            event.isCtrlPressed && event.key == Key.Spacebar -> {
                                onCompletionTrigger()
                                true
                            }
                            // Arrow keys when popup visible
                            completionVisible && event.key == Key.DirectionDown -> {
                                onCompletionDown()
                                true
                            }
                            completionVisible && event.key == Key.DirectionUp -> {
                                onCompletionUp()
                                true
                            }
                            // Enter/Tab to accept
                            completionVisible && (event.key == Key.Enter || event.key == Key.Tab) -> {
                                if (completionItems.isNotEmpty() && selectedCompletionIndex in completionItems.indices) {
                                    onCompletionAccept(completionItems[selectedCompletionIndex])
                                    true
                                } else false
                            }
                            // Escape to dismiss
                            completionVisible && event.key == Key.Escape -> {
                                onCompletionDismiss()
                                true
                            }
                            else -> false
                        }
                    }
                    .drawBehind {
                        textLayoutResult?.let { layout ->
                            val textLength = layout.layoutInput.text.length
                            if (cursorOffset <= textLength) {
                                val currentLine = layout.getLineForOffset(cursorOffset.coerceAtMost(textLength))
                                val lineTop = layout.getLineTop(currentLine)
                                val lineBottom = layout.getLineBottom(currentLine)
                                drawRect(
                                    color = highlightColor,
                                    topLeft = Offset(0f, lineTop),
                                    size = Size(size.width, lineBottom - lineTop)
                                )
                            }
                        }
                    },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = syntaxColors.default
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
                                color = syntaxColors.default.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Completion popup overlay
        if (completionVisible && completionItems.isNotEmpty()) {
            CompletionPopup(
                items = completionItems,
                selectedIndex = selectedCompletionIndex,
                onItemClick = onCompletionAccept,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.offset { popupOffset }
            )
        }
    }
}
