package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Student
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.StatusLunasBg
import com.example.ui.theme.StatusLunasText
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary

import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.remember
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanIce
import com.example.ui.theme.StatusJatuhTempoBg
import com.example.ui.theme.StatusJatuhTempoBorder
import com.example.ui.theme.StatusJatuhTempoText
import java.util.Calendar

@Composable
fun AttendanceSuccessDialog(
  student: Student,
  checkInTime: String,
  onScanAnother: () -> Unit,
  onDismiss: () -> Unit
) {
  val todayDayName = remember {
    val cal = Calendar.getInstance()
    when (cal.get(Calendar.DAY_OF_WEEK)) {
      Calendar.SUNDAY -> "Minggu"
      Calendar.MONDAY -> "Senin"
      Calendar.TUESDAY -> "Selasa"
      Calendar.WEDNESDAY -> "Selasa" // Shifted from Rabu
      Calendar.THURSDAY -> "Kamis"
      Calendar.FRIDAY -> "Jum'at"
      Calendar.SATURDAY -> "Sabtu"
      else -> ""
    }
  }

  val isDayMatched = remember(student.hariLatihan, todayDayName) {
    if (todayDayName.isBlank()) true
    else {
      val cleanDay = todayDayName.lowercase().replace("'", "")
      val studentDays = student.hariLatihan.lowercase().replace("'", "")
      studentDays.contains(cleanDay)
    }
  }
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("dialog_attendance_success")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Success Green Animated Check Icon
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(StatusLunasBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = StatusLunasText,
            modifier = Modifier.size(38.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Presensi Berhasil Dicatat!",
          fontSize = 18.sp,
          fontWeight = FontWeight.ExtraBold,
          color = TextDarkPrimary,
          textAlign = TextAlign.Center
        )

        Surface(
          color = StatusLunasBg,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.padding(top = 6.dp)
        ) {
          Text(
            text = "STATUS: HADIR",
            color = StatusLunasText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Student Info Summary Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Student Name
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = OceanDeep,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Nama Siswa",
                  fontSize = 10.sp,
                  color = TextDarkSecondary
                )
                Text(
                  text = student.namaLengkap,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextDarkPrimary
                )
                Text(
                  text = "ID: ${student.id}",
                  fontSize = 11.sp,
                  color = OceanDeep,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            // Class Category
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Pool,
                contentDescription = null,
                tint = OceanDeep,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Program / Kategori Kelas",
                  fontSize = 10.sp,
                  color = TextDarkSecondary
                )
                Text(
                  text = student.kelas,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextDarkPrimary
                )
              }
            }

            // Training Schedule & Day Verification
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = OceanDeep,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Jadwal Latihan Rutin",
                  fontSize = 10.sp,
                  color = TextDarkSecondary
                )
                Text(
                  text = "${student.hariLatihan} (${student.jamLatihan})",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextDarkPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isDayMatched) StatusLunasBg else StatusJatuhTempoBg,
                  modifier = Modifier.padding(top = 2.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (isDayMatched) Icons.Default.CheckCircle else Icons.Default.Info,
                      contentDescription = null,
                      tint = if (isDayMatched) StatusLunasText else StatusJatuhTempoText,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isDayMatched) {
                        "Sesuai Jadwal Hari Ini ($todayDayName)"
                      } else {
                        "Perhatian: Latihan Di Luar Jadwal Rutin"
                      },
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDayMatched) StatusLunasText else StatusJatuhTempoText
                    )
                  }
                }
              }
            }

            // Check-in Time
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = OceanDeep,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Waktu Check-In",
                  fontSize = 10.sp,
                  color = TextDarkSecondary
                )
                Text(
                  text = checkInTime,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = StatusLunasText
                )
              }
            }

            // Coach
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Sports,
                contentDescription = null,
                tint = OceanDeep,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Pelatih Bertugas",
                  fontSize = 10.sp,
                  color = TextDarkSecondary
                )
                Text(
                  text = student.coach,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = TextDarkPrimary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Button(
          onClick = onScanAnother,
          colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_scan_another_student")
        ) {
          Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Scan Siswa Lain", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_dismiss_scan_dialog")
        ) {
          Text("Tutup", color = TextDarkSecondary, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
