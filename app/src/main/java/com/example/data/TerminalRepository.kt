package com.example.data

import com.example.ai.InstitutionalAiProvider
import com.example.engine.DerivativesEngine
import com.example.engine.RiskEngine
import com.example.engine.SetupAndScoringEngine
import com.example.engine.TechnicalEngine
import com.example.model.*
import com.example.provider.DemoDataProvider
import com.example.provider.LiveMarketDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UserTerminalSettings(
    val isDemoMode: Boolean = false, // Default to LIVE REAL DATA as requested
    val selectedTimeframe: Timeframe = Timeframe.M15,
    val darkTheme: Boolean = true,
    val defaultCurrency: String = "USD",
    val riskPercentPresetA: Double = 0.50,
    val riskPercentPresetB: Double = 0.35,
    val riskPercentPresetC: Double = 0.20,
    val dailyMaxLossR: Double = -3.0,
    val correlatedRiskThresholdPct: Double = 1.0,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",
    val geminiTemperature: Double = 0.20,
    val structureSensitivityLeftBars: Int = 5,
    val structureSensitivityRightBars: Int = 5,
    val fastMetricStaleSeconds: Int = 30
)

class TerminalRepository {

    private val liveProvider = LiveMarketDataProvider()
    private val demoProvider = DemoDataProvider()
    val aiProvider = InstitutionalAiProvider()

    private val _settings = MutableStateFlow(UserTerminalSettings())
    val settings: StateFlow<UserTerminalSettings> = _settings.asStateFlow()

    private val _currentStatus = MutableStateFlow(DataStatus.LIVE_DATA)
    val currentStatus: StateFlow<DataStatus> = _currentStatus.asStateFlow()

    private val _watchlist = MutableStateFlow<List<WatchlistItem>>(
        listOf(
            WatchlistItem("BTCUSDT", Timeframe.H1, TradeDirection.LONG, "انباشت نهادی کلان در سطح ماهانه", "بریک‌اوت و تثبیت در بالای سطح ارزش", isPinned = true),
            WatchlistItem("ETHUSDT", Timeframe.M15, TradeDirection.NO_TRADE, "فشردگی نوسانات درون محدوده رنج", "برگشت به میانگین در رنج", isPinned = false),
            WatchlistItem("SOLUSDT", Timeframe.M15, TradeDirection.LONG, "برخورد با نقدینگی و بازپس‌گیری سریع در سطح VAL", "سویپ نقدینگی + ورود لانگ", isPinned = true)
        )
    )
    val watchlist: StateFlow<List<WatchlistItem>> = _watchlist.asStateFlow()

    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(
        listOf(
            JournalEntry(
                id = UUID.randomUUID().toString(),
                date = "۱۴۰۳/۰۷/۱۲ ۱۴:۳۰",
                asset = "SOLUSDT",
                direction = TradeDirection.LONG,
                setup = "سویپ نقدینگی + بازپس‌گیری سطح ارزش",
                regime = MarketRegime.STRONG_BULL,
                entryPrice = 218.40,
                stopPrice = 215.80,
                tp1Price = 222.0,
                tp2Price = 226.50,
                tp3Price = 232.0,
                riskPercent = 0.50,
                notional = 12000.0,
                leverage = 2.4,
                rMultiple = 2.65,
                mfe = 3.1,
                mae = 0.25,
                holdingTime = "۲ ساعت و ۴۵ دقیقه",
                session = "آغاز سشن نیویورک",
                catalyst = "اخبار ارتقای فایردنسر سولانا",
                reasonEntry = "تخلیه کف قبلی، جهش مثبت دلتای اسپات و تایید بازپس‌گیری ۵ دقیقه.",
                reasonExit = "خروج پله‌ای در تارگت ۲ (۲۲۶.۵۰ دلار) و ریسک‌فری باقی‌مانده.",
                mistake = "بدون خطا. اجرای کاملاً منطبق بر قوانین ترمینال.",
                isWin = true
            ),
            JournalEntry(
                id = UUID.randomUUID().toString(),
                date = "۱۴۰۳/۰۷/۱۱ ۰۹:۱۵",
                asset = "ETHUSDT",
                direction = TradeDirection.SHORT,
                setup = "حراج ناموفق و فیک بریک در سقف",
                regime = MarketRegime.RANGE,
                entryPrice = 3520.0,
                stopPrice = 3548.0,
                tp1Price = 3480.0,
                tp2Price = 3440.0,
                tp3Price = 3400.0,
                riskPercent = 0.35,
                notional = 8500.0,
                leverage = 1.8,
                rMultiple = 2.10,
                mfe = 2.4,
                mae = 0.40,
                holdingTime = "۴ ساعت و ۱۰ دقیقه",
                session = "سشن صبح لندن",
                catalyst = "ندارد",
                reasonEntry = "سویپ سقف ناحیه ارزش (VAH) همزمان با کاهش حجم اسپات و تراکم پرپچوال.",
                reasonExit = "کاور پوزیشن در محدوده میانگین وزنی حجمی (VWAP) و POC.",
                mistake = "خروج زودهنگام قبل از رسیدن قیمت به لبه پایینی ارزش (VAL).",
                isWin = true
            ),
            JournalEntry(
                id = UUID.randomUUID().toString(),
                date = "۱۴۰۳/۰۷/۱۰ ۱۶:۰۰",
                asset = "DOGEUSDT",
                direction = TradeDirection.LONG,
                setup = "تلاش ناموفق برای بریک‌اوت",
                regime = MarketRegime.RANGE,
                entryPrice = 0.295,
                stopPrice = 0.290,
                tp1Price = 0.305,
                tp2Price = 0.315,
                tp3Price = 0.330,
                riskPercent = 0.20,
                notional = 4000.0,
                leverage = 1.0,
                rMultiple = -1.0,
                mfe = 0.3,
                mae = 1.0,
                holdingTime = "۴۵ دقیقه",
                session = "عصر نیویورک",
                catalyst = "شایعات شبکه‌های اجتماعی",
                reasonEntry = "پیش‌بینی شکست سطح بدون صبر برای تثبیت حجم.",
                reasonExit = "فعال شدن دقیق حد ضرر بر اساس استراتژی.",
                mistake = "نقض قانون: ترید در میانه رنج بدون تاییدیه دلتای اسپات.",
                isWin = false
            )
        )
    )
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()

    fun setMode(isDemo: Boolean) {
        _settings.value = _settings.value.copy(isDemoMode = isDemo)
        _currentStatus.value = if (isDemo) DataStatus.DEMO_DATA else DataStatus.LIVE_DATA
    }

    fun updateSettings(newSettings: UserTerminalSettings) {
        _settings.value = newSettings
        aiProvider.updateConfig(newSettings.geminiApiKey, newSettings.geminiModel, newSettings.geminiTemperature)
    }

    fun setScenario(scenarioIndex: Int) {
        demoProvider.activeScenario = scenarioIndex
    }

    suspend fun getAssets(): List<AssetSummary> {
        return if (!_settings.value.isDemoMode) {
            try {
                val liveList = liveProvider.getAssets()
                _currentStatus.value = DataStatus.LIVE_DATA
                liveList
            } catch (e: Exception) {
                // If network fails, use fallback and flag partial
                _currentStatus.value = DataStatus.PARTIAL_DATA
                demoProvider.getAssets().map { it.copy(status = DataStatus.PARTIAL_DATA) }
            }
        } else {
            _currentStatus.value = DataStatus.DEMO_DATA
            demoProvider.getAssets().map { it.copy(status = DataStatus.DEMO_DATA) }
        }
    }

    suspend fun getCandles(symbol: String, timeframe: Timeframe): List<Candle> {
        return if (!_settings.value.isDemoMode) {
            try {
                liveProvider.getCandles(symbol, timeframe)
            } catch (e: Exception) {
                demoProvider.getCandles(symbol, timeframe)
            }
        } else {
            demoProvider.getCandles(symbol, timeframe)
        }
    }

    suspend fun getTechnicalIndicators(symbol: String, timeframe: Timeframe): TechnicalIndicators {
        val candles = getCandles(symbol, timeframe)
        return TechnicalEngine.computeAll(candles, timeframe)
    }

    suspend fun getDerivativesMetrics(symbol: String): DerivativesMetrics {
        return if (!_settings.value.isDemoMode) {
            try {
                liveProvider.getDerivativesMetrics(symbol)
            } catch (e: Exception) {
                demoProvider.getDerivativesMetrics(symbol)
            }
        } else {
            demoProvider.getDerivativesMetrics(symbol)
        }
    }

    suspend fun getOrderBook(symbol: String): OrderBookDepth {
        return if (!_settings.value.isDemoMode) {
            try {
                liveProvider.getOrderBook(symbol)
            } catch (e: Exception) {
                demoProvider.getOrderBook(symbol)
            }
        } else {
            demoProvider.getOrderBook(symbol)
        }
    }

    suspend fun getOptionsContext(symbol: String): OptionsContext {
        return demoProvider.getOptionsContext(symbol)
    }

    suspend fun getMacroEvents(): List<MacroEvent> {
        return listOf(
            MacroEvent("شاخص تورم هسته آمریکا (Core CPI)", "۴۲ دقیقه", "۱۶:۰۰", "بحرانی", "۳.۱٪", "۳.۲٪", isPreEventRisk = true),
            MacroEvent("تصمیم‌گیری نرخ بهره فدرال رزرو (FOMC)", "۳ روز", "۲۱:۳۰", "بحرانی", "۵.۲۵٪", "۵.۵۰٪", isPreEventRisk = false),
            MacroEvent("داده اشتغال غیرکشاورزی (NFP)", "۵ روز", "۱۶:۰۰", "زیاد", "۱۸۰K", "۲۱۶K", isPreEventRisk = false),
            MacroEvent("درخواست‌های بیمه بیکاری اولیه آمریکا", "۱ روز", "۱۶:۰۰", "متوسط", "۲۱۵K", "۲۱۸K", isPreEventRisk = false)
        )
    }

    suspend fun getCatalysts(): List<CatalystItem> {
        return listOf(
            CatalystItem(
                headline = "ورود خالص سرمایه به صندوق‌های ETF اسپات بیت‌کوین به ۵۸۰+ میلیون دلار رسید",
                source = "تحلیل‌های بورس کالای شیکاگو (CME)",
                timestamp = "۲۴ دقیقه پیش",
                relevance = "بسیار زیاد",
                relatedAssets = listOf("BTC", "ETH"),
                confidence = "تایید رسمی نهادهای حضانتی"
            ),
            CatalystItem(
                headline = "استقرار نودهای اعتبارسنج ارتقای فایردنسر بر روی تست‌نت اصلی سولانا",
                source = "تیم فنی بنیاد سولانا",
                timestamp = "۱ ساعت پیش",
                relevance = "زیاد",
                relatedAssets = listOf("SOL"),
                confidence = "تایید شده در زنجیره"
            ),
            CatalystItem(
                headline = "گروه CME سررسیدهای هفتگی جدید برای قراردادهای آپشن بیت‌کوین و اتریوم را معرفی کرد",
                source = "اطلاعیه رسمی CME",
                timestamp = "۳ ساعت پیش",
                relevance = "متوسط",
                relatedAssets = listOf("BTC", "ETH"),
                confidence = "رسمی"
            ),
            CatalystItem(
                headline = "برنامه آزادسازی عرضه (Token Unlock) برای توکن‌های سویی و آوالانچ در هفته جاری",
                source = "رصدگر TokenUnlocks",
                timestamp = "۴ ساعت پیش",
                relevance = "زیاد",
                relatedAssets = listOf("SUI", "AVAX"),
                confidence = "تایید آنچین"
            )
        )
    }

    suspend fun getTokenUnlocks(): List<TokenUnlockItem> {
        return listOf(
            TokenUnlockItem(
                date = "۱۵ مهر ۱۴۰۳",
                token = "SUI",
                unlockAmount = 64000000.0,
                usdValue = 220800000.0,
                pctCirculating = 2.45,
                pctMarketCap = 2.40,
                pct7dVolume = 18.2,
                unlockPressure = "زیاد (High)",
                warningLevel = "فشار عرضه ملایم تا زیاد"
            ),
            TokenUnlockItem(
                date = "۱۷ مهر ۱۴۰۳",
                token = "AVAX",
                unlockAmount = 1670000.0,
                usdValue = 63800000.0,
                pctCirculating = 0.42,
                pctMarketCap = 0.41,
                pct7dVolume = 5.8,
                unlockPressure = "پایین (Low)",
                warningLevel = "عادی و قابل جذب"
            ),
            TokenUnlockItem(
                date = "۲۱ مهر ۱۴۰۳",
                token = "TIA",
                unlockAmount = 175000000.0,
                usdValue = 890000000.0,
                pctCirculating = 82.5,
                pctMarketCap = 78.0,
                pct7dVolume = 142.0,
                unlockPressure = "بحرانی (Extreme)",
                warningLevel = "ریسک شکست قیمت / عرضه تهاجمی"
            )
        )
    }

    suspend fun getAlerts(): List<AlertNotification> {
        return listOf(
            AlertNotification(
                id = "alert-1",
                symbol = "SOLUSDT",
                title = "تایید سویپ نقدینگی و بازپس‌گیری سطح ارزش",
                message = "تثبیت کندل ۵ دقیقه بالاتر از لبه پایینی ارزش (VAL) با جهش ۲.۴ برابری حجم معاملات.",
                timestamp = "۳ دقیقه پیش",
                severity = "CRITICAL"
            ),
            AlertNotification(
                id = "alert-2",
                symbol = "DOGEUSDT",
                title = "هشدار تراکم افراطی در بازار مشتقات",
                message = "ضریب فاندینگ ریت (Z-score) به +۲.۴۵ رسید در حالی که دلتای خرید اسپات رو به کاهش است.",
                timestamp = "۱۲ دقیقه پیش",
                severity = "WARNING"
            ),
            AlertNotification(
                id = "alert-3",
                symbol = "MACRO",
                title = "هشدار ریسک نوسان رویداد کلان اقتصادی",
                message = "انتشار داده‌های شاخص تورم هسته آمریکا (CPI) ظرف ۴۲ دقیقه آینده؛ احتمال افزایش اسپرد و اسلیپیج.",
                timestamp = "۱۸ دقیقه پیش",
                severity = "WARNING"
            )
        )
    }

    fun toggleWatchlistPin(symbol: String) {
        _watchlist.value = _watchlist.value.map {
            if (it.symbol == symbol) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun addToWatchlist(item: WatchlistItem) {
        if (_watchlist.value.none { it.symbol == item.symbol }) {
            _watchlist.value = _watchlist.value + item
        }
    }

    fun removeFromWatchlist(symbol: String) {
        _watchlist.value = _watchlist.value.filter { it.symbol != symbol }
    }

    fun addJournalEntry(entry: JournalEntry) {
        _journalEntries.value = listOf(entry) + _journalEntries.value
    }
}
