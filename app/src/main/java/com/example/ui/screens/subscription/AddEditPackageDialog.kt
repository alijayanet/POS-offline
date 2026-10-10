package com.example.ui.screens.subscription

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubscriptionPackage
import com.example.ui.theme.EditorialCardWhite
import com.example.ui.theme.EditorialNavy
import com.example.ui.theme.EditorialOutline
import com.example.ui.theme.EditorialPrimaryBlue

@Composable
fun AddEditPackageDialog(
    packageToEdit: SubscriptionPackage? = null,
    onDismiss: () -> Unit,
    onSave: (SubscriptionPackage) -> Unit
) {
    var name by remember { mutableStateOf(packageToEdit?.name ?: "") }
    var priceText by remember { mutableStateOf(if (packageToEdit != null) packageToEdit.price.toLong().toString() else "") }
    var speedInfo by remember { mutableStateOf(packageToEdit?.speedInfo ?: "") }
    var description by remember { mutableStateOf(packageToEdit?.description ?: "") }

    val priceNum = priceText.toDoubleOrNull() ?: 0.0
    val isFormValid = name.isNotBlank() && priceNum > 0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, EditorialOutline, RoundedCornerShape(24.dp)),
            color = EditorialCardWhite
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = if (packageToEdit == null) "Tambah Paket Langganan" else "Edit Paket Langganan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EditorialNavy
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Paket *") },
                    placeholder = { Text("Contoh: Paket Gold 30 Mbps") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tarif Bulanan (Rp) *") },
                    leadingIcon = { Text("Rp ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = speedInfo,
                    onValueChange = { speedInfo = it },
                    label = { Text("Kecepatan / Tipe (Opsional)") },
                    placeholder = { Text("Contoh: 30 Mbps Unlimited / 60 Channel") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Paket (Opsional)") },
                    placeholder = { Text("Contoh: Pilihan favorit keluarga...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            if (isFormValid) {
                                onSave(
                                    SubscriptionPackage(
                                        id = packageToEdit?.id ?: 0L,
                                        name = name.trim(),
                                        price = priceNum,
                                        speedInfo = speedInfo.trim(),
                                        description = description.trim(),
                                        isActive = true,
                                        createdAt = packageToEdit?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialPrimaryBlue)
                    ) {
                        Text("Simpan Paket", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
