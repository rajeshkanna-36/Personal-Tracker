package com.example.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

val LocalHazeState = compositionLocalOf<HazeState?> { null }

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    
    val baseModifier = modifier
        .shadow(
            elevation = 8.dp,
            shape = shape,
            spotColor = Color.Black.copy(alpha = 0.15f),
            ambientColor = Color.Black.copy(alpha = 0.05f)
        )
        .clip(shape)

    val hazeModifier = if (hazeState != null) {
        baseModifier.hazeChild(
            state = hazeState,
            shape = shape,
            style = HazeStyle(
                tint = Color.Transparent,
                blurRadius = 12.dp,
                noiseFactor = 0f
            )
        )
    } else {
        baseModifier
    }

    Column(
        modifier = hazeModifier
            .background(
                color = Color.Transparent
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = shape
            ),
        content = content
    )
}
