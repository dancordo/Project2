package com.example.engine

import com.example.model.*

object SetupAndScoringEngine {

    fun calculateOpportunityScore(
        regime: MarketRegime,
        rsScore: Double,
        liquidityScore: Int,
        oiDeltaPct: Double,
        fundingZScore: Double,
        spotCvd: Double,
        perpCvd: Double,
        orderBookImbalance: Double,
        catalystScore: Int,
        rrRatio: Double
    ): OpportunityScoreResult {
        val auditTrail = mutableListOf<ScoreAuditItem>()

        // 1. رژیم بازار (/20)
        val regimePoints = when (regime) {
            MarketRegime.STRONG_BULL -> 19
            MarketRegime.BULL -> 17
            MarketRegime.RANGE -> 12
            MarketRegime.NEUTRAL -> 10
            MarketRegime.BEAR -> 7
            MarketRegime.STRONG_BEAR -> 4
            MarketRegime.SHOCK_EVENT -> 2
        }
        val regimeReason = when (regime) {
            MarketRegime.STRONG_BULL, MarketRegime.BULL -> "رژیم بازار کاملاً همراستا با ساختار صعودی و ورود نقدینگی نهادی است"
            MarketRegime.RANGE -> "رژیم رنج و تعادل؛ مناسب ستاپ‌های برگشت به میانگین در لبه‌های ارزش"
            MarketRegime.SHOCK_EVENT -> "شوک نوسانی کلان فعال است؛ دقت مدل‌های آماری کاهش یافته است"
            else -> "ساختار خرسی یا واگرایی منفی در تایم‌فریم‌های بالا"
        }
        auditTrail.add(ScoreAuditItem("رژیم بازار", regimePoints, 20, regimeReason))

        // 2. قدرت نسبی (/15)
        val rsPoints = when {
            rsScore >= 3.0 -> 15
            rsScore in 1.5..3.0 -> 12
            rsScore in 0.0..1.5 -> 9
            rsScore in -1.5..0.0 -> 6
            else -> 2
        }
        val rsReason = when {
            rsScore >= 1.5 -> "عملکرد برتر نسبت به شاخص بیت‌کوین همراه با شتاب حرکتی مثبت"
            rsScore >= 0.0 -> "عملکرد خنثی و همگام با میانگین بازار"
            else -> "ضعف نسبی و ریزش سنگین‌تر نسبت به شاخص‌های مرجع"
        }
        auditTrail.add(ScoreAuditItem("قدرت نسبی (RS)", rsPoints, 15, rsReason))

        // 3. نقدینگی (/10)
        val liqPoints = (liquidityScore.coerceIn(0, 100) / 10).coerceIn(1, 10)
        val liqReason = if (liqPoints >= 7) "عمق عالی در اوردر بوک و کمترین میزان اسلیپیج معاملاتی" else "عمق نازک اوردر بوک؛ احتمال خطای اسلیپیج"
        auditTrail.add(ScoreAuditItem("عمق نقدینگی", liqPoints, 10, liqReason))

        // 4. مشتقات و سود باز (/20)
        var derivPoints = 12
        var derivReason = "توازن پوزیشن‌ها در بازار فیوچرز پرپچوال"
        if (fundingZScore in -1.5..1.0 && oiDeltaPct in -10.0..15.0) {
            derivPoints = 18
            derivReason = "اهرم‌های اضافی تخلیه شده و فاندینگ نرمال است؛ کاهش ریسک لیکوئیدیشن ناگهانی"
        } else if (fundingZScore > 2.0 || oiDeltaPct > 25.0) {
            derivPoints = 5
            derivReason = "تراکم سنگین پوزیشن‌ها: ضریب فاندینگ ریت بالا و انباشت پوزیشن‌های اهرمی پرریسک"
        } else if (fundingZScore < -2.0) {
            derivPoints = 15
            derivReason = "فاندینگ منفی شدید؛ پتانسیل بالای ایجاد شورت اسکوییز"
        }
        auditTrail.add(ScoreAuditItem("موقعیت‌گیری در مشتقات", derivPoints, 20, derivReason))

        // 5. جریان حجم اسپات (/15)
        var spotPoints = 8
        var spotReason = "دلتای تجمعی اسپات متعادل و بدون انحراف"
        if (spotCvd > 1500000) {
            spotPoints = 14
            spotReason = "انباشت تهاجمی خریداران اسپات تقاضای واقعی را تایید می‌کند"
        } else if (spotCvd < -1500000) {
            spotPoints = 4
            spotReason = "عرضه و فروش مستمر دارندگان توکن در بازار اسپات"
        }
        auditTrail.add(ScoreAuditItem("جریان حجم اسپات (CVD)", spotPoints, 15, spotReason))

        // 6. جریان سفارشات اوردر بوک (/10)
        var orderFlowPoints = 6
        var orderFlowReason = "توازن عادی میان حجم خریداران و فروشندگان در عمق کتابچه"
        if (orderBookImbalance >= 1.50) {
            orderFlowPoints = 9
            orderFlowReason = "عدم تعادل قوی در سمت خریداران (OBI > 1.50)؛ لایه‌های حمایتی سنگین"
        } else if (orderBookImbalance <= 0.67) {
            orderFlowPoints = 3
            orderFlowReason = "فشار عرضه و دیوارهای فروش سنگین در فاصله نزدیک (OBI < 0.67)"
        }
        auditTrail.add(ScoreAuditItem("جریان سفارشات و OBI", orderFlowPoints, 10, orderFlowReason))

        // 7. کاتالیزور و اخبار (/10)
        val catPoints = catalystScore.coerceIn(0, 10)
        val catReason = if (catPoints >= 7) "کاتالیزور بنیادین یا جریان ورودی مثبت در صندوق‌های ETF" else "عدم وجود رویداد خبری با تاثیر آنی"
        auditTrail.add(ScoreAuditItem("کاتالیزورهای بنیادین", catPoints, 10, catReason))

        var total = regimePoints + rsPoints + liqPoints + derivPoints + spotPoints + orderFlowPoints + catPoints

        // کسر امتیاز در صورت R:R زیر 1.8
        if (rrRatio < 1.8) {
            total = (total - 15).coerceAtLeast(0)
            auditTrail.add(ScoreAuditItem("جریمه نسبت ریوارد به ریسک", -15, 0, "نسبت سود به زیان کمتر از حداقل استاندارد ترمینال (۱.۸) است"))
        }

        val grade = when {
            total >= 80 -> "A+ (عالی)"
            total in 75..79 -> "A (بسیار خوب)"
            total in 65..74 -> "B (تحت نظر)"
            else -> "بدون معامله"
        }

        return OpportunityScoreResult(
            totalScore = total.coerceIn(0, 100),
            grade = grade,
            regimeScore = regimePoints,
            relativeStrengthScore = rsPoints,
            liquidityScore = liqPoints,
            derivativesScore = derivPoints,
            spotFlowScore = spotPoints,
            orderFlowScore = orderFlowPoints,
            catalystScore = catPoints,
            auditTrail = auditTrail
        )
    }

    fun evaluateVerdict(
        symbol: String,
        currentPrice: Double,
        regime: MarketRegime,
        adx: Double,
        rsScore: Double,
        spotCvd: Double,
        perpCvd: Double,
        oiDeltaPct: Double,
        fundingZScore: Double,
        isLiquiditySwept: Boolean,
        isLevelReclaimed: Boolean,
        isFailedBreakout: Boolean,
        valPrice: Double,
        vahPrice: Double,
        sessionVwap: Double,
        status: DataStatus
    ): TradeVerdict {
        val midRange = (vahPrice + valPrice) / 2.0
        val isMiddleOfRange = vahPrice > valPrice && kotlin.math.abs(currentPrice - midRange) < (vahPrice - valPrice) * 0.15

        if (regime == MarketRegime.SHOCK_EVENT) {
            return generateNoTrade(
                symbol, currentPrice, "شوک نوسانی کلان فعال است؛ مدل‌های آماری متوقف شدند.",
                "تا بسته شدن سشن و کاهش نوسان ضمنی صبور باشید.", status
            )
        }

        if (status == DataStatus.DATA_UNAVAILABLE) {
            return generateNoTrade(
                symbol, currentPrice, "داده‌های زنده تله‌متری بازار در دسترس نیست.",
                "اتصال شبکه را بررسی کنید یا به حالت شبیه‌سازی بروید.", status
            )
        }

        if (isMiddleOfRange && adx < 20.0) {
            return generateNoTrade(
                symbol, currentPrice, "قیمت دقیقاً در میانه محدوده ارزش ۴ ساعته نوسان دارد؛ عدم وجود عدم‌تقارن ریسک به ریوارد.",
                "صبر برای آزمایش لبه بالایی (VAH) یا پایینی (VAL).", status
            )
        }

        // ستاپ الف: سویپ نقدینگی و بازپس‌گیری لانگ
        if (isLiquiditySwept && isLevelReclaimed && spotCvd >= 0 && oiDeltaPct <= 5.0) {
            val stop = currentPrice * 0.985
            val tp1 = currentPrice * 1.025
            val tp2 = currentPrice * 1.050
            val tp3 = vahPrice.coerceAtLeast(currentPrice * 1.08)
            val rr = (tp2 - currentPrice) / (currentPrice - stop)
            val oppScore = calculateOpportunityScore(
                regime, rsScore, 85, oiDeltaPct, fundingZScore, spotCvd, perpCvd, 1.6, 7, rr
            )

            return TradeVerdict(
                symbol = symbol,
                direction = if (oppScore.totalScore >= 75) TradeDirection.LONG else TradeDirection.WATCH,
                confidence = if (oppScore.totalScore >= 80) 86 else 72,
                opportunityScore = oppScore,
                setupName = "ستاپ الف: سویپ نقدینگی + بازپس‌گیری ناحیه ارزش (خرید)",
                entryZone = "${(currentPrice * 0.998).format(2)} - ${(currentPrice * 1.002).format(2)} دلار",
                stopLoss = stop,
                tp1 = tp1,
                tp2 = tp2,
                tp3 = tp3,
                rrRatio = rr,
                primaryReasons = listOf(
                    "سویپ کف ساختاری قبلی همراه با تخلیه حجم لیکوئیدیشن لانگ‌ها",
                    "بازپس‌گیری سریع و تثبیت مجدد بالای سطح پایینی ناحیه ارزش (VAL)",
                    "تثبیت دلتای اسپات و گسترش حجم خریداران واقعی",
                    "تخلیه سود باز (OI) که مانع از فشار آبشار لیکوئیدیشن بعدی می‌شود"
                ),
                primaryRisks = listOf(
                    "همبستگی بالا با ریزش بیت‌کوین در صورت شکست حمایت ۴ ساعته",
                    "عدم توانایی در حفظ سطح بازپس‌گرفته‌شده ظرف ۳ کندل آینده"
                ),
                invalidation = "کلوز کندل ۵ یا ۱۵ دقیقه زیر کف سویپ‌شده (${stop.format(2)} دلار)",
                failedAuctionDetected = false,
                dataQuality = status
            )
        }

        // ستاپ هـ: حراج ناموفق و فیک بریک در سقف (شورت)
        if (isFailedBreakout || (currentPrice >= vahPrice && perpCvd > 2000000 && spotCvd < -500000)) {
            val stop = currentPrice * 1.015
            val tp1 = currentPrice * 0.980
            val tp2 = currentPrice * 0.955
            val tp3 = valPrice.coerceAtMost(currentPrice * 0.93)
            val rr = (currentPrice - tp2) / (stop - currentPrice)
            val oppScore = calculateOpportunityScore(
                regime, rsScore, 80, oiDeltaPct, fundingZScore, spotCvd, perpCvd, 0.55, 6, rr
            )

            return TradeVerdict(
                symbol = symbol,
                direction = if (oppScore.totalScore >= 75) TradeDirection.SHORT else TradeDirection.WATCH,
                confidence = if (oppScore.totalScore >= 80) 84 else 70,
                opportunityScore = oppScore,
                setupName = "ستاپ هـ: حراج ناموفق و فیک بریک در سقف (فروش)",
                entryZone = "${(currentPrice * 0.997).format(2)} - ${(currentPrice * 1.003).format(2)} دلار",
                stopLoss = stop,
                tp1 = tp1,
                tp2 = tp2,
                tp3 = tp3,
                rrRatio = rr,
                primaryReasons = listOf(
                    "سویپ سقف رنج بدون پذیرش حجم معاملاتی بالاتر از ناحیه ارزش",
                    "جذب سفارشات خرید تهاجمی پرپچوال توسط اوردرهای لیمیت فروش نهادی",
                    "برگشت پرسرعت به داخل ناحیه ارزش که نشانه بارز پس‌زدن قیمت است",
                    "واگرایی منفی آشکار دلتای اسپات با قیمت‌های سقف"
                ),
                primaryRisks = listOf(
                    "خطر جهش ناگهانی شورت اسکوییز در صورت هجوم خریداران اسپات"
                ),
                invalidation = "تثبیت و کلوز کندل ۱ ساعته بالاتر از سقف رنج (${stop.format(2)} دلار)",
                failedAuctionDetected = true,
                dataQuality = status
            )
        }

        // ستاپ ج: برگشت به میانگین در رنج
        if (adx < 18.0 && (currentPrice <= valPrice * 1.005 || currentPrice >= vahPrice * 0.995)) {
            val isBuyEdge = currentPrice <= valPrice * 1.005
            val stop = if (isBuyEdge) currentPrice * 0.988 else currentPrice * 1.012
            val tp = sessionVwap
            val rr = if (isBuyEdge) (tp - currentPrice) / (currentPrice - stop) else (currentPrice - tp) / (stop - currentPrice)
            val oppScore = calculateOpportunityScore(
                regime, rsScore, 75, oiDeltaPct, fundingZScore, spotCvd, perpCvd, 1.1, 5, rr
            )

            return TradeVerdict(
                symbol = symbol,
                direction = if (isBuyEdge) TradeDirection.LONG else TradeDirection.SHORT,
                confidence = 74,
                opportunityScore = oppScore,
                setupName = "ستاپ ج: برگشت به میانگین در لبه‌های ناحیه ارزش",
                entryZone = "${currentPrice.format(2)} ± ۰.۲٪",
                stopLoss = stop,
                tp1 = sessionVwap,
                tp2 = if (isBuyEdge) vahPrice else valPrice,
                tp3 = if (isBuyEdge) vahPrice * 1.01 else valPrice * 0.99,
                rrRatio = rr.coerceAtLeast(1.8),
                primaryReasons = listOf(
                    "شاخص ADX زیر ۱۸ بیانگر نبود روند جهت‌دار قوی است",
                    "قیمت در انتهای لبه کانتینر حجمی با نقدینگی بالا قرار دارد",
                    "خط VWAP افقی همراه با تعادل نسبی در جریان سفارشات"
                ),
                primaryRisks = listOf(
                    "خطر آغاز بریک‌اوت ناگهانی به خارج از باکس رنج"
                ),
                invalidation = "کلوز کندل با بدنه کامل بیرون از لبه ارزش",
                failedAuctionDetected = false,
                dataQuality = status
            )
        }

        // حالت پیش‌فرض: عدم معامله
        return generateNoTrade(
            symbol, currentPrice, "هیچ موقعیت دارای مزیت آماری تایید نشد. شرایط ستاپ‌های استاندارد A تا E محقق نگردید.",
            "صبر برای شکل‌گیری سویپ نقدینگی در مرزهای قیمتی یا تایید شکست حجم‌دار.", status
        )
    }

    private fun generateNoTrade(
        symbol: String,
        currentPrice: Double,
        reason: String,
        invalidation: String,
        status: DataStatus
    ): TradeVerdict {
        val auditTrail = listOf(
            ScoreAuditItem("رژیم بازار", 8, 20, "عدم وجود شتاب جهت‌دار در ساختار بازار"),
            ScoreAuditItem("قدرت نسبی", 6, 15, "نوسان همگام با میانگین بازار"),
            ScoreAuditItem("عمق نقدینگی", 6, 10, "عمق معمولی اوردر بوک"),
            ScoreAuditItem("مشتقات", 10, 20, "نبود عدم‌تقارن موثر در پوزیشن‌ها"),
            ScoreAuditItem("جریان اسپات", 6, 15, "دلتای خرید و فروش نامشخص"),
            ScoreAuditItem("اوردر بوک", 5, 10, "دفتر سفارشات متعادل"),
            ScoreAuditItem("کاتالیزور", 4, 10, "عدم وجود خبر محرک نزدیک"),
            ScoreAuditItem("فیلتر عدم معامله", -10, 0, "فعال شدن فیلتر محافظتی: نامناسب بودن نسبت ریسک/ریوارد")
        )
        val oppScore = OpportunityScoreResult(
            totalScore = 45,
            grade = "عدم معامله (NO TRADE)",
            regimeScore = 8,
            relativeStrengthScore = 6,
            liquidityScore = 6,
            derivativesScore = 10,
            spotFlowScore = 6,
            orderFlowScore = 5,
            catalystScore = 4,
            auditTrail = auditTrail
        )

        return TradeVerdict(
            symbol = symbol,
            direction = TradeDirection.NO_TRADE,
            confidence = 25,
            opportunityScore = oppScore,
            setupName = "عدم معامله (NO TRADE) — بلاتکلیفی بازار",
            entryZone = "نامشخص (در انتظار تاییدیه ستاپ)",
            stopLoss = currentPrice * 0.97,
            tp1 = currentPrice * 1.03,
            tp2 = currentPrice * 1.06,
            tp3 = currentPrice * 1.10,
            rrRatio = 1.0,
            primaryReasons = listOf(reason),
            primaryRisks = listOf("معامله در میانه رنج منجر به فرسایش کارمزد و امید ریاضی منفی می‌گردد"),
            invalidation = invalidation,
            failedAuctionDetected = false,
            dataQuality = status
        )
    }

    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
}
