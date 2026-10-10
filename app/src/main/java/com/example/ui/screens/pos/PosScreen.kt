package com.example.ui.screens.pos

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.OnAlertRedContainer
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer

@Composable
fun PosScreen(
    viewModel: PosViewModel,
    onNavigateToInventory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showHeldCartsDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var showHoldCartDialog by remember { mutableStateOf(false) }
    var productForManualQty by remember { mutableStateOf<Pair<Product, Double>?>(null) }
    var isCartExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (uiState.cartItems.isNotEmpty()) 90.dp else 0.dp)
        ) {
            // Search & Barcode Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Cari barang atau ketik barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_search_input")
                )

                // Camera Barcode Scanner Button
                IconButton(
                    onClick = { viewModel.openCameraScanner() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .testTag("pos_barcode_scan_button")
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Kamera",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Park / Held Carts List Button
                if (uiState.heldCarts.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ) {
                                Text("${uiState.heldCarts.size}")
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showHeldCartsDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Antrean Tertahan",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == "Semua",
                        onClick = { viewModel.setCategory("Semua") },
                        label = { Text("Semua") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                items(uiState.categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category.name,
                        onClick = { viewModel.setCategory(category.name) },
                        label = { Text(category.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Product List Grid
            if (uiState.products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak ada produk ditemukan",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onNavigateToInventory) {
                            Text("Buka Manajemen Stok")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        val cartItem = uiState.cartItems.find { it.product.id == product.id }
                        PosProductItem(
                            product = product,
                            quantityInCart = cartItem?.quantity ?: 0.0,
                            onAddToCart = { viewModel.addToCart(product, 1.0) },
                            onIncrease = { viewModel.addToCart(product, 1.0) },
                            onDecrease = {
                                if (cartItem != null) {
                                    viewModel.updateCartQuantity(product.id, (cartItem.quantity - 1.0).coerceAtLeast(0.0))
                                }
                            },
                            onOpenManualQty = {
                                productForManualQty = product to (cartItem?.quantity ?: 1.0)
                            }
                        )
                    }
                }
            }
        }

        // Bottom Floating Cart Bar / Panel
        AnimatedVisibility(
            visible = uiState.cartItems.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("pos_bottom_cart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Expanded cart details
                    if (isCartExpanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Keranjang Belanja (${uiState.totalItemsCount} item)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                Text(
                                    text = "Kosongkan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlertRed,
                                    modifier = Modifier
                                        .clickable { viewModel.clearCart() }
                                        .padding(4.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { isCartExpanded = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ExpandMore, contentDescription = "Tutup Detail")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // List of items in cart
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(uiState.cartItems, key = { it.product.id }) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.product.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${CurrencyUtils.formatQty(item.quantity)} ${item.selectedUnit} x ${CurrencyUtils.formatRupiah(item.unitPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.updateCartQuantity(item.product.id, (item.quantity - 1.0).coerceAtLeast(0.0)) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                if (item.quantity <= 1.0) Icons.Default.DeleteOutline else Icons.Default.Remove,
                                                contentDescription = "Kurang",
                                                tint = if (item.quantity <= 1.0) AlertRed else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = CurrencyUtils.formatQty(item.quantity),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                                .clickable { productForManualQty = item.product to item.quantity }
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                        IconButton(
                                            onClick = { viewModel.updateCartQuantity(item.product.id, item.quantity + 1.0) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Tambah")
                                        }
                                    }
                                }
                            }
                        }

                        // Row action: Hold Cart & Discount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showHoldCartDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tahan Keranjang", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { showDiscountDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Discount, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (uiState.totalDiscount > 0) "-${CurrencyUtils.formatRupiah(uiState.totalDiscount)}" else "Diskon Belanja",
                                    fontSize = 12.sp
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Bottom Row Summary & Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { isCartExpanded = !isCartExpanded }
                                .padding(end = 8.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text("${uiState.totalItemsCount}")
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Keranjang",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiah(uiState.grandTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                if (isCartExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.testTag("pos_pay_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bayar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Manual Quantity Input Dialog
        productForManualQty?.let { (product, currentQty) ->
            ManualQtyInputDialog(
                product = product,
                currentQuantity = currentQty,
                onConfirm = { newQty ->
                    viewModel.updateCartQuantity(product.id, newQty)
                    productForManualQty = null
                },
                onRemove = {
                    viewModel.removeFromCart(product.id)
                    productForManualQty = null
                },
                onDismiss = { productForManualQty = null }
            )
        }

        // Live Camera Barcode Scanner Dialog
        if (uiState.isCameraScanning) {
            com.example.ui.components.CameraBarcodeScannerDialog(
                onBarcodeScanned = { barcode ->
                    viewModel.scanBarcode(barcode)
                },
                onDismiss = { viewModel.closeCameraScanner() }
            )
        }

        // Manual Barcode / Search Dialog
        if (showBarcodeScanner) {
            BarcodeScannerDialog(
                products = uiState.products,
                onProductSelected = { product ->
                    viewModel.addToCart(product)
                    Toast.makeText(context, "Ditambahkan: ${product.name}", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showBarcodeScanner = false }
            )
        }

        // Held Carts (Parked Carts) List Dialog
        if (showHeldCartsDialog) {
            HeldCartsDialog(
                heldCarts = uiState.heldCarts,
                onResume = { cart ->
                    viewModel.resumeHeldCart(cart)
                    showHeldCartsDialog = false
                },
                onDelete = { cart ->
                    viewModel.deleteHeldCart(cart)
                },
                onDismiss = { showHeldCartsDialog = false }
            )
        }

        // Hold Cart Note Dialog
        if (showHoldCartDialog) {
            HoldCartNoteDialog(
                onConfirm = { note ->
                    viewModel.holdCurrentCart(note)
                    showHoldCartDialog = false
                },
                onDismiss = { showHoldCartDialog = false }
            )
        }

        // Discount Input Dialog
        if (showDiscountDialog) {
            DiscountInputDialog(
                currentDiscount = uiState.totalDiscount,
                subtotal = uiState.subtotal,
                onApplyDiscount = { discount ->
                    viewModel.setTotalDiscount(discount)
                    showDiscountDialog = false
                },
                onDismiss = { showDiscountDialog = false }
            )
        }

        // Payment Processing Dialog
        if (showPaymentDialog) {
            PaymentDialog(
                grandTotal = uiState.grandTotal,
                customers = uiState.customers,
                selectedCustomer = uiState.selectedCustomer,
                storeProfile = uiState.storeProfile,
                onSelectCustomer = { viewModel.setSelectedCustomer(it) },
                onConfirmPayment = { method, paidAmount, notes ->
                    viewModel.processPayment(method, paidAmount, notes)
                    showPaymentDialog = false
                },
                onDismiss = { showPaymentDialog = false }
            )
        }

        // Receipt Completed Dialog
        uiState.lastCompletedOrder?.let { completedOrder ->
            ReceiptDialog(
                order = completedOrder,
                items = uiState.lastCompletedItems,
                storeProfile = uiState.storeProfile,
                onDismiss = { viewModel.dismissReceipt() },
                onNewTransaction = { viewModel.dismissReceipt() }
            )
        }
    }
}

@Composable
private fun PosProductItem(
    product: Product,
    quantityInCart: Double,
    onAddToCart: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onOpenManualQty: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = product.stock > 0) { onOpenManualQty() }
            .testTag("pos_product_item_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Product info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CurrencyUtils.formatRupiah(product.sellPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "/ ${product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stock Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isOutOfStock = product.stock <= 0
                    val isLowStock = product.stock <= product.minStockAlert && !isOutOfStock

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isOutOfStock -> AlertRedContainer
                                    isLowStock -> Color(0xFFFEF3C7)
                                    else -> PrimaryGreenContainer
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when {
                                isOutOfStock -> "Stok Habis"
                                isLowStock -> "Sisa ${CurrencyUtils.formatQty(product.stock)} ${product.unit} (Menipis)"
                                else -> "Stok: ${CurrencyUtils.formatQty(product.stock)} ${product.unit}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isOutOfStock -> OnAlertRedContainer
                                isLowStock -> Color(0xFF78350F)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    if (product.barcode.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = product.barcode,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action Counter or Add Button
            if (quantityInCart > 0.0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (quantityInCart <= 1.0) Icons.Default.DeleteOutline else Icons.Default.Remove,
                            contentDescription = "Kurang",
                            tint = if (quantityInCart <= 1.0) AlertRed else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = CurrencyUtils.formatQty(quantityInCart),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenManualQty() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(32.dp),
                        enabled = quantityInCart < product.stock
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onAddToCart,
                    enabled = product.stock > 0,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("add_to_cart_${product.id}")
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Beli")
                }
            }
        }
    }
}

@Composable
fun HeldCartsDialog(
    heldCarts: List<com.example.data.model.HeldCart>,
    onResume: (com.example.data.model.HeldCart) -> Unit,
    onDelete: (com.example.data.model.HeldCart) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Antrean Keranjang Tertahan (${heldCarts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (heldCarts.isEmpty()) {
                    Text("Tidak ada antrean tertahan saat ini.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(heldCarts, key = { it.id }) { cart ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(cart.note, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${cart.customerName} • ${cart.itemsCount} barang",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            CurrencyUtils.formatRupiah(cart.totalAmount),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = { onDelete(cart) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = AlertRed)
                                        }
                                        Button(
                                            onClick = { onResume(cart) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Panggil")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HoldCartNoteDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf("") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tahan Keranjang (Park Cart)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Beri nama/catatan antrean (contoh: 'Ibu baju merah', 'Bpk belanja gula'):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Contoh: Pembeli Tertunda #1") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(note) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan Antrean")
                    }
                }
            }
        }
    }
}

@Composable
fun DiscountInputDialog(
    currentDiscount: Double,
    subtotal: Double,
    onApplyDiscount: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var discountText by remember { mutableStateOf(if (currentDiscount > 0) currentDiscount.toLong().toString() else "") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Potongan Diskon Belanja",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Subtotal saat ini: ${CurrencyUtils.formatRupiah(subtotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Diskon (Rp)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val disc = discountText.toDoubleOrNull() ?: 0.0
                            onApplyDiscount(disc)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Terapkan Diskon")
                    }
                }
            }
        }
    }
}

@Composable
fun ManualQtyInputDialog(
    product: Product,
    currentQuantity: Double,
    onConfirm: (Double) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDecimalUnit = product.unit.lowercase().trim() in listOf(
        "kg", "kilogram", "liter", "ltr", "gram", "gr", "ons", "meter", "m", "cm", "mm", "m2", "m²", "m3", "m³", "kubik", "kwintal", "ton", "ml", "yard", "lembar"
    )
    var qtyText by remember { mutableStateOf(if (currentQuantity > 0.0) CurrencyUtils.formatQty(currentQuantity) else "1") }
    val enteredQty = qtyText.toDoubleOrNull() ?: 0.0
    val maxStock = product.stock

    // Calculate effective unit price with wholesale discount support
    val effectivePrice = if (product.wholesaleMinQty > 0 && product.wholesalePrice > 0 && enteredQty >= product.wholesaleMinQty) {
        product.wholesalePrice
    } else {
        product.sellPrice
    }
    val calculatedSubtotal = effectivePrice * enteredQty

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Input Jumlah Barang",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Product Information
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Harga: ${CurrencyUtils.formatRupiah(product.sellPrice)} / ${product.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tersedia: ${CurrencyUtils.formatQty(product.stock)} ${product.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (product.stock <= product.minStockAlert) AlertRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (product.wholesaleMinQty > 0 && product.wholesalePrice > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 Harga grosir min. ${CurrencyUtils.formatQty(product.wholesaleMinQty)} ${product.unit}: ${CurrencyUtils.formatRupiah(product.wholesalePrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quantity Input Field
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { input ->
                            val sanitized = input.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
                            val num = sanitized.toDoubleOrNull() ?: 0.0
                            if (num <= maxStock || maxStock <= 0.0) {
                                qtyText = sanitized
                            } else {
                                qtyText = CurrencyUtils.formatQty(maxStock)
                            }
                        },
                        label = { Text("Jumlah Pembelian (${product.unit})") },
                        placeholder = { Text(if (isDecimalUnit) "Contoh: 1.5 atau 0.5" else "Contoh: 100") },
                        trailingIcon = {
                            if (qtyText.isNotEmpty()) {
                                IconButton(onClick = { qtyText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                }
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Quantity Addition Buttons
                    Text(
                        text = "Tambah Cepat / Nominal Cepat:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val quickAmounts = if (isDecimalUnit) listOf(0.25, 0.5, 1.0, 5.0) else listOf(5.0, 10.0, 50.0, 100.0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickAmounts.forEach { addAmount ->
                            OutlinedButton(
                                onClick = {
                                    val current = qtyText.toDoubleOrNull() ?: 0.0
                                    val newTotal = (current + addAmount).coerceAtMost(maxStock)
                                    qtyText = CurrencyUtils.formatQty(newTotal)
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+${CurrencyUtils.formatQty(addAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedButton(
                            onClick = { qtyText = CurrencyUtils.formatQty(maxStock) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Text("Max", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estimated Subtotal Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Item Ini:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(calculatedSubtotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentQuantity > 0.0) {
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AlertRedContainer)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = AlertRed)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                val finalQty = qtyText.toDoubleOrNull() ?: 0.0
                                if (finalQty > 0.0) {
                                    onConfirm(finalQty)
                                } else {
                                    onRemove()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Terapkan Jumlah", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
