package com.example.data.model

enum class AttendanceStatus(val label: String) {
  UNMARKED("Belum Absen"),
  HADIR("Hadir"),
  IZIN("Izin"),
  ALPHA("Alpha")
}

data class Student(
  val id: String = "",
  val timestamp: String = "",
  val namaLengkap: String = "",
  val ttl: String = "",
  val asalSekolah: String = "",
  val lamaLatihan: String = "",
  val tujuan: String = "",
  val kelas: String = "", // Prestasi (> 1 Tahun), Pemula Moderat (6 - 12 Bulan), Pemula (3 - 6 Bulan), Pemula Basic (< 3 Bulan)
  val hariLatihan: String = "Minggu",
  val jamLatihan: String = "08.00 - 09.30 WIB",
  val ukuranJersey: String = "",
  val namaIbu: String = "",
  val namaAyah: String = "",
  val namaOrangTua: String = "",
  val alamat: String = "",
  val noHp: String = "",
  val waNumber: String = "",
  val fotoUrl: String = "",
  val drivePhotoId: String = "",
  val statusPembayaran: String = "Lunas", // Lunas, Belum Lunas, Jatuh Tempo
  val coach: String = "Coach Fahmi", // Lead coach or designated coach
  val pencapaian: String = "",
  val kepuasanPelatih: String = "",
  val penilaianFasilitas: String = "",
  val pendapatTiket: String = "",
  val kritikSaran: String = "",
  val catatanKhusus: String = "",
  // --- Raport Feature Architecture Prep ---
  val gayaBebasTime: String = "",      // Catatan waktu 50m Gaya Bebas (e.g. "00:44.20")
  val gayaDadaTime: String = "",       // Catatan waktu 50m Gaya Dada (e.g. "00:51.10")
  val gayaPunggungTime: String = "",   // Catatan waktu 50m Gaya Punggung (e.g. "00:58.30")
  val gayaKupuTime: String = "",       // Catatan waktu 50m Gaya Kupu-kupu (e.g. "00:56.00")
  val attendanceRate: String = "92%",  // Tingkat persentase kehadiran latihan
  val ratingTeknik: Int = 4,           // Rating teknik berenang (1-5 bintang)
  val ratingDisiplin: Int = 5,         // Rating kedisiplinan & fokus (1-5 bintang)
  val catatanPelatihRaport: String = "", // Catatan evaluasi pelatih untuk rapor
  val rekomendasiLevel: String = "",   // Rekomendasi kenaikan tingkat (e.g. "Siap Naik ke Pemula Moderat")
  val tanggalRaport: String = ""       // Tanggal rapor dicetak / dievaluasi
) {
  val hasRaportMetrics: Boolean
    get() = gayaBebasTime.isNotBlank() || gayaDadaTime.isNotBlank() || catatanPelatihRaport.isNotBlank() || rekomendasiLevel.isNotBlank()

  val initials: String
    get() {
      val parts = namaLengkap.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
      return when {
        parts.isEmpty() -> "SG"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts[1].first()}".uppercase()
      }
    }

  val displayParentName: String
    get() {
      if (namaOrangTua.isNotBlank()) return namaOrangTua
      val parts = mutableListOf<String>()
      if (namaIbu.isNotBlank() && namaIbu != "-") parts.add("Ibu $namaIbu")
      if (namaAyah.isNotBlank() && namaAyah != "-") parts.add("Bpk. $namaAyah")
      return if (parts.isNotEmpty()) parts.joinToString(" & ") else "Orang Tua Siswa"
    }

  val cleanWhatsAppNumber: String
    get() {
      if (waNumber.isNotBlank()) return waNumber
      val digits = noHp.filter { it.isDigit() }
      return when {
        digits.startsWith("0") -> "62" + digits.substring(1)
        digits.startsWith("62") -> digits
        digits.isNotBlank() -> "62$digits"
        else -> ""
      }
    }

  val directPhotoUrl: String?
    get() {
      if (drivePhotoId.isNotBlank()) {
        return "https://lh3.googleusercontent.com/d/$drivePhotoId"
      }
      if (fotoUrl.startsWith("http")) return fotoUrl
      return null
    }

  // Returns all 3 coaches assigned to this student's class level
  fun getAssignedCoaches(classList: List<ClassCategory>): List<String> {
    val matchingClass = classList.firstOrNull {
      it.name.equals(kelas, ignoreCase = true) ||
      it.shortName.equals(kelas, ignoreCase = true) ||
      kelas.contains(it.name, ignoreCase = true) ||
      kelas.contains(it.shortName, ignoreCase = true)
    }
    if (matchingClass != null && matchingClass.assignedCoaches.isNotEmpty()) {
      return matchingClass.assignedCoaches
    }
    return if (coach.isNotBlank()) listOf(coach) else emptyList()
  }
}
