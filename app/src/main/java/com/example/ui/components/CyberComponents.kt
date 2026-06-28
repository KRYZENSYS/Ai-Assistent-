package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberGlow
import kotlin.math.sin

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .drawBehind {
                // Drop subtle atmospheric back shadow
                drawRect(
                    color = glowColor.copy(alpha = 0.05f),
                    size = size
                )
            }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.1f))
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
        ),
        shape = RoundedCornerShape(12.dp),
        content = content
    )
}

@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "cyber_button",
    neonColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.4f
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(8.dp))
            .background(neonColor.copy(alpha = 0.05f * alpha))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(neonColor.copy(alpha = 0.8f * alpha), neonColor.copy(alpha = 0.2f * alpha))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = neonColor.copy(alpha = alpha),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun CyberHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Modern sci-fi accent separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun CyberWaveform(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    pulseSpeedMs: Int = 1200,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")
    
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseShift"
    )

    val amplitudeMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeedMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Amplitude"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val path = Path()

        path.moveTo(0f, centerY)
        
        val pointsCount = 100
        val baseAmplitude = if (isActive) centerY * 0.7f * amplitudeMultiplier else 2f
        
        for (i in 0..pointsCount) {
            val x = (i / pointsCount.toFloat()) * width
            // Compound sine waves for futuristic cyber modulation feel
            val angle1 = (i / pointsCount.toFloat()) * 4 * Math.PI + phaseShift
            val angle2 = (i / pointsCount.toFloat()) * 8 * Math.PI - phaseShift * 1.5
            
            val y = centerY + (sin(angle1) * baseAmplitude * 0.7f + sin(angle2) * baseAmplitude * 0.3f).toFloat()
            path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
        
        // Secondary dimmer shadow wave
        val shadowPath = Path()
        shadowPath.moveTo(0f, centerY)
        for (i in 0..pointsCount) {
            val x = (i / pointsCount.toFloat()) * width
            val angle1 = (i / pointsCount.toFloat()) * 4 * Math.PI + phaseShift + Math.PI / 4
            val y = centerY + (sin(angle1) * baseAmplitude * 0.5f).toFloat()
            shadowPath.lineTo(x, y)
        }
        
        drawPath(
            path = shadowPath,
            color = color.copy(alpha = 0.3f),
            style = Stroke(
                width = 1.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
fun CyberStatusIndicator(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.tertiary,
    inactiveColor: Color = Color.Gray
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val transition = rememberInfiniteTransition(label = "LedBlink")
        val alpha by if (isActive) {
            transition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Blink"
            )
        } else {
            remember { mutableStateOf(0.4f) }
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background((if (isActive) activeColor else inactiveColor).copy(alpha = alpha))
                .border(
                    width = 1.dp,
                    color = if (isActive) activeColor else inactiveColor,
                    shape = RoundedCornerShape(50)
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label.uppercase(),
            color = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
