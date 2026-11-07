package com.example.model

enum class VolumeUnit(val label: String, val shortLabel: String, val toLiters: Double) {
  LITERS("Liters", "L", 1.0),
  MILLILITERS("Milliliters", "mL", 0.001),
  GALLONS_US("US Gallons", "gal", 3.78541),
  FLUID_OUNCES_US("US Fl Oz", "fl oz", 0.0295735),
  GALLONS_UK("Imperial Gallons", "imp gal", 4.54609);

  fun convertTo(amount: Double, targetUnit: VolumeUnit): Double {
    val inLiters = amount * this.toLiters
    return inLiters / targetUnit.toLiters
  }
}

enum class TempUnit(val label: String, val symbol: String) {
  CELSIUS("Celsius", "°C"),
  FAHRENHEIT("Fahrenheit", "°F");

  fun toCelsius(temp: Double): Double {
    return if (this == CELSIUS) temp else (temp - 32.0) * 5.0 / 9.0
  }

  fun fromCelsius(tempC: Double): Double {
    return if (this == CELSIUS) tempC else (tempC * 9.0 / 5.0) + 32.0
  }
}

enum class CalibrationTemp(val label: String, val tempC: Double, val displayStr: String) {
  OIML_20C("20°C (68°F) - International Standard", 20.0, "20°C / 68°F"),
  TTB_60F("60°F (15.56°C) - US TTB Standard", 15.5556, "60°F / 15.6°C")
}

enum class DilutionInputMode(val label: String) {
  BY_SPIRIT_VOLUME("I have Spirit, calculate Water to add"),
  BY_TARGET_VOLUME("I want a Target Volume of diluted Spirit")
}

enum class StillType(val label: String, val typicalHeartsAbv: Double, val description: String) {
  POT_STILL("Pot Still (Whiskey, Rum, Brandy)", 72.0, "Rich congener profile, typical collection 60-80% ABV"),
  REFLUX_COLUMN("Reflux Column (Vodka, Neutral)", 93.0, "High purity separation, typical collection 90-95% ABV")
}

data class DilutionResult(
  val initialVolume: Double,
  val initialAbv: Double,
  val targetAbv: Double,
  val waterToAdd: Double,
  val finalVolume: Double,
  val contractionVolume: Double,
  val volumeUnit: VolumeUnit,
  val initialProofUS: Double,
  val targetProofUS: Double,
  val initialProofUK: Double,
  val targetProofUK: Double,
  val pureAlcoholVolume: Double,
  val appliedContraction: Boolean
)

data class TempCorrectionResult(
  val apparentAbv: Double,
  val measuredTemp: Double,
  val tempUnit: TempUnit,
  val calibrationTemp: CalibrationTemp,
  val trueAbv: Double,
  val abvAdjustment: Double,
  val trueProofUS: Double,
  val trueProofUK: Double,
  val explanation: String
)

data class BlendResult(
  val volumeA: Double,
  val abvA: Double,
  val volumeB: Double,
  val abvB: Double,
  val finalVolume: Double,
  val finalAbv: Double,
  val finalProofUS: Double,
  val totalPureAlcohol: Double,
  val volumeUnit: VolumeUnit
)

data class FortificationResult(
  val currentVolume: Double,
  val currentAbv: Double,
  val targetAbv: Double,
  val fortifierAbv: Double,
  val fortifierVolumeNeeded: Double,
  val finalTotalVolume: Double,
  val volumeUnit: VolumeUnit
)

data class StillCutEstimate(
  val name: String,
  val percentageOfAlcohol: Double,
  val volumeAlcoholLiters: Double,
  val estimatedLiquidVolume: Double,
  val collectionAbvRange: String,
  val action: String,
  val description: String
)

data class StillRunResult(
  val washVolume: Double,
  val washAbv: Double,
  val stillType: StillType,
  val totalPureAlcoholLiters: Double,
  val cuts: List<StillCutEstimate>,
  val estimatedHeartsYieldAt40Proof: Double,
  val volumeUnit: VolumeUnit
)
