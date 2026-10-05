package com.example.ai

import com.example.model.*
import com.example.provider.AIProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class InstitutionalAiProvider(
    private var apiKey: String = "",
    private var modelName: String = "gemini-2.5-flash",
    private var temperature: Double = 0.2
) : AIProvider {

    override val providerName: String
        get() = if (apiKey.isNotBlank()) "هوش مصنوعی جمینای ($modelName)" else "موتور تحلیل نهادی ترمینال (آفلاین)"

    fun updateConfig(key: String, model: String, temp: Double) {
        apiKey = key
        modelName = model
        temperature = temp
    }

    override suspend fun generateAnalysis(
        symbol: String,
        price: Double,
        regime: MarketRegime,
        verdict: TradeVerdict,
        derivatives: DerivativesMetrics,
        indicators: TechnicalIndicators,
        dataStatus: DataStatus
    ): String = withContext(Dispatchers.IO) {
        if (dataStatus == DataStatus.DATA_UNAVAILABLE) {
            return@withContext """
                [وضعیت تله‌متری: داده در دسترس نیست]
                ارتباط زنده با سرورهای صرافی موقتاً قطع است.
                بر اساس پروتکل سخت‌گیرانه نهادی ترمینال، از ساخت داده‌ها و قیمت‌های غیرواقعی خودداری می‌شود.
                توصیه: عدم معامله (NO TRADE) — اتصال شبکه را بررسی نمایید یا به حالت شبیه‌سازی تغییر وضعیت دهید.
            """.trimIndent()
        }

        val prompt = buildStructuredPrompt(symbol, price, regime, verdict, derivatives, indicators, dataStatus)

        if (apiKey.isNotBlank()) {
            try {
                return@withContext callGeminiApi(prompt)
            } catch (e: Exception) {
                return@withContext generateDeterministicInstitutionalAnalysis(symbol, price, regime, verdict, derivatives, indicators, dataStatus, "پیام: فراخوانی API جمینای با خطا مواجه شد (${e.message}). تحلیل به صورت سیستماتیک توسط موتور ترمینال تولید شد.")
            }
        } else {
            return@withContext generateDeterministicInstitutionalAnalysis(symbol, price, regime, verdict, derivatives, indicators, dataStatus, null)
        }
    }

    private fun buildStructuredPrompt(
        symbol: String,
        price: Double,
        regime: MarketRegime,
        verdict: TradeVerdict,
        derivatives: DerivativesMetrics,
        indicators: TechnicalIndicators,
        dataStatus: DataStatus
    ): String {
        return """
            شما تحلیلگر ارشد و تریدر نهادی بازارهای فیوچرز پرپچوال کریپتو هستید.
            تحلیل شما باید دقیق، حرفه‌ای، به زبان فارسی و با ادبیات اصیل بازارهای مالی نوشته شود.
            تنها بر اساس داده‌های تله‌متری واقعی زیر تحلیل کنید و از پیش‌بینی‌های قطعی یا جعل قیمت جداً خودداری فرمایید.
            اگر شواهد متعارض است، صراحتاً «عدم معامله (NO TRADE)» اعلام کنید.
            
            [مشخصات تله‌متری نماد]
            نماد: $symbol | قیمت زنده: $price دلار | وضعیت داده: ${dataStatus.label}
            رژیم کلی بازار: ${regime.label}
            سیگنال ساختاری ترمینال: ${verdict.direction.label} | درصد اطمینان: ${verdict.confidence}٪ | امتیاز فرصت: ${verdict.opportunityScore.totalScore} از ۱۰۰ (${verdict.opportunityScore.grade})
            بازار مشتقات و سود باز:
            - سود باز (OI): $${derivatives.openInterest} (تغییر ۲۴ ساعته: ${derivatives.oiChange24hPct}٪)
            - نرخ فاندینگ ۸ ساعته: ${(derivatives.fundingRate8h * 100)}٪ | ضریب فاندینگ (Z-score): ${derivatives.fundingZScore} (${derivatives.fundingCrowdingStatus})
            - وضعیت مبنای بازده سالانه (۳ ماهه): ${derivatives.basisAnnualized3M}٪ (${derivatives.termStructure.label})
            - وضعیت لیکوئیدیشن‌ها: ${derivatives.liquidationFlushType.label} (شدت: ${derivatives.liquidationIntensity})
            - جریان دلتای تجمعی (CVD): اسپات = $${derivatives.spotCvd} | پرپچوال = $${derivatives.perpCvd}
            - یادداشت تطابق CVD: ${derivatives.cvdDivergenceNote}
            شاخص‌های تکنیکال و کانتینر ارزش:
            - میانگین‌های متحرک: EMA20=$${indicators.ema20} | EMA50=$${indicators.ema50} | EMA200=$${indicators.ema200}
            - میانگین وزنی حجمی سشن (VWAP): $${indicators.sessionVwap} | AVWAP هفتگی: $${indicators.weeklyAvwap}
            - شاخص‌های نوسان و شتاب: ATR(14)=$${indicators.atr14} | ADX(14)=${indicators.adx14} | RSI(14)=${indicators.rsi14}
            - پروفایل حجمی (Volume Profile): POC=$${indicators.volumeProfile.poc} | VAH=$${indicators.volumeProfile.vah} | VAL=$${indicators.volumeProfile.valPrice}
            
            لطفاً پاسخ خود را دقیقاً در این ۸ بخش ساختاریافته به فارسی ارائه دهید:
            ۱. داده‌های مشاهده‌شده (OBSERVED DATA)
            ۲. متریک‌های مشتق‌شده و سود باز (DERIVED METRICS)
            ۳. تفسیر جریان نقدینگی و واگرایی CVD
            ۴. رژیم و فرضیه معاملاتی
            ۵. پلن معاملاتی دقیق (جهت، محدوده ورود، حد ضرر، تارگت ۱، ۲، ۳ و نسبت R:R)
            ۶. معیارهای ابطال دقیق تحلیل (INVALIDATION)
            ۷. ریسک‌های سیستماتیک و نقاط تاریک داده‌ها
            ۸. سناریوی گاوها در برابر سناریوی خرس‌ها
        """.trimIndent()
    }

    private fun callGeminiApi(prompt: String): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.connectTimeout = 8000
        conn.readTimeout = 12000
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", temperature)
                put("maxOutputTokens", 1200)
            })
        }

        conn.outputStream.use { os ->
            os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
        }

        val code = conn.responseCode
        if (code != 200) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            throw RuntimeException("Gemini API error ($code): $err")
        }

        val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(responseText)
        val candidates = root.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "پاسخی از هوش مصنوعی دریافت نشد")
            }
        }
        return "عدم پاسخ کاندید معتبر از جمینای"
    }

    private fun generateDeterministicInstitutionalAnalysis(
        symbol: String,
        price: Double,
        regime: MarketRegime,
        verdict: TradeVerdict,
        derivatives: DerivativesMetrics,
        indicators: TechnicalIndicators,
        dataStatus: DataStatus,
        apiNotice: String?
    ): String {
        val noticeHeader = if (apiNotice != null) "[$apiNotice]\n\n" else ""
        return """
${noticeHeader}=== گزارش تحلیلی نهادی ترمینال فیوچرز ادج ===
نماد: $symbol | وضعیت تله‌متری: ${dataStatus.label}
قیمت واقعی: ${"%.2f".format(price)}$ | امتیاز فرصت: ${verdict.opportunityScore.totalScore} از ۱۰۰ (${verdict.opportunityScore.grade})

۱. داده‌های مشاهده‌شده (OBSERVED DATA)
• قیمت زنده بازار: ${"%.2f".format(price)} دلار
• نرخ فاندینگ ریت ۸ ساعته: ${"%.4f".format(derivatives.fundingRate8h * 100)}٪ در صرافی‌های اصلی
• سود باز پرپچوال (Open Interest): ${"%.2f".format(derivatives.openInterest / 1e9)} میلیارد دلار (تغییر ۲۴ساعته: ${derivatives.oiChange24hPct}٪)
• وضعیت نقدینگی اجباری: ${derivatives.liquidationFlushType.label} (مجموع حجم لیکوئیدیشن: ${"%.1f".format((derivatives.longLiquidationVol24h + derivatives.shortLiquidationVol24h) / 1e6)} میلیون دلار)
• لایه‌های پروفایل حجم: نقطه کنترل (POC) در ${"%.2f".format(indicators.volumeProfile.poc)}$ | سقف ارزش (VAH) در ${"%.2f".format(indicators.volumeProfile.vah)}$ | کف ارزش (VAL) در ${"%.2f".format(indicators.volumeProfile.valPrice)}$

۲. متریک‌های مشتق‌شده و سود باز
• ضریب انحراف معیار فاندینگ (Z-Score): ${"%.2f".format(derivatives.fundingZScore)} (${derivatives.fundingCrowdingStatus})
• دلتای تجمعی حجم: اسپات ${"%.1f".format(derivatives.spotCvd / 1e6)}M$ در برابر پرپچوال ${"%.1f".format(derivatives.perpCvd / 1e6)}M$
• وضعیت ساختار زمانی بازدهی ۳ ماهه: ${"%.2f".format(derivatives.basisAnnualized3M)}٪ (${derivatives.termStructure.label})
• شاخص‌های مومنتوم: ADX=${"%.1f".format(indicators.adx14)} | RSI=${"%.1f".format(indicators.rsi14)} | نوسان واقعی (ATR)=${"%.2f".format(indicators.atr14)}$

۳. تفسیر جریان نقدینگی و واگرایی CVD
• ${derivatives.cvdDivergenceNote}
• موقعیت مکانی: قیمت نسبت به میانگین وزنی سشن (VWAP=${"%.2f".format(indicators.sessionVwap)}$) و AVWAP هفتگی (${"%.2f".format(indicators.weeklyAvwap)}$) ارزیابی می‌گردد.
• ساختار بازار: ${if (verdict.failedAuctionDetected) "حراج ناموفق و فیک بریک در سقف با پس‌زدن سریع حجم تایید شده است." else "ساختار پله‌ای سقف‌ها و کف‌ها با یکپارچگی بالا حفظ شده است."}

۴. فرضیه و رژیم معاملاتی
• وضعیت کلان: ${regime.label} — ${regime.description}
• ستاپ تحت نظر: ${verdict.setupName}
• سوگیری جهتی: ${verdict.direction.label} (درجه اطمینان: ${verdict.confidence}٪)

۵. پلن معاملاتی دقیق (TRADE PLAN)
• ستاپ: ${verdict.setupName}
• محدوده بهینه ورود: ${verdict.entryZone}
• حد ضرر محافظتی: ${"%.2f".format(verdict.stopLoss)} دلار
• تارگت اول (کاهش ۵۰٪ حجم ریسک): ${"%.2f".format(verdict.tp1)} دلار
• تارگت دوم (هدف اصلی ارزش): ${"%.2f".format(verdict.tp2)} دلار
• تارگت سوم (استخر نقدینگی مخالف): ${"%.2f".format(verdict.tp3)} دلار
• نسبت ریوارد به ریسک مورد انتظار: ${"%.2f".format(verdict.rrRatio)} به ۱

۶. معیارهای ابطال تحلیل (INVALIDATION)
• شرط ابطال: ${verdict.invalidation}
• ایست زمانی: سپری شدن ۱۰ کندل بدون رسیدن به حداقل سود 0.5R نشانه درجا زدن ترید بوده و خروج دستی توصیه می‌شود.

۷. ریسک‌های سیستماتیک و داده‌های نامعلوم
• ریسک‌های اصلی: ${verdict.primaryRisks.joinToString("؛ ")}
• داده‌های نامعلوم: سفارشات پنهان (Iceberg) سازمانی در این تله‌متری محاسبه نشده‌اند.

۸. سناریوی گاوها در برابر خرس‌ها
• سناریوی صعودی: تداوم انباشت اسپات منجر به جاروب کردن لایه‌های عرضه بالای سر و حمله به نقدینگی‌های بعدی می‌شود.
• سناریوی نزولی: شکست حمایت کلیدی یا خبرهای غیرمنتظره کلان می‌تواند منجر به آبشار لیکوئیدیشن به زیر کف ساختار گردد.
        """.trimIndent()
    }

    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
}
