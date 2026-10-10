package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialPrimaryDark

data class DailySalesData(
    val dayLabel: String,
    val totalSales: Double,
    val orderCount: Int
)

@Composable
fun SalesChart(
    dailyData: List<DailySalesData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GRAFIK PENJUALAN 7 HARI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EditorialMuted,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Aktivitas Pendapatan Mingguan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                }
                val totalWeek = dailyData.sumOf { it.totalSales }
                Text(
                    text = CurrencyUtils.formatRupiah(totalWeek),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EditorialPrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            val maxSales = (dailyData.maxOfOrNull { it.totalSales } ?: 1.0).coerceAtLeast(10000.0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(125.dp)) {
                    val width = size.width
                    val height = size.height
                    val barCount = dailyData.size
                    if (barCount == 0) return@Canvas

                    val barWidth = (width / barCount) * 0.42f
                    val stepX = width / barCount

                    // Draw subtle grid lines
                    val gridColor = Color(0x1F535F70)
                    for (i in 1..3) {
                        val y = height * (i / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    dailyData.forEachIndexed { index, data ->
                        val barHeight = ((data.totalSales / maxSales).toFloat() * (height - 10f)).coerceAtLeast(6f)
                        val startX = (index * stepX) + (stepX - barWidth) / 2f
                        val startY = height - barHeight

                        val brush = Brush.verticalGradient(
                            colors = listOf(EditorialPrimaryBlue, EditorialPrimaryDark),
                            startY = startY,
                            endY = height
                        )

                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(startX, startY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                }
            }

            // Labels below bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dailyData.forEach { data ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = data.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = EditorialMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
