package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AttendanceStatus(val labelHindi: String, val labelEnglish: String, val shortCode: String) {
    PRESENT("पूरा दिन", "Full Day", "P"),
    HALF_DAY("आधा दिन", "Half Day", "HD"),
    OVERTIME("ओवरटाइम", "Overtime", "OT"),
    ABSENT("गैरहाज़िर", "Absent", "A"),
    LEAVE("छुट्टी", "Leave", "L")
}

enum class PaymentType(val labelHindi: String, val labelEnglish: String) {
    ADVANCE("पेशगी / खर्चा", "Advance / Expense"),
    SALARY_PAYOUT("हिसाब चुकता", "Salary Payout")
}

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val role: String = "कारीगर (Mistri)", // Master Mistri, Karigar, Helper, Polisher
    val dailyWage: Double = 800.0,
    val overtimeHourlyRate: Double = 100.0,
    val photoPath: String? = null,
    val joinedDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "work_sites")
data class WorkSite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteName: String,
    val siteAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isActive: Boolean = true
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: Long,
    val dateString: String, // format: "yyyy-MM-dd"
    val status: String, // AttendanceStatus.name
    val inTime: Long? = null,
    val outTime: Long? = null,
    val overtimeHours: Double = 0.0,
    val siteId: Long? = null,
    val siteName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAddress: String? = null,
    val selfiePhotoUri: String? = null,
    val wageCalculated: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: Long,
    val dateString: String, // format: "yyyy-MM-dd"
    val amount: Double,
    val type: String = PaymentType.ADVANCE.name,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
