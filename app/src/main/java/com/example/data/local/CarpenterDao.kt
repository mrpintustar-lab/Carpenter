package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.PaymentRecord
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface CarpenterDao {

    // --- Worker queries ---
    @Query("SELECT * FROM workers ORDER BY isActive DESC, name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    fun getWorkerById(id: Long): Flow<Worker?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    // --- WorkSite queries ---
    @Query("SELECT * FROM work_sites ORDER BY isActive DESC, siteName ASC")
    fun getAllSites(): Flow<List<WorkSite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: WorkSite): Long

    @Update
    suspend fun updateSite(site: WorkSite)

    @Delete
    suspend fun deleteSite(site: WorkSite)

    // --- Attendance queries ---
    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString")
    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString LIKE :monthPrefix ORDER BY dateString DESC")
    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE workerId = :workerId ORDER BY dateString DESC")
    fun getAttendanceForWorker(workerId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE workerId = :workerId AND dateString = :dateString LIMIT 1")
    suspend fun getWorkerAttendanceForDateDirect(workerId: Long, dateString: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE workerId = :workerId AND dateString = :dateString LIMIT 1")
    fun getWorkerAttendanceForDateFlow(workerId: Long, dateString: String): Flow<AttendanceRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Update
    suspend fun updateAttendance(record: AttendanceRecord)

    @Delete
    suspend fun deleteAttendance(record: AttendanceRecord)

    // --- Payment queries ---
    @Query("SELECT * FROM payment_records ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE workerId = :workerId ORDER BY timestamp DESC")
    fun getPaymentsForWorker(workerId: Long): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE dateString LIKE :monthPrefix ORDER BY timestamp DESC")
    fun getPaymentsForMonth(monthPrefix: String): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Delete
    suspend fun deletePayment(payment: PaymentRecord)
}
