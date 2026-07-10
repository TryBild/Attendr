package com.trybild.attendr.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trybild.attendr.ui.theme.AttendrError
import com.trybild.attendr.ui.theme.AttendrErrorBg
import com.trybild.attendr.ui.theme.AttendrSuccess
import com.trybild.attendr.ui.theme.GlassSurface
import kotlinx.coroutines.delay

@Composable
fun ErrorToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isSuccess: Boolean = false
) {
    LaunchedEffect(visible, message) {
        if (visible) {
            delay(4000L)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(280)
        ) + fadeIn(animationSpec = tween(280)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(220)
        ) + fadeOut(animationSpec = tween(220))
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isSuccess) GlassSurface else AttendrErrorBg),
            border = BorderStroke(1.dp, if (isSuccess) AttendrSuccess else AttendrError),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = message,
                color = if (isSuccess) AttendrSuccess else AttendrError,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}
