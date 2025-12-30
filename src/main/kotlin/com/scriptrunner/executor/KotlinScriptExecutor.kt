package com.scriptrunner.executor

import com.scriptrunner.model.ExecutionEvent
import com.scriptrunner.model.OutputLine
import com.scriptrunner.model.Script
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.coroutines.coroutineContext

class KotlinScriptExecutor : ScriptExecutor {

    private val logger = LoggerFactory.getLogger(KotlinScriptExecutor::class.java)

    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var process: Process? = null

    override fun execute(script: Script): Flow<ExecutionEvent> = flow {
        val tempFile = File.createTempFile("script", ".${script.language.extension}")

        try {
            _isRunning.value = true
            logger.debug("Writing script to {}", tempFile.absolutePath)

            tempFile.writeText(script.content)

            val command = script.language.command + tempFile.absolutePath
            logger.debug("Executing: {}", command.joinToString(" "))

            val processBuilder = ProcessBuilder(command)
                .redirectErrorStream(true)

            process = processBuilder.start()
            val proc = process ?: return@flow

            val reader = proc.inputStream.bufferedReader()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (!coroutineContext.isActive) break
                emit(ExecutionEvent.Output(OutputLine(line!!)))
            }
            reader.close()

            val exitCode = proc.waitFor()
            logger.debug("Script finished with exit code {}", exitCode)
            emit(ExecutionEvent.Finished(exitCode))

        } catch (e: Exception) {
            logger.error("Script execution failed", e)
            emit(ExecutionEvent.Failed(e.message ?: "Unknown error"))
        } finally {
            _isRunning.value = false
            process = null
            tempFile.delete()
            logger.debug("Cleaned up temp file")
        }
    }.flowOn(Dispatchers.IO)

    override fun cancel() {
        logger.debug("Cancelling script execution")
        process?.destroyForcibly()
        process = null
        _isRunning.value = false
    }
}
