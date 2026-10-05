package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun DashboardScreen(
    state: TerminalUiState,
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Section 1: رژیم کلی بازار و متغیرهای کلان
        item {
            GlobalRegimeCard(state)
        }

        // Section 2: بیت‌کوین و اتریوم به عنوان ستون‌های بازار
        item {
            BtcEthAnchorSection(state, onSelectSymbol = { viewModel.selectSymbol(it) })
        }

        // Section 3: انتخاب سناریو در صورت فعال بودن حالت دمو
        if (!state.isLiveMode) {
            item {
                ScenarioSelectorCard(
                    activeScenario = state.activeScenario,
                    onSelectScenario = { viewModel.selectScenario(it) }
                )
            }
        }

        // Section 4: لیدربرد برترین فرصت‌های معاملاتی
        item {
            TerminalSectionHeader(
                title = "فرصت‌های برتر معاملاتی",
                subtitle = "رتبه‌بندی شده با مدل ارزیابی ۷ فاکتوره پرپچوال"
            )
        }

        items(state.assets.sortedByDescending { it.opportunityScore }.take(3)) { asset ->
            OpportunityLeaderboardCard(
                asset = asset,
                onClick = { viewModel.selectSymbol(asset.symbol) }
            )
        }

        // Section 5: پالس نقدینگی، سود باز و لیکوئیدیشن‌ها
        item {
            DerivativesPulseCard(state)
        }

        // Section 6: پیشتازان و بازماندگان قدرت نسبی
        item {
            LeadersAndLaggardsSection(state, onSelect = { viewModel.selectSymbol(it) })
        }

        // Section 7: رویدادهای کلان و کاتالیزورها
        item {
            UpcomingCatalystsCard(state)
        }

        // Section 8: خلاصه وضعیت نهادی هوش مصنوعی
        item {
            AiMarketBriefDashboardCard(
                state = state,
                onRequestAi = {
                    viewModel.selectTab(com.example.viewmodel.TerminalTab.AI_ANALYST)
                    viewModel.requestAiAnalysis()
                }
            )
        }
    }
}

@Composable
private fun GlobalRegimeCard(state: TerminalUiState) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "رژیم بازار:",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    RegimeBadge(regime = MarketRegime.BULL)
                }
                DataStatusBadge(status = state.currentStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Macro Ticker Grid: DXY, SPX, US10Y, VIX
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MacroTickerItem("شاخص دلار (DXY)", "102.45", "-0.18%", false)
                MacroTickerItem("اس اند پی (S&P 500)", "5,750", "+0.42%", true)
                MacroTickerItem("اوراق ۱۰ ساله", "4.08%", "-2 bps", false)
                MacroTickerItem("نوسان (VIX)", "15.80", "-3.2%", false)
            }
        }
    }
}

@Composable
private fun MacroTickerItem(name: String, value: String, change: String, isPositive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, fontFamily = FontFamily.SansSerif, fontSize = 9.sp, color = TextMuted)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(
            change,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = if (isPositive) BullGreen else BearRed
        )
    }
}

@Composable
private fun BtcEthAnchorSection(state: TerminalUiState, onSelectSymbol: (String) -> Unit) {
    val btc = state.assets.find { it.symbol == "BTCUSDT" }
    val eth = state.assets.find { it.symbol == "ETHUSDT" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (btc != null) {
            AnchorAssetCard(
                asset = btc,
                modifier = Modifier.weight(1f),
                onClick = { onSelectSymbol("BTCUSDT") }
            )
        }
        if (eth != null) {
            AnchorAssetCard(
                asset = eth,
                modifier = Modifier.weight(1f),
                onClick = { onSelectSymbol("ETHUSDT") }
            )
        }
    }
}

@Composable
private fun AnchorAssetCard(
    asset: AssetSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("anchor_card_${asset.symbol}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = asset.symbol.replace("USDT", ""),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                RegimeBadge(regime = asset.regime)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${"%.2f".format(asset.price)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (asset.change24h >= 0) BullGreen else BearRed
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("۱ساعته: ${asset.change1h}%", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                Text("۴ساعته: ${asset.change4h}%", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                Text("۲۴ساعته: ${asset.change24h}%", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = if (asset.change24h >= 0) BullGreen else BearRed)
            }
        }
    }
}

@Composable
private fun ScenarioSelectorCard(activeScenario: Int, onSelectScenario: (Int) -> Unit) {
    val scenarios = listOf(
        Pair(1, "۱. روند صعودی قدرتمند"),
        Pair(2, "۲. روند نزولی قدرتمند"),
        Pair(3, "۳. برگشت به میانگین در رنج"),
        Pair(4, "۴. سویپ نقدینگی و بازپس‌گیری"),
        Pair(5, "۵. شورت اسکوییز تهاجمی"),
        Pair(6, "۶. فیک بریک و حراج ناموفق"),
        Pair(7, "۷. پامپ با اهرم بالا (تراکم)"),
        Pair(8, "۸. بریک‌اوت با لیدری اسپات")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "انتخاب سناریوی شبیه‌سازی بازار",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerminalCyan
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "برای ارزیابی رفتار هوش مصنوعی و مدل‌های تحلیلی، سناریوی بازار را تغییر دهید:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                scenarios.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { (idx, name) ->
                            val isSelected = idx == activeScenario
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) TerminalCyan.copy(alpha = 0.2f) else TerminalSurface)
                                    .border(1.dp, if (isSelected) TerminalCyan else TerminalBorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { onSelectScenario(idx) }
                                    .padding(vertical = 5.dp, horizontal = 6.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TerminalCyan else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpportunityLeaderboardCard(
    asset: AssetSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("opp_card_${asset.symbol}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = asset.symbol,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    DirectionBadge(direction = asset.bias)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "دلتای اسپات: ${"%.1f".format(asset.spotCvd / 1e6)}M$ | تغییر سود باز: ${asset.oiDeltaPct}%",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                OppScoreChip(
                    score = asset.opportunityScore,
                    grade = if (asset.opportunityScore >= 80) "A+" else if (asset.opportunityScore >= 70) "A" else "B"
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$${"%.2f".format(asset.price)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun DerivativesPulseCard(state: TerminalUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "پالس مشتقات و وضعیت انباشت پوزیشن‌ها",
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow(
                label = "هشدار تراکم فاندینگ",
                value = "دوج‌کوین (Z: +2.45)",
                valueColor = BearRed,
                subValue = "تراکم سنگین لانگ"
            )
            MetricRow(
                label = "مجموع حجم لیکوئیدیشن ۲۴ ساعت",
                value = "۱۴۸.۵ میلیون دلار",
                valueColor = TextPrimary,
                subValue = "غلبه شورت اسکوییز (۸۸M$)"
            )
            MetricRow(
                label = "انحراف فاندینگ میان صرافی‌ها",
                value = "OKX در برابر Bybit (0.038%)",
                valueColor = WarningAmber,
                subValue = "واگرایی رصد شد"
            )
            MetricRow(
                label = "نسبت حجم فیوچرز به اسپات",
                value = "1.60x",
                valueColor = TextPrimary,
                subValue = "سطح نوسان طبیعی"
            )
        }
    }
}

@Composable
private fun LeadersAndLaggardsSection(state: TerminalUiState, onSelect: (String) -> Unit) {
    val sorted = state.assets.sortedByDescending { it.change24h }
    val leaders = sorted.take(2)
    val laggards = sorted.takeLast(2)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("پیشتازان بازار (RS)", fontFamily = FontFamily.SansSerif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BullGreen)
                Spacer(modifier = Modifier.height(4.dp))
                leaders.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item.symbol) }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.symbol.replace("USDT", ""), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary)
                        Text("+${item.change24h}%", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BullGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("بازماندگان بازار", fontFamily = FontFamily.SansSerif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BearRed)
                Spacer(modifier = Modifier.height(4.dp))
                laggards.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item.symbol) }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.symbol.replace("USDT", ""), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary)
                        Text("${item.change24h}%", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BearRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingCatalystsCard(state: TerminalUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "کاتالیزورهای مهم و تقویم رویدادهای کلان",
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = WarningAmber
            )
            Spacer(modifier = Modifier.height(6.dp))

            state.macroEvents.take(2).forEach { event ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• ${event.eventName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (event.isPreEventRisk) "ریسک رویداد: ${event.timeRemaining}" else event.timeRemaining,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (event.isPreEventRisk) BearRed else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AiMarketBriefDashboardCard(
    state: TerminalUiState,
    onRequestAi: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRequestAi() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "خلاصه تحلیلی هوش مصنوعی ترمینال",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalCyan
                    )
                }
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "رژیم کلی: تداوم ساختار گاوی. بیت‌کوین بالاتر از میانگین وزنی هفتگی تثبیت شده است. سولانا ستاپ الف (سویپ نقدینگی و بازپس‌گیری کف ارزش) را با جهش دلتای اسپات فعال کرده است. دوج‌کوین در تراکم اهرمی و فاز عدم معامله است.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
