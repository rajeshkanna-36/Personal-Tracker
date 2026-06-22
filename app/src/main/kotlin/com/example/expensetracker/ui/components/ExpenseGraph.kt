package com.example.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.local.ExpenseEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseGraph(
    expenses: List<ExpenseEntity>,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📊", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No data to display",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    // Group expenses by day (last 7 days)
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val last7Days = (6 downTo 0).map { i ->
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -i)
        cal.timeInMillis
    }

    val dayLabels = last7Days.map { millis ->
        SimpleDateFormat("EEE", Locale.getDefault()).format(Date(millis))
    }

    val grouped = expenses.filter { it.type == "Expense" }.groupBy { expense ->
        val exCal = Calendar.getInstance().apply { timeInMillis = expense.date }
        exCal.set(Calendar.HOUR_OF_DAY, 0)
        exCal.set(Calendar.MINUTE, 0)
        exCal.set(Calendar.SECOND, 0)
        exCal.set(Calendar.MILLISECOND, 0)
        exCal.timeInMillis
    }

    val dataPoints = last7Days.map { day ->
        grouped[day]?.sumOf { it.amount } ?: 0.0
    }

    val maxAmount = dataPoints.maxOrNull()?.takeIf { it > 0 } ?: 1.0

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 8.dp)
        ) {
            val width = size.width
            val height = size.height - 24.dp.toPx() // leave room for labels above bars
            val barCount = dataPoints.size
            val totalSpacing = width * 0.3f
            val barWidth = (width - totalSpacing) / barCount
            val spacing = totalSpacing / (barCount + 1)

            dataPoints.forEachIndexed { index, amount ->
                val barHeight = (amount / maxAmount * height * 0.85f).toFloat()
                val startX = spacing + index * (barWidth + spacing)
                val startY = height - barHeight + 20.dp.toPx()

                // Background track
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(startX, 20.dp.toPx()),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )

                // Value bar
                if (barHeight > 0) {
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(startX, startY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }

                // Amount label above bar
                if (amount > 0) {
                    val label = if (amount >= 1000) "₹${(amount / 1000).toInt()}k" else "₹${amount.toInt()}"
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        startX + barWidth / 2,
                        startY - 8.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textColor.toArgb()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            }
        }

        // Day labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
