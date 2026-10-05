package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TerminalUiState
import com.example.viewmodel.TerminalViewModel

@Composable
fun FlowScreen(
    state: TerminalUiState,
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    val derivatives = state.derivatives
    val options = state.optionsContext

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("flow_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ماتریس جریان نقدینگی و بازار مشتقات",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TerminalCyan
                        )
                        DataStatusBadge(status = state.currentStatus)
                    }
                    Text(
                        text = "رصد واگرایی CVD، نرخ‌های فاندینگ بین صرافی‌ها و کلاسترهای لیکوئیدیشن",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Section 1: واگرایی اسپات و پرپچوال
        if (derivatives != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TerminalCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "واگرایی دلتای تجمعی حجم (Spot vs Perp CVD)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerminalSurface)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = derivatives.cvdDivergenceNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        MetricRow("دلتای تجمعی حجم اسپات", "$${"%.1f".format(derivatives.spotCvd / 1e6)}M", if (derivatives.spotCvd >= 0) BullGreen else BearRed)
                        MetricRow("دلتای تجمعی حجم پرپچوال", "$${"%.1f".format(derivatives.perpCvd / 1e6)}M", if (derivatives.perpCvd >= 0) BullGreen else BearRed)
                        MetricRow("نسبت حجم معاملات فیوچرز به اسپات", "${"%.2f".format(derivatives.perpSpotVolumeRatio)}x", TextPrimary)
                    }
                }
            }
        }

        // Section 2: جدول فاندینگ ریت صرافی‌ها
        if (derivatives != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TerminalCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "تطبیق نرخ فاندینگ میان صرافی‌های اصلی",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "رصد فرصت‌های آربیتراژ و انحراف فاندینگ میان پلتفرم‌ها:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        derivatives.crossExchangeFunding.forEach { venue ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TerminalSurface)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = venue.exchange,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${"%.4f".format(venue.rate * 100)}٪ / ۸ ساعت",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (venue.rate > 0.02) BearRed else BullGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // Section 3: کلاسترهای لیکوئیدیشن
        if (derivatives != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TerminalCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "کلاسترهای لیکوئیدیشن و جریان نقدینگی اجباری",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MetricRow("دسته‌بندی تخلیه لیکوئیدیشن", derivatives.liquidationFlushType.label, TerminalCyan)
                        MetricRow("حجم لیکوئیدیشن پوزیشن‌های لانگ", "$${"%.1f".format(derivatives.longLiquidationVol24h / 1e6)}M", BearRed)
                        MetricRow("حجم لیکوئیدیشن پوزیشن‌های شورت", "$${"%.1f".format(derivatives.shortLiquidationVol24h / 1e6)}M", BullGreen)
                        MetricRow("شاخص شدت آبشار لیکوئیدیشن", "${"%.1f".format(derivatives.liquidationIntensity)} از ۱۰۰", if (derivatives.liquidationIntensity > 70) WarningAmber else TextSecondary)
                    }
                }
            }
        }

        // Section 4: بستر آپشن‌ها
        if (options != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TerminalCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "بستر بازار آپشن‌ها (Deribit)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MetricRow("نوسان ضمنی (IV)", "${"%.1f".format(options.impliedVolatility)}٪", TextPrimary)
                        MetricRow("اسکیو دلتای ۲۵ (Risk Reversal)", "${"%.1f".format(options.riskReversal25d)}٪", if (options.riskReversal25d >= 0) BullGreen else BearRed, "تقاضای کال")
                        MetricRow("نسبت معاملات پوت به کال", "${"%.2f".format(options.putCallRatio)}", TextSecondary)
                        MetricRow("استرایک نقطه حداکثر درد (Max Pain)", "$${"%.0f".format(options.maxPainPrice)}", TerminalCyan)
                        MetricRow("تراکم سررسیدها", options.expiryConcentration, TextSecondary)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "بستر گامای بازارسازان: ${options.gammaContext}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
