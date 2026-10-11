package com.example.ui.screens.inventory

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.QrCodeGenerator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.QuickAddStockDialog
import com.example.ui.components.StatCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberContainer
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueContainer
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.OnAlertRedContainer
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer
import com.example.ui.theme.EditorialAlertRed
import com.example.ui.theme.EditorialAlertRedContainer
import com.example.ui.theme.EditorialBlueContainer
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialMuted
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue
import com.example.ui.theme.EditorialSurfaceVariant
import com.example.ui.theme.EditorialTextDark

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productForStockAdjust by remember { mutableStateOf<Product?>(null) }
    var productForBarcodePrint by remember { mutableStateOf<Product?>(null) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToCsvUri(context, uri)
        }
    }

    val exportTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportTemplateUri(context, uri)
        }
    }

    var pendingImportProducts by remember { mutableStateOf<List<Product>?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importProductsFromUri(context, uri) { prods ->
                pendingImportProducts = prods
                showImportConfirmDialog = true
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
                onClick = { isAddingNewProduct = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Barang")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("inventory_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Inventory Value & Stat Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Nilai Aset Modal",
                        value = CurrencyUtils.formatRupiah(uiState.totalInventoryCostValue),
                        subtitle = "Modal tersimpan di toko",
                        icon = Icons.Default.Inventory2,
                        iconBgColor = AccentBlueContainer,
                        iconTint = AccentBlue,
                        modifier = Modifier.weight(1f),
                        testTag = "inventory_cost_stat"
                    )

                    StatCard(
                        title = "Nilai Jual Total",
                        value = CurrencyUtils.formatRupiah(uiState.totalInventoryRetailValue),
                        subtitle = "Estimasi omzet total",
                        icon = Icons.Default.TrendingUp,
                        iconBgColor = PrimaryGreenContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        testTag = "inventory_retail_stat"
                    )
                }
            }

            // Excel / Spreadsheet Integration Card (Redesigned Editorial Style)
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
                        // Title & Info
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
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Impor & Ekspor Data Stok (Excel)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EditorialNavy
                                    )
                                    Text(
                                        text = "Format .xlsx / .csv dengan header rapi otomatis",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EditorialMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Total count badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EditorialBlueContainer
                            ) {
                                Text(
                                    text = "${uiState.products.size} Produk",
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

                        // 3 Action Buttons Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Ekspor Button
                            OutlinedButton(
                                onClick = {
                                    val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                                    exportCsvLauncher.launch("Stok_Produk_${sdf.format(java.util.Date())}.csv")
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ekspor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // 2. Impor Button
                            Button(
                                onClick = { importCsvLauncher.launch("text/*") },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Impor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // 3. Unduh Template Button
                            OutlinedButton(
                                onClick = {
                                    exportTemplateLauncher.launch("Template_Impor_Produk_TokoPOS.csv")
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Template", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Search Bar & Barcode Scanner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cari nama, barcode, atau supplier...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inventory_search_input")
                    )

                    IconButton(
                        onClick = { showBarcodeScanner = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .testTag("inventory_scan_barcode_button")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Stock Status Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StockFilterOption.entries.forEach { option ->
                        item {
                            FilterChip(
                                selected = uiState.stockFilter == option,
                                onClick = { viewModel.setStockFilter(option) },
                                label = {
                                    Text(
                                        when (option) {
                                            StockFilterOption.ALL -> "Semua (${uiState.totalProductCount})"
                                            StockFilterOption.LOW_STOCK -> "Menipis (${uiState.lowStockCount})"
                                            StockFilterOption.OUT_OF_STOCK -> "Habis"
                                            StockFilterOption.SAFE -> "Aman"
                                        }
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (option == StockFilterOption.LOW_STOCK) AlertRedContainer else MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = if (option == StockFilterOption.LOW_STOCK) OnAlertRedContainer else MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Category Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == "Semua",
                            onClick = { viewModel.setCategory("Semua") },
                            label = { Text("Semua Kategori") }
                        )
                    }
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category.name,
                            onClick = { viewModel.setCategory(category.name) },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            // Product List Items
            if (uiState.products.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ProductionQuantityLimits,
                                    contentDescription = null,
                                    modifier = Modifier.size(54.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tidak ada barang ditemukan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Coba ubah kata kunci atau tambah produk baru",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(uiState.products, key = { it.id }) { product ->
                    InventoryProductCard(
                        product = product,
                        onEdit = { selectedProductForEdit = product },
                        onAdjustStock = { productForStockAdjust = product },
                        onPrintBarcode = { productForBarcodePrint = product }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Padding for FAB
            }
        }
    }

    // Print Barcode Label Dialog
    productForBarcodePrint?.let { product ->
        PrintBarcodeLabelDialog(
            product = product,
            onPrint = { copies ->
                viewModel.printBarcodeLabel(product, copies)
                productForBarcodePrint = null
            },
            onDismiss = { productForBarcodePrint = null }
        )
    }

    // Add / Edit Product Dialog
    if (isAddingNewProduct || selectedProductForEdit != null) {
        ProductDetailEditDialog(
            initialProduct = selectedProductForEdit,
            categories = uiState.categories,
            onSave = { product ->
                viewModel.saveProduct(product)
                isAddingNewProduct = false
                selectedProductForEdit = null
            },
            onDelete = { productId ->
                viewModel.deleteProduct(productId)
                selectedProductForEdit = null
            },
            onDismiss = {
                isAddingNewProduct = false
                selectedProductForEdit = null
            }
        )
    }

    // Adjust Stock Dialog
    productForStockAdjust?.let { product ->
        QuickAddStockDialog(
            product = product,
            onConfirm = { amount ->
                viewModel.adjustStock(product.id, amount)
                productForStockAdjust = null
            },
            onDismiss = { productForStockAdjust = null }
        )
    }

    // Barcode Search Dialog
    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            products = uiState.products,
            onProductSelected = { product ->
                selectedProductForEdit = product
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }

    // Import Products CSV Preview & Confirmation Dialog
    if (showImportConfirmDialog && pendingImportProducts != null) {
        val productsToImport = pendingImportProducts!!
        Dialog(onDismissRequest = { showImportConfirmDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "KONFIRMASI IMPOR PRODUK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ditemukan ${productsToImport.size} Produk",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Data dari file Excel / CSV akan disimpan ke dalam katalog stok produk Anda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preview of first 5 items
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Contoh Produk yang Terbaca:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            productsToImport.take(4).forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• ${p.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${CurrencyUtils.formatQty(p.stock)} ${p.unit} @ ${CurrencyUtils.formatRupiah(p.sellPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (productsToImport.size > 4) {
                                Text(
                                    text = "... dan ${productsToImport.size - 4} produk lainnya",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showImportConfirmDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.executeImport(productsToImport)
                                showImportConfirmDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Impor Sekarang (${productsToImport.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryProductCard(
    product: Product,
    onEdit: () -> Unit,
    onAdjustStock: () -> Unit,
    onPrintBarcode: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("inventory_item_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val codeInfo = buildList {
                        if (product.sku.isNotBlank()) add("SKU: ${product.sku}")
                        if (product.barcode.isNotBlank()) add("Barcode: ${product.barcode}")
                    }.joinToString(" • ")

                    Text(
                        text = "${product.category}${if (codeInfo.isNotBlank()) " • $codeInfo" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock Badge
                val isOutOfStock = product.stock <= 0
                val isLowStock = product.stock <= product.minStockAlert && !isOutOfStock

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isOutOfStock -> AlertRedContainer
                                isLowStock -> Color(0xFFFEF3C7)
                                else -> PrimaryGreenContainer
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${CurrencyUtils.formatQty(product.stock)} ${product.unit}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isOutOfStock -> AlertRed
                            isLowStock -> Color(0xFF78350F)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Pricing & Margins
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Harga Jual",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(product.sellPrice),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = "Harga Modal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(product.buyPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    val profit = product.sellPrice - product.buyPrice
                    Text(
                        text = "Laba/Unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(profit),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (profit >= 0) MaterialTheme.colorScheme.primary else AlertRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons: Barcode Print, Adjust Stock & Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPrintBarcode,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Barcode", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onAdjustStock,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("adjust_stock_button_${product.id}")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stok", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("edit_product_button_${product.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PrintBarcodeLabelDialog(
    product: Product,
    onPrint: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var copies by remember { mutableStateOf(1) }
    val barcodeText = product.barcode.ifBlank { product.sku.ifBlank { product.id.toString() } }
    val barcodeBitmap = remember(barcodeText) {
        QrCodeGenerator.generateBarcodeBitmap(barcodeText, 400, 140)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pinned Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cetak Label Barcode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable/Padded Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preview Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyUtils.formatRupiah(product.sellPrice),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (barcodeBitmap != null) {
                                Image(
                                    bitmap = barcodeBitmap.asImageBitmap(),
                                    contentDescription = "Barcode Preview",
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(70.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = barcodeText,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Copies Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jumlah Lembar Label:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (copies > 1) copies-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang")
                            }

                            Text(
                                text = "$copies",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = { if (copies < 50) copies++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Pinned Footer Action Bar
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onPrint(copies) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak $copies Label Barcode", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
