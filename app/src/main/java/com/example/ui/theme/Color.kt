package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Vision Pro Dark Palette
val RuDarkBg = Color(0xFF030712)
val RuDarkSurface = Color(0xFF0C1222)
val RuDarkSurfaceVariant = Color(0xFF131B2E)
val RuDarkCardBorder = Color(0x26FFFFFF)
val RuDarkTextPrimary = Color(0xFFF8FAFC)
val RuDarkTextSecondary = Color(0xFF94A3B8)
val RuDarkPrimary = Color(0xFF38BDF8)
val RuDarkOnPrimary = Color(0xFF031522)

// Clean Light Palette
val RuLightBg = Color(0xFFF4F7FB)
val RuLightSurface = Color(0xFFFFFFFF)
val RuLightSurfaceVariant = Color(0xFFEAF0F8)
val RuLightCardBorder = Color(0x1F0F172A)
val RuLightTextPrimary = Color(0xFF0F172A)
val RuLightTextSecondary = Color(0xFF475569)
val RuLightPrimary = Color(0xFF0284C7)
val RuLightOnPrimary = Color(0xFFFFFFFF)

// Calm Matte Study Session Palette
val StudyDarkBg = Color(0xFF090D16)
val StudyDarkCard = Color(0xFF111827)
val StudyLightBg = Color(0xFFF6F8FC)
val StudyLightCard = Color(0xFFFFFFFF)

// Semantic Status & Stat Capsule Colors
val AccentBlueDark = Color(0xFF38BDF8)
val AccentBlueLight = Color(0xFF0284C7)

val AccentEmeraldDark = Color(0xFF34D399)
val AccentEmeraldLight = Color(0xFF059669)

val AccentAmberDark = Color(0xFFFBBF24)
val AccentAmberLight = Color(0xFFB45309)

val AccentPurpleDark = Color(0xFFA78BFA)
val AccentPurpleLight = Color(0xFF7C3AED)

val AccentRedDark = Color(0xFFFB7185)
val AccentRedLight = Color(0xFFE11D48)

// Exact subject color palette ported from index.html (subCol)
val SubjectPalette: List<Color> = listOf(
    Color(0xFF38BDF8),
    Color(0xFF34D399),
    Color(0xFFFBBF24),
    Color(0xFFF472B6),
    Color(0xFFA78BFA),
    Color(0xFFFB923C),
    Color(0xFF2DD4BF),
    Color(0xFFF87171)
)

fun subjectColor(name: String): Color {
    var h = 0L
    val t = name.trim()
    for (ch in t) {
        h = ((h * 31L) + ch.code.toLong()) and 0xFFFFFFFFL
    }
    val idx = (h % SubjectPalette.size.toLong()).toInt()
    return SubjectPalette[idx]
}

fun parseHexColor(hex: String, fallback: Color = AccentBlueDark): Color {
    return try {
        val cleaned = hex.trim().removePrefix("#")
        val parsed = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(0xFF000000L or parsed)
            8 -> Color(parsed)
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
