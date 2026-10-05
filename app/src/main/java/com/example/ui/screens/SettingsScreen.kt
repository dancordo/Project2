package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserTerminalSettings
import com.example.model.DataStatus
import com.example.ui.components.DataStatusBadge
import com.example.ui.components.MetricRow
import com.example.ui.theme.*
import com.example.viewmodel.TerminalUiState
import com.example.viewmodel.TerminalViewModel

@Composable
fun SettingsScreen(
    state: TerminalUiState,
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember { mutableStateOf(state.settings.geminiApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // Operational Mode Switch
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حالت کاری ترمینال (داده واقعی / آزمایشی)",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TerminalCyan
                        )
                        DataStatusBadge(status = state.currentStatus)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.isLiveMode) "دریافت داده‌های زنده و واقعی بازار" else "حالت شبیه‌سازی و سناریوهای تستی",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (state.isLiveMode) "اتصال مستقیم به سرورهای بایننس و بازار پرپچوال جهت نمایش قیمت‌های واقعی." else "اجرای سناریوهای سنتتیک بدون نیاز به اینترنت برای بررسی الگوریتم‌ها.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = state.isLiveMode,
                            onCheckedChange = { viewModel.toggleMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TerminalCyan,
                                checkedTrackColor = TerminalCardElevated
                            )
                        )
                    }
                }
            }
        }

        // AI Provider Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پیکربندی هوش مصنوعی و کلید اختصاصی",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "کلید API در حافظه محلی ذخیره می‌شود. در صورت خالی گذاشتن، موتور تحلیل نهادی داخلی ترمینال به صورت سیستماتیک و بدون اینترنت پاسخ می‌دهد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            viewModel.updateSettings(state.settings.copy(geminiApiKey = it))
                        },
                        label = { Text("کلید جمینای (Gemini API Key)", fontSize = 11.sp) },
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TerminalCyan, unfocusedBorderColor = TerminalBorder)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    MetricRow("مدل فعال", state.settings.geminiModel, TextPrimary)
                    MetricRow("دمای تحلیلی (Temperature)", "${state.settings.geminiTemperature}", TextSecondary, "تمرکز حداکثری بر شواهد عینی")
                }
            }
        }

        // Technical Defaults
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "تنظیمات پیش‌فرض اندیکاتورها و حساسیت چرخش ساختار",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    MetricRow("دوره‌های میانگین متحرک (EMA)", "20 / 50 / 200", TextPrimary)
                    MetricRow("دوره نوسان واقعی (ATR)", "۱۴ کندل", TextPrimary)
                    MetricRow("دوره شاخص روند (ADX)", "۱۴ کندل", TextPrimary)
                    MetricRow("شاخص قدرت نسبی (RSI)", "۱۴ کندل", TextPrimary)
                    MetricRow("حساسیت سویینگ ۵ دقیقه", "۳ چپ / ۳ راست", TextSecondary)
                    MetricRow("حساسیت سویینگ ۱۵ دقیقه", "۵ چپ / ۵ راست", TextSecondary)
                    MetricRow("حساسیت سویینگ ۱ ساعته", "۸ چپ / ۸ راست", TextSecondary)
                    MetricRow("حساسیت سویینگ ۴ ساعته", "۱۰ چپ / ۱۰ راست", TextSecondary)
                }
            }
        }

        // Token Unlocks
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "تقویم آنلاک و آزادسازی توکن‌ها",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    state.tokenUnlocks.forEach { unlock ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${unlock.token} (${unlock.date})", fontFamily = FontFamily.SansSerif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("$${"%.1f".format(unlock.usdValue / 1e6)}M (${unlock.pct7dVolume}٪ حجم ۷روزه)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = if (unlock.unlockPressure.contains("Extreme") || unlock.unlockPressure.contains("بحرانی")) BearRed else WarningAmber)
                        }
                    }
                }
            }
        }

        // Disclaimer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بیانیه سلب مسئولیت مالی و تحلیلی",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "این برنامه صرفاً یک ابزار تحلیلی، آماری و کمک‌آموزشی برای تصمیم‌گیری است و به هیچ وجه سیگنال مستقیم خرید و فروش یا مشاوره سرمایه‌گذاری محسوب نمی‌شود. معاملات فیوچرز پرپچوال کریپتو دارای ریسک شدید لیکوئیدیشن و از دست رفتن سرمایه است. نسخه اول هیچ معامله‌ای را به صورت خودکار انجام نمی‌دهد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
