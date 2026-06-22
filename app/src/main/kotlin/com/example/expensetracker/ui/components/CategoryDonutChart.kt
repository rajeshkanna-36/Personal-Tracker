package com.example.expensetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

val categoryColors = mapOf(
    "General" to Color(0xFF6366F1),
    "Materials" to Color(0xFFF59E0B),
    "Labor" to Color(0xFF10B981),
    "Transport" to Color(0xFF3B82F6),
    "Software" to Color(0xFF8B5CF6),
    "Medicine" to Color(0xFFEF4444),
    "Travel" to Color(0xFF0EA5E9),
    "Food" to Color(0xFFF97316),
    "Groceries" to Color(0xFF22C55E),
    "Entertainment" to Color(0xFFA855F7),
    "Other" to Color(0xFFEC4899)
)

val categoryEmojis = mapOf(
    "General" to "🛒",
    "Materials" to "🧱",
    "Labor" to "👷",
    "Transport" to "🚚",
    "Software" to "💻",
    "Medicine" to "💊",
    "Travel" to "✈️",
    "Food" to "🍔",
    "Groceries" to "🛒",
    "Entertainment" to "🍿",
    "Other" to "📦"
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
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = 24.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = (slice.amount / totalSpent * 360f * animProgress.value).toFloat()
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "spent",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.take(5).forEach { slice ->
                val pct = (slice.amount / totalSpent * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = slice.color)
                    }
                    Text(
                        "${slice.emoji} ${slice.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
