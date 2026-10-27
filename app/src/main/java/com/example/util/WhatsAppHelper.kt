package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.data.model.Student
import java.net.URLEncoder

object WhatsAppHelper {
  const val WA_BUSINESS_PACKAGE = "com.whatsapp.w4b"
  const val WA_STANDARD_PACKAGE = "com.whatsapp"

  /**
   * Sends a WhatsApp message with explicit Intent checks:
   * 1. Detect if WhatsApp Business (com.whatsapp.w4b) is installed first and launch it.
   * 2. Fallback to standard WhatsApp (com.whatsapp) if installed.
   * 3. Fallback to web browser/intent chooser (https://api.whatsapp.com/send?phone=...) if neither is installed.
   */
  fun sendWhatsAppMessage(context: Context, phone: String, message: String) {
    val cleanPhone = when {
      phone.startsWith("0") -> "62" + phone.substring(1)
      phone.startsWith("62") -> phone
      phone.startsWith("+62") -> phone.substring(1)
      phone.isNotBlank() -> "62$phone"
      else -> ""
    }.filter { it.isDigit() }

    if (cleanPhone.isBlank()) {
      Toast.makeText(context, "Nomor WhatsApp belum terdaftar", Toast.LENGTH_SHORT).show()
      return
    }

    try {
      val encoded = URLEncoder.encode(message, "UTF-8")
      val packageManager = context.packageManager

      val isW4bInstalled = isPackageInstalled(packageManager, WA_BUSINESS_PACKAGE)
      val isStandardInstalled = isPackageInstalled(packageManager, WA_STANDARD_PACKAGE)

      val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded")

      val targetPackage = when {
        isW4bInstalled -> WA_BUSINESS_PACKAGE
        isStandardInstalled -> WA_STANDARD_PACKAGE
        else -> null
      }

      val intent = if (targetPackage != null) {
        Intent(Intent.ACTION_VIEW, uri).apply {
          setPackage(targetPackage)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      } else {
        Intent(Intent.ACTION_VIEW, uri).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }

      context.startActivity(intent)
    } catch (e: Exception) {
      // Fallback to generic URL action if direct package launch encountered an error
      try {
        val encoded = URLEncoder.encode(message, "UTF-8")
        val fallbackUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallbackIntent)
      } catch (fallbackError: Exception) {
        Toast.makeText(context, "Gagal membuka WhatsApp: ${fallbackError.localizedMessage}", Toast.LENGTH_LONG).show()
      }
    }
  }

  private fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean {
    return try {
      packageManager.getPackageInfo(packageName, 0)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      false
    } catch (e: Exception) {
      false
    }
  }

  fun sendPaymentReminder(context: Context, student: Student) {
    val cleanPhone = student.cleanWhatsAppNumber
    if (cleanPhone.isBlank()) {
      Toast.makeText(context, "Nomor WhatsApp belum terdaftar untuk siswa ini", Toast.LENGTH_SHORT).show()
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

    val statusNote = if (student.statusPembayaran.equals("Lunas", ignoreCase = true)) {
      "telah tercatat *LUNAS*. Terima kasih banyak atas kerjasamanya! 🙏"
    } else {
      "tercatat *${student.statusPembayaran.uppercase()}*. Mohon konfirmasi atau penyelesaian administrasi agar latihan ananda berjalan lancar. 🙏"
    }

    val message = """
      Halo $parentGreeting, kami dari Administrasi & Keuangan *SAC Garut* 🏊‍♂️
      
      Menginformasikan status pembayaran iuran les renang ananda *${student.namaLengkap}*:
      • Kelas: ${student.kelas}
      • Jadwal: ${student.hariLatihan} (${student.jamLatihan})
      • Status: *${student.statusPembayaran}*
      
      Status administrasi saat ini $statusNote
      
      Informasi lebih lanjut silakan balas pesan ini. Terima kasih!
    """.trimIndent()

    sendWhatsAppMessage(context, cleanPhone, message)
  }

  fun sendStudentInfo(context: Context, student: Student) {
    val cleanPhone = student.cleanWhatsAppNumber
    if (cleanPhone.isBlank()) {
      Toast.makeText(context, "Nomor WhatsApp belum terdaftar untuk siswa ini", Toast.LENGTH_SHORT).show()
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
      Halo $parentGreeting, kami dari Manajemen *SAC Garut (Swimming Academy)* 🏊‍♂️
      
      Menginformasikan data latihan ananda *${student.namaLengkap}*:
      • Kelas: ${student.kelas}
      • Jadwal: ${student.hariLatihan} (${student.jamLatihan})
      • Pelatih: ${student.coach}
      • Status Pembayaran: *${student.statusPembayaran}*
      
      Apabila ada pertanyaan terkait perkembangan renang ananda atau administrasi, silakan hubungi kami kembali. Terima kasih! 🙏
    """.trimIndent()

    sendWhatsAppMessage(context, cleanPhone, message)
  }

  fun shareRaportPdfViaWhatsApp(
    context: Context,
    pdfFile: java.io.File,
    student: Student
  ) {
    val cleanPhone = student.cleanWhatsAppNumber
    val uri = RaportPdfGenerator.getFileProviderUri(context, pdfFile)

    val parentGreeting = if (student.namaIbu.isNotBlank() && student.namaIbu != "-") {
      "Ibu ${student.namaIbu}"
    } else if (student.namaAyah.isNotBlank() && student.namaAyah != "-") {
      "Bpk. ${student.namaAyah}"
    } else if (student.namaOrangTua.isNotBlank()) {
      student.namaOrangTua
    } else {
      "Ayah/Bunda"
    }

    val coverMessage = """
      Halo $parentGreeting, kami dari Tim Kepelatihan *SAC Garut (Swimming Academy)* 🏊‍♂️
      
      Bersama ini kami lampirkan dokumen *Raport Perkembangan Renang Siswa* untuk ananda tercinta *${student.namaLengkap}*:
      • ID Siswa: ${student.id}
      • Level Kelas: ${student.kelas}
      • Pelatih Kepala: ${student.coach}
      • Dokumen: Terlampir resmi dalam format PDF.
      
      Silakan unduh dan periksa capaian waktu serta evaluasi ananda. Terima kasih banyak atas kepercayaan Ayah/Bunda! 🙏
    """.trimIndent()

    val packageManager = context.packageManager
    val isW4bInstalled = isPackageInstalled(packageManager, WA_BUSINESS_PACKAGE)
    val isStandardInstalled = isPackageInstalled(packageManager, WA_STANDARD_PACKAGE)

    val targetPackage = when {
      isW4bInstalled -> WA_BUSINESS_PACKAGE
      isStandardInstalled -> WA_STANDARD_PACKAGE
      else -> null
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_TEXT, coverMessage)
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
      val chooser = Intent.createChooser(shareIntent, "Kirim Raport Renang SAC Garut").apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    }
  }
}
