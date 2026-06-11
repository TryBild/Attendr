package com.trybild.attendr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trybild.attendr.ui.theme.AttendrBorder
import com.trybild.attendr.ui.theme.AttendrNavy

@Composable
fun StepDotsIndicator(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isActive = index + 1 == current
            if (isActive) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(AttendrNavy, CircleShape)
                )
            } else {
                Box(
                    Modifier
                        .size(8.dp)
                        .border(1.dp, AttendrBorder, CircleShape)
                )
            }
        }
    }
}
