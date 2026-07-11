package com.trybild.attendr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trybild.attendr.ui.theme.*

@Composable
fun AttendrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String = "",
    errorText: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val isError = errorText.isNotEmpty()
    val focused = remember { mutableStateOf(false) }

    val borderColor = when {
        isError   -> AttendrError.copy(alpha = 0.7f)
        focused.value -> AttendrNavy.copy(alpha = 0.6f)
        else      -> GlassBorder
    }
    val bgColor = when {
        isError   -> AttendrError.copy(alpha = 0.05f)
        else      -> GlassSurface
    }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                label,
                color = if (isError) AttendrError else AttendrTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(bgColor, RoundedCornerShape(12.dp))
                .border(
                    width = if (focused.value || isError) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                enabled = enabled,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(AttendrNavy),
                textStyle = TextStyle(
                    color = AttendrTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                onTextLayout = {},
                decorationBox = { inner ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (leadingContent != null) {
                            leadingContent()
                            Spacer(Modifier.width(10.dp))
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder.ifEmpty { label },
                                    color = AttendrTextHint,
                                    fontSize = 15.sp
                                )
                            }
                            inner()
                        }
                        if (trailingContent != null) {
                            Spacer(Modifier.width(8.dp))
                            trailingContent()
                        }
                    }
                }
            )
        }

        if (isError) {
            Spacer(Modifier.height(4.dp))
            Text(errorText, color = AttendrError, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        } else if (helperText.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(helperText, color = AttendrTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}
