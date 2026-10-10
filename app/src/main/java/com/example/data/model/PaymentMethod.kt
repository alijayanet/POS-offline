package com.example.data.model

enum class PaymentMethod(val displayName: String) {
    CASH("Tunai"),
    QRIS("QRIS"),
    TRANSFER("Transfer Bank"),
    DEBT("Kasbon / Hutang");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: CASH
        }
    }
}
