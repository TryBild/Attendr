package com.trybild.attendr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val AttendrTypography = Typography(
    displayLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)
)

// ── Stitch redesign — typography scale ─────────────────────────────────────
// Scoped to Login + Profile screens only. Values match the Stitch mockup's
// Tailwind fontSize tokens exactly (size/lineHeight/letterSpacing/weight).
val StitchDisplay = TextStyle(
    fontFamily = InterFontFamily, fontWeight = FontWeight.ExtraBold,
    fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.02).em
)
val StitchHeadlineLg = TextStyle(
    fontFamily = InterFontFamily, fontWeight = FontWeight.Bold,
    fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.01).em
)
val StitchHeadlineLgMobile = TextStyle(
    fontFamily = InterFontFamily, fontWeight = FontWeight.Bold,
    fontSize = 24.sp, lineHeight = 30.sp
)
val StitchHeadlineMd = TextStyle(
    fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp, lineHeight = 28.sp
)
val StitchBodyLg = TextStyle(
    fontFamily = InterFontFamily, fontWeight = FontWeight.Normal,
    fontSize = 18.sp, lineHeight = 26.sp
)
val StitchBodyMd = TextStyle(
    fontFamily = InterFontFamily, fontWeight = FontWeight.Normal,
    fontSize = 16.sp, lineHeight = 24.sp
)
val StitchLabelBold = TextStyle(
    fontFamily = InterFontFamily, fontWeight = FontWeight.Bold,
    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.05.em
)
val StitchLabelSm = TextStyle(
    fontFamily = InterFontFamily, fontWeight = FontWeight.Medium,
    fontSize = 12.sp, lineHeight = 16.sp
)
