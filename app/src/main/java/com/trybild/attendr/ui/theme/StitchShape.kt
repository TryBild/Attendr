package com.trybild.attendr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// ── Stitch redesign — shape tokens ─────────────────────────────────────────
// Matches the mockup's Tailwind borderRadius scale (default/lg/xl/full).
// Scoped to Login + Profile screens only.
val StitchShapeDefault = RoundedCornerShape(2.dp)
val StitchShapeLg      = RoundedCornerShape(4.dp)
val StitchShapeXl      = RoundedCornerShape(8.dp)
val StitchShapeFull    = RoundedCornerShape(12.dp)
