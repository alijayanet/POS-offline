package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Beranda", Icons.Default.Dashboard)
    data object Pos : Screen("pos", "Kasir", Icons.Default.PointOfSale)
    data object ManualInvoice : Screen("manual_invoice", "Nota", Icons.Default.ReceiptLong)
    data object Inventory : Screen("inventory", "Stok", Icons.Default.Inventory2)
    data object Subscription : Screen("subscription", "Tagihan", Icons.Default.Receipt)
    data object History : Screen("history", "Riwayat", Icons.AutoMirrored.Filled.ListAlt)
    data object Debts : Screen("debts", "Hutang", Icons.AutoMirrored.Filled.MenuBook)
    data object Reports : Screen("reports", "Laporan", Icons.Default.Assessment)
    data object Expenses : Screen("expenses", "Pengeluaran", Icons.Default.Assessment)
    data object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Pos,
    Screen.ManualInvoice,
    Screen.Inventory,
    Screen.Subscription,
    Screen.History,
    Screen.Settings
)
