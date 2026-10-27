package com.example.data.model

data class CoachProfile(
  val id: String = "",
  val name: String = "",
  val role: String = "",
  val phone: String = "",
  val bio: String = "",
  val photoUrl: String = ""
) {
  val initials: String
    get() {
      val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
      return when {
        parts.isEmpty() -> "CH"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts[1].first()}".uppercase()
      }
    }
}
