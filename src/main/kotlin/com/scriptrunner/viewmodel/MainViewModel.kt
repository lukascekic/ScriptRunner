package com.scriptrunner.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.scriptrunner.completion.CompletionItem
import com.scriptrunner.completion.KeywordCompletionProvider
import com.scriptrunner.executor.KotlinScriptExecutor
import com.scriptrunner.executor.ScriptExecutor
import com.scriptrunner.model.ErrorLocation
import com.scriptrunner.model.ExecutionEvent
import com.scriptrunner.model.ExecutionState
import com.scriptrunner.model.OutputLine
import com.scriptrunner.model.Script
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.TimeSource

/** Main application state and business logic. */
class MainViewModel {

    private val executor: ScriptExecutor = KotlinScriptExecutor()
    private var executionJob: Job? = null
    private var startTime: TimeSource.Monotonic.ValueTimeMark? = null
    private val completionProvider = KeywordCompletionProvider()

    val scriptContent = mutableStateOf(TextFieldValue(""))
    val outputLines = mutableStateListOf<OutputLine>()
    val executionState = mutableStateOf<ExecutionState>(ExecutionState.Idle)
    val clearGeneration = mutableStateOf(0)
    val isDarkTheme = mutableStateOf(false)

    // Completion state
    val completionItems = mutableStateOf<List<CompletionItem>>(emptyList())
    val completionVisible = mutableStateOf(false)
    val selectedCompletionIndex = mutableStateOf(0)
    private var completionPrefix = ""
    private var previousText = ""

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    /** Executes the current script content. */
    fun runScript(scope: CoroutineScope) {
        if (executionState.value is ExecutionState.Running) return

        outputLines.clear()
        clearGeneration.value++
        executionState.value = ExecutionState.Running
        startTime = TimeSource.Monotonic.markNow()

        val script = Script(scriptContent.value.text)

        executionJob = scope.launch {
            executor.execute(script).collect { event ->
                when (event) {
                    is ExecutionEvent.Output -> {
                        outputLines.add(event.line)
                    }
                    is ExecutionEvent.Finished -> {
                        val duration = startTime?.elapsedNow() ?: Duration.ZERO
                        executionState.value = ExecutionState.Completed(event.exitCode, duration)
                    }
                    is ExecutionEvent.Failed -> {
                        executionState.value = ExecutionState.Error(event.error)
                    }
                }
            }
        }
    }

    /** Stops the currently running script. */
    fun stopScript() {
        executor.cancel()
        executionJob?.cancel()
        executionJob = null
        val duration = startTime?.elapsedNow() ?: Duration.ZERO
        executionState.value = ExecutionState.Completed(-1, duration)
    }

    fun clearOutput() {
        outputLines.clear()
        clearGeneration.value++
    }

    fun updateScript(value: TextFieldValue) {
        val textChanged = value.text != previousText
        previousText = value.text
        scriptContent.value = value

        if (textChanged) {
            // Auto-trigger completion when typing identifier characters (letters or underscore only)
            val cursor = value.selection.start
            if (cursor > 0) {
                val lastChar = value.text.getOrNull(cursor - 1)
                if (lastChar != null && (lastChar.isLetter() || lastChar == '_')) {
                    updateCompletions()
                } else {
                    dismissCompletion()
                }
            } else {
                dismissCompletion()
            }
        } else if (completionVisible.value) {
            // Cursor moved without text change - dismiss completion
            dismissCompletion()
        }
    }

    /** Moves cursor to the error location in the editor. */
    fun navigateToError(error: ErrorLocation) {
        val text = scriptContent.value.text
        val lines = text.lines()

        // Calculate character offset for line:column
        val lineIndex = (error.line - 1).coerceIn(0, lines.size - 1)
        var offset = 0
        for (i in 0 until lineIndex) {
            offset += lines[i].length + 1 // +1 for newline
        }
        offset += (error.column - 1).coerceIn(0, lines.getOrNull(lineIndex)?.length ?: 0)
        offset = offset.coerceIn(0, text.length)

        scriptContent.value = scriptContent.value.copy(
            selection = TextRange(offset)
        )
    }

    /** Updates completion suggestions based on current cursor position. */
    fun updateCompletions() {
        val code = scriptContent.value.text
        val cursorOffset = scriptContent.value.selection.start
        val context = completionProvider.createContext(code, cursorOffset)

        completionPrefix = context.prefix
        val items = completionProvider.getCompletions(context)

        completionItems.value = items
        completionVisible.value = items.isNotEmpty()
        selectedCompletionIndex.value = 0
    }

    /** Shows completion popup manually (e.g., Ctrl+Space). */
    fun showCompletions() {
        updateCompletions()
    }

    /** Accepts the selected completion item and inserts it into the editor. */
    fun acceptCompletion(item: CompletionItem) {
        val code = scriptContent.value.text
        val cursorOffset = scriptContent.value.selection.start

        // Recalculate context to get fresh prefix
        val context = completionProvider.createContext(code, cursorOffset)
        val currentPrefix = context.prefix

        // Safety checks
        if (currentPrefix.length > cursorOffset) {
            dismissCompletion()
            return
        }

        // Replace prefix with completion text
        val prefixStart = (cursorOffset - currentPrefix.length).coerceAtLeast(0)
        val newText = code.substring(0, prefixStart) + item.text + code.substring(cursorOffset)
        val newCursor = prefixStart + item.text.length

        // Update text without triggering auto-completion
        previousText = newText
        scriptContent.value = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )

        dismissCompletion()
    }

    /** Dismisses the completion popup. */
    fun dismissCompletion() {
        completionVisible.value = false
        completionItems.value = emptyList()
        selectedCompletionIndex.value = 0
        completionPrefix = ""
    }

    /** Moves selection up in completion list. */
    fun completionUp() {
        val items = completionItems.value
        if (items.isEmpty()) return
        selectedCompletionIndex.value = (selectedCompletionIndex.value - 1 + items.size) % items.size
    }

    /** Moves selection down in completion list. */
    fun completionDown() {
        val items = completionItems.value
        if (items.isEmpty()) return
        selectedCompletionIndex.value = (selectedCompletionIndex.value + 1) % items.size
    }

    /** Accepts currently selected completion. Returns true if there was something to accept. */
    fun acceptSelectedCompletion(): Boolean {
        val items = completionItems.value
        val index = selectedCompletionIndex.value
        if (items.isEmpty() || index !in items.indices) return false
        acceptCompletion(items[index])
        return true
    }
}
