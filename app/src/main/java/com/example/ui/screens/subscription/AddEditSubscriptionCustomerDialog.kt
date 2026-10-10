package com.example.ui.screens.subscription

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubscriptionCustomer
import com.example.data.model.SubscriptionPackage
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import com.example.ui.theme.EditorialSuccessGreen
import com.example.ui.theme.EditorialSuccessGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionCustomerDialog(
    customerToEdit: SubscriptionCustomer? = null,
    packages: List<SubscriptionPackage>,
    onDismiss: () -> Unit,
    onSave: (SubscriptionCustomer) -> Unit
) {
    var customerCode by remember {
        mutableStateOf(customerToEdit?.customerCode ?: ("PLG-" + (100..999).random()))
    }
    var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
    var address by remember { mutableStateOf(customerToEdit?.address ?: "") }

    var selectedPackage by remember {
        mutableStateOf(
            packages.find { it.id == customerToEdit?.packageId } ?: packages.firstOrNull()
        )
    }
    var packageDropdownExpanded by remember { mutableStateOf(false) }

    var dueDayText by remember {
        mutableStateOf((customerToEdit?.dueDay ?: 10).toString())
    }
    var status by remember { mutableStateOf(customerToEdit?.status ?: "ACTIVE") }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(customerToEdit?.notes ?: "") }
    var isAlreadyPaidThisMonth by remember { mutableStateOf(false) }

    val dueDayNum = dueDayText.toIntOrNull()?.coerceIn(1, 31) ?: 10
    val isFormValid = name.isNotBlank() && selectedPackage != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
            color = EditorialCardWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (customerToEdit == null) "Tambah Pelanggan Langganan" else "Edit Data Pelanggan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EditorialNavy
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customerCode,
                        onValueChange = { customerCode = it },
                        label = { Text("ID Pelanggan") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.45f)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap *") },
                        placeholder = { Text("Budi Santoso") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.55f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. WhatsApp / HP") },
                    placeholder = { Text("081234567890") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat / Lokasi Pemasangan") },
                    placeholder = { Text("Jl. Merpati No. 15, RT 02/04") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Package Dropdown
                ExposedDropdownMenuBox(
                    expanded = packageDropdownExpanded,
                    onExpandedChange = { packageDropdownExpanded = !packageDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPackage?.let { "${it.name} (${CurrencyUtils.formatRupiah(it.price)})" } ?: "Pilih Paket",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilihan Paket *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = packageDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = packageDropdownExpanded,
                        onDismissRequest = { packageDropdownExpanded = false }
                    ) {
                        packages.forEach { pkg ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(pkg.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${CurrencyUtils.formatRupiah(pkg.price)} / bulan ${if (pkg.speedInfo.isNotBlank()) "• ${pkg.speedInfo}" else ""}",
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPackage = pkg
                                    packageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Tanggal Jatuh Tempo (1-31)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = when (status) {
                                "ACTIVE" -> "🟢 Aktif"
                                "ISOLIR" -> "🔴 Terisolir"
                                else -> "⚪ Non-aktif"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🟢 Aktif") },
                                onClick = { status = "ACTIVE"; statusDropdownExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("🔴 Terisolir") },
                                onClick = { status = "ISOLIR"; statusDropdownExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("⚪ Non-aktif") },
                                onClick = { status = "INACTIVE"; statusDropdownExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / No. Port / Modem (Opsional)") },
                    placeholder = { Text("Contoh: Port 3 ODP-04...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (customerToEdit == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAlreadyPaidThisMonth = !isAlreadyPaidThisMonth }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAlreadyPaidThisMonth,
                                onCheckedChange = { isAlreadyPaidThisMonth = it },
                                colors = CheckboxDefaults.colors(checkedColor = EditorialSuccessGreen)
                            )
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = "Sudah Bayar untuk Bulan Pertama",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EditorialNavy
                                )
                                Text(
                                    text = if (isAlreadyPaidThisMonth) "Status: Lunas s/d bulan depan" else "Status: Buat tagihan pertama di daftar tagihan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val pkg = selectedPackage!!
                                val initialPaidUntil = if (customerToEdit != null) {
                                    customerToEdit.paidUntilTimestamp
                                } else {
                                    if (isAlreadyPaidThisMonth) {
                                        val cal = java.util.Calendar.getInstance()
                                        cal.set(java.util.Calendar.DAY_OF_MONTH, dueDayNum.coerceIn(1, 28))
                                        cal.add(java.util.Calendar.MONTH, 1)
                                        cal.timeInMillis
                                    } else {
                                        0L // Fresh unpaid subscription: bill is automatically generated!
                                    }
                                }

                                onSave(
                                    SubscriptionCustomer(
                                        id = customerToEdit?.id ?: 0L,
                                        customerCode = customerCode.trim(),
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        address = address.trim(),
                                        packageId = pkg.id,
                                        packageName = pkg.name,
                                        monthlyPrice = pkg.price,
                                        installationDate = customerToEdit?.installationDate ?: System.currentTimeMillis(),
                                        dueDay = dueDayNum,
                                        paidUntilTimestamp = initialPaidUntil,
                                        status = status,
                                        notes = notes.trim(),
                                        createdAt = customerToEdit?.createdAt ?: System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                    ) {
                        Text("Simpan Pelanggan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
