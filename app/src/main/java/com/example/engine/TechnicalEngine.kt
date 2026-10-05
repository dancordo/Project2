package com.example.engine

import com.example.model.Candle
import com.example.model.StructureMarker
import com.example.model.StructureType
import com.example.model.TechnicalIndicators
import com.example.model.Timeframe
import com.example.model.VolumeProfileLevel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TechnicalEngine {

    fun calculateEma(candles: List<Candle>, period: Int): Double {
        if (candles.isEmpty()) return 0.0
        val k = 2.0 / (period + 1.0)
        var ema = candles.first().close
        for (i in 1 until candles.size) {
            ema = candles[i].close * k + ema * (1.0 - k)
        }
        return ema
    }

    fun calculateAtr(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < 2) return 0.0
        val trs = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val high = candles[i].high
            val low = candles[i].low
            val prevClose = candles[i - 1].close
            val tr = max(high - low, max(abs(high - prevClose), abs(low - prevClose)))
            trs.add(tr)
        }
        if (trs.isEmpty()) return 0.0
        val lookback = min(period, trs.size)
        return trs.takeLast(lookback).average()
    }

    fun calculateRsi(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < period + 1) return 50.0
        var gains = 0.0
        var losses = 0.0
        for (i in 1..period) {
            val change = candles[i].close - candles[i - 1].close
            if (change > 0) gains += change else losses += abs(change)
        }
        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in period + 1 until candles.size) {
            val change = candles[i].close - candles[i - 1].close
            if (change > 0) {
                avgGain = (avgGain * (period - 1) + change) / period
                avgLoss = (avgLoss * (period - 1)) / period
            } else {
                avgGain = (avgGain * (period - 1)) / period
                avgLoss = (avgLoss * (period - 1) + abs(change)) / period
            }
        }
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    fun calculateAdx(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < period * 2) return 22.0
        val trList = mutableListOf<Double>()
        val plusDmList = mutableListOf<Double>()
        val minusDmList = mutableListOf<Double>()

        for (i in 1 until candles.size) {
            val h = candles[i].high
            val l = candles[i].low
            val ph = candles[i - 1].high
            val pl = candles[i - 1].low
            val pc = candles[i - 1].close

            val tr = max(h - l, max(abs(h - pc), abs(l - pc)))
            val upMove = h - ph
            val downMove = pl - l

            val plusDm = if (upMove > downMove && upMove > 0) upMove else 0.0
            val minusDm = if (downMove > upMove && downMove > 0) downMove else 0.0

            trList.add(tr)
            plusDmList.add(plusDm)
            minusDmList.add(minusDm)
        }

        val dxList = mutableListOf<Double>()
        for (i in period - 1 until trList.size) {
            val sumTr = trList.subList(i - period + 1, i + 1).sum()
            val sumPlus = plusDmList.subList(i - period + 1, i + 1).sum()
            val sumMinus = minusDmList.subList(i - period + 1, i + 1).sum()

            val plusDi = if (sumTr > 0) 100.0 * (sumPlus / sumTr) else 0.0
            val minusDi = if (sumTr > 0) 100.0 * (sumMinus / sumTr) else 0.0
            val diSum = plusDi + minusDi
            val dx = if (diSum > 0) 100.0 * abs(plusDi - minusDi) / diSum else 0.0
            dxList.add(dx)
        }
        return if (dxList.isNotEmpty()) dxList.takeLast(period).average() else 20.0
    }

    fun calculateSessionVwap(candles: List<Candle>): Double {
        if (candles.isEmpty()) return 0.0
        var cumulativeVolume = 0.0
        var cumulativeVp = 0.0
        for (c in candles) {
            val typicalPrice = (c.high + c.low + c.close) / 3.0
            cumulativeVp += typicalPrice * c.volume
            cumulativeVolume += c.volume
        }
        return if (cumulativeVolume > 0) cumulativeVp / cumulativeVolume else candles.last().close
    }

    fun calculateVolumeProfile(candles: List<Candle>, numBuckets: Int = 24): VolumeProfileLevel {
        if (candles.isEmpty()) {
            return VolumeProfileLevel(0.0, 0.0, 0.0, emptyList(), emptyList())
        }
        val minPrice = candles.minOf { it.low }
        val maxPrice = candles.maxOf { it.high }
        if (maxPrice <= minPrice) {
            val p = candles.first().close
            return VolumeProfileLevel(p, p * 1.01, p * 0.99, emptyList(), emptyList())
        }
        val bucketStep = (maxPrice - minPrice) / numBuckets
        val bucketVolumes = DoubleArray(numBuckets)

        for (c in candles) {
            val typical = (c.high + c.low + c.close) / 3.0
            val bucketIdx = min(((typical - minPrice) / bucketStep).toInt(), numBuckets - 1)
            bucketVolumes[bucketIdx] += c.volume
        }

        var maxVolIdx = 0
        var maxVol = 0.0
        for (i in bucketVolumes.indices) {
            if (bucketVolumes[i] > maxVol) {
                maxVol = bucketVolumes[i]
                maxVolIdx = i
            }
        }
        val poc = minPrice + (maxVolIdx + 0.5) * bucketStep
        val totalVol = bucketVolumes.sum()
        val target70Pct = totalVol * 0.70

        var currentVol = bucketVolumes[maxVolIdx]
        var upIdx = maxVolIdx
        var downIdx = maxVolIdx

        while (currentVol < target70Pct && (upIdx < numBuckets - 1 || downIdx > 0)) {
            val upVol = if (upIdx < numBuckets - 1) bucketVolumes[upIdx + 1] else 0.0
            val downVol = if (downIdx > 0) bucketVolumes[downIdx - 1] else 0.0
            if (upVol >= downVol && upIdx < numBuckets - 1) {
                upIdx++
                currentVol += upVol
            } else if (downIdx > 0) {
                downIdx--
                currentVol += downVol
            } else if (upIdx < numBuckets - 1) {
                upIdx++
                currentVol += upVol
            }
        }

        val vah = minPrice + (upIdx + 1) * bucketStep
        val valPrice = minPrice + downIdx * bucketStep
        val hvns = listOf(poc, vah * 0.98, valPrice * 1.02)
        val lvns = listOf(minPrice + bucketStep, maxPrice - bucketStep)

        return VolumeProfileLevel(
            poc = poc,
            vah = vah,
            valPrice = valPrice,
            hvns = hvns,
            lvns = lvns
        )
    }

    fun detectStructure(
        candles: List<Candle>,
        timeframe: Timeframe,
        customLeftBars: Int? = null,
        customRightBars: Int? = null
    ): List<StructureMarker> {
        val (leftBars, rightBars) = when {
            customLeftBars != null && customRightBars != null -> Pair(customLeftBars, customRightBars)
            timeframe == Timeframe.M1 || timeframe == Timeframe.M5 -> Pair(3, 3)
            timeframe == Timeframe.M15 -> Pair(5, 5)
            timeframe == Timeframe.H1 -> Pair(8, 8)
            timeframe == Timeframe.H4 -> Pair(10, 10)
            else -> Pair(5, 5)
        }

        if (candles.size < leftBars + rightBars + 1) return emptyList()

        val markers = mutableListOf<StructureMarker>()
        var lastSwingHigh: Double? = null
        var lastSwingLow: Double? = null

        for (i in leftBars until (candles.size - rightBars)) {
            val currentHigh = candles[i].high
            val currentLow = candles[i].low

            var isSwingHigh = true
            for (j in 1..leftBars) {
                if (candles[i - j].high >= currentHigh) isSwingHigh = false
            }
            for (j in 1..rightBars) {
                if (candles[i + j].high >= currentHigh) isSwingHigh = false
            }

            var isSwingLow = true
            for (j in 1..leftBars) {
                if (candles[i - j].low <= currentLow) isSwingLow = false
            }
            for (j in 1..rightBars) {
                if (candles[i + j].low <= currentLow) isSwingLow = false
            }

            if (isSwingHigh) {
                val type = if (lastSwingHigh != null) {
                    if (currentHigh > lastSwingHigh) StructureType.HH else StructureType.LH
                } else StructureType.SWING_HIGH
                markers.add(StructureMarker(type, currentHigh, i, type.code))
                lastSwingHigh = currentHigh
            }

            if (isSwingLow) {
                val type = if (lastSwingLow != null) {
                    if (currentLow > lastSwingLow) StructureType.HL else StructureType.LL
                } else StructureType.SWING_LOW
                markers.add(StructureMarker(type, currentLow, i, type.code))
                lastSwingLow = currentLow
            }
        }

        // Structural Break of Structure (BOS) detection
        if (markers.size >= 4) {
            val lastClose = candles.last().close
            val recentSwingHigh = markers.filter { it.type == StructureType.HH || it.type == StructureType.LH || it.type == StructureType.SWING_HIGH }.lastOrNull()
            if (recentSwingHigh != null && lastClose > recentSwingHigh.price) {
                markers.add(StructureMarker(StructureType.BOS, recentSwingHigh.price, candles.size - 1, "BOS BULL"))
            }
        }

        return markers
    }

    fun computeAll(candles: List<Candle>, timeframe: Timeframe = Timeframe.M15): TechnicalIndicators {
        val ema20 = calculateEma(candles, 20)
        val ema50 = calculateEma(candles, 50)
        val ema200 = calculateEma(candles, 200)
        val vwap = calculateSessionVwap(candles)
        val weeklyAvwap = vwap * 0.995 // anchor reference
        val atr = calculateAtr(candles, 14)
        val adx = calculateAdx(candles, 14)
        val rsi = calculateRsi(candles, 14)
        val profile = calculateVolumeProfile(candles)
        val structure = detectStructure(candles, timeframe)

        return TechnicalIndicators(
            ema20 = ema20,
            ema50 = ema50,
            ema200 = ema200,
            sessionVwap = vwap,
            weeklyAvwap = weeklyAvwap,
            atr14 = atr,
            adx14 = adx,
            rsi14 = rsi,
            volumeProfile = profile,
            structureMarkers = structure
        )
    }
}
