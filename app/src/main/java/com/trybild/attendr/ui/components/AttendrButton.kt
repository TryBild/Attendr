package com.trybild.attendr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trybild.attendr.ui.theme.AttendrNavy

enum class AttendrButtonVariant { Primary, Outlined }

@Composable
fun AttendrButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: AttendrButtonVariant = AttendrButtonVariant.Primary,
    containerColor: Color = AttendrNavy,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    val buttonModifier = modifier.fillMaxWidth().height(56.dp)

    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = if (variant == AttendrButtonVariant.Primary) Color.White else AttendrNavy,
                strokeWidth = 2.5.dp
            )
        } else {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    when (variant) {
        AttendrButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !loading,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = Color.White,
                disabledContainerColor = containerColor.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            ),
            content = content
        )
        AttendrButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !loading,
            shape = shape,
            border = BorderStroke(2.dp, if (enabled) AttendrNavy else AttendrNavy.copy(alpha = 0.4f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = AttendrNavy,
                disabledContainerColor = Color.White,
                disabledContentColor = AttendrNavy.copy(alpha = 0.4f)
            ),
            content = content
        )
    }
}
