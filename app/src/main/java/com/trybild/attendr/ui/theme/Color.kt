package com.trybild.attendr.ui.theme

import androidx.compose.ui.graphics.Color

// Brand
val AttendrNavy        = Color(0xFF1B3A7B)
val AttendrNavyDark    = Color(0xFF152F66)
val AttendrNavyLight   = Color(0xFF2952A3)

// Glass surfaces — white-based
val AttendrBackground  = Color(0xFFF0F4FF)   // very light blue-tinted white page bg
val AttendrSurface     = Color(0xFFFFFFFF)

// Glass card tokens
val GlassSurface       = Color(0x99FFFFFF)   // white 60% alpha — main glass card bg
val GlassBorder        = Color(0x4D1B3A7B)   // navy 30% alpha — card border
val GlassSurfaceHover  = Color(0xB3FFFFFF)   // white 70% alpha — pressed/active state

// Text
val AttendrTextPrimary   = Color(0xFF0D1B35)
val AttendrTextSecondary = Color(0xFF4A5568)
val AttendrTextHint      = Color(0xFF9AA5B4)

// Semantic
val AttendrBorder   = Color(0xFFCDD5E0)
val AttendrError    = Color(0xFFD32F2F)
val AttendrErrorBg  = Color(0xFFFFEBEE)
val AttendrSuccess  = Color(0xFF2E7D32)
val AttendrDivider  = Color(0xFFE8EDF5)

// Gradient stops for animated background
val BgGradientTop    = Color(0xFFE8F0FE)
val BgGradientMid    = Color(0xFFF0F4FF)
val BgGradientBottom = Color(0xFFFFFFFF)

// Accent orbs (subtle, light mode)
val OrbBlue    = Color(0x331B3A7B)   // navy 20% alpha
val OrbIndigo  = Color(0x261E40AF)   // indigo 15% alpha
val OrbSky     = Color(0x1A3B82F6)   // sky 10% alpha

// ── Stitch redesign — Material 3 "industrial" tokens ──────────────────────
// Scoped to Login + Profile screens only, per the Stitch mockups. Every
// other screen keeps the navy-glassmorphism tokens above untouched.
val StitchPrimary               = Color(0xFF003D9B)
val StitchPrimaryContainer      = Color(0xFF0052CC)
val StitchOnPrimary             = Color(0xFFFFFFFF)
val StitchOnPrimaryContainer    = Color(0xFFC4D2FF)
val StitchSecondary             = Color(0xFF006E2F)
val StitchSecondaryContainer    = Color(0xFF6BFF8F)
val StitchOnSecondaryContainer  = Color(0xFF007432)
val StitchTertiary              = Color(0xFF603B00)
val StitchTertiaryContainer     = Color(0xFF805000)
val StitchError                 = Color(0xFFBA1A1A)
val StitchOnError               = Color(0xFFFFFFFF)
val StitchErrorContainer        = Color(0xFFFFDAD6)
val StitchOnErrorContainer      = Color(0xFF93000A)
val StitchBackground            = Color(0xFFF8F9FF)
val StitchOnBackground          = Color(0xFF0D1C2E)
val StitchSurface               = Color(0xFFF8F9FF)
val StitchSurfaceBright         = Color(0xFFF8F9FF)
val StitchSurfaceDim            = Color(0xFFCCDBF3)
val StitchSurfaceContainerLowest  = Color(0xFFFFFFFF)
val StitchSurfaceContainerLow     = Color(0xFFEFF4FF)
val StitchSurfaceContainer        = Color(0xFFE6EEFF)
val StitchSurfaceContainerHigh    = Color(0xFFDCE9FF)
val StitchSurfaceContainerHighest = Color(0xFFD5E3FC)
val StitchOnSurface             = Color(0xFF0D1C2E)
val StitchOnSurfaceVariant      = Color(0xFF434654)
val StitchOutline               = Color(0xFF737685)
val StitchOutlineVariant        = Color(0xFFC3C6D6)
val StitchInverseSurface        = Color(0xFF233144)
val StitchInverseOnSurface      = Color(0xFFEAF1FF)
