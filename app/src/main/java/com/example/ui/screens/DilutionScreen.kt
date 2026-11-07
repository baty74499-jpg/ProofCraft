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
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DilutionInputMode
import com.example.model.VolumeUnit
import com.example.ui.components.ContractionInfoCard
import com.example.ui.components.SaveBatchDialog
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.WaterAqua
import com.example.viewmodel.DistillersViewModel

@Composable
fun DilutionScreen(
  viewModel: DistillersViewModel,
  modifier: Modifier = Modifier
) {
  val volumeInput by viewModel.dilutionVolumeInput.collectAsState()
  val initialAbvInput by viewModel.dilutionInitialAbvInput.collectAsState()
  val targetAbvInput by viewModel.dilutionTargetAbvInput.collectAsState()
  val mode by viewModel.dilutionMode.collectAsState()
  val unit by viewModel.dilutionUnit.collectAsState()
  val applyContraction by viewModel.applyContraction.collectAsState()
  val result by viewModel.dilutionResult.collectAsState()

  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  var showSaveDialog by remember { mutableStateOf(false) }

  if (showSaveDialog && result != null) {
    SaveBatchDialog(
      defaultTitle = "Dilution Run (${String.format("%.1f", result!!.initialAbv)}% → ${String.format("%.1f", result!!.targetAbv)}%)",
      onDismiss = { showSaveDialog = false },
      onConfirm = { title, notes ->
        viewModel.saveDilutionRun(title, notes)
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
    // Header Banner
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
          imageVector = Icons.Default.WaterDrop,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(26.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = "Spirits Dilution Calculator",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Precision cutting with volume contraction compensation",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Mode Selector (Segmented Tab)
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(4.dp)
      ) {
        val spiritSelected = mode == DilutionInputMode.BY_SPIRIT_VOLUME
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (spiritSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { viewModel.setDilutionMode(DilutionInputMode.BY_SPIRIT_VOLUME) }
            .padding(vertical = 10.dp)
            .testTag("mode_by_spirit_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "By Spirit Volume",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (spiritSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (spiritSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
          )
        }

        val targetSelected = mode == DilutionInputMode.BY_TARGET_VOLUME
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (targetSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { viewModel.setDilutionMode(DilutionInputMode.BY_TARGET_VOLUME) }
            .padding(vertical = 10.dp)
            .testTag("mode_by_target_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "By Target Volume",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (targetSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (targetSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 1. Initial / Current ABV Card
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
            text = "Current Spirit Strength",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          val initDouble = initialAbvInput.toDoubleOrNull() ?: 0.0
          Text(
            text = "${String.format("%.1f", initDouble * 2.0)}° US Proof",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = initialAbvInput,
          onValueChange = viewModel::updateDilutionInitialAbv,
          label = { Text("Initial ABV (%)") },
          trailingIcon = { Text("%", modifier = Modifier.padding(end = 12.dp)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dilution_initial_abv_input")
        )

        // Slider for quick tactile fine-tuning
        val initialSliderVal = initialAbvInput.toFloatOrNull()?.coerceIn(10f, 98f) ?: 65f
        Slider(
          value = initialSliderVal,
          onValueChange = { viewModel.updateDilutionInitialAbv(String.format("%.1f", it)) },
          valueRange = 10f..98f,
          colors = SliderDefaults.colors(thumbColor = CopperPrimary, activeTrackColor = CopperPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dilution_initial_abv_slider")
        )

        // Common Cask / Still presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PresetChip(label = "62.5% Cask", onClick = { viewModel.updateDilutionInitialAbv("62.5") })
          PresetChip(label = "65% Barrel", onClick = { viewModel.updateDilutionInitialAbv("65.0") })
          PresetChip(label = "75% Hearts", onClick = { viewModel.updateDilutionInitialAbv("75.0") })
          PresetChip(label = "95% Neutral", onClick = { viewModel.updateDilutionInitialAbv("95.0") })
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Volume Card
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
            text = if (mode == DilutionInputMode.BY_SPIRIT_VOLUME) "Spirit Volume on Hand" else "Target Final Spirit Volume",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          // Unit Selector Dropdown
          var unitExpanded by remember { mutableStateOf(false) }
          Box {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.clickable { unitExpanded = true }
            ) {
              Text(
                text = unit.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
            DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
              VolumeUnit.entries.forEach { u ->
                DropdownMenuItem(
                  text = { Text("${u.label} (${u.shortLabel})") },
                  onClick = {
                    viewModel.setDilutionUnit(u)
                    unitExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = volumeInput,
          onValueChange = viewModel::updateDilutionVolume,
          label = { Text("Volume (${unit.shortLabel})") },
          trailingIcon = { Text(unit.shortLabel, modifier = Modifier.padding(end = 12.dp)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dilution_volume_input")
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Target ABV Card
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
            text = "Target ABV",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          val targetDouble = targetAbvInput.toDoubleOrNull() ?: 0.0
          Text(
            text = "${String.format("%.1f", targetDouble * 2.0)}° US Proof",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = targetAbvInput,
          onValueChange = viewModel::updateDilutionTargetAbv,
          label = { Text("Desired Final ABV (%)") },
          trailingIcon = { Text("%", modifier = Modifier.padding(end = 12.dp)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dilution_target_abv_input")
        )

        val targetSliderVal = targetAbvInput.toFloatOrNull()?.coerceIn(15f, 70f) ?: 40f
        Slider(
          value = targetSliderVal,
          onValueChange = { viewModel.updateDilutionTargetAbv(String.format("%.1f", it)) },
          valueRange = 15f..70f,
          colors = SliderDefaults.colors(thumbColor = WaterAqua, activeTrackColor = WaterAqua),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dilution_target_abv_slider")
        )

        // Commercial bottling presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PresetChip(label = "40% (80°)", onClick = { viewModel.updateDilutionTargetAbv("40.0") })
          PresetChip(label = "43% (86°)", onClick = { viewModel.updateDilutionTargetAbv("43.0") })
          PresetChip(label = "46% (92°)", onClick = { viewModel.updateDilutionTargetAbv("46.0") })
          PresetChip(label = "50% (100°)", onClick = { viewModel.updateDilutionTargetAbv("50.0") })
          PresetChip(label = "57% Navy", onClick = { viewModel.updateDilutionTargetAbv("57.0") })
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Contraction Info Card
    ContractionInfoCard(
      applyContraction = applyContraction,
      onToggleContraction = viewModel::toggleApplyContraction,
      contractionVolume = result?.contractionVolume ?: 0.0,
      volumeUnit = unit
    )

    Spacer(modifier = Modifier.height(16.dp))

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
          .testTag("dilution_result_card")
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "DILUTION SUMMARY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Primary Water To Add highlight
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .background(WaterAqua.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = WaterAqua,
                modifier = Modifier.size(28.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "Water to Add",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.2f", res.waterToAdd)} ${res.volumeUnit.shortLabel}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(16.dp))

          // Key metrics grid
          Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Final Volume",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.2f", res.finalVolume)} ${res.volumeUnit.shortLabel}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Target Strength",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.1f", res.targetAbv)}% ABV",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Proof Rating",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.1f", res.targetProofUS)}° US (${String.format("%.1f", res.targetProofUK)}° UK)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Pure Alcohol",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.2f", res.pureAlcoholVolume)} ${res.volumeUnit.shortLabel}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          if (res.appliedContraction && res.contractionVolume > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              Text(
                text = "✓ Includes ${String.format("%.3f", res.contractionVolume)} ${res.volumeUnit.shortLabel} extra water to offset contraction shrinkage.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Action buttons: Save & Copy
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { showSaveDialog = true },
              modifier = Modifier
                .weight(1f)
                .testTag("save_dilution_button")
            ) {
              Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Save Run")
            }

            OutlinedButton(
              onClick = {
                val clip = ClipData.newPlainText(
                  "Dilution Summary",
                  "Initial: ${String.format("%.2f", res.initialVolume)} ${res.volumeUnit.shortLabel} @ ${res.initialAbv}% ABV\n" +
                      "Water to Add: ${String.format("%.2f", res.waterToAdd)} ${res.volumeUnit.shortLabel}\n" +
                      "Final: ${String.format("%.2f", res.finalVolume)} ${res.volumeUnit.shortLabel} @ ${res.targetAbv}% ABV (${String.format("%.1f", res.targetProofUS)}° Proof)"
                )
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
              },
              modifier = Modifier.testTag("copy_dilution_button")
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    } else {
      // Empty / Invalid state prompt
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Enter valid ABV and volume to calculate water ratio",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Target ABV must be lower than Initial ABV",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun PresetChip(
  label: String,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    modifier = Modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
  }
}
