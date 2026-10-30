package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.PaymentRecord
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [Worker::class, WorkSite::class, AttendanceRecord::class, PaymentRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carpenterDao(): CarpenterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "carpenter_haziri.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.carpenterDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: CarpenterDao) {
            // Pre-seed realistic work sites
            val site1Id = dao.insertSite(
                WorkSite(
                    siteName = "सेक्टर 62 विला (Sector 62 Villa)",
                    siteAddress = "Noida Sector 62, Near Metro",
                    latitude = 28.6280,
                    longitude = 77.3649,
                    isActive = true
                )
            )
            val site2Id = dao.insertSite(
                WorkSite(
                    siteName = "गोदाम कारखाना (Main Workshop)",
                    siteAddress = "Industrial Area Phase 2",
                    latitude = 28.5355,
                    longitude = 77.3910,
                    isActive = true
                )
            )
            dao.insertSite(
                WorkSite(
                    siteName = "शर्मा जी फ्लैट इंटीरियर (Sharma Ji Interior)",
                    siteAddress = "Tower B, Flat 402, Gaur City",
                    latitude = 28.5980,
                    longitude = 77.4280,
                    isActive = true
                )
            )

            // Pre-seed realistic carpenters / workers
            val w1Id = dao.insertWorker(
                Worker(
                    name = "रमेश मिस्त्री (Ramesh Mistri)",
                    phone = "9876543210",
                    role = "कारीगर / हेड मिस्त्री (Head Mistri)",
                    dailyWage = 1000.0,
                    overtimeHourlyRate = 120.0,
                    notes = "मॉड्यूलर किचन और अलमारी विशेषज्ञ"
                )
            )
            val w2Id = dao.insertWorker(
                Worker(
                    name = "सोनू कुमार (Sonu Kumar)",
                    phone = "9812345678",
                    role = "कारपेंटर कारीगर (Carpenter)",
                    dailyWage = 800.0,
                    overtimeHourlyRate = 100.0,
                    notes = "दरवाजे, चौखट और खिड़की फिटिंग"
                )
            )
            val w3Id = dao.insertWorker(
                Worker(
                    name = "दिलीप शर्मा (Dilip Sharma)",
                    phone = "9834567890",
                    role = "पॉलिश कारीगर (Polisher)",
                    dailyWage = 850.0,
                    overtimeHourlyRate = 110.0,
                    notes = "PU पॉलिश और मेलामाइन फिनिशिंग"
                )
            )
            val w4Id = dao.insertWorker(
                Worker(
                    name = "राजू हेल्पर (Raju Helper)",
                    phone = "9845678901",
                    role = "हेल्पर (Helper)",
                    dailyWage = 500.0,
                    overtimeHourlyRate = 70.0,
                    notes = "प्लाई कटिंग और लोडिंग"
                )
            )
            val w5Id = dao.insertWorker(
                Worker(
                    name = "विकास कटर (Vikas Cutter)",
                    phone = "9856789012",
                    role = "कटर मशीन ऑपरेटर (Cutter)",
                    dailyWage = 750.0,
                    overtimeHourlyRate = 90.0,
                    notes = "कटर मशीन और लेमिनेट पेस्टिंग"
                )
            )

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Initial attendance for today so app looks rich on first launch
            val inTimeToday = System.currentTimeMillis() - (4 * 3600 * 1000) // 4 hours ago (around 9 AM)
            dao.insertAttendance(
                AttendanceRecord(
                    workerId = w1Id,
                    dateString = today,
                    status = "PRESENT",
                    inTime = inTimeToday,
                    outTime = null,
                    overtimeHours = 0.0,
                    siteId = site1Id,
                    siteName = "सेक्टर 62 विला (Sector 62 Villa)",
                    latitude = 28.6280,
                    longitude = 77.3649,
                    locationAddress = "Sector 62, Noida",
                    wageCalculated = 1000.0,
                    notes = "सुबह 9 बजे समय पर आया"
                )
            )
            dao.insertAttendance(
                AttendanceRecord(
                    workerId = w2Id,
                    dateString = today,
                    status = "PRESENT",
                    inTime = inTimeToday + 600000,
                    outTime = null,
                    overtimeHours = 0.0,
                    siteId = site1Id,
                    siteName = "सेक्टर 62 विला (Sector 62 Villa)",
                    latitude = 28.6280,
                    longitude = 77.3649,
                    locationAddress = "Sector 62, Noida",
                    wageCalculated = 800.0,
                    notes = ""
                )
            )

            // Seed an advance payment
            dao.insertPayment(
                PaymentRecord(
                    workerId = w2Id,
                    dateString = today,
                    amount = 500.0,
                    type = "ADVANCE",
                    notes = "किराने के लिए पेशगी ली",
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
        }
    }
}
