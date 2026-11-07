package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.AlcoholChemistry
import com.example.data.AppDatabase
import com.example.data.SavedCalculation
import com.example.model.BlendResult
import com.example.model.CalibrationTemp
import com.example.model.DilutionInputMode
import com.example.model.DilutionResult
import com.example.model.FortificationResult
import com.example.model.StillRunResult
import com.example.model.StillType
import com.example.model.TempCorrectionResult
import com.example.model.TempUnit
import com.example.model.VolumeUnit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DistillersViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val dao = database.savedCalculationDao()

  val savedCalculations: StateFlow<List<SavedCalculation>> = dao.getAllCalculations()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _toastMessage = MutableSharedFlow<String>()
  val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

  // -------------------------------------------------------------
  // 1. Dilution Calculator State
  // -------------------------------------------------------------
  private val _dilutionVolumeInput = MutableStateFlow("5.0")
  val dilutionVolumeInput: StateFlow<String> = _dilutionVolumeInput.asStateFlow()

  private val _dilutionInitialAbvInput = MutableStateFlow("65.0")
  val dilutionInitialAbvInput: StateFlow<String> = _dilutionInitialAbvInput.asStateFlow()

  private val _dilutionTargetAbvInput = MutableStateFlow("40.0")
  val dilutionTargetAbvInput: StateFlow<String> = _dilutionTargetAbvInput.asStateFlow()

  private val _dilutionMode = MutableStateFlow(DilutionInputMode.BY_SPIRIT_VOLUME)
  val dilutionMode: StateFlow<DilutionInputMode> = _dilutionMode.asStateFlow()

  private val _dilutionUnit = MutableStateFlow(VolumeUnit.LITERS)
  val dilutionUnit: StateFlow<VolumeUnit> = _dilutionUnit.asStateFlow()

  private val _applyContraction = MutableStateFlow(true)
  val applyContraction: StateFlow<Boolean> = _applyContraction.asStateFlow()

  private val _dilutionResult = MutableStateFlow<DilutionResult?>(null)
  val dilutionResult: StateFlow<DilutionResult?> = _dilutionResult.asStateFlow()

  // -------------------------------------------------------------
  // 2. Hydrometer Temp Correction State
  // -------------------------------------------------------------
  private val _apparentAbvInput = MutableStateFlow("54.0")
  val apparentAbvInput: StateFlow<String> = _apparentAbvInput.asStateFlow()

  private val _measuredTempInput = MutableStateFlow("24.0")
  val measuredTempInput: StateFlow<String> = _measuredTempInput.asStateFlow()

  private val _tempUnit = MutableStateFlow(TempUnit.CELSIUS)
  val tempUnit: StateFlow<TempUnit> = _tempUnit.asStateFlow()

  private val _calibrationTemp = MutableStateFlow(CalibrationTemp.OIML_20C)
  val calibrationTemp: StateFlow<CalibrationTemp> = _calibrationTemp.asStateFlow()

  private val _tempCorrectionResult = MutableStateFlow<TempCorrectionResult?>(null)
  val tempCorrectionResult: StateFlow<TempCorrectionResult?> = _tempCorrectionResult.asStateFlow()

  // -------------------------------------------------------------
  // 3. Blending & Fortification State
  // -------------------------------------------------------------
  private val _blendMode = MutableStateFlow(0) // 0: Blend Two Spirits, 1: Fortify / Spike
  val blendMode: StateFlow<Int> = _blendMode.asStateFlow()

  private val _blendVolumeA = MutableStateFlow("3.0")
  val blendVolumeA: StateFlow<String> = _blendVolumeA.asStateFlow()

  private val _blendAbvA = MutableStateFlow("60.0")
  val blendAbvA: StateFlow<String> = _blendAbvA.asStateFlow()

  private val _blendVolumeB = MutableStateFlow("2.0")
  val blendVolumeB: StateFlow<String> = _blendVolumeB.asStateFlow()

  private val _blendAbvB = MutableStateFlow("35.0")
  val blendAbvB: StateFlow<String> = _blendAbvB.asStateFlow()

  private val _blendUnit = MutableStateFlow(VolumeUnit.LITERS)
  val blendUnit: StateFlow<VolumeUnit> = _blendUnit.asStateFlow()

  private val _blendResult = MutableStateFlow<BlendResult?>(null)
  val blendResult: StateFlow<BlendResult?> = _blendResult.asStateFlow()

  // Fortification specifics
  private val _fortifyTargetAbv = MutableStateFlow("46.0")
  val fortifyTargetAbv: StateFlow<String> = _fortifyTargetAbv.asStateFlow()

  private val _fortifySpiritAbv = MutableStateFlow("95.0")
  val fortifySpiritAbv: StateFlow<String> = _fortifySpiritAbv.asStateFlow()

  private val _fortificationResult = MutableStateFlow<FortificationResult?>(null)
  val fortificationResult: StateFlow<FortificationResult?> = _fortificationResult.asStateFlow()

  // -------------------------------------------------------------
  // 4. Still Run Yield State
  // -------------------------------------------------------------
  private val _washVolumeInput = MutableStateFlow("25.0")
  val washVolumeInput: StateFlow<String> = _washVolumeInput.asStateFlow()

  private val _washAbvInput = MutableStateFlow("9.5")
  val washAbvInput: StateFlow<String> = _washAbvInput.asStateFlow()

  private val _stillType = MutableStateFlow(StillType.POT_STILL)
  val stillType: StateFlow<StillType> = _stillType.asStateFlow()

  private val _stillUnit = MutableStateFlow(VolumeUnit.LITERS)
  val stillUnit: StateFlow<VolumeUnit> = _stillUnit.asStateFlow()

  private val _stillRunResult = MutableStateFlow<StillRunResult?>(null)
  val stillRunResult: StateFlow<StillRunResult?> = _stillRunResult.asStateFlow()

  init {
    recalculateDilution()
    recalculateTempCorrection()
    recalculateBlend()
    recalculateFortification()
    recalculateStillRun()
  }

  // -------------------------------------------------------------
  // Actions: Dilution
  // -------------------------------------------------------------
  fun updateDilutionVolume(input: String) {
    _dilutionVolumeInput.value = input
    recalculateDilution()
  }

  fun updateDilutionInitialAbv(input: String) {
    _dilutionInitialAbvInput.value = input
    recalculateDilution()
  }

  fun updateDilutionTargetAbv(input: String) {
    _dilutionTargetAbvInput.value = input
    recalculateDilution()
  }

  fun setDilutionMode(mode: DilutionInputMode) {
    _dilutionMode.value = mode
    recalculateDilution()
  }

  fun setDilutionUnit(unit: VolumeUnit) {
    _dilutionUnit.value = unit
    recalculateDilution()
  }

  fun toggleApplyContraction(apply: Boolean) {
    _applyContraction.value = apply
    recalculateDilution()
  }

  private fun recalculateDilution() {
    val vol = _dilutionVolumeInput.value.toDoubleOrNull() ?: 0.0
    val initAbv = _dilutionInitialAbvInput.value.toDoubleOrNull() ?: 0.0
    val targetAbv = _dilutionTargetAbvInput.value.toDoubleOrNull() ?: 0.0

    if (vol > 0 && initAbv > 0 && targetAbv > 0 && targetAbv < initAbv) {
      _dilutionResult.value = AlcoholChemistry.calculateDilution(
        initialVolumeInput = vol,
        initialAbv = initAbv,
        targetAbv = targetAbv,
        inputMode = _dilutionMode.value,
        volumeUnit = _dilutionUnit.value,
        applyContraction = _applyContraction.value
      )
    } else {
      _dilutionResult.value = null
    }
  }

  // -------------------------------------------------------------
  // Actions: Hydrometer Temperature Correction
  // -------------------------------------------------------------
  fun updateApparentAbv(input: String) {
    _apparentAbvInput.value = input
    recalculateTempCorrection()
  }

  fun updateMeasuredTemp(input: String) {
    _measuredTempInput.value = input
    recalculateTempCorrection()
  }

  fun setTempUnit(unit: TempUnit) {
    val currentTemp = _measuredTempInput.value.toDoubleOrNull()
    if (currentTemp != null) {
      val converted = if (unit == TempUnit.FAHRENHEIT) {
        TempUnit.CELSIUS.fromCelsius(currentTemp)
      } else {
        TempUnit.FAHRENHEIT.toCelsius(currentTemp)
      }
      _measuredTempInput.value = String.format("%.1f", converted)
    }
    _tempUnit.value = unit
    recalculateTempCorrection()
  }

  fun setCalibrationTemp(cal: CalibrationTemp) {
    _calibrationTemp.value = cal
    recalculateTempCorrection()
  }

  private fun recalculateTempCorrection() {
    val apparent = _apparentAbvInput.value.toDoubleOrNull() ?: 0.0
    val temp = _measuredTempInput.value.toDoubleOrNull() ?: 20.0

    if (apparent > 0) {
      _tempCorrectionResult.value = AlcoholChemistry.calculateHydrometerTempCorrection(
        apparentAbv = apparent,
        measuredTemp = temp,
        tempUnit = _tempUnit.value,
        calibrationTemp = _calibrationTemp.value
      )
    } else {
      _tempCorrectionResult.value = null
    }
  }

  /**
   * Transfers corrected True ABV into the Dilution Initial ABV field directly.
   */
  fun applyTrueAbvToDilution(onNavigateToDilution: () -> Unit) {
    val result = _tempCorrectionResult.value
    if (result != null) {
      _dilutionInitialAbvInput.value = String.format("%.1f", result.trueAbv)
      recalculateDilution()
      viewModelScope.launch {
        _toastMessage.emit("Transferred True ABV (${String.format("%.1f%%", result.trueAbv)}) to Dilution Calculator")
      }
      onNavigateToDilution()
    }
  }

  // -------------------------------------------------------------
  // Actions: Blending & Fortification
  // -------------------------------------------------------------
  fun setBlendMode(mode: Int) {
    _blendMode.value = mode
  }

  fun updateBlendVolumeA(v: String) {
    _blendVolumeA.value = v
    recalculateBlend()
    recalculateFortification()
  }

  fun updateBlendAbvA(v: String) {
    _blendAbvA.value = v
    recalculateBlend()
    recalculateFortification()
  }

  fun updateBlendVolumeB(v: String) {
    _blendVolumeB.value = v
    recalculateBlend()
  }

  fun updateBlendAbvB(v: String) {
    _blendAbvB.value = v
    recalculateBlend()
  }

  fun setBlendUnit(unit: VolumeUnit) {
    _blendUnit.value = unit
    recalculateBlend()
    recalculateFortification()
  }

  fun updateFortifyTargetAbv(v: String) {
    _fortifyTargetAbv.value = v
    recalculateFortification()
  }

  fun updateFortifySpiritAbv(v: String) {
    _fortifySpiritAbv.value = v
    recalculateFortification()
  }

  private fun recalculateBlend() {
    val vA = _blendVolumeA.value.toDoubleOrNull() ?: 0.0
    val aA = _blendAbvA.value.toDoubleOrNull() ?: 0.0
    val vB = _blendVolumeB.value.toDoubleOrNull() ?: 0.0
    val aB = _blendAbvB.value.toDoubleOrNull() ?: 0.0

    if (vA > 0 || vB > 0) {
      _blendResult.value = AlcoholChemistry.blendSpirits(vA, aA, vB, aB, _blendUnit.value)
    } else {
      _blendResult.value = null
    }
  }

  private fun recalculateFortification() {
    val curVol = _blendVolumeA.value.toDoubleOrNull() ?: 0.0
    val curAbv = _blendAbvA.value.toDoubleOrNull() ?: 0.0
    val fortAbv = _fortifySpiritAbv.value.toDoubleOrNull() ?: 95.0
    val tgtAbv = _fortifyTargetAbv.value.toDoubleOrNull() ?: 0.0

    if (curVol > 0 && curAbv > 0 && fortAbv > tgtAbv && tgtAbv > curAbv) {
      _fortificationResult.value = AlcoholChemistry.calculateFortification(
        currentVolume = curVol,
        currentAbv = curAbv,
        fortifierAbv = fortAbv,
        targetAbv = tgtAbv,
        unit = _blendUnit.value
      )
    } else {
      _fortificationResult.value = null
    }
  }

  // -------------------------------------------------------------
  // Actions: Still Run
  // -------------------------------------------------------------
  fun updateWashVolume(v: String) {
    _washVolumeInput.value = v
    recalculateStillRun()
  }

  fun updateWashAbv(v: String) {
    _washAbvInput.value = v
    recalculateStillRun()
  }

  fun setStillType(type: StillType) {
    _stillType.value = type
    recalculateStillRun()
  }

  fun setStillUnit(unit: VolumeUnit) {
    _stillUnit.value = unit
    recalculateStillRun()
  }

  private fun recalculateStillRun() {
    val vol = _washVolumeInput.value.toDoubleOrNull() ?: 0.0
    val abv = _washAbvInput.value.toDoubleOrNull() ?: 0.0

    if (vol > 0 && abv > 0) {
      _stillRunResult.value = AlcoholChemistry.calculateStillRun(vol, abv, _stillType.value, _stillUnit.value)
    } else {
      _stillRunResult.value = null
    }
  }

  // -------------------------------------------------------------
  // Database: Saving and deleting calculations
  // -------------------------------------------------------------
  fun saveDilutionRun(title: String, notes: String = "") {
    val res = _dilutionResult.value ?: return
    viewModelScope.launch {
      val item = SavedCalculation(
        title = title.ifBlank { "Dilution Run (${String.format("%.1f", res.initialAbv)}% → ${String.format("%.1f", res.targetAbv)}%)" },
        type = "DILUTION",
        inputSummary = "Initial: ${String.format("%.2f", res.initialVolume)} ${res.volumeUnit.shortLabel} @ ${String.format("%.1f", res.initialAbv)}% ABV (${String.format("%.1f", res.initialProofUS)}° Proof)",
        resultSummary = "Add ${String.format("%.2f", res.waterToAdd)} ${res.volumeUnit.shortLabel} Water → Target: ${String.format("%.2f", res.finalVolume)} ${res.volumeUnit.shortLabel} @ ${String.format("%.1f", res.targetAbv)}% ABV",
        secondarySummary = if (res.appliedContraction && res.contractionVolume > 0) {
          "Contraction volume shrinkage: ${String.format("%.3f", res.contractionVolume)} ${res.volumeUnit.shortLabel}"
        } else {
          "Ideal dilution (without contraction correction)"
        },
        notes = notes
      )
      dao.insertCalculation(item)
      _toastMessage.emit("Dilution saved to batch log")
    }
  }

  fun saveHydrometerRun(title: String, notes: String = "") {
    val res = _tempCorrectionResult.value ?: return
    viewModelScope.launch {
      val item = SavedCalculation(
        title = title.ifBlank { "Hydrometer Check (${String.format("%.1f", res.trueAbv)}% True ABV)" },
        type = "HYDROMETER",
        inputSummary = "Reading: ${String.format("%.1f", res.apparentAbv)}% ABV at ${String.format("%.1f", res.measuredTemp)}${res.tempUnit.symbol} (Calibrated: ${res.calibrationTemp.displayStr})",
        resultSummary = "True ABV: ${String.format("%.1f", res.trueAbv)}% ABV (${String.format("%.1f", res.trueProofUS)}° Proof)",
        secondarySummary = "Temp Adjustment: ${if (res.abvAdjustment >= 0) "+" else ""}${String.format("%.2f", res.abvAdjustment)}% ABV",
        notes = notes
      )
      dao.insertCalculation(item)
      _toastMessage.emit("Hydrometer check saved to log")
    }
  }

  fun saveBlendRun(title: String, notes: String = "") {
    val res = _blendResult.value ?: return
    viewModelScope.launch {
      val item = SavedCalculation(
        title = title.ifBlank { "Spirit Blend (${String.format("%.1f", res.finalAbv)}% ABV)" },
        type = "BLEND",
        inputSummary = "A: ${String.format("%.2f", res.volumeA)} ${res.volumeUnit.shortLabel} @ ${String.format("%.1f", res.abvA)}% + B: ${String.format("%.2f", res.volumeB)} ${res.volumeUnit.shortLabel} @ ${String.format("%.1f", res.abvB)}%",
        resultSummary = "Blended: ${String.format("%.2f", res.finalVolume)} ${res.volumeUnit.shortLabel} @ ${String.format("%.1f", res.finalAbv)}% ABV (${String.format("%.1f", res.finalProofUS)}° Proof)",
        secondarySummary = "Total Pure Alcohol: ${String.format("%.2f", res.totalPureAlcohol)} ${res.volumeUnit.shortLabel}",
        notes = notes
      )
      dao.insertCalculation(item)
      _toastMessage.emit("Blend saved to batch log")
    }
  }

  fun saveStillRun(title: String, notes: String = "") {
    val res = _stillRunResult.value ?: return
    viewModelScope.launch {
      val item = SavedCalculation(
        title = title.ifBlank { "Still Run (${res.stillType.name})" },
        type = "STILL_RUN",
        inputSummary = "Wash: ${String.format("%.1f", res.washVolume)} ${res.volumeUnit.shortLabel} @ ${String.format("%.1f", res.washAbv)}% ABV (${res.stillType.label})",
        resultSummary = "Pure Alcohol: ${String.format("%.2f", res.totalPureAlcoholLiters)} L → Est. Hearts: ${String.format("%.2f", res.estimatedHeartsYieldAt40Proof)} ${res.volumeUnit.shortLabel} @ 40% ABV",
        secondarySummary = "Foreshots: ${String.format("%.0f", res.cuts.first().volumeAlcoholLiters * 1000)} mL discard",
        notes = notes
      )
      dao.insertCalculation(item)
      _toastMessage.emit("Still run yield saved to log")
    }
  }

  fun deleteCalculation(calculation: SavedCalculation) {
    viewModelScope.launch {
      dao.deleteCalculation(calculation)
      _toastMessage.emit("Calculation removed from log")
    }
  }
}
