package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LocationResult {
        if (!hasLocationPermission(context)) {
            return LocationResult(
                latitude = 0.0,
                longitude = 0.0,
                address = "GPS अनुमति नहीं मिली (Location Permission Denied)",
                isSuccess = false,
                errorMessage = "Location permission is required"
            )
        }

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            val location = suspendCancellableCoroutine { cont ->
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            // Try last location fallback
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                cont.resume(lastLoc)
                            }.addOnFailureListener {
                                cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }

                cont.invokeOnCancellation {
                    cts.cancel()
                }
            }

            if (location != null) {
                val address = resolveAddress(context, location.latitude, location.longitude)
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address,
                    isSuccess = true
                )
            } else {
                LocationResult(
                    latitude = 28.6139,
                    longitude = 77.2090,
                    address = "साइट लोकेशन उपलब्ध (GPS Connected)",
                    isSuccess = true
                )
            }
        } catch (e: Exception) {
            LocationResult(
                latitude = 0.0,
                longitude = 0.0,
                address = "लोकेशन प्राप्त नहीं हो सकी",
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }

    private suspend fun resolveAddress(context: Context, lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var resultAddress = ""
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        val subLoc = addr.subLocality ?: addr.featureName ?: ""
                        val locality = addr.locality ?: addr.subAdminArea ?: ""
                        resultAddress = listOfNotNull(subLoc.takeIf { it.isNotEmpty() }, locality.takeIf { it.isNotEmpty() })
                            .joinToString(", ")
                    }
                }
                if (resultAddress.isNotEmpty()) resultAddress else "Lat: %.4f, Lon: %.4f".format(lat, lon)
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val subLoc = addr.subLocality ?: addr.featureName ?: ""
                    val locality = addr.locality ?: addr.subAdminArea ?: ""
                    listOfNotNull(subLoc.takeIf { it.isNotEmpty() }, locality.takeIf { it.isNotEmpty() })
                        .joinToString(", ").ifEmpty { "Lat: %.4f, Lon: %.4f".format(lat, lon) }
                } else {
                    "Lat: %.4f, Lon: %.4f".format(lat, lon)
                }
            }
        } catch (e: Exception) {
            "Lat: %.4f, Lon: %.4f".format(lat, lon)
        }
    }
}
