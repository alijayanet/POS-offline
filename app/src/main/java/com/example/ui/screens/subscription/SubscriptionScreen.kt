package com.example.ui.screens.subscription

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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.model.SubscriptionPayment
import com.example.util.ThermalPrinterManager
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.components.CustomerIdCardDialog
import com.example.ui.components.CustomerCardData
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SubscriptionCustomer
import com.example.data.model.SubscriptionPackage
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.StatCard
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
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialPurple
import com.example.ui.theme.EditorialPurpleContainer
import com.example.ui.theme.EditorialSuccessGreen
import com.example.ui.theme.EditorialSuccessGreenContainer
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.EditorialTextDark
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer
import com.example.util.SubscriptionWhatsAppUtils
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.TextButton
import com.example.util.DynamicFirebaseManager

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val isCollectorMode = uiState.activeRole == "COLLECTOR"
    val tabTitles = if (isCollectorMode) {
        listOf("Tagihan & Tempo", "Riwayat Bayar")
    } else {
        listOf("Tagihan & Tempo", "Data Pelanggan", "Paket Langganan", "Riwayat Bayar", "Kolektor Lapangan")
    }

    var customerToEdit by remember { mutableStateOf<SubscriptionCustomer?>(null) }
    var isAddingCustomer by remember { mutableStateOf(false) }

    var packageToEdit by remember { mutableStateOf<SubscriptionPackage?>(null) }
    var isAddingPackage by remember { mutableStateOf(false) }

    var billingItemToPay by remember { mutableStateOf<CustomerBillingItem?>(null) }
    var customerToDelete by remember { mutableStateOf<SubscriptionCustomer?>(null) }
    var packageToDelete by remember { mutableStateOf<SubscriptionPackage?>(null) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var customerForIdCard by remember { mutableStateOf<CustomerCardData?>(null) }

    val exportSubCustomerCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToCsvUri(context, uri)
        }
    }

    val exportSubCustomerTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportTemplateUri(context, uri)
        }
    }

    var pendingImportSubCustomers by remember { mutableStateOf<List<SubscriptionCustomer>?>(null) }
    var showImportSubCustomerConfirmDialog by remember { mutableStateOf(false) }

    val importSubCustomerCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importCustomersFromUri(context, uri) { custs ->
                pendingImportSubCustomers = custs
                showImportSubCustomerConfirmDialog = true
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
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { isAddingCustomer = true },
                    containerColor = EditorialPrimaryBlue,
                    contentColor = EditorialCardWhite
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Pelanggan")
                }
            } else if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = { isAddingPackage = true },
                    containerColor = EditorialPrimaryBlue,
                    contentColor = EditorialCardWhite
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Paket")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("subscription_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Collector Mode Active Banner
            if (isCollectorMode) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EditorialPrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EditorialBlueContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = EditorialPrimaryBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "MODE KOLEKTOR PENAGIH",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EditorialPrimaryBlue,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Text(
                                        text = uiState.activeCollectorName.ifEmpty { "Petugas Lapangan" },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EditorialNavy
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    DynamicFirebaseManager.resetToAdminRole(context)
                                    Toast.makeText(context, "Beralih kembali ke Mode Admin", Toast.LENGTH_SHORT).show()
                                    // Activity reload or recreate
                                    (context as? android.app.Activity)?.recreate()
                                }
                            ) {
                                Text("Keluar", color = AlertRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Header Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Tagihan Jatuh Tempo",
                        value = "${uiState.totalOverdueCustomers} Pelanggan",
                        subtitle = CurrencyUtils.formatRupiah(uiState.totalOverdueAmount),
                        icon = Icons.Default.MoneyOff,
                        iconBgColor = if (uiState.totalOverdueCustomers > 0) AlertRedContainer else EditorialBlueContainer,
                        iconTint = if (uiState.totalOverdueCustomers > 0) AlertRed else EditorialPrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Potensi Bulanan",
                        value = CurrencyUtils.formatRupiah(uiState.totalMonthlyRevenuePotential),
                        subtitle = "${uiState.totalActiveCustomers} Pelanggan Aktif",
                        icon = Icons.Default.TrendingUp,
                        iconBgColor = EditorialSuccessGreenContainer,
                        iconTint = EditorialSuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab Selector
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
                                        letterSpacing = 0.6.sp,
                                        fontSize = 11.sp,
                                        color = if (selectedTab == index) EditorialPrimaryBlue else EditorialMuted
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Tab 0: Tagihan & Jatuh Tempo
            if (selectedTab == 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Cari nama, ID, atau scan barcode...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Close, contentDescription = "Hapus")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )

                            // Scan Barcode Button
                            Surface(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { showBarcodeScanner = true },
                                color = EditorialPrimaryBlue,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = "Scan Barcode Pelanggan",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(BillingFilter.entries) { filter ->
                                FilterChip(
                                    selected = uiState.selectedFilter == filter,
                                    onClick = { viewModel.setFilter(filter) },
                                    label = { Text(filter.displayName, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EditorialPrimaryBlue,
                                        selectedLabelColor = EditorialCardWhite
                                    )
                                )
                            }
                        }
                    }
                }

                if (uiState.billingItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = EditorialMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (uiState.customers.isEmpty()) "Belum ada pelanggan langganan. Tambah pelanggan di Tab 'Data Pelanggan'." else "Tidak ada tagihan yang sesuai filter.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EditorialMuted
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.billingItems, key = { it.customer.id }) { item ->
                        CustomerBillingCard(
                            item = item,
                            onShowIdCard = {
                                customerForIdCard = CustomerCardData(
                                    id = item.customer.id,
                                    customerCode = item.customer.customerCode,
                                    name = item.customer.name,
                                    phone = item.customer.phone,
                                    address = item.customer.address,
                                    packageName = item.customer.packageName,
                                    monthlyPrice = item.customer.monthlyPrice,
                                    dueDay = item.customer.dueDay,
                                    isSubscription = true,
                                    status = item.customer.status
                                )
                            },
                            onSendWhatsApp = {
                                val message = SubscriptionWhatsAppUtils.generateBillingMessage(
                                    customer = item.customer,
                                    monthsDue = item.monthsDue.coerceAtLeast(1),
                                    totalAmount = if (item.totalAmountDue > 0) item.totalAmountDue else item.customer.monthlyPrice,
                                    dueDateTimestamp = item.nextDueDateTimestamp,
                                    storeProfile = uiState.storeProfile
                                )
                                SubscriptionWhatsAppUtils.sendWhatsAppMessage(
                                    context = context,
                                    phone = item.customer.phone,
                                    message = message
                                )
                            },
                            onPay = {
                                billingItemToPay = item
                            }
                        )
                    }
                }
            }

            // Tab 1: Data Pelanggan Langganan
            else if (selectedTab == 1) {
                // Excel / Spreadsheet Integration Card for Subscribers
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
                                            text = "Impor & Ekspor Data Pelanggan",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EditorialNavy
                                        )
                                        Text(
                                            text = "Format Excel / CSV (ID, Nama, Paket, Jatuh Tempo)",
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
                                        exportSubCustomerCsvLauncher.launch("Pelanggan_Langganan_${sdf.format(java.util.Date())}.csv")
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
                                    onClick = { importSubCustomerCsvLauncher.launch("text/*") },
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
                                        exportSubCustomerTemplateLauncher.launch("Template_Impor_Pelanggan_Langganan.csv")
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    tint = EditorialMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada data pelanggan langganan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EditorialMuted
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { isAddingCustomer = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Pelanggan")
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.customers, key = { it.id }) { customer ->
                        CustomerDataCard(
                            customer = customer,
                            onShowIdCard = {
                                customerForIdCard = CustomerCardData(
                                    id = customer.id,
                                    customerCode = customer.customerCode,
                                    name = customer.name,
                                    phone = customer.phone,
                                    address = customer.address,
                                    packageName = customer.packageName,
                                    monthlyPrice = customer.monthlyPrice,
                                    dueDay = customer.dueDay,
                                    isSubscription = true,
                                    status = customer.status
                                )
                            },
                            onEdit = { customerToEdit = customer },
                            onDelete = { customerToDelete = customer }
                        )
                    }
                }
            }

            // Tab 2: Paket Langganan
            else if (selectedTab == 2) {
                if (uiState.packages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = EditorialMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada paket langganan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EditorialMuted
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { isAddingPackage = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Paket Baru")
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.packages, key = { it.id }) { pkg ->
                        PackageCard(
                            pkg = pkg,
                            onEdit = { packageToEdit = pkg },
                            onDelete = { packageToDelete = pkg }
                        )
                    }
                }
            }

            // Tab 3: Riwayat Pembayaran Langganan
            else if ((isCollectorMode && selectedTab == 1) || (!isCollectorMode && selectedTab == 3)) {
                if (uiState.payments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = EditorialMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada riwayat pembayaran tagihan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EditorialMuted
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.payments, key = { it.id }) { payment ->
                        SubscriptionPaymentCard(
                            payment = payment,
                            onPrintReceipt = {
                                val profile = uiState.storeProfile
                                val printerMac = profile?.bluetoothPrinterMac ?: ""
                                val paperSize = profile?.paperSize ?: "58mm"

                                if (printerMac.isNotBlank()) {
                                    scope.launch {
                                        val result = ThermalPrinterManager.printSubscriptionReceipt(
                                            context = context,
                                            deviceAddress = printerMac,
                                            payment = payment,
                                            paidUntilTimestamp = payment.periodEndTimestamp,
                                            storeProfile = profile,
                                            paperSize = paperSize
                                        )
                                        if (result.isFailure) {
                                            Toast.makeText(context, "Gagal cetak: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    val paired = ThermalPrinterManager.getPairedPrinters(context)
                                    if (paired.isNotEmpty()) {
                                        scope.launch {
                                            val result = ThermalPrinterManager.printSubscriptionReceipt(
                                                context = context,
                                                deviceAddress = paired.first().address,
                                                payment = payment,
                                                paidUntilTimestamp = payment.periodEndTimestamp,
                                                storeProfile = profile,
                                                paperSize = paperSize
                                            )
                                            if (result.isFailure) {
                                                Toast.makeText(context, "Gagal cetak: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Printer Bluetooth belum dipilih di Pengaturan", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onSendWhatsApp = {
                                val receiptText = SubscriptionWhatsAppUtils.generatePaymentReceiptMessage(
                                    payment = payment,
                                    paidUntilTimestamp = payment.periodEndTimestamp,
                                    storeProfile = uiState.storeProfile
                                )
                                val custPhone = uiState.customers.find { it.id == payment.customerId }?.phone
                                SubscriptionWhatsAppUtils.sendWhatsAppMessage(
                                    context = context,
                                    phone = custPhone,
                                    message = receiptText
                                )
                            }
                        )
                    }
                }
            }

            // Tab 4: Kelola Kolektor Lapangan (Khusus Admin)
            else if (!isCollectorMode && selectedTab == 4) {
                item {
                    CollectorManagementSection(
                        collectors = uiState.collectors,
                        onAddCollector = { newCol -> viewModel.saveCollector(newCol) },
                        onEditCollector = { updatedCol -> viewModel.saveCollector(updatedCol) },
                        onDeleteCollector = { col -> viewModel.deleteCollector(col) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (isAddingCustomer || customerToEdit != null) {
        AddEditSubscriptionCustomerDialog(
            customerToEdit = customerToEdit,
            packages = uiState.packages,
            onDismiss = {
                isAddingCustomer = false
                customerToEdit = null
            },
            onSave = { cust ->
                viewModel.saveCustomer(cust)
                isAddingCustomer = false
                customerToEdit = null
            }
        )
    }

    if (isAddingPackage || packageToEdit != null) {
        AddEditPackageDialog(
            packageToEdit = packageToEdit,
            onDismiss = {
                isAddingPackage = false
                packageToEdit = null
            },
            onSave = { pkg ->
                viewModel.savePackage(pkg)
                isAddingPackage = false
                packageToEdit = null
            }
        )
    }

    if (billingItemToPay != null) {
        val item = billingItemToPay!!
        PaySubscriptionDialog(
            billingItem = item,
            onDismiss = { billingItemToPay = null },
            onConfirmPayment = { months, method, notes, sendWA, printThermal ->
                viewModel.processPayment(
                    customer = item.customer,
                    monthsCount = months,
                    paymentMethod = method,
                    notes = notes,
                    onSuccess = { payment, newPaidUntil ->
                        if (sendWA && item.customer.phone.isNotBlank()) {
                            val receiptText = SubscriptionWhatsAppUtils.generatePaymentReceiptMessage(
                                payment = payment,
                                paidUntilTimestamp = newPaidUntil,
                                storeProfile = uiState.storeProfile
                            )
                            SubscriptionWhatsAppUtils.sendWhatsAppMessage(
                                context = context,
                                phone = item.customer.phone,
                                message = receiptText
                            )
                        }

                        if (printThermal) {
                            val profile = uiState.storeProfile
                            val printerMac = profile?.bluetoothPrinterMac ?: ""
                            val paperSize = profile?.paperSize ?: "58mm"

                            if (printerMac.isNotBlank()) {
                                scope.launch {
                                    val result = ThermalPrinterManager.printSubscriptionReceipt(
                                        context = context,
                                        deviceAddress = printerMac,
                                        payment = payment,
                                        paidUntilTimestamp = newPaidUntil,
                                        storeProfile = profile,
                                        paperSize = paperSize
                                    )
                                    if (result.isFailure) {
                                        Toast.makeText(context, "Gagal cetak: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Struk kuitansi berhasil dicetak!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                val paired = ThermalPrinterManager.getPairedPrinters(context)
                                if (paired.isNotEmpty()) {
                                    scope.launch {
                                        val result = ThermalPrinterManager.printSubscriptionReceipt(
                                            context = context,
                                            deviceAddress = paired.first().address,
                                            payment = payment,
                                            paidUntilTimestamp = newPaidUntil,
                                            storeProfile = profile,
                                            paperSize = paperSize
                                        )
                                        if (result.isFailure) {
                                            Toast.makeText(context, "Gagal cetak: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Struk kuitansi berhasil dicetak!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Printer Bluetooth belum dipilih di menu Pengaturan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
                billingItemToPay = null
            }
        )
    }

    // Import Subscription Customer Confirm Dialog
    if (showImportSubCustomerConfirmDialog && pendingImportSubCustomers != null) {
        val count = pendingImportSubCustomers!!.size
        Dialog(onDismissRequest = { showImportSubCustomerConfirmDialog = false }) {
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
                        text = "Ditemukan $count data pelanggan langganan dari file Excel / CSV. Tambahkan ke database sistem?",
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
                            pendingImportSubCustomers!!.take(3).forEach { c ->
                                Text(
                                    text = "• [${c.customerCode}] ${c.name} - ${c.packageName} (Tempo tgl ${c.dueDay})",
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
                            onClick = { showImportSubCustomerConfirmDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                viewModel.executeImport(pendingImportSubCustomers!!)
                                showImportSubCustomerConfirmDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Impor Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Delete Customer Confirmation Dialog
    if (customerToDelete != null) {
        val c = customerToDelete!!
        Dialog(onDismissRequest = { customerToDelete = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = EditorialCardWhite
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Hapus Pelanggan?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hapus data langganan untuk ${c.name} (${c.customerCode})?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EditorialMuted
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { customerToDelete = null },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.deleteCustomer(c)
                                customerToDelete = null
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

    // Delete Package Confirmation Dialog
    if (packageToDelete != null) {
        val p = packageToDelete!!
        Dialog(onDismissRequest = { packageToDelete = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = EditorialCardWhite
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Hapus Paket Langganan?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hapus paket ${p.name} (${CurrencyUtils.formatRupiah(p.price)})?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EditorialMuted
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { packageToDelete = null },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.deletePackage(p)
                                packageToDelete = null
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

    // Barcode Scanner Dialog for Subscription
    if (showBarcodeScanner) {
        CameraBarcodeScannerDialog(
            onBarcodeScanned = { scannedCode ->
                showBarcodeScanner = false
                viewModel.setSearchQuery(scannedCode)
                val matchedItem = uiState.billingItems.find {
                    it.customer.customerCode.equals(scannedCode, ignoreCase = true) ||
                    it.customer.phone.equals(scannedCode, ignoreCase = true) ||
                    it.customer.name.contains(scannedCode, ignoreCase = true)
                }
                if (matchedItem != null) {
                    billingItemToPay = matchedItem
                }
            },
            onDismiss = { showBarcodeScanner = false }
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
private fun CustomerBillingCard(
    item: CustomerBillingItem,
    onShowIdCard: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onPay: () -> Unit
) {
    val cust = item.customer
    val isOverdue = item.status == BillingStatus.OVERDUE
    val isDueToday = item.status == BillingStatus.DUE_TODAY
    val isPaid = item.status == BillingStatus.PAID

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when {
                    isOverdue -> AlertRed.copy(alpha = 0.5f)
                    isDueToday -> EditorialAmber.copy(alpha = 0.5f)
                    else -> EditorialOutline
                },
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverdue -> AlertRedContainer.copy(alpha = 0.25f)
                isDueToday -> EditorialAmberContainer.copy(alpha = 0.25f)
                else -> EditorialCardWhite
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Customer Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cust.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Text(
                        text = "${if (cust.customerCode.isNotBlank()) "[${cust.customerCode}] " else ""}${cust.packageName} • ${CurrencyUtils.formatRupiah(cust.monthlyPrice)}/bln",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialPrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isPaid -> EditorialSuccessGreenContainer
                        isOverdue -> AlertRedContainer
                        else -> EditorialAmberContainer
                    }
                ) {
                    Text(
                        text = when {
                            isPaid -> "Lunas"
                            isOverdue -> "Tunggakan (${item.monthsDue} Bulan)"
                            else -> "Tempo Hari Ini"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isPaid -> EditorialSuccessGreen
                            isOverdue -> AlertRed
                            else -> EditorialAmber
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Due Date & Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Jatuh Tempo Tiap Tgl ${cust.dueDay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        fontSize = 11.sp
                    )
                    if (cust.phone.isNotBlank()) {
                        Text(
                            text = "WA: ${cust.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isPaid) "Lunas Sampai:" else "Total Tagihan:",
                        style = MaterialTheme.typography.labelSmall,
                        color = EditorialMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (isPaid) CurrencyUtils.formatDateOnly(cust.paidUntilTimestamp) else CurrencyUtils.formatRupiah(item.totalAmountDue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) EditorialSuccessGreen else AlertRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = EditorialOutline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onShowIdCard,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(15.dp), tint = EditorialPrimaryBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kartu ID", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EditorialNavy)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onSendWhatsApp,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp), tint = EditorialSuccessGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kirim WA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EditorialNavy)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onPay,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaid) EditorialSuccessGreen else EditorialPrimaryBlue
                    )
                ) {
                    Icon(
                        if (isPaid) Icons.Default.Add else Icons.Default.PointOfSale,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPaid) "Perpanjang" else if (item.monthsDue > 1) "Bayar (${item.monthsDue} Bln)" else "Bayar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerDataCard(
    customer: SubscriptionCustomer,
    onShowIdCard: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialOutline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (customer.status == "ACTIVE") EditorialSuccessGreenContainer else AlertRedContainer
                    ) {
                        Text(
                            text = if (customer.status == "ACTIVE") "Aktif" else customer.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (customer.status == "ACTIVE") EditorialSuccessGreen else AlertRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (customer.customerCode.isNotBlank()) "ID: ${customer.customerCode} • " else ""}${customer.packageName} (${CurrencyUtils.formatRupiah(customer.monthlyPrice)}/bln)",
                    style = MaterialTheme.typography.bodySmall,
                    color = EditorialPrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )

                if (customer.address.isNotBlank()) {
                    Text(
                        text = "📍 ${customer.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "🗓️ Jatuh tempo tiap tgl ${customer.dueDay} • Pasang: ${CurrencyUtils.formatDateOnly(customer.installationDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EditorialMuted,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShowIdCard) {
                    Icon(Icons.Default.QrCode, contentDescription = "Kartu ID", tint = EditorialPrimaryBlue)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EditorialNavy)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AlertRed)
                }
            }
        }
    }
}

@Composable
private fun PackageCard(
    pkg: SubscriptionPackage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialOutline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialCardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EditorialNavy
                )
                Text(
                    text = "${CurrencyUtils.formatRupiah(pkg.price)} / bulan",
                    style = MaterialTheme.typography.titleSmall,
                    color = EditorialPrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
                if (pkg.speedInfo.isNotBlank()) {
                    Text(
                        text = "⚡ ${pkg.speedInfo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted
                    )
                }
                if (pkg.description.isNotBlank()) {
                    Text(
                        text = pkg.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EditorialPrimaryBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AlertRed)
                }
            }
        }
    }
}

@Composable
fun SubscriptionPaymentCard(
    payment: SubscriptionPayment,
    onPrintReceipt: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialOutline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
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
                Column {
                    Text(
                        text = payment.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )
                    Text(
                        text = "Faktur: ${payment.invoiceNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EditorialSuccessGreenContainer
                ) {
                    Text(
                        text = "LUNAS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EditorialSuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = EditorialOutline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Paket ${payment.packageName} (${payment.monthsCount} Bulan)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = EditorialNavy
                    )
                    Text(
                        text = "Bayar: ${CurrencyUtils.formatDate(payment.timestamp)} • ${payment.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = CurrencyUtils.formatRupiah(payment.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EditorialSuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSendWhatsApp,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EditorialSurfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Kirim WA",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onPrintReceipt,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak Struk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}