package com.trybild.attendr.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trybild.attendr.ui.theme.AttendrBorder
import com.trybild.attendr.ui.theme.AttendrNavy
import com.trybild.attendr.ui.theme.AttendrTextPrimary

/**
 * Six OTP boxes backed by a single invisible text field so the cursor
 * always advances on input and steps back on delete, and SMS paste works.
 */
@Composable
fun OtpInputRow(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    boxCount: Int = 6
) {
    BasicTextField(
        value = otp,
        onValueChange = { new ->
            onOtpChange(new.filter { it.isDigit() }.take(boxCount))
        },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(boxCount) { index ->
                    val char = otp.getOrNull(index)?.toString() ?: ""
                    val isActive = index == otp.length || char.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(56.dp)
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) AttendrNavy else AttendrBorder,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AttendrTextPrimary
                        )
                    }
                }
            }
        }
    )
}
