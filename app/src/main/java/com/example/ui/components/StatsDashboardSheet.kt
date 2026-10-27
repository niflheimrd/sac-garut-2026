package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.StatusBelumLunasText
import com.example.ui.theme.StatusJatuhTempoText
import com.example.ui.theme.StatusLunasText
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.TextDarkTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDashboardSheet(
  students: List<Student>,
  classes: List<com.example.data.model.ClassCategory> = emptyList(),
  coaches: List<com.example.data.model.CoachProfile> = emptyList(),
  parentSurveys: List<com.example.data.model.SurveyModel> = emptyList(),
  onDismiss: () -> Unit,
  onResetData: () -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier
) {
  val total = students.size
  val lunas = students.count { it.statusPembayaran.equals("Lunas", ignoreCase = true) }
  val belumLunas = students.count { it.statusPembayaran.equals("Belum Lunas", ignoreCase = true) }
  val jatuhTempo = students.count { it.statusPembayaran.equals("Jatuh Tempo", ignoreCase = true) }

  val lunasRatio = if (total > 0) lunas.toFloat() / total else 0f
  val belumLunasRatio = if (total > 0) belumLunas.toFloat() / total else 0f
  val jatuhTempoRatio = if (total > 0) jatuhTempo.toFloat() / total else 0f

  val classList = classes.map { it.name }.ifEmpty {
    listOf("Prestasi (> 1 Tahun)", "Pemula Moderat (6 - 12 Bulan)", "Pemula (3 - 6 Bulan)", "Pemula Basic (< 3 Bulan)")
  }
  val coachList = coaches.map { it.name }.ifEmpty {
    listOf("Coach Fahmi", "Coach Gilang", "Coach Dani", "Coach Asep", "Coach Sarah", "Coach Bayu")
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = BackgroundLight,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = modifier.testTag("stats_dashboard_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 2.dp
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.logo_sac_garut),
                contentDescription = "Logo SAC Garut",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Ringkasan Data SAC Garut",
              style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
            Text(
              text = "Swimming Academy Center 2026",
              fontSize = 11.sp,
              color = TextDarkSecondary
            )
          }
        }
        IconButton(onClick = onDismiss) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextDarkSecondary)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Overview Metrics Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricCard(
          title = "Total Siswa",
          count = "$total",
          color = OceanDeep,
          bgColor = Color(0xFFE5EEF8),
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = "Lunas",
          count = "$lunas",
          color = StatusLunasText,
          bgColor = Color(0xFFE6F7EE),
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = "Belum Lunas",
          count = "$belumLunas",
          color = StatusBelumLunasText,
          bgColor = Color(0xFFFDE8E8),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Financial Status Progress Bar Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "PROPORSI KEUANGAN & PEMBAYARAN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkTertiary,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          ProgressItem(
            label = "Lunas",
            count = lunas,
            ratio = lunasRatio,
            color = StatusLunasText
          )
          Spacer(modifier = Modifier.height(8.dp))
          ProgressItem(
            label = "Belum Lunas",
            count = belumLunas,
            ratio = belumLunasRatio,
            color = StatusBelumLunasText
          )
          Spacer(modifier = Modifier.height(8.dp))
          ProgressItem(
            label = "Jatuh Tempo",
            count = jatuhTempo,
            ratio = jatuhTempoRatio,
            color = StatusJatuhTempoText
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Distribution per Class
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "DISTRIBUSI KELAS RENANG",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkTertiary,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(10.dp))

          classList.forEach { cls ->
            val count = students.count { it.kelas.equals(cls, ignoreCase = true) }
            val ratio = if (total > 0) count.toFloat() / total else 0f
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = cls,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextDarkPrimary
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "$count siswa",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = OceanBlue
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "(${(ratio * 100).toInt()}%)",
                  fontSize = 11.sp,
                  color = TextDarkTertiary
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Distribution per Coach
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "DISTRIBUSI PELATIH / COACH",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkTertiary,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(10.dp))

          coachList.forEach { coach ->
            val count = students.count { it.coach.equals(coach, ignoreCase = true) }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = coach,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextDarkPrimary
              )
              Text(
                text = "$count siswa aktif",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OceanDeep
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Respon Survei Orang Tua (Firestore survei_orangtua)
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(androidx.compose.foundation.shape.CircleShape)
                  .background(Color(0xFF10B981))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Survei & Masukan Orang Tua",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFE2ECF4)
            ) {
              Text(
                text = "${parentSurveys.size} Respon",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OceanDeep,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (parentSurveys.isEmpty()) {
            Text(
              text = "Belum ada respon masuk dari survei_orangtua. Form otomatis tersinkronisasi saat orang tua mengisi evaluasi.",
              fontSize = 12.sp,
              color = TextDarkTertiary,
              lineHeight = 17.sp
            )
          } else {
            val totalSurveys = parentSurveys.size
            val sesuaiCount = parentSurveys.count { 
              it.pencapaian.contains("Sesuai", ignoreCase = true) || it.pencapaian.contains("Melampaui", ignoreCase = true) 
            }
            val belumSesuaiCount = totalSurveys - sesuaiCount
            val sesuaiRatio = if (totalSurveys > 0) sesuaiCount.toFloat() / totalSurveys else 0f

            val tiketSesuaiCount = parentSurveys.count { 
              it.pendapatTiket.contains("Tidak memberatkan", ignoreCase = true) || it.pendapatTiket.contains("Sesuai", ignoreCase = true) 
            }
            val tiketKhususCount = totalSurveys - tiketSesuaiCount
            val tiketRatio = if (totalSurveys > 0) tiketSesuaiCount.toFloat() / totalSurveys else 0f

            // Summary Metric Chips
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(
                    text = "SESUAI HARAPAN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                  )
                  Text(
                    text = "${((sesuaiRatio) * 100).toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B5E20)
                  )
                  Text(
                    text = "$sesuaiCount dari $totalSurveys siswa",
                    fontSize = 10.sp,
                    color = Color(0xFF388E3C)
                  )
                }
              }

              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(
                    text = "TIKET TIDAK MEMBERATKAN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0369A1)
                  )
                  Text(
                    text = "${((tiketRatio) * 100).toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF075985)
                  )
                  Text(
                    text = "$tiketSesuaiCount responden puas",
                    fontSize = 10.sp,
                    color = Color(0xFF0284C7)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Proportions
            ProgressItem(
              label = "Pencapaian Sesuai / Melampaui Harapan",
              count = sesuaiCount,
              ratio = sesuaiRatio,
              color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(6.dp))
            ProgressItem(
              label = "Pencapaian Perlu Peningkatan",
              count = belumSesuaiCount,
              ratio = if (totalSurveys > 0) belumSesuaiCount.toFloat() / totalSurveys else 0f,
              color = Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            ProgressItem(
              label = "Tiket Sesuai Fasilitas Kolam",
              count = tiketSesuaiCount,
              ratio = tiketRatio,
              color = Color(0xFF0EA5E9)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "RESPON & SARAN TERBARU:",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = TextDarkTertiary,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              parentSurveys.take(5).forEach { survey ->
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp)
                ) {
                  Column {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = if (survey.namaSiswa.isNotBlank()) survey.namaSiswa else "Siswa SAC",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanDeep
                      )
                      if (survey.kepuasanPelatih.isNotBlank()) {
                        Text(
                          text = "Kepuasan: ${survey.kepuasanPelatih}",
                          fontSize = 11.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = Color(0xFFD97706)
                        )
                      }
                    }
                    if (survey.namaOrangTua.isNotBlank()) {
                      Text(
                        text = "Wali: ${survey.namaOrangTua}",
                        fontSize = 11.sp,
                        color = TextDarkSecondary
                      )
                    }
                    if (survey.saranKritik.isNotBlank()) {
                      Spacer(modifier = Modifier.height(3.dp))
                      Text(
                        text = "\"${survey.saranKritik}\"",
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = TextDarkPrimary
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Reset Data Button
      OutlinedButton(
        onClick = {
          onResetData()
          onDismiss()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = null,
          tint = TextDarkSecondary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Reset Data ke Google Sheets Master",
          color = TextDarkSecondary,
          fontSize = 13.sp
        )
      }
    }
  }
}

@Composable
fun MetricCard(
  title: String,
  count: String,
  color: Color,
  bgColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(bgColor)
      .padding(12.dp)
  ) {
    Column {
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = TextDarkSecondary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = count,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color
      )
    }
  }
}

@Composable
fun ProgressItem(
  label: String,
  count: Int,
  ratio: Float,
  color: Color,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextDarkPrimary
      )
      Text(
        text = "$count (${(ratio * 100).toInt()}%)",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { ratio },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = color,
      trackColor = Color(0xFFF1F5F9),
    )
  }
}
