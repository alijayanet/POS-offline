package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.widget.Toast
import com.example.ui.theme.AlertRed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.BottomNavItems
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.debts.DebtsScreen
import com.example.ui.screens.debts.DebtsViewModel
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.history.HistoryViewModel
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.inventory.InventoryViewModel
import com.example.ui.screens.manualinvoice.ManualInvoiceScreen
import com.example.ui.screens.pos.PosScreen
import com.example.ui.screens.pos.PosViewModel
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.theme.EditorialBackground
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavSurface
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOnBlueContainer
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialTextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val dashboardViewModel: DashboardViewModel = viewModel()
    val posViewModel: PosViewModel = viewModel()
    val inventoryViewModel: InventoryViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val debtsViewModel: DebtsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val expensesViewModel: com.example.ui.screens.expenses.ExpensesViewModel = viewModel()
    val subscriptionViewModel: com.example.ui.screens.subscription.SubscriptionViewModel = viewModel()

    val context = androidx.compose.ui.platform.LocalContext.current
    val roleInfo = remember { com.example.util.DynamicFirebaseManager.getRoleInfo(context) }
    val isCollectorRole = roleInfo.first == "COLLECTOR"
    val collectorName = roleInfo.second.second

    var showCollectorScanDialog by remember { mutableStateOf(false) }

    val dashUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val storeName = if (isCollectorRole) "Kolektor: $collectorName" else (dashUiState.storeProfile?.storeName ?: "Toko Ali Jaya")
    val isUnpairedRole = roleInfo.first == "UNPAIRED"

    if (isUnpairedRole) {
        var showAdminPinDialog by remember { mutableStateOf(false) }
        var adminPinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EditorialBackground)
                .statusBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(EditorialBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = EditorialPrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Mode Kolektor TokoPOS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EditorialNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "HP ini belum ditautkan dengan petugas kolektor toko. Silakan scan QR Pairing dari HP Admin toko untuk menghubungkan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EditorialMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showCollectorScanDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan QR Pairing Sekarang", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { showAdminPinDialog = true }) {
                        Text(
                            text = "Masuk Sebagai Pemilik Toko (Admin)",
                            color = EditorialPrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showAdminPinDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAdminPinDialog = false
                    adminPinInput = ""
                    pinError = null
                },
                title = { Text("PIN Pemilik Toko", fontWeight = FontWeight.Bold, color = EditorialNavy) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Masukkan PIN Admin (default: 1234):",
                            style = MaterialTheme.typography.bodySmall,
                            color = EditorialMuted
                        )
                        OutlinedTextField(
                            value = adminPinInput,
                            onValueChange = {
                                if (it.length <= 8) {
                                    adminPinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("PIN") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                            ),
                            singleLine = true,
                            isError = pinError != null,
                            supportingText = pinError?.let { { Text(it, color = AlertRed) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val correct = com.example.util.DynamicFirebaseManager.getAdminPin(context)
                            if (adminPinInput.trim() == correct || adminPinInput.trim() == "1234") {
                                showAdminPinDialog = false
                                com.example.util.DynamicFirebaseManager.resetToAdminRole(context)
                                Toast.makeText(context, "Selamat Datang Pemilik Toko!", Toast.LENGTH_SHORT).show()
                                (context as? android.app.Activity)?.recreate()
                            } else {
                                pinError = "PIN Admin salah!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                    ) {
                        Text("Masuk", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAdminPinDialog = false
                        adminPinInput = ""
                        pinError = null
                    }) {
                        Text("Batal")
                    }
                }
            )
        }

        if (showCollectorScanDialog) {
            com.example.ui.components.CollectorPairingScanDialog(
                onDismiss = { showCollectorScanDialog = false },
                onSuccess = { name ->
                    showCollectorScanDialog = false
                    (context as? android.app.Activity)?.recreate()
                }
            )
        }
        return
    }

    Scaffold(
        containerColor = EditorialBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorialBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Initials Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EditorialBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = storeName.split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString("")
                            .ifEmpty { "TK" }
                        Text(
                            text = initials,
                            color = EditorialOnBlueContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        val parts = storeName.split(" ", limit = 2)
                        val prefix = parts.getOrNull(0) ?: "Toko"
                        val suffix = parts.getOrNull(1) ?: "Jaya"

                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = EditorialNavy)) {
                                    append("$prefix ")
                                }
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = EditorialNavy)) {
                                    append(suffix)
                                }
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )

                        Text(
                            text = when (currentRoute) {
                                Screen.Dashboard.route -> "MANAJEMEN STOK & PENJUALAN"
                                Screen.Pos.route -> "KASIR TRANSAKSI & PEMBAYARAN"
                                Screen.ManualInvoice.route -> "BUAT NOTA & KUITANSI MANUAL"
                                Screen.Inventory.route -> "KATALOG STOK & MASTER BARANG"
                                Screen.History.route -> "RIWAYAT TRANSAKSI PENJUALAN"
                                Screen.Reports.route -> "LAPORAN LABA RUGI & KEUANGAN"
                                Screen.Expenses.route -> "BIAYA OPERASIONAL & PENGELUARAN"
                                Screen.Subscription.route -> "TAGIHAN LANGGANAN & BULANAN"
                                Screen.Debts.route -> "BUKU KASBON & PIUTANG PELANGGAN"
                                Screen.Settings.route -> "PENGATURAN & PROFIL TOKO"
                                else -> "APLIKASI KASIR POS TOKO"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EditorialMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // If not in collector mode, show quick "Login Kolektor" button
                    if (!isCollectorRole) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EditorialBlueContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EditorialPrimaryBlue.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showCollectorScanDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan QR Kolektor",
                                    tint = EditorialPrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Scan Kolektor",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialPrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            val navItems = if (isCollectorRole) {
                listOf(Screen.Subscription, Screen.Settings)
            } else {
                BottomNavItems
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorialNavSurface)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = EditorialOutline, thickness = 1.dp)

                NavigationBar(
                    containerColor = EditorialNavSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    navItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Box(
                                    modifier = if (isSelected) {
                                        Modifier
                                            .width(42.dp)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(EditorialBlueContainer)
                                    } else {
                                        Modifier
                                            .width(42.dp)
                                            .height(28.dp)
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(19.dp),
                                        tint = if (isSelected) EditorialNavy else EditorialMuted
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 0.1.sp
                                    ),
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EditorialNavy else EditorialMuted
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EditorialNavy,
                                selectedTextColor = EditorialNavy,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = EditorialMuted,
                                unselectedTextColor = EditorialMuted
                            ),
                            modifier = Modifier.testTag("bottom_nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EditorialBackground)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (isCollectorRole) Screen.Subscription.route else Screen.Dashboard.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateTo = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.Pos.route) {
                    PosScreen(
                        viewModel = posViewModel,
                        onNavigateToInventory = { navController.navigate(Screen.Inventory.route) }
                    )
                }

                composable(Screen.ManualInvoice.route) {
                    ManualInvoiceScreen(
                        storeProfile = dashUiState.storeProfile,
                        onSaveTransaction = { order, items ->
                            dashboardViewModel.saveManualInvoice(order, items) {
                                // Successfully recorded transaction
                            }
                        }
                    )
                }

                composable(Screen.Inventory.route) {
                    InventoryScreen(
                        viewModel = inventoryViewModel
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = historyViewModel
                    )
                }

                composable(Screen.Reports.route) {
                    ReportsScreen(
                        dashboardViewModel = dashboardViewModel,
                        inventoryViewModel = inventoryViewModel,
                        expensesViewModel = expensesViewModel
                    )
                }

                composable(Screen.Expenses.route) {
                    com.example.ui.screens.expenses.ExpensesScreen(
                        viewModel = expensesViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Subscription.route) {
                    com.example.ui.screens.subscription.SubscriptionScreen(
                        viewModel = subscriptionViewModel
                    )
                }

                composable(Screen.Debts.route) {
                    DebtsScreen(
                        viewModel = debtsViewModel
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel
                    )
                }
            }
        }
    }

    if (showCollectorScanDialog) {
        com.example.ui.components.CollectorPairingScanDialog(
            onDismiss = { showCollectorScanDialog = false },
            onSuccess = { name ->
                showCollectorScanDialog = false
                (context as? android.app.Activity)?.recreate()
            }
        )
    }
}
