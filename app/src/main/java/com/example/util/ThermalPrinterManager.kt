package com.example.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Base64
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.SubscriptionPayment
import com.example.data.model.TransactionOrder
import com.example.data.model.TransactionItem
import com.example.ui.components.CurrencyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

object ThermalPrinterManager {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // ESC/POS Command Constants
    private val ESC_INIT = byteArrayOf(0x1B, 0x40) // Initialize printer
    private val ESC_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    private val ESC_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    private val ESC_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)
    private val ESC_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    private val ESC_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    private val ESC_FEED_PAPER_5 = byteArrayOf(0x1B, 0x64, 0x05) // Feed 5 lines
    private val ESC_CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x42, 0x00)

    /**
     * Convert a Bitmap to ESC/POS raster bytes for printing via GS v 0.
     * Packs 8 horizontal pixels per byte (MSB = leftmost pixel).
     * Black pixel = bit 1, White pixel = bit 0.
     * Returns the complete GS v 0 command bytes ready to write to the printer OutputStream.
     *
     * @param bitmap The monochrome (black & white) bitmap to convert
     * @param maxWidthDots Max print width in dots (58mm ≈ 384 dots, 80mm ≈ 576 dots)
     */
    /**
     * Convert a Bitmap to ESC/POS raster bytes for printing via GS v 0.
     * Generates a centered QR code on a full-width white canvas (maxWidthDots)
     * so that the QR code is guaranteed to be centered on ALL printers without relying on ESC a 1.
     *
     * @param bitmap The monochrome (black & white) bitmap to convert
     * @param maxWidthDots Max print width in dots (58mm ≈ 384 dots, 80mm ≈ 576 dots)
     */
    private fun bitmapToEscPosRaster(bitmap: Bitmap, maxWidthDots: Int = 384): ByteArray {
        // Ideal QR size: 240 dots for 58mm (leaves ~72 dots margins on each side), 320 dots for 80mm
        val targetQrSize = if (maxWidthDots > 400) 320 else 240
        val scaledQr = Bitmap.createScaledBitmap(bitmap, targetQrSize, targetQrSize, true)

        // Create full-width bitmap with white background and centered QR code
        val fullWidthBitmap = Bitmap.createBitmap(maxWidthDots, targetQrSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(fullWidthBitmap)
        canvas.drawColor(Color.WHITE)
        val left = ((maxWidthDots - targetQrSize) / 2f).coerceAtLeast(0f)
        canvas.drawBitmap(scaledQr, left, 0f, null)

        val widthBytes = (maxWidthDots + 7) / 8 // Round up to byte boundary
        val height = fullWidthBitmap.height

        // Build raster data
        val rasterData = ByteArray(widthBytes * height)
        for (y in 0 until height) {
            for (x in 0 until maxWidthDots) {
                val pixel = fullWidthBitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                // Threshold: dark pixels (<= 128) become black (bit = 1)
                if (luminance <= 128) {
                    val byteIndex = y * widthBytes + (x / 8)
                    val bitIndex = 7 - (x % 8)
                    rasterData[byteIndex] = (rasterData[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                }
            }
        }

        if (scaledQr != bitmap) scaledQr.recycle()
        fullWidthBitmap.recycle()

        // GS v 0 command: 1D 76 30 m xL xH yL yH <data>
        // m = 0 (Normal mode)
        // xL xH = width in bytes (low byte, high byte)
        // yL yH = height in dots (low byte, high byte)
        val xL = (widthBytes and 0xFF).toByte()
        val xH = ((widthBytes shr 8) and 0xFF).toByte()
        val yL = (height and 0xFF).toByte()
        val yH = ((height shr 8) and 0xFF).toByte()

        val header = byteArrayOf(0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH)
        return header + rasterData
    }

    /**
     * Write a QRIS QR code bitmap to the ESC/POS output stream.
     * Generates a dynamic QRIS payload with the exact transaction amount,
     * renders a QR code bitmap, and prints it centered on the receipt.
     *
     * @param outputStream The Bluetooth socket output stream
     * @param qrisPayload The static QRIS EMVCo payload string from StoreProfile
     * @param amount The transaction grand total amount
     * @param paperSize "58mm" or "80mm"
     */
    private fun writeQrisToStream(
        outputStream: OutputStream,
        qrisPayload: String,
        amount: Double,
        paperSize: String = "58mm"
    ) {
        if (qrisPayload.isBlank() || amount <= 0) return

        try {
            // Convert static QRIS to dynamic with exact amount (with fallback to static if conversion fails)
            val dynamicPayload = try {
                QrisEngine.convertStaticQrisToDynamic(qrisPayload, amount)
            } catch (e: Exception) {
                qrisPayload
            }
            val merchantName = QrisEngine.getMerchantNameFromPayload(qrisPayload)

            // Generate QR code bitmap
            val qrBitmap = QrCodeGenerator.generateQrBitmap(dynamicPayload, 300, 300, margin = 1)
                ?: return

            val lineWidth = if (paperSize.contains("80")) 48 else 32
            val maxWidthDots = if (paperSize.contains("80")) 576 else 384

            // Print separator and QRIS header
            val qrisHeader = buildString {
                append("-".repeat(lineWidth)).append("\r\n")
                append(centerText("SCAN QRIS UNTUK BAYAR", lineWidth)).append("\r\n")
                append(centerText(CurrencyUtils.formatRupiah(amount), lineWidth)).append("\r\n")
                append("\r\n")
            }
            outputStream.write(qrisHeader.toByteArray(Charsets.ISO_8859_1))

            // Print QR code bitmap as raster image (already centered on maxWidthDots)
            val rasterBytes = bitmapToEscPosRaster(qrBitmap, maxWidthDots)
            outputStream.write(rasterBytes)

            // Print merchant name below QR
            val qrisFooter = buildString {
                append("\r\n")
                append(centerText(merchantName, lineWidth)).append("\r\n")
                append(centerText("Gunakan GoPay/OVO/Dana/", lineWidth)).append("\r\n")
                append(centerText("ShopeePay/mBanking", lineWidth)).append("\r\n")
                append("-".repeat(lineWidth)).append("\r\n")
            }
            outputStream.write(qrisFooter.toByteArray(Charsets.ISO_8859_1))

            qrBitmap.recycle()
        } catch (e: Exception) {
            // Silently skip QRIS printing if conversion fails - receipt text is still printed
            e.printStackTrace()
        }
    }

    data class PairedPrinter(
        val name: String,
        val address: String
    )

    /**
     * List all bonded/paired Bluetooth devices that could be thermal printers
     */
    @SuppressLint("MissingPermission")
    fun getPairedPrinters(context: Context): List<PairedPrinter> {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()

        return try {
            adapter.bondedDevices.map { device ->
                PairedPrinter(
                    name = device.name ?: "Printer (${device.address})",
                    address = device.address
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Generate plain formatted receipt text (58mm = 32 chars, 80mm = 48 chars)
     */
    fun buildReceiptText(
        order: TransactionOrder,
        items: List<TransactionItem>,
        storeProfile: StoreProfile?,
        lineWidth: Int = 32
    ): String {
        val sb = StringBuilder()
        val storeName = storeProfile?.storeName?.ifBlank { "TOKO KASIR" } ?: "TOKO KASIR"
        val address = storeProfile?.address ?: ""
        val phone = storeProfile?.phone ?: ""
        val footer = storeProfile?.receiptFooter?.ifBlank { "Terima kasih atas kunjungan Anda!" } ?: "Terima kasih atas kunjungan Anda!"

        // Header
        sb.append(centerText(storeName.uppercase(), lineWidth)).append("\r\n")
        if (address.isNotBlank()) {
            sb.append(centerText(address, lineWidth)).append("\r\n")
        }
        if (phone.isNotBlank()) {
            sb.append(centerText("Telp: $phone", lineWidth)).append("\r\n")
        }
        sb.append("=".repeat(lineWidth)).append("\r\n")

        // Metadata
        sb.append("No. Faktur: ${order.invoiceNumber}\r\n")
        sb.append("Tanggal   : ${CurrencyUtils.formatDate(order.timestamp)}\r\n")
        sb.append("Pelanggan : ${order.customerName}\r\n")
        val payMethod = PaymentMethod.fromString(order.paymentMethod)
        sb.append("Metode    : ${payMethod.displayName}\r\n")
        sb.append("-".repeat(lineWidth)).append("\r\n")

        // Items
        for (item in items) {
            sb.append(item.productName).append("\r\n")
            val qtyPrice = "${CurrencyUtils.formatQty(item.quantity)} ${item.unit} x ${CurrencyUtils.formatRupiah(item.sellPrice)}"
            val subtotal = CurrencyUtils.formatRupiah(item.subtotal)
            sb.append(twoColumnText(qtyPrice, subtotal, lineWidth)).append("\r\n")
        }
        sb.append("-".repeat(lineWidth)).append("\r\n")

        // Summary
        sb.append(twoColumnText("Subtotal", CurrencyUtils.formatRupiah(order.subtotal), lineWidth)).append("\r\n")
        if (order.discountAmount > 0) {
            sb.append(twoColumnText("Diskon", "-${CurrencyUtils.formatRupiah(order.discountAmount)}", lineWidth)).append("\r\n")
        }
        if (order.taxAmount > 0) {
            sb.append(twoColumnText("PPN", CurrencyUtils.formatRupiah(order.taxAmount), lineWidth)).append("\r\n")
        }
        sb.append("=".repeat(lineWidth)).append("\r\n")
        sb.append(twoColumnText("TOTAL AKHIR", CurrencyUtils.formatRupiah(order.grandTotal), lineWidth)).append("\r\n")

        if (payMethod == PaymentMethod.CASH) {
            sb.append(twoColumnText("Tunai", CurrencyUtils.formatRupiah(order.paidAmount), lineWidth)).append("\r\n")
            sb.append(twoColumnText("Kembalian", CurrencyUtils.formatRupiah(order.changeAmount), lineWidth)).append("\r\n")
        } else if (payMethod == PaymentMethod.DEBT) {
            sb.append(twoColumnText("Status", "BELUM LUNAS (TEMPO)", lineWidth)).append("\r\n")
        }

        sb.append("=".repeat(lineWidth)).append("\r\n")
        sb.append(centerText(footer, lineWidth)).append("\r\n")
        
        // Balanced 3 Trailing blank lines
        sb.append("   \r\n   \r\n   \r\n")

        return sb.toString()
    }

    /**
     * Print direct via Bluetooth ESC/POS Socket with auto-fallback and flush delay.
     * When qrisPayload is provided and payment method is QRIS, a dynamic QR code
     * matching the exact transaction amount is printed on the receipt.
     */
    @SuppressLint("MissingPermission")
    suspend fun printBluetoothReceipt(
        context: Context,
        deviceAddress: String,
        order: TransactionOrder,
        items: List<TransactionItem>,
        storeProfile: StoreProfile?,
        paperSize: String = "58mm",
        qrisPayload: String = ""
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
            ?: return@withContext Result.failure(Exception("Bluetooth tidak didukung pada perangkat ini"))

        if (!adapter.isEnabled) {
            return@withContext Result.failure(Exception("Bluetooth belum aktif. Silakan aktifkan Bluetooth."))
        }

        val device: BluetoothDevice = try {
            adapter.getRemoteDevice(deviceAddress)
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Alamat printer Bluetooth tidak valid: ${e.message}"))
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            adapter.cancelDiscovery()

            // Try Standard SPP UUID first
            socket = try {
                val s = device.createRfcommSocketToServiceRecord(SPP_UUID)
                s.connect()
                s
            } catch (e1: Exception) {
                // Fallback to Reflection RFCOMM Port 1 (common for POS-58, MPT-II, GOOJPRT)
                try {
                    val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    val s = m.invoke(device, 1) as BluetoothSocket
                    s.connect()
                    s
                } catch (e2: Exception) {
                    throw Exception("Gagal menghubungkan ke printer (${device.name ?: deviceAddress}): ${e2.message}")
                }
            }

            outputStream = socket.outputStream

            val lineWidth = if (paperSize.contains("80")) 48 else 32
            val text = buildReceiptText(order, items, storeProfile, lineWidth)

            // 1. Initialize ESC/POS
            outputStream.write(ESC_INIT)
            
            // 2. Write raw formatted bytes (using ISO-8859-1 for maximum thermal printer compatibility)
            val printBytes = try {
                text.toByteArray(Charsets.ISO_8859_1)
            } catch (_: Exception) {
                text.toByteArray(Charsets.UTF_8)
            }
            outputStream.write(printBytes)

            // 3. Print QRIS QR Code if payment method is QRIS and payload is available
            val payMethod = PaymentMethod.fromString(order.paymentMethod)
            val effectiveQris = qrisPayload.ifBlank { storeProfile?.qrisStaticPayload ?: "" }
            if (payMethod == PaymentMethod.QRIS && effectiveQris.isNotBlank() && order.grandTotal > 0) {
                writeQrisToStream(outputStream, effectiveQris, order.grandTotal, paperSize)
            }

            // 4. Line feed & Paper Feed commands:
            // Balanced ~2.5 cm paper feed so the receipt clears the tear bar without wasting paper
            if (payMethod == PaymentMethod.QRIS && effectiveQris.isNotBlank() && order.grandTotal > 0) {
                // ESC J 176 (Feed paper by 176 dots ≈ 22 mm)
                outputStream.write(byteArrayOf(0x1B, 0x4A, 0xB0.toByte()))
                // 1 blank line with space (≈ 3-4 mm) -> Total feed ≈ 2.5 cm (hemat kertas & aman sobek)
                outputStream.write("   \r\n".toByteArray(Charsets.ISO_8859_1))
            } else {
                outputStream.write(byteArrayOf(0x1B, 0x64, 0x02))
                outputStream.write("   \r\n".toByteArray(Charsets.ISO_8859_1))
            }
            outputStream.flush()

            // 5. Crucial: Wait for Bluetooth buffer to empty into printer head before closing socket
            val delayMs = if (payMethod == PaymentMethod.QRIS && effectiveQris.isNotBlank()) 2500L else 1200L
            kotlinx.coroutines.delay(delayMs)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mencetak: ${e.message}"))
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Generate plain formatted subscription payment receipt text (58mm / 80mm)
     */
    fun buildSubscriptionReceiptText(
        payment: SubscriptionPayment,
        paidUntilTimestamp: Long,
        storeProfile: StoreProfile?,
        lineWidth: Int = 32
    ): String {
        val sb = StringBuilder()
        val storeName = storeProfile?.storeName?.ifBlank { "TOKO KASIR & TAGIHAN" } ?: "TOKO KASIR & TAGIHAN"
        val address = storeProfile?.address ?: ""
        val phone = storeProfile?.phone ?: ""
        val footer = storeProfile?.receiptFooter?.ifBlank { "Terima kasih atas pembayaran Anda!" } ?: "Terima kasih atas pembayaran Anda!"

        // Header
        sb.append(centerText(storeName.uppercase(), lineWidth)).append("\r\n")
        if (address.isNotBlank()) {
            sb.append(centerText(address, lineWidth)).append("\r\n")
        }
        if (phone.isNotBlank()) {
            sb.append(centerText("Telp: $phone", lineWidth)).append("\r\n")
        }
        sb.append("=".repeat(lineWidth)).append("\r\n")
        sb.append(centerText("KUITANSI PEMBAYARAN", lineWidth)).append("\r\n")
        sb.append(centerText("LANGGANAN", lineWidth)).append("\r\n")
        sb.append("=".repeat(lineWidth)).append("\r\n")

        // Metadata
        sb.append("No. Faktur: ${payment.invoiceNumber}\r\n")
        sb.append("Tanggal   : ${CurrencyUtils.formatDate(payment.timestamp)}\r\n")
        if (payment.customerCode.isNotBlank()) {
            sb.append("ID Member : ${payment.customerCode}\r\n")
        }
        sb.append("Pelanggan : ${payment.customerName}\r\n")
        sb.append("Layanan   : ${payment.packageName}\r\n")
        sb.append("Metode    : ${payment.paymentMethod}\r\n")
        sb.append("-".repeat(lineWidth)).append("\r\n")

        // Items
        val qtyPrice = "${payment.monthsCount} Bulan x ${CurrencyUtils.formatRupiah(payment.monthlyPrice)}"
        val total = CurrencyUtils.formatRupiah(payment.totalAmount)
        sb.append(twoColumnText(qtyPrice, total, lineWidth)).append("\r\n")
        sb.append("-".repeat(lineWidth)).append("\r\n")

        // Status & Valid Until
        sb.append(twoColumnText("Status", "LUNAS", lineWidth)).append("\r\n")
        sb.append(twoColumnText("Lunas S/D", CurrencyUtils.formatDateOnly(paidUntilTimestamp), lineWidth)).append("\r\n")
        sb.append("=".repeat(lineWidth)).append("\r\n")
        sb.append(twoColumnText("TOTAL BAYAR", total, lineWidth)).append("\r\n")
        sb.append("=".repeat(lineWidth)).append("\r\n")
        sb.append(centerText(footer, lineWidth)).append("\r\n")
        
        // Trailing feed
        sb.append("\r\n\r\n\r\n")

        return sb.toString()
    }

    /**
     * Print subscription receipt directly to Bluetooth Thermal Printer
     */
    @SuppressLint("MissingPermission")
    suspend fun printSubscriptionReceipt(
        context: Context,
        deviceAddress: String,
        payment: SubscriptionPayment,
        paidUntilTimestamp: Long,
        storeProfile: StoreProfile?,
        paperSize: String = "58mm"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
            ?: return@withContext Result.failure(Exception("Bluetooth tidak didukung pada perangkat ini"))

        if (!adapter.isEnabled) {
            return@withContext Result.failure(Exception("Bluetooth belum aktif. Silakan aktifkan Bluetooth."))
        }

        val device: BluetoothDevice = try {
            adapter.getRemoteDevice(deviceAddress)
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Alamat printer Bluetooth tidak valid: ${e.message}"))
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            adapter.cancelDiscovery()

            socket = try {
                val s = device.createRfcommSocketToServiceRecord(SPP_UUID)
                s.connect()
                s
            } catch (e1: Exception) {
                try {
                    val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    val s = m.invoke(device, 1) as BluetoothSocket
                    s.connect()
                    s
                } catch (e2: Exception) {
                    throw Exception("Gagal menghubungkan ke printer (${device.name ?: deviceAddress}): ${e2.message}")
                }
            }

            outputStream = socket.outputStream

            val lineWidth = if (paperSize.contains("80")) 48 else 32
            val text = buildSubscriptionReceiptText(payment, paidUntilTimestamp, storeProfile, lineWidth)

            outputStream.write(ESC_INIT)
            val printBytes = try {
                text.toByteArray(Charsets.ISO_8859_1)
            } catch (_: Exception) {
                text.toByteArray(Charsets.UTF_8)
            }
            outputStream.write(printBytes)
            outputStream.write(byteArrayOf(0x1B, 0x64, 0x02))
            outputStream.flush()
            kotlinx.coroutines.delay(1200)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mencetak struk langganan: ${e.message}"))
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Print standard Product Barcode Label on Bluetooth Thermal Printer
     */
    @SuppressLint("MissingPermission")
    suspend fun printProductBarcodeLabel(
        context: Context,
        deviceAddress: String,
        product: Product,
        copies: Int = 1,
        storeName: String = "TOKO KASIR"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
            ?: return@withContext Result.failure(Exception("Bluetooth tidak didukung pada perangkat ini"))

        if (!adapter.isEnabled) {
            return@withContext Result.failure(Exception("Bluetooth belum aktif. Silakan aktifkan Bluetooth."))
        }

        val device: BluetoothDevice = try {
            adapter.getRemoteDevice(deviceAddress)
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Alamat printer tidak valid: ${e.message}"))
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            adapter.cancelDiscovery()
            socket = try {
                val s = device.createRfcommSocketToServiceRecord(SPP_UUID)
                s.connect()
                s
            } catch (e1: Exception) {
                val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                val s = m.invoke(device, 1) as BluetoothSocket
                s.connect()
                s
            }

            outputStream = socket.outputStream

            for (c in 1..copies) {
                // Initialize & Center
                outputStream.write(ESC_INIT)
                outputStream.write(ESC_ALIGN_CENTER)

                val labelHeader = buildString {
                    append(storeName.uppercase()).append("\r\n")
                    append(product.name).append("\r\n")
                    append(CurrencyUtils.formatRupiah(product.sellPrice)).append("\r\n")
                }
                outputStream.write(labelHeader.toByteArray(Charsets.ISO_8859_1))

                val code = product.barcode.ifBlank { product.id.toString() }
                val codeBytes = code.toByteArray(Charsets.US_ASCII)

                // GS h 50 (Height), GS w 2 (Width), GS H 2 (HRI text below)
                outputStream.write(byteArrayOf(0x1D, 0x68, 50))
                outputStream.write(byteArrayOf(0x1D, 0x77, 2))
                outputStream.write(byteArrayOf(0x1D, 0x48, 2))

                // CODE128 command: GS k 73 <len> {B <data>
                val code128Payload = byteArrayOf(0x1D, 0x6B, 73, (codeBytes.size + 2).toByte(), 0x7B, 0x42) + codeBytes
                outputStream.write(code128Payload)
                outputStream.write("\r\n\r\n".toByteArray(Charsets.ISO_8859_1))

                if (c < copies) {
                    outputStream.write(byteArrayOf(0x1B, 0x64, 0x02))
                }
            }

            outputStream.write(byteArrayOf(0x1B, 0x64, 0x02))
            outputStream.flush()
            kotlinx.coroutines.delay(1200)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal cetak label barcode: ${e.message}"))
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Print Customer Member ID Card with Barcode via Bluetooth Thermal Printer
     */
    @SuppressLint("MissingPermission")
    suspend fun printCustomerIdCard(
        deviceAddress: String,
        storeName: String,
        customerName: String,
        customerCode: String,
        packageName: String? = null,
        phone: String? = null,
        copies: Int = 1
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                ?: return@withContext Result.failure(Exception("Bluetooth tidak didukung"))
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)

            socket = try {
                device.createRfcommSocketToServiceRecord(SPP_UUID).apply { connect() }
            } catch (e: Exception) {
                val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                val s = m.invoke(device, 1) as BluetoothSocket
                s.connect()
                s
            }

            outputStream = socket.outputStream

            for (c in 1..copies) {
                outputStream.write(ESC_INIT)
                outputStream.write(ESC_ALIGN_CENTER)

                val header = buildString {
                    append(storeName.uppercase()).append("\r\n")
                    append("--- KARTU MEMBER / PELANGGAN ---\r\n")
                    append(customerName).append("\r\n")
                    if (!packageName.isNullOrBlank()) {
                        append("Paket: $packageName\r\n")
                    }
                    if (!phone.isNullOrBlank()) {
                        append("No. WA: $phone\r\n")
                    }
                }
                outputStream.write(header.toByteArray(Charsets.ISO_8859_1))

                val code = customerCode.ifBlank { "PLG-001" }
                val codeBytes = code.toByteArray(Charsets.US_ASCII)

                // GS h 60 (Height), GS w 2 (Width), GS H 2 (HRI text below)
                outputStream.write(byteArrayOf(0x1D, 0x68, 60))
                outputStream.write(byteArrayOf(0x1D, 0x77, 2))
                outputStream.write(byteArrayOf(0x1D, 0x48, 2))

                // CODE128 command: GS k 73 <len> {B <data>
                val code128Payload = byteArrayOf(0x1D, 0x6B, 73, (codeBytes.size + 2).toByte(), 0x7B, 0x42) + codeBytes
                outputStream.write(code128Payload)
                outputStream.write("\r\n\r\n".toByteArray(Charsets.ISO_8859_1))

                if (c < copies) {
                    outputStream.write(byteArrayOf(0x1B, 0x64, 0x02))
                }
            }

            outputStream.write(byteArrayOf(0x1B, 0x64, 0x02))
            outputStream.flush()
            kotlinx.coroutines.delay(1200)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal cetak ID Card barcode: ${e.message}"))
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Launch RawBT Android App via Intent
     */
    fun printViaRawBt(
        context: Context,
        order: TransactionOrder,
        items: List<TransactionItem>,
        storeProfile: StoreProfile?,
        paperSize: String = "58mm"
    ): Boolean {
        val lineWidth = if (paperSize.contains("80")) 48 else 32
        val text = buildReceiptText(order, items, storeProfile, lineWidth)
        val base64Text = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("rawbt:base64,$base64Text")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // RawBT not installed, fallback to market or direct intent
            try {
                val sendIntent = Intent("ru.a402d.rawbtprinter.action.PRINT").apply {
                    putExtra("ru.a402d.rawbtprinter.extra.PRINT_RAW", text.toByteArray(Charsets.UTF_8))
                    type = "text/plain"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(sendIntent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val leftPad = (width - text.length) / 2
        val rightPad = width - text.length - leftPad
        return " ".repeat(leftPad) + text + " ".repeat(rightPad)
    }

    private fun twoColumnText(left: String, right: String, width: Int): String {
        val totalLen = left.length + right.length
        if (totalLen >= width) {
            val maxLeft = width - right.length - 1
            val trimmedLeft = if (maxLeft > 0) left.take(maxLeft) else left
            val spaces = (width - trimmedLeft.length - right.length).coerceAtLeast(1)
            return trimmedLeft + " ".repeat(spaces) + right
        }
        val spaces = width - totalLen
        return left + " ".repeat(spaces) + right
    }
}
