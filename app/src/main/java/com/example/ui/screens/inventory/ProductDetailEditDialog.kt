package com.example.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.AlertRed
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailEditDialog(
    initialProduct: Product?,
    categories: List<Category>,
    onSave: (Product) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: if (categories.isNotEmpty()) categories[0].name else "Sembako") }
    var buyPriceText by remember { mutableStateOf(if (initialProduct != null) initialProduct.buyPrice.toLong().toString() else "0") }
    var sellPriceText by remember { mutableStateOf(if (initialProduct != null) initialProduct.sellPrice.toLong().toString() else "0") }
    var stockText by remember { mutableStateOf(if (initialProduct != null) CurrencyUtils.formatQty(initialProduct.stock) else "10") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "Pcs") }
    var minStockText by remember { mutableStateOf(if (initialProduct != null) CurrencyUtils.formatQty(initialProduct.minStockAlert) else "5") }
    var wholesalePriceText by remember { mutableStateOf(if (initialProduct?.wholesalePrice != null && initialProduct.wholesalePrice > 0) initialProduct.wholesalePrice.toLong().toString() else "") }
    var wholesaleMinQtyText by remember { mutableStateOf(if (initialProduct?.wholesaleMinQty != null && initialProduct.wholesaleMinQty > 0) CurrencyUtils.formatQty(initialProduct.wholesaleMinQty) else "") }
    var supplier by remember { mutableStateOf(initialProduct?.supplier ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val buyPriceNum = buyPriceText.toDoubleOrNull() ?: 0.0
    val sellPriceNum = sellPriceText.toDoubleOrNull() ?: 0.0
    val marginProfit = (sellPriceNum - buyPriceNum)
    val marginPercent = if (buyPriceNum > 0) ((marginProfit / buyPriceNum) * 100).toInt() else 0

    val unitOptions = listOf(
        // Retail & Toko Kelontong
        "Pcs", "Bungkus", "Botol", "Dus", "Karung", "Pack", "Renteng", "Sachet", "Kotak", "Box", "Lusin", "Kodi", "Rim", "Ikat", "Sisir",
        // Kuliner, Warung Makan & Minuman
        "Porsi", "Cup", "Gelas", "Piring", "Mangkok", "Paket", "Potong", "Biji", "Butir", "Tusuk", "Loyang", "Kaleng",
        // Berat & Timbangan (Desimal)
        "Kg", "Gram", "Ons", "Kwintal", "Ton",
        // Volume & Cairan (Desimal)
        "Liter", "Ml", "Galon", "Jerigen",
        // Panjang, Luas & Material Bangunan / Tekstil / Percetakan (Desimal)
        "Meter", "Cm", "Roll", "Lembar", "Batang", "Sak", "M²",
        // Jasa, Fashion & Rental
        "Set", "Pasang", "Unit", "Jam", "Hari", "Sesi"
    )

    val filteredUnitOptions = if (unit.isBlank()) unitOptions else {
        val matches = unitOptions.filter { it.contains(unit, ignoreCase = true) }
        if (matches.isEmpty()) unitOptions else matches
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("product_edit_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Pinned Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialProduct == null) "Tambah Barang Baru" else "Edit Detail Barang",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // 2. Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Product Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Barang *") },
                        placeholder = { Text("Contoh: Minyak Goreng Bimoli 2L") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barcode / SKU with Auto-generate helper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode / Kode SKU") },
                            placeholder = { Text("899...") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_barcode_input")
                        )

                        OutlinedButton(
                            onClick = {
                                val randomCode = "899" + (100000000L + (Math.random() * 900000000L).toLong())
                                barcode = randomCode
                            },
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category & Unit Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kategori") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategory = cat.name
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Unit Dropdown
                        ExposedDropdownMenuBox(
                            expanded = unitDropdownExpanded,
                            onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {
                                    unit = it
                                    unitDropdownExpanded = true
                                },
                                label = { Text("Satuan *") },
                                placeholder = { Text("Pcs / Porsi / Meter...") },
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false }
                            ) {
                                filteredUnitOptions.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unit = u
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buy Price & Sell Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = buyPriceText,
                            onValueChange = { buyPriceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Harga Beli (Modal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_buy_price_input")
                        )

                        OutlinedTextField(
                            value = sellPriceText,
                            onValueChange = { sellPriceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Harga Jual *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_sell_price_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stock & Min Stock Alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }.replace(',', '.') },
                            label = { Text("Jumlah Stok") },
                            placeholder = { Text("Contoh: 1.5 atau 10") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_stock_input")
                        )

                        OutlinedTextField(
                            value = minStockText,
                            onValueChange = { minStockText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }.replace(',', '.') },
                            label = { Text("Batas Peringatan") },
                            placeholder = { Text("Contoh: 2 atau 0.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_min_stock_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wholesale Price (Grosir) Section
                    Text(
                        text = "Harga Grosir / Bertingkat (Opsional)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = wholesalePriceText,
                            onValueChange = { wholesalePriceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Harga Grosir (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = wholesaleMinQtyText,
                            onValueChange = { wholesaleMinQtyText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }.replace(',', '.') },
                            label = { Text("Min. Jumlah") },
                            placeholder = { Text("Contoh: 5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Supplier
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier / Distributor (Opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Catatan / Keterangan (Opsional)") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Pinned Bottom Footer Action Bar
                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (initialProduct != null && onDelete != null) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("delete_product_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AlertRed)
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
                                if (name.isNotBlank() && sellPriceNum > 0) {
                                    val newProduct = Product(
                                        id = initialProduct?.id ?: 0L,
                                        barcode = barcode.trim(),
                                        name = name.trim(),
                                        category = selectedCategory,
                                        buyPrice = buyPriceText.toDoubleOrNull() ?: 0.0,
                                        sellPrice = sellPriceNum,
                                        stock = stockText.toDoubleOrNull() ?: 0.0,
                                        unit = unit,
                                        minStockAlert = minStockText.toDoubleOrNull() ?: 5.0,
                                        wholesalePrice = wholesalePriceText.toDoubleOrNull() ?: 0.0,
                                        wholesaleMinQty = wholesaleMinQtyText.toDoubleOrNull() ?: 0.0,
                                        description = description.trim(),
                                        supplier = supplier.trim(),
                                        isActive = true,
                                        createdAt = initialProduct?.createdAt ?: System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    onSave(newProduct)
                                    onDismiss()
                                }
                            },
                            enabled = name.isNotBlank() && sellPriceNum > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_product_button")
                        ) {
                            Text("Simpan Produk", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && initialProduct != null && onDelete != null) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Hapus Barang?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                        Spacer(modifier = Modifier.height(8.dp))
                    Text("Apakah Anda yakin ingin menghapus '${initialProduct.name}' dari daftar stok?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showDeleteConfirm = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onDelete(initialProduct.id)
                                showDeleteConfirm = false
                                onDismiss()
                            },
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
