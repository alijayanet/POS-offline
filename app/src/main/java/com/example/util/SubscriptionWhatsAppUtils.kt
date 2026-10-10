package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.StoreProfile
import com.example.data.model.SubscriptionCustomer
import com.example.data.model.SubscriptionPayment
import com.example.ui.components.CurrencyUtils
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SubscriptionWhatsAppUtils {

    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    private val fullDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale("id", "ID"))

    /**
     * Generate Modern & Detailed Billing Reminder or Overdue Notice
     */
    fun generateBillingMessage(
        customer: SubscriptionCustomer,
        monthsDue: Int,
        totalAmount: Double,
        dueDateTimestamp: Long,
        storeProfile: StoreProfile?
    ): String {
        val storeName = storeProfile?.storeName?.ifBlank { "TOKO POS INTERNET & TV" } ?: "TOKO POS INTERNET & TV"
        val phone = storeProfile?.phone ?: ""
        val address = storeProfile?.address ?: ""

        val isOverdue = monthsDue > 1

        val sb = StringBuilder()

        if (isOverdue) {
            sb.append("⚠️ *PEMBERITAHUAN TUNGGAKAN TAGIHAN*\n")
        } else {
            sb.append("🔔 *PEMBERITAHUAN TAGIHAN BULANAN*\n")
        }

        sb.append("🏪 *").append(storeName.uppercase()).append("*\n")
        if (address.isNotBlank()) sb.append("📍 ").append(address).append("\n")
        if (phone.isNotBlank()) sb.append("📞 CS: ").append(phone).append("\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Halo Kak *").append(customer.name).append("*, semoga sehat dan lancar selalu! ✨\n\n")

        if (isOverdue) {
            sb.append("Kami menginformasikan rincian tagihan layanan Anda yang telah jatuh tempo:\n\n")
        } else {
            sb.append("Berikut adalah rincian tagihan langganan Anda untuk periode bulan ini:\n\n")
        }

        sb.append("👤 *DATA PELANGGAN:*\n")
        if (customer.customerCode.isNotBlank()) {
            sb.append("• ID Pelanggan : `").append(customer.customerCode).append("`\n")
        }
        sb.append("• Nama         : *").append(customer.name).append("*\n")
        if (customer.address.isNotBlank()) {
            sb.append("• Alamat       : ").append(customer.address).append("\n")
        }
        sb.append("• Paket        : *").append(customer.packageName).append("*\n")
        sb.append("• Jatuh Tempo  : 📅 *").append(fullDateFormat.format(Date(dueDateTimestamp))).append("*\n")
        sb.append("• Status       : ").append(if (isOverdue) "🔴 *Menunggak $monthsDue Bulan*" else "🟡 *Belum Bayar ($monthsDue Bulan)*").append("\n\n")

        sb.append("💰 *RINCIAN PEMBAYARAN:*\n")
        sb.append("• Tarif / Bulan : ").append(CurrencyUtils.formatRupiah(customer.monthlyPrice)).append("\n")
        sb.append("• Jumlah Bulan  : ").append(monthsDue).append(" Bulan\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💳 *TOTAL TAGIHAN : ").append(CurrencyUtils.formatRupiah(totalAmount)).append("*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n\n")

        sb.append("📌 *Pilihan Pembayaran Transfer:*\n")
        val bankInfo = storeProfile?.bankAccountsInfo?.ifBlank {
            "🏧 *BCA*  : `8820 1928 334` (a.n Ali Jaya)\n🏧 *BRI*  : `0123 0100 4567 508` (a.n Ali Jaya)\n🏧 *Dana/OVO/ShopeePay* : `0812 3456 7890`\n📱 *Kasir Toko* : Tersedia Tunai / QRIS di toko"
        } ?: "🏧 *BCA*  : `8820 1928 334` (a.n Ali Jaya)\n🏧 *BRI*  : `0123 0100 4567 508` (a.n Ali Jaya)\n🏧 *Dana/OVO/ShopeePay* : `0812 3456 7890`\n📱 *Kasir Toko* : Tersedia Tunai / QRIS di toko"
        sb.append(bankInfo).append("\n\n")

        if (isOverdue) {
            sb.append("_Agar koneksi/layanan Anda tetap aktif dan terhindar dari isolir sementara, mohon segera melakukan pembayaran. Jika sudah transfer, mohon kirimkan bukti pembayaran. Terima kasih atas pengertiannya!_ 🙏")
        } else {
            sb.append("_Mohon kirimkan bukti transfer setelah pembayaran berhasil. Jika Anda sudah melakukan pembayaran, silakan abaikan pesan ini. Terima kasih banyak atas kepercayaannya!_ 🙏")
        }

        return sb.toString()
    }

    /**
     * Generate Modern Digital Payment Receipt
     */
    fun generatePaymentReceiptMessage(
        payment: SubscriptionPayment,
        paidUntilTimestamp: Long,
        storeProfile: StoreProfile?
    ): String {
        val storeName = storeProfile?.storeName?.ifBlank { "TOKO POS INTERNET & TV" } ?: "TOKO POS INTERNET & TV"
        val phone = storeProfile?.phone ?: ""
        val address = storeProfile?.address ?: ""

        val sb = StringBuilder()
        sb.append("✅ *KUITANSI PEMBAYARAN LUNAS*\n")
        sb.append("🏪 *").append(storeName.uppercase()).append("*\n")
        if (address.isNotBlank()) sb.append("📍 ").append(address).append("\n")
        if (phone.isNotBlank()) sb.append("📞 ").append(phone).append("\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("No. Kuitansi : `").append(payment.invoiceNumber).append("`\n")
        sb.append("Tanggal      : ").append(dateTimeFormat.format(Date(payment.timestamp))).append(" WIB\n\n")

        sb.append("👤 *DATA PELANGGAN:*\n")
        if (payment.customerCode.isNotBlank()) {
            sb.append("• ID Pelanggan : `").append(payment.customerCode).append("`\n")
        }
        sb.append("• Nama         : *").append(payment.customerName).append("*\n")
        sb.append("• Layanan      : *").append(payment.packageName).append("*\n\n")

        sb.append("📋 *RINCIAN PEMBAYARAN:*\n")
        sb.append("• Pembayaran   : *").append(payment.monthsCount).append(" Bulan*\n")
        sb.append("• Tarif / Bulan: ").append(CurrencyUtils.formatRupiah(payment.monthlyPrice)).append("\n")
        sb.append("• Status       : 🟢 *LUNAS*\n")
        sb.append("• Lunas Sampai : 📅 *").append(fullDateFormat.format(Date(paidUntilTimestamp))).append("*\n")
        sb.append("• Metode Bayar : *").append(payment.paymentMethod).append("*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💰 *TOTAL DITERIMA : ").append(CurrencyUtils.formatRupiah(payment.totalAmount)).append("*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n\n")

        sb.append("_Terima kasih atas pembayaran Anda! Masa aktif layanan Anda telah berhasil diperpanjang. Selamat beraktivitas!_ ✨🙏")

        return sb.toString()
    }

    /**
     * Launch WhatsApp Intent (Direct chat or Share)
     */
    fun sendWhatsAppMessage(
        context: Context,
        phone: String?,
        message: String
    ) {
        try {
            val cleanPhone = formatIndonesianPhone(phone)
            if (cleanPhone.isNotBlank()) {
                val encodedText = URLEncoder.encode(message, "UTF-8")
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra(Intent.EXTRA_SUBJECT, "Pemberitahuan Tagihan")
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(sendIntent)
                } catch (e: Exception) {
                    val chooserIntent = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        },
                        "Kirim Tagihan via"
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
