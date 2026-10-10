package com.example.ui.screens.manualinvoice

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import com.example.ui.components.CurrencyUtils
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
import com.example.ui.theme.EditorialTextDark
import com.example.ui.theme.PrimaryGreen
import com.example.util.ThermalPrinterManager
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Highly reactive item state holder for Compose text inputs.
 */
class ManualItemState(
    val id: String = UUID.randomUUID().toString(),
    initialName: String = "",
    initialQty: String = "1",
    initialUnit: String = "Pcs",
    initialPrice: String = ""
) {
    var name by mutableStateOf(initialName)
    var quantityText by mutableStateOf(initialQty)
    var unit by mutableStateOf(initialUnit)
    var priceText by mutableStateOf(initialPrice)

    val quantity: Double
        get() = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0

    val price: Double
        get() = priceText.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

    val subtotal: Double
        get() = quantity * price
}

val QuickUnits = listOf("Pcs", "Kg", "Meter", "Porsi", "Paket", "Lembar", "Liter", "Box", "Set", "Lusin", "Jam", "Hari", "Bungkus")

@Composable
fun ManualInvoiceScreen(
    storeProfile: StoreProfile?,
    onSaveTransaction: (TransactionOrder, List<TransactionItem>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var invoiceNumber by remember {
        mutableStateOf("NOTA-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date()))
    }
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

    // Live Reactive Calculations
    val validItems = items.filter { it.name.isNotBlank() || it.price > 0 }
    val totalKindsCount = validItems.size
    val totalUnitsCount = items.sumOf { it.quantity }
    val subtotal = items.sumOf { it.subtotal }
    val discount = discountText.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
    val grandTotal = (subtotal - discount).coerceAtLeast(0.0)

    val paidAmount = paidAmountText.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: grandTotal
    val changeAmount = if (selectedMethod == "TUNAI") (paidAmount - grandTotal).coerceAtLeast(0.0) else 0.0

    val isFormValid = items.any { it.name.isNotBlank() && it.price > 0 }

    fun resetForm() {
        invoiceNumber = "NOTA-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        customerName = ""
        customerPhone = ""
        discountText = ""
        selectedMethod = "TUNAI"
        paidAmountText = ""
        notes = ""
        items.clear()
        items.add(ManualItemState(initialName = "", initialQty = "1", initialUnit = "Pcs", initialPrice = ""))
    }

    fun buildOrderAndItems(): Pair<TransactionOrder, List<TransactionItem>> {
        val validRows = items.filter { it.name.isNotBlank() && it.price > 0 }
        val order = TransactionOrder(
            invoiceNumber = invoiceNumber.trim().ifBlank { "NOTA-" + System.currentTimeMillis() },
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EditorialBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = EditorialPrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Nota Manual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                            Text(
                                text = "Kuitansi bebas barang, satuan & harga instan",
                                style = MaterialTheme.typography.bodySmall,
                                color = EditorialMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { resetForm() },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 2. Info Pelanggan & No Nota
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INFORMASI PELANGGAN & NOTA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EditorialMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Nama Pelanggan / Pembeli") },
                            placeholder = { Text("Pelanggan Umum") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
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

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("Nomor Nota / Invoice") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Section Title + Tambah Baris
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAFTAR BARANG / JASA (${items.size} ITEM)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EditorialMuted,
                    letterSpacing = 1.2.sp
                )

                Button(
                    onClick = {
                        items.add(ManualItemState(initialName = "", initialQty = "1", initialUnit = "Pcs", initialPrice = ""))
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. List of Items
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Item Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EditorialBlueContainer
                        ) {
                            Text(
                                text = "Item #${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EditorialPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (items.size > 1) {
                            IconButton(
                                onClick = { items.removeAt(index) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Hapus Item",
                                    tint = AlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Nama / Jenis
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { item.name = it },
                        label = { Text("Jenis / Nama Barang / Jasa") },
                        placeholder = { Text("Misal: Servis AC, Nasi Kotak, Kain Rayon...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Qty, Satuan, Harga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = item.quantityText,
                            onValueChange = { item.quantityText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                            label = { Text("Jumlah") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.9f)
                        )

                        OutlinedTextField(
                            value = item.unit,
                            onValueChange = { item.unit = it },
                            label = { Text("Satuan") },
                            placeholder = { Text("Pcs") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.9f)
                        )

                        OutlinedTextField(
                            value = item.priceText,
                            onValueChange = { item.priceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Harga Satuan (Rp)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.4f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Satuan Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickUnits.forEach { u ->
                            val isSelected = item.unit.equals(u, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) EditorialPrimaryBlue else EditorialSurfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) EditorialPrimaryBlue else EditorialOutline.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { item.unit = u }
                            ) {
                                Text(
                                    text = u,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else EditorialNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Subtotal Display Bar
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subtotal: ${CurrencyUtils.formatQty(item.quantity)} ${item.unit} x ${CurrencyUtils.formatRupiah(item.price)}",
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
        }

        // 5. Card Ringkasan & Total Akhir
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = EditorialNavy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                        Spacer(modifier = Modifier.height(6.dp))
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

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL AKHIR",
                            style = MaterialTheme.typography.titleMedium,
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
        }

        // 6. Form Pembayaran (Diskon, Bayar, Metode)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PEMBAYARAN & CATATAN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EditorialMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EditorialSuccessGreenContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Uang Kembalian:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EditorialNavy
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiah(changeAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialSuccessGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Metode Pembayaran:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EditorialMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) EditorialPrimaryBlue.copy(alpha = 0.15f) else EditorialCardWhite
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan (Opsional)") },
                        placeholder = { Text("Misal: Garansi servis 30 hari...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 7. Tombol Aksi Lengkap
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row A: Cetak Struk & Kirim WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                            Toast.makeText(context, "Struk nota manual berhasil dicetak!", Toast.LENGTH_SHORT).show()
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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Struk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim WA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Row B: Salin Teks & Simpan ke Penjualan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. Salin Teks
                    OutlinedButton(
                        onClick = {
                            if (isFormValid) {
                                val waText = generateWhatsAppText()
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Nota Manual", waText))
                                Toast.makeText(context, "Rincian nota disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // 4. Simpan Penjualan
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val (order, txItems) = buildOrderAndItems()
                                onSaveTransaction(order, txItems)
                                Toast.makeText(context, "Nota manual berhasil disimpan ke penjualan toko!", Toast.LENGTH_SHORT).show()
                                resetForm()
                            } else {
                                Toast.makeText(context, "Isi minimal 1 nama dan harga barang!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan ke Penjualan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
