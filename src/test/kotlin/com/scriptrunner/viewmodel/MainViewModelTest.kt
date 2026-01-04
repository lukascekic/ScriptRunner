package com.scriptrunner.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import com.scriptrunner.model.ErrorLocation
import com.scriptrunner.model.ExecutionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MainViewModelTest {

    @Test
    fun `initial state is Idle`() {
        val viewModel = MainViewModel()

        assertIs<ExecutionState.Idle>(viewModel.executionState.value)
    }

    @Test
    fun `navigateToError moves cursor to correct offset`() {
        val viewModel = MainViewModel()
        val code = "line1\nline2\nline3"
        viewModel.updateScript(TextFieldValue(code))

        // Navigate to line 2, column 3
        viewModel.navigateToError(ErrorLocation(line = 2, column = 3, message = "error"))

        // Expected offset: "line1\n" (6 chars) + 2 chars = 8
        val expectedOffset = 8
        assertEquals(expectedOffset, viewModel.scriptContent.value.selection.start)
    }
}
