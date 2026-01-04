package com.scriptrunner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scriptrunner.completion.CompletionItem
import com.scriptrunner.completion.CompletionType

@Composable
fun CompletionPopup(
    items: List<CompletionItem>,
    selectedIndex: Int,
    onItemClick: (CompletionItem) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to selected item
    LaunchedEffect(selectedIndex) {
        if (items.isNotEmpty() && selectedIndex in items.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    val backgroundColor = if (isDarkTheme) Color(0xFF2B2D30) else Color(0xFFF7F8FA)
    val borderColor = if (isDarkTheme) Color(0xFF43454A) else Color(0xFFD1D1D1)
    val selectedColor = if (isDarkTheme) Color(0xFF2E436E) else Color(0xFFCCE4FF)
    val textColor = if (isDarkTheme) Color(0xFFBCBEC4) else Color(0xFF1E1E1E)

    Box(
        modifier = modifier
            .width(300.dp)
            .heightIn(max = 200.dp)
            .background(backgroundColor)
            .border(1.dp, borderColor)
    ) {
        LazyColumn(state = listState) {
            itemsIndexed(items) { index, item ->
                CompletionItemRow(
                    item = item,
                    isSelected = index == selectedIndex,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    isDarkTheme = isDarkTheme,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun CompletionItemRow(
    item: CompletionItem,
    isSelected: Boolean,
    selectedColor: Color,
    textColor: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val typeColor = when (item.type) {
        CompletionType.KEYWORD -> if (isDarkTheme) Color(0xFFCF8E6D) else Color(0xFF793CA1)
        CompletionType.BUILTIN -> if (isDarkTheme) Color(0xFF56A8F5) else Color(0xFF0057A8)
        CompletionType.TYPE -> if (isDarkTheme) Color(0xFF56A8F5) else Color(0xFF0057A8)
    }

    val typeLabel = when (item.type) {
        CompletionType.KEYWORD -> "K"
        CompletionType.BUILTIN -> "F"
        CompletionType.TYPE -> "T"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) selectedColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = typeLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = typeColor,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = item.displayText,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = textColor
        )
    }
}
