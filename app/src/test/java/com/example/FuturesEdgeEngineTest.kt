package com.example

import com.example.engine.DerivativesEngine
import com.example.engine.RiskCalculationInputs
import com.example.engine.RiskEngine
import com.example.engine.SetupAndScoringEngine
import com.example.model.BasisTerm
import com.example.model.MarketRegime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FuturesEdgeEngineTest {

    @Test
    fun testRiskCalculatorPositionSizing_SampleTestCase() {
        // Section 56 Sample Test Case:
        // Account: $20,000, Risk: 0.4%, Entry: 100,000, Stop: 99,000
        // Expected: Risk = $80, Stop distance = 1%, Position Notional = $8,000
        val inputs = RiskCalculationInputs(
            accountEquity = 20000.0,
            riskPercent = 0.4,
            entryPrice = 100000.0,
            stopPrice = 99000.0
        )

        val output = RiskEngine.calculateRisk(inputs)

        assertEquals(80.0, output.riskAmountDollars, 0.001)
        assertEquals(1.0, output.stopDistancePct, 0.001)
        assertEquals(8000.0, output.positionNotional, 0.001)
        assertEquals(0.4, output.effectiveLeverage, 0.001)
    }

    @Test
    fun testFundingZScoreCalculation() {
        val history = listOf(0.01, 0.012, 0.008, 0.011, 0.009)
        val current = 0.028 // High spike
        val z = DerivativesEngine.calculateZScore(current, history)
        assertTrue("Funding Z-Score should be elevated (> 2.0)", z > 2.0)

        val interpretation = DerivativesEngine.interpretFundingCrowding(z)
        assertEquals("Extreme long crowding / flush risk", interpretation)
    }

    @Test
    fun testAnnualizedBasisCalculation() {
        // Futures = 101,000, Spot = 100,000, 90 days to expiry
        val basis = DerivativesEngine.calculateAnnualizedBasis(101000.0, 100000.0, 90.0)
        assertTrue("Annualized basis should be positive", basis > 4.0)

        val term = DerivativesEngine.classifyBasisTerm(basis)
        assertEquals(BasisTerm.CONTANGO, term)
    }

    @Test
    fun testDerivativesFourQuadrantInterpretation() {
        // Price Up + OI Up
        val q1 = DerivativesEngine.interpretOiPrice(2.5, 4.2)
        assertEquals(DerivativesEngine.OiQuadrant.PRICE_UP_OI_UP, q1)

        // Price Up + OI Down
        val q2 = DerivativesEngine.interpretOiPrice(1.8, -3.4)
        assertEquals(DerivativesEngine.OiQuadrant.PRICE_UP_OI_DOWN, q2)

        // Price Down + OI Up
        val q3 = DerivativesEngine.interpretOiPrice(-2.0, 5.0)
        assertEquals(DerivativesEngine.OiQuadrant.PRICE_DOWN_OI_UP, q3)

        // Price Down + OI Down
        val q4 = DerivativesEngine.interpretOiPrice(-1.5, -6.0)
        assertEquals(DerivativesEngine.OiQuadrant.PRICE_DOWN_OI_DOWN, q4)
    }

    @Test
    fun testOpportunityScoreHeuristic() {
        val result = SetupAndScoringEngine.calculateOpportunityScore(
            regime = MarketRegime.BULL,
            rsScore = 2.4,
            liquidityScore = 90,
            oiDeltaPct = 4.0,
            fundingZScore = 0.5,
            spotCvd = 2500000.0,
            perpCvd = 3000000.0,
            orderBookImbalance = 1.65,
            catalystScore = 8,
            rrRatio = 2.4
        )

        assertTrue("Opportunity score should be high for aligned metrics (> 75)", result.totalScore >= 75)
        assertEquals(7, result.auditTrail.size)
    }

    @Test
    fun testExpectancyCalculation() {
        // 6 wins with average 2.0R, 4 losses with average -1.0R
        // Expectancy = 0.6 * 2.0 - 0.4 * 1.0 = 1.2 - 0.4 = 0.8R
        val expectancy = RiskEngine.calculateExpectancy(
            winCount = 6,
            lossCount = 4,
            avgWinR = 2.0,
            avgLossR = 1.0
        )
        assertEquals(0.8, expectancy, 0.001)
    }

    @Test
    fun testTimeStopStalling() {
        val (stalling, message) = RiskEngine.checkTimeStop(barsInTrade = 12, currentMfeR = 0.3, maxBarsThreshold = 10)
        assertTrue(stalling)
        assertTrue(message.contains("ایست زمانی"))
    }
}
