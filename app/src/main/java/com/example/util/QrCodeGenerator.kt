package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.tasks.await
import java.util.EnumMap

object QrCodeGenerator {

    /**
     * Generate QR Code Bitmap with customizable size and margins
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        margin: Int = 2
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
                put(EncodeHintType.MARGIN, margin)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decode QR Code or Barcode from an image Uri using Google ML Kit
     */
    suspend fun decodeFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputImage = InputImage.fromFilePath(context, uri)
            val scanner = BarcodeScanning.getClient()
            val barcodes = scanner.process(inputImage).await()
            for (barcode in barcodes) {
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrBlank()) {
                    return rawValue.trim()
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate standard 1D Barcode (CODE128 or EAN13) Bitmap for product labeling & printing
     */
    fun generateBarcodeBitmap(
        content: String,
        width: Int = 400,
        height: Int = 140
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.MARGIN, 1)
            }
            val format = if (content.length == 13 && content.all { it.isDigit() }) {
                BarcodeFormat.EAN_13
            } else {
                BarcodeFormat.CODE_128
            }
            val writer = com.google.zxing.MultiFormatWriter()
            val bitMatrix = writer.encode(content, format, width, height, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
