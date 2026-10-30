package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.Worker
import com.example.ui.theme.CraftPrimary
import com.example.ui.theme.StatusOvertime
import com.example.ui.theme.StatusPresent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CheckOutDialog(
    worker: Worker,
    attendance: AttendanceRecord?,
    onDismiss: () -> Unit,
    onConfirm: (overtimeHours: Double, notes: String) -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val now = remember { System.currentTimeMillis() }
    val inTime = attendance?.inTime ?: (now - 8 * 3600 * 1000)

    val regularHours = ((now - inTime) / (1000.0 * 3600.0)).coerceAtLeast(0.0)

    var overtimeHours by remember { mutableDoubleStateOf(attendance?.overtimeHours ?: 0.0) }
    var notes by remember { mutableStateOf(attendance?.notes ?: "") }

    val overtimeWage = overtimeHours * worker.overtimeHourlyRate
    val totalTodayWage = worker.dailyWage + overtimeWage

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = StatusOvertime,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "काम समाप्त / चेक-आउट (Out-Time)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${worker.name} • दिहाड़ी: ₹${worker.dailyWage.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // In / Out time card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("आने का समय (In Time)", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = timeFormatter.format(Date(inTime)),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPresent
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("जाने का समय (Out Time)", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = timeFormatter.format(Date(now)),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "कुल काम के घंटे: %.1f घंटे".format(regularHours),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Overtime Stepper
                Text(
                    text = "ओवरटाइम (Overtime Hours)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalIconButton(
                        onClick = { if (overtimeHours >= 0.5) overtimeHours -= 0.5 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease OT")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${overtimeHours} घंटे",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CraftPrimary
                        )
                        Text(
                            text = "+ ₹${overtimeWage.toInt()} (दर: ₹${worker.overtimeHourlyRate.toInt()}/घंटा)",
                            fontSize = 11.sp,
                            color = StatusOvertime
                        )
                    }

                    FilledTonalIconButton(
                        onClick = { overtimeHours += 0.5 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase OT")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Total Wage Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("आज की कुल मजदूरी (Total Wage)", fontSize = 12.sp, color = Color(0xFF065F46))
                            Text(
                                "दिहाड़ी ₹${worker.dailyWage.toInt()} + ओटी ₹${overtimeWage.toInt()}",
                                fontSize = 11.sp,
                                color = Color(0xFF047857)
                            )
                        }
                        Text(
                            text = "₹${totalTodayWage.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("काम पूरा होने का नोट") },
                    placeholder = { Text("जैसे: अलमारी का ढांचा तैयार हुआ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(overtimeHours, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusOvertime),
                modifier = Modifier.testTag("confirm_checkout_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("चेक-आउट दर्ज करें")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
