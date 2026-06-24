package com.trybild.attendr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trybild.attendr.ui.home.GeofenceBadge

private val BadgeGreen = Color(0xFF16A34A)
private val BadgeAmber = Color(0xFFD97706)
private val BadgeRed = Color(0xFFDC2626)

@Composable
fun GeofenceBadgeChip(badge: GeofenceBadge, modifier: Modifier = Modifier, isMockDetected: Boolean = false) {
    if (isMockDetected) {
        BadgeChip(text = "Fake GPS detected", color = BadgeRed, modifier = modifier)
        return
    }
    val (text, color) = when (badge) {
        is GeofenceBadge.InsideZone   -> "Inside office zone" to BadgeGreen
        is GeofenceBadge.DistanceAway -> "${badge.meters}m away from office" to BadgeAmber
        else -> return
    }
    BadgeChip(text = text, color = color, modifier = modifier)
}

@Composable
private fun BadgeChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.10f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = color
            )
        }
    }
}
