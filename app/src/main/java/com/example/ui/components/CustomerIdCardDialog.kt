package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StoreProfile
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialSuccessGreen
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.EditorialTextDark
import com.example.util.QrCodeGenerator
import com.example.util.SubscriptionWhatsAppUtils
import com.example.util.ThermalPrinterManager
import kotlinx.coroutines.launch

data class CustomerCardData(
    val id: Long,
    val customerCode: String,
    val name: String,
    val phone: String,
    val address: String,
    val packageName: String? = null,
    val monthlyPrice: Double? = null,
    val dueDay: Int? = null,
    val isSubscription: Boolean = false,
    val status: String = "ACTIVE"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerIdCardDialog(
    customerData: CustomerCardData,
    storeProfile: StoreProfile?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val storeName = storeProfile?.storeName?.ifBlank { "Toko Ali Jaya" } ?: "Toko Ali Jaya"
    val storePhone = storeProfile?.phone ?: ""

    val barcodeCode = customerData.customerCode.ifBlank {
        if (customerData.isSubscription) "PLG-${customerData.id}" else "CUST-${customerData.id}"
    }

    // Generate Barcode and QR Code bitmaps
    val barcodeBitmap = remember(barcodeCode) {
        QrCodeGenerator.generateBarcodeBitmap(barcodeCode, 480, 140)
    }

    var showPrinterDropdown by remember { mutableStateOf(false) }
    val pairedPrinters = remember { ThermalPrinterManager.getPairedPrinters(context) }
    var selectedPrinter by remember { mutableStateOf(pairedPrinters.firstOrNull()) }
    var isPrinting by remember { mutableStateOf(false) }
    var copies by remember { mutableIntStateOf(1) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp)),
            color = EditorialCardWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kartu Member / ID Pelanggan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EditorialNavy
                        )
                        Text(
                            text = "Barcode dapat discan saat pembayaran",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = EditorialNavy)
                    }
                }

                HorizontalDivider(color = EditorialOutline.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // DIGITAL ID CARD PREVIEW (CREDIT CARD STYLE)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(EditorialPrimaryBlue.copy(alpha = 0.6f), Color(0xFF1E293B))
                                ),
                                RoundedCornerShape(22.dp)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Modern Dark Slate Card
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Top Row: Store Name & Member Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = storeName.uppercase(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                    if (storePhone.isNotBlank()) {
                                        Text(
                                            text = "CS: $storePhone",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (customerData.isSubscription) EditorialPrimaryBlue else Color(0xFF0284C7)
                                ) {
                                    Text(
                                        text = if (customerData.isSubscription) "KARTU LANGGANAN" else "MEMBER TOKO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Customer Details Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Initials
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(EditorialPrimaryBlue, Color(0xFF38BDF8)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = customerData.name.split(" ")
                                        .take(2)
                                        .mapNotNull { it.firstOrNull()?.uppercase() }
                                        .joinToString("")
                                        .ifEmpty { "P" }
                                    Text(
                                        text = initials,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customerData.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "ID: $barcodeCode",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp
                                        )
                                        if (customerData.phone.isNotBlank()) {
                                            Text(
                                                text = "• ${customerData.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.75f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            if (customerData.packageName != null || customerData.dueDay != null || customerData.address.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (customerData.packageName != null) {
                                        Text(
                                            text = "Paket: ${customerData.packageName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 10.sp
                                        )
                                    }
                                    if (customerData.dueDay != null) {
                                        Text(
                                            text = "Tempo Tiap Tgl ${customerData.dueDay}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFDE047),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Barcode Container (High Contrast White Container)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (barcodeBitmap != null) {
                                        Image(
                                            bitmap = barcodeBitmap.asImageBitmap(),
                                            contentDescription = "Customer Barcode",
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .height(60.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = barcodeCode,
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // ACTION BUTTONS (CETAK, WHATSAPP, SALIN)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Action: Send ID Card via WhatsApp
                    Button(
                        onClick = {
                            val cardMessage = buildString {
                                append("💳 *KARTU PELANGGAN RESMI - ${storeName.uppercase()}*\n")
                                append("━━━━━━━━━━━━━━━━━━━━\n")
                                append("Halo Kak *${customerData.name}*! Berikut adalah ID Card & Barcode Pelanggan Anda:\n\n")
                                append("👤 *Nama*: ${customerData.name}\n")
                                append("🆔 *ID Pelanggan*: `${barcodeCode}`\n")
                                if (!customerData.packageName.isNullOrBlank()) {
                                    append("📦 *Paket Layanan*: ${customerData.packageName}\n")
                                }
                                if (customerData.monthlyPrice != null) {
                                    append("💰 *Tarif Bulanan*: ${com.example.ui.components.CurrencyUtils.formatRupiah(customerData.monthlyPrice)}\n")
                                }
                                if (customerData.dueDay != null) {
                                    append("📅 *Jatuh Tempo*: Tiap Tanggal ${customerData.dueDay}\n")
                                }
                                if (customerData.address.isNotBlank()) {
                                    append("📍 *Alamat*: ${customerData.address}\n")
                                }
                                append("━━━━━━━━━━━━━━━━━━━━\n")
                                append("📌 *Info Pembayaran*: Cukup tunjukkan atau sebutkan ID Pelanggan `${barcodeCode}` ini saat membayar di kasir atau konfirmasi via transfer.\n\n")
                                append("Terima kasih telah menjadi pelanggan setia kami! 🙏✨")
                            }

                            SubscriptionWhatsAppUtils.sendWhatsAppMessage(
                                context = context,
                                phone = customerData.phone,
                                message = cardMessage
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialSuccessGreen),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim ID Card via WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    // Printer Selection & Thermal Print Button
                    if (pairedPrinters.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = showPrinterDropdown,
                                onExpandedChange = { showPrinterDropdown = !showPrinterDropdown },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedPrinter?.name ?: "Pilih Printer",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPrinterDropdown) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                ExposedDropdownMenu(
                                    expanded = showPrinterDropdown,
                                    onDismissRequest = { showPrinterDropdown = false }
                                ) {
                                    pairedPrinters.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p.name, fontSize = 12.sp) },
                                            onClick = {
                                                selectedPrinter = p
                                                showPrinterDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    selectedPrinter?.let { printer ->
                                        isPrinting = true
                                        scope.launch {
                                            val result = ThermalPrinterManager.printCustomerIdCard(
                                                deviceAddress = printer.address,
                                                storeName = storeName,
                                                customerName = customerData.name,
                                                customerCode = barcodeCode,
                                                packageName = customerData.packageName,
                                                phone = customerData.phone,
                                                copies = copies
                                            )
                                            isPrinting = false
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "ID Card Berhasil Dicetak!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                enabled = selectedPrinter != null && !isPrinting,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPrinting) "Mencetak..." else "Cetak Thermal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Copy Code Button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ID Pelanggan", barcodeCode))
                            Toast.makeText(context, "ID Pelanggan '$barcodeCode' disalin!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin ID Barcode: $barcodeCode", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
