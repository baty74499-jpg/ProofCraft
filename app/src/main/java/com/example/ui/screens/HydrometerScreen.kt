package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalibrationTemp
import com.example.model.TempUnit
import com.example.ui.components.HydrometerGaugeView
import com.example.ui.components.SaveBatchDialog
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.WaterAqua
import com.example.viewmodel.DistillersViewModel
import kotlin.math.abs

@Composable
fun HydrometerScreen(
  viewModel: DistillersViewModel,
  onNavigateToDilution: () -> Unit,
  modifier: Modifier = Modifier
) {
  val apparentAbvInput by viewModel.apparentAbvInput.collectAsState()
  val measuredTempInput by viewModel.measuredTempInput.collectAsState()
  val tempUnit by viewModel.tempUnit.collectAsState()
  val calibrationTemp by viewModel.calibrationTemp.collectAsState()
  val result by viewModel.tempCorrectionResult.collectAsState()

  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  var showSaveDialog by remember { mutableStateOf(false) }

  if (showSaveDialog && result != null) {
    SaveBatchDialog(
      defaultTitle = "Hydrometer Check (${String.format("%.1f", result!!.trueAbv)}% True ABV)",
      onDismiss = { showSaveDialog = false },
      onConfirm = { title, notes ->
        viewModel.saveHydrometerRun(title, notes)
        showSaveDialog = false
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.DeviceThermostat,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(26.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = "Hydrometer Temp Correction",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Calculates true spirit strength at reference temperature",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Calibration Standard Selector Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Hydrometer Calibration Standard",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          var calDropdownExpanded by remember { mutableStateOf(false) }
          Box {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.clickable { calDropdownExpanded = true }
            ) {
              Text(
                text = if (calibrationTemp == CalibrationTemp.OIML_20C) "20°C (68°F)" else "60°F (15.6°C)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            DropdownMenu(expanded = calDropdownExpanded, onDismissRequest = { calDropdownExpanded = false }) {
              CalibrationTemp.entries.forEach { cal ->
                DropdownMenuItem(
                  text = { Text(cal.label) },
                  onClick = {
                    viewModel.setCalibrationTemp(cal)
                    calDropdownExpanded = false
                  }
                )
              }
            }
          }
        }
        Text(
          text = if (calibrationTemp == CalibrationTemp.OIML_20C) {
            "Standard for European & international alcoholmeters"
          } else {
            "Standard for US TTB / Gauging Manual proof hydrometers"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          fontSize = 11.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Apparent Hydrometer Reading Input Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Indicated Hydrometer Reading",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          val appDouble = apparentAbvInput.toDoubleOrNull() ?: 0.0
          Text(
            text = "${String.format("%.1f", appDouble * 2.0)}° Indicated Proof",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = apparentAbvInput,
          onValueChange = viewModel::updateApparentAbv,
          label = { Text("Apparent Alcohol Strength (% ABV)") },
          trailingIcon = { Text("% ABV", modifier = Modifier.padding(end = 12.dp)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("hydrometer_apparent_abv_input")
        )

        val appSliderVal = apparentAbvInput.toFloatOrNull()?.coerceIn(0f, 100f) ?: 50f
        Slider(
          value = appSliderVal,
          onValueChange = { viewModel.updateApparentAbv(String.format("%.1f", it)) },
          valueRange = 0f..100f,
          colors = SliderDefaults.colors(thumbColor = CopperPrimary, activeTrackColor = CopperPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("hydrometer_apparent_slider")
        )

        // Presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PresetChip(label = "40% ABV", onClick = { viewModel.updateApparentAbv("40.0") })
          PresetChip(label = "50% ABV", onClick = { viewModel.updateApparentAbv("50.0") })
          PresetChip(label = "65% ABV", onClick = { viewModel.updateApparentAbv("65.0") })
          PresetChip(label = "80% ABV", onClick = { viewModel.updateApparentAbv("80.0") })
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Liquid Temperature Input Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Liquid Temperature",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          // Temp Unit Switcher (°C / °F)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(modifier = Modifier.padding(2.dp)) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (tempUnit == TempUnit.CELSIUS) MaterialTheme.colorScheme.primary else Color.Transparent)
                  .clickable { viewModel.setTempUnit(TempUnit.CELSIUS) }
                  .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "°C",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (tempUnit == TempUnit.CELSIUS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (tempUnit == TempUnit.FAHRENHEIT) MaterialTheme.colorScheme.primary else Color.Transparent)
                  .clickable { viewModel.setTempUnit(TempUnit.FAHRENHEIT) }
                  .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "°F",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (tempUnit == TempUnit.FAHRENHEIT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = measuredTempInput,
          onValueChange = viewModel::updateMeasuredTemp,
          label = { Text("Measured Temperature (${tempUnit.symbol})") },
          trailingIcon = { Text(tempUnit.symbol, modifier = Modifier.padding(end = 12.dp)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("hydrometer_temperature_input")
        )

        val tempRangeMin = if (tempUnit == TempUnit.CELSIUS) 5f else 40f
        val tempRangeMax = if (tempUnit == TempUnit.CELSIUS) 40f else 105f
        val tempVal = measuredTempInput.toFloatOrNull()?.coerceIn(tempRangeMin, tempRangeMax) ?: 20f

        Slider(
          value = tempVal,
          onValueChange = { viewModel.updateMeasuredTemp(String.format("%.1f", it)) },
          valueRange = tempRangeMin..tempRangeMax,
          colors = SliderDefaults.colors(thumbColor = WaterAqua, activeTrackColor = WaterAqua),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("hydrometer_temp_slider")
        )

        // Common temp presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (tempUnit == TempUnit.CELSIUS) {
            PresetChip(label = "15°C Cold", onClick = { viewModel.updateMeasuredTemp("15.0") })
            PresetChip(label = "20°C Standard", onClick = { viewModel.updateMeasuredTemp("20.0") })
            PresetChip(label = "25°C Room", onClick = { viewModel.updateMeasuredTemp("25.0") })
            PresetChip(label = "30°C Warm", onClick = { viewModel.updateMeasuredTemp("30.0") })
          } else {
            PresetChip(label = "60°F TTB", onClick = { viewModel.updateMeasuredTemp("60.0") })
            PresetChip(label = "68°F Standard", onClick = { viewModel.updateMeasuredTemp("68.0") })
            PresetChip(label = "77°F Room", onClick = { viewModel.updateMeasuredTemp("77.0") })
            PresetChip(label = "86°F Warm", onClick = { viewModel.updateMeasuredTemp("86.0") })
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive Hydrometer Gauge Visual
    HydrometerGaugeView(correctionResult = result)

    Spacer(modifier = Modifier.height(14.dp))

    // HERO RESULTS CARD
    if (result != null) {
      val res = result!!
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hydrometer_result_card")
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TEMPERATURE-CORRECTED STRENGTH",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              letterSpacing = 1.2.sp
            )

            // Adjustment pill
            val adjColor = when {
              abs(res.abvAdjustment) < 0.1 -> MaterialTheme.colorScheme.secondary
              res.abvAdjustment < 0 -> Color(0xFFEF4444)
              else -> Color(0xFF3B82F6)
            }
            Box(
              modifier = Modifier
                .background(adjColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .border(1.dp, adjColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "${if (res.abvAdjustment >= 0) "+" else ""}${String.format("%.2f", res.abvAdjustment)}% ABV",
                color = adjColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Big True ABV Display
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "True Alcohol by Volume",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.2f", res.trueAbv)}% ABV",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(14.dp))

          // Key metrics row
          Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "True US Proof",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.1f", res.trueProofUS)}° Proof",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "True UK Sikes Proof",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.1f", res.trueProofUK)}° Sikes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Explanation card
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
              .padding(10.dp)
          ) {
            Row(verticalAlignment = Alignment.Top) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                  .size(16.dp)
                  .padding(top = 2.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = res.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Direct Transfer to Dilution action!
          Button(
            onClick = { viewModel.applyTrueAbvToDilution(onNavigateToDilution) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("apply_true_abv_to_dilution_button")
          ) {
            Text("Use ${String.format("%.1f", res.trueAbv)}% in Dilution Calculator")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { showSaveDialog = true },
              modifier = Modifier
                .weight(1f)
                .testTag("save_hydrometer_button")
            ) {
              Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save to Log")
            }

            OutlinedButton(
              onClick = {
                val clip = ClipData.newPlainText(
                  "Hydrometer Temp Correction",
                  "Reading: ${String.format("%.1f", res.apparentAbv)}% ABV at ${String.format("%.1f", res.measuredTemp)}${res.tempUnit.symbol}\n" +
                      "Standard: ${res.calibrationTemp.displayStr}\n" +
                      "True ABV: ${String.format("%.2f", res.trueAbv)}% ABV (${String.format("%.1f", res.trueProofUS)}° Proof)\n" +
                      "Adjustment: ${if (res.abvAdjustment >= 0) "+" else ""}${String.format("%.2f", res.abvAdjustment)}% ABV"
                )
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
              },
              modifier = Modifier.testTag("copy_hydrometer_button")
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
