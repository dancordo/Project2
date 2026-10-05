package com.example.provider

import com.example.engine.DerivativesEngine
import com.example.engine.TechnicalEngine
import com.example.model.*
import kotlin.random.Random

class DemoDataProvider : MarketDataProvider, DerivativesDataProvider, MacroAndNewsProvider {

    var activeScenario: Int = 1 // 1..8 scenarios

    private val basePrices = mapOf(
        "BTCUSDT" to 85238.0,
        "ETHUSDT" to 2701.90,
        "SOLUSDT" to 121.14,
        "BNBUSDT" to 795.00,
        "AVAXUSDT" to 10.995,
        "DOGEUSDT" to 0.09326,
        "LINKUSDT" to 14.08,
        "SUIUSDT" to 1.1746
    )

    override suspend fun getAssets(): List<AssetSummary> {
        return listOf(
            AssetSummary(
                symbol = "BTCUSDT",
                baseName = "Bitcoin",
                price = basePrices["BTCUSDT"]!!,
                change24h = 3.42,
                change1h = 0.45,
                change4h = 1.82,
                volume24h = 42800000000.0,
                relativeVolume = 1.85,
                openInterest = 18400000000.0,
                oiDeltaPct = 4.2,
                fundingRate8h = 0.0082,
                fundingZScore = 0.65,
                basisPct = 0.12,
                liquidations24h = 68400000.0,
                spotCvd = 48500000.0,
                perpCvd = 62000000.0,
                rsScore = 2.1,
                liquidityScore = 98,
                opportunityScore = 84,
                regime = MarketRegime.BULL,
                bias = TradeDirection.LONG,
                status = DataStatus.DEMO_DATA
            ),
            AssetSummary(
                symbol = "ETHUSDT",
                baseName = "Ethereum",
                price = basePrices["ETHUSDT"]!!,
                change24h = -0.85,
                change1h = -0.22,
                change4h = -0.55,
                volume24h = 19200000000.0,
                relativeVolume = 0.92,
                openInterest = 8900000000.0,
                oiDeltaPct = -2.1,
                fundingRate8h = 0.0025,
                fundingZScore = -0.45,
                basisPct = 0.04,
                liquidations24h = 41200000.0,
                spotCvd = -12000000.0,
                perpCvd = 14500000.0,
                rsScore = -1.8,
                liquidityScore = 94,
                opportunityScore = 48,
                regime = MarketRegime.RANGE,
                bias = TradeDirection.NO_TRADE,
                status = DataStatus.DEMO_DATA
            ),
            AssetSummary(
                symbol = "SOLUSDT",
                baseName = "Solana",
                price = basePrices["SOLUSDT"]!!,
                change24h = 6.84,
                change1h = 1.15,
                change4h = 3.90,
                volume24h = 9400000000.0,
                relativeVolume = 2.45,
                openInterest = 3850000000.0,
                oiDeltaPct = 8.4,
                fundingRate8h = 0.0125,
                fundingZScore = 1.15,
                basisPct = 0.22,
                liquidations24h = 34000000.0,
                spotCvd = 31000000.0,
                perpCvd = 38000000.0,
                rsScore = 4.8,
                liquidityScore = 88,
                opportunityScore = 88,
                regime = MarketRegime.STRONG_BULL,
                bias = TradeDirection.LONG,
                status = DataStatus.DEMO_DATA
            ),
            AssetSummary(
                symbol = "AVAXUSDT",
                baseName = "Avalanche",
                price = basePrices["AVAXUSDT"]!!,
                change24h = 2.15,
                change1h = 0.10,
                change4h = 1.20,
                volume24h = 1850000000.0,
                relativeVolume = 1.20,
                openInterest = 740000000.0,
                oiDeltaPct = 1.8,
                fundingRate8h = 0.0055,
                fundingZScore = 0.20,
                basisPct = 0.08,
                liquidations24h = 6500000.0,
                spotCvd = 4200000.0,
                perpCvd = 5100000.0,
                rsScore = 0.5,
                liquidityScore = 79,
                opportunityScore = 68,
                regime = MarketRegime.BULL,
                bias = TradeDirection.WATCH,
                status = DataStatus.DEMO_DATA
            ),
            AssetSummary(
                symbol = "DOGEUSDT",
                baseName = "Dogecoin",
                price = basePrices["DOGEUSDT"]!!,
                change24h = -3.40,
                change1h = -0.80,
                change4h = -1.90,
                volume24h = 2900000000.0,
                relativeVolume = 1.60,
                openInterest = 1450000000.0,
                oiDeltaPct = 14.5,
                fundingRate8h = 0.0280,
                fundingZScore = 2.45,
                basisPct = 0.35,
                liquidations24h = 18900000.0,
                spotCvd = -22000000.0,
                perpCvd = 45000000.0,
                rsScore = -4.2,
                liquidityScore = 82,
                opportunityScore = 42,
                regime = MarketRegime.BEAR,
                bias = TradeDirection.NO_TRADE,
                status = DataStatus.DEMO_DATA
            ),
            AssetSummary(
                symbol = "LINKUSDT",
                baseName = "Chainlink",
                price = basePrices["LINKUSDT"]!!,
                change24h = 4.10,
                change1h = 0.35,
                change4h = 2.10,
                volume24h = 1420000000.0,
                relativeVolume = 1.75,
                openInterest = 580000000.0,
                oiDeltaPct = 3.6,
                fundingRate8h = 0.0070,
                fundingZScore = 0.50,
                basisPct = 0.10,
                liquidations24h = 4200000.0,
                spotCvd = 11500000.0,
                perpCvd = 13000000.0,
                rsScore = 2.6,
                liquidityScore = 78,
                opportunityScore = 78,
                regime = MarketRegime.BULL,
                bias = TradeDirection.LONG,
                status = DataStatus.DEMO_DATA
            ),
            AssetSummary(
                symbol = "SUIUSDT",
                baseName = "Sui",
                price = basePrices["SUIUSDT"]!!,
                change24h = 8.90,
                change1h = 1.40,
                change4h = 5.20,
                volume24h = 2400000000.0,
                relativeVolume = 3.10,
                openInterest = 920000000.0,
                oiDeltaPct = 19.2,
                fundingRate8h = 0.0180,
                fundingZScore = 1.85,
                basisPct = 0.28,
                liquidations24h = 12400000.0,
                spotCvd = 18000000.0,
                perpCvd = 42000000.0,
                rsScore = 6.2,
                liquidityScore = 80,
                opportunityScore = 74,
                regime = MarketRegime.STRONG_BULL,
                bias = TradeDirection.WATCH,
                status = DataStatus.DEMO_DATA
            ),
            AssetSummary(
                symbol = "BNBUSDT",
                baseName = "BNB",
                price = basePrices["BNBUSDT"]!!,
                change24h = 0.95,
                change1h = 0.05,
                change4h = 0.40,
                volume24h = 1600000000.0,
                relativeVolume = 0.85,
                openInterest = 680000000.0,
                oiDeltaPct = 0.4,
                fundingRate8h = 0.0060,
                fundingZScore = 0.15,
                basisPct = 0.09,
                liquidations24h = 2900000.0,
                spotCvd = 1800000.0,
                perpCvd = 2200000.0,
                rsScore = -0.4,
                liquidityScore = 84,
                opportunityScore = 62,
                regime = MarketRegime.RANGE,
                bias = TradeDirection.NO_TRADE,
                status = DataStatus.DEMO_DATA
            )
        )
    }

    override suspend fun getCandles(symbol: String, timeframe: Timeframe): List<Candle> {
        val base = basePrices[symbol] ?: 100.0
        val count = 60
        val candles = mutableListOf<Candle>()
        val rnd = Random(symbol.hashCode() + timeframe.ordinal + activeScenario * 17)

        var current = base * 0.95
        val now = System.currentTimeMillis()
        val intervalMs = timeframe.minutes * 60 * 1000L

        for (i in 0 until count) {
            val ts = now - (count - i) * intervalMs
            val volatility = current * 0.008
            val delta = (rnd.nextDouble() - 0.47) * volatility

            // If scenario 4 (sweep + reclaim) near end:
            val close = when {
                activeScenario == 4 && i == count - 4 -> current - volatility * 2.8 // sweep down
                activeScenario == 4 && i >= count - 3 -> current + volatility * 1.5 // strong reclaim
                else -> current + delta
            }

            val high = maxOf(current, close) + rnd.nextDouble() * volatility * 0.8
            val low = minOf(current, close) - rnd.nextDouble() * volatility * 0.8
            val vol = (base * 12.0) * (0.8 + rnd.nextDouble() * 0.8) * if (i >= count - 5) 2.1 else 1.0

            candles.add(Candle(ts, current, high, low, close, vol))
            current = close
        }
        return candles
    }

    override suspend fun getTechnicalIndicators(symbol: String, timeframe: Timeframe): TechnicalIndicators {
        val candles = getCandles(symbol, timeframe)
        return TechnicalEngine.computeAll(candles, timeframe)
    }

    override suspend fun getOrderBook(symbol: String): OrderBookDepth {
        val isSol = symbol.contains("SOL")
        val isDoge = symbol.contains("DOGE")
        val (bid5, ask5) = if (isSol) Pair(1420000.0, 890000.0) else if (isDoge) Pair(540000.0, 1180000.0) else Pair(4200000.0, 3900000.0)
        val (bid10, ask10) = Pair(bid5 * 2.1, ask5 * 1.95)
        val obi = if (ask5 > 0) bid5 / ask5 else 1.0
        val interp = when {
            obi >= 1.50 -> "Strong bid-side book imbalance (>1.50 OBI); active passive buyer absorption"
            obi <= 0.67 -> "Strong ask-side imbalance (<0.67 OBI); heavy overhead ask distribution"
            else -> "Balanced order book liquidity"
        }
        return OrderBookDepth(
            bidDepth5bps = bid5,
            askDepth5bps = ask5,
            bidDepth10bps = bid10,
            askDepth10bps = ask10,
            obi = obi,
            interpretation = interp
        )
    }

    override suspend fun getDerivativesMetrics(symbol: String): DerivativesMetrics {
        val isBtc = symbol.contains("BTC")
        val isSol = symbol.contains("SOL")
        val isDoge = symbol.contains("DOGE")

        val oi = if (isBtc) 18450000000.0 else if (isSol) 3850000000.0 else 1450000000.0
        val oiDelta = if (isSol) 8.4 else if (isDoge) 14.5 else 4.2
        val funding = if (isDoge) 0.0280 else if (isSol) 0.0125 else 0.0082
        val fundingZ = if (isDoge) 2.45 else if (isSol) 1.15 else 0.65
        val liqType = if (isSol) LiquidationFlushType.LONG_FLUSH else if (isBtc) LiquidationFlushType.SHORT_FLUSH else LiquidationFlushType.NONE

        val crossRates = listOf(
            CrossExchangeFunding("Binance", funding),
            CrossExchangeFunding("Bybit", funding * 0.98),
            CrossExchangeFunding("OKX", funding * 1.04)
        )

        return DerivativesMetrics(
            openInterest = oi,
            oiChange24hPct = oiDelta,
            oiZScore = fundingZ * 0.8,
            fundingRate8h = funding,
            fundingZScore = fundingZ,
            fundingPercentile = if (fundingZ > 2.0) 96.5 else 62.0,
            crossExchangeFunding = crossRates,
            fundingCrowdingStatus = DerivativesEngine.interpretFundingCrowding(fundingZ),
            basisPerp = 0.12,
            basisAnnualized3M = 7.4,
            termStructure = BasisTerm.CONTANGO,
            longLiquidationVol24h = if (isSol) 24500000.0 else 18000000.0,
            shortLiquidationVol24h = if (isBtc) 48000000.0 else 12000000.0,
            liquidationIntensity = 74.5,
            liquidationFlushType = liqType,
            longShortRatio = 1.28,
            spotVolume24h = 42800000000.0,
            perpVolume24h = 68500000000.0,
            perpSpotVolumeRatio = 1.60,
            spotCvd = if (isDoge) -22000000.0 else 48500000.0,
            perpCvd = if (isDoge) 45000000.0 else 62000000.0,
            cvdDivergenceNote = if (isDoge)
                "Crowding Risk: Perp CVD aggressively buying while Spot CVD continuously dumps"
            else
                "Spot-led expansion: Spot CVD accumulation confirms price strength"
        )
    }

    override suspend fun getCrossExchangeFunding(symbol: String): List<CrossExchangeFunding> {
        val base = 0.0082
        return listOf(
            CrossExchangeFunding("Binance", base),
            CrossExchangeFunding("Bybit", base - 0.0015),
            CrossExchangeFunding("OKX", base + 0.0020),
            CrossExchangeFunding("Deribit", base - 0.0008)
        )
    }

    override suspend fun getOptionsContext(symbol: String): OptionsContext {
        return OptionsContext(
            impliedVolatility = 54.2,
            riskReversal25d = 2.4, // slight call skew
            putCallRatio = 0.68,
            maxPainPrice = 96000.0,
            expiryConcentration = "Monthly expiry concentrated at $100K call strike",
            gammaContext = "Dealer net long gamma above $98K; volatility expected to dampen in current zone"
        )
    }

    override suspend fun getMacroEvents(): List<MacroEvent> {
        return listOf(
            MacroEvent("US Core CPI (YoY)", "42 min", "08:30 EST", "High", "3.1%", "3.2%", isPreEventRisk = true),
            MacroEvent("FOMC Rate Decision", "3 days", "14:00 EST", "Critical", "5.25%", "5.50%", isPreEventRisk = false),
            MacroEvent("Non-Farm Payrolls (NFP)", "5 days", "08:30 EST", "High", "180K", "216K", isPreEventRisk = false),
            MacroEvent("US Initial Jobless Claims", "1 day", "08:30 EST", "Medium", "215K", "218K", isPreEventRisk = false)
        )
    }

    override suspend fun getCatalysts(): List<CatalystItem> {
        return listOf(
            CatalystItem(
                headline = "Institutional Spot ETF Net Inflow reaches +$580M in 24h",
                source = "Farside / CME Analytics",
                timestamp = "24m ago",
                relevance = "High",
                relatedAssets = listOf("BTC", "ETH"),
                confidence = "High (Verified ETF Custody)"
            ),
            CatalystItem(
                headline = "Solana Mainnet Breakpoint Upgrades deploy Firedancer v1 validator testnet",
                source = "Solana Foundation Tech Blog",
                timestamp = "1h 15m ago",
                relevance = "High",
                relatedAssets = listOf("SOL"),
                confidence = "Confirmed"
            ),
            CatalystItem(
                headline = "CME Group announces expanded Bitcoin & Ether Friday weekly options suites",
                source = "CME Release",
                timestamp = "3h ago",
                relevance = "Medium",
                relatedAssets = listOf("BTC", "ETH"),
                confidence = "Official"
            ),
            CatalystItem(
                headline = "Major token emission unlock scheduled for SUI & AVAX this week",
                source = "TokenUnlocks API",
                timestamp = "4h ago",
                relevance = "High",
                relatedAssets = listOf("SUI", "AVAX"),
                confidence = "On-Chain Verified"
            )
        )
    }

    override suspend fun getTokenUnlocks(): List<TokenUnlockItem> {
        return listOf(
            TokenUnlockItem(
                date = "Oct 06, 2026",
                token = "SUI",
                unlockAmount = 64000000.0,
                usdValue = 220800000.0,
                pctCirculating = 2.45,
                pctMarketCap = 2.40,
                pct7dVolume = 18.2,
                unlockPressure = "High",
                warningLevel = "MODERATE TO HIGH"
            ),
            TokenUnlockItem(
                date = "Oct 08, 2026",
                token = "AVAX",
                unlockAmount = 1670000.0,
                usdValue = 63800000.0,
                pctCirculating = 0.42,
                pctMarketCap = 0.41,
                pct7dVolume = 5.8,
                unlockPressure = "Low",
                warningLevel = "NORMAL"
            ),
            TokenUnlockItem(
                date = "Oct 12, 2026",
                token = "TIA",
                unlockAmount = 175000000.0,
                usdValue = 890000000.0,
                pctCirculating = 82.5,
                pctMarketCap = 78.0,
                pct7dVolume = 142.0,
                unlockPressure = "Extreme",
                warningLevel = "CRITICAL / CLIFF"
            )
        )
    }

    override suspend fun getAlerts(): List<AlertNotification> {
        return listOf(
            AlertNotification(
                id = "alert-1",
                symbol = "SOLUSDT",
                title = "Liquidity Sweep + Reclaim Confirmed",
                message = "5M structural reclaim of $221.80 Value Area Low with 2.4x volume expansion.",
                timestamp = "3m ago",
                severity = "CRITICAL"
            ),
            AlertNotification(
                id = "alert-2",
                symbol = "DOGEUSDT",
                title = "Crowded Derivatives Spike Alert",
                message = "Funding z-score reached +2.45 with OI +14.5% while Spot CVD declined.",
                timestamp = "12m ago",
                severity = "WARNING"
            ),
            AlertNotification(
                id = "alert-3",
                symbol = "MACRO",
                title = "Pre-Event Risk Warning",
                message = "US Core CPI release in 42 minutes. Spreads may widen; execution volatility high.",
                timestamp = "18m ago",
                severity = "WARNING"
            ),
            AlertNotification(
                id = "alert-4",
                symbol = "BTCUSDT",
                title = "Short Liquidation Flush",
                message = "$48M short liquidations flushed above $98,200. Open Interest normalized.",
                timestamp = "25m ago",
                severity = "INFO"
            )
        )
    }
}
