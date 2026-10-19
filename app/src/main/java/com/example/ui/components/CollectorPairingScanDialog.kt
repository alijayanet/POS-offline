package com.example.ui.components

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.util.DynamicFirebaseManager
import com.example.util.FirebaseSyncConfig
import org.json.JSONObject

@Composable
fun CollectorPairingScanDialog(
    onDismiss: () -> Unit,
    onSuccess: (collectorName: String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var showCameraScanner by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var manualInputMode by remember { mutableStateOf(false) }
    var manualJsonText by remember { mutableStateOf("") }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            manualInputMode = true
        }
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun processPairingJson(raw: String) {
        try {
            val json = JSONObject(raw)
            val type = json.optString("type", "")
            if (type != "TOKOPOS_PAIRING") {
                scanError = "QR Code bukan QR Pairing TokoPOS!"
                return
            }

            val projectId = json.optString("projectId", "")
            val apiKey = json.optString("apiKey", "")
            val appId = json.optString("appId", "")
            val storeId = json.optString("storeId", "")
            val collectorId = json.optString("collectorId", "")
            val collectorName = json.optString("collectorName", "")

            if (projectId.isEmpty() || apiKey.isEmpty() || collectorId.isEmpty()) {
                scanError = "Data konfigurasi di dalam QR Code tidak lengkap!"
                return
            }

            // Save Firebase config
            val config = FirebaseSyncConfig(
                isEnabled = true,
                projectId = projectId,
                apiKey = apiKey,
                applicationId = appId,
                storeId = storeId
            )
            DynamicFirebaseManager.saveSyncConfig(context, config)

            // Save Collector Role
            DynamicFirebaseManager.saveRoleCollector(context, collectorId, collectorName)

            Toast.makeText(context, "Selamat Datang, $collectorName!", Toast.LENGTH_LONG).show()
            onSuccess(collectorName)
        } catch (e: Exception) {
            scanError = "Format QR tidak valid: ${e.message}"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EditorialBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = EditorialPrimaryBlue)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Scan QR Pairing Admin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                            Text(
                                text = "Arahkan kamera ke layar HP Admin",
                                style = MaterialTheme.typography.bodySmall,
                                color = EditorialMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = EditorialMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (scanError != null) {
                    Surface(
                        color = AlertRedContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = scanError ?: "",
                            color = AlertRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (!manualInputMode && hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { showCameraScanner = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Kamera Scan QR", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = { manualInputMode = true }) {
                        Text("Atau Tempel / Ketik Teks Konfigurasi", fontSize = 12.sp, color = EditorialPrimaryBlue)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = manualJsonText,
                            onValueChange = { manualJsonText = it },
                            label = { Text("Teks JSON Konfigurasi") },
                            placeholder = { Text("{\"type\":\"TOKOPOS_PAIRING\"...}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            maxLines = 6
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { processPairingJson(manualJsonText.trim()) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Hubungkan Akun", fontWeight = FontWeight.Bold)
                        }

                        if (hasCameraPermission) {
                            TextButton(
                                onClick = { manualInputMode = false },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Gunakan Kamera Scan QR", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCameraScanner) {
        CameraBarcodeScannerDialog(
            onBarcodeScanned = { scanned ->
                showCameraScanner = false
                processPairingJson(scanned)
            },
            onDismiss = { showCameraScanner = false }
        )
    }
}
