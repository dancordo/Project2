package com.example.provider

import com.example.model.*
import kotlinx.coroutines.flow.Flow

interface MarketDataProvider {
    suspend fun getAssets(): List<AssetSummary>
    suspend fun getCandles(symbol: String, timeframe: Timeframe): List<Candle>
    suspend fun getTechnicalIndicators(symbol: String, timeframe: Timeframe): TechnicalIndicators
    suspend fun getOrderBook(symbol: String): OrderBookDepth
}

interface DerivativesDataProvider {
    suspend fun getDerivativesMetrics(symbol: String): DerivativesMetrics
    suspend fun getCrossExchangeFunding(symbol: String): List<CrossExchangeFunding>
    suspend fun getOptionsContext(symbol: String): OptionsContext
}

interface MacroAndNewsProvider {
    suspend fun getMacroEvents(): List<MacroEvent>
    suspend fun getCatalysts(): List<CatalystItem>
    suspend fun getTokenUnlocks(): List<TokenUnlockItem>
    suspend fun getAlerts(): List<AlertNotification>
}

interface AIProvider {
    val providerName: String
    suspend fun generateAnalysis(
        symbol: String,
        price: Double,
        regime: MarketRegime,
        verdict: TradeVerdict,
        derivatives: DerivativesMetrics,
        indicators: TechnicalIndicators,
        dataStatus: DataStatus
    ): String
}
