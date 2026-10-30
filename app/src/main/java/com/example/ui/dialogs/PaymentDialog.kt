package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Worker
import com.example.ui.theme.CraftPrimary
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusPresent

@Composable
fun PaymentDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, type: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("ADVANCE") } // ADVANCE or SALARY_PAYOUT
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CurrencyRupee,
                    contentDescription = null,
                    tint = if (paymentType == "ADVANCE") StatusHalfDay else StatusPresent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (paymentType == "ADVANCE") "पेशगी / खर्चा (Advance)" else "हिसाब चुकता (Salary Payout)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Type selection chips
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = paymentType == "ADVANCE",
                        onClick = { paymentType = "ADVANCE" },
                        label = { Text("उधार / पेशगी (Advance)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusHalfDay.copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFB45309)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = paymentType == "SALARY_PAYOUT",
                        onClick = { paymentType = "SALARY_PAYOUT" },
                        label = { Text("हिसाब चुकता (Payout)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusPresent.copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF047857)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("रुपये की राशि (Amount ₹) *") },
                    placeholder = { Text("जैसे: 500") },
                    leadingIcon = {
                        Text("₹", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("कारण / विवरण (Reason / Notes)") },
                    placeholder = {
                        Text(if (paymentType == "ADVANCE") "जैसे: राशन के लिए, औजार के लिए" else "जैसे: 15 दिन का पूरा हिसाब")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, paymentType, notes.trim())
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (paymentType == "ADVANCE") CraftPrimary else StatusPresent
                ),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("दर्ज करें (Save)")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
