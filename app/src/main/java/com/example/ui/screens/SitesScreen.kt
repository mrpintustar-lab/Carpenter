package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.WorkSite
import com.example.ui.CarpenterViewModel
import com.example.ui.theme.CraftPrimary
import com.example.util.LocationHelper
import kotlinx.coroutines.launch

@Composable
fun SitesScreen(
    viewModel: CarpenterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sites by viewModel.allSites.collectAsStateWithLifecycle()

    var isAddingSite by remember { mutableStateOf(false) }
    var siteToEdit by remember { mutableStateOf<WorkSite?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📍 कार्य स्थल व प्रोजेक्ट्स (Work Sites & GPS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "कारपेंटर किस साइट पर काम कर रहा है उसका रिकॉर्ड रखें",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sites List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sites, key = { it.id }) { site ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("site_card_${site.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = site.siteName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (site.siteAddress.isNotEmpty()) {
                                        Text(
                                            text = site.siteAddress,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (site.latitude != 0.0 && site.longitude != 0.0) {
                                        Text(
                                            text = "GPS: %.4f, %.4f".format(site.latitude, site.longitude),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                IconButton(onClick = { siteToEdit = site }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Site", tint = CraftPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (site.latitude != 0.0 && site.longitude != 0.0) {
                                    OutlinedButton(
                                        onClick = {
                                            val gmmIntentUri = Uri.parse("geo:${site.latitude},${site.longitude}?q=${site.latitude},${site.longitude}(${site.siteName})")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            context.startActivity(mapIntent)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("नक्शे पर देखें (Maps)", fontSize = 11.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.deleteSite(site) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Site", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isAddingSite = true },
            containerColor = CraftPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_site_fab")
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Add Site")
                Spacer(modifier = Modifier.width(6.dp))
                Text("नई साइट", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    if (isAddingSite || siteToEdit != null) {
        AddEditSiteDialog(
            siteToEdit = siteToEdit,
            onDismiss = {
                isAddingSite = false
                siteToEdit = null
            },
            onSave = { site ->
                viewModel.saveSite(site)
                isAddingSite = false
                siteToEdit = null
            }
        )
    }
}

@Composable
fun AddEditSiteDialog(
    siteToEdit: WorkSite?,
    onDismiss: () -> Unit,
    onSave: (WorkSite) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var siteName by remember { mutableStateOf(siteToEdit?.siteName ?: "") }
    var siteAddress by remember { mutableStateOf(siteToEdit?.siteAddress ?: "") }
    var latitude by remember { mutableDoubleStateOf(siteToEdit?.latitude ?: 0.0) }
    var longitude by remember { mutableDoubleStateOf(siteToEdit?.longitude ?: 0.0) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (siteToEdit == null) "नई साइट / प्रोजेक्ट जोड़ें" else "साइट जानकारी बदलें",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = siteName,
                    onValueChange = { siteName = it },
                    label = { Text("साइट का नाम *") },
                    placeholder = { Text("जैसे: सेक्टर 62 विला / शर्मा जी फ्लैट") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = siteAddress,
                    onValueChange = { siteAddress = it },
                    label = { Text("पता / लोकेशन विवरण") },
                    placeholder = { Text("जैसे: फ्लैट नंबर, लैंडमार्क, शहर") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isFetchingLocation = true
                            val result = LocationHelper.getCurrentLocation(context)
                            if (result.isSuccess) {
                                latitude = result.latitude
                                longitude = result.longitude
                                if (siteAddress.isEmpty()) {
                                    siteAddress = result.address
                                }
                            }
                            isFetchingLocation = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFetchingLocation) "GPS लोकेशन ला रहे हैं..." else "📍 वर्तमान GPS लोकेशन भरें")
                }

                if (latitude != 0.0 && longitude != 0.0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "GPS दर्ज: %.4f, %.4f".format(latitude, longitude),
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val site = siteToEdit?.copy(
                        siteName = siteName.trim(),
                        siteAddress = siteAddress.trim(),
                        latitude = latitude,
                        longitude = longitude
                    ) ?: WorkSite(
                        siteName = siteName.trim(),
                        siteAddress = siteAddress.trim(),
                        latitude = latitude,
                        longitude = longitude
                    )
                    onSave(site)
                },
                enabled = siteName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CraftPrimary)
            ) {
                Text(if (siteToEdit == null) "साइट जोड़ें" else "सेव करें")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
