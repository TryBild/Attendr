package com.trybild.attendr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.trybild.attendr.ui.theme.*
import java.util.Calendar

private val MONTHS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

@Composable
fun MonthPickerDialog(
    onDismiss: () -> Unit,
    onMonthSelected: (String) -> Unit
) {
    val cal = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH)) }

    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select Month",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AttendrTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ChevronLeft, "Previous year", tint = AttendrNavy)
                    }
                    Text(
                        "$selectedYear",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AttendrTextPrimary
                    )
                    IconButton(
                        onClick = { if (selectedYear < currentYear) selectedYear++ },
                        enabled = selectedYear < currentYear
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            "Next year",
                            tint = if (selectedYear < currentYear) AttendrNavy
                                   else AttendrTextSecondary.copy(alpha = 0.3f)
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(MONTHS.size) { index ->
                        val isFuture = selectedYear == currentYear && index > currentMonth
                        val isSelected = index == selectedMonth && selectedYear == selectedYear
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (isSelected) Modifier.background(AttendrNavy)
                                    else Modifier.background(Color.Transparent)
                                )
                                .then(
                                    if (!isSelected && !isFuture) Modifier.border(1.dp, AttendrBorder, RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable(enabled = !isFuture) { selectedMonth = index }
                        ) {
                            Text(
                                MONTHS[index],
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                textAlign = TextAlign.Center,
                                color = when {
                                    isSelected -> Color.White
                                    isFuture -> AttendrTextSecondary.copy(alpha = 0.3f)
                                    else -> AttendrTextPrimary
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = AttendrTextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    AttendrButton(
                        text = "Export",
                        onClick = {
                            val monthStr = "${selectedYear}-${String.format("%02d", selectedMonth + 1)}"
                            onMonthSelected(monthStr)
                        },
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }
    }
}
