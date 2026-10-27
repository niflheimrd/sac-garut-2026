package com.example.data.model

data class ClassCategory(
  val id: String = "",
  val name: String = "", // e.g. Prestasi (> 1 Tahun), Pemula Moderat (6 - 12 Bulan), Pemula (3 - 6 Bulan), Pemula Basic (< 3 Bulan)
  val duration: String = "", // e.g. > 1 Tahun, 6 - 12 Bulan, 3 - 6 Bulan, < 3 Bulan
  val description: String = "",
  val targetFocus: String = "",
  val coachUtama: String = "",
  val asistenCoach1: String = "",
  val asistenCoach2: String = ""
) {
  val assignedCoaches: List<String>
    get() = listOf(coachUtama, asistenCoach1, asistenCoach2).filter { it.isNotBlank() }

  val shortName: String
    get() = when {
      name.contains("Prestasi", ignoreCase = true) -> "Prestasi"
      name.contains("Lanjutan", ignoreCase = true) -> "Lanjutan"
      name.contains("Moderat", ignoreCase = true) -> "Pemula Moderat"
      name.contains("Basic", ignoreCase = true) -> "Pemula Basic"
      else -> name
    }
}
