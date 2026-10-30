package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Worker
import com.example.ui.CarpenterViewModel
import com.example.ui.WorkerMonthlySummary
import com.example.ui.dialogs.PaymentDialog
import com.example.ui.theme.CraftPrimary
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusPresent
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MonthlyReportScreen(
    viewModel: CarpenterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthlySummaries by viewModel.monthlySummaries.collectAsStateWithLifecycle()
    val monthlyAttendance by viewModel.monthlyAttendance.collectAsStateWithLifecycle()

    var workerForPayout by remember { mutableStateOf<Worker?>(null) }

    val monthDisplayName = remember(selectedMonth) {
        try {
            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth)
            if (date != null) {
                SimpleDateFormat("MMMM yyyy", Locale("hi", "IN")).format(date)
            } else selectedMonth
        } catch (_: Exception) {
            selectedMonth
        }
    }

    val totalTeamWages = remember(monthlySummaries) {
        monthlySummaries.sumOf { it.totalWageEarned }
    }
    val totalTeamAdvances = remember(monthlySummaries) {
        monthlySummaries.sumOf { it.totalAdvanceTaken }
    }
    val totalNetPayable = remember(monthlySummaries) {
        monthlySummaries.sumOf { it.netBalancePayable }
    }

    fun shareSalarySlip(summary: WorkerMonthlySummary) {
        val slipText = """
🪚 *कारपेंटर मजदूरी पर्ची (Salary Slip)* 🪚
📅 महीना: $monthDisplayName
👷 कारपेंटर: *${summary.worker.name}* (${summary.worker.role})
📱 मोबाइल: ${summary.worker.phone}
💰 दिहाड़ी दर: ₹${summary.worker.dailyWage.toInt()}/दिन

📊 *हाज़िरी विवरण:*
- पूरा दिन (Full Days): ${summary.presentDays} दिन
- आधा दिन (Half Days): ${summary.halfDays} दिन
- गैरहाज़िर (Absent): ${summary.absentDays} दिन
- ओवरटाइम (OT): ${summary.overtimeHours} घंटे

💵 *मजदूरी हिसाब:*
- कुल कमाई (Gross): *₹${summary.totalWageEarned.toInt()}*
- पेशगी / उधार (Advance): - ₹${summary.totalAdvanceTaken.toInt()}
━━━━━━━━━━━━━━━━━━━━
👉 *बकाया भुगतान (Net Payable): ₹${summary.netBalancePayable.toInt()}*
━━━━━━━━━━━━━━━━━━━━
_Carpenter Haziri App द्वारा तैयार किया गया_
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, slipText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "${summary.worker.name} की पर्ची भेजें")
        context.startActivity(shareIntent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Month Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.changeMonthOffset(-1) },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = CraftPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monthDisplayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.changeMonthOffset(1) },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }
        }

        // Monthly Financial Summary
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "पूरे महीने का कुल हिसाब (Monthly Grand Total)",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("कुल मजदूरी बनी", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(
                            "₹${totalTeamWages.toInt()}",
                            color = Color(0xFF34D399),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("कुल पेशगी/खर्चा", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(
                            "- ₹${totalTeamAdvances.toInt()}",
                            color = Color(0xFFF87171),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("कुल बकाया राशि", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(
                            "₹${totalNetPayable.toInt()}",
                            color = Color(0xFFFBBF24),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // List of Worker Salary Slips
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(monthlySummaries, key = { it.worker.id }) { summary ->
                WorkerSalarySlipCard(
                    summary = summary,
                    onShareSlip = { shareSalarySlip(summary) },
                    onPayout = { workerForPayout = summary.worker }
                )
            }
        }
    }

    workerForPayout?.let { worker ->
        PaymentDialog(
            worker = worker,
            onDismiss = { workerForPayout = null },
            onConfirm = { amount, type, notes ->
                viewModel.addPayment(worker.id, amount, type, notes)
                workerForPayout = null
            }
        )
    }
}

@Composable
fun WorkerSalarySlipCard(
    summary: WorkerMonthlySummary,
    onShareSlip: () -> Unit,
    onPayout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("salary_slip_card_${summary.worker.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = summary.worker.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${summary.worker.role} • ₹${summary.worker.dailyWage.toInt()}/दिन",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (summary.netBalancePayable > 0) Color(0xFFECFDF5) else Color(0xFFF1F5F9)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("बकाया राशि", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            "₹${summary.netBalancePayable.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (summary.netBalancePayable > 0) Color(0xFF047857) else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance Breakdown Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttendanceMiniTag("पूरा: ${summary.presentDays}", StatusPresent, Modifier.weight(1f))
                AttendanceMiniTag("आधा: ${summary.halfDays}", StatusHalfDay, Modifier.weight(1f))
                AttendanceMiniTag("ओटी: ${summary.overtimeHours.toInt()}h", Color(0xFF3B82F6), Modifier.weight(1f))
                AttendanceMiniTag("छुट्टी: ${summary.absentDays}", StatusAbsent, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Earnings calculation
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "कुल कमाई: ₹${summary.totalWageEarned.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "पेशगी/खर्चा: -₹${summary.totalAdvanceTaken.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB91C1C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Share WhatsApp slip & Payout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onPayout,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("हिसाब चुकता", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onShareSlip,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("share_whatsapp_slip_${summary.worker.id}")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp पर्ची भेजें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AttendanceMiniTag(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
