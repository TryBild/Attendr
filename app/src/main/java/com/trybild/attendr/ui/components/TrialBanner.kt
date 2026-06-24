package com.trybild.attendr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val BannerGreen = Color(0xFF16A34A)
private val BannerGreenBg = Color(0xFFDCFCE7)
private val BannerOrange = Color(0xFFD97706)
private val BannerOrangeBg = Color(0xFFFEF3C7)
private val BannerRed = Color(0xFFDC2626)
private val BannerRedBg = Color(0xFFFEE2E2)

@Composable
fun TrialBanner(
    status: String,
    daysLeft: Int,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (status != "trialing" && status != "expired") return

    val (bg, fg, text) = when {
        status == "expired" -> Triple(BannerRedBg, BannerRed, "Trial expired. Upgrade to continue.")
        daysLeft <= 3 -> Triple(BannerRedBg, BannerRed, "$daysLeft day${if (daysLeft != 1) "s" else ""} left in trial")
        daysLeft <= 7 -> Triple(BannerOrangeBg, BannerOrange, "$daysLeft days left in trial")
        else -> Triple(BannerGreenBg, BannerGreen, "$daysLeft days left in trial")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onUpgradeClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = fg
                )
                if (status == "trialing") {
                    Text(
                        "Tap to upgrade to Pro",
                        style = MaterialTheme.typography.labelSmall,
                        color = fg.copy(alpha = 0.7f)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = fg
            ) {
                Text(
                    if (status == "expired") "Upgrade" else "Pro",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
