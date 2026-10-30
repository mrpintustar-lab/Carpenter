package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.PaymentRecord
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import com.example.data.repository.AttendanceRepository
import com.example.util.ImageStorageHelper
import com.example.util.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WorkerMonthlySummary(
    val worker: Worker,
    val presentDays: Int,
    val halfDays: Int,
    val absentDays: Int,
    val overtimeHours: Double,
    val totalWageEarned: Double,
    val totalAdvanceTaken: Double,
    val netBalancePayable: Double
)

class CarpenterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AttendanceRepository(database.carpenterDao())
    }

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private val _selectedDate = MutableStateFlow(dateFormatter.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(monthFormatter.format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedSiteId = MutableStateFlow<Long?>(null)
    val selectedSiteId: StateFlow<Long?> = _selectedSiteId.asStateFlow()

    private val _isContractorMode = MutableStateFlow(true)
    val isContractorMode: StateFlow<Boolean> = _isContractorMode.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val allWorkers: StateFlow<List<Worker>> = repository.allWorkers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSites: StateFlow<List<WorkSite>> = repository.allSites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyAttendance: StateFlow<List<AttendanceRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyAttendance: StateFlow<List<AttendanceRecord>> = _selectedMonth
        .flatMapLatest { month -> repository.getAttendanceForMonth("$month%") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPayments: StateFlow<List<PaymentRecord>> = repository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyPayments: StateFlow<List<PaymentRecord>> = _selectedMonth
        .flatMapLatest { month -> repository.getPaymentsForMonth("$month%") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Monthly Summary per Worker
    val monthlySummaries: StateFlow<List<WorkerMonthlySummary>> = combine(
        allWorkers,
        monthlyAttendance,
        monthlyPayments
    ) { workers, attendances, payments ->
        workers.map { worker ->
            val workerAttendances = attendances.filter { it.workerId == worker.id }
            val workerPayments = payments.filter { it.workerId == worker.id }

            var presentCount = 0
            var halfDayCount = 0
            var absentCount = 0
            var totalOtHours = 0.0
            var totalEarned = 0.0

            for (att in workerAttendances) {
                when (att.status) {
                    AttendanceStatus.PRESENT.name -> {
                        presentCount++
                        totalEarned += att.wageCalculated
                        totalOtHours += att.overtimeHours
                    }
                    AttendanceStatus.HALF_DAY.name -> {
                        halfDayCount++
                        totalEarned += att.wageCalculated
                        totalOtHours += att.overtimeHours
                    }
                    AttendanceStatus.OVERTIME.name -> {
                        presentCount++
                        totalEarned += att.wageCalculated
                        totalOtHours += att.overtimeHours
                    }
                    AttendanceStatus.ABSENT.name -> {
                        absentCount++
                    }
                }
            }

            val totalAdvance = workerPayments.filter { it.type == "ADVANCE" }.sumOf { it.amount }
            val totalPayout = workerPayments.filter { it.type == "SALARY_PAYOUT" }.sumOf { it.amount }
            val netPayable = (totalEarned - totalAdvance - totalPayout).coerceAtLeast(0.0)

            WorkerMonthlySummary(
                worker = worker,
                presentDays = presentCount,
                halfDays = halfDayCount,
                absentDays = absentCount,
                overtimeHours = totalOtHours,
                totalWageEarned = totalEarned,
                totalAdvanceTaken = totalAdvance,
                netBalancePayable = netPayable
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setSelectedSite(siteId: Long?) {
        _selectedSiteId.value = siteId
    }

    fun toggleContractorMode() {
        _isContractorMode.value = !_isContractorMode.value
    }

    // 1-Click Fast Attendance
    fun markOneClickAttendance(worker: Worker, status: AttendanceStatus) {
        viewModelScope.launch {
            val site = allSites.value.find { it.id == _selectedSiteId.value }
            repository.markAttendance(
                worker = worker,
                dateString = _selectedDate.value,
                status = status,
                siteId = site?.id,
                siteName = site?.siteName ?: "Main Site",
                notes = "एक क्लिक हाज़िरी: ${status.labelHindi}"
            )
            _userMessage.value = "${worker.name} की हाज़िरी दर्ज: ${status.labelHindi}"
        }
    }

    // Mark All Present (सबकी हाज़िरी एक साथ)
    fun markAllPresent() {
        viewModelScope.launch {
            val active = allWorkers.value.filter { it.isActive }
            val site = allSites.value.find { it.id == _selectedSiteId.value }
            repository.markBulkAllPresent(
                workers = active,
                dateString = _selectedDate.value,
                siteId = site?.id,
                siteName = site?.siteName
            )
            _userMessage.value = "सभी ${active.size} कारपेंटरों की हाज़िरी लगा दी गई! ✅"
        }
    }

    // Selfie + GPS Attendance Check-In
    fun checkInWithSelfie(
        worker: Worker,
        selfieBitmap: Bitmap,
        context: Context,
        notes: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Get current GPS location
                val locResult = LocationHelper.getCurrentLocation(context)
                val currentSite = allSites.value.find { it.id == _selectedSiteId.value }
                val siteName = currentSite?.siteName ?: locResult.address

                // Watermark & Save Selfie
                val savedPath = ImageStorageHelper.saveWatermarkedSelfie(
                    context = context,
                    originalBitmap = selfieBitmap,
                    workerName = worker.name,
                    siteName = siteName,
                    locationText = locResult.address
                )

                // Save Attendance Record
                repository.checkInWorker(
                    worker = worker,
                    dateString = _selectedDate.value,
                    siteId = currentSite?.id,
                    siteName = siteName,
                    latitude = locResult.latitude,
                    longitude = locResult.longitude,
                    locationAddress = locResult.address,
                    selfieUri = savedPath,
                    notes = notes
                )
            }
            _userMessage.value = "${worker.name} का सेल्फी और GPS चेक-इन सफल! 📸📍"
        }
    }

    // Check-Out with In/Out Time & Overtime calculation
    fun checkOutWorker(worker: Worker, overtimeHours: Double, notes: String) {
        viewModelScope.launch {
            repository.checkOutWorker(
                worker = worker,
                dateString = _selectedDate.value,
                overtimeHours = overtimeHours,
                notes = notes
            )
            _userMessage.value = "${worker.name} का चेक-आउट पूरा हुआ (ओटी: ${overtimeHours} घंटे)"
        }
    }

    fun saveWorker(worker: Worker) {
        viewModelScope.launch {
            repository.saveWorker(worker)
            _userMessage.value = "कारपेंटर जानकारी सुरक्षित की गई!"
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
            _userMessage.value = "कारपेंटर को हटा दिया गया"
        }
    }

    fun saveSite(site: WorkSite) {
        viewModelScope.launch {
            repository.saveSite(site)
            _userMessage.value = "कार्य स्थल (Site) सुरक्षित किया गया!"
        }
    }

    fun deleteSite(site: WorkSite) {
        viewModelScope.launch {
            repository.deleteSite(site)
            _userMessage.value = "साइट हटाई गई"
        }
    }

    fun addPayment(workerId: Long, amount: Double, type: String, notes: String) {
        viewModelScope.launch {
            val payment = PaymentRecord(
                workerId = workerId,
                dateString = _selectedDate.value,
                amount = amount,
                type = type,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            repository.recordPayment(payment)
            val typeName = if (type == "ADVANCE") "पेशगी / उधार" else "हिसाब भुगतान"
            _userMessage.value = "₹$amount का $typeName दर्ज किया गया!"
        }
    }

    fun deletePayment(payment: PaymentRecord) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            _userMessage.value = "भुगतान रिकॉर्ड हटाया गया"
        }
    }

    fun deleteAttendance(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteAttendance(record)
            _userMessage.value = "हाज़िरी रिकॉर्ड हटाया गया"
        }
    }

    fun changeDateOffset(dayDelta: Int) {
        val cal = Calendar.getInstance()
        try {
            val current = dateFormatter.parse(_selectedDate.value)
            if (current != null) cal.time = current
        } catch (_: Exception) {}
        cal.add(Calendar.DAY_OF_YEAR, dayDelta)
        _selectedDate.value = dateFormatter.format(cal.time)
    }

    fun changeMonthOffset(monthDelta: Int) {
        val cal = Calendar.getInstance()
        try {
            val current = monthFormatter.parse(_selectedMonth.value)
            if (current != null) cal.time = current
        } catch (_: Exception) {}
        cal.add(Calendar.MONTH, monthDelta)
        _selectedMonth.value = monthFormatter.format(cal.time)
    }
}
