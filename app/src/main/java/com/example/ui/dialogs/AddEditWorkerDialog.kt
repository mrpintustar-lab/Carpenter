package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Worker
import com.example.ui.theme.CraftPrimary

@Composable
fun AddEditWorkerDialog(
    workerToEdit: Worker?,
    onDismiss: () -> Unit,
    onSave: (Worker) -> Unit
) {
    var name by remember { mutableStateOf(workerToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(workerToEdit?.phone ?: "") }
    var role by remember { mutableStateOf(workerToEdit?.role ?: "कारीगर / कारपेंटर (Carpenter)") }
    var dailyWageText by remember { mutableStateOf(workerToEdit?.dailyWage?.toInt()?.toString() ?: "800") }
    var overtimeRateText by remember { mutableStateOf(workerToEdit?.overtimeHourlyRate?.toInt()?.toString() ?: "100") }
    var notes by remember { mutableStateOf(workerToEdit?.notes ?: "") }
    var isActive by remember { mutableStateOf(workerToEdit?.isActive ?: true) }

    var roleMenuExpanded by remember { mutableStateOf(false) }

    val roleOptions = listOf(
        "कारीगर / हेड मिस्त्री (Head Mistri)",
        "कारीगर / कारपेंटर (Carpenter)",
        "पॉलिश कारीगर (Polisher)",
        "कटर मशीन ऑपरेटर (Cutter)",
        "हेल्पर (Helper)",
        "सोफा / अपहोल्स्ट्री मिस्त्री"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CraftPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (workerToEdit == null) "नया कारपेंटर जोड़ें" else "कारपेंटर जानकारी बदलें",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("कारपेंटर का नाम *") },
                    placeholder = { Text("जैसे: रमेश मिस्त्री") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("worker_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("मोबाइल नंबर") },
                    placeholder = { Text("जैसे: 9876543210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("worker_phone_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role Dropdown
                Column {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("पद / रोल (Role)") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { roleMenuExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { roleMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        roleOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    role = opt
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dailyWageText,
                        onValueChange = { dailyWageText = it },
                        label = { Text("रोज़ की दिहाड़ी (₹) *") },
                        placeholder = { Text("800") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("worker_wage_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = overtimeRateText,
                        onValueChange = { overtimeRateText = it },
                        label = { Text("ओटी दर (₹/घंटा)") },
                        placeholder = { Text("100") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("विशेषता / विवरण (Notes)") },
                    placeholder = { Text("जैसे: किचन, अलमारी, लेमिनेट") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isActive) "वर्तमान में कार्यरत (Active Worker)" else "निष्क्रिय (Inactive)",
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wage = dailyWageText.toDoubleOrNull() ?: 800.0
                    val otRate = overtimeRateText.toDoubleOrNull() ?: 100.0
                    val worker = workerToEdit?.copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        role = role,
                        dailyWage = wage,
                        overtimeHourlyRate = otRate,
                        notes = notes.trim(),
                        isActive = isActive
                    ) ?: Worker(
                        name = name.trim(),
                        phone = phone.trim(),
                        role = role,
                        dailyWage = wage,
                        overtimeHourlyRate = otRate,
                        notes = notes.trim(),
                        isActive = isActive
                    )
                    onSave(worker)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CraftPrimary),
                modifier = Modifier.testTag("save_worker_button")
            ) {
                Text(if (workerToEdit == null) "जोड़ें" else "अपडेट करें")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
