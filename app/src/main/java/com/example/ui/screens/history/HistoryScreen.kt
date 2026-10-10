package com.example.ui.screens.history

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionOrder
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.EditorialAlertRed
import com.example.ui.theme.EditorialAlertRedContainer
import com.example.ui.theme.EditorialAmber
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOnAlertRedContainer
import com.example.ui.theme.EditorialOnBlueContainer
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialPurple
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.EditorialTextDark

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var orderToCancel by remember { mutableStateOf<TransactionOrder?>(null) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Banner for Selected Period (Editorial Blue Container)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialBlueContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL PENJUALAN (${uiState.totalOrdersCount} TRANSAKSI)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EditorialOnBlueContainer.copy(alpha = 0.7f),
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatRupiah(uiState.totalRevenue),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = EditorialOnBlueContainer
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LABA BERSIH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EditorialOnBlueContainer.copy(alpha = 0.7f),
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatRupiah(uiState.totalProfit),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialPrimaryBlue
                            )
                        )
                    }
                }
            }
        }

        // Search Input Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari no. faktur atau nama pelanggan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EditorialMuted) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = EditorialMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input")
            )
        }

        // Time Period Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryDateFilter.entries.forEach { filter ->
                    item {
                        FilterChip(
                            selected = uiState.dateFilter == filter,
                            onClick = { viewModel.setDateFilter(filter) },
                            label = {
                                Text(
                                    text = filter.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (uiState.dateFilter == filter) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EditorialBlueContainer,
                                selectedLabelColor = EditorialOnBlueContainer
                            )
                        )
                    }
                }
            }
        }

        // Payment Method Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.paymentFilter == "Semua",
                        onClick = { viewModel.setPaymentFilter("Semua") },
                        label = { Text("Semua Metode") },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EditorialNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                PaymentMethod.entries.forEach { method ->
                    item {
                        FilterChip(
                            selected = uiState.paymentFilter.equals(method.name, ignoreCase = true) || uiState.paymentFilter.equals(method.displayName, ignoreCase = true),
                            onClick = { viewModel.setPaymentFilter(method.name) },
                            label = { Text(method.displayName) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EditorialNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Order List Header
        item {
            Text(
                text = "DAFTAR TRANSAKSI (${uiState.orders.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = EditorialMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Order List Cards
        if (uiState.orders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = EditorialMuted
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak ada transaksi penjualan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EditorialMuted
                            )
                        }
                    }
                }
            }
        } else {
            items(uiState.orders, key = { it.id }) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EditorialOutline, RoundedCornerShape(20.dp))
                        .clickable { viewModel.viewReceipt(order) }
                        .testTag("history_order_${order.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Top info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EditorialSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = EditorialNavy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = order.invoiceNumber,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = EditorialTextDark
                                        )
                                    )
                                    Text(
                                        text = CurrencyUtils.formatDate(order.timestamp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EditorialMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            // Payment method badge
                            val payMethod = PaymentMethod.fromString(order.paymentMethod)
                            val badgeColor = when (payMethod) {
                                PaymentMethod.CASH -> EditorialBlueContainer
                                PaymentMethod.QRIS -> EditorialPurple.copy(alpha = 0.15f)
                                PaymentMethod.TRANSFER -> EditorialBlueContainer
                                PaymentMethod.DEBT -> EditorialAlertRedContainer
                            }
                            val textColor = when (payMethod) {
                                PaymentMethod.CASH -> EditorialOnBlueContainer
                                PaymentMethod.QRIS -> EditorialPurple
                                PaymentMethod.TRANSFER -> EditorialPrimaryBlue
                                PaymentMethod.DEBT -> EditorialAlertRed
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = payMethod.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = textColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = EditorialOutline.copy(alpha = 0.4f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Customer & Total Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Pelanggan:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EditorialMuted,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = order.customerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = EditorialTextDark
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EditorialMuted,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiah(order.grandTotal),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EditorialPrimaryBlue
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.viewReceipt(order) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lihat Struk", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { orderToCancel = order },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Batalkan",
                                    tint = EditorialAlertRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Receipt Detail Modal
    if (uiState.selectedOrderForReceipt != null) {
        ReceiptDialog(
            order = uiState.selectedOrderForReceipt!!,
            items = uiState.selectedOrderItems,
            storeProfile = uiState.storeProfile,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // Cancel / Delete Order Confirmation Dialog
    if (orderToCancel != null) {
        Dialog(onDismissRequest = { orderToCancel = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                color = EditorialCardWhite
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Batalkan Transaksi?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Transaksi #${orderToCancel!!.invoiceNumber} akan dibatalkan dan jumlah stok barang akan dikembalikan ke inventaris.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EditorialMuted
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { orderToCancel = null },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.cancelTransaction(orderToCancel!!)
                                orderToCancel = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialAlertRed)
                        ) {
                            Text("Batalkan Transaksi")
                        }
                    }
                }
            }
        }
    }
}
