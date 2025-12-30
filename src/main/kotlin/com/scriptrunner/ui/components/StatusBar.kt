package com.scriptrunner.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scriptrunner.model.ExecutionState

@Composable
fun StatusBar(
    executionState: ExecutionState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (executionState) {
                is ExecutionState.Idle -> {
                    Text(
                        text = "Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is ExecutionState.Running -> {
                    RunningIndicator()
                }
                is ExecutionState.Completed -> {
                    val exitColor = if (executionState.exitCode == 0) Color(0xFF4CAF50) else Color.Red
                    Text(
                        text = "Exit: ${executionState.exitCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = exitColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = formatDuration(executionState.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is ExecutionState.Error -> {
                    Text(
                        text = "Error: ${executionState.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun RunningIndicator() {
    val transition = rememberInfiniteTransition(label = "running")
    val dotCount by transition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    val dots = ".".repeat(dotCount.toInt())
    Text(
        text = "Running$dots",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary
    )
}

private fun formatDuration(duration: kotlin.time.Duration): String {
    val seconds = duration.inWholeMilliseconds / 1000.0
    return "%.2fs".format(seconds)
}
