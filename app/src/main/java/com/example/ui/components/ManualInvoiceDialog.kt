package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import com.example.ui.screens.manualinvoice.ManualItemState
import com.example.ui.screens.manualinvoice.QuickUnits
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialSuccessGreen
import com.example.ui.theme.EditorialSuccessGreenContainer
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.PrimaryGreen
import com.example.util.ThermalPrinterManager
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ManualInvoiceDialog(
    storeProfile: StoreProfile?,
    onDismiss: () -> Unit,
    onSaveTransaction: (TransactionOrder, List<TransactionItem>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val initialInvoiceNo = remember {
        "NOTA-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
    }

    var invoiceNumber by remember { mutableStateOf(initialInvoiceNo) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("TUNAI") }
    var paidAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val items = remember {
        mutableStateListOf(
            ManualItemState(initialName = "", initialQty = "1", initialUnit = "Pcs", initialPrice = "")
        )
    }

    // Live Calculations
    val validItems = items.filter { it.name.isNotBlank() || it.price > 0 }
    val totalKindsCount = validItems.size
    val totalUnitsCount = items.sumOf { it.quantity }
    val subtotal = items.sumOf { it.subtotal }
    val discount = discountText.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
    val grandTotal = (subtotal - discount).coerceAtLeast(0.0)

    val paidAmount = paidAmountText.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: grandTotal
    val changeAmount = if (selectedMethod == "TUNAI") (paidAmount - grandTotal).coerceAtLeast(0.0) else 0.0

    val isFormValid = items.any { it.name.isNotBlank() && it.price > 0 }

    fun buildOrderAndItems(): Pair<TransactionOrder, List<TransactionItem>> {
        val validRows = items.filter { it.name.isNotBlank() && it.price > 0 }
        val order = TransactionOrder(
            invoiceNumber = invoiceNumber.trim().ifBlank { initialInvoiceNo },
            customerName = customerName.trim().ifBlank { "Pelanggan Umum" },
            subtotal = subtotal,
            discountAmount = discount,
            taxAmount = 0.0,
            grandTotal = grandTotal,
            paidAmount = if (selectedMethod == "TUNAI") paidAmount else grandTotal,
            changeAmount = changeAmount,
            paymentMethod = selectedMethod,
            totalProfit = grandTotal * 0.3,
            notes = notes.trim(),
            timestamp = System.currentTimeMillis()
        )

        val txItems = validRows.map { row ->
            TransactionItem(
                orderId = 0L,
                productId = 0L,
                productName = row.name.trim(),
                category = "Nota Manual",
                buyPrice = 0.0,
                sellPrice = row.price,
                quantity = row.quantity,
                unit = row.unit.trim().ifBlank { "Pcs" },
                discount = 0.0,
                subtotal = row.subtotal,
                profit = row.subtotal * 0.3
            )
        }
        return Pair(order, txItems)
    }

    fun generateWhatsAppText(): String {
        val (order, txItems) = buildOrderAndItems()
        val storeName = storeProfile?.storeName?.ifBlank { "TOKO POS" } ?: "TOKO POS"
        val address = storeProfile?.address ?: ""
        val phone = storeProfile?.phone ?: ""

        val sb = StringBuilder()
        sb.append("🧾 *NOTA PEMBAYARAN*\n")
        sb.append("🏪 *").append(storeName.uppercase()).append("*\n")
        if (address.isNotBlank()) sb.append("📍 ").append(address).append("\n")
        if (phone.isNotBlank()) sb.append("📞 Telp/WA: ").append(phone).append("\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("No. Nota : `").append(order.invoiceNumber).append("`\n")
        sb.append("Tanggal  : ").append(CurrencyUtils.formatDate(order.timestamp)).append("\n")
        sb.append("Kepada   : *").append(order.customerName).append("*\n\n")

        sb.append("📋 *RINCIAN BARANG / JASA:*\n")
        for (item in txItems) {
            sb.append("• *").append(item.productName).append("*\n")
            sb.append("  ").append(CurrencyUtils.formatQty(item.quantity)).append(" ").append(item.unit)
            sb.append(" x ").append(CurrencyUtils.formatRupiah(item.sellPrice))
            sb.append(" = *").append(CurrencyUtils.formatRupiah(item.subtotal)).append("*\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Subtotal : ").append(CurrencyUtils.formatRupiah(order.subtotal)).append("\n")
        if (order.discountAmount > 0) {
            sb.append("Diskon   : -").append(CurrencyUtils.formatRupiah(order.discountAmount)).append("\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💰 *TOTAL AKHIR : ").append(CurrencyUtils.formatRupiah(order.grandTotal)).append("*\n")
        sb.append("Metode   : *").append(order.paymentMethod).append("*\n")
        if (order.paymentMethod == "TUNAI") {
            sb.append("Bayar    : ").append(CurrencyUtils.formatRupiah(order.paidAmount)).append("\n")
            sb.append("Kembali  : ").append(CurrencyUtils.formatRupiah(order.changeAmount)).append("\n")
        }
        if (order.notes.isNotBlank()) {
            sb.append("Catatan  : _").append(order.notes).append("_\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n\n")
        sb.append("_Terima kasih atas kunjungan dan kepercayaannya!_ 🙏✨")

        return sb.toString()
    }

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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
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
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = EditorialPrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Buat Nota Manual",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                            Text(
                                text = "Input nama, satuan & harga bebas seketika",
                                style = MaterialTheme.typography.bodySmall,
                                color = EditorialMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = EditorialOutline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Pelanggan & No Nota
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nama Pelanggan") },
                        placeholder = { Text("Pelanggan Umum") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("No. WhatsApp") },
                        placeholder = { Text("08xx") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DAFTAR BARANG / JASA MANUAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RINCIAN BARANG / JASA (${items.size} ITEM)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EditorialMuted,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = {
                            items.add(ManualItemState(initialName = "", initialQty = "1", initialUnit = "Pcs", initialPrice = ""))
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Baris", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Items list
                items.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, EditorialOutline.copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Item #${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialPrimaryBlue
                                )

                                if (items.size > 1) {
                                    IconButton(
                                        onClick = { items.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = AlertRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // 1. Nama / Jenis Barang
                            OutlinedTextField(
                                value = item.name,
                                onValueChange = { item.name = it },
                                label = { Text("Jenis / Nama Barang / Jasa") },
                                placeholder = { Text("Misal: Servis AC, Kain, Nasi Kotak...") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 2. Qty, Satuan, Harga
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = item.quantityText,
                                    onValueChange = { item.quantityText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                                    label = { Text("Jumlah") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(0.9f)
                                )

                                OutlinedTextField(
                                    value = item.unit,
                                    onValueChange = { item.unit = it },
                                    label = { Text("Satuan") },
                                    placeholder = { Text("Pcs") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(0.9f)
                                )

                                OutlinedTextField(
                                    value = item.priceText,
                                    onValueChange = { item.priceText = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Harga Satuan (Rp)") },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.4f)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick Satuan selector chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                QuickUnits.forEach { u ->
                                    val isSelected = item.unit.equals(u, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) EditorialPrimaryBlue else EditorialCardWhite,
                                        border = BorderStroke(1.dp, if (isSelected) EditorialPrimaryBlue else EditorialOutline),
                                        modifier = Modifier.clickable { item.unit = u }
                                    ) {
                                        Text(
                                            text = u,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else EditorialNavy,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Subtotal baris
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subtotal Item:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EditorialMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiah(item.subtotal),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialNavy
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // RINGKASAN & PEMBAYARAN
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EditorialNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total $totalKindsCount Jenis ($totalUnitsCount Satuan)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(subtotal),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        if (discount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Potongan Diskon",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFCA5A5)
                                )
                                Text(
                                    text = "-${CurrencyUtils.formatRupiah(discount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL AKHIR",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(grandTotal),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Diskon & Metode Pembayaran
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Diskon (Rp)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = paidAmountText,
                        onValueChange = { paidAmountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Uang Dibayar (Rp)") },
                        placeholder = { Text(CurrencyUtils.formatRupiahWithoutSymbol(grandTotal)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedMethod == "TUNAI" && paidAmount >= grandTotal && grandTotal > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kembalian:",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialMuted
                        )
                        Text(
                            text = CurrencyUtils.formatRupiah(changeAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EditorialPrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metode Bayar Selector
                Text(
                    text = "Metode Pembayaran:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EditorialMuted
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("TUNAI", "TRANSFER", "QRIS", "HUTANG").forEach { m ->
                        val isSelected = selectedMethod == m
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMethod = m }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) EditorialPrimaryBlue else EditorialOutline,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) EditorialPrimaryBlue.copy(alpha = 0.15f) else EditorialCardWhite
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = m,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EditorialPrimaryBlue else EditorialNavy
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    placeholder = { Text("Misal: Garansi servis 30 hari...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // AKSI NOTA MANUAL (Cetak, WA, Simpan)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Cetak & WhatsApp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Cetak Thermal
                        OutlinedButton(
                            onClick = {
                                if (isFormValid) {
                                    val (order, txItems) = buildOrderAndItems()
                                    val printerMac = storeProfile?.bluetoothPrinterMac ?: ""
                                    val paperSize = storeProfile?.paperSize ?: "58mm"

                                    if (printerMac.isNotBlank()) {
                                        scope.launch {
                                            val res = ThermalPrinterManager.printBluetoothReceipt(
                                                context = context,
                                                deviceAddress = printerMac,
                                                order = order,
                                                items = txItems,
                                                storeProfile = storeProfile,
                                                paperSize = paperSize
                                            )
                                            if (res.isFailure) {
                                                Toast.makeText(context, "Gagal cetak: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Struk nota manual dicetak!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        val paired = ThermalPrinterManager.getPairedPrinters(context)
                                        if (paired.isNotEmpty()) {
                                            scope.launch {
                                                ThermalPrinterManager.printBluetoothReceipt(
                                                    context = context,
                                                    deviceAddress = paired.first().address,
                                                    order = order,
                                                    items = txItems,
                                                    storeProfile = storeProfile,
                                                    paperSize = paperSize
                                                )
                                            }
                                        } else {
                                            Toast.makeText(context, "Printer Bluetooth belum dipilih di Pengaturan", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Isi minimal 1 nama dan harga barang!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak Struk", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // 2. Kirim WhatsApp
                        OutlinedButton(
                            onClick = {
                                if (isFormValid) {
                                    val waText = generateWhatsAppText()
                                    var cleanPhone = customerPhone.replace(Regex("[^0-9]"), "")
                                    if (cleanPhone.startsWith("0")) cleanPhone = "62" + cleanPhone.substring(1)
                                    else if (cleanPhone.startsWith("8")) cleanPhone = "62" + cleanPhone

                                    if (cleanPhone.isNotBlank()) {
                                        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(waText, "UTF-8")}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, waText)
                                            setPackage("com.whatsapp")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        try {
                                            context.startActivity(sendIntent)
                                        } catch (_: Exception) {
                                            val chooser = Intent.createChooser(
                                                Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, waText)
                                                },
                                                "Kirim Nota via"
                                            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                            context.startActivity(chooser)
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Isi minimal 1 nama dan harga barang!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim WA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Row 2: Salin Teks & Simpan ke Penjualan Toko
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3. Salin Teks
                        OutlinedButton(
                            onClick = {
                                if (isFormValid) {
                                    val waText = generateWhatsAppText()
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("Nota Manual", waText))
                                    Toast.makeText(context, "Rincian nota berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin", fontSize = 12.sp)
                        }

                        // 4. Simpan Transaksi Penjualan
                        Button(
                            onClick = {
                                if (isFormValid) {
                                    val (order, txItems) = buildOrderAndItems()
                                    onSaveTransaction(order, txItems)
                                    Toast.makeText(context, "Transaksi nota manual berhasil disimpan ke penjualan toko!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Isi minimal 1 nama dan harga barang!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                            modifier = Modifier.weight(1.4f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Penjualan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
