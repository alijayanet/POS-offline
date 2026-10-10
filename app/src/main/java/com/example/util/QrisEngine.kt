package com.example.util

import java.util.Locale

/**
 * QRIS EMVCo Engine
 * Mengonversi QRIS Statis Merchant menjadi QRIS Dinamis sesuai nominal transaksi.
 */
object QrisEngine {

    data class TlvItem(
        val tag: String,
        val value: String
    )

    fun normalizeQrisPayload(raw: String?): String {
        var s = (raw ?: "").replace(Regex("[\r\n\t ]+"), "").trim()
        val idx = s.indexOf("000201")
        if (idx > 0) {
            s = s.substring(idx)
        }
        val lastCrc = s.lastIndexOf("6304")
        if (lastCrc >= 0 && s.length >= lastCrc + 8) {
            s = s.substring(0, lastCrc + 8)
        }
        return s
    }

    fun crc16CcittFalse(input: String): Int {
        var crc = 0xFFFF
        for (i in input.indices) {
            val b = input[i].code and 0xFF
            crc = crc xor (b shl 8)
            for (bit in 0 until 8) {
                crc = if ((crc and 0x8000) != 0) {
                    ((crc shl 1) xor 0x1021) and 0xFFFF
                } else {
                    (crc shl 1) and 0xFFFF
                }
            }
        }
        return crc and 0xFFFF
    }

    fun parseEmvTlvString(input: String?): List<TlvItem> {
        val raw = normalizeQrisPayload(input)
        if (raw.isBlank()) throw IllegalArgumentException("Payload QRIS kosong")
        if (raw.length < 8) throw IllegalArgumentException("Payload QRIS terlalu pendek")

        val items = mutableListOf<TlvItem>()
        var i = 0
        while (i < raw.length) {
            if (i + 4 > raw.length) break
            val tag = raw.substring(i, i + 2)
            val lenStr = raw.substring(i + 2, i + 4)
            val len = lenStr.toIntOrNull() ?: break
            val start = i + 4
            val end = start + len
            if (end > raw.length) break
            val value = raw.substring(start, end)
            items.add(TlvItem(tag, value))
            i = end
        }
        return items
    }

    fun buildEmvTlvString(items: List<TlvItem>): String {
        val sb = StringBuilder()
        for (item in items) {
            val tag = item.tag
            val value = item.value
            val lenStr = String.format(Locale.US, "%02d", value.length)
            sb.append(tag).append(lenStr).append(value)
        }
        return sb.toString()
    }

    fun getMerchantNameFromPayload(staticPayload: String?): String {
        return try {
            val items = parseEmvTlvString(staticPayload)
            items.find { it.tag == "59" }?.value ?: "MERCHANT TOKO"
        } catch (e: Exception) {
            "MERCHANT TOKO"
        }
    }

    fun convertStaticQrisToDynamic(staticPayload: String?, amount: Double): String {
        val amt = amount.toLong().coerceAtLeast(0)
        if (amt <= 0) throw IllegalArgumentException("Nominal QRIS harus lebih besar dari 0")

        val normalized = normalizeQrisPayload(staticPayload)
        val source = parseEmvTlvString(normalized)

        val managed = setOf("54", "55", "56", "57", "63")
        val result = mutableListOf<TlvItem>()
        var amountInserted = false

        for (el in source) {
            if (managed.contains(el.tag)) continue
            if (el.tag == "01") {
                result.add(TlvItem("01", "12")) // 12 = Dynamic QR
                continue
            }
            if (el.tag == "58" && !amountInserted) {
                result.add(TlvItem("54", amt.toString()))
                amountInserted = true
            }
            result.add(el)
        }

        if (!amountInserted) {
            result.add(TlvItem("54", amt.toString()))
        }

        val body = buildEmvTlvString(result)
        val partial = body + "6304"
        val crc = String.format(Locale.US, "%04X", crc16CcittFalse(partial))
        return partial + crc
    }
}
