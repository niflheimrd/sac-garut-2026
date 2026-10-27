package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Student
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap

object QrCodeGenerator {

  /**
   * Generates a monochrome QR code Bitmap using ZXing.
   */
  fun generateQrBitmap(
    content: String,
    sizePx: Int = 512,
    foregroundColor: Int = Color.BLACK,
    backgroundColor: Int = Color.WHITE
  ): Bitmap? {
    if (content.isBlank()) return null
    return try {
      val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
        put(EncodeHintType.MARGIN, 1)
        put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        put(EncodeHintType.CHARACTER_SET, "UTF-8")
      }
      val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
      val width = bitMatrix.width
      val height = bitMatrix.height
      val pixels = IntArray(width * height)
      for (y in 0 until height) {
        val offset = y * width
        for (x in 0 until width) {
          pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
        }
      }
      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
      bitmap
    } catch (e: Exception) {
      Log.e("QrCodeGenerator", "Error creating QR bitmap: ${e.message}", e)
      null
    }
  }

  /**
   * Draws a complete, elegant Student ID Member Card with the QR Code.
   */
  fun generateStudentIdCardBitmap(
    student: Student,
    qrBitmap: Bitmap
  ): Bitmap {
    val width = 720
    val height = 1040
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#F0F4F8")
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Inner Card Container
    val cardPaint = Paint().apply {
      isAntiAlias = true
      color = Color.WHITE
      setShadowLayer(16f, 0f, 6f, Color.argb(40, 0, 0, 0))
    }
    val cardRect = RectF(36f, 36f, (width - 36).toFloat(), (height - 36).toFloat())
    canvas.drawRoundRect(cardRect, 28f, 28f, cardPaint)

    // Header Banner
    val headerPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#0A2540") // Ocean Deep
    }
    val headerRect = RectF(36f, 36f, (width - 36).toFloat(), 210f)
    canvas.drawRoundRect(headerRect, 28f, 28f, headerPaint)
    // Flatten bottom corners of header
    canvas.drawRect(36f, 150f, (width - 36).toFloat(), 210f, headerPaint)

    // Header Accent Strip
    val accentPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#00A3FF") // Ocean Cyan
    }
    canvas.drawRect(36f, 204f, (width - 36).toFloat(), 212f, accentPaint)

    // Header Text
    val titlePaint = Paint().apply {
      isAntiAlias = true
      color = Color.WHITE
      textSize = 34f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("SAC GARUT", (width / 2).toFloat(), 105f, titlePaint)

    val subtitlePaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#B0D5FF")
      textSize = 19f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      textAlign = Paint.Align.CENTER
      letterSpacing = 0.08f
    }
    canvas.drawText("SWIMMING ACADEMY CENTER • KARTU ABSENSI RESMI", (width / 2).toFloat(), 145f, subtitlePaint)

    val yearBadgePaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#00A3FF")
      textSize = 16f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("TAHUN AJARAN 2026", (width / 2).toFloat(), 180f, yearBadgePaint)

    // Student Info
    var yPos = 270f
    val namePaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#0A2540")
      textSize = 30f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    val displayName = if (student.namaLengkap.length > 28) student.namaLengkap.take(28) + "..." else student.namaLengkap
    canvas.drawText(displayName, (width / 2).toFloat(), yPos, namePaint)

    // Student ID Tag
    yPos += 45f
    val idBadgeBg = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#E5EEF8")
    }
    val idTextPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#006699")
      textSize = 20f
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    val idRect = RectF((width / 2 - 130).toFloat(), yPos - 28f, (width / 2 + 130).toFloat(), yPos + 12f)
    canvas.drawRoundRect(idRect, 10f, 10f, idBadgeBg)
    canvas.drawText(student.id, (width / 2).toFloat(), yPos, idTextPaint)

    // Details Box
    yPos += 50f
    val boxRect = RectF(70f, yPos, (width - 70).toFloat(), yPos + 130f)
    val infoBoxPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#F8FAFC")
    }
    val boxBorderPaint = Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
      strokeWidth = 2f
      color = Color.parseColor("#E2E8F0")
    }
    canvas.drawRoundRect(boxRect, 16f, 16f, infoBoxPaint)
    canvas.drawRoundRect(boxRect, 16f, 16f, boxBorderPaint)

    val labelPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#64748B")
      textSize = 17f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    val valPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#0F172A")
      textSize = 18f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    canvas.drawText("Program:", 90f, yPos + 35f, labelPaint)
    val classText = if (student.kelas.length > 24) student.kelas.take(24) + "..." else student.kelas
    canvas.drawText(classText, 210f, yPos + 35f, valPaint)

    canvas.drawText("Jadwal:", 90f, yPos + 72f, labelPaint)
    val scheduleText = "${student.hariLatihan} (${student.jamLatihan.take(13)})"
    canvas.drawText(scheduleText, 210f, yPos + 72f, valPaint)

    canvas.drawText("Pelatih:", 90f, yPos + 109f, labelPaint)
    canvas.drawText(student.coach, 210f, yPos + 109f, valPaint)

    // QR Code Frame
    yPos += 165f
    val qrSize = 340
    val qrLeft = ((width - qrSize) / 2).toFloat()
    val qrTop = yPos
    val qrFrameRect = RectF(qrLeft - 16f, qrTop - 16f, qrLeft + qrSize + 16f, qrTop + qrSize + 16f)
    val qrBgPaint = Paint().apply {
      isAntiAlias = true
      color = Color.WHITE
    }
    val qrBorderPaint = Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
      strokeWidth = 4f
      color = Color.parseColor("#006699")
    }
    canvas.drawRoundRect(qrFrameRect, 20f, 20f, qrBgPaint)
    canvas.drawRoundRect(qrFrameRect, 20f, 20f, qrBorderPaint)

    // Draw scaled QR Code
    val srcRect = android.graphics.Rect(0, 0, qrBitmap.width, qrBitmap.height)
    val dstRect = RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize)
    canvas.drawBitmap(qrBitmap, srcRect, dstRect, null)

    // QR Instruction Text
    val instrPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#475569")
      textSize = 18f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Tunjukkan barcode ini kepada Coach saat check-in latihan", (width / 2).toFloat(), qrTop + qrSize + 55f, instrPaint)

    // Security Code footer
    val secPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#94A3B8")
      textSize = 14f
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("VERIFIED MEMBER • SAC-GARUT-PRESENSI-2026", (width / 2).toFloat(), (height - 60).toFloat(), secPaint)

    return bitmap
  }

  /**
   * Saves the Card bitmap into local app cache directory and returns the File.
   */
  fun saveQrCardToFile(context: Context, bitmap: Bitmap, studentId: String): File? {
    return try {
      val dir = File(context.cacheDir, "qr_cards").apply { if (!exists()) mkdirs() }
      val cleanId = studentId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
      val file = File(dir, "KARTU_ABSENSI_SAC_${cleanId}.png")
      FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
      }
      file
    } catch (e: Exception) {
      Log.e("QrCodeGenerator", "Error saving QR card to file", e)
      null
    }
  }

  /**
   * Shares the generated card image directly to WhatsApp.
   */
  fun shareQrCardViaWhatsApp(
    context: Context,
    cardFile: File,
    student: Student
  ) {
    val cleanPhone = student.cleanWhatsAppNumber
    val uri: Uri = try {
      FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cardFile)
    } catch (e: Exception) {
      Log.e("QrCodeGenerator", "Error creating FileProvider URI for card", e)
      Toast.makeText(context, "Gagal memproses file kartu QR", Toast.LENGTH_SHORT).show()
      return
    }

    val parentGreeting = if (student.namaIbu.isNotBlank() && student.namaIbu != "-") {
      "Ibu ${student.namaIbu}"
    } else if (student.namaAyah.isNotBlank() && student.namaAyah != "-") {
      "Bpk. ${student.namaAyah}"
    } else if (student.namaOrangTua.isNotBlank()) {
      student.namaOrangTua
    } else {
      "Ayah/Bunda"
    }

    val message = """
      Halo $parentGreeting, kami dari Tim Administrasi *SAC Garut (Swimming Academy Center)* 🏊‍♂️
      
      Berikut kami kirimkan *Kartu Anggota & Barcode Absensi Digital* untuk ananda *${student.namaLengkap}*:
      • ID Siswa: *${student.id}*
      • Program Kelas: ${student.kelas}
      • Jadwal Latihan: ${student.hariLatihan} (${student.jamLatihan})
      • Pelatih: ${student.coach}
      
      Mohon simpan gambar barcode terlampir di galeri HP. Cukup perlihatkan barcode ini ke pelatih saat tiba di kolam untuk pencatatan presensi kehadiran otomatis. Terima kasih! 🙏
    """.trimIndent()

    val packageManager = context.packageManager
    val isW4b = try {
      packageManager.getPackageInfo(WhatsAppHelper.WA_BUSINESS_PACKAGE, 0)
      true
    } catch (e: Exception) { false }

    val isStd = try {
      packageManager.getPackageInfo(WhatsAppHelper.WA_STANDARD_PACKAGE, 0)
      true
    } catch (e: Exception) { false }

    val targetPackage = when {
      isW4b -> WhatsAppHelper.WA_BUSINESS_PACKAGE
      isStd -> WhatsAppHelper.WA_STANDARD_PACKAGE
      else -> null
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "image/png"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_TEXT, message)
      if (cleanPhone.isNotBlank()) {
        putExtra("jid", "$cleanPhone@s.whatsapp.net")
      }
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      if (targetPackage != null) {
        setPackage(targetPackage)
      }
    }

    try {
      context.startActivity(shareIntent)
    } catch (e: Exception) {
      val chooser = Intent.createChooser(shareIntent, "Bagikan Kartu Barcode Siswa").apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    }
  }
}
