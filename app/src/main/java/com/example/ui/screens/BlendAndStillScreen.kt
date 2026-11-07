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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PropaneTank
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StillType
import com.example.model.VolumeUnit
import com.example.ui.components.SaveBatchDialog
import com.example.ui.theme.CutForeshotsRed
import com.example.ui.theme.CutHeadsOrange
import com.example.ui.theme.CutHeartsGreen
import com.example.ui.theme.CutTailsBronze
import com.example.viewmodel.DistillersViewModel

@Composable
fun BlendAndStillScreen(
  viewModel: DistillersViewModel,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableStateOf(0) } // 0: Blend & Fortify, 1: Still Run Yield

  val context = LocalContext.current
  var showSaveDialog by remember { mutableStateOf(false) }

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
          imageVector = if (selectedSubTab == 0) Icons.Default.Tune else Icons.Default.PropaneTank,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(26.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = if (selectedSubTab == 0) "Blending & Fortification" else "Still Run & Cuts Yield",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (selectedSubTab == 0) "Mix batches or spike ABV with neutral spirit" else "Estimate pure alcohol, cuts, and bottling yield",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Sub-tab switcher
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
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selectedSubTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { selectedSubTab = 0 }
            .padding(vertical = 10.dp)
            .testTag("tab_blend_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Spirit Blending",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
            color = if (selectedSubTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selectedSubTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { selectedSubTab = 1 }
            .padding(vertical = 10.dp)
            .testTag("tab_still_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Still Run Yield",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
            color = if (selectedSubTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (selectedSubTab == 0) {
      // BLENDING & FORTIFICATION CONTENT
      BlendingSection(viewModel = viewModel, onSave = { showSaveDialog = true })
    } else {
      // STILL RUN CONTENT
      StillRunSection(viewModel = viewModel, onSave = { showSaveDialog = true })
    }

    Spacer(modifier = Modifier.height(24.dp))
  }

  if (showSaveDialog) {
    if (selectedSubTab == 0) {
      val blendRes = viewModel.blendResult.collectAsState().value
      SaveBatchDialog(
        defaultTitle = "Spirit Blend (${String.format("%.1f", blendRes?.finalAbv ?: 40.0)}% ABV)",
        onDismiss = { showSaveDialog = false },
        onConfirm = { title, notes ->
          viewModel.saveBlendRun(title, notes)
          showSaveDialog = false
        }
      )
    } else {
      val stillRes = viewModel.stillRunResult.collectAsState().value
      SaveBatchDialog(
        defaultTitle = "Still Run (${stillRes?.stillType?.name ?: "Run"})",
        onDismiss = { showSaveDialog = false },
        onConfirm = { title, notes ->
          viewModel.saveStillRun(title, notes)
          showSaveDialog = false
        }
      )
    }
  }
}

@Composable
private fun BlendingSection(
  viewModel: DistillersViewModel,
  onSave: () -> Unit
) {
  val blendMode by viewModel.blendMode.collectAsState()
  val volA by viewModel.blendVolumeA.collectAsState()
  val abvA by viewModel.blendAbvA.collectAsState()
  val volB by viewModel.blendVolumeB.collectAsState()
  val abvB by viewModel.blendAbvB.collectAsState()
  val unit by viewModel.blendUnit.collectAsState()
  val blendResult by viewModel.blendResult.collectAsState()

  val fortTargetAbv by viewModel.fortifyTargetAbv.collectAsState()
  val fortSpiritAbv by viewModel.fortifySpiritAbv.collectAsState()
  val fortificationResult by viewModel.fortificationResult.collectAsState()

  val context = LocalContext.current

  // Secondary mode selector: Blend Two vs Fortify
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = if (blendMode == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier
        .weight(1f)
        .clickable { viewModel.setBlendMode(0) }
    ) {
      Text(
        text = "Blend Two Spirits",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (blendMode == 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
      )
    }

    Surface(
      shape = RoundedCornerShape(8.dp),
      color = if (blendMode == 1) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier
        .weight(1f)
        .clickable { viewModel.setBlendMode(1) }
    ) {
      Text(
        text = "Fortify / Spike ABV",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (blendMode == 1) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
      )
    }
  }

  Spacer(modifier = Modifier.height(14.dp))

  if (blendMode == 0) {
    // BLEND TWO SPIRITS
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Spirit A (Base)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = volA,
            onValueChange = viewModel::updateBlendVolumeA,
            label = { Text("Volume (${unit.shortLabel})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            modifier = Modifier
              .weight(1f)
              .testTag("blend_vol_a_input")
          )
          OutlinedTextField(
            value = abvA,
            onValueChange = viewModel::updateBlendAbvA,
            label = { Text("ABV (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            modifier = Modifier
              .weight(1f)
              .testTag("blend_abv_a_input")
          )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(14.dp))

        Text("Spirit B (Modifier)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = volB,
            onValueChange = viewModel::updateBlendVolumeB,
            label = { Text("Volume (${unit.shortLabel})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            modifier = Modifier
              .weight(1f)
              .testTag("blend_vol_b_input")
          )
          OutlinedTextField(
            value = abvB,
            onValueChange = viewModel::updateBlendAbvB,
            label = { Text("ABV (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            modifier = Modifier
              .weight(1f)
              .testTag("blend_abv_b_input")
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Results Card
    if (blendResult != null) {
      val res = blendResult!!
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "BLENDED SPIRIT RESULT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "${String.format("%.1f", res.finalAbv)}% ABV",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "${String.format("%.1f", res.finalProofUS)}° US Proof",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Total Volume", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("${String.format("%.2f", res.finalVolume)} ${res.volumeUnit.shortLabel}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
              Text("Pure Alcohol", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("${String.format("%.2f", res.totalPureAlcohol)} ${res.volumeUnit.shortLabel}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) {
              Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save Blend")
            }
            OutlinedButton(
              onClick = {
                val clip = ClipData.newPlainText(
                  "Blend Summary",
                  "A: ${String.format("%.2f", res.volumeA)} ${res.volumeUnit.shortLabel} @ ${res.abvA}%\n" +
                      "B: ${String.format("%.2f", res.volumeB)} ${res.volumeUnit.shortLabel} @ ${res.abvB}%\n" +
                      "Blended: ${String.format("%.2f", res.finalVolume)} ${res.volumeUnit.shortLabel} @ ${String.format("%.1f", res.finalAbv)}% ABV (${String.format("%.1f", res.finalProofUS)}° Proof)"
                )
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
              }
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }
  } else {
    // FORTIFICATION MODE
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Current Batch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = volA,
            onValueChange = viewModel::updateBlendVolumeA,
            label = { Text("Volume (${unit.shortLabel})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = abvA,
            onValueChange = viewModel::updateBlendAbvA,
            label = { Text("Current ABV (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(14.dp))

        Text("Target & Fortifier Strength", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = fortSpiritAbv,
            onValueChange = viewModel::updateFortifySpiritAbv,
            label = { Text("Fortifier ABV (%)") },
            placeholder = { Text("95.0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = fortTargetAbv,
            onValueChange = viewModel::updateFortifyTargetAbv,
            label = { Text("Target ABV (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (fortificationResult != null) {
      val res = fortificationResult!!
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "FORTIFICATION REQUIRED",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Add ${String.format("%.2f", res.fortifierVolumeNeeded)} ${res.volumeUnit.shortLabel}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "of ${String.format("%.1f", res.fortifierAbv)}% Spirit to reach ${String.format("%.1f", res.targetAbv)}% ABV",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "New Final Batch Volume: ${String.format("%.2f", res.finalTotalVolume)} ${res.volumeUnit.shortLabel}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun StillRunSection(
  viewModel: DistillersViewModel,
  onSave: () -> Unit
) {
  val washVol by viewModel.washVolumeInput.collectAsState()
  val washAbv by viewModel.washAbvInput.collectAsState()
  val stillType by viewModel.stillType.collectAsState()
  val unit by viewModel.stillUnit.collectAsState()
  val result by viewModel.stillRunResult.collectAsState()

  // Input Card
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("Boiler Charge / Wash", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

      Spacer(modifier = Modifier.height(8.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = washVol,
          onValueChange = viewModel::updateWashVolume,
          label = { Text("Wash Volume (${unit.shortLabel})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = washAbv,
          onValueChange = viewModel::updateWashAbv,
          label = { Text("Wash ABV (%)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text("Distillation Apparatus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

      Spacer(modifier = Modifier.height(6.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (stillType == StillType.POT_STILL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .weight(1f)
            .clickable { viewModel.setStillType(StillType.POT_STILL) }
        ) {
          Text(
            text = "Pot Still (Whiskey)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (stillType == StillType.POT_STILL) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (stillType == StillType.REFLUX_COLUMN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .weight(1f)
            .clickable { viewModel.setStillType(StillType.REFLUX_COLUMN) }
        ) {
          Text(
            text = "Reflux (Vodka/Gin)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (stillType == StillType.REFLUX_COLUMN) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
          )
        }
      }
    }
  }

  Spacer(modifier = Modifier.height(14.dp))

  if (result != null) {
    val res = result!!
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text(
          text = "ESTIMATED CUTS & YIELD",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "~${String.format("%.2f", res.estimatedHeartsYieldAt40Proof)} ${res.volumeUnit.shortLabel}",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "Estimated finished spirits yield at standard 40% ABV (80° Proof)",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Cut breakdown visual bars
        res.cuts.forEach { cut ->
          val cutColor = when {
            cut.name.startsWith("Foreshots") -> CutForeshotsRed
            cut.name.startsWith("Heads") -> CutHeadsOrange
            cut.name.startsWith("Hearts") -> CutHeartsGreen
            else -> CutTailsBronze
          }

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
              .border(1.dp, cutColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(cutColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(cut.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              }
              Text(
                text = "${String.format("%.1f", cut.percentageOfAlcohol)}% of PA",
                style = MaterialTheme.typography.labelSmall,
                color = cutColor,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Est: ${String.format("%.0f", cut.estimatedLiquidVolume * 1000)} mL (${cut.collectionAbvRange}) • ${cut.action}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = cut.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
          Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Save Still Run")
        }
      }
    }
  }
}
