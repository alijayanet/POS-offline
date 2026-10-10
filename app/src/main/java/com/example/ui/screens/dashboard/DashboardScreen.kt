package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethod
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.ManualInvoiceDialog
import com.example.ui.components.SalesChart
import com.example.ui.components.StatCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EditorialAlertRed
import com.example.ui.theme.EditorialAlertRedContainer
import com.example.ui.theme.EditorialAmber
import com.example.ui.theme.EditorialAmberContainer
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOnAlertRedContainer
import com.example.ui.theme.EditorialOnBlueContainer
import com.example.ui.theme.EditorialOnPurpleContainer
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialPurple
import com.example.ui.theme.EditorialPurpleContainer
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.EditorialTextDark

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateTo: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showManualInvoiceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Two Big Editorial Rounded Feature Cards (Total Stok & Omzet)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Card: Total Stok (bg-[#D1E4FF])
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clickable { onNavigateTo(Screen.Inventory.route) },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialBlueContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TOTAL STOK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EditorialOnBlueContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Column {
                            Text(
                                text = "${uiState.totalProductCount}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = EditorialOnBlueContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${uiState.lowStockCount} Perlu Restock",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EditorialOnBlueContainer,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Right Card: Omzet (bg-[#EADDFF])
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clickable { onNavigateTo(Screen.Reports.route) },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialPurpleContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "OMZET HARI INI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EditorialOnPurpleContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Column {
                            Text(
                                text = CurrencyUtils.formatRupiah(uiState.todayRevenue),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = EditorialOnPurpleContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${uiState.todayOrdersCount} Transaksi Selesai",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EditorialOnPurpleContainer,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Low Stock Alert Banner (if any)
        if (uiState.lowStockCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EditorialAlertRed.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .clickable { onNavigateTo(Screen.Inventory.route) }
                        .testTag("low_stock_banner"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialAlertRedContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EditorialAlertRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Peringatan",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Peringatan: ${uiState.lowStockCount} Barang Stok Menipis!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EditorialOnAlertRedContainer
                            )
                            Text(
                                text = "Segera cek inventaris dan pesan ulang ke supplier",
                                style = MaterialTheme.typography.bodySmall,
                                color = EditorialOnAlertRedContainer.copy(alpha = 0.85f)
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Lihat",
                            tint = EditorialOnAlertRedContainer
                        )
                    }
                }
            }
        }

        // Alert: Subscription Billing Due / Overdue
        if (uiState.overdueSubscriptionCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onNavigateTo(Screen.Subscription.route) }
                        .testTag("dashboard_subscription_alert"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialAmberContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EditorialAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = "Tagihan",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tagihan: ${uiState.overdueSubscriptionCount} Pelanggan Jatuh Tempo!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "Total tertunggak: ${CurrencyUtils.formatRupiah(uiState.overdueSubscriptionTotal)} • Klik untuk kirim WA",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF78350F).copy(alpha = 0.85f)
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Lihat",
                            tint = Color(0xFF78350F)
                        )
                    }
                }
            }
        }

        // Section: Aksi Cepat (Quick Actions Grid)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "AKSI CEPAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = EditorialMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Kasir (Highlighted Primary)
                    EditorialQuickActionButton(
                        label = "Kasir",
                        icon = Icons.Default.PointOfSale,
                        isPrimary = true,
                        onClick = { onNavigateTo(Screen.Pos.route) },
                        modifier = Modifier.weight(1f)
                    )

                    // 2. Nota Manual
                    EditorialQuickActionButton(
                        label = "Nota Manual",
                        icon = Icons.Default.ReceiptLong,
                        isPrimary = false,
                        onClick = { onNavigateTo(Screen.ManualInvoice.route) },
                        modifier = Modifier.weight(1f)
                    )

                    // 3. Tagihan Langganan
                    EditorialQuickActionButton(
                        label = "Tagihan",
                        icon = Icons.Default.Receipt,
                        isPrimary = false,
                        onClick = { onNavigateTo(Screen.Subscription.route) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 4. Tambah Barang
                    EditorialQuickActionButton(
                        label = "Tambah Stok",
                        icon = Icons.Default.Add,
                        isPrimary = false,
                        onClick = { onNavigateTo(Screen.Inventory.route) },
                        modifier = Modifier.weight(1f)
                    )

                    // 5. Scan Barcode / Cari
                    EditorialQuickActionButton(
                        label = "Scan Barcode",
                        icon = Icons.Default.QrCodeScanner,
                        isPrimary = false,
                        onClick = { onNavigateTo(Screen.Pos.route) },
                        modifier = Modifier.weight(1f)
                    )

                    // 6. Laporan
                    EditorialQuickActionButton(
                        label = "Laporan",
                        icon = Icons.Default.Assessment,
                        isPrimary = false,
                        onClick = { onNavigateTo(Screen.Reports.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Laba Bersih & Kasbon Secondary Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Laba Bersih",
                    value = CurrencyUtils.formatRupiah(uiState.todayProfit),
                    subtitle = "Margin profit hari ini",
                    icon = Icons.Default.TrendingUp,
                    iconBgColor = EditorialAmberContainer,
                    iconTint = EditorialAmber,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_card_profit"
                )

                StatCard(
                    title = "Kasbon Toko",
                    value = CurrencyUtils.formatRupiah(uiState.totalCustomerDebt),
                    subtitle = "Piutang pelanggan",
                    icon = Icons.Default.MenuBook,
                    iconBgColor = if (uiState.totalCustomerDebt > 0) EditorialAlertRedContainer else EditorialBlueContainer,
                    iconTint = if (uiState.totalCustomerDebt > 0) EditorialAlertRed else EditorialPrimaryBlue,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo(Screen.Debts.route) },
                    testTag = "stat_card_debt"
                )
            }
        }

        // 7-day Sales Chart with Editorial Styling
        item {
            SalesChart(dailyData = uiState.weeklySalesChart)
        }

        // Section: Aktivitas Terbaru (Recent Transactions)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AKTIVITAS TERBARU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = EditorialMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                )
                Text(
                    text = "Lihat Semua",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = EditorialPrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.clickable { onNavigateTo(Screen.History.route) }
                )
            }
        }

        if (uiState.recentTransactions.isEmpty()) {
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
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat aktivitas penjualan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EditorialMuted
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        uiState.recentTransactions.take(5).forEachIndexed { index, order ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateTo(Screen.History.route) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(EditorialSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = EditorialNavy,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Penjualan #${order.invoiceNumber}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = EditorialTextDark
                                            )
                                        )
                                        Text(
                                            text = "${order.customerName} • ${PaymentMethod.fromString(order.paymentMethod).displayName}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = EditorialMuted,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyUtils.formatRupiah(order.grandTotal),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EditorialPrimaryBlue
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

                            if (index < uiState.recentTransactions.take(5).size - 1) {
                                HorizontalDivider(
                                    color = EditorialOutline.copy(alpha = 0.6f),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Best Selling Products Section
        if (uiState.bestSellingProducts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRODUK TERLARIS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EditorialMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        )
                    )
                    Text(
                        text = "Katalog Stok",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EditorialPrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.clickable { onNavigateTo(Screen.Inventory.route) }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        uiState.bestSellingProducts.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(EditorialBlueContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = EditorialOnBlueContainer
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.productName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = EditorialTextDark
                                            )
                                        )
                                        Text(
                                            text = "Terjual: ${CurrencyUtils.formatQty(item.totalSold)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = EditorialPrimaryBlue,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = CurrencyUtils.formatRupiah(item.totalRevenue),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EditorialNavy
                                    )
                                )
                            }

                            if (index < uiState.bestSellingProducts.size - 1) {
                                HorizontalDivider(
                                    color = EditorialOutline.copy(alpha = 0.6f),
                                    thickness = 1.dp
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

    if (showManualInvoiceDialog) {
        ManualInvoiceDialog(
            storeProfile = uiState.storeProfile,
            onDismiss = { showManualInvoiceDialog = false },
            onSaveTransaction = { order, items ->
                viewModel.saveManualInvoice(order, items) {
                    // Successfully saved to transactions
                }
            }
        )
    }
}

@Composable
private fun EditorialQuickActionButton(
    label: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .then(
                    if (isPrimary) {
                        Modifier.background(EditorialPrimaryBlue)
                    } else {
                        Modifier
                            .background(EditorialCardWhite)
                            .border(1.dp, EditorialOutline, RoundedCornerShape(18.dp))
                    }
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) Color.White else EditorialNavy,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) EditorialPrimaryBlue else EditorialTextDark
            )
        )
    }
}
