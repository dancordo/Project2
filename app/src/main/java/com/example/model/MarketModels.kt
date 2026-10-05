package com.example.model

enum class MarketRegime(val label: String, val description: String) {
    STRONG_BULL("گاوی قوی (STRONG BULL)", "انباشت تهاجمی در بازار اسپات، شتاب روند در حال گسترش"),
    BULL("گاوی صعودی (BULL)", "سقف‌ها و کف‌های بالاتر، جذب فشار فروش در اصلاح‌ها"),
    NEUTRAL("خنثی (NEUTRAL)", "بازار متعادل، عدم حضور بازیگر مسلط جهتی"),
    RANGE("رنج نوسانی (RANGE)", "برگشت به میانگین بین لبه‌های ناحیه ارزش، مومنتوم پایین"),
    BEAR("خرسی نزولی (BEAR)", "تشکیل سقف‌های پایین‌تر، توزیع اسپات در پولبک‌ها"),
    STRONG_BEAR("خرسی شدید (STRONG BEAR)", "آبشار لیکوئیدیشن، شکست سنگین ساختار بازار"),
    SHOCK_EVENT("شوک رویداد (SHOCK)", "شوک نوسانی غیرعادی، ابطال مدل‌های استاندارد")
}

enum class DataStatus(val label: String) {
    LIVE_DATA("داده زنده واقعی"),
    DEMO_DATA("داده شبیه‌سازی"),
    STALE_DATA("داده منقضی شده"),
    PARTIAL_DATA("داده ناقص"),
    DATA_UNAVAILABLE("داده در دسترس نیست")
}

enum class TradeDirection(val label: String) {
    LONG("خرید (LONG)"),
    SHORT("فروش (SHORT)"),
    WATCH("زیر نظر (WATCH)"),
    NO_TRADE("عدم معامله (NO TRADE)")
}

enum class Timeframe(val label: String, val minutes: Int) {
    M1("۱ دقیقه", 1),
    M5("۵ دقیقه", 5),
    M15("۱۵ دقیقه", 15),
    H1("۱ ساعت", 60),
    H4("۴ ساعت", 240),
    D1("روزانه", 1440)
}

enum class StructureType(val code: String, val isBullish: Boolean) {
    HH("HH", true),
    HL("HL", true),
    LH("LH", false),
    LL("LL", false),
    BOS("BOS", true),
    FAILED_BOS("FAILED BOS", false),
    SWING_HIGH("SWING H", false),
    SWING_LOW("SWING L", true)
}

enum class LiquidationFlushType(val label: String) {
    LONG_FLUSH("تخلیه لانگ‌ها (LONG FLUSH)"),
    SHORT_FLUSH("اسکوییز شورت‌ها (SHORT FLUSH)"),
    MIXED("ترکیبی (MIXED)"),
    NONE("عادی (NONE)")
}

enum class BasisTerm(val label: String) {
    CONTANGO("کونتانگو (Contango)"),
    NEUTRAL("خنثی (Neutral)"),
    BACKWARDATION("بک‌واردیشن (Backwardation)")
}

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class StructureMarker(
    val type: StructureType,
    val price: Double,
    val index: Int,
    val label: String
)

data class VolumeProfileLevel(
    val poc: Double,
    val vah: Double,
    val valPrice: Double,
    val hvns: List<Double>,
    val lvns: List<Double>
)

data class TechnicalIndicators(
    val ema20: Double,
    val ema50: Double,
    val ema200: Double,
    val sessionVwap: Double,
    val weeklyAvwap: Double,
    val atr14: Double,
    val adx14: Double,
    val rsi14: Double,
    val volumeProfile: VolumeProfileLevel,
    val structureMarkers: List<StructureMarker>
)

data class OrderBookLevel(
    val price: Double,
    val size: Double,
    val isBid: Boolean
)

data class OrderBookDepth(
    val bidDepth5bps: Double,
    val askDepth5bps: Double,
    val bidDepth10bps: Double,
    val askDepth10bps: Double,
    val obi: Double,
    val interpretation: String
)

data class CrossExchangeFunding(
    val exchange: String,
    val rate: Double,
    val isPredicted: Boolean = false
)

data class DerivativesMetrics(
    val openInterest: Double,
    val oiChange24hPct: Double,
    val oiZScore: Double,
    val fundingRate8h: Double,
    val fundingZScore: Double,
    val fundingPercentile: Double,
    val crossExchangeFunding: List<CrossExchangeFunding>,
    val fundingCrowdingStatus: String,
    val basisPerp: Double,
    val basisAnnualized3M: Double,
    val termStructure: BasisTerm,
    val longLiquidationVol24h: Double,
    val shortLiquidationVol24h: Double,
    val liquidationIntensity: Double,
    val liquidationFlushType: LiquidationFlushType,
    val longShortRatio: Double,
    val spotVolume24h: Double,
    val perpVolume24h: Double,
    val perpSpotVolumeRatio: Double,
    val spotCvd: Double,
    val perpCvd: Double,
    val cvdDivergenceNote: String
)

data class ScoreAuditItem(
    val category: String,
    val pointsAwarded: Int,
    val maxPoints: Int,
    val reason: String
)

data class OpportunityScoreResult(
    val totalScore: Int,
    val grade: String, // A+, A, B, NO TRADE
    val regimeScore: Int,
    val relativeStrengthScore: Int,
    val liquidityScore: Int,
    val derivativesScore: Int,
    val spotFlowScore: Int,
    val orderFlowScore: Int,
    val catalystScore: Int,
    val auditTrail: List<ScoreAuditItem>
)

data class TradeVerdict(
    val symbol: String,
    val direction: TradeDirection,
    val confidence: Int,
    val opportunityScore: OpportunityScoreResult,
    val setupName: String,
    val entryZone: String,
    val stopLoss: Double,
    val tp1: Double,
    val tp2: Double,
    val tp3: Double,
    val rrRatio: Double,
    val primaryReasons: List<String>,
    val primaryRisks: List<String>,
    val invalidation: String,
    val failedAuctionDetected: Boolean = false,
    val dataQuality: DataStatus = DataStatus.DEMO_DATA
)

data class RelativeStrengthMetrics(
    val rs1h: Double,
    val rs4h: Double,
    val rs1d: Double,
    val rs7d: Double,
    val acceleration: String,
    val btcRelativeBreakout: Boolean,
    val rank: Int
)

data class AssetSummary(
    val symbol: String,
    val baseName: String,
    val price: Double,
    val change24h: Double,
    val change1h: Double,
    val change4h: Double,
    val volume24h: Double,
    val relativeVolume: Double,
    val openInterest: Double,
    val oiDeltaPct: Double,
    val fundingRate8h: Double,
    val fundingZScore: Double,
    val basisPct: Double,
    val liquidations24h: Double,
    val spotCvd: Double,
    val perpCvd: Double,
    val rsScore: Double,
    val liquidityScore: Int,
    val opportunityScore: Int,
    val regime: MarketRegime,
    val bias: TradeDirection,
    val status: DataStatus = DataStatus.DEMO_DATA
)

data class TokenUnlockItem(
    val date: String,
    val token: String,
    val unlockAmount: Double,
    val usdValue: Double,
    val pctCirculating: Double,
    val pctMarketCap: Double,
    val pct7dVolume: Double,
    val unlockPressure: String, // Low, Moderate, High, Extreme
    val warningLevel: String
)

data class MacroEvent(
    val eventName: String,
    val timeRemaining: String,
    val scheduledTime: String,
    val importance: String, // High, Critical, Medium
    val consensus: String,
    val previous: String,
    val isPreEventRisk: Boolean
)

data class CatalystItem(
    val headline: String,
    val source: String,
    val timestamp: String,
    val relevance: String,
    val relatedAssets: List<String>,
    val confidence: String
)

data class OptionsContext(
    val impliedVolatility: Double,
    val riskReversal25d: Double,
    val putCallRatio: Double,
    val maxPainPrice: Double,
    val expiryConcentration: String,
    val gammaContext: String
)

data class JournalEntry(
    val id: String,
    val date: String,
    val asset: String,
    val direction: TradeDirection,
    val setup: String,
    val regime: MarketRegime,
    val entryPrice: Double,
    val stopPrice: Double,
    val tp1Price: Double,
    val tp2Price: Double,
    val tp3Price: Double,
    val riskPercent: Double,
    val notional: Double,
    val leverage: Double,
    val rMultiple: Double,
    val mfe: Double,
    val mae: Double,
    val holdingTime: String,
    val session: String,
    val catalyst: String,
    val reasonEntry: String,
    val reasonExit: String,
    val mistake: String,
    val isWin: Boolean
)

data class WatchlistItem(
    val symbol: String,
    val preferredTimeframe: Timeframe,
    val bias: TradeDirection,
    val notes: String,
    val setupType: String,
    val isPinned: Boolean = false,
    val alertOnScore80: Boolean = true
)

data class AlertNotification(
    val id: String,
    val symbol: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val severity: String // CRITICAL, WARNING, INFO
)
