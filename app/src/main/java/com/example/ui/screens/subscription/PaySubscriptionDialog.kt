package com.example.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubscriptionCustomer
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialSuccessGreen
import com.example.ui.theme.EditorialSuccessGreenContainer

import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Print

@Composable
fun PaySubscriptionDialog(
    billingItem: CustomerBillingItem,
    onDismiss: () -> Unit,
    onConfirmPayment: (monthsCount: Int, paymentMethod: String, notes: String, sendWhatsApp: Boolean, printThermal: Boolean) -> Unit
) {
    val customer = billingItem.customer
    val initialMonths = if (billingItem.monthsDue > 0) billingItem.monthsDue else 1

    var selectedMonths by remember { mutableIntStateOf(initialMonths) }
    var selectedMethod by remember { mutableStateOf("TUNAI") }
    var notes by remember { mutableStateOf("") }
    var printThermalReceipt by remember { mutableStateOf(true) }
    var sendWhatsAppReceipt by remember { mutableStateOf(customer.phone.isNotBlank()) }

    val totalAmount = customer.monthlyPrice * selectedMonths

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bayar Tagihan Langganan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EditorialNavy
                        )
                        Text(
                            text = customer.name + if (customer.customerCode.isNotBlank()) " (${customer.customerCode})" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (billingItem.monthsDue > 1) AlertRedContainer.copy(alpha = 0.5f) else EditorialSuccessGreenContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Paket: ${customer.packageName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tarif: ${CurrencyUtils.formatRupiah(customer.monthlyPrice)} / bulan",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (billingItem.monthsDue > 1) {
                            Text(
                                text = "Menunggak ${billingItem.monthsDue} Bulan",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        } else if (billingItem.status == BillingStatus.PAID) {
                            Text(
                                text = "Status: LUNAS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EditorialSuccessGreen
                            )
                        } else {
                            Text(
                                text = "Jatuh Tempo tgl ${customer.dueDay}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EditorialSuccessGreen
                            )
                        }
                    }
                }

                if (billingItem.status == BillingStatus.PAID) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EditorialSuccessGreenContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = EditorialSuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pelanggan sudah LUNAS s/d ${CurrencyUtils.formatDateOnly(customer.paidUntilTimestamp)}. Pembayaran ini akan memperpanjang masa aktif ke bulan berikutnya.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EditorialNavy,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month Duration Selector
                Text(
                    text = "Pilih Durasi Pembayaran:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presetMonths = listOf(1, 2, 3, 6, 12)
                    presetMonths.forEach { m ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMonths = m }
                                .border(
                                    width = if (selectedMonths == m) 2.dp else 1.dp,
                                    color = if (selectedMonths == m) EditorialPrimaryBlue else EditorialOutline,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMonths == m) EditorialPrimaryBlue.copy(alpha = 0.15f) else EditorialCardWhite
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (m == 12) "1 Thn" else "$m Bln",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedMonths == m) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedMonths == m) EditorialPrimaryBlue else EditorialNavy
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Plus Minus Custom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jumlah Bulan Bayar:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (selectedMonths > 1) selectedMonths-- },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang")
                        }

                        Text(
                            text = "$selectedMonths Bulan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = { if (selectedMonths < 36) selectedMonths++ },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(EditorialPrimaryBlue.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", tint = EditorialPrimaryBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method
                Text(
                    text = "Metode Pembayaran:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("TUNAI", "TRANSFER", "QRIS").forEach { method ->
                        val isSelected = selectedMethod == method
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMethod = method }
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
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EditorialPrimaryBlue else EditorialNavy,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Summary Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EditorialNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL PEMBAYARAN",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$selectedMonths Bulan x ${CurrencyUtils.formatRupiah(customer.monthlyPrice)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Text(
                            text = CurrencyUtils.formatRupiah(totalAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Output Actions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { printThermalReceipt = !printThermalReceipt }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = printThermalReceipt,
                                onCheckedChange = { printThermalReceipt = it },
                                colors = CheckboxDefaults.colors(checkedColor = EditorialPrimaryBlue)
                            )
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = EditorialPrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cetak Struk Kuitansi (Printer Thermal)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EditorialNavy
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sendWhatsAppReceipt = !sendWhatsAppReceipt }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sendWhatsAppReceipt,
                                onCheckedChange = { sendWhatsAppReceipt = it },
                                colors = CheckboxDefaults.colors(checkedColor = EditorialSuccessGreen)
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = EditorialSuccessGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kirim Kuitansi Lunas via WhatsApp",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EditorialNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            onConfirmPayment(selectedMonths, selectedMethod, notes, sendWhatsAppReceipt, printThermalReceipt)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialSuccessGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (billingItem.status == BillingStatus.PAID) "Konfirmasi Perpanjangan" else "Konfirmasi & Bayar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
