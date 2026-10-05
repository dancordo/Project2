package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TerminalRepository
import com.example.data.UserTerminalSettings
import com.example.engine.DerivativesEngine
import com.example.engine.RiskEngine
import com.example.engine.RiskCalculationInputs
import com.example.engine.RiskCalculationOutput
import com.example.engine.PortfolioRiskSummary
import com.example.engine.SetupAndScoringEngine
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TerminalTab(val label: String) {
    DASHBOARD("Dashboard"),
    MARKETS("Markets"),
    ASSET("Asset"),
    FLOW("Flow"),
    AI_ANALYST("AI Analyst"),
    RISK_JOURNAL("Risk/Journal"),
    SETTINGS("Settings")
}

data class TerminalUiState(
    val currentTab: TerminalTab = TerminalTab.DASHBOARD,
    val selectedSymbol: String = "BTCUSDT",
    val selectedTimeframe: Timeframe = Timeframe.M15,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLiveMode: Boolean = true, // Default to LIVE REAL-TIME DATA
    val currentStatus: DataStatus = DataStatus.LIVE_DATA,
    val activeScenario: Int = 4,
    val lastRefreshTime: String = "هم‌اکنون",
    val assets: List<AssetSummary> = emptyList(),
    val candles: List<Candle> = emptyList(),
    val indicators: TechnicalIndicators? = null,
    val derivatives: DerivativesMetrics? = null,
    val orderBook: OrderBookDepth? = null,
    val optionsContext: OptionsContext? = null,
    val tradeVerdict: TradeVerdict? = null,
    val macroEvents: List<MacroEvent> = emptyList(),
    val catalysts: List<CatalystItem> = emptyList(),
    val tokenUnlocks: List<TokenUnlockItem> = emptyList(),
    val alerts: List<AlertNotification> = emptyList(),
    val aiAnalysisText: String = "",
    val isAiLoading: Boolean = false,
    val riskInputs: RiskCalculationInputs = RiskCalculationInputs(),
    val riskOutput: RiskCalculationOutput? = null,
    val portfolioHeat: PortfolioRiskSummary? = null,
    val journalEntries: List<JournalEntry> = emptyList(),
    val watchlist: List<WatchlistItem> = emptyList(),
    val settings: UserTerminalSettings = UserTerminalSettings(),
    val statusBannerMessage: String? = null
)

class TerminalViewModel(
    private val repository: TerminalRepository = TerminalRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeRepository()
        startLivePollingLoop()
    }

    private fun startLivePollingLoop() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(3500)
                if (_uiState.value.isLiveMode) {
                    try {
                        val freshAssets = repository.getAssets()
                        val currentSym = _uiState.value.selectedSymbol
                        val updatedAsset = freshAssets.find { it.symbol == currentSym }

                        _uiState.value = _uiState.value.copy(
                            assets = freshAssets,
                            currentStatus = repository.currentStatus.value,
                            lastRefreshTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                        )

                        if (updatedAsset != null && _uiState.value.candles.isNotEmpty()) {
                            val lastCandle = _uiState.value.candles.last()
                            if (lastCandle.close != updatedAsset.price) {
                                val updatedCandles = _uiState.value.candles.dropLast(1) + lastCandle.copy(close = updatedAsset.price)
                                _uiState.value = _uiState.value.copy(candles = updatedCandles)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun refreshLiveData() {
        loadInitialData()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            repository.watchlist.collect { list ->
                _uiState.value = _uiState.value.copy(watchlist = list)
            }
        }
        viewModelScope.launch {
            repository.journalEntries.collect { entries ->
                _uiState.value = _uiState.value.copy(journalEntries = entries)
                recomputePortfolioHeat()
            }
        }
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
        viewModelScope.launch {
            repository.currentStatus.collect { status ->
                _uiState.value = _uiState.value.copy(currentStatus = status)
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val assetsList = repository.getAssets()
            val macro = repository.getMacroEvents()
            val cat = repository.getCatalysts()
            val unlocks = repository.getTokenUnlocks()
            val alertList = repository.getAlerts()

            _uiState.value = _uiState.value.copy(
                assets = assetsList,
                macroEvents = macro,
                catalysts = cat,
                tokenUnlocks = unlocks,
                alerts = alertList
            )

            loadAssetDetails(_uiState.value.selectedSymbol, _uiState.value.selectedTimeframe)
            recomputeRiskCalc()
            recomputePortfolioHeat()
        }
    }

    fun selectTab(tab: TerminalTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchActive = active)
    }

    fun selectSymbol(symbol: String) {
        _uiState.value = _uiState.value.copy(
            selectedSymbol = symbol,
            isSearchActive = false,
            currentTab = TerminalTab.ASSET
        )
        loadAssetDetails(symbol, _uiState.value.selectedTimeframe)
    }

    fun selectTimeframe(tf: Timeframe) {
        _uiState.value = _uiState.value.copy(selectedTimeframe = tf)
        loadAssetDetails(_uiState.value.selectedSymbol, tf)
    }

    fun toggleMode(isLive: Boolean) {
        repository.setMode(!isLive) // demo = !isLive
        val status = if (isLive) DataStatus.LIVE_DATA else DataStatus.DEMO_DATA
        _uiState.value = _uiState.value.copy(
            isLiveMode = isLive,
            currentStatus = status,
            statusBannerMessage = if (isLive) "دریافت نرخ‌های زنده و لحظه‌ای فعال شد." else "حالت شبیه‌سازی فعال شد."
        )
        loadInitialData()
    }

    fun selectScenario(index: Int) {
        repository.setScenario(index)
        _uiState.value = _uiState.value.copy(activeScenario = index)
        loadAssetDetails(_uiState.value.selectedSymbol, _uiState.value.selectedTimeframe)
    }

    private fun loadAssetDetails(symbol: String, timeframe: Timeframe) {
        viewModelScope.launch {
            val candles = repository.getCandles(symbol, timeframe)
            val indicators = repository.getTechnicalIndicators(symbol, timeframe)
            val derivatives = repository.getDerivativesMetrics(symbol)
            val orderBook = repository.getOrderBook(symbol)
            val options = repository.getOptionsContext(symbol)

            val currentPrice = candles.lastOrNull()?.close ?: 100.0
            val asset = _uiState.value.assets.find { it.symbol == symbol }
            val regime = asset?.regime ?: MarketRegime.BULL
            val rs = asset?.rsScore ?: 1.0

            val verdict = SetupAndScoringEngine.evaluateVerdict(
                symbol = symbol,
                currentPrice = currentPrice,
                regime = regime,
                adx = indicators.adx14,
                rsScore = rs,
                spotCvd = derivatives.spotCvd,
                perpCvd = derivatives.perpCvd,
                oiDeltaPct = derivatives.oiChange24hPct,
                fundingZScore = derivatives.fundingZScore,
                isLiquiditySwept = _uiState.value.activeScenario == 4 || derivatives.liquidationFlushType == LiquidationFlushType.LONG_FLUSH,
                isLevelReclaimed = true,
                isFailedBreakout = _uiState.value.activeScenario == 6,
                valPrice = indicators.volumeProfile.valPrice,
                vahPrice = indicators.volumeProfile.vah,
                sessionVwap = indicators.sessionVwap,
                status = _uiState.value.currentStatus
            )

            _uiState.value = _uiState.value.copy(
                candles = candles,
                indicators = indicators,
                derivatives = derivatives,
                orderBook = orderBook,
                optionsContext = options,
                tradeVerdict = verdict,
                riskInputs = _uiState.value.riskInputs.copy(
                    entryPrice = currentPrice,
                    stopPrice = verdict.stopLoss,
                    tp1Price = verdict.tp1,
                    tp2Price = verdict.tp2,
                    tp3Price = verdict.tp3
                )
            )
            recomputeRiskCalc()
        }
    }

    fun requestAiAnalysis() {
        val s = _uiState.value
        val verdict = s.tradeVerdict ?: return
        val derivatives = s.derivatives ?: return
        val indicators = s.indicators ?: return
        val price = s.candles.lastOrNull()?.close ?: 100.0

        _uiState.value = _uiState.value.copy(isAiLoading = true)

        viewModelScope.launch {
            val response = repository.aiProvider.generateAnalysis(
                symbol = s.selectedSymbol,
                price = price,
                regime = verdict.opportunityScore.grade.let { MarketRegime.BULL },
                verdict = verdict,
                derivatives = derivatives,
                indicators = indicators,
                dataStatus = s.currentStatus
            )
            _uiState.value = _uiState.value.copy(
                aiAnalysisText = response,
                isAiLoading = false
            )
        }
    }

    fun updateRiskInputs(newInputs: RiskCalculationInputs) {
        _uiState.value = _uiState.value.copy(riskInputs = newInputs)
        recomputeRiskCalc()
    }

    private fun recomputeRiskCalc() {
        val out = RiskEngine.calculateRisk(_uiState.value.riskInputs)
        _uiState.value = _uiState.value.copy(riskOutput = out)
    }

    private fun recomputePortfolioHeat() {
        val positions = listOf(
            Pair("BTC", 14000.0),
            Pair("SOL", 8000.0),
            Pair("ETH", -5000.0)
        )
        val dailyLoss = _uiState.value.journalEntries.filter { !it.isWin }.sumOf { it.rMultiple }
        val heat = RiskEngine.evaluatePortfolioHeat(
            accountEquity = _uiState.value.riskInputs.accountEquity,
            positions = positions,
            dailyPnlR = dailyLoss,
            killSwitchLimitR = _uiState.value.settings.dailyMaxLossR
        )
        _uiState.value = _uiState.value.copy(portfolioHeat = heat)
    }

    fun toggleWatchlistPin(symbol: String) {
        repository.toggleWatchlistPin(symbol)
    }

    fun addToWatchlist(symbol: String) {
        val asset = _uiState.value.assets.find { it.symbol == symbol }
        val item = WatchlistItem(
            symbol = symbol,
            preferredTimeframe = _uiState.value.selectedTimeframe,
            bias = asset?.bias ?: TradeDirection.WATCH,
            notes = "Added from terminal scanner",
            setupType = _uiState.value.tradeVerdict?.setupName ?: "Technical Setup",
            isPinned = false
        )
        repository.addToWatchlist(item)
    }

    fun addCurrentTradeToJournal() {
        val v = _uiState.value.tradeVerdict ?: return
        val r = _uiState.value.riskOutput ?: return
        val entry = JournalEntry(
            id = java.util.UUID.randomUUID().toString(),
            date = "2026-10-04 09:30",
            asset = v.symbol,
            direction = v.direction,
            setup = v.setupName,
            regime = MarketRegime.BULL,
            entryPrice = _uiState.value.riskInputs.entryPrice,
            stopPrice = v.stopLoss,
            tp1Price = v.tp1,
            tp2Price = v.tp2,
            tp3Price = v.tp3,
            riskPercent = _uiState.value.riskInputs.riskPercent,
            notional = r.positionNotional,
            leverage = r.effectiveLeverage,
            rMultiple = 2.0,
            mfe = 2.5,
            mae = 0.3,
            holdingTime = "Open",
            session = "NY Morning",
            catalyst = "Market Regime expansion",
            reasonEntry = v.primaryReasons.firstOrNull() ?: "Setup confirmed",
            reasonExit = "Active in journal",
            mistake = "None",
            isWin = true
        )
        repository.addJournalEntry(entry)
    }

    fun updateSettings(settings: UserTerminalSettings) {
        repository.updateSettings(settings)
    }
}
