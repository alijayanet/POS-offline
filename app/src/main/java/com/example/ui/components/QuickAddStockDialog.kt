package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product

@Composable
fun QuickAddStockDialog(
    product: Product,
    onConfirm: (amount: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val isDecimalUnit = product.unit.lowercase().trim() in listOf(
        "kg", "kilogram", "liter", "ltr", "gram", "gr", "ons", "meter", "m", "cm", "mm", "m2", "m²", "m3", "m³", "kubik", "kwintal", "ton", "ml", "yard", "lembar"
    )
    var amountInput by remember { mutableStateOf(if (isDecimalUnit) "1" else "10") }
    var isAdding by remember { mutableStateOf(true) } // true for restock, false for reduction

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .testTag("quick_add_stock_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Sesuaikan Stok Barang",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Stok Saat Ini: ${CurrencyUtils.formatQty(product.stock)} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Add / Deduct
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isAdding = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdding) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isAdding) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("+ Tambah Stok")
                    }

                    Button(
                        onClick = { isAdding = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAdding) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!isAdding) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("- Kurangi Stok")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }.replace(',', '.') },
                    label = { Text(if (isAdding) "Jumlah Tambah (${product.unit})" else "Jumlah Kurang (${product.unit})") },
                    placeholder = { Text(if (isDecimalUnit) "Contoh: 1.5 atau 0.5" else "Contoh: 10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stock_amount_input")
                )

                // Quick preset buttons
                Spacer(modifier = Modifier.height(12.dp))
                val presets = if (isDecimalUnit) listOf("0.5", "1", "2", "5", "10") else listOf("5", "10", "20", "50", "100")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        OutlinedButton(
                            onClick = { amountInput = preset },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(preset)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val amountNum = amountInput.toDoubleOrNull() ?: 0.0
                val finalStock = if (isAdding) product.stock + amountNum else (product.stock - amountNum).coerceAtLeast(0.0)

                Text(
                    text = "Estimasi Stok Baru: ${CurrencyUtils.formatQty(finalStock)} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = amountInput.toDoubleOrNull() ?: 0.0
                            if (qty > 0) {
                                onConfirm(if (isAdding) qty else -qty)
                            }
                            onDismiss()
                        },
                        modifier = Modifier.testTag("confirm_stock_adjustment_button")
                    ) {
                        Text("Simpan Perubahan")
                    }
                }
            }
        }
    }
}
