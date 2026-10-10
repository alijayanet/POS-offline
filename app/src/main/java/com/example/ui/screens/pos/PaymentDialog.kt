package com.example.ui.screens.pos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Customer
import com.example.data.model.PaymentMethod
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryGreen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.model.StoreProfile
import com.example.util.QrCodeGenerator
import com.example.util.QrisEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    grandTotal: Double,
    customers: List<Customer>,
    selectedCustomer: Customer?,
    storeProfile: StoreProfile?,
    onSelectCustomer: (Customer?) -> Unit,
    onConfirmPayment: (method: PaymentMethod, paidAmount: Double, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var paidAmountText by remember { mutableStateOf(grandTotal.toLong().toString()) }
    var notesText by remember { mutableStateOf("") }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    val paidAmountNum = paidAmountText.toDoubleOrNull() ?: 0.0
    val changeAmount = (paidAmountNum - grandTotal).coerceAtLeast(0.0)

    var dynamicQrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var merchantName by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(selectedMethod, grandTotal, storeProfile) {
        if (selectedMethod == PaymentMethod.QRIS) {
            val staticPayload = storeProfile?.qrisStaticPayload ?: ""
            if (staticPayload.isNotBlank()) {
                try {
                    val dynamic = QrisEngine.convertStaticQrisToDynamic(staticPayload, grandTotal)
                    merchantName = QrisEngine.getMerchantNameFromPayload(staticPayload)
                    dynamicQrBitmap = QrCodeGenerator.generateQrBitmap(dynamic, 600, 600)
                } catch (e: Exception) {
                    merchantName = storeProfile?.storeName ?: "Toko Kasir"
                    dynamicQrBitmap = QrCodeGenerator.generateQrBitmap(staticPayload, 600, 600)
                }
            } else {
                val demoPayload = "00020101021226580014ID.LINKAJA.WWW01189360091100220033000215000000000000000520454115303360540" + grandTotal.toLong() + "5802ID5914TOKO ALI JAYA6007JAKARTA6304"
                merchantName = storeProfile?.storeName ?: "Toko Ali Jaya"
                dynamicQrBitmap = QrCodeGenerator.generateQrBitmap(demoPayload, 600, 600)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("payment_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Pinned Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pembayaran Kasir",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // 2. Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {

                // Grand Total Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL TAGIHAN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatRupiah(grandTotal),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer Selection
                Text(
                    text = "Pelanggan:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = customerDropdownExpanded,
                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.name ?: "Pelanggan Umum (Cash)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("select_customer_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pelanggan Umum (Tanpa Nama)") },
                            onClick = {
                                onSelectCustomer(null)
                                customerDropdownExpanded = false
                            }
                        )
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Medium)
                                        if (customer.totalDebt > 0) {
                                            Text(
                                                "Hutang: ${CurrencyUtils.formatRupiah(customer.totalDebt)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AlertRed
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectCustomer(customer)
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Methods Options
                Text(
                    text = "Metode Pembayaran:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethodChip(
                        title = "Tunai",
                        icon = Icons.Default.Payments,
                        isSelected = selectedMethod == PaymentMethod.CASH,
                        color = PrimaryGreen,
                        onClick = {
                            selectedMethod = PaymentMethod.CASH
                            paidAmountText = grandTotal.toLong().toString()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodChip(
                        title = "QRIS",
                        icon = Icons.Default.QrCode,
                        isSelected = selectedMethod == PaymentMethod.QRIS,
                        color = AccentPurple,
                        onClick = {
                            selectedMethod = PaymentMethod.QRIS
                            paidAmountText = grandTotal.toLong().toString()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodChip(
                        title = "Transfer",
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedMethod == PaymentMethod.TRANSFER,
                        color = AccentBlue,
                        onClick = {
                            selectedMethod = PaymentMethod.TRANSFER
                            paidAmountText = grandTotal.toLong().toString()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodChip(
                        title = "Kasbon",
                        icon = Icons.Default.MenuBook,
                        isSelected = selectedMethod == PaymentMethod.DEBT,
                        color = AccentAmber,
                        onClick = {
                            selectedMethod = PaymentMethod.DEBT
                            paidAmountText = "0"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specific Method Interface
                when (selectedMethod) {
                    PaymentMethod.CASH -> {
                        Text(
                            text = "Nominal Uang Diterima:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = paidAmountText,
                            onValueChange = { paidAmountText = it.filter { ch -> ch.isDigit() } },
                            leadingIcon = { Text("Rp ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                            trailingIcon = {
                                if (paidAmountText.isNotEmpty()) {
                                    IconButton(onClick = { paidAmountText = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cash_paid_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1. Prominent Uang Pas Card
                        val isUangPasSelected = paidAmountText == grandTotal.toLong().toString()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { paidAmountText = grandTotal.toLong().toString() }
                                .border(
                                    width = if (isUangPasSelected) 2.dp else 1.dp,
                                    color = if (isUangPasSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUangPasSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = if (isUangPasSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Uang Pas",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUangPasSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = CurrencyUtils.formatRupiah(grandTotal),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUangPasSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Quick Nominals Grid (2x2)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(20000, 50000).forEach { amount ->
                                val isSelected = paidAmountText == amount.toString()
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { paidAmountText = amount.toString() }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Rp ${amount / 1000}.000",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100000, 200000).forEach { amount ->
                                val isSelected = paidAmountText == amount.toString()
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { paidAmountText = amount.toString() }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Rp ${amount / 1000}.000",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Kembalian Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (paidAmountNum >= grandTotal) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (paidAmountNum >= grandTotal) "Kembalian:" else "Uang Kurang:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (paidAmountNum >= grandTotal) PrimaryGreen else AlertRed
                                )
                                Text(
                                    text = if (paidAmountNum >= grandTotal) CurrencyUtils.formatRupiah(changeAmount) else CurrencyUtils.formatRupiah(grandTotal - paidAmountNum),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (paidAmountNum >= grandTotal) PrimaryGreen else AlertRed
                                )
                            }
                        }
                    }

                    PaymentMethod.QRIS -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "QRIS Dinamis (Nominal Pas)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = merchantName.ifBlank { storeProfile?.storeName ?: "Toko Kasir" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                if (dynamicQrBitmap != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(190.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White)
                                            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = dynamicQrBitmap!!.asImageBitmap(),
                                            contentDescription = "Dynamic QRIS Code",
                                            modifier = Modifier.size(174.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(170.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Membuat QR Code...", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Nominal: " + CurrencyUtils.formatRupiah(grandTotal),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "Pelanggan scan QRIS dan nominal langsung terisi otomatis tanpa perlu ketik manual.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    PaymentMethod.TRANSFER -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "Rekening Bank Toko:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• BCA: 8820 1928 334 (Ali Jaya)", style = MaterialTheme.typography.bodyMedium)
                                Text("• BRI: 0123 0100 4567 508 (Ali Jaya)", style = MaterialTheme.typography.bodyMedium)
                                Text("• Mandiri: 123 00 9876543 1 (Ali Jaya)", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    PaymentMethod.DEBT -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCustomer != null) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                if (selectedCustomer == null) {
                                    Text(
                                        text = "⚠️ Perhatian: Pilih Nama Pelanggan di atas untuk mencatat Kasbon / Hutang.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertRed
                                    )
                                } else {
                                    Text(
                                        text = "Catatan Kasbon untuk: ${selectedCustomer.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F)
                                    )
                                    Text(
                                        text = "Total yang akan dicatat sebagai hutang: ${CurrencyUtils.formatRupiah(grandTotal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF78350F)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes Field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan Transaksi (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                }

                // 3. Pinned Bottom Footer Action Bar
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    val canSubmit = when (selectedMethod) {
                        PaymentMethod.CASH -> paidAmountNum >= grandTotal
                        PaymentMethod.DEBT -> selectedCustomer != null
                        PaymentMethod.QRIS, PaymentMethod.TRANSFER -> true
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (canSubmit) {
                                onConfirmPayment(selectedMethod, paidAmountNum, notesText)
                                onDismiss()
                            }
                        },
                        enabled = canSubmit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_payment_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Konfirmasi & Bayar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
