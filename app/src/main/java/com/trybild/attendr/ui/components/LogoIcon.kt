package com.trybild.attendr.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.trybild.attendr.R

@Composable
fun LogoIcon(size: Dp = 96.dp, modifier: Modifier = Modifier) {
    val drawable = if (size >= 80.dp) R.drawable.ic_attendr_logo else R.drawable.ic_logo
    Image(
        painter = painterResource(drawable),
        contentDescription = stringResource(R.string.logo_content_description),
        modifier = modifier.size(size)
    )
}
