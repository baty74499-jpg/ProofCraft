package com.example.calculator

import com.example.model.CalibrationTemp
import com.example.model.DilutionInputMode
import com.example.model.DilutionResult
import com.example.model.StillCutEstimate
import com.example.model.StillRunResult
import com.example.model.StillType
import com.example.model.TempCorrectionResult
import com.example.model.TempUnit
import com.example.model.VolumeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object AlcoholChemistry {

  /**
   * Calculates dilution when cutting high-proof spirits with water.
   * Supports both "By Spirit Volume" (how much water to add to my spirit)
   * and "By Target Volume" (I want 10L of 40% spirit, how much 70% spirit and water do I need?).
   */
  fun calculateDilution(
    initialVolumeInput: Double,
    initialAbv: Double,
    targetAbv: Double,
    inputMode: DilutionInputMode,
    volumeUnit: VolumeUnit,
    applyContraction: Boolean = true
  ): DilutionResult {
    val clampedInitialAbv = initialAbv.coerceIn(0.1, 100.0)
    val clampedTargetAbv = targetAbv.coerceIn(0.1, clampedInitialAbv - 0.01)

    // Contraction factor: ethanol-water mixing volume reduction.
    // Peaks around 50-55% ABV at ~3.6% reduction of total volume.
    val contractionFactor = calculateContractionPercentage(clampedInitialAbv, clampedTargetAbv) / 100.0

    val initialSpiritVolume: Double
    val targetFinalVolume: Double
    val waterToAdd: Double
    val contractionVolume: Double

    when (inputMode) {
      DilutionInputMode.BY_SPIRIT_VOLUME -> {
        initialSpiritVolume = initialVolumeInput
        val naiveWater = initialSpiritVolume * ((clampedInitialAbv / clampedTargetAbv) - 1.0)
        val naiveTotal = initialSpiritVolume + naiveWater

        if (applyContraction) {
          // Contraction shrinks volume, so to actually achieve target final ABV
          // with exact volume, extra water compensates for contraction
          contractionVolume = naiveTotal * contractionFactor
          waterToAdd = naiveWater + contractionVolume
          targetFinalVolume = initialSpiritVolume + waterToAdd - contractionVolume
        } else {
          contractionVolume = 0.0
          waterToAdd = naiveWater
          targetFinalVolume = naiveTotal
        }
      }
      DilutionInputMode.BY_TARGET_VOLUME -> {
        targetFinalVolume = initialVolumeInput
        // How much initial spirit is needed to supply pure alcohol for target volume:
        val pureAlcoholNeeded = targetFinalVolume * (clampedTargetAbv / 100.0)
        initialSpiritVolume = pureAlcoholNeeded / (clampedInitialAbv / 100.0)

        val naiveWater = targetFinalVolume - initialSpiritVolume
        if (applyContraction) {
          contractionVolume = targetFinalVolume * contractionFactor
          waterToAdd = naiveWater + contractionVolume
        } else {
          contractionVolume = 0.0
          waterToAdd = naiveWater
        }
      }
    }

    val pureAlcohol = initialSpiritVolume * (clampedInitialAbv / 100.0)

    val initialProofUS = clampedInitialAbv * 2.0
    val targetProofUS = clampedTargetAbv * 2.0
    val initialProofUK = clampedInitialAbv * 1.7512
    val targetProofUK = clampedTargetAbv * 1.7512

    return DilutionResult(
      initialVolume = initialSpiritVolume,
      initialAbv = clampedInitialAbv,
      targetAbv = clampedTargetAbv,
      waterToAdd = max(0.0, waterToAdd),
      finalVolume = targetFinalVolume,
      contractionVolume = contractionVolume,
      volumeUnit = volumeUnit,
      initialProofUS = initialProofUS,
      targetProofUS = targetProofUS,
      initialProofUK = initialProofUK,
      targetProofUK = targetProofUK,
      pureAlcoholVolume = pureAlcohol,
      appliedContraction = applyContraction
    )
  }

  /**
   * Ethanol-Water contraction percentage for final mixture at target ABV.
   * Empirical parabolic curve matching OIML/AOAC tables:
   * Max contraction ~3.55% around 54% ABV.
   */
  fun calculateContractionPercentage(initialAbv: Double, targetAbv: Double): Double {
    val x = targetAbv.coerceIn(0.0, 100.0)
    // Bell-shaped curve centered around 53% ABV
    val baseContraction = 3.55 * (1.0 - ((x - 53.0) / 53.0).pow(2.0)).coerceAtLeast(0.0)
    // Scale by initial ABV differential (diluting from 95% contracts more than from 45%)
    val deltaFactor = ((initialAbv - targetAbv) / 60.0).coerceIn(0.2, 1.0)
    return (baseContraction * deltaFactor).coerceIn(0.0, 4.0)
  }

  /**
   * Temperature correction for hydrometer/alcoholmeter readings.
   * Standard hydrometer is calibrated at 20°C (68°F) or 60°F (15.56°C).
   */
  fun calculateHydrometerTempCorrection(
    apparentAbv: Double,
    measuredTemp: Double,
    tempUnit: TempUnit,
    calibrationTemp: CalibrationTemp
  ): TempCorrectionResult {
    val apparent = apparentAbv.coerceIn(0.0, 100.0)
    val tempC = tempUnit.toCelsius(measuredTemp)
    val calC = calibrationTemp.tempC

    val deltaTemp = tempC - calC

    // Rate of apparent ABV shift per degree Celsius (derived from TTB Table 1 Gauging Manual):
    // Near 50% ABV, thermal expansion causes ~0.28% ABV reading error per °C.
    // Higher ABV has higher thermal expansion rate (pure ethanol expands 5x more than water).
    val expansionRatePerDegC = 0.125 + (0.0033 * apparent) + (0.000012 * apparent.pow(2.0))

    // Non-linear correction for large delta-T (>10°C):
    val nonLinearity = 0.0012 * deltaTemp.pow(2.0) * if (deltaTemp >= 0) -1.0 else 1.0

    // Total adjustment to reading:
    // If liquid is hotter than calibrated (deltaTemp > 0), liquid is less dense, hydrometer sinks deeper,
    // so reading is TOO HIGH. True ABV is LOWER (negative adjustment).
    val adjustment = -(deltaTemp * expansionRatePerDegC + nonLinearity)

    val trueAbv = (apparent + adjustment).coerceIn(0.0, 100.0)
    val trueProofUS = trueAbv * 2.0
    val trueProofUK = trueAbv * 1.7512

    val explanation = when {
      abs(deltaTemp) < 0.25 -> {
        "Liquid is at calibration temperature (${calibrationTemp.displayStr}). No temperature adjustment needed."
      }
      deltaTemp > 0 -> {
        val degStr = if (tempUnit == TempUnit.CELSIUS) {
          String.format("%.1f°C", deltaTemp)
        } else {
          String.format("%.1f°F", deltaTemp * 9.0 / 5.0)
        }
        "Liquid is $degStr warmer than calibration. Warm liquid is less dense, causing the hydrometer to sink deeper. True ABV is ${String.format("%.2f%%", abs(adjustment))} lower than indicated."
      }
      else -> {
        val degStr = if (tempUnit == TempUnit.CELSIUS) {
          String.format("%.1f°C", abs(deltaTemp))
        } else {
          String.format("%.1f°F", abs(deltaTemp) * 9.0 / 5.0)
        }
        "Liquid is $degStr colder than calibration. Cold liquid is denser, causing the hydrometer to float higher. True ABV is ${String.format("%.2f%%", abs(adjustment))} higher than indicated."
      }
    }

    return TempCorrectionResult(
      apparentAbv = apparent,
      measuredTemp = measuredTemp,
      tempUnit = tempUnit,
      calibrationTemp = calibrationTemp,
      trueAbv = trueAbv,
      abvAdjustment = adjustment,
      trueProofUS = trueProofUS,
      trueProofUK = trueProofUK,
      explanation = explanation
    )
  }

  /**
   * Blending two spirits with different volumes and ABVs.
   */
  fun blendSpirits(
    volumeA: Double,
    abvA: Double,
    volumeB: Double,
    abvB: Double,
    unit: VolumeUnit
  ): com.example.model.BlendResult {
    val volA = max(0.0, volumeA)
    val volB = max(0.0, volumeB)
    val clampedAbvA = abvA.coerceIn(0.0, 100.0)
    val clampedAbvB = abvB.coerceIn(0.0, 100.0)

    val pureA = volA * (clampedAbvA / 100.0)
    val pureB = volB * (clampedAbvB / 100.0)
    val totalPure = pureA + pureB
    val totalVolume = volA + volB

    val finalAbv = if (totalVolume > 0.0) (totalPure / totalVolume) * 100.0 else 0.0
    val finalProofUS = finalAbv * 2.0

    return com.example.model.BlendResult(
      volumeA = volA,
      abvA = clampedAbvA,
      volumeB = volB,
      abvB = clampedAbvB,
      finalVolume = totalVolume,
      finalAbv = finalAbv,
      finalProofUS = finalProofUS,
      totalPureAlcohol = totalPure,
      volumeUnit = unit
    )
  }

  /**
   * Fortification / Spiking calculator:
   * How much high-strength spirit (e.g. 95% neutral spirit) is needed to raise a batch
   * from current ABV to target ABV.
   */
  fun calculateFortification(
    currentVolume: Double,
    currentAbv: Double,
    fortifierAbv: Double,
    targetAbv: Double,
    unit: VolumeUnit
  ): com.example.model.FortificationResult {
    val curVol = max(0.0, currentVolume)
    val curAbv = currentAbv.coerceIn(0.0, 100.0)
    val fortAbv = fortifierAbv.coerceIn(curAbv + 0.1, 100.0)
    val tgtAbv = targetAbv.coerceIn(curAbv, fortAbv - 0.01)

    // V_fort * (fortAbv - tgtAbv) = curVol * (tgtAbv - curAbv)
    val fortVolumeNeeded = if (fortAbv > tgtAbv) {
      curVol * (tgtAbv - curAbv) / (fortAbv - tgtAbv)
    } else {
      0.0
    }

    val finalTotalVol = curVol + fortVolumeNeeded

    return com.example.model.FortificationResult(
      currentVolume = curVol,
      currentAbv = curAbv,
      targetAbv = tgtAbv,
      fortifierAbv = fortAbv,
      fortifierVolumeNeeded = max(0.0, fortVolumeNeeded),
      finalTotalVolume = finalTotalVol,
      volumeUnit = unit
    )
  }

  /**
   * Still Run Estimator:
   * Predicts cuts breakdown (Foreshots, Heads, Hearts, Tails) and yield at 40% ABV
   * for a pot still or reflux column run.
   */
  fun calculateStillRun(
    washVolume: Double,
    washAbv: Double,
    stillType: StillType,
    unit: VolumeUnit
  ): StillRunResult {
    val washVolInLiters = washVolume * unit.toLiters
    val clampedWashAbv = washAbv.coerceIn(1.0, 25.0)

    val totalPureAlcoholLiters = washVolInLiters * (clampedWashAbv / 100.0)

    val cuts = when (stillType) {
      StillType.POT_STILL -> {
        // Pot still traditional cut fractions of total pure alcohol:
        // Foreshots: ~1.5%
        // Heads: ~12%
        // Hearts: ~62% (average ~70-75% ABV)
        // Tails: ~24.5% (feints)
        val foreshotsPA = totalPureAlcoholLiters * 0.015
        val headsPA = totalPureAlcoholLiters * 0.12
        val heartsPA = totalPureAlcoholLiters * 0.62
        val tailsPA = totalPureAlcoholLiters * 0.245

        listOf(
          StillCutEstimate(
            name = "Foreshots",
            percentageOfAlcohol = 1.5,
            volumeAlcoholLiters = foreshotsPA,
            estimatedLiquidVolume = foreshotsPA / 0.82,
            collectionAbvRange = "80-84% ABV",
            action = "Discard",
            description = "Contains toxic low-boiling compounds (methanol, acetone, ethyl acetate). Always discard."
          ),
          StillCutEstimate(
            name = "Heads",
            percentageOfAlcohol = 12.0,
            volumeAlcoholLiters = headsPA,
            estimatedLiquidVolume = headsPA / 0.78,
            collectionAbvRange = "74-80% ABV",
            action = "Save as Feints",
            description = "Sharp, solvent, chemical aromas (aldehydes). Collect separately and add to future stripping runs."
          ),
          StillCutEstimate(
            name = "Hearts (Sweet Spot)",
            percentageOfAlcohol = 62.0,
            volumeAlcoholLiters = heartsPA,
            estimatedLiquidVolume = heartsPA / 0.70,
            collectionAbvRange = "62-74% ABV",
            action = "Keep for Aging / Drinking",
            description = "Pure ethanol, pleasant esters, rich grain/fruit character. The prime spirit cut."
          ),
          StillCutEstimate(
            name = "Tails",
            percentageOfAlcohol = 24.5,
            volumeAlcoholLiters = tailsPA,
            estimatedLiquidVolume = tailsPA / 0.40,
            collectionAbvRange = "<60% down to 20% ABV",
            action = "Save as Feints",
            description = "Cardboard, oily fusel alcohols, heavy wet dog notes. Recycle into next feints run."
          )
        )
      }
      StillType.REFLUX_COLUMN -> {
        // Reflux column separates much more sharply at high azeotropic purity (~93-95%):
        val foreshotsPA = totalPureAlcoholLiters * 0.01
        val headsPA = totalPureAlcoholLiters * 0.08
        val heartsPA = totalPureAlcoholLiters * 0.78
        val tailsPA = totalPureAlcoholLiters * 0.13

        listOf(
          StillCutEstimate(
            name = "Foreshots",
            percentageOfAlcohol = 1.0,
            volumeAlcoholLiters = foreshotsPA,
            estimatedLiquidVolume = foreshotsPA / 0.95,
            collectionAbvRange = "94-96% ABV",
            action = "Discard",
            description = "Initial concentrated volatiles. Discard without tasting."
          ),
          StillCutEstimate(
            name = "Heads",
            percentageOfAlcohol = 8.0,
            volumeAlcoholLiters = headsPA,
            estimatedLiquidVolume = headsPA / 0.94,
            collectionAbvRange = "93-95% ABV",
            action = "Save as Feints",
            description = "Solvent aromas. Clean fraction starts once chemical burn disappears."
          ),
          StillCutEstimate(
            name = "Hearts (Neutral Spirits)",
            percentageOfAlcohol = 78.0,
            volumeAlcoholLiters = heartsPA,
            estimatedLiquidVolume = heartsPA / 0.93,
            collectionAbvRange = "92-95% ABV",
            action = "Keep for Vodka / Gin / Liqueurs",
            description = "Ultra-pure neutral ethanol with clean mouthfeel and minimal congeners."
          ),
          StillCutEstimate(
            name = "Tails",
            percentageOfAlcohol = 13.0,
            volumeAlcoholLiters = tailsPA,
            estimatedLiquidVolume = tailsPA / 0.60,
            collectionAbvRange = "Column temp rises sharply",
            action = "Shut down / Recycle",
            description = "Heavy fusels. Stop collecting when column temperature begins climbing."
          )
        )
      }
    }

    // Hearts pure alcohol diluted to standard 40% (80 US Proof) commercial strength:
    val heartsPA = cuts.first { it.name.startsWith("Hearts") }.volumeAlcoholLiters
    val finalYieldAt40Liters = heartsPA / 0.40

    return StillRunResult(
      washVolume = washVolume,
      washAbv = clampedWashAbv,
      stillType = stillType,
      totalPureAlcoholLiters = totalPureAlcoholLiters,
      cuts = cuts,
      estimatedHeartsYieldAt40Proof = finalYieldAt40Liters / unit.toLiters,
      volumeUnit = unit
    )
  }
}
