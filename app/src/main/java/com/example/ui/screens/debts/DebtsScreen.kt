package com.example.ui.screens.debts

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableChart
import com.example.ui.components.CustomerIdCardDialog
import com.example.ui.components.CustomerCardData
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.DebtRecord
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.StatCard
import com.example.ui.theme.EditorialAlertRed
import com.example.ui.theme.EditorialAlertRedContainer
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOnAlertRedContainer
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.EditorialTextDark
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer

@Composable
fun DebtsScreen(
    viewModel: DebtsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Daftar Kasbon", "Data Pelanggan")

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForEdit by remember { mutableStateOf<Customer?>(null) }
    var selectedDebtForPayment by remember { mutableStateOf<DebtRecord?>(null) }
    var customerForIdCard by remember { mutableStateOf<CustomerCardData?>(null) }

    val exportCustomerCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToCsvUri(context, uri)
        }
    }

    val exportCustomerTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportTemplateUri(context, uri)
        }
    }

    var pendingImportCustomers by remember { mutableStateOf<List<Customer>?>(null) }
    var showImportCustomerConfirmDialog by remember { mutableStateOf(false) }

    val importCustomerCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importCustomersFromUri(context, uri) { custs ->
                pendingImportCustomers = custs
                showImportCustomerConfirmDialog = true
            }
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = EditorialPrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Pelanggan")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("debts_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Total Debt Stat Card
            item {
                StatCard(
                    title = "Total Saldo Piutang / Kasbon",
                    value = CurrencyUtils.formatRupiah(uiState.totalCustomerDebt),
                    subtitle = "${uiState.debtRecords.size} Catatan Kasbon Belum Lunas",
                    icon = Icons.Default.MenuBook,
                    iconBgColor = if (uiState.totalCustomerDebt > 0) EditorialAlertRedContainer else EditorialBlueContainer,
                    iconTint = if (uiState.totalCustomerDebt > 0) EditorialAlertRed else EditorialPrimaryBlue,
                    testTag = "total_debt_stat_card"
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Cari nama atau no. telepon...") },
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
                        .testTag("debts_search_input")
                )
            }

            // Tab Row
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
                                        letterSpacing = 1.sp,
                                        color = if (selectedTab == index) EditorialPrimaryBlue else EditorialMuted
                                    )
                                )
                            }
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // --- TAB 1: DAFTAR KASBON AKTIF ---
                if (uiState.debtRecords.isEmpty()) {
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
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = EditorialPrimaryBlue
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tidak ada piutang aktif!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EditorialNavy
                                    )
                                    Text(
                                        text = "Semua transaksi kasbon pelanggan telah lunas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EditorialMuted
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.debtRecords, key = { it.id }) { debt ->
                        DebtRecordCard(
                            debt = debt,
                            onPay = { selectedDebtForPayment = debt },
                            onSendReminder = {
                                sendDebtReminder(context, debt)
                            }
                        )
                    }
                }
            } else {
                // --- TAB 2: DATA PELANGGAN ---
                // Excel / Spreadsheet Integration Card for Customers
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EditorialOutline, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryGreenContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.TableChart,
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Impor & Ekspor Kontak Pelanggan",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EditorialNavy
                                        )
                                        Text(
                                            text = "Format Excel / CSV (Nama, No WA, Alamat, dll.)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EditorialMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EditorialBlueContainer
                                ) {
                                    Text(
                                        text = "${uiState.customers.size} Pelanggan",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EditorialPrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = EditorialOutline.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                                        exportCustomerCsvLauncher.launch("Pelanggan_Toko_${sdf.format(java.util.Date())}.csv")
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ekspor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { importCustomerCsvLauncher.launch("text/*") },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Impor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        exportCustomerTemplateLauncher.launch("Template_Impor_Pelanggan_TokoPOS.csv")
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = EditorialPrimaryBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Template", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                if (uiState.customers.isEmpty()) {
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
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = EditorialMuted
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Belum ada pelanggan terdaftar",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EditorialNavy
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showAddCustomerDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                                    ) {
                                        Text("+ Tambah Pelanggan Baru")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.customers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onShowIdCard = {
                                customerForIdCard = CustomerCardData(
                                    id = customer.id,
                                    customerCode = "CUST-${customer.id}",
                                    name = customer.name,
                                    phone = customer.phone,
                                    address = customer.address,
                                    isSubscription = false
                                )
                            },
                            onEdit = { selectedCustomerForEdit = customer }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        CustomerEditDialog(
            initialCustomer = null,
            onSave = { newCust ->
                viewModel.saveCustomer(newCust)
                showAddCustomerDialog = false
            },
            onDismiss = { showAddCustomerDialog = false }
        )
    }

    // Edit Customer Dialog
    if (selectedCustomerForEdit != null) {
        CustomerEditDialog(
            initialCustomer = selectedCustomerForEdit,
            onSave = { updatedCust ->
                viewModel.saveCustomer(updatedCust)
                selectedCustomerForEdit = null
            },
            onDelete = { custToDelete ->
                viewModel.deleteCustomer(custToDelete)
                selectedCustomerForEdit = null
            },
            onDismiss = { selectedCustomerForEdit = null }
        )
    }

    // Import Customer Confirm Dialog
    if (showImportCustomerConfirmDialog && pendingImportCustomers != null) {
        val count = pendingImportCustomers!!.size
        Dialog(onDismissRequest = { showImportCustomerConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = EditorialCardWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Konfirmasi Impor Pelanggan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ditemukan $count data pelanggan dari file Excel / CSV. Tambahkan data ini ke daftar kontak toko?",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preview Table
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EditorialSurfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Pratinjau (Contoh ${minOf(3, count)} data pertama):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EditorialNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            pendingImportCustomers!!.take(3).forEach { c ->
                                Text(
                                    text = "• ${c.name} (${if (c.phone.isNotBlank()) c.phone else "Tanpa No HP"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EditorialTextDark,
                                    fontSize = 11.sp
                                )
                            }
                            if (count > 3) {
                                Text(
                                    text = "... dan ${count - 3} pelanggan lainnya",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EditorialMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showImportCustomerConfirmDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                viewModel.executeImport(pendingImportCustomers!!)
                                showImportCustomerConfirmDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Impor", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Settle Debt Dialog
    selectedDebtForPayment?.let { debt ->
        SettleDebtDialog(
            debt = debt,
            onConfirmPayment = { amount ->
                viewModel.payDebt(debt, amount)
                selectedDebtForPayment = null
            },
            onDismiss = { selectedDebtForPayment = null }
        )
    }

    // Customer ID Card Dialog
    if (customerForIdCard != null) {
        CustomerIdCardDialog(
            customerData = customerForIdCard!!,
            storeProfile = uiState.storeProfile,
            onDismiss = { customerForIdCard = null }
        )
    }
}

@Composable
private fun DebtRecordCard(
    debt: DebtRecord,
    onPay: () -> Unit,
    onSendReminder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialOutline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = debt.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialTextDark
                        )
                    )
                    Text(
                        text = "Faktur: ${debt.invoiceNumber}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = EditorialMuted,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = CurrencyUtils.formatDate(debt.createdAt),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = EditorialMuted,
                            fontSize = 11.sp
                        )
                    )
                    if (debt.notes.isNotBlank()) {
                        Text(
                            text = "Catatan: ${debt.notes}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EditorialMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (debt.isSettled) EditorialBlueContainer else EditorialAlertRedContainer
                ) {
                    Text(
                        text = if (debt.isSettled) "LUNAS" else "BELUM LUNAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (debt.isSettled) EditorialPrimaryBlue else EditorialAlertRed
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = EditorialOutline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sisa Kasbon:",
                        style = MaterialTheme.typography.labelSmall.copy(color = EditorialMuted, fontSize = 10.sp)
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(debt.remainingDebt),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (debt.isSettled) EditorialTextDark else EditorialAlertRed
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!debt.isSettled) {
                        IconButton(
                            onClick = onSendReminder,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EditorialSurfaceVariant)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Kirim Pengingat",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Button(
                            onClick = onPay,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bayar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    onShowIdCard: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialOutline, RoundedCornerShape(20.dp))
            .clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EditorialSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = EditorialNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialTextDark
                        )
                    )
                    if (customer.phone.isNotBlank()) {
                        Text(
                            text = "Telp: ${customer.phone}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EditorialMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                    if (customer.address.isNotBlank()) {
                        Text(
                            text = "Alamat: ${customer.address}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EditorialMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    if (customer.totalDebt > 0) {
                        Text(
                            text = "Kasbon:",
                            style = MaterialTheme.typography.labelSmall.copy(color = EditorialMuted, fontSize = 10.sp)
                        )
                        Text(
                            text = CurrencyUtils.formatRupiah(customer.totalDebt),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialAlertRed
                            )
                        )
                    } else {
                        Text(
                            text = "Lunas",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialPrimaryBlue
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onShowIdCard,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EditorialSurfaceVariant)
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = "Kartu ID Barcode",
                        tint = EditorialPrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerEditDialog(
    initialCustomer: Customer?,
    onSave: (Customer) -> Unit,
    onDelete: ((Customer) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
            color = EditorialCardWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Text(
                    text = if (initialCustomer == null) "Tambah Pelanggan Baru" else "Edit Data Pelanggan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pelanggan *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. WhatsApp / HP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Pelanggan") },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (initialCustomer != null && onDelete != null) {
                        IconButton(onClick = {
                            onDelete(initialCustomer)
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = EditorialAlertRed)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val newCustomer = Customer(
                                        id = initialCustomer?.id ?: 0,
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        address = address.trim(),
                                        notes = notes.trim(),
                                        totalDebt = initialCustomer?.totalDebt ?: 0.0
                                    )
                                    onSave(newCustomer)
                                }
                            },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                        ) {
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettleDebtDialog(
    debt: DebtRecord,
    onConfirmPayment: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var payAmountText by remember { mutableStateOf(debt.remainingDebt.toLong().toString()) }

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
            ) {
                Text(
                    text = "Bayar / Cicil Kasbon",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pelanggan: ${debt.customerName}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = EditorialTextDark
                    )
                )
                Text(
                    text = "Sisa Piutang: ${CurrencyUtils.formatRupiah(debt.remainingDebt)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = EditorialAlertRed,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = payAmountText,
                    onValueChange = { payAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Pembayaran (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Full Settle Button
                OutlinedButton(
                    onClick = { payAmountText = debt.remainingDebt.toLong().toString() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bayar Lunas Sepenuhnya (${CurrencyUtils.formatRupiah(debt.remainingDebt)})", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                            val amount = payAmountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onConfirmPayment(amount)
                            }
                        },
                        enabled = (payAmountText.toDoubleOrNull() ?: 0.0) > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                    ) {
                        Text("Simpan Pembayaran")
                    }
                }
            }
        }
    }
}

private fun sendDebtReminder(context: Context, debt: DebtRecord) {
    val message = "Halo ${debt.customerName}, ini pengingat dari Toko Ali Jaya mengenai catatan kasbon sebesar ${CurrencyUtils.formatRupiah(debt.remainingDebt)} (Faktur: ${debt.invoiceNumber.ifBlank { "Kasbon" }}). Terima kasih banyak 🙏"
    val encodedMessage = Uri.encode(message)
    val url = "https://api.whatsapp.com/send?text=$encodedMessage"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Kirim Pengingat Kasbon"))
    }
}
