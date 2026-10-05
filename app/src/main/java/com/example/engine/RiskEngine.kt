package com.example.engine

import kotlin.math.abs

data class RiskCalculationInputs(
    val accountEquity: Double = 20000.0,
    val riskPercent: Double = 0.40, // 0.40%
    val entryPrice: Double = 100000.0,
    val stopPrice: Double = 99000.0,
    val tp1Price: Double = 101500.0,
    val tp2Price: Double = 102500.0,
    val tp3Price: Double = 104000.0,
    val feeTakerPct: Double = 0.05, // 0.05%
    val fundingDragPct: Double = 0.01 // 0.01%
)

data class RiskCalculationOutput(
    val riskAmountDollars: Double,
    val stopDistancePct: Double,
    val stopDistanceDollars: Double,
    val positionNotional: Double,
    val marginRequired: Double,
    val effectiveLeverage: Double,
    val tp1Reward: Double,
    val tp2Reward: Double,
    val tp3Reward: Double,
    val rrRatio: Double,
    val estimatedTakerFee: Double,
    val estimatedFundingDrag: Double,
    val netEstimatedPnlAtTp2: Double,
    val formulaExplanation: String
)

data class PortfolioRiskSummary(
    val grossExposure: Double,
    val longExposure: Double,
    val shortExposure: Double,
    val netExposure: Double,
    val correlatedRiskPercent: Double,
    val isCorrelatedRiskElevated: Boolean,
    val dailyPnlR: Double,
    val isKillSwitchTriggered: Boolean,
    val warningMessage: String?
)

object RiskEngine {

    fun calculateRisk(inputs: RiskCalculationInputs, maxLeverage: Double = 10.0): RiskCalculationOutput {
        val riskDollars = inputs.accountEquity * (inputs.riskPercent / 100.0)
        val stopDistancePrice = abs(inputs.entryPrice - inputs.stopPrice)
        val stopDistancePct = if (inputs.entryPrice > 0) (stopDistancePrice / inputs.entryPrice) * 100.0 else 1.0

        // Formula: Position Notional = (Account Equity * Risk%) / Stop Distance %
        val positionNotional = if (stopDistancePct > 0) {
            riskDollars / (stopDistancePct / 100.0)
        } else 0.0

        val margin = if (maxLeverage > 0) positionNotional / maxLeverage else positionNotional
        val effectiveLeverage = if (inputs.accountEquity > 0) positionNotional / inputs.accountEquity else 0.0

        val isLong = inputs.stopPrice < inputs.entryPrice
        val tp1Dist = if (isLong) inputs.tp1Price - inputs.entryPrice else inputs.entryPrice - inputs.tp1Price
        val tp2Dist = if (isLong) inputs.tp2Price - inputs.entryPrice else inputs.entryPrice - inputs.tp2Price
        val tp3Dist = if (isLong) inputs.tp3Price - inputs.entryPrice else inputs.entryPrice - inputs.tp3Price

        val tp1Pct = if (inputs.entryPrice > 0) (tp1Dist / inputs.entryPrice) else 0.0
        val tp2Pct = if (inputs.entryPrice > 0) (tp2Dist / inputs.entryPrice) else 0.0
        val tp3Pct = if (inputs.entryPrice > 0) (tp3Dist / inputs.entryPrice) else 0.0

        val tp1Reward = positionNotional * tp1Pct
        val tp2Reward = positionNotional * tp2Pct
        val tp3Reward = positionNotional * tp3Pct

        val rr = if (stopDistancePrice > 0) abs(tp2Dist) / stopDistancePrice else 0.0

        val estimatedFees = positionNotional * 2.0 * (inputs.feeTakerPct / 100.0) // ورود + خروج
        val estimatedFunding = positionNotional * (inputs.fundingDragPct / 100.0)
        val netTp2 = tp2Reward - estimatedFees - estimatedFunding

        val formulaText = "فرمول ریاضی دقیق محاسبه حجم پوزیشن:\n" +
                "• مبلغ ریسک مجاز = ${inputs.accountEquity.format(0)}$ × ${inputs.riskPercent}٪ = ${riskDollars.format(2)}$\n" +
                "• فاصله تا حد ضرر = |${inputs.entryPrice.format(0)}$ - ${inputs.stopPrice.format(0)}$| = ${stopDistancePct.format(2)}٪\n" +
                "• ارزش اسمی پوزیشن = ${riskDollars.format(2)}$ ÷ ${(stopDistancePct / 100.0).format(4)} = ${positionNotional.format(2)}$\n" +
                "• اهرم موثر مورد نیاز = ${positionNotional.format(0)}$ ÷ ${inputs.accountEquity.format(0)}$ = ${effectiveLeverage.format(2)}x"

        return RiskCalculationOutput(
            riskAmountDollars = riskDollars,
            stopDistancePct = stopDistancePct,
            stopDistanceDollars = stopDistancePrice,
            positionNotional = positionNotional,
            marginRequired = margin,
            effectiveLeverage = effectiveLeverage,
            tp1Reward = tp1Reward,
            tp2Reward = tp2Reward,
            tp3Reward = tp3Reward,
            rrRatio = rr,
            estimatedTakerFee = estimatedFees,
            estimatedFundingDrag = estimatedFunding,
            netEstimatedPnlAtTp2 = netTp2,
            formulaExplanation = formulaText
        )
    }

    fun evaluatePortfolioHeat(
        accountEquity: Double,
        positions: List<Pair<String, Double>>,
        dailyPnlR: Double,
        killSwitchLimitR: Double = -3.0,
        killSwitchEquityPct: Double = 1.5
    ): PortfolioRiskSummary {
        var longExp = 0.0
        var shortExp = 0.0
        var cryptoClusterExp = 0.0

        for ((sym, notional) in positions) {
            if (notional >= 0) longExp += notional else shortExp += abs(notional)
            if (sym in listOf("BTC", "ETH", "SOL", "AVAX") && notional > 0) {
                cryptoClusterExp += notional
            }
        }
        val grossExp = longExp + shortExp
        val netExp = longExp - shortExp

        val correlatedRiskPct = if (accountEquity > 0) (cryptoClusterExp / accountEquity) * 100.0 else 0.0
        val isCorrelatedElevated = correlatedRiskPct > 100.0

        val isKillSwitchTriggered = dailyPnlR <= killSwitchLimitR

        val warning = when {
            isKillSwitchTriggered -> "سقف زیان روزانه فعال شد (${dailyPnlR.format(1)}R). ترمینال به حالت نظارت تغییر یافت."
            isCorrelatedElevated -> "هشدار ریسک همبستگی: حجم لانگ در کلاستر BTC/ETH/SOL بیش از حد ایمن است."
            else -> null
        }

        return PortfolioRiskSummary(
            grossExposure = grossExp,
            longExposure = longExp,
            shortExposure = shortExp,
            netExposure = netExp,
            correlatedRiskPercent = correlatedRiskPct,
            isCorrelatedRiskElevated = isCorrelatedElevated,
            dailyPnlR = dailyPnlR,
            isKillSwitchTriggered = isKillSwitchTriggered,
            warningMessage = warning
        )
    }

    fun calculateExpectancy(
        winCount: Int,
        lossCount: Int,
        avgWinR: Double,
        avgLossR: Double
    ): Double {
        val total = winCount + lossCount
        if (total == 0) return 0.0
        val pWin = winCount.toDouble() / total
        val pLoss = lossCount.toDouble() / total
        return (pWin * avgWinR) - (pLoss * abs(avgLossR))
    }

    fun checkTimeStop(barsInTrade: Int, currentMfeR: Double, maxBarsThreshold: Int = 10): Pair<Boolean, String> {
        return if (barsInTrade >= maxBarsThreshold && currentMfeR < 0.5) {
            Pair(true, "ایست زمانی: $barsInTrade کندل سپری شده و پیشروی زیر 0.5R است. توصیه به خروج در نقطه سربه‌سر.")
        } else {
            Pair(false, "پیشرفت زمانی ترید درون محدوده مجاز ($barsInTrade از $maxBarsThreshold کندل).")
        }
    }

    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
}
