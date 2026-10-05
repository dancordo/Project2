package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RiskCalculationInputs
import com.example.model.JournalEntry
import com.example.model.TradeDirection
import com.example.ui.components.DirectionBadge
import com.example.ui.components.MetricRow
import com.example.ui.components.TerminalSectionHeader
import com.example.ui.theme.*
import com.example.viewmodel.TerminalUiState
import com.example.viewmodel.TerminalViewModel

@Composable
fun RiskAndJournalScreen(
    state: TerminalUiState,
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("risk_journal_screen")
    ) {
        // Sub-Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubTabButton("ماشین‌حساب ریسک و هیت‌پورتفو", selectedSubTab == 0, Modifier.weight(1f)) { selectedSubTab = 0 }
            SubTabButton("ژورنال ترید و امید ریاضی", selectedSubTab == 1, Modifier.weight(1f)) { selectedSubTab = 1 }
        }

        if (selectedSubTab == 0) {
            RiskCalculatorSection(state, viewModel)
        } else {
            TradeJournalSection(state, viewModel)
        }
    }
}

@Composable
private fun SubTabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) TerminalCyan.copy(alpha = 0.2f) else TerminalCard)
            .border(1.dp, if (isSelected) TerminalCyan else TerminalBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.SansSerif,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) TerminalCyan else TextSecondary
        )
    }
}

@Composable
private fun RiskCalculatorSection(state: TerminalUiState, viewModel: TerminalViewModel) {
    val inputs = state.riskInputs
    val output = state.riskOutput
    val heat = state.portfolioHeat

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // Daily Kill Switch
        if (heat?.warningMessage != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (heat.isKillSwitchTriggered) BearRedBg else WarningAmberBg)
                        .border(1.dp, if (heat.isKillSwitchTriggered) BearRed else WarningAmber, RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = if (heat.isKillSwitchTriggered) BearRed else WarningAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = heat.warningMessage,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (heat.isKillSwitchTriggered) BearRed else WarningAmber
                    )
                }
            }
        }

        // Portfolio Heat Summary
        if (heat != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TerminalCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "هیت پورتفو و ریسک همبستگی پوزیشن‌ها",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MetricRow("مجموع حجم اسمی باز", "$${"%.2f".format(heat.grossExposure)}", TextPrimary)
                        MetricRow("خالص مواجهه جهتی", "$${"%.2f".format(heat.netExposure)}", if (heat.netExposure >= 0) BullGreen else BearRed)
                        MetricRow("ریسک همبستگی کلاستر کریپتو", "${"%.1f".format(heat.correlatedRiskPercent)}٪", if (heat.isCorrelatedRiskElevated) WarningAmber else TextSecondary, "لانگ‌های BTC/ETH/SOL")
                        MetricRow("سود/زیان محقق‌شده امروز", "${"%.2f".format(heat.dailyPnlR)}R", if (heat.dailyPnlR >= 0) BullGreen else BearRed)
                    }
                }
            }
        }

        // Calculator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "محاسبه‌گر حرفه‌ای حجم پوزیشن و مدیریت سرمایه",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RiskPresetChip("ستاپ عالی (۰.۵۰٪)", inputs.riskPercent == 0.50) { viewModel.updateRiskInputs(inputs.copy(riskPercent = 0.50)) }
                        RiskPresetChip("ستاپ خوب (۰.۳۵٪)", inputs.riskPercent == 0.35) { viewModel.updateRiskInputs(inputs.copy(riskPercent = 0.35)) }
                        RiskPresetChip("ستاپ معمولی (۰.۲۰٪)", inputs.riskPercent == 0.20) { viewModel.updateRiskInputs(inputs.copy(riskPercent = 0.20)) }
                        RiskPresetChip("رویداد (۰.۱۵٪)", inputs.riskPercent == 0.15) { viewModel.updateRiskInputs(inputs.copy(riskPercent = 0.15)) }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = "${inputs.accountEquity}",
                        onValueChange = { str ->
                            str.toDoubleOrNull()?.let { viewModel.updateRiskInputs(inputs.copy(accountEquity = it)) }
                        },
                        label = { Text("موجودی حساب (دلار)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TerminalCyan, unfocusedBorderColor = TerminalBorder)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = "${inputs.entryPrice}",
                            onValueChange = { str ->
                                str.toDoubleOrNull()?.let { viewModel.updateRiskInputs(inputs.copy(entryPrice = it)) }
                            },
                            label = { Text("قیمت ورود ($)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TerminalCyan, unfocusedBorderColor = TerminalBorder)
                        )

                        OutlinedTextField(
                            value = "${inputs.stopPrice}",
                            onValueChange = { str ->
                                str.toDoubleOrNull()?.let { viewModel.updateRiskInputs(inputs.copy(stopPrice = it)) }
                            },
                            label = { Text("حد ضرر ($)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TerminalCyan, unfocusedBorderColor = TerminalBorder)
                        )
                    }

                    if (output != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerminalSurface)
                                .border(1.dp, TerminalBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = output.formulaExplanation,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                lineHeight = 18.sp,
                                color = TerminalCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        MetricRow("مبلغ ریسک به دلار", "$${"%.2f".format(output.riskAmountDollars)}", BearRed)
                        MetricRow("فاصله تا حد ضرر", "${"%.2f".format(output.stopDistancePct)}٪", TextSecondary)
                        MetricRow("ارزش اسمی پوزیشن", "$${"%.2f".format(output.positionNotional)}", TextPrimary)
                        MetricRow("اهرم موثر مورد نیاز", "${"%.2f".format(output.effectiveLeverage)}x", if (output.effectiveLeverage > 5.0) WarningAmber else BullGreen)
                        MetricRow("سود خالص در تارگت ۲", "+$${"%.2f".format(output.netEstimatedPnlAtTp2)}", BullGreen)
                        MetricRow("کارمزد تیکر + درگ فاندینگ", "-$${"%.2f".format(output.estimatedTakerFee + output.estimatedFundingDrag)}", BearRed)
                        MetricRow("نسبت ریوارد به ریسک", "${"%.2f".format(output.rrRatio)} به ۱", if (output.rrRatio >= 1.8) BullGreen else WarningAmber)
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskPresetChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) TerminalCyan.copy(alpha = 0.2f) else TerminalSurface)
            .border(1.dp, if (isSelected) TerminalCyan else TerminalBorderSubtle, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.SansSerif,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) TerminalCyan else TextSecondary
        )
    }
}

@Composable
private fun TradeJournalSection(state: TerminalUiState, viewModel: TerminalViewModel) {
    val entries = state.journalEntries
    val wins = entries.filter { it.isWin }
    val losses = entries.filter { !it.isWin }
    val total = entries.size
    val winRate = if (total > 0) (wins.size.toDouble() / total) * 100.0 else 0.0
    val avgWin = if (wins.isNotEmpty()) wins.map { it.rMultiple }.average() else 0.0
    val avgLoss = if (losses.isNotEmpty()) losses.map { it.rMultiple }.average() else 0.0
    val expectancy = if (total > 0) ((wins.size.toDouble() / total) * avgWin) + ((losses.size.toDouble() / total) * avgLoss) else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // Statistics Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "امید ریاضی و آمار عملکرد تریدها",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalSurface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("درصد برد (Win Rate)", fontFamily = FontFamily.SansSerif, fontSize = 9.sp, color = TextMuted)
                            Text("${"%.1f".format(winRate)}٪", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BullGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("امید ریاضی (Expectancy)", fontFamily = FontFamily.SansSerif, fontSize = 9.sp, color = TextMuted)
                            Text("${"%.2f".format(expectancy)}R", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (expectancy >= 0) BullGreen else BearRed)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("میانگین برد", fontFamily = FontFamily.SansSerif, fontSize = 9.sp, color = TextMuted)
                            Text("+${"%.2f".format(avgWin)}R", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BullGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("میانگین باخت", fontFamily = FontFamily.SansSerif, fontSize = 9.sp, color = TextMuted)
                            Text("${"%.2f".format(avgLoss)}R", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BearRed)
                        }
                    }
                }
            }
        }

        // Journal Entries List
        item {
            TerminalSectionHeader(title = "تاریخچه ژورنال معاملات (${entries.size})")
        }

        items(entries) { entry ->
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
                                text = entry.asset,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            DirectionBadge(direction = entry.direction)
                        }
                        Text(
                            text = "${if (entry.rMultiple >= 0) "+" else ""}${"%.2f".format(entry.rMultiple)}R",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (entry.isWin) BullGreen else BearRed
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${entry.date} • ${entry.setup} • ${entry.session}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "دلیل ورود: ${entry.reasonEntry}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "دلیل خروج: ${entry.reasonExit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    if (entry.mistake.isNotBlank() && entry.mistake != "None" && entry.mistake != "ندارد") {
                        Text(
                            text = "اشتباه معاملاتی: ${entry.mistake}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarningAmber,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
