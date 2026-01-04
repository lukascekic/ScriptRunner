package com.scriptrunner.executor

import com.scriptrunner.model.ExecutionEvent
import com.scriptrunner.model.Script
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class KotlinScriptExecutorTest {

    private val executor = KotlinScriptExecutor()

    @Test
    fun `simple println outputs correctly`() = runTest(timeout = 2.minutes) {
        val script = Script("""println("Hello")""")
        val events = executor.execute(script).toList()

        assertEquals(2, events.size)
        assertIs<ExecutionEvent.Output>(events[0])
        assertEquals("Hello", (events[0] as ExecutionEvent.Output).line.text)
        assertIs<ExecutionEvent.Finished>(events[1])
    }

    @Test
    fun `multiple lines output in order`() = runTest(timeout = 2.minutes) {
        val script = Script("""
            println("Line 1")
            println("Line 2")
            println("Line 3")
        """.trimIndent())
        val events = executor.execute(script).toList()

        val outputs = events.filterIsInstance<ExecutionEvent.Output>()
        assertEquals(3, outputs.size)
        assertEquals("Line 1", outputs[0].line.text)
        assertEquals("Line 2", outputs[1].line.text)
        assertEquals("Line 3", outputs[2].line.text)
    }

    @Test
    fun `successful script returns exit code 0`() = runTest(timeout = 2.minutes) {
        val script = Script("""println("OK")""")
        val events = executor.execute(script).toList()

        val finished = events.last()
        assertIs<ExecutionEvent.Finished>(finished)
        assertEquals(0, finished.exitCode)
    }

    @Test
    fun `syntax error returns non-zero exit code`() = runTest(timeout = 2.minutes) {
        val script = Script("""this is not valid kotlin""")
        val events = executor.execute(script).toList()

        val finished = events.last()
        assertIs<ExecutionEvent.Finished>(finished)
        assertTrue(finished.exitCode != 0)
    }

    @Test
    fun `isRunning is false before and after execution`() = runTest(timeout = 2.minutes) {
        assertFalse(executor.isRunning.value)

        val script = Script("""println("test")""")
        executor.execute(script).toList()

        assertFalse(executor.isRunning.value)
    }
}
