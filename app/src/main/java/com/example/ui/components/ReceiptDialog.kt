package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PaymentMethod
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder

@Composable
fun ReceiptDialog(
    order: TransactionOrder,
    items: List<TransactionItem>,
    storeProfile: StoreProfile?,
    onDismiss: () -> Unit,
    onNewTransaction: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val storeName = storeProfile?.storeName ?: "Toko Ali Jaya"
    val storeAddress = storeProfile?.address ?: "Jl. Raya Pasar Baru No. 12"
    val storePhone = storeProfile?.phone ?: "0812-3456-7890"
    val footerNote = storeProfile?.receiptFooter ?: "Terima kasih atas kunjungan Anda!"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("receipt_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Struk Pembayaran",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_receipt_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thermal receipt container (paper style)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Store Info
                        Text(
                            text = storeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = storeAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Telp/WA: $storePhone",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Invoice metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "No: ${order.invoiceNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF334155)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tgl: ${CurrencyUtils.formatDate(order.timestamp)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF334155)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kasir: ${storeProfile?.ownerName ?: "Admin"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Pelanggan: ${order.customerName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF334155)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Items list
                        items.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF0F172A)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${CurrencyUtils.formatQty(item.quantity)} ${item.unit} x ${CurrencyUtils.formatRupiah(item.sellPrice)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRupiah(item.subtotal),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Totals
                        ReceiptLine(title = "Subtotal", value = CurrencyUtils.formatRupiah(order.subtotal))
                        if (order.discountAmount > 0) {
                            ReceiptLine(title = "Diskon", value = "-${CurrencyUtils.formatRupiah(order.discountAmount)}")
                        }
                        if (order.taxAmount > 0) {
                            ReceiptLine(title = "PPN", value = CurrencyUtils.formatRupiah(order.taxAmount))
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        ReceiptLine(
                            title = "TOTAL",
                            value = CurrencyUtils.formatRupiah(order.grandTotal),
                            isBold = true,
                            fontSize = 15
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        val methodEnum = PaymentMethod.fromString(order.paymentMethod)
                        ReceiptLine(title = "Metode", value = methodEnum.displayName)
                        ReceiptLine(title = "Bayar", value = CurrencyUtils.formatRupiah(order.paidAmount))
                        if (methodEnum == PaymentMethod.DEBT) {
                            val sisaHutang = order.grandTotal - order.paidAmount
                            ReceiptLine(
                                title = "Sisa Hutang",
                                value = CurrencyUtils.formatRupiah(sisaHutang),
                                isBold = true
                            )
                        } else {
                            ReceiptLine(
                                title = "Kembalian",
                                value = CurrencyUtils.formatRupiah(order.changeAmount)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = footerNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons: Share & Print & New Transaction
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                var isPrinting by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareReceiptText(context, order, items, storeProfile)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan WA")
                    }

                    Button(
                        onClick = {
                            val printerType = storeProfile?.printerType ?: "BLUETOOTH"
                            val printerMac = storeProfile?.bluetoothPrinterMac ?: ""
                            val paperSize = storeProfile?.paperSize ?: "58mm"

                            if (printerType == "RAWBT") {
                                val success = com.example.util.ThermalPrinterManager.printViaRawBt(
                                    context, order, items, storeProfile, paperSize
                                )
                                if (!success) {
                                    Toast.makeText(context, "Aplikasi RawBT belum terpasang. Menggunakan format teks...", Toast.LENGTH_SHORT).show()
                                    shareReceiptText(context, order, items, storeProfile)
                                }
                            } else {
                                if (printerMac.isBlank()) {
                                    // Try paired printer if exists
                                    val paired = com.example.util.ThermalPrinterManager.getPairedPrinters(context)
                                    if (paired.isNotEmpty()) {
                                        scope.launch {
                                            isPrinting = true
                                            val res = com.example.util.ThermalPrinterManager.printBluetoothReceipt(
                                                context, paired.first().address, order, items, storeProfile, paperSize
                                            )
                                            isPrinting = false
                                            if (res.isSuccess) {
                                                Toast.makeText(context, "Struk berhasil dicetak ke ${paired.first().name}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Gagal mencetak: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Printer Bluetooth belum diatur di Pengaturan Toko. Hubungkan printer di menu Pengaturan.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    scope.launch {
                                        isPrinting = true
                                        val res = com.example.util.ThermalPrinterManager.printBluetoothReceipt(
                                            context, printerMac, order, items, storeProfile, paperSize
                                        )
                                        isPrinting = false
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Gagal: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isPrinting,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_receipt_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Cetak")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPrinting) "Mencetak..." else "Cetak Struk")
                    }
                }

                if (onNewTransaction != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onDismiss()
                            onNewTransaction()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_transaction_from_receipt_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Transaksi Baru")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptLine(
    title: String,
    value: String,
    isBold: Boolean = false,
    fontSize: Int = 12
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontSize = fontSize.sp
            ),
            color = Color(0xFF0F172A)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontSize = fontSize.sp
            ),
            color = Color(0xFF0F172A)
        )
    }
}

private fun shareReceiptText(
    context: Context,
    order: TransactionOrder,
    items: List<TransactionItem>,
    storeProfile: StoreProfile?
) {
    com.example.util.WhatsAppReceiptSender.sendReceiptViaWhatsApp(
        context = context,
        order = order,
        items = items,
        profile = storeProfile
    )
}
