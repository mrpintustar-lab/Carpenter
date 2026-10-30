package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Worker
import com.example.ui.CarpenterViewModel
import com.example.ui.dialogs.CheckOutDialog
import com.example.ui.dialogs.PaymentDialog
import com.example.ui.dialogs.SelfieAttendanceDialog
import com.example.ui.dialogs.SelfiePreviewDialog
import com.example.ui.dialogs.WorkerDetailDialog
import com.example.ui.theme.CraftPrimary
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusOvertime
import com.example.ui.theme.StatusPresent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: CarpenterViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val workers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val sites by viewModel.allSites.collectAsStateWithLifecycle()
    val dailyAttendance by viewModel.dailyAttendance.collectAsStateWithLifecycle()
    val selectedSiteId by viewModel.selectedSiteId.collectAsStateWithLifecycle()
    val isContractorMode by viewModel.isContractorMode.collectAsStateWithLifecycle()
    val monthlySummaries by viewModel.monthlySummaries.collectAsStateWithLifecycle()

    var workerForSelfie by remember { mutableStateOf<Worker?>(null) }
    var workerForCheckOut by remember { mutableStateOf<Pair<Worker, AttendanceRecord?>?>(null) }
    var workerForPayment by remember { mutableStateOf<Worker?>(null) }
    var workerForDetail by remember { mutableStateOf<Worker?>(null) }
    var attendanceForPreview by remember { mutableStateOf<Pair<AttendanceRecord, String>?>(null) }

    val activeWorkers = remember(workers) { workers.filter { it.isActive } }

    // Today's stats
    val presentCount = remember(dailyAttendance) {
        dailyAttendance.count { it.status == AttendanceStatus.PRESENT.name || it.status == AttendanceStatus.OVERTIME.name }
    }
    val halfDayCount = remember(dailyAttendance) {
        dailyAttendance.count { it.status == AttendanceStatus.HALF_DAY.name }
    }
    val absentCount = remember(dailyAttendance) {
        dailyAttendance.count { it.status == AttendanceStatus.ABSENT.name }
    }
    val totalWageToday = remember(dailyAttendance) {
        dailyAttendance.sumOf { it.wageCalculated }
    }

    val displayDateFormatted = remember(selectedDate) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (date != null) {
                SimpleDateFormat("EEE, dd MMMM yyyy", Locale("hi", "IN")).format(date)
            } else selectedDate
        } catch (_: Exception) {
            selectedDate
        }
    }

    val todayString = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Date & Mode Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.changeDateOffset(-1) },
                        modifier = Modifier.testTag("prev_day_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                tint = CraftPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = displayDateFormatted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        if (selectedDate == todayString) {
                            Text("आज (Today)", fontSize = 11.sp, color = StatusPresent, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = "आज की तारीख पर जाएं",
                                fontSize = 11.sp,
                                color = CraftPrimary,
                                modifier = Modifier.clickable { viewModel.setSelectedDate(todayString) }
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.changeDateOffset(1) },
                        modifier = Modifier.testTag("next_day_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                    }
                }

                // Site Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedSiteId == null,
                        onClick = { viewModel.setSelectedSite(null) },
                        label = { Text("सभी साइट्स (All Sites)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CraftPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = CraftPrimary
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    sites.forEach { site ->
                        FilterChip(
                            selected = selectedSiteId == site.id,
                            onClick = { viewModel.setSelectedSite(site.id) },
                            label = { Text("📍 ${site.siteName}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CraftPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = CraftPrimary
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }

        // Stats Overview Banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present
                StatPill(
                    label = "हाज़िर (P)",
                    count = "$presentCount",
                    color = StatusPresent
                )
                // Half-Day
                StatPill(
                    label = "आधा (HD)",
                    count = "$halfDayCount",
                    color = StatusHalfDay
                )
                // Absent
                StatPill(
                    label = "गैरहाज़िर (A)",
                    count = "$absentCount",
                    color = StatusAbsent
                )
                // Total Today Wages
                Column(horizontalAlignment = Alignment.End) {
                    Text("आज की दिहाड़ी", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "₹${totalWageToday.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF047857)
                    )
                }
            }
        }

        // Fast Action Bar: One Click Attendance For All
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.markAllPresent() },
                colors = ButtonDefaults.buttonColors(containerColor = CraftPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("mark_all_present_button")
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("⚡ सबकी हाज़िरी लगाओ (One-Click)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.toggleContractorMode() },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isContractorMode) "ठेकेदार मोड" else "कारपेंटर मोड",
                    fontSize = 11.sp
                )
            }
        }

        // Workers List with 1-Click Buttons
        if (activeWorkers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("कोई कारपेंटर नहीं मिला", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("नीचे 'कारीगर' टैब में जाकर नया कारपेंटर जोड़ें", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeWorkers, key = { it.id }) { worker ->
                    val attendance = dailyAttendance.find { it.workerId == worker.id }
                    CarpenterAttendanceCard(
                        worker = worker,
                        attendance = attendance,
                        onOneClickStatus = { status ->
                            viewModel.markOneClickAttendance(worker, status)
                        },
                        onSelfieCheckIn = {
                            workerForSelfie = worker
                        },
                        onCheckOut = {
                            workerForCheckOut = Pair(worker, attendance)
                        },
                        onPayment = {
                            workerForPayment = worker
                        },
                        onViewDetail = {
                            workerForDetail = worker
                        },
                        onPreviewSelfie = {
                            if (attendance?.selfiePhotoUri != null) {
                                attendanceForPreview = Pair(attendance, worker.name)
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    workerForSelfie?.let { worker ->
        val site = sites.find { it.id == selectedSiteId }
        SelfieAttendanceDialog(
            worker = worker,
            siteName = site?.siteName ?: "Main Work Site",
            onDismiss = { workerForSelfie = null },
            onConfirm = { bitmap, notes ->
                viewModel.checkInWithSelfie(worker, bitmap, context, notes)
                workerForSelfie = null
            }
        )
    }

    workerForCheckOut?.let { (worker, att) ->
        CheckOutDialog(
            worker = worker,
            attendance = att,
            onDismiss = { workerForCheckOut = null },
            onConfirm = { otHours, notes ->
                viewModel.checkOutWorker(worker, otHours, notes)
                workerForCheckOut = null
            }
        )
    }

    workerForPayment?.let { worker ->
        PaymentDialog(
            worker = worker,
            onDismiss = { workerForPayment = null },
            onConfirm = { amount, type, notes ->
                viewModel.addPayment(worker.id, amount, type, notes)
                workerForPayment = null
            }
        )
    }

    workerForDetail?.let { worker ->
        val summary = monthlySummaries.find { it.worker.id == worker.id }
        WorkerDetailDialog(
            worker = worker,
            summary = summary,
            onDismiss = { workerForDetail = null },
            onEdit = {
                // Handled in Workers screen or can invoke editor
                workerForDetail = null
            },
            onDelete = {
                viewModel.deleteWorker(worker)
                workerForDetail = null
            }
        )
    }

    attendanceForPreview?.let { (att, name) ->
        SelfiePreviewDialog(
            attendance = att,
            workerName = name,
            onDismiss = { attendanceForPreview = null }
        )
    }
}

@Composable
fun CarpenterAttendanceCard(
    worker: Worker,
    attendance: AttendanceRecord?,
    onOneClickStatus: (AttendanceStatus) -> Unit,
    onSelfieCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onPayment: () -> Unit,
    onViewDetail: () -> Unit,
    onPreviewSelfie: () -> Unit
) {
    val status = attendance?.status
    val hasSelfie = !attendance?.selfiePhotoUri.isNullOrEmpty()
    val inTimeStr = attendance?.inTime?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
    }
    val outTimeStr = attendance?.outTime?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (status != null) 1.5.dp else 1.dp,
            color = when (status) {
                AttendanceStatus.PRESENT.name -> StatusPresent
                AttendanceStatus.HALF_DAY.name -> StatusHalfDay
                AttendanceStatus.OVERTIME.name -> StatusOvertime
                AttendanceStatus.ABSENT.name -> StatusAbsent
                else -> Color(0xFFE2E8F0)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_card_${worker.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Worker info top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = CraftPrimary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onViewDetail() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = worker.name.take(1),
                            fontWeight = FontWeight.Bold,
                            color = CraftPrimary,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onViewDetail() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = worker.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (hasSelfie) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Selfie Verified",
                                tint = StatusPresent,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onPreviewSelfie() }
                            )
                        }
                    }
                    Text(
                        text = "${worker.role} • दिहाड़ी: ₹${worker.dailyWage.toInt()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (status) {
                        AttendanceStatus.PRESENT.name -> StatusPresent.copy(alpha = 0.15f)
                        AttendanceStatus.HALF_DAY.name -> StatusHalfDay.copy(alpha = 0.15f)
                        AttendanceStatus.OVERTIME.name -> StatusOvertime.copy(alpha = 0.15f)
                        AttendanceStatus.ABSENT.name -> StatusAbsent.copy(alpha = 0.15f)
                        else -> Color(0xFFF1F5F9)
                    }
                ) {
                    Text(
                        text = when (status) {
                            AttendanceStatus.PRESENT.name -> "✅ पूरा दिन (P)"
                            AttendanceStatus.HALF_DAY.name -> "⛅ आधा दिन (HD)"
                            AttendanceStatus.OVERTIME.name -> "⚡ ओवरटाइम (OT)"
                            AttendanceStatus.ABSENT.name -> "❌ गैरहाज़िर (A)"
                            else -> "बाकी (Pending)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            AttendanceStatus.PRESENT.name -> StatusPresent
                            AttendanceStatus.HALF_DAY.name -> Color(0xFFB45309)
                            AttendanceStatus.OVERTIME.name -> StatusOvertime
                            AttendanceStatus.ABSENT.name -> StatusAbsent
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // In / Out & Location metadata if present
            if (inTimeStr != null || !attendance?.siteName.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (outTimeStr != null) "In: $inTimeStr • Out: $outTimeStr" else "In: $inTimeStr (काम चालू)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (!attendance?.siteName.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFE11D48))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = attendance.siteName ?: "",
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // One Click Attendance Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Present (P)
                StatusButton(
                    label = "P",
                    sub = "पूरा दिन",
                    isSelected = status == AttendanceStatus.PRESENT.name,
                    activeColor = StatusPresent,
                    onClick = { onOneClickStatus(AttendanceStatus.PRESENT) },
                    modifier = Modifier.weight(1f)
                )

                // Half Day (HD)
                StatusButton(
                    label = "HD",
                    sub = "आधा",
                    isSelected = status == AttendanceStatus.HALF_DAY.name,
                    activeColor = StatusHalfDay,
                    onClick = { onOneClickStatus(AttendanceStatus.HALF_DAY) },
                    modifier = Modifier.weight(1f)
                )

                // Overtime (OT)
                StatusButton(
                    label = "OT",
                    sub = "ओटी",
                    isSelected = status == AttendanceStatus.OVERTIME.name,
                    activeColor = StatusOvertime,
                    onClick = { onOneClickStatus(AttendanceStatus.OVERTIME) },
                    modifier = Modifier.weight(1f)
                )

                // Absent (A)
                StatusButton(
                    label = "A",
                    sub = "गैरहाज़िर",
                    isSelected = status == AttendanceStatus.ABSENT.name,
                    activeColor = StatusAbsent,
                    onClick = { onOneClickStatus(AttendanceStatus.ABSENT) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Tools Row: Selfie Check-in, Check-out, Kharcha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selfie button
                OutlinedButton(
                    onClick = {
                        if (hasSelfie) onPreviewSelfie() else onSelfieCheckIn()
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = if (hasSelfie) ButtonDefaults.outlinedButtonColors(contentColor = StatusPresent) else ButtonDefaults.outlinedButtonColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (hasSelfie) Icons.Default.Verified else Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (hasSelfie) "फोटो देखें" else "📸 सेल्फी", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Check-out button
                OutlinedButton(
                    onClick = onCheckOut,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (outTimeStr != null) "चेक-आउट ✅" else "🕘 आउट टाइम", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Advance / Kharcha button
                OutlinedButton(
                    onClick = onPayment,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("💰 खर्चा", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StatusButton(
    label: String,
    sub: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (isSelected) activeColor else Color.Transparent),
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = sub,
                fontSize = 9.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Gray
            )
        }
    }
}

@Composable
fun StatPill(
    label: String,
    count: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(10.dp)
        ) {}
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(count, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = color)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
