package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun DataStatusBadge(
    status: DataStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (bg, textColor) = when (status) {
        DataStatus.LIVE_DATA -> Pair(LiveBadgeBg, LiveBadgeText)
        DataStatus.DEMO_DATA -> Pair(DemoBadgeBg, DemoBadgeText)
        DataStatus.STALE_DATA -> Pair(StaleBadgeBg, StaleBadgeText)
        DataStatus.PARTIAL_DATA -> Pair(WarningAmberBg, WarningAmber)
        DataStatus.DATA_UNAVAILABLE -> Pair(ErrorBadgeBg, ErrorBadgeText)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("status_badge_${status.name}")
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun RegimeBadge(regime: MarketRegime, modifier: Modifier = Modifier) {
    val (bg, color) = when (regime) {
        MarketRegime.STRONG_BULL, MarketRegime.BULL -> Pair(BullGreenBg, BullGreen)
        MarketRegime.BEAR, MarketRegime.STRONG_BEAR -> Pair(BearRedBg, BearRed)
        MarketRegime.RANGE -> Pair(Color(0x2E38BDF8), TerminalBlue)
        MarketRegime.SHOCK_EVENT -> Pair(WarningAmberBg, WarningAmber)
        MarketRegime.NEUTRAL -> Pair(Color(0x2E94A3B8), TextSecondary)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = regime.label,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun DirectionBadge(direction: TradeDirection, modifier: Modifier = Modifier) {
    val (bg, color) = when (direction) {
        TradeDirection.LONG -> Pair(BullGreenBg, BullGreen)
        TradeDirection.SHORT -> Pair(BearRedBg, BearRed)
        TradeDirection.WATCH -> Pair(Color(0x2E38BDF8), TerminalBlue)
        TradeDirection.NO_TRADE -> Pair(Color(0x2E64748B), TextMuted)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = direction.label,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun OppScoreChip(score: Int, grade: String, modifier: Modifier = Modifier) {
    val color = when {
        score >= 80 -> BullGreen
        score >= 70 -> TerminalBlue
        score >= 60 -> WarningAmber
        else -> TextMuted
    }
    val bg = color.copy(alpha = 0.12f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$score",
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "($grade)",
            color = color.copy(alpha = 0.8f),
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )
    }
}

@Composable
fun TerminalSectionHeader(
    title: String,
    subtitle: String? = null,
    trailingAction: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
        trailingAction?.invoke()
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    subValue: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = valueColor
            )
            if (subValue != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subValue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
