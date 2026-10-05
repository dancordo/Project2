package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun InteractiveCandleChart(
    candles: List<Candle>,
    indicators: TechnicalIndicators,
    selectedTimeframe: Timeframe,
    onTimeframeSelected: (Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    // Indicator toggles
    var showEma20 by remember { mutableStateOf(true) }
    var showEma50 by remember { mutableStateOf(true) }
    var showEma200 by remember { mutableStateOf(false) }
    var showVwap by remember { mutableStateOf(true) }
    var showAvwap by remember { mutableStateOf(false) }
    var showVolumeProfile by remember { mutableStateOf(true) }
    var showStructure by remember { mutableStateOf(true) }

    // Touch inspection
    var touchX by remember { mutableStateOf<Float?>(null) }
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalCard)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .testTag("interactive_chart")
    ) {
        // Timeframe selector bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Timeframe.values().forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) TerminalCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) TerminalCyan else TerminalBorder,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onTimeframeSelected(tf) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tf.label,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            color = if (isSelected) TerminalCyan else TextSecondary
                        )
                    }
                }
            }

            // Quick legend/toggle chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ChartToggleChip("EMA", showEma20 || showEma50) {
                    val next = !showEma20
                    showEma20 = next
                    showEma50 = next
                }
                ChartToggleChip("VWAP", showVwap) { showVwap = !showVwap }
                ChartToggleChip("AVWAP", showAvwap) { showAvwap = !showAvwap }
                ChartToggleChip("VP", showVolumeProfile) { showVolumeProfile = !showVolumeProfile }
                ChartToggleChip("BOS", showStructure) { showStructure = !showStructure }
            }
        }

        // Active inspection readout
        val lastCandle = candles.lastOrNull()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (lastCandle != null) {
                Text(
                    text = "O: ${lastCandle.open.format(2)} H: ${lastCandle.high.format(2)} L: ${lastCandle.low.format(2)} C: ${lastCandle.close.format(2)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Vol: ${(lastCandle.volume / 1000).format(1)}k",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchX = offset.x
                            tryAwaitRelease()
                            touchX = null
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            touchX = change.position.x
                        },
                        onDragEnd = { touchX = null },
                        onDragCancel = { touchX = null }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (candles.isEmpty()) return@Canvas

                val chartWidth = size.width
                val chartHeight = size.height
                val priceHeight = chartHeight * 0.78f
                val volHeight = chartHeight * 0.20f
                val volTop = chartHeight * 0.80f

                val visibleCount = min(candles.size, 55)
                val visibleCandles = candles.takeLast(visibleCount)

                val minPrice = visibleCandles.minOf { it.low }
                val maxPrice = visibleCandles.maxOf { it.high }
                val priceRange = max(maxPrice - minPrice, 0.001)

                val maxVol = max(visibleCandles.maxOf { it.volume }, 1.0)

                val candleWidth = chartWidth / visibleCount
                val bodyWidth = max(candleWidth * 0.65f, 2f)

                fun priceToY(p: Double): Float {
                    return (priceHeight - ((p - minPrice) / priceRange * priceHeight)).toFloat()
                }

                // Grid lines (3 horizontal price lines)
                for (step in 1..3) {
                    val p = minPrice + priceRange * (step / 4.0)
                    val y = priceToY(p)
                    drawLine(
                        color = TerminalBorderSubtle,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = p.format(1),
                        topLeft = Offset(chartWidth - 45f, y - 14f),
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Volume Profile on right (if enabled)
                if (showVolumeProfile) {
                    val vp = indicators.volumeProfile
                    val pocY = priceToY(vp.poc)
                    val vahY = priceToY(vp.vah)
                    val valY = priceToY(vp.valPrice)

                    // Horizontal lines for POC, VAH, VAL
                    drawLine(TerminalCyan.copy(alpha = 0.6f), Offset(chartWidth * 0.6f, pocY), Offset(chartWidth, pocY), 1.5f)
                    drawText(textMeasurer, "POC", Offset(chartWidth - 28f, pocY - 12f), TextStyle(color = TerminalCyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace))

                    drawLine(BullGreen.copy(alpha = 0.4f), Offset(chartWidth * 0.6f, vahY), Offset(chartWidth, vahY), 1f)
                    drawText(textMeasurer, "VAH", Offset(chartWidth - 28f, vahY - 12f), TextStyle(color = BullGreen, fontSize = 8.sp, fontFamily = FontFamily.Monospace))

                    drawLine(BearRed.copy(alpha = 0.4f), Offset(chartWidth * 0.6f, valY), Offset(chartWidth, valY), 1f)
                    drawText(textMeasurer, "VAL", Offset(chartWidth - 28f, valY - 12f), TextStyle(color = BearRed, fontSize = 8.sp, fontFamily = FontFamily.Monospace))
                }

                // Session VWAP & AVWAP
                if (showVwap) {
                    val vwapY = priceToY(indicators.sessionVwap)
                    drawLine(
                        color = WarningAmber.copy(alpha = 0.8f),
                        start = Offset(0f, vwapY),
                        end = Offset(chartWidth, vwapY),
                        strokeWidth = 1.5f
                    )
                }
                if (showAvwap) {
                    val avwapY = priceToY(indicators.weeklyAvwap)
                    drawLine(
                        color = TerminalPurple.copy(alpha = 0.8f),
                        start = Offset(0f, avwapY),
                        end = Offset(chartWidth, avwapY),
                        strokeWidth = 1.2f
                    )
                }

                // Candlesticks and Volume Bars
                for (i in visibleCandles.indices) {
                    val c = visibleCandles[i]
                    val x = i * candleWidth + (candleWidth / 2f)

                    val isUp = c.close >= c.open
                    val candleColor = if (isUp) BullGreen else BearRed

                    val highY = priceToY(c.high)
                    val lowY = priceToY(c.low)
                    val openY = priceToY(c.open)
                    val closeY = priceToY(c.close)

                    // Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(x, highY),
                        end = Offset(x, lowY),
                        strokeWidth = 1.2f
                    )

                    // Body
                    val topY = min(openY, closeY)
                    val bottomY = max(openY, closeY)
                    val height = max(bottomY - topY, 1.5f)

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(x - bodyWidth / 2f, topY),
                        size = Size(bodyWidth, height)
                    )

                    // Volume Bar at bottom
                    val barHeight = ((c.volume / maxVol) * volHeight).toFloat()
                    val barTop = chartHeight - barHeight
                    drawRect(
                        color = candleColor.copy(alpha = 0.4f),
                        topLeft = Offset(x - bodyWidth / 2f, barTop),
                        size = Size(bodyWidth, barHeight)
                    )
                }

                // Market Structure Markers
                if (showStructure) {
                    indicators.structureMarkers.takeLast(4).forEach { marker ->
                        val index = (visibleCandles.size - 1) - (candles.size - 1 - marker.index)
                        if (index in 0 until visibleCandles.size) {
                            val x = index * candleWidth + (candleWidth / 2f)
                            val y = priceToY(marker.price)
                            val markerColor = if (marker.type.isBullish) BullGreen else BearRed

                            drawCircle(
                                color = markerColor,
                                radius = 3f,
                                center = Offset(x, y)
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = marker.type.code,
                                topLeft = Offset(x - 8f, if (marker.type.isBullish) y + 4f else y - 14f),
                                style = TextStyle(
                                    color = markerColor,
                                    fontSize = 7.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Crosshair on touch
                touchX?.let { tx ->
                    if (tx in 0f..chartWidth) {
                        drawLine(
                            color = TextSecondary.copy(alpha = 0.5f),
                            start = Offset(tx, 0f),
                            end = Offset(tx, chartHeight),
                            strokeWidth = 1f
                        )
                        val candleIdx = min((tx / candleWidth).toInt(), visibleCandles.size - 1)
                        if (candleIdx >= 0) {
                            val inspected = visibleCandles[candleIdx]
                            val cy = priceToY(inspected.close)
                            drawLine(
                                color = TextSecondary.copy(alpha = 0.5f),
                                start = Offset(0f, cy),
                                end = Offset(chartWidth, cy),
                                strokeWidth = 1f
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "$${inspected.close.format(2)}",
                                topLeft = Offset(4f, cy - 14f),
                                style = TextStyle(
                                    color = TerminalCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartToggleChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (isActive) TerminalBlue.copy(alpha = 0.2f) else TerminalSurface)
            .border(1.dp, if (isActive) TerminalBlue else TerminalBorderSubtle, RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isActive) TerminalBlue else TextMuted
        )
    }
}

private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
