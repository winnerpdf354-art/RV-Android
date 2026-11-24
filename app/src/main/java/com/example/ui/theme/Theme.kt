package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.BgStyle
import com.example.data.CardStyle
import com.example.data.LearningStatus
import java.util.Calendar

@Immutable
data class RuExtendedColors(
    val isDark: Boolean,
    val bgStyle: BgStyle,
    val cardStyle: CardStyle,
    val blue: Color,
    val emerald: Color,
    val amber: Color,
    val purple: Color,
    val red: Color,
    val cardFill: Color,
    val cardBorder: Color,
    val cardElevatedFill: Color,
    val subtleFill: Color,
    val mutedText: Color,
    val studyBg: Color,
    val studyCard: Color
) {
    fun statusColor(status: LearningStatus): Color = when (status) {
        LearningStatus.EASY -> emerald
        LearningStatus.MEDIUM -> amber
        LearningStatus.HARD -> red
    }
}

val LocalRuColors = staticCompositionLocalOf {
    buildRuExtendedColors(isDark = true, bgStyle = BgStyle.GRAIN_GLOW, cardStyle = CardStyle.FROSTED)
}

fun buildRuExtendedColors(
    isDark: Boolean,
    bgStyle: BgStyle,
    cardStyle: CardStyle
): RuExtendedColors {
    val cardFill = when (cardStyle) {
        CardStyle.FROSTED -> if (isDark) Color(0xCC11192B) else Color(0xE6FFFFFF)
        CardStyle.GLASS -> if (isDark) Color(0x99121B2E) else Color(0xC7FFFFFF)
        CardStyle.CLEAR_GLASS -> if (isDark) Color(0x47111B30) else Color(0x88FFFFFF)
        CardStyle.SOLID -> if (isDark) Color(0xFF0E1525) else Color(0xFFFFFFFF)
    }
    val cardBorder = when (cardStyle) {
        CardStyle.FROSTED -> if (isDark) Color(0x2694A3B8) else Color(0x220F172A)
        CardStyle.GLASS -> if (isDark) Color(0x33CBD5E1) else Color(0x2A0F172A)
        CardStyle.CLEAR_GLASS -> if (isDark) Color(0x3D94A3B8) else Color(0x300F172A)
        CardStyle.SOLID -> if (isDark) Color(0x1EFFFFFF) else Color(0x1A0F172A)
    }
    return RuExtendedColors(
        isDark = isDark,
        bgStyle = bgStyle,
        cardStyle = cardStyle,
        blue = if (isDark) AccentBlueDark else AccentBlueLight,
        emerald = if (isDark) AccentEmeraldDark else AccentEmeraldLight,
        amber = if (isDark) AccentAmberDark else AccentAmberLight,
        purple = if (isDark) AccentPurpleDark else AccentPurpleLight,
        red = if (isDark) AccentRedDark else AccentRedLight,
        cardFill = cardFill,
        cardBorder = cardBorder,
        cardElevatedFill = if (isDark) Color(0xFF152036) else Color(0xFFF1F5FB),
        subtleFill = if (isDark) Color(0x16FFFFFF) else Color(0x0E0F172A),
        mutedText = if (isDark) RuDarkTextSecondary else RuLightTextSecondary,
        studyBg = if (isDark) StudyDarkBg else StudyLightBg,
        studyCard = if (isDark) StudyDarkCard else StudyLightCard
    )
}

private val RuDarkColorScheme = darkColorScheme(
    primary = RuDarkPrimary,
    onPrimary = RuDarkOnPrimary,
    primaryContainer = Color(0xFF0C354D),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = AccentEmeraldDark,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = AccentPurpleDark,
    onTertiary = Color(0xFF2E1065),
    background = RuDarkBg,
    onBackground = RuDarkTextPrimary,
    surface = RuDarkSurface,
    onSurface = RuDarkTextPrimary,
    surfaceVariant = RuDarkSurfaceVariant,
    onSurfaceVariant = RuDarkTextSecondary,
    outline = RuDarkCardBorder,
    error = AccentRedDark,
    onError = Color(0xFF4C0519)
)

private val RuLightColorScheme = lightColorScheme(
    primary = RuLightPrimary,
    onPrimary = RuLightOnPrimary,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF075985),
    secondary = AccentEmeraldLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = AccentPurpleLight,
    onTertiary = Color.White,
    background = RuLightBg,
    onBackground = RuLightTextPrimary,
    surface = RuLightSurface,
    onSurface = RuLightTextPrimary,
    surfaceVariant = RuLightSurfaceVariant,
    onSurfaceVariant = RuLightTextSecondary,
    outline = RuLightCardBorder,
    error = AccentRedLight,
    onError = Color.White
)

@Composable
fun RevisionUniverseTheme(
    isDarkTheme: Boolean = true,
    bgStyle: BgStyle = BgStyle.GRAIN_GLOW,
    cardStyle: CardStyle = CardStyle.FROSTED,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) RuDarkColorScheme else RuLightColorScheme
    val extended = buildRuExtendedColors(isDarkTheme, bgStyle, cardStyle)
    CompositionLocalProvider(LocalRuColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun AppBackgroundLayer(
    modifier: Modifier = Modifier,
    bgStyle: BgStyle = LocalRuColors.current.bgStyle,
    isDark: Boolean = LocalRuColors.current.isDark,
    content: @Composable BoxScope.() -> Unit
) {
    val baseBg = if (isDark) RuDarkBg else RuLightBg
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val orbPrimary = when {
        hour in 5..11 -> if (isDark) Color(0x3838BDF8) else Color(0x280284C7)
        hour in 12..17 -> if (isDark) Color(0x3334D399) else Color(0x24059669)
        hour in 18..21 -> if (isDark) Color(0x35FB923C) else Color(0x24EA580C)
        else -> if (isDark) Color(0x38A78BFA) else Color(0x267C3AED)
    }
    val orbSecondary = when {
        hour in 5..11 -> if (isDark) Color(0x2A34D399) else Color(0x1E059669)
        hour in 12..17 -> if (isDark) Color(0x2A38BDF8) else Color(0x1E0284C7)
        hour in 18..21 -> if (isDark) Color(0x2AF472B6) else Color(0x1EDB2777)
        else -> if (isDark) Color(0x2B38BDF8) else Color(0x1E0284C7)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (bgStyle) {
                BgStyle.GRAIN_GLOW -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(orbPrimary, Color.Transparent),
                            center = Offset(size.width * 0.15f, size.height * 0.08f),
                            radius = size.width * 0.85f
                        ),
                        radius = size.width * 0.85f,
                        center = Offset(size.width * 0.15f, size.height * 0.08f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(orbSecondary, Color.Transparent),
                            center = Offset(size.width * 0.9f, size.height * 0.65f),
                            radius = size.width * 0.75f
                        ),
                        radius = size.width * 0.75f,
                        center = Offset(size.width * 0.9f, size.height * 0.65f)
                    )
                    // Subtle micro-grain stipple
                    val dotColor = if (isDark) Color(0x0DFFFFFF) else Color(0x0A0F172A)
                    val step = 36f
                    var y = 18f
                    while (y < size.height) {
                        var x = ((y.toInt() * 17) % 36).toFloat()
                        while (x < size.width) {
                            drawCircle(dotColor, radius = 0.85f, center = Offset(x, y))
                            x += step
                        }
                        y += step
                    }
                }
                BgStyle.SUBTLE_GRAIN -> {
                    val dotColor = if (isDark) Color(0x11FFFFFF) else Color(0x0C0F172A)
                    val step = 28f
                    var y = 14f
                    while (y < size.height) {
                        var x = ((y.toInt() * 13) % 28).toFloat()
                        while (x < size.width) {
                            drawCircle(dotColor, radius = 0.9f, center = Offset(x, y))
                            x += step
                        }
                        y += step
                    }
                }
                BgStyle.PURE_CLEAN -> Unit
                BgStyle.DOT_GRID -> {
                    val gridColor = if (isDark) Color(0x1A94A3B8) else Color(0x2264748B)
                    val step = 42f
                    var y = 21f
                    while (y < size.height) {
                        var x = 21f
                        while (x < size.width) {
                            drawCircle(gridColor, radius = 1.6f, center = Offset(x, y))
                            x += step
                        }
                        y += step
                    }
                }
                BgStyle.FLOATING_ORBS -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(orbPrimary.copy(alpha = orbPrimary.alpha * 1.35f), Color.Transparent),
                            center = Offset(size.width * 0.82f, size.height * 0.14f),
                            radius = size.width * 0.68f
                        ),
                        radius = size.width * 0.68f,
                        center = Offset(size.width * 0.82f, size.height * 0.14f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(orbSecondary.copy(alpha = orbSecondary.alpha * 1.35f), Color.Transparent),
                            center = Offset(size.width * 0.18f, size.height * 0.52f),
                            radius = size.width * 0.72f
                        ),
                        radius = size.width * 0.72f,
                        center = Offset(size.width * 0.18f, size.height * 0.52f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isDark) Color(0x26FBBF24) else Color(0x1CD97706),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.7f, size.height * 0.88f),
                            radius = size.width * 0.6f
                        ),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * 0.7f, size.height * 0.88f)
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun RuCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    containerColor: Color = LocalRuColors.current.cardFill,
    borderColor: Color = LocalRuColors.current.cardBorder,
    borderWidth: Dp = 1.dp,
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = tonalElevation,
        content = content
    )
}
