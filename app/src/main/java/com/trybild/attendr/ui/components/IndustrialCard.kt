package com.trybild.attendr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.trybild.attendr.ui.theme.*

// Shared Stitch "industrial card" pattern - white surface, thin border, 8dp
// radius. Used by both Admin and Employee ProfileScreen variants.
@Composable
fun IndustrialSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = StitchLabelSm,
        color = StitchOutline,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun IndustrialCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(StitchShapeXl)
            .background(IndustrialCardBackground)
            .border(1.5.dp, IndustrialCardBorder, StitchShapeXl),
        content = content
    )
}

@Composable
fun IndustrialCardRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = StitchOnSurfaceVariant)
            Text(label, style = StitchBodyMd, color = StitchOnSurface)
        }
        trailing?.invoke()
    }
}
