package com.example.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyUtils {
    private val rupiahSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        currencySymbol = "Rp "
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
    }

    private val rupiahFormat = DecimalFormat("Rp #,###", rupiahSymbols).apply {
        maximumFractionDigits = 0
    }

    fun formatRupiah(amount: Double?): String {
        val value = amount ?: 0.0
        return rupiahFormat.format(value)
    }

    fun formatRupiahWithoutSymbol(amount: Double?): String {
        val value = amount ?: 0.0
        val format = DecimalFormat("#,###", rupiahSymbols).apply {
            maximumFractionDigits = 0
        }
        return format.format(value)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    /**
     * Format decimal or whole quantity numbers cleanly (e.g. 1, 1.5, 0.5, 2.75)
     */
    fun formatQty(qty: Double?): String {
        val value = qty ?: 0.0
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            val df = DecimalFormat("#.###", DecimalFormatSymbols(Locale.US))
            df.format(value)
        }
    }
}
