package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TempCorrectionResult
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.WaterAqua
import kotlin.math.abs

@Composable
fun HydrometerGaugeView(
  correctionResult: TempCorrectionResult?,
  modifier: Modifier = Modifier
) {
  val apparentAbv = correctionResult?.apparentAbv ?: 50.0
  val trueAbv = correctionResult?.trueAbv ?: 50.0
  val delta = correctionResult?.abvAdjustment ?: 0.0

  // Animate hydrometer depth based on ABV
  val animatedApparent by animateFloatAsState(
    targetValue = apparentAbv.toFloat().coerceIn(0f, 100f),
    animationSpec = tween(durationMillis = 600),
    label = "apparent_abv_anim"
  )
  val animatedTrue by animateFloatAsState(
    targetValue = trueAbv.toFloat().coerceIn(0f, 100f),
    animationSpec = tween(durationMillis = 600),
    label = "true_abv_anim"
  )

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "HYDROMETER STEM & MENISCUS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Visual cross-section in test cylinder",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Status chip
        val badgeColor = when {
          abs(delta) < 0.1 -> MaterialTheme.colorScheme.secondary
          delta < 0 -> Color(0xFFEF4444) // Warm reading high
          else -> Color(0xFF3B82F6) // Cold reading low
        }
        Box(
          modifier = Modifier
            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = when {
              abs(delta) < 0.1 -> "Calibrated"
              delta < 0 -> "${String.format("%.1f", abs(delta))}% High"
              else -> "${String.format("%.1f", abs(delta))}% Low"
            },
            color = badgeColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Custom Canvas drawing test jar + floating hydrometer stem + scale ticks
      val jarGlassColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
      val liquidColor = WaterAqua.copy(alpha = 0.25f)
      val hydrometerStemColor = MaterialTheme.colorScheme.surface
      val hydrometerBorderColor = MaterialTheme.colorScheme.outline
      val hydrometerBulbColor = MaterialTheme.colorScheme.surfaceVariant
      val hydrometerTicksColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        val w = size.width
        val h = size.height

        val jarWidth = 140.dp.toPx()
        val jarHeight = 110.dp.toPx()
        val jarLeft = (w - jarWidth) / 2f
        val jarTop = 15.dp.toPx()

        // 1. Draw Glass Test Jar
        drawRoundRect(
          color = jarGlassColor,
          topLeft = Offset(jarLeft, jarTop),
          size = Size(jarWidth, jarHeight),
          cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
          style = Stroke(width = 2.5.dp.toPx())
        )

        // Jar Lip
        drawLine(
          color = jarGlassColor,
          start = Offset(jarLeft - 8.dp.toPx(), jarTop),
          end = Offset(jarLeft + jarWidth + 8.dp.toPx(), jarTop),
          strokeWidth = 3.dp.toPx()
        )

        // 2. Liquid in Jar (fill bottom 75%)
        val liquidSurfaceY = jarTop + (jarHeight * 0.32f)
        val liquidHeight = jarHeight - (jarHeight * 0.32f)

        // Liquid background with subtle gradient
        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(liquidColor.copy(alpha = 0.15f), liquidColor.copy(alpha = 0.45f)),
            startY = liquidSurfaceY,
            endY = jarTop + jarHeight
          ),
          topLeft = Offset(jarLeft + 2.dp.toPx(), liquidSurfaceY),
          size = Size(jarWidth - 4.dp.toPx(), liquidHeight - 2.dp.toPx()),
          cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // 3. Floating Hydrometer Stem
        val stemWidth = 26.dp.toPx()
        val stemLeft = (w - stemWidth) / 2f
        val stemTop = 4.dp.toPx()
        val stemBottom = jarTop + jarHeight - 8.dp.toPx()

        // Stem Body
        drawRoundRect(
          color = hydrometerStemColor,
          topLeft = Offset(stemLeft, stemTop),
          size = Size(stemWidth, stemBottom - stemTop),
          cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        drawRoundRect(
          color = hydrometerBorderColor,
          topLeft = Offset(stemLeft, stemTop),
          size = Size(stemWidth, stemBottom - stemTop),
          cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
          style = Stroke(width = 1.5.dp.toPx())
        )

        // Hydrometer Bulb at bottom
        val bulbRadius = 18.dp.toPx()
        drawCircle(
          color = hydrometerBulbColor,
          radius = bulbRadius,
          center = Offset(w / 2f, stemBottom - 10.dp.toPx())
        )
        drawCircle(
          color = hydrometerBorderColor,
          radius = bulbRadius,
          center = Offset(w / 2f, stemBottom - 10.dp.toPx()),
          style = Stroke(width = 1.5.dp.toPx())
        )

        // Hydrometer internal graduation ticks
        for (i in 0..8) {
          val tickY = stemTop + 14.dp.toPx() + (i * 7.dp.toPx())
          val tickWidth = if (i % 2 == 0) 14.dp.toPx() else 8.dp.toPx()
          drawLine(
            color = hydrometerTicksColor,
            start = Offset((w - tickWidth) / 2f, tickY),
            end = Offset((w + tickWidth) / 2f, tickY),
            strokeWidth = 1.2.dp.toPx()
          )
        }

        // 4. Liquid Meniscus curve adhering to hydrometer stem & jar wall
        val meniscusPath = Path().apply {
          moveTo(jarLeft + 3.dp.toPx(), liquidSurfaceY - 3.dp.toPx())
          quadraticTo(
            jarLeft + 20.dp.toPx(), liquidSurfaceY,
            stemLeft, liquidSurfaceY
          )
          moveTo(stemLeft + stemWidth, liquidSurfaceY)
          quadraticTo(
            jarLeft + jarWidth - 20.dp.toPx(), liquidSurfaceY,
            jarLeft + jarWidth - 3.dp.toPx(), liquidSurfaceY - 3.dp.toPx()
          )
        }
        drawPath(
          path = meniscusPath,
          color = WaterAqua.copy(alpha = 0.8f),
          style = Stroke(width = 2.dp.toPx())
        )

        // 5. Apparent Reading Line (Where liquid surface hits stem)
        drawLine(
          color = CopperPrimary,
          start = Offset(jarLeft - 18.dp.toPx(), liquidSurfaceY),
          end = Offset(stemLeft, liquidSurfaceY),
          strokeWidth = 2.dp.toPx()
        )

        // 6. Temperature-Corrected True ABV Level Line
        // If delta < 0 (warm), true ABV is lower, so in real spirit at calibrated temp,
        // it would float higher (level line lower on stem).
        val shiftPixels = (delta * 2.2f).coerceIn(-24.0, 24.0).toFloat()
        val correctedLevelY = liquidSurfaceY + shiftPixels

        if (abs(delta) >= 0.1) {
          drawLine(
            color = if (delta < 0) Color(0xFFEF4444) else Color(0xFF3B82F6),
            start = Offset(stemLeft + stemWidth, correctedLevelY),
            end = Offset(jarLeft + jarWidth + 18.dp.toPx(), correctedLevelY),
            strokeWidth = 2.5.dp.toPx()
          )
        }
      }

      // Legend row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .background(CopperPrimary, RoundedCornerShape(2.dp))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Apparent: ${String.format("%.1f%%", apparentAbv)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .background(if (delta < 0) Color(0xFFEF4444) else Color(0xFF3B82F6), RoundedCornerShape(2.dp))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "True: ${String.format("%.1f%%", trueAbv)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}
