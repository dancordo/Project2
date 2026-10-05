package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.TradeDirection
import com.example.model.TradeVerdict
import com.example.ui.theme.*

@Composable
fun TradePlanCard(
    verdict: TradeVerdict,
    onSaveSetup: () -> Unit = {},
    onAddToWatchlist: () -> Unit = {},
    onCreateAlert: () -> Unit = {},
    onSendToJournal: () -> Unit = {},
    onCopyAnalysis: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isAuditExpanded by remember { mutableStateOf(false) }

    val borderColor = when (verdict.direction) {
        TradeDirection.LONG -> BullGreen.copy(alpha = 0.5f)
        TradeDirection.SHORT -> BearRed.copy(alpha = 0.5f)
        TradeDirection.WATCH -> TerminalBlue.copy(alpha = 0.4f)
        TradeDirection.NO_TRADE -> TerminalBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .testTag("trade_plan_card"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Verdict, Direction, Confidence, Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DirectionBadge(direction = verdict.direction)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "درجه اطمینان: ${verdict.confidence}٪",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isAuditExpanded = !isAuditExpanded }
                ) {
                    OppScoreChip(
                        score = verdict.opportunityScore.totalScore,
                        grade = verdict.opportunityScore.grade
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isAuditExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "جزییات امتیاز",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Setup Title
            Text(
                text = verdict.setupName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Audit Trail Accordion
            AnimatedVisibility(visible = isAuditExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "تفکیک ۷ فاکتور امتیاز فرصت معامله (۰ تا ۱۰۰)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    verdict.opportunityScore.auditTrail.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.category} (${item.pointsAwarded}/${item.maxPoints})",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.pointsAwarded < 0) BearRed else TextPrimary
                            )
                        }
                        Text(
                            text = item.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Parameter Grid: Entry, Stop, Targets, R:R
            if (verdict.direction != TradeDirection.NO_TRADE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurface)
                        .padding(10.dp)
                ) {
                    MetricRow(
                        label = "محدوده بهینه ورود",
                        value = verdict.entryZone,
                        valueColor = TerminalCyan
                    )
                    MetricRow(
                        label = "حد ضرر و سطح ابطال",
                        value = "${"%.2f".format(verdict.stopLoss)} دلار",
                        valueColor = BearRed
                    )
                    MetricRow(
                        label = "تارگت اول (کاهش ریسک)",
                        value = "${"%.2f".format(verdict.tp1)} دلار",
                        valueColor = BullGreen
                    )
                    MetricRow(
                        label = "تارگت دوم (هدف ارزش اصلی)",
                        value = "${"%.2f".format(verdict.tp2)} دلار",
                        valueColor = BullGreen
                    )
                    MetricRow(
                        label = "تارگت سوم (نقدینگی متضاد)",
                        value = "${"%.2f".format(verdict.tp3)} دلار",
                        valueColor = BullGreen
                    )
                    MetricRow(
                        label = "نسبت ریوارد به ریسک (R:R)",
                        value = "${"%.2f".format(verdict.rrRatio)} به ۱",
                        valueColor = if (verdict.rrRatio >= 1.8) BullGreen else WarningAmber
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Primary Reasons & Invalidation
            Text(
                text = "دلایل ورود بر اساس جریان سفارشات:",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            verdict.primaryReasons.forEach { reason ->
                Text(
                    text = "• $reason",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ریسک‌های اصلی و سطح ابطال:",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BearRed
            )
            Text(
                text = verdict.invalidation,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToWatchlist,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("action_add_watchlist"),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واچ‌لیست", fontSize = 11.sp, color = TextPrimary)
                }

                OutlinedButton(
                    onClick = onCreateAlert,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("action_create_alert"),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp), tint = TerminalCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("هشدار", fontSize = 11.sp, color = TextPrimary)
                }

                OutlinedButton(
                    onClick = onSendToJournal,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("action_journal"),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = WarningAmber)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ژورنال", fontSize = 11.sp, color = TextPrimary)
                }

                OutlinedButton(
                    onClick = onCopyAnalysis,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("action_copy_analysis"),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("کپی", fontSize = 11.sp, color = TextPrimary)
                }
            }
        }
    }
}
