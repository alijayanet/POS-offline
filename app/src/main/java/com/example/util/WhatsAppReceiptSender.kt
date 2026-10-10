package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import com.example.ui.components.CurrencyUtils
import java.net.URLEncoder

object WhatsAppReceiptSender {

    fun generateReceiptText(
        order: TransactionOrder,
        items: List<TransactionItem>,
        profile: StoreProfile?
    ): String {
        val storeName = profile?.storeName?.ifBlank { "Toko Ali Jaya" } ?: "Toko Ali Jaya"
        val address = profile?.address ?: ""
        val phone = profile?.phone ?: ""
        val footer = profile?.receiptFooter?.ifBlank { "Terima kasih atas kunjungan Anda!" } ?: "Terima kasih atas kunjungan Anda!"

        val sb = StringBuilder()
        sb.append("🧾 *STRUK PEMBELIAN*\n")
        sb.append("🏪 *").append(storeName).append("*\n")
        if (address.isNotBlank()) sb.append("📍 ").append(address).append("\n")
        if (phone.isNotBlank()) sb.append("📞 ").append(phone).append("\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("No. Faktur: *").append(order.invoiceNumber).append("*\n")
        sb.append("Tanggal: ").append(CurrencyUtils.formatDate(order.timestamp)).append("\n")
        sb.append("Pelanggan: ").append(order.customerName).append("\n")
        sb.append("Metode: ").append(order.paymentMethod).append("\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")

        items.forEachIndexed { index, item ->
            sb.append("${index + 1}. *${item.productName}*\n")
            sb.append("   ${CurrencyUtils.formatQty(item.quantity)} ${item.unit} x ${CurrencyUtils.formatRupiah(item.sellPrice)} = *${CurrencyUtils.formatRupiah(item.subtotal)}*\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Subtotal: ${CurrencyUtils.formatRupiah(order.subtotal)}\n")
        if (order.discountAmount > 0) {
            sb.append("Diskon: -${CurrencyUtils.formatRupiah(order.discountAmount)}\n")
        }
        if (order.taxAmount > 0) {
            sb.append("Pajak: +${CurrencyUtils.formatRupiah(order.taxAmount)}\n")
        }
        sb.append("💰 *TOTAL: ${CurrencyUtils.formatRupiah(order.grandTotal)}*\n")
        sb.append("Bayar: ${CurrencyUtils.formatRupiah(order.paidAmount)}\n")
        sb.append("Kembalian: ${CurrencyUtils.formatRupiah(order.changeAmount)}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append(footer)

        return sb.toString()
    }

    fun sendReceiptViaWhatsApp(
        context: Context,
        order: TransactionOrder,
        items: List<TransactionItem>,
        profile: StoreProfile?,
        targetPhone: String? = null
    ) {
        try {
            val receiptText = generateReceiptText(order, items, profile)
            val cleanPhone = formatIndonesianPhone(targetPhone)

            if (cleanPhone.isNotBlank()) {
                // Direct WhatsApp URL to phone number
                val encodedText = URLEncoder.encode(receiptText, "UTF-8")
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                // Share to WhatsApp / Chooser
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, receiptText)
                    putExtra(Intent.EXTRA_SUBJECT, "Struk Pembelian ${order.invoiceNumber}")
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    context.startActivity(sendIntent)
                } catch (e: Exception) {
                    // Fallback to general chooser if WhatsApp specific intent fails
                    val chooserIntent = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, receiptText)
                        },
                        "Kirim Struk Digital via"
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooserIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatIndonesianPhone(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        var p = phone.replace(Regex("[^0-9]"), "")
        if (p.startsWith("0")) {
            p = "62" + p.substring(1)
        } else if (p.startsWith("8")) {
            p = "62" + p
        }
        return p
    }
}
