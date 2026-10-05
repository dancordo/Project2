package com.example.engine

import com.example.model.BasisTerm
import com.example.model.CrossExchangeFunding
import kotlin.math.abs
import kotlin.math.sqrt

object DerivativesEngine {

    enum class OiQuadrant(val title: String, val interpretation: String) {
        PRICE_UP_OI_UP("Price ↑ + OI ↑", "Fresh aggressive positioning / trend participation candidate"),
        PRICE_UP_OI_DOWN("Price ↑ + OI ↓", "Short covering / deleveraging rally candidate"),
        PRICE_DOWN_OI_UP("Price ↓ + OI ↑", "Fresh downside positioning / trapped long candidate"),
        PRICE_DOWN_OI_DOWN("Price ↓ + OI ↓", "Long liquidation / deleveraging cascade candidate")
    }

    fun interpretOiPrice(priceChangePct: Double, oiChangePct: Double): OiQuadrant {
        return when {
            priceChangePct >= 0 && oiChangePct >= 0 -> OiQuadrant.PRICE_UP_OI_UP
            priceChangePct >= 0 && oiChangePct < 0 -> OiQuadrant.PRICE_UP_OI_DOWN
            priceChangePct < 0 && oiChangePct >= 0 -> OiQuadrant.PRICE_DOWN_OI_UP
            else -> OiQuadrant.PRICE_DOWN_OI_DOWN
        }
    }

    fun calculateZScore(current: Double, history: List<Double>): Double {
        if (history.size < 2) return 0.0
        val mean = history.average()
        val variance = history.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        if (stdDev == 0.0) return 0.0
        return (current - mean) / stdDev
    }

    fun interpretFundingCrowding(zScore: Double): String {
        return when {
            zScore < -2.0 -> "Extreme negative / short crowded"
            zScore in -2.0..-1.0 -> "Bearish crowding"
            zScore in -1.0..1.0 -> "Normal distribution"
            zScore in 1.0..2.0 -> "Bullish crowding"
            else -> "Extreme long crowding / flush risk"
        }
    }

    fun calculateAnnualizedBasis(futuresPrice: Double, spotPrice: Double, daysToExpiry: Double): Double {
        if (spotPrice <= 0 || daysToExpiry <= 0) return 0.0
        return ((futuresPrice - spotPrice) / spotPrice) * (365.0 / daysToExpiry) * 100.0
    }

    fun classifyBasisTerm(annualizedBasisPct: Double): BasisTerm {
        return when {
            annualizedBasisPct > 4.0 -> BasisTerm.CONTANGO
            annualizedBasisPct < -1.0 -> BasisTerm.BACKWARDATION
            else -> BasisTerm.NEUTRAL
        }
    }

    fun checkCrossExchangeDivergence(rates: List<CrossExchangeFunding>, thresholdSpread: Double = 0.02): Pair<Boolean, String> {
        if (rates.size < 2) return Pair(false, "Single provider rate available")
        val maxRate = rates.maxOf { it.rate }
        val minRate = rates.minOf { it.rate }
        val spread = maxRate - minRate
        return if (spread >= thresholdSpread) {
            Pair(true, "Cross-exchange funding divergence detected: ${(spread * 100).format(3)}% spread between ${rates.minByOrNull { it.rate }?.exchange} and ${rates.maxByOrNull { it.rate }?.exchange}")
        } else {
            Pair(false, "Cross-exchange funding aligned within normal bounds")
        }
    }

    fun analyzeCvdDivergence(priceChangePct: Double, spotCvd: Double, perpCvd: Double, oiDeltaPct: Double): String {
        return when {
            priceChangePct > 0 && spotCvd > 0 && perpCvd > 0 && oiDeltaPct > 0 ->
                "High-quality bullish participation candidate (Spot & Perp aligned with expansion)"
            priceChangePct > 0 && spotCvd <= 0 && perpCvd > 2000000 && oiDeltaPct > 5.0 ->
                "Leverage-driven move / crowding risk: perp CVD and OI spiking without spot backing"
            priceChangePct < 0 && spotCvd > 0 && perpCvd < 0 ->
                "Potential absorption / divergence candidate: spot accumulation absorbing perp sell-off"
            priceChangePct < 0 && spotCvd < 0 && perpCvd < 0 ->
                "Broad distribution: synchronized spot and perp selling"
            else ->
                "Balanced order flow: no prominent CVD divergence detected"
        }
    }

    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
}
