package com.example.expensetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.local.ExpenseEntity
import java.text.NumberFormat
import java.util.Locale

data class CategorySlice(
    val category: String,
    val amount: Double,
    val color: Color,
    val emoji: String
)

// Vibrant colors for the chart
val categoryColors = mapOf(
    "General" to Color(0xFF6E56CF),    // Deep Purple
    "Materials" to Color(0xFFFF9500),  // Bright Orange
    "Labor" to Color(0xFF34C759),      // Neon Green
    "Transport" to Color(0xFF007AFF),  // Blue
    "Software" to Color(0xFFAF52DE),   // Violet
    "Medicine" to Color(0xFFFF3B30),   // Red
    "Travel" to Color(0xFF5AC8FA),     // Cyan
    "Food" to Color(0xFFFF2D55),       // Pink
    "Groceries" to Color(0xFF32ADE6),  // Light Blue
    "Entertainment" to Color(0xFFFFCC00), // Yellow
    "Other" to Color(0xFF8E8E93),      // Grey
    "Seeds" to Color(0xFF4CAF50),      // Green
    "Fertilizer" to Color(0xFF8BC34A), // Light Green
    "Pesticides" to Color(0xFFFF5722), // Deep Orange
    "Equipment" to Color(0xFF607D8B),  // Blue Grey
    "Stocks" to Color(0xFF2196F3),     // Blue
    "Mutual Funds" to Color(0xFF03A9F4), // Light Blue
    "Real Estate" to Color(0xFF795548), // Brown
    "Bonds" to Color(0xFF9C27B0),      // Purple
    "Crypto" to Color(0xFFFFC107)      // Amber
)

val categoryEmojis = mapOf(
    "General" to "🛍️",
    "Materials" to "🧱",
    "Labor" to "👷",
    "Transport" to "🚚",
    "Software" to "💻",
    "Medicine" to "💊",
    "Travel" to "✈️",
    "Food" to "🍔",
    "Groceries" to "🛒",
    "Entertainment" to "🍿",
    "Other" to "📦",
    "Seeds" to "🌱",
    "Fertilizer" to "🪴",
    "Pesticides" to "🧪",
    "Equipment" to "🚜",
    "Stocks" to "📈",
    "Mutual Funds" to "💼",
    "Real Estate" to "🏠",
    "Bonds" to "📜",
    "Crypto" to "₿"
)

@Composable
fun CategoryDonutChart(
    expenses: List<ExpenseEntity>,
    modifier: Modifier = Modifier
) {
    val expenseOnly = expenses.filter { it.type == "Expense" }
    if (expenseOnly.isEmpty()) return

    val totalSpent = expenseOnly.sumOf { it.amount }
    val slices = expenseOnly
        .groupBy { it.category }
        .map { (category, items) ->
            CategorySlice(
                category = category,
                amount = items.sumOf { it.amount },
                color = categoryColors[category] ?: Color(0xFF6B7280),
                emoji = categoryEmojis[category] ?: "📦"
            )
        }
        .sortedByDescending { it.amount }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(1200)) // Smoother entrance
    }

    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    // Adapt to Material Theme dynamically
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val textColor = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val strokeWidth = 32.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                var startAngle = -90f
                // Draw slices
                slices.forEach { slice ->
                    val sweep = (slice.amount / totalSpent * 360f * animProgress.value).toFloat()
                    
                    val gap = if (slices.size > 1) 2.5f else 0f
                    val actualSweep = maxOf(0.1f, sweep - gap)

                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = actualSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweep
                }
            }
            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    formatter.format(totalSpent),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = textColor
                )
                Text(
                    "spent",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = labelColor
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            slices.take(4).forEach { slice ->
                val pct = (slice.amount / totalSpent * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )
                    Text(
                        "${slice.emoji} ${slice.category}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = slice.color 
                    )
                }
            }
        }
    }
}
