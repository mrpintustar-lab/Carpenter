package com.example.data.repository

import com.example.data.local.CarpenterDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.PaymentRecord
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AttendanceRepository(private val dao: CarpenterDao) {

    val allWorkers: Flow<List<Worker>> = dao.getAllWorkers()
    val activeWorkers: Flow<List<Worker>> = dao.getActiveWorkers()
    val allSites: Flow<List<WorkSite>> = dao.getAllSites()
    val allPayments: Flow<List<PaymentRecord>> = dao.getAllPayments()

    fun getWorkerById(id: Long): Flow<Worker?> = dao.getWorkerById(id)

    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForDate(dateString)

    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForMonth(monthPrefix)

    fun getAttendanceForWorker(workerId: Long): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForWorker(workerId)

    fun getPaymentsForWorker(workerId: Long): Flow<List<PaymentRecord>> =
        dao.getPaymentsForWorker(workerId)

    fun getPaymentsForMonth(monthPrefix: String): Flow<List<PaymentRecord>> =
        dao.getPaymentsForMonth(monthPrefix)

    suspend fun saveWorker(worker: Worker): Long {
        return if (worker.id == 0L) {
            dao.insertWorker(worker)
        } else {
            dao.updateWorker(worker)
            worker.id
        }
    }

    suspend fun deleteWorker(worker: Worker) = dao.deleteWorker(worker)

    suspend fun saveSite(site: WorkSite): Long {
        return if (site.id == 0L) {
            dao.insertSite(site)
        } else {
            dao.updateSite(site)
            site.id
        }
    }

    suspend fun deleteSite(site: WorkSite) = dao.deleteSite(site)

    suspend fun recordPayment(payment: PaymentRecord): Long = dao.insertPayment(payment)

    suspend fun deletePayment(payment: PaymentRecord) = dao.deletePayment(payment)

    suspend fun markAttendance(
        worker: Worker,
        dateString: String,
        status: AttendanceStatus,
        siteId: Long? = null,
        siteName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        locationAddress: String? = null,
        selfieUri: String? = null,
        inTime: Long? = null,
        outTime: Long? = null,
        overtimeHours: Double = 0.0,
        notes: String = ""
    ): Long {
        val existing = dao.getWorkerAttendanceForDateDirect(worker.id, dateString)

        // Calculate wage according to status
        val baseDailyWage = worker.dailyWage
        val otPay = overtimeHours * worker.overtimeHourlyRate
        val calculatedWage = when (status) {
            AttendanceStatus.PRESENT -> baseDailyWage + otPay
            AttendanceStatus.HALF_DAY -> (baseDailyWage * 0.5) + otPay
            AttendanceStatus.OVERTIME -> baseDailyWage + otPay
            AttendanceStatus.ABSENT -> 0.0
            AttendanceStatus.LEAVE -> 0.0
        }

        val effectiveInTime = inTime ?: existing?.inTime ?: if (status != AttendanceStatus.ABSENT && status != AttendanceStatus.LEAVE) System.currentTimeMillis() else null
        val effectiveOutTime = outTime ?: existing?.outTime

        val record = AttendanceRecord(
            id = existing?.id ?: 0L,
            workerId = worker.id,
            dateString = dateString,
            status = status.name,
            inTime = effectiveInTime,
            outTime = effectiveOutTime,
            overtimeHours = overtimeHours,
            siteId = siteId ?: existing?.siteId,
            siteName = siteName ?: existing?.siteName,
            latitude = latitude ?: existing?.latitude,
            longitude = longitude ?: existing?.longitude,
            locationAddress = locationAddress ?: existing?.locationAddress,
            selfiePhotoUri = selfieUri ?: existing?.selfiePhotoUri,
            wageCalculated = calculatedWage,
            notes = if (notes.isNotEmpty()) notes else (existing?.notes ?: "")
        )

        return dao.insertAttendance(record)
    }

    suspend fun markBulkAllPresent(workers: List<Worker>, dateString: String, siteId: Long?, siteName: String?) {
        for (worker in workers) {
            markAttendance(
                worker = worker,
                dateString = dateString,
                status = AttendanceStatus.PRESENT,
                siteId = siteId,
                siteName = siteName,
                notes = "एक क्लिक में हाज़िरी (One Click Attendance)"
            )
        }
    }

    suspend fun checkInWorker(
        worker: Worker,
        dateString: String,
        siteId: Long?,
        siteName: String?,
        latitude: Double?,
        longitude: Double?,
        locationAddress: String?,
        selfieUri: String?,
        notes: String
    ): Long {
        val existing = dao.getWorkerAttendanceForDateDirect(worker.id, dateString)
        val now = System.currentTimeMillis()
        val record = AttendanceRecord(
            id = existing?.id ?: 0L,
            workerId = worker.id,
            dateString = dateString,
            status = AttendanceStatus.PRESENT.name,
            inTime = now,
            outTime = existing?.outTime,
            overtimeHours = existing?.overtimeHours ?: 0.0,
            siteId = siteId ?: existing?.siteId,
            siteName = siteName ?: existing?.siteName,
            latitude = latitude ?: existing?.latitude,
            longitude = longitude ?: existing?.longitude,
            locationAddress = locationAddress ?: existing?.locationAddress,
            selfiePhotoUri = selfieUri ?: existing?.selfiePhotoUri,
            wageCalculated = worker.dailyWage,
            notes = notes.ifEmpty { "सेल्फी और GPS से चेक-इन किया" }
        )
        return dao.insertAttendance(record)
    }

    suspend fun checkOutWorker(
        worker: Worker,
        dateString: String,
        overtimeHours: Double,
        notes: String
    ): Long {
        val existing = dao.getWorkerAttendanceForDateDirect(worker.id, dateString)
        val now = System.currentTimeMillis()
        val status = if (overtimeHours > 0) AttendanceStatus.OVERTIME.name else (existing?.status ?: AttendanceStatus.PRESENT.name)
        val wage = worker.dailyWage + (overtimeHours * worker.overtimeHourlyRate)

        val record = AttendanceRecord(
            id = existing?.id ?: 0L,
            workerId = worker.id,
            dateString = dateString,
            status = status,
            inTime = existing?.inTime ?: (now - 8 * 3600 * 1000),
            outTime = now,
            overtimeHours = overtimeHours,
            siteId = existing?.siteId,
            siteName = existing?.siteName,
            latitude = existing?.latitude,
            longitude = existing?.longitude,
            locationAddress = existing?.locationAddress,
            selfiePhotoUri = existing?.selfiePhotoUri,
            wageCalculated = wage,
            notes = if (notes.isNotEmpty()) notes else (existing?.notes ?: "काम समाप्त / चेक-आउट")
        )
        return dao.insertAttendance(record)
    }

    suspend fun deleteAttendance(record: AttendanceRecord) = dao.deleteAttendance(record)
}
