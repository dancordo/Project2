package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DataStatusBadge
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.TerminalTab
import com.example.viewmodel.TerminalUiState
import com.example.viewmodel.TerminalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTerminalScreen(
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    // RTL Layout Direction for authentic Persian experience
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(TerminalBg)
                .testTag("main_terminal_screen"),
            containerColor = TerminalBg,
            topBar = {
                TerminalTopBar(
                    state = state,
                    onToggleSearch = { viewModel.toggleSearchActive(!state.isSearchActive) },
                    onToggleMode = { viewModel.toggleMode(!state.isLiveMode) },
                    onRefresh = { viewModel.refreshLiveData() }
                )
            },
            bottomBar = {
                TerminalBottomNavBar(
                    currentTab = state.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (state.currentTab) {
                    TerminalTab.DASHBOARD -> DashboardScreen(state = state, viewModel = viewModel)
                    TerminalTab.MARKETS -> MarketsScreen(state = state, viewModel = viewModel)
                    TerminalTab.ASSET -> AssetDetailScreen(state = state, viewModel = viewModel)
                    TerminalTab.FLOW -> FlowScreen(state = state, viewModel = viewModel)
                    TerminalTab.AI_ANALYST -> AiAnalystScreen(state = state, viewModel = viewModel)
                    TerminalTab.RISK_JOURNAL -> RiskAndJournalScreen(state = state, viewModel = viewModel)
                    TerminalTab.SETTINGS -> SettingsScreen(state = state, viewModel = viewModel)
                }

                if (state.isSearchActive) {
                    GlobalSearchOverlay(
                        state = state,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onSelectSymbol = { viewModel.selectSymbol(it) },
                        onClose = { viewModel.toggleSearchActive(false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalTopBar(
    state: TerminalUiState,
    onToggleSearch: () -> Unit,
    onToggleMode: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        color = TerminalSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalCyan.copy(alpha = 0.2f))
                            .border(1.dp, TerminalCyan, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TerminalCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "فیوچرز ادج هوشمند",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (state.isLiveMode) BullGreen else WarningAmber)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isLiveMode) "نرخ لحظه‌ای (${state.lastRefreshTime})" else "حالت شبیه‌سازی",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 9.sp,
                                color = if (state.isLiveMode) BullGreen else TextMuted
                            )
                        }
                    }
                }

                // Status Badge, Refresh & Search
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DataStatusBadge(
                        status = state.currentStatus,
                        onClick = onToggleMode
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalCard)
                            .border(1.dp, TerminalBorder, RoundedCornerShape(4.dp))
                            .testTag("btn_refresh_live")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تازه‌سازی قیمت‌ها",
                            tint = BullGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleSearch,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalCard)
                            .border(1.dp, TerminalBorder, RoundedCornerShape(4.dp))
                            .testTag("btn_global_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجوی نمادها",
                            tint = TerminalCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalBottomNavBar(
    currentTab: TerminalTab,
    onTabSelected: (TerminalTab) -> Unit
) {
    Surface(
        color = TerminalSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TerminalTabItem("داشبورد", Icons.Default.Dashboard, currentTab == TerminalTab.DASHBOARD) { onTabSelected(TerminalTab.DASHBOARD) }
            TerminalTabItem("بازارها", Icons.Default.ShowChart, currentTab == TerminalTab.MARKETS) { onTabSelected(TerminalTab.MARKETS) }
            TerminalTabItem("تحلیل نماد", Icons.Default.CandlestickChart, currentTab == TerminalTab.ASSET) { onTabSelected(TerminalTab.ASSET) }
            TerminalTabItem("جریان سفارشات", Icons.Default.WaterfallChart, currentTab == TerminalTab.FLOW) { onTabSelected(TerminalTab.FLOW) }
            TerminalTabItem("هوش مصنوعی", Icons.Default.AutoAwesome, currentTab == TerminalTab.AI_ANALYST) { onTabSelected(TerminalTab.AI_ANALYST) }
            TerminalTabItem("ریسک و ژورنال", Icons.Default.Calculate, currentTab == TerminalTab.RISK_JOURNAL) { onTabSelected(TerminalTab.RISK_JOURNAL) }
            TerminalTabItem("تنظیمات", Icons.Default.Settings, currentTab == TerminalTab.SETTINGS) { onTabSelected(TerminalTab.SETTINGS) }
        }
    }
}

@Composable
private fun TerminalTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) TerminalCardElevated else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("nav_tab_$label"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) TerminalCyan else TextMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = FontFamily.SansSerif,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) TerminalCyan else TextMuted
        )
    }
}

@Composable
private fun GlobalSearchOverlay(
    state: TerminalUiState,
    onQueryChange: (String) -> Unit,
    onSelectSymbol: (String) -> Unit,
    onClose: () -> Unit
) {
    val results = state.assets.filter {
        state.searchQuery.isBlank() || it.symbol.contains(state.searchQuery, ignoreCase = true) || it.baseName.contains(state.searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onClose() }
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCardElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "جستجوی زنده در نمادهای بازار",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalCyan
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("مثال: BTC, ETH, SOL, بیت‌کوین...", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TerminalCyan, unfocusedBorderColor = TerminalBorder)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(results) { asset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSymbol(asset.symbol) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(asset.symbol, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(asset.baseName, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$${"%.2f".format(asset.price)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                                Text("${if (asset.change24h >= 0) "+" else ""}${"%.2f".format(asset.change24h)}%", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = if (asset.change24h >= 0) BullGreen else BearRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
