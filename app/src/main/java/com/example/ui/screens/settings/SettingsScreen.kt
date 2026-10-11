package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StoreProfile
import com.example.ui.theme.EditorialAlertRed
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOnBlueContainer
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.EditorialTextDark

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import com.example.ui.theme.EditorialAlertRedContainer
import com.example.ui.theme.EditorialPurple
import com.example.ui.theme.EditorialPurpleContainer
import com.example.ui.theme.EditorialSuccessGreen
import com.example.ui.theme.EditorialSuccessGreenContainer
import com.example.util.ThermalPrinterManager
import com.example.util.QrisEngine
import com.example.ui.components.CurrencyUtils

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val currentProfile = uiState.storeProfile

    var storeName by remember(currentProfile) { mutableStateOf(currentProfile?.storeName ?: "Toko Ali Jaya") }
    var ownerName by remember(currentProfile) { mutableStateOf(currentProfile?.ownerName ?: "Ali Jaya") }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile?.phone ?: "0812-3456-7890") }
    var address by remember(currentProfile) { mutableStateOf(currentProfile?.address ?: "Jl. Raya Pasar Baru No. 12") }
    var receiptFooter by remember(currentProfile) { mutableStateOf(currentProfile?.receiptFooter ?: "Terima kasih atas kunjungan Anda!") }
    var bankAccountsInfo by remember(currentProfile) { mutableStateOf(currentProfile?.bankAccountsInfo ?: "🏧 BCA : 8820 1928 334 (a.n Ali Jaya)\n🏧 BRI : 0123 0100 4567 508 (a.n Ali Jaya)\n🏧 Dana/OVO/ShopeePay : 0812 3456 7890\n📱 Kasir Toko : Tersedia Tunai / QRIS di toko") }
    var taxRateText by remember(currentProfile) { mutableStateOf(currentProfile?.taxRatePercent?.toString() ?: "0") }
    var isTaxEnabled by remember(currentProfile) { mutableStateOf(currentProfile?.isTaxEnabled ?: false) }

    var qrisPayload by remember(currentProfile) { mutableStateOf(currentProfile?.qrisStaticPayload ?: "") }
    var printerType by remember(currentProfile) { mutableStateOf(currentProfile?.printerType ?: "BLUETOOTH") }
    var printerMac by remember(currentProfile) { mutableStateOf(currentProfile?.bluetoothPrinterMac ?: "") }
    var paperSize by remember(currentProfile) { mutableStateOf(currentProfile?.paperSize ?: "58mm") }

    var pairedPrinters by remember { mutableStateOf<List<ThermalPrinterManager.PairedPrinter>>(emptyList()) }

    fun refreshBluetoothPrinters() {
        pairedPrinters = ThermalPrinterManager.getPairedPrinters(context)
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshBluetoothPrinters()
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            bluetoothPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            refreshBluetoothPrinters()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.scanQrisFromImage(uri)
        }
    }

    val saveBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.saveBackupToUri(context, uri)
        }
    }

    var selectedRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var restoreSummary by remember { mutableStateOf<com.example.util.BackupSummary?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var replaceExistingOnRestore by remember { mutableStateOf(true) }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            val readRes = com.example.util.BackupManager.readJsonFromUri(context, uri)
            if (readRes.isSuccess) {
                val summary = com.example.util.BackupManager.parseBackupSummary(readRes.getOrNull() ?: "")
                if (summary != null) {
                    restoreSummary = summary
                    showRestoreConfirmDialog = true
                } else {
                    Toast.makeText(context, "Format file backup tidak valid!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Gagal membuka file: ${readRes.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Profil Toko
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EditorialBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = EditorialPrimaryBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PROFIL TOKO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EditorialMuted,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Identitas & Format Struk",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Nama Toko") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("settings_store_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Nama Pemilik / Kasir") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("No. WhatsApp / Telepon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Toko") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = receiptFooter,
                        onValueChange = { receiptFooter = it },
                        label = { Text("Catatan Kaki Struk") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bankAccountsInfo,
                        onValueChange = { bankAccountsInfo = it },
                        label = { Text("Rekening Bank / E-Wallet (Pesan Tagihan WA)") },
                        placeholder = { Text("Contoh:\n🏧 BCA : 8820 1928 334 (a.n Ali Jaya)\n🏧 BRI : 0123 0100 4567 508 (a.n Ali Jaya)\n🏧 Dana/OVO : 0812 3456 7890") },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = EditorialOutline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // PPN / Tax Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aktifkan PPN / Pajak",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialTextDark
                                )
                            )
                            Text(
                                text = "Otomatis dihitung pada kalkulasi kasir",
                                style = MaterialTheme.typography.bodySmall,
                                color = EditorialMuted
                            )
                        }
                        Switch(
                            checked = isTaxEnabled,
                            onCheckedChange = { isTaxEnabled = it }
                        )
                    }

                    if (isTaxEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = taxRateText,
                            onValueChange = { taxRateText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Tarif Pajak (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val profile = StoreProfile(
                                id = 1,
                                storeName = storeName.trim(),
                                ownerName = ownerName.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                receiptFooter = receiptFooter.trim(),
                                currencySymbol = "Rp",
                                taxRatePercent = taxRateText.toDoubleOrNull() ?: 0.0,
                                isTaxEnabled = isTaxEnabled,
                                qrisStaticPayload = qrisPayload.trim(),
                                printerType = printerType,
                                bluetoothPrinterMac = printerMac,
                                paperSize = paperSize,
                                bankAccountsInfo = bankAccountsInfo.trim()
                            )
                            viewModel.saveStoreProfile(profile)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_store_profile_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Profil Toko", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Pengaturan QRIS Dinamis & Upload Gambar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EditorialBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = EditorialPrimaryBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PEMBAYARAN QRIS DINAMIS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EditorialMuted,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Upload Stiker / Data QRIS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Aplikasi akan otomatis mengonversi QRIS Statis toko menjadi QRIS Dinamis per transaksi sehingga nominal belanja langsung terisi otomatis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Upload Button
                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📸 Upload Foto / Screenshot QRIS")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = qrisPayload,
                        onValueChange = { qrisPayload = it },
                        label = { Text("Teks Payload QRIS Statis (000201...)") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (qrisPayload.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val merchant = QrisEngine.getMerchantNameFromPayload(qrisPayload)
                        Text(
                            text = "Merchant Terdeteksi: $merchant",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EditorialPrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val current = currentProfile ?: StoreProfile()
                            viewModel.saveStoreProfile(current.copy(qrisStaticPayload = qrisPayload.trim()))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Data QRIS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Pengaturan Printer Thermal Bluetooth / RawBT
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(EditorialBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = EditorialPrimaryBlue)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "PRINTER THERMAL STRUK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EditorialMuted,
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Koneksi Bluetooth & RawBT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialNavy
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    bluetoothPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.BLUETOOTH_CONNECT,
                                            android.Manifest.permission.BLUETOOTH_SCAN
                                        )
                                    )
                                } else {
                                    refreshBluetoothPrinters()
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EditorialBlueContainer)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Pindai Printer", tint = EditorialPrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Printer Type Selection Chips
                    Text(
                        text = "Metode Cetak Struk:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.FilterChip(
                            selected = printerType == "BLUETOOTH",
                            onClick = { printerType = "BLUETOOTH" },
                            label = { Text("Bluetooth Direct") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = printerType == "RAWBT",
                            onClick = { printerType = "RAWBT" },
                            label = { Text("RawBT App") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = printerType == "SYSTEM",
                            onClick = { printerType = "SYSTEM" },
                            label = { Text("System Print") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Paper Size Chips
                    Text(
                        text = "Ukuran Lebar Kertas:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.FilterChip(
                            selected = paperSize == "58mm",
                            onClick = { paperSize = "58mm" },
                            label = { Text("58 mm (Standard)") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = paperSize == "80mm",
                            onClick = { paperSize = "80mm" },
                            label = { Text("80 mm (Besar)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bluetooth Paired Devices List
                    if (printerType == "BLUETOOTH") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Printer Bluetooth Terpasang:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${pairedPrinters.size} ditemukan",
                                style = MaterialTheme.typography.labelSmall,
                                color = EditorialMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        if (pairedPrinters.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = EditorialSurfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚠️ Belum ada printer terdeteksi atau izin Bluetooth belum aktif.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "1. Pastikan Bluetooth HP aktif dan printer sudah di-Pairing di menu Bluetooth HP.\n2. Klik tombol putar (🔄) di pojok kanan atas kartu ini untuk memuat ulang printer.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EditorialMuted
                                    )
                                }
                            }
                        } else {
                            pairedPrinters.forEach { p ->
                                val isSelected = printerMac == p.address
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) EditorialBlueContainer else EditorialSurfaceVariant)
                                        .clickable { printerMac = p.address }
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) EditorialPrimaryBlue else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Bold, color = EditorialTextDark)
                                        Text(p.address, fontSize = 11.sp, color = EditorialMuted)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Terpilih", tint = EditorialPrimaryBlue)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = printerMac,
                            onValueChange = { printerMac = it },
                            label = { Text("MAC Address Printer (Contoh: 66:22:33:44:55:66)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.testPrint(printerMac, paperSize) },
                                shape = RoundedCornerShape(12.dp),
                                enabled = printerMac.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tes Cetak", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val current = currentProfile ?: StoreProfile()
                                    viewModel.saveStoreProfile(
                                        current.copy(
                                            printerType = printerType,
                                            bluetoothPrinterMac = printerMac.trim(),
                                            paperSize = paperSize
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simpan Printer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val current = currentProfile ?: StoreProfile()
                                viewModel.saveStoreProfile(
                                    current.copy(
                                        printerType = printerType,
                                        bluetoothPrinterMac = printerMac.trim(),
                                        paperSize = paperSize
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Pengaturan Cetak", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Kelola Kategori
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
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
                                    .background(EditorialSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    tint = EditorialNavy
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "KATEGORI BARANG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EditorialMuted,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        IconButton(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EditorialBlueContainer)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Kategori", tint = EditorialPrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    uiState.categories.forEachIndexed { index, category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = EditorialTextDark
                                )
                            )

                            IconButton(
                                onClick = { viewModel.deleteCategory(category) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = EditorialAlertRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (index < uiState.categories.size - 1) {
                            HorizontalDivider(color = EditorialOutline.copy(alpha = 0.35f))
                        }
                    }
                }
            }
        }

        // Section: Backup & Restore Data
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EditorialPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Storage,
                                contentDescription = null,
                                tint = EditorialPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BACKUP & RESTORE DATA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EditorialMuted,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Cadangkan & Pulihkan Database",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Amankan seluruh database toko (produk & stok, kategori, riwayat transaksi, pelanggan, catatan hutang, dan supplier) ke dalam file backup (.json). Sangat penting untuk mencegah kehilangan data saat berganti HP atau reset aplikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Button 1: Export / Save Backup File
                    Button(
                        onClick = {
                            val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                            val defaultFileName = "TokoPOS_Backup_${sdf.format(java.util.Date())}.json"
                            saveBackupLauncher.launch(defaultFileName)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan File Backup (.json)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button 2 & 3: Share & Restore
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.shareBackup(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim / WA", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { restoreFileLauncher.launch("application/json") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impor / Restore", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = EditorialOutline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Maintenance: Reset Demo Data
                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Muat Ulang Data Sampel Toko", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Section: About App
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint = EditorialPrimaryBlue,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TokoPOS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialNavy
                        )
                    )
                    Text(
                        text = "Aplikasi Kasir POS & Manajemen Stok (100% Offline)",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted
                    )
                    Text(
                        text = "Versi 1.1 • Bebas Server & Bebas Pelacakan",
                        style = MaterialTheme.typography.labelSmall,
                        color = EditorialMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showPrivacyPolicyDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kebijakan Privasi & Keamanan Data", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                color = EditorialCardWhite
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Tambah Kategori Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Nama Kategori") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showAddCategoryDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCatName.isNotBlank()) {
                                    viewModel.addCategory(newCatName.trim())
                                    showAddCategoryDialog = false
                                }
                            },
                            enabled = newCatName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                        ) {
                            Text("Tambah")
                        }
                    }
                }
            }
        }
    }

    // Reset Demo Confirmation Dialog
    if (showResetConfirmDialog) {
        Dialog(onDismissRequest = { showResetConfirmDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                color = EditorialCardWhite
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Muat Ulang Data Sampel?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ini akan mengisi ulang katalog dengan barang-barang sembako, kategori, dan pelanggan bawaan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EditorialMuted
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.resetDemoData()
                                showResetConfirmDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                        ) {
                            Text("Muat Ulang")
                        }
                    }
                }
            }
        }
    }

    // Restore Backup Confirmation Dialog
    if (showRestoreConfirmDialog && restoreSummary != null) {
        val summary = restoreSummary!!
        Dialog(onDismissRequest = { showRestoreConfirmDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                color = EditorialCardWhite
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EditorialPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = EditorialPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PULIHKAN DATA (RESTORE)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EditorialMuted,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Konfirmasi Pemulihan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary Info Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = EditorialSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Informasi File Backup:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nama Toko:", style = MaterialTheme.typography.bodySmall, color = EditorialMuted)
                                Text(summary.storeName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tanggal Backup:", style = MaterialTheme.typography.bodySmall, color = EditorialMuted)
                                Text(CurrencyUtils.formatDate(summary.backupDate), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Produk / Stok:", style = MaterialTheme.typography.bodySmall, color = EditorialMuted)
                                Text("${summary.totalProducts} Produk", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EditorialPrimaryBlue)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Transaksi Kasir:", style = MaterialTheme.typography.bodySmall, color = EditorialMuted)
                                Text("${summary.totalOrders} Transaksi", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EditorialSuccessGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pelanggan & Hutang:", style = MaterialTheme.typography.bodySmall, color = EditorialMuted)
                                Text("${summary.totalCustomers} Pelanggan (${summary.totalDebts} Catatan)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Toggle: Replace vs Merge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (replaceExistingOnRestore) EditorialAlertRedContainer.copy(alpha = 0.5f) else EditorialBlueContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (replaceExistingOnRestore) "Timpa Semua Data (Bersih)" else "Gabungkan Data (Merge)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (replaceExistingOnRestore) EditorialAlertRed else EditorialPrimaryBlue
                                )
                                Text(
                                    text = if (replaceExistingOnRestore) "Hapus data saat ini dan ganti 100% dengan file backup" else "Tambahkan produk & riwayat tanpa menghapus data yang ada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EditorialMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = replaceExistingOnRestore,
                                onCheckedChange = { replaceExistingOnRestore = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showRestoreConfirmDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val uri = selectedRestoreUri
                                if (uri != null) {
                                    viewModel.restoreFromUri(
                                        context = context,
                                        uri = uri,
                                        replaceExisting = replaceExistingOnRestore,
                                        onSuccess = {
                                            showRestoreConfirmDialog = false
                                        }
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pulihkan Sekarang")
                        }
                    }
                }
            }
        }
    }

    if (showPrivacyPolicyDialog) {
        Dialog(onDismissRequest = { showPrivacyPolicyDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                color = EditorialCardWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kebijakan Privasi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EditorialNavy
                        )
                        IconButton(onClick = { showPrivacyPolicyDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    HorizontalDivider(color = EditorialOutline)
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "1. Penyimpanan Data 100% Offline Lokal",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = EditorialPrimaryBlue
                        )
                        Text(
                            text = "TokoPOS dirancang sepenuhnya offline. Seluruh data transaksi, pelanggan, katalog produk, dan laporan keuangan tersimpan aman secara lokal di memori smartphone Anda (SQLite Room). Aplikasi tidak memiliki server eksternal dan tidak mengumpulkan, menyalin, ataupun menjual data toko Anda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialNavy
                        )

                        Text(
                            text = "2. Izin Kamera (Scan Barcode & QRIS)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = EditorialPrimaryBlue
                        )
                        Text(
                            text = "Izin kamera digunakan secara eksklusif untuk mendeteksi barcode kemasan produk dan scan QR Code menggunakan pustaka Google ML Kit langsung pada perangkat (on-device). Kamera tidak mengambil foto, tidak merekam video, dan tidak mengunggah data visual ke internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialNavy
                        )

                        Text(
                            text = "3. Izin Bluetooth (Printer Struk Thermal)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = EditorialPrimaryBlue
                        )
                        Text(
                            text = "Izin Bluetooth hanya digunakan untuk menghubungkan smartphone ke printer thermal Bluetooth (ESC/POS) guna mencetak struk kasir dan kuitansi nota. Aplikasi tidak menggunakan Bluetooth untuk melacak posisi/lokasi fisik pengguna (neverForLocation).",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialNavy
                        )

                        Text(
                            text = "4. Ekspor File & Backup Cadangan",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = EditorialPrimaryBlue
                        )
                        Text(
                            text = "File cadangan database (.json) dan ekspor laporan keuangan (.csv) hanya diproses saat Anda meminta ekspor secara manual. File dibagikan melalui protokol Android FileProvider yang terisolasi dan aman.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialNavy
                        )

                        Text(
                            text = "5. Kendali Penuh & Penghapusan Data",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = EditorialPrimaryBlue
                        )
                        Text(
                            text = "Anda memiliki kendali penuh atas data bisnis Anda. Anda dapat mereset data toko kapan saja melalui menu Pengaturan, atau menghapus seluruh data secara permanen cukup dengan menghapus data aplikasi atau mencopot pemasangan (uninstall) aplikasi dari perangkat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialNavy
                        )

                        Text(
                            text = "6. Kontak & Pengembang Aplikasi",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = EditorialPrimaryBlue
                        )
                        Text(
                            text = "Pengembang: ALIJAYA NET\nEmail: alijayanet@gmail.com\nTelepon/WhatsApp: 081947215703\nPaket: com.alijaya.tokopos.app",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showPrivacyPolicyDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
