package com.example.data.model

data class CartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val selectedUnit: String = product.unit,
    val conversionFactor: Double = 1.0,
    val customPrice: Double? = null,
    val discount: Double = 0.0 // Rupiah discount per item
) {
    val isWholesaleApplied: Boolean
        get() = product.wholesaleMinQty > 0 && quantity >= product.wholesaleMinQty && product.wholesalePrice > 0

    val unitPrice: Double
        get() {
            if (customPrice != null) return customPrice
            val basePrice = if (isWholesaleApplied) product.wholesalePrice else product.sellPrice
            return basePrice * conversionFactor
        }

    val subtotal: Double
        get() = ((unitPrice * quantity) - discount).coerceAtLeast(0.0)

    val totalCost: Double
        get() = product.buyPrice * conversionFactor * quantity

    val totalProfit: Double
        get() = subtotal - totalCost
}
