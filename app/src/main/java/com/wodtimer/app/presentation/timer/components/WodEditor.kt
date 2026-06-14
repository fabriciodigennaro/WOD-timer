package com.wodtimer.app.presentation.timer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wodtimer.app.presentation.theme.*

@Composable
fun WodEditor(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 200.dp)
            .background(
                color = CardBackground,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            textStyle = TextStyle(
                color = TimerWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(Green),
            maxLines = 8,
            decorationBox = { innerTextField ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = "Write your WOD here...\nEMOM 12\n1: 10 Burpees\n2: 15 Wall Balls",
                            color = TimerDim,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun WodEditorHeader(
    wodTitle: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        BasicTextField(
            value = wodTitle,
            onValueChange = onTitleChange,
            textStyle = TextStyle(
                color = TimerWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(Green),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (wodTitle.isEmpty()) {
                        Text(
                            text = "Tap to add title",
                            color = TimerDim,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            }
        )
    }

    Spacer(modifier = Modifier.height(4.dp))
}
