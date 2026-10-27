package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RaportPdfGenerator {

  /**
   * Generates a clean, professional A4 Raport PDF for SAC Garut using Android native PdfDocument and Canvas.
   * Saves to context.cacheDir and returns the FileProvider Uri for instant sharing or viewing.
   */
  fun generateRaportPdf(context: Context, student: Student, attendanceRate: String): File? {
    val pageWidth = 595 // Standard A4 width in PostScript points (72 dpi)
    val pageHeight = 842 // Standard A4 height in points
    val document = PdfDocument()

    try {
      val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
      val page = document.startPage(pageInfo)
      val canvas: Canvas = page.canvas

      val paint = Paint().apply {
        isAntiAlias = true
      }

      // Background color
      paint.color = Color.WHITE
      canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

      // Header Banner (Ocean Deep Navy #0A3663)
      paint.color = Color.rgb(10, 54, 99)
      canvas.drawRect(0f, 0f, pageWidth.toFloat(), 105f, paint)

      // Header Accent line (Cyan Blue #0284C7)
      paint.color = Color.rgb(2, 132, 199)
      canvas.drawRect(0f, 105f, pageWidth.toFloat(), 110f, paint)

      // Header Title & Logo text
      paint.color = Color.WHITE
      paint.textSize = 20f
      paint.isFakeBoldText = true
      canvas.drawText("SAC GARUT - SWIMMING ACADEMY", 36f, 42f, paint)

      paint.textSize = 12f
      paint.isFakeBoldText = false
      paint.color = Color.rgb(224, 242, 254)
      canvas.drawText("Klub & Akademi Renang Prestasi Kabupaten Garut", 36f, 62f, paint)

      paint.textSize = 14f
      paint.isFakeBoldText = true
      paint.color = Color.rgb(255, 237, 74) // Gold
      canvas.drawText("RAPORT EVALUASI & PERKEMBANGAN SISWA", 36f, 88f, paint)

      // Date / Document Stamp right aligned in header
      val currentDateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
      paint.color = Color.WHITE
      paint.textSize = 10f
      paint.isFakeBoldText = false
      canvas.drawText("Tanggal: $currentDateStr", pageWidth - 160f, 42f, paint)
      canvas.drawText("No. Dok: SAC-EV/${student.id}", pageWidth - 160f, 60f, paint)

      var currentY = 135f

      // SECTION 1: BIODATA SISWA CARD
      paint.color = Color.rgb(241, 245, 249) // light slate
      val profileRect = RectF(36f, currentY, pageWidth - 36f, currentY + 115f)
      canvas.drawRoundRect(profileRect, 10f, 10f, paint)

      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 1f
      paint.color = Color.rgb(203, 213, 225)
      canvas.drawRoundRect(profileRect, 10f, 10f, paint)
      paint.style = Paint.Style.FILL

      // Student info text inside card
      paint.textSize = 12f
      paint.isFakeBoldText = true
      paint.color = Color.rgb(10, 54, 99)
      canvas.drawText("INFORMASI SISWA", 50f, currentY + 22f, paint)

      paint.isFakeBoldText = false
      paint.textSize = 10.5f
      paint.color = Color.rgb(51, 65, 85)

      // Left Column
      canvas.drawText("Nama Lengkap : ${student.namaLengkap}", 50f, currentY + 44f, paint)
      canvas.drawText("ID Siswa          : ${student.id}", 50f, currentY + 62f, paint)
      canvas.drawText("Asal Sekolah   : ${student.asalSekolah.ifBlank { "-" }}", 50f, currentY + 80f, paint)
      canvas.drawText("Orang Tua        : ${student.displayParentName}", 50f, currentY + 98f, paint)

      // Right Column
      val rightColX = pageWidth / 2f + 20f
      canvas.drawText("Kelas & Level : ${student.kelas}", rightColX, currentY + 44f, paint)
      canvas.drawText("Jadwal             : ${student.hariLatihan} (${student.jamLatihan})", rightColX, currentY + 62f, paint)
      canvas.drawText("Pelatih/Coach  : ${student.coach}", rightColX, currentY + 80f, paint)
      canvas.drawText("Kehadiran        : $attendanceRate (Aktif)", rightColX, currentY + 98f, paint)

      currentY += 135f

      // SECTION 2: ATTENDANCE & ATTITUDE BANNER
      paint.color = Color.rgb(238, 242, 255)
      val attRect = RectF(36f, currentY, pageWidth - 36f, currentY + 40f)
      canvas.drawRoundRect(attRect, 8f, 8f, paint)

      paint.color = Color.rgb(30, 64, 175)
      paint.textSize = 11f
      paint.isFakeBoldText = true
      canvas.drawText("Tingkat Kehadiran Latihan: $attendanceRate", 50f, currentY + 25f, paint)

      val starTeknikStr = "★".repeat(student.ratingTeknik.coerceIn(1, 5)) + "☆".repeat(5 - student.ratingTeknik.coerceIn(1, 5))
      val starDisiplinStr = "★".repeat(student.ratingDisiplin.coerceIn(1, 5)) + "☆".repeat(5 - student.ratingDisiplin.coerceIn(1, 5))
      canvas.drawText("Teknik: $starTeknikStr (${student.ratingTeknik}/5)  •  Disiplin: $starDisiplinStr (${student.ratingDisiplin}/5)", rightColX - 30f, currentY + 25f, paint)

      currentY += 56f

      // SECTION 3: TIME SHEET (CATATAN WAKTU 50 METER)
      paint.color = Color.rgb(10, 54, 99)
      paint.textSize = 12f
      paint.isFakeBoldText = true
      canvas.drawText("TIME SHEET HASIL EVALUASI TIME TRIAL (50 METER)", 36f, currentY + 12f, paint)

      currentY += 22f

      // Table Header
      paint.color = Color.rgb(14, 116, 144) // Cyan ocean dark
      canvas.drawRect(36f, currentY, pageWidth - 36f, currentY + 24f, paint)

      paint.color = Color.WHITE
      paint.textSize = 10f
      paint.isFakeBoldText = true
      canvas.drawText("NO", 46f, currentY + 16f, paint)
      canvas.drawText("GAYA RENANG (50 METER)", 80f, currentY + 16f, paint)
      canvas.drawText("CATATAN WAKTU", 290f, currentY + 16f, paint)
      canvas.drawText("KATEGORI & STATUS", 420f, currentY + 16f, paint)

      currentY += 24f

      // Table Rows
      val strokeRows = listOf(
        Triple("1. Gaya Bebas (Freestyle)", student.gayaBebasTime.ifBlank { "-" }, if (student.gayaBebasTime.isNotBlank()) "Tercatat" else "Belum Diambil"),
        Triple("2. Gaya Dada (Breaststroke)", student.gayaDadaTime.ifBlank { "-" }, if (student.gayaDadaTime.isNotBlank()) "Tercatat" else "Belum Diambil"),
        Triple("3. Gaya Punggung (Backstroke)", student.gayaPunggungTime.ifBlank { "-" }, if (student.gayaPunggungTime.isNotBlank()) "Tercatat" else "Belum Diambil"),
        Triple("4. Gaya Kupu-kupu (Butterfly)", student.gayaKupuTime.ifBlank { "-" }, if (student.gayaKupuTime.isNotBlank()) "Tercatat" else "Belum Diambil")
      )

      paint.isFakeBoldText = false
      paint.textSize = 10f

      strokeRows.forEachIndexed { index, (stroke, time, status) ->
        val rowBg = if (index % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
        paint.color = rowBg
        canvas.drawRect(36f, currentY, pageWidth - 36f, currentY + 22f, paint)

        // Border line below row
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(36f, currentY + 22f, pageWidth - 36f, currentY + 22f, paint)

        paint.color = Color.rgb(30, 41, 59)
        canvas.drawText("${index + 1}", 48f, currentY + 15f, paint)
        paint.isFakeBoldText = true
        canvas.drawText(stroke, 80f, currentY + 15f, paint)
        paint.isFakeBoldText = false

        paint.color = if (time != "-") Color.rgb(10, 54, 99) else Color.GRAY
        paint.isFakeBoldText = time != "-"
        canvas.drawText(if (time != "-") "$time detik" else "-", 290f, currentY + 15f, paint)
        paint.isFakeBoldText = false

        paint.color = if (status == "Tercatat") Color.rgb(22, 101, 52) else Color.rgb(100, 116, 139)
        canvas.drawText(status, 420f, currentY + 15f, paint)

        currentY += 22f
      }

      currentY += 20f

      // SECTION 4: EVALUASI PELATIH & REKOMENDASI TINGKAT
      paint.color = Color.rgb(10, 54, 99)
      paint.textSize = 12f
      paint.isFakeBoldText = true
      canvas.drawText("CATATAN EVALUASI & REKOMENDASI PELATIH", 36f, currentY + 10f, paint)

      currentY += 20f

      // Notes Box
      val notesBox = RectF(36f, currentY, pageWidth - 36f, currentY + 95f)
      paint.color = Color.rgb(248, 250, 252)
      canvas.drawRoundRect(notesBox, 8f, 8f, paint)
      paint.style = Paint.Style.STROKE
      paint.color = Color.rgb(203, 213, 225)
      canvas.drawRoundRect(notesBox, 8f, 8f, paint)
      paint.style = Paint.Style.FILL

      paint.color = Color.rgb(15, 23, 42)
      paint.textSize = 10f
      paint.isFakeBoldText = false

      val coachNotes = if (student.catatanPelatihRaport.isNotBlank()) {
        student.catatanPelatihRaport
      } else {
        "Ananda menunjukkan antusiasme yang sangat baik, teknik ayunan tangan dan pernapasan stabil. Terus pertahankan kedisiplinan dan latihan rutin."
      }

      // Draw wrapped coach notes (split by 70 chars approx)
      val noteLines = coachNotes.chunked(78)
      var textY = currentY + 22f
      for (line in noteLines.take(3)) {
        canvas.drawText(line, 48f, textY, paint)
        textY += 16f
      }

      // Rekomendasi level highlight banner
      val levelRec = if (student.rekomendasiLevel.isNotBlank()) {
        student.rekomendasiLevel
      } else {
        "Tetap pada level ${student.kelas} dengan pemantapan teknik endurance"
      }
      paint.color = Color.rgb(2, 132, 199)
      paint.isFakeBoldText = true
      canvas.drawText("Rekomendasi Tahapan Selanjutnya: $levelRec", 48f, currentY + 80f, paint)

      currentY += 120f

      // SECTION 5: SIGNATURE & OFFICIAL CLOSING
      paint.isFakeBoldText = false
      paint.color = Color.rgb(51, 65, 85)
      paint.textSize = 10f

      val signatureLeft = 60f
      val signatureRight = pageWidth - 200f

      canvas.drawText("Mengetahui,", signatureLeft, currentY, paint)
      canvas.drawText("Orang Tua / Wali Siswa,", signatureLeft, currentY + 15f, paint)

      canvas.drawText("Garut, $currentDateStr", signatureRight, currentY, paint)
      canvas.drawText("Pelatih Kepala (Head Coach),", signatureRight, currentY + 15f, paint)

      val signLineY = currentY + 68f
      paint.color = Color.rgb(148, 163, 184)
      canvas.drawLine(signatureLeft, signLineY, signatureLeft + 140f, signLineY, paint)
      canvas.drawLine(signatureRight, signLineY, signatureRight + 140f, signLineY, paint)

      paint.color = Color.rgb(15, 23, 42)
      paint.textSize = 10f
      paint.isFakeBoldText = true
      canvas.drawText(student.displayParentName, signatureLeft, signLineY + 14f, paint)
      canvas.drawText(student.coach.ifBlank { "Coach Fahmi" }, signatureRight, signLineY + 14f, paint)

      paint.isFakeBoldText = false
      paint.textSize = 9f
      paint.color = Color.rgb(100, 116, 139)
      canvas.drawText("Tanda Tangan & Nama Terang", signatureLeft, signLineY + 26f, paint)
      canvas.drawText("SAC Garut Academy", signatureRight, signLineY + 26f, paint)

      // Footer brand ribbon
      paint.color = Color.rgb(10, 54, 99)
      canvas.drawRect(0f, pageHeight - 24f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

      paint.color = Color.WHITE
      paint.textSize = 9f
      canvas.drawText("Dokumen Resmi Hasil Evaluasi Renang SAC Garut • www.sacgarut.id", 36f, pageHeight - 9f, paint)

      document.finishPage(page)

      // Save PDF file in cacheDir
      val reportsDir = File(context.cacheDir, "reports")
      if (!reportsDir.exists()) {
        reportsDir.mkdirs()
      }
      val cleanFileName = "Raport_${student.namaLengkap.replace("\\s+".toRegex(), "_")}_${student.id}.pdf"
      val pdfFile = File(reportsDir, cleanFileName)
      val outputStream = FileOutputStream(pdfFile)
      document.writeTo(outputStream)
      outputStream.flush()
      outputStream.close()

      Log.d("RaportPdfGenerator", "PDF generated successfully at: ${pdfFile.absolutePath}")
      return pdfFile
    } catch (e: Exception) {
      Log.e("RaportPdfGenerator", "Error generating Raport PDF", e)
      return null
    } finally {
      document.close()
    }
  }

  fun getFileProviderUri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )
  }
}
