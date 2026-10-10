package com.example.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.SalesChart
import com.example.ui.components.StatCard
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.inventory.InventoryViewModel
import com.example.ui.theme.EditorialAlertRed
import com.example.ui.theme.EditorialAlertRedContainer
import com.example.ui.theme.EditorialAmber
import com.example.ui.theme.EditorialAmberContainer
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOnBlueContainer
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialPurple
import com.example.ui.theme.EditorialPurpleContainer
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.EditorialTextDark

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Dialog
import com.example.data.model.Expense
import com.example.ui.screens.expenses.AddExpenseDialog
import com.example.ui.screens.expenses.EXPENSE_CATEGORIES
import com.example.ui.screens.expenses.ExpensesViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EditorialSuccessGreen
import com.example.ui.theme.EditorialSuccessGreenContainer

@Composable
fun ReportsScreen(
    dashboardViewModel: DashboardViewModel,
    inventoryViewModel: InventoryViewModel,
    expensesViewModel: ExpensesViewModel
) {
    val dashState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val invState by inventoryViewModel.uiState.collectAsStateWithLifecycle()
    val expState by expensesViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Keuangan & Kas", "Biaya Pengeluaran", "Inventaris & Aset")

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val csv = com.example.util.CsvSpreadsheetUtils.exportSalesReportToCsv(
                orders = emptyList(),
                startDate = System.currentTimeMillis() - 86400000L * 7,
                endDate = System.currentTimeMillis(),
                totalExpenses = expState.totalExpenses
            )
            val res = com.example.util.CsvSpreadsheetUtils.writeCsvToUri(context, uri, csv)
            if (res.isSuccess) {
                Toast.makeText(context, "Laporan berhasil diekspor ke Excel/CSV!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val netProfitToday = dashState.todayProfit - expState.totalExpenses

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selector with Editorial Styling
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = EditorialCardWhite,
                contentColor = EditorialPrimaryBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, EditorialOutline, RoundedCornerShape(16.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 11.sp,
                                    color = if (selectedTab == index) EditorialPrimaryBlue else EditorialMuted
                                )
                            )
                        }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // --- TAB 1: LAPORAN KEUANGAN ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RINGKASAN LABA RUGI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EditorialMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Excel Export Button
                        Button(
                            onClick = {
                                val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                                exportCsvLauncher.launch("Laporan_Keuangan_TokoPOS_${sdf.format(java.util.Date())}.csv")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // WhatsApp Share Button
                        OutlinedButton(
                            onClick = {
                                shareFinancialReport(
                                    context = context,
                                    storeName = dashState.storeProfile?.storeName ?: "Toko Ali Jaya",
                                    todayRevenue = dashState.todayRevenue,
                                    todayGrossProfit = dashState.todayProfit,
                                    todayExpenses = expState.totalExpenses,
                                    todayNetProfit = netProfitToday,
                                    todayOrders = dashState.todayOrdersCount,
                                    totalDebt = dashState.totalCustomerDebt,
                                    assetCost = invState.totalInventoryCostValue,
                                    assetRetail = invState.totalInventoryRetailValue
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan WA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Stat Cards Row 1: Omset & Laba Kotor
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Omset Penjualan",
                        value = CurrencyUtils.formatRupiah(dashState.todayRevenue),
                        subtitle = "${dashState.todayOrdersCount} Transaksi hari ini",
                        icon = Icons.Default.AttachMoney,
                        iconBgColor = EditorialBlueContainer,
                        iconTint = EditorialPrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Laba Kotor (Gross)",
                        value = CurrencyUtils.formatRupiah(dashState.todayProfit),
                        subtitle = "Margin penjualan barang",
                        icon = Icons.Default.TrendingUp,
                        iconBgColor = EditorialAmberContainer,
                        iconTint = EditorialAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Stat Cards Row 2: Pengeluaran & Laba Bersih
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Biaya Pengeluaran",
                        value = CurrencyUtils.formatRupiah(expState.totalExpenses),
                        subtitle = "${expState.expenses.size} Catatan Biaya",
                        icon = Icons.Default.MoneyOff,
                        iconBgColor = AlertRedContainer,
                        iconTint = AlertRed,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Laba Bersih Toko",
                        value = CurrencyUtils.formatRupiah(netProfitToday),
                        subtitle = "Laba Kotor - Biaya",
                        icon = Icons.Default.Assessment,
                        iconBgColor = EditorialSuccessGreenContainer,
                        iconTint = EditorialSuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SalesChart(dailyData = dashState.weeklySalesChart)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "RINCIAN BUKU KAS TOKO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EditorialMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        ReportRow(
                            label = "Omset Penjualan Hari Ini",
                            value = CurrencyUtils.formatRupiah(dashState.todayRevenue),
                            isPositive = true
                        )
                        ReportRow(
                            label = "Estimasi Modal Terjual (HPP)",
                            value = CurrencyUtils.formatRupiah((dashState.todayRevenue - dashState.todayProfit).coerceAtLeast(0.0)),
                            isPositive = false
                        )
                        ReportRow(
                            label = "Laba Kotor Penjualan",
                            value = CurrencyUtils.formatRupiah(dashState.todayProfit),
                            isPositive = true,
                            isBold = true
                        )
                        ReportRow(
                            label = "Total Biaya Operasional Toko",
                            value = "- ${CurrencyUtils.formatRupiah(expState.totalExpenses)}",
                            isPositive = false,
                            isBold = true
                        )
                        HorizontalDivider(
                            color = EditorialOutline.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        ReportRow(
                            label = "Laba Bersih Toko (Net Profit)",
                            value = CurrencyUtils.formatRupiah(netProfitToday),
                            isPositive = netProfitToday >= 0,
                            isBold = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ReportRow(
                            label = "Total Piutang / Kasbon Belum Lunas",
                            value = CurrencyUtils.formatRupiah(dashState.totalCustomerDebt),
                            isPositive = false,
                            isBold = true
                        )
                    }
                }
            }
        } else if (selectedTab == 1) {
            // --- TAB 2: BIAYA PENGELUARAN (EXPENSES) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CATATAN BIAYA PENGELUARAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EditorialMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = "Total: ${CurrencyUtils.formatRupiah(expState.totalExpenses)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        )
                    }

                    Button(
                        onClick = { showAddExpenseDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Catat Biaya", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Category Filter Pills
            item {
                val allCats = listOf("Semua") + EXPENSE_CATEGORIES
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allCats) { cat ->
                        FilterChip(
                            selected = expState.selectedCategory == cat,
                            onClick = { expensesViewModel.setCategoryFilter(cat) },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AlertRed,
                                selectedLabelColor = EditorialCardWhite
                            )
                        )
                    }
                }
            }

            if (expState.expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = EditorialMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Belum ada catatan pengeluaran operasional.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EditorialMuted
                            )
                        }
                    }
                }
            } else {
                items(expState.expenses, key = { it.id }) { expense ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EditorialOutline.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AlertRedContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = expense.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AlertRed,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = CurrencyUtils.formatDate(expense.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EditorialMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                if (expense.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = expense.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = EditorialNavy
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "- ${CurrencyUtils.formatRupiah(expense.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { expenseToDelete = expense },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = EditorialMuted.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // --- TAB 2: LAPORAN STOK & ASET ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Nilai Modal Aset",
                        value = CurrencyUtils.formatRupiah(invState.totalInventoryCostValue),
                        subtitle = "Total modal tertahan di stok",
                        icon = Icons.Default.Inventory2,
                        iconBgColor = EditorialBlueContainer,
                        iconTint = EditorialPrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Potensi Nilai Jual",
                        value = CurrencyUtils.formatRupiah(invState.totalInventoryRetailValue),
                        subtitle = "${invState.totalProductCount} Jenis Produk",
                        icon = Icons.Default.BarChart,
                        iconBgColor = EditorialPurpleContainer,
                        iconTint = EditorialPurple,
                        modifier = Modifier.weight(1f)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "POTENSI KEUNTUNGAN INVENTARIS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EditorialMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val potentialProfit = invState.totalInventoryRetailValue - invState.totalInventoryCostValue
                        Text(
                            text = CurrencyUtils.formatRupiah(potentialProfit),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialPrimaryBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Selisih antara harga jual dan modal seluruh stok toko yang ada saat ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialMuted
                        )
                    }
                }
            }

            // Low Stock Warning List
            if (dashState.lowStockList.isNotEmpty()) {
                item {
                    Text(
                        text = "PERLU SEGERA RESTOCK (${dashState.lowStockCount})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EditorialAlertRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                dashState.lowStockList.forEach { prod ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, EditorialAlertRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (prod.stock <= 0.0) EditorialAlertRedContainer else EditorialAmberContainer.copy(alpha = 0.5f)
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
                                        text = prod.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EditorialTextDark
                                        )
                                    )
                                    Text(
                                        text = "Supplier: ${prod.supplier.ifBlank { "-" }}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = EditorialMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Text(
                                    text = if (prod.stock <= 0.0) "HABIS" else "Sisa ${CurrencyUtils.formatQty(prod.stock)} ${prod.unit}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (prod.stock <= 0.0) EditorialAlertRed else EditorialAmber
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { cat, amt, notes ->
                expensesViewModel.addExpense(cat, amt, notes)
                showAddExpenseDialog = false
            }
        )
    }

    // Delete Expense Confirmation Dialog
    if (expenseToDelete != null) {
        val exp = expenseToDelete!!
        Dialog(onDismissRequest = { expenseToDelete = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = EditorialCardWhite
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Hapus Catatan Pengeluaran?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hapus pengeluaran ${exp.category} sebesar ${CurrencyUtils.formatRupiah(exp.amount)}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EditorialMuted
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { expenseToDelete = null },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                expensesViewModel.deleteExpense(exp)
                                expenseToDelete = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                        ) {
                            Text("Hapus")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(
    label: String,
    value: String,
    isPositive: Boolean = true,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = EditorialTextDark
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isPositive) EditorialPrimaryBlue else EditorialTextDark
            )
        )
    }
}

private fun shareFinancialReport(
    context: Context,
    storeName: String,
    todayRevenue: Double,
    todayGrossProfit: Double,
    todayExpenses: Double,
    todayNetProfit: Double,
    todayOrders: Int,
    totalDebt: Double,
    assetCost: Double,
    assetRetail: Double
) {
    val sb = StringBuilder()
    sb.append("📊 *LAPORAN KEUANGAN TOKO - $storeName*\n")
    sb.append("Tanggal: ${CurrencyUtils.formatDateOnly(System.currentTimeMillis())}\n")
    sb.append("================================\n")
    sb.append("💰 *Aktivitas Hari Ini:*\n")
    sb.append("• Total Transaksi : $todayOrders\n")
    sb.append("• Total Omset     : ${CurrencyUtils.formatRupiah(todayRevenue)}\n")
    sb.append("• Laba Kotor      : ${CurrencyUtils.formatRupiah(todayGrossProfit)}\n")
    sb.append("• Biaya Operasional: - ${CurrencyUtils.formatRupiah(todayExpenses)}\n")
    sb.append("• *Laba Bersih Toko: ${CurrencyUtils.formatRupiah(todayNetProfit)}*\n\n")
    sb.append("📦 *Inventaris & Aset:*\n")
    sb.append("• Total Nilai Modal: ${CurrencyUtils.formatRupiah(assetCost)}\n")
    sb.append("• Total Nilai Jual : ${CurrencyUtils.formatRupiah(assetRetail)}\n\n")
    sb.append("📒 *Buku Kasbon / Piutang:*\n")
    sb.append("• Total Piutang Aktif: ${CurrencyUtils.formatRupiah(totalDebt)}\n")
    sb.append("================================\n")
    sb.append("Dibuat otomatis via Aplikasi Toko POS.")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Ekspor Laporan Keuangan"))
}
