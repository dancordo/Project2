package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssetSummary
import com.example.model.TradeDirection
import com.example.ui.components.DataStatusBadge
import com.example.ui.components.DirectionBadge
import com.example.ui.components.OppScoreChip
import com.example.ui.components.RegimeBadge
import com.example.ui.theme.*
import com.example.viewmodel.TerminalUiState
import com.example.viewmodel.TerminalViewModel

enum class ScannerSort(val label: String) {
    OPP_SCORE("امتیاز فرصت"),
    VOLUME("حجم ۲۴ساعته"),
    REL_VOL("حجم نسبی"),
    OI_CHANGE("تغییر سود باز"),
    FUNDING_Z("ضریب فاندینگ"),
    LIQUIDATIONS("لیکوئیدیشن‌ها"),
    RELATIVE_STRENGTH("قدرت نسبی")
}

@Composable
fun MarketsScreen(
    state: TerminalUiState,
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    var activeSort by remember { mutableStateOf(ScannerSort.OPP_SCORE) }
    var selectedFilterDirection by remember { mutableStateOf<TradeDirection?>(null) }
    val hScrollState = rememberScrollState()

    val filteredAssets = state.assets
        .filter { asset ->
            (state.searchQuery.isBlank() || asset.symbol.contains(state.searchQuery, ignoreCase = true) || asset.baseName.contains(state.searchQuery, ignoreCase = true)) &&
            (selectedFilterDirection == null || asset.bias == selectedFilterDirection)
        }
        .sortedWith(
            Comparator { a, b ->
                when (activeSort) {
                    ScannerSort.OPP_SCORE -> b.opportunityScore.compareTo(a.opportunityScore)
                    ScannerSort.VOLUME -> b.volume24h.compareTo(a.volume24h)
                    ScannerSort.REL_VOL -> b.relativeVolume.compareTo(a.relativeVolume)
                    ScannerSort.OI_CHANGE -> b.oiDeltaPct.compareTo(a.oiDeltaPct)
                    ScannerSort.FUNDING_Z -> b.fundingZScore.compareTo(a.fundingZScore)
                    ScannerSort.LIQUIDATIONS -> b.liquidations24h.compareTo(a.liquidations24h)
                    ScannerSort.RELATIVE_STRENGTH -> b.rsScore.compareTo(a.rsScore)
                }
            }
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("markets_screen")
    ) {
        // Controls Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اسکنر فیوچرز پرپچوال (${filteredAssets.size} نماد)",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerminalCyan
                )
                DataStatusBadge(status = state.currentStatus)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sort Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScannerSort.values().forEach { sort ->
                    val isSelected = sort == activeSort
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) TerminalCyan.copy(alpha = 0.2f) else TerminalCard)
                            .border(1.dp, if (isSelected) TerminalCyan else TerminalBorder, RoundedCornerShape(4.dp))
                            .clickable { activeSort = sort }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = sort.label,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TerminalCyan else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChipItem("همه", selectedFilterDirection == null) { selectedFilterDirection = null }
                FilterChipItem("فرصت خرید", selectedFilterDirection == TradeDirection.LONG) { selectedFilterDirection = TradeDirection.LONG }
                FilterChipItem("فرصت فروش", selectedFilterDirection == TradeDirection.SHORT) { selectedFilterDirection = TradeDirection.SHORT }
                FilterChipItem("تحت نظر", selectedFilterDirection == TradeDirection.WATCH) { selectedFilterDirection = TradeDirection.WATCH }
                FilterChipItem("عدم معامله", selectedFilterDirection == TradeDirection.NO_TRADE) { selectedFilterDirection = TradeDirection.NO_TRADE }
            }
        }

        // Table
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(hScrollState)
            ) {
                item {
                    ScannerTableHeader()
                }

                items(filteredAssets) { asset ->
                    ScannerTableRow(
                        asset = asset,
                        onClick = { viewModel.selectSymbol(asset.symbol) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) TerminalBlue.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, if (isSelected) TerminalBlue else TerminalBorderSubtle, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.SansSerif,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) TerminalBlue else TextMuted
        )
    }
}

@Composable
private fun ScannerTableHeader() {
    Row(
        modifier = Modifier
            .background(TerminalCardElevated)
            .border(1.dp, TerminalBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeadCell("نماد", 100.dp)
        TableHeadCell("قیمت زنده", 90.dp)
        TableHeadCell("۲۴ساعته", 65.dp)
        TableHeadCell("۱ساعته", 60.dp)
        TableHeadCell("۴ساعته", 60.dp)
        TableHeadCell("امتیاز فرصت", 100.dp)
        TableHeadCell("سیگنال", 85.dp)
        TableHeadCell("حجم نسبی", 70.dp)
        TableHeadCell("تغییر سود باز", 85.dp)
        TableHeadCell("فاندینگ", 75.dp)
        TableHeadCell("ضریب Z", 70.dp)
        TableHeadCell("دلتای اسپات", 85.dp)
        TableHeadCell("دلتای پرپچوال", 85.dp)
        TableHeadCell("رژیم", 100.dp)
    }
}

@Composable
private fun ScannerTableRow(asset: AssetSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .border(1.dp, TerminalBorderSubtle)
            .background(TerminalCard)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("scanner_row_${asset.symbol}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Symbol
        Column(modifier = Modifier.width(100.dp)) {
            Text(
                text = asset.symbol,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextPrimary
            )
            Text(
                text = asset.baseName,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 9.sp
            )
        }

        // Price
        Text(
            text = "$${"%.2f".format(asset.price)}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = TextPrimary,
            modifier = Modifier.width(90.dp)
        )

        // 24H%
        Text(
            text = "${if (asset.change24h >= 0) "+" else ""}${"%.2f".format(asset.change24h)}%",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (asset.change24h >= 0) BullGreen else BearRed,
            modifier = Modifier.width(65.dp)
        )

        // 1H%
        Text(
            text = "${if (asset.change1h >= 0) "+" else ""}${"%.2f".format(asset.change1h)}%",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = if (asset.change1h >= 0) BullGreen else BearRed,
            modifier = Modifier.width(60.dp)
        )

        // 4H%
        Text(
            text = "${if (asset.change4h >= 0) "+" else ""}${"%.2f".format(asset.change4h)}%",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = if (asset.change4h >= 0) BullGreen else BearRed,
            modifier = Modifier.width(60.dp)
        )

        // Opp Score
        Box(modifier = Modifier.width(100.dp)) {
            OppScoreChip(
                score = asset.opportunityScore,
                grade = if (asset.opportunityScore >= 80) "A+" else if (asset.opportunityScore >= 70) "A" else "B"
            )
        }

        // Bias
        Box(modifier = Modifier.width(85.dp)) {
            DirectionBadge(direction = asset.bias)
        }

        // Relative Vol
        Text(
            text = "${"%.2f".format(asset.relativeVolume)}x",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = if (asset.relativeVolume >= 2.0) TerminalCyan else if (asset.relativeVolume >= 1.5) BullGreen else TextSecondary,
            modifier = Modifier.width(70.dp)
        )

        // ΔOI%
        Text(
            text = "${if (asset.oiDeltaPct >= 0) "+" else ""}${"%.1f".format(asset.oiDeltaPct)}%",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = if (asset.oiDeltaPct >= 5.0) BullGreen else if (asset.oiDeltaPct <= -5.0) BearRed else TextSecondary,
            modifier = Modifier.width(85.dp)
        )

        // Funding
        Text(
            text = "${"%.3f".format(asset.fundingRate8h * 100)}%",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = if (asset.fundingRate8h > 0.02) BearRed else TextPrimary,
            modifier = Modifier.width(75.dp)
        )

        // Funding Z
        Text(
            text = "${if (asset.fundingZScore >= 0) "+" else ""}${"%.2f".format(asset.fundingZScore)}",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (asset.fundingZScore > 2.0) BearRed else if (asset.fundingZScore < -2.0) BullGreen else TextSecondary,
            modifier = Modifier.width(70.dp)
        )

        // Spot CVD
        Text(
            text = "$${"%.1f".format(asset.spotCvd / 1e6)}M",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = if (asset.spotCvd >= 0) BullGreen else BearRed,
            modifier = Modifier.width(85.dp)
        )

        // Perp CVD
        Text(
            text = "$${"%.1f".format(asset.perpCvd / 1e6)}M",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = if (asset.perpCvd >= 0) BullGreen else BearRed,
            modifier = Modifier.width(85.dp)
        )

        // Regime
        Box(modifier = Modifier.width(100.dp)) {
            RegimeBadge(regime = asset.regime)
        }
    }
}

@Composable
private fun TableHeadCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = TextMuted,
        modifier = Modifier.width(width)
    )
}
