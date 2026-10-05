package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import com.example.engine.DerivativesEngine
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TerminalTab
import com.example.viewmodel.TerminalUiState
import com.example.viewmodel.TerminalViewModel

@Composable
fun AssetDetailScreen(
    state: TerminalUiState,
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    val currentCandle = state.candles.lastOrNull()
    val price = currentCandle?.close ?: 100.0
    val asset = state.assets.find { it.symbol == state.selectedSymbol }
    val indicators = state.indicators
    val derivatives = state.derivatives
    val verdict = state.tradeVerdict

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("asset_detail_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        // Asset Header
        item {
            AssetDetailHeader(
                symbol = state.selectedSymbol,
                persianName = asset?.baseName ?: "ارز دیجیتال",
                price = price,
                change24h = asset?.change24h ?: 0.0,
                status = state.currentStatus
            )
        }

        // Failed Auction Alert Banner if detected
        if (verdict?.failedAuctionDetected == true) {
            item {
                FailedAuctionBanner()
            }
        }

        // Primary Trade Verdict Card
        if (verdict != null) {
            item {
                TradePlanCard(
                    verdict = verdict,
                    onAddToWatchlist = { viewModel.addToWatchlist(state.selectedSymbol) },
                    onSendToJournal = {
                        viewModel.addCurrentTradeToJournal()
                        viewModel.selectTab(TerminalTab.RISK_JOURNAL)
                    },
                    onCreateAlert = {},
                    onCopyAnalysis = {}
                )
            }
        }

        // Multi-Timeframe Interactive Candle Chart
        if (indicators != null) {
            item {
                InteractiveCandleChart(
                    candles = state.candles,
                    indicators = indicators,
                    selectedTimeframe = state.selectedTimeframe,
                    onTimeframeSelected = { viewModel.selectTimeframe(it) }
                )
            }
        }

        // Technical Indicators Panel
        if (indicators != null) {
            item {
                TechnicalEngineTelemetryCard(indicators, price)
            }
        }

        // Derivatives Engine Panel
        if (derivatives != null && asset != null) {
            item {
                DerivativesTelemetryCard(derivatives, asset)
            }
        }

        // Spot vs Perp CVD Flow Divergence Panel
        if (derivatives != null) {
            item {
                SpotPerpCvdCard(derivatives)
            }
        }

        // Order Book Depth & Imbalance Panel
        if (state.orderBook != null) {
            item {
                OrderBookDepthCard(state.orderBook)
            }
        }
    }
}

@Composable
private fun AssetDetailHeader(
    symbol: String,
    persianName: String,
    price: Double,
    change24h: Double,
    status: DataStatus
) {
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = symbol,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "قرارداد پرپچوال USDT-M",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            color = TerminalCyan
                        )
                    }
                    Text(
                        text = "$persianName • دفتر سفارشات تجمیعی زنده",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                DataStatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$${"%.2f".format(price)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = if (change24h >= 0) BullGreen else BearRed
                )
                Text(
                    text = "${if (change24h >= 0) "+" else ""}${"%.2f".format(change24h)}٪ (۲۴ ساعت)",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (change24h >= 0) BullGreen else BearRed
                )
            }
        }
    }
}

@Composable
private fun FailedAuctionBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(BearRedBg)
            .border(1.dp, BearRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = BearRed, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "هشدار حراج ناموفق و فیک بریک (Failed Auction)",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BearRed
            )
            Text(
                text = "قیمت سقف ناحیه ارزش را سویپ کرد اما با هجوم سفارشات فروش مجدداً به داخل رنج بازگردانده شد.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TechnicalEngineTelemetryCard(indicators: TechnicalIndicators, currentPrice: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "تله‌متری موتور تکنیکال و کانتینر ارزش",
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow("میانگین نمایی ۲۰ (EMA 20)", "$${"%.2f".format(indicators.ema20)}", if (currentPrice >= indicators.ema20) BullGreen else BearRed)
            MetricRow("میانگین نمایی ۵۰ (EMA 50)", "$${"%.2f".format(indicators.ema50)}", if (currentPrice >= indicators.ema50) BullGreen else BearRed)
            MetricRow("میانگین نمایی ۲۰۰ (EMA 200)", "$${"%.2f".format(indicators.ema200)}", if (currentPrice >= indicators.ema200) BullGreen else BearRed)
            MetricRow("میانگین وزنی سشن (Session VWAP)", "$${"%.2f".format(indicators.sessionVwap)}", WarningAmber)
            MetricRow("میانگین وزنی هفتگی (Weekly AVWAP)", "$${"%.2f".format(indicators.weeklyAvwap)}", TerminalPurple)
            MetricRow("دامنه نوسان واقعی (ATR 14)", "$${"%.2f".format(indicators.atr14)}", TextPrimary)
            MetricRow("مومنتوم روند (ADX 14)", "${"%.1f".format(indicators.adx14)}", if (indicators.adx14 >= 25.0) TerminalCyan else TextSecondary, if (indicators.adx14 < 18.0) "رنج نوسانی" else "روند قوی")
            MetricRow("شاخص قدرت نسبی (RSI 14)", "${"%.1f".format(indicators.rsi14)}", if (indicators.rsi14 >= 70.0) BearRed else if (indicators.rsi14 <= 30.0) BullGreen else TextPrimary)
            MetricRow("نقطه اوج حجم پروفایل (POC)", "$${"%.2f".format(indicators.volumeProfile.poc)}", TerminalCyan, "سطح تجمع حجم")
            MetricRow("محدوده ارزش (VAL تا VAH)", "$${"%.2f".format(indicators.volumeProfile.valPrice)} - $${"%.2f".format(indicators.volumeProfile.vah)}", TextSecondary, "کانتینر ۷۰٪ حجم")
        }
    }
}

@Composable
private fun DerivativesTelemetryCard(derivatives: DerivativesMetrics, asset: AssetSummary) {
    val oiInterp = DerivativesEngine.interpretOiPrice(asset.change24h, derivatives.oiChange24hPct)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "موتور بازار مشتقات و سود باز (Open Interest)",
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow("سود باز (Open Interest)", "$${"%.2f".format(derivatives.openInterest / 1e9)} میلیارد دلار", TextPrimary, "تغییر: ${if (derivatives.oiChange24hPct >= 0) "+" else ""}${derivatives.oiChange24hPct}٪")
            MetricRow("تحلیل ۴گانه قیمت و OI", oiInterp.title, TerminalCyan, oiInterp.interpretation)
            MetricRow("نرخ فاندینگ ریت ۸ ساعته", "${"%.4f".format(derivatives.fundingRate8h * 100)}٪", if (derivatives.fundingRate8h > 0.02) BearRed else BullGreen)
            MetricRow("ضریب Z فاندینگ ریت", "${"%.2f".format(derivatives.fundingZScore)}", if (derivatives.fundingZScore > 2.0) BearRed else if (derivatives.fundingZScore < -2.0) BullGreen else TextPrimary, derivatives.fundingCrowdingStatus)
            MetricRow("صدک ۳۰ روزه فاندینگ", "${"%.1f".format(derivatives.fundingPercentile)}٪", if (derivatives.fundingPercentile > 90.0) WarningAmber else TextSecondary)
            MetricRow("نرخ مبنای سالانه ۳ ماهه", "${"%.2f".format(derivatives.basisAnnualized3M)}٪", TextPrimary, derivatives.termStructure.label)
            MetricRow("مجموع لیکوئیدیشن‌ها", "$${"%.1f".format((derivatives.longLiquidationVol24h + derivatives.shortLiquidationVol24h) / 1e6)}M", TextPrimary, derivatives.liquidationFlushType.label)
            MetricRow("نسبت پوزیشن‌های لانگ به شورت", "${"%.2f".format(derivatives.longShortRatio)}", TextSecondary)
        }
    }
}

@Composable
private fun SpotPerpCvdCard(derivatives: DerivativesMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "موتور واگرایی دلتای حجم اسپات در برابر پرپچوال (CVD)",
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalCyan
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = derivatives.cvdDivergenceNote,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow("دلتای تجمعی حجم اسپات", "$${"%.1f".format(derivatives.spotCvd / 1e6)}M", if (derivatives.spotCvd >= 0) BullGreen else BearRed)
            MetricRow("دلتای تجمعی حجم پرپچوال", "$${"%.1f".format(derivatives.perpCvd / 1e6)}M", if (derivatives.perpCvd >= 0) BullGreen else BearRed)
            MetricRow("ضریب حجم فیوچرز به اسپات", "${"%.2f".format(derivatives.perpSpotVolumeRatio)}x", if (derivatives.perpSpotVolumeRatio > 2.5) WarningAmber else TextSecondary)
        }
    }
}

@Composable
private fun OrderBookDepthCard(orderBook: OrderBookDepth) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "عدم تعادل در دفتر سفارشات (OBI)",
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalCyan
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = orderBook.interpretation,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow("شاخص عدم تعادل (OBI)", "${"%.2f".format(orderBook.obi)}", if (orderBook.obi >= 1.50) BullGreen else if (orderBook.obi <= 0.67) BearRed else TextPrimary)
            MetricRow("عمق خریداران در ±۵ صدم درصد", "$${"%.1f".format(orderBook.bidDepth5bps / 1000)}k", BullGreen)
            MetricRow("عمق فروشندگان در ±۵ صدم درصد", "$${"%.1f".format(orderBook.askDepth5bps / 1000)}k", BearRed)
            MetricRow("عمق خریداران در ±۱۰ صدم درصد", "$${"%.1f".format(orderBook.bidDepth10bps / 1000)}k", BullGreen)
            MetricRow("عمق فروشندگان در ±۱۰ صدم درصد", "$${"%.1f".format(orderBook.askDepth10bps / 1000)}k", BearRed)
        }
    }
}
