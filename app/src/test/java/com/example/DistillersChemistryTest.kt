package com.example

import com.example.calculator.AlcoholChemistry
import com.example.model.CalibrationTemp
import com.example.model.DilutionInputMode
import com.example.model.StillType
import com.example.model.TempUnit
import com.example.model.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DistillersChemistryTest {

  @Test
  fun testDilutionCalculation_simpleWithoutContraction() {
    // 5 Liters of 60% spirit diluted to 40% spirit:
    // Naive water: 5 * (60 / 40 - 1) = 5 * 0.5 = 2.5 Liters
    val result = AlcoholChemistry.calculateDilution(
      initialVolumeInput = 5.0,
      initialAbv = 60.0,
      targetAbv = 40.0,
      inputMode = DilutionInputMode.BY_SPIRIT_VOLUME,
      volumeUnit = VolumeUnit.LITERS,
      applyContraction = false
    )

    assertEquals(2.5, result.waterToAdd, 0.001)
    assertEquals(7.5, result.finalVolume, 0.001)
    assertEquals(120.0, result.initialProofUS, 0.001)
    assertEquals(80.0, result.targetProofUS, 0.001)
  }

  @Test
  fun testDilutionCalculation_withContraction() {
    // With contraction, extra water is required to offset molecular shrinkage
    val result = AlcoholChemistry.calculateDilution(
      initialVolumeInput = 5.0,
      initialAbv = 60.0,
      targetAbv = 40.0,
      inputMode = DilutionInputMode.BY_SPIRIT_VOLUME,
      volumeUnit = VolumeUnit.LITERS,
      applyContraction = true
    )

    assertTrue("Contraction volume should be positive", result.contractionVolume > 0.0)
    assertTrue("Water with contraction compensation must exceed naive 2.5L", result.waterToAdd > 2.5)
  }

  @Test
  fun testHydrometerTempCorrection_atCalibration() {
    // At 20°C calibrated with 20°C liquid:
    val result = AlcoholChemistry.calculateHydrometerTempCorrection(
      apparentAbv = 50.0,
      measuredTemp = 20.0,
      tempUnit = TempUnit.CELSIUS,
      calibrationTemp = CalibrationTemp.OIML_20C
    )

    assertEquals(50.0, result.trueAbv, 0.01)
    assertEquals(0.0, result.abvAdjustment, 0.01)
    assertEquals(100.0, result.trueProofUS, 0.01)
  }

  @Test
  fun testHydrometerTempCorrection_warmLiquidReadsHigh() {
    // Liquid at 26°C (+6°C warmer than 20°C):
    // Warm liquid expands, sinks deeper, apparent ABV is higher than true ABV.
    val result = AlcoholChemistry.calculateHydrometerTempCorrection(
      apparentAbv = 50.0,
      measuredTemp = 26.0,
      tempUnit = TempUnit.CELSIUS,
      calibrationTemp = CalibrationTemp.OIML_20C
    )

    assertTrue("True ABV must be lower than apparent reading for warm liquid", result.trueAbv < 50.0)
    assertTrue("Adjustment must be negative", result.abvAdjustment < 0.0)
    // TTB Table 1 shift for 50% ABV over 6°C is ~1.7-1.9%
    assertEquals(48.2, result.trueAbv, 0.4)
  }

  @Test
  fun testHydrometerTempCorrection_coldLiquidReadsLow() {
    // Liquid at 14°C (-6°C colder than 20°C):
    val result = AlcoholChemistry.calculateHydrometerTempCorrection(
      apparentAbv = 50.0,
      measuredTemp = 14.0,
      tempUnit = TempUnit.CELSIUS,
      calibrationTemp = CalibrationTemp.OIML_20C
    )

    assertTrue("True ABV must be higher than apparent reading for cold liquid", result.trueAbv > 50.0)
    assertTrue("Adjustment must be positive", result.abvAdjustment > 0.0)
  }

  @Test
  fun testBlendSpirits() {
    // 3L of 60% ABV + 2L of 35% ABV:
    // Pure A = 1.8L, Pure B = 0.7L -> Total pure = 2.5L in 5L total -> 50% ABV
    val result = AlcoholChemistry.blendSpirits(
      volumeA = 3.0,
      abvA = 60.0,
      volumeB = 2.0,
      abvB = 35.0,
      unit = VolumeUnit.LITERS
    )

    assertEquals(5.0, result.finalVolume, 0.001)
    assertEquals(50.0, result.finalAbv, 0.001)
    assertEquals(100.0, result.finalProofUS, 0.001)
    assertEquals(2.5, result.totalPureAlcohol, 0.001)
  }

  @Test
  fun testFortification() {
    // Have 10L of 40% spirit. Want 45% using 95% spirit.
    // 10 * (45 - 40) / (95 - 45) = 10 * 5 / 50 = 1.0 Liter
    val result = AlcoholChemistry.calculateFortification(
      currentVolume = 10.0,
      currentAbv = 40.0,
      fortifierAbv = 95.0,
      targetAbv = 45.0,
      unit = VolumeUnit.LITERS
    )

    assertEquals(1.0, result.fortifierVolumeNeeded, 0.001)
    assertEquals(11.0, result.finalTotalVolume, 0.001)
  }

  @Test
  fun testStillRunEstimator() {
    val result = AlcoholChemistry.calculateStillRun(
      washVolume = 25.0,
      washAbv = 10.0,
      stillType = StillType.POT_STILL,
      unit = VolumeUnit.LITERS
    )

    // 25L * 10% = 2.5L Pure Alcohol
    assertEquals(2.5, result.totalPureAlcoholLiters, 0.001)
    assertEquals(4, result.cuts.size)

    // Foreshots, Heads, Hearts, Tails
    assertEquals("Foreshots", result.cuts[0].name)
    assertTrue(result.cuts[0].volumeAlcoholLiters > 0.0)
    assertTrue("Estimated finished yield at 40% proof should be around 3.8L", result.estimatedHeartsYieldAt40Proof > 3.0)
  }

  @Test
  fun testVolumeUnitConversions() {
    // 1 US Gallon ~ 3.78541 Liters
    val litersInGal = VolumeUnit.GALLONS_US.convertTo(1.0, VolumeUnit.LITERS)
    assertEquals(3.78541, litersInGal, 0.001)

    // 1000 mL = 1 L
    val lInMl = VolumeUnit.MILLILITERS.convertTo(1000.0, VolumeUnit.LITERS)
    assertEquals(1.0, lInMl, 0.001)
  }
}
