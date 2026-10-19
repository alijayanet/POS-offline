package com.example.ui.screens.subscription

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Collector
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EditorialBackground
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialSuccessGreen
import com.example.ui.theme.EditorialSuccessGreenContainer
import com.example.util.DynamicFirebaseManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

@Composable
fun CollectorManagementSection(
    collectors: List<Collector>,
    onAddCollector: (Collector) -> Unit,
    onEditCollector: (Collector) -> Unit,
    onDeleteCollector: (Collector) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var collectorToEdit by remember { mutableStateOf<Collector?>(null) }
    var collectorForQrPairing by remember { mutableStateOf<Collector?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialOutline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EditorialBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = EditorialPrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kelola Kolektor Penagih",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EditorialNavy
                        )
                        Text(
                            text = "Petugas lapangan yang menagih via HP mereka",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (collectors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada kolektor. Klik Tambah untuk membuat akun penagih.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    collectors.forEach { collector ->
                        CollectorItemCard(
                            collector = collector,
                            onPairQr = { collectorForQrPairing = collector },
                            onEdit = { collectorToEdit = collector },
                            onDelete = { onDeleteCollector(collector) },
                            onToggleActive = {
                                onEditCollector(collector.copy(isActive = !collector.isActive))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || collectorToEdit != null) {
        AddEditCollectorDialog(
            collector = collectorToEdit,
            onDismiss = {
                showAddDialog = false
                collectorToEdit = null
            },
            onSave = { saved ->
                if (collectorToEdit == null) {
                    onAddCollector(saved)
                } else {
                    onEditCollector(saved)
                }
                showAddDialog = false
                collectorToEdit = null
            }
        )
    }

    collectorForQrPairing?.let { col ->
        CollectorQrPairingDialog(
            collector = col,
            onDismiss = { collectorForQrPairing = null }
        )
    }
}

@Composable
fun CollectorItemCard(
    collector: Collector,
    onPairQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialOutline.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = collector.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (collector.isActive) EditorialSuccessGreenContainer else AlertRedContainer
                    ) {
                        Text(
                            text = if (collector.isActive) "AKTIF" else "NONAKTIF",
                            color = if (collector.isActive) EditorialSuccessGreen else AlertRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (collector.assignedArea.isNotEmpty()) {
                    Text(
                        text = "Area: ${collector.assignedArea}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        fontSize = 11.sp
                    )
                }
                if (collector.phone.isNotEmpty()) {
                    Text(
                        text = "HP: ${collector.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // QR Pairing Button
                Button(
                    onClick = onPairQr,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialBlueContainer),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR Pairing", tint = EditorialPrimaryBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tautkan", fontSize = 11.sp, color = EditorialPrimaryBlue, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EditorialMuted, modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AlertRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddEditCollectorDialog(
    collector: Collector?,
    onDismiss: () -> Unit,
    onSave: (Collector) -> Unit
) {
    var name by remember { mutableStateOf(collector?.name ?: "") }
    var phone by remember { mutableStateOf(collector?.phone ?: "") }
    var assignedArea by remember { mutableStateOf(collector?.assignedArea ?: "") }
    var isActive by remember { mutableStateOf(collector?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (collector == null) "Tambah Kolektor Baru" else "Edit Data Kolektor",
                fontWeight = FontWeight.Bold,
                color = EditorialNavy
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Petugas Kolektor *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor HP / WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assignedArea,
                    onValueChange = { assignedArea = it },
                    label = { Text("Wilayah Tugas (misal: RT 01 - 04)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val id = collector?.id ?: "KOL-${System.currentTimeMillis().toString().takeLast(6)}"
                    onSave(
                        Collector(
                            id = id,
                            name = name.trim(),
                            phone = phone.trim(),
                            assignedArea = assignedArea.trim(),
                            isActive = isActive,
                            createdAt = collector?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun CollectorQrPairingDialog(
    collector: Collector,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val syncConfig = DynamicFirebaseManager.getSyncConfig(context)

    // Build JSON payload
    val payloadJson = remember {
        val obj = JSONObject()
        obj.put("type", "TOKOPOS_PAIRING")
        obj.put("projectId", syncConfig.projectId)
        obj.put("apiKey", syncConfig.apiKey)
        obj.put("appId", syncConfig.applicationId)
        obj.put("storeId", syncConfig.storeId)
        obj.put("collectorId", collector.id)
        obj.put("collectorName", collector.name)
        obj.toString()
    }

    val qrBitmap = remember(payloadJson) {
        generateQrCodeBitmap(payloadJson, 512)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tautkan HP Kolektor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EditorialNavy
                )
                Text(
                    text = "Petugas: ${collector.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EditorialPrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (syncConfig.projectId.isEmpty() || syncConfig.apiKey.isEmpty()) {
                    Surface(
                        color = AlertRedContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cloud Firebase belum diisi di Pengaturan Toko. Silakan masukkan Project ID & API Key terlebih dahulu.",
                            color = AlertRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(EditorialCardWhite)
                            .border(1.dp, EditorialOutline, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code Pairing",
                            modifier = Modifier.size(200.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Di HP Kolektor, buka TokoPOS lalu pilih:\n'Masuk Sebagai Kolektor (Scan QR)'",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun generateQrCodeBitmap(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
