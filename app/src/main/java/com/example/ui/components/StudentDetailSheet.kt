package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanIce
import com.example.ui.theme.StatusBelumLunasText
import com.example.ui.theme.StatusJatuhTempoText
import com.example.ui.theme.StatusLunasText
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.TextDarkTertiary
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppGreenDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailSheet(
  student: Student,
  onDismiss: () -> Unit,
  onEditClick: () -> Unit,
  onPaymentStatusChange: (String) -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
  classes: List<com.example.data.model.ClassCategory> = emptyList(),
  onAvatarClick: ((Student) -> Unit)? = null,
  onOpenRaport: (() -> Unit)? = null,
  onShowQrCard: ((Student) -> Unit)? = null
) {
  val context = LocalContext.current

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = BackgroundLight,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = modifier.testTag("student_detail_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      // Top close and edit buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Detail Data Siswa",
          style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = OceanDeep
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEditClick,
            modifier = Modifier.testTag("btn_edit_student")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Data Siswa",
              tint = OceanBlue
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("btn_close_sheet")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Tutup",
              tint = TextDarkSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Student Header Profile Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            StudentAvatar(
              student = student,
              size = 64.dp,
              onClick = if (onAvatarClick != null) { { onAvatarClick(student) } } else null
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = student.namaLengkap,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "ID: ${student.id} • Jersei: ${student.ukuranJersey.ifBlank { "Standar" }}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OceanAzure
              )
              if (student.asalSekolah.isNotBlank() && student.asalSekolah != "-") {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = TextDarkTertiary,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = student.asalSekolah,
                    fontSize = 12.sp,
                    color = TextDarkSecondary
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = Color(0xFFE2EAF1))
          Spacer(modifier = Modifier.height(14.dp))

          // Status Pembayaran Interactive Section
          Text(
            text = "STATUS PEMBAYARAN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkTertiary,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            StatusToggleButton(
              label = "Lunas",
              isSelected = student.statusPembayaran.equals("Lunas", ignoreCase = true),
              selectedColor = StatusLunasText,
              selectedBg = Color(0xFFE6F7EE),
              icon = Icons.Default.CheckCircle,
              onClick = { onPaymentStatusChange("Lunas") },
              modifier = Modifier.weight(1f)
            )

            StatusToggleButton(
              label = "Belum Lunas",
              isSelected = student.statusPembayaran.equals("Belum Lunas", ignoreCase = true),
              selectedColor = StatusBelumLunasText,
              selectedBg = Color(0xFFFDE8E8),
              icon = Icons.Default.ErrorOutline,
              onClick = { onPaymentStatusChange("Belum Lunas") },
              modifier = Modifier.weight(1f)
            )

            StatusToggleButton(
              label = "Jatuh Tempo",
              isSelected = student.statusPembayaran.equals("Jatuh Tempo", ignoreCase = true),
              selectedColor = StatusJatuhTempoText,
              selectedBg = Color(0xFFFEF3C7),
              icon = Icons.Default.HourglassBottom,
              onClick = { onPaymentStatusChange("Jatuh Tempo") },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // WhatsApp Quick-Action Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "HUBUNGI ORANG TUA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkTertiary,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = student.displayParentName,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDarkPrimary
          )
          Text(
            text = "No. HP: ${student.noHp.ifBlank { "Belum dicantumkan" }}",
            fontSize = 13.sp,
            color = TextDarkSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Big WhatsApp Action Button
          Button(
            onClick = { openWhatsApp(context, student) },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_detail_whatsapp"),
            colors = ButtonDefaults.buttonColors(
              containerColor = WhatsAppGreen,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Chat WhatsApp Orang Tua",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          if (student.noHp.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = {
                try {
                  val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.noHp}"))
                  context.startActivity(dialIntent)
                } catch (e: Exception) {
                  Toast.makeText(context, "Tidak dapat membuka panggilan", Toast.LENGTH_SHORT).show()
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = OceanBlue,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Telepon Langsung (${student.noHp})",
                color = OceanBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Jadwal Latihan & Pelatih Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "JADWAL & KELAS LATIHAN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkTertiary,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          DetailRow(
            icon = Icons.Default.CalendarMonth,
            label = "Hari Latihan",
            value = student.hariLatihan
          )
          DetailRow(
            icon = Icons.Default.AccessTime,
            label = "Jam Latihan",
            value = student.jamLatihan
          )
          val assignedCoaches = student.getAssignedCoaches(classes)
          val matchingClass = classes.firstOrNull { it.name.equals(student.kelas, ignoreCase = true) }

          DetailRow(
            icon = Icons.Default.Sports,
            label = "Level & Program Kelas",
            value = student.kelas
          )
          DetailRow(
            icon = Icons.Default.Person,
            label = "Tim Pelatih Kelas (3 Coach)",
            value = assignedCoaches.joinToString(" • ")
          )
          if (matchingClass != null) {
            DetailRow(
              icon = Icons.Default.Person,
              label = "Susunan Pelatih",
              value = "1. Utama: ${matchingClass.coachUtama}\n2. Asisten 1: ${matchingClass.asistenCoach1}\n3. Asisten 2: ${matchingClass.asistenCoach2}"
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Biodata & Informasi Tambahan Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "BIODATA & REGISTRASI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkTertiary,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          if (student.ttl.isNotBlank()) {
            DetailRow(icon = Icons.Default.CalendarMonth, label = "Tempat, Tgl Lahir", value = student.ttl)
          }
          if (student.alamat.isNotBlank()) {
            DetailRow(icon = Icons.Default.LocationOn, label = "Alamat", value = student.alamat)
          }
          if (student.lamaLatihan.isNotBlank()) {
            DetailRow(icon = Icons.Default.HourglassBottom, label = "Lama di SAC", value = student.lamaLatihan)
          }
          if (student.tujuan.isNotBlank()) {
            DetailRow(icon = Icons.Default.Star, label = "Tujuan Mengikuti", value = student.tujuan)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Raport Feature Architecture Prep Card
      Card(
        modifier = Modifier.fillMaxWidth().testTag("card_raport_prep"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "METRIK RAPORT SISWA (PREP)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep,
              letterSpacing = 0.8.sp
            )
            Surface(
              color = OceanIce,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "Kehadiran: ${student.attendanceRate.ifBlank { "92%" }}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = OceanBlue,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 4 Stroke Times Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            RaportStrokeBox(label = "Bebas 50m", time = student.gayaBebasTime.ifBlank { "-" }, modifier = Modifier.weight(1f))
            RaportStrokeBox(label = "Dada 50m", time = student.gayaDadaTime.ifBlank { "-" }, modifier = Modifier.weight(1f))
            RaportStrokeBox(label = "Punggung", time = student.gayaPunggungTime.ifBlank { "-" }, modifier = Modifier.weight(1f))
            RaportStrokeBox(label = "Kupu-kupu", time = student.gayaKupuTime.ifBlank { "-" }, modifier = Modifier.weight(1f))
          }

          if (student.catatanPelatihRaport.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            FeedbackItem(label = "Catatan Pelatih untuk Raport:", text = student.catatanPelatihRaport)
          }

          if (student.rekomendasiLevel.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            FeedbackItem(label = "Rekomendasi Kenaikan Tingkat:", text = student.rekomendasiLevel, isHighlight = true)
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = { onOpenRaport?.invoke() },
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_open_student_raport")
          ) {
            Icon(
              imageVector = Icons.Default.Assessment,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka & Kelola Raport Siswa (PDF)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Kartu Anggota & Barcode Digital
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "KARTU ANGGOTA & BARCODE ABSENSI",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextDarkTertiary,
              letterSpacing = 0.8.sp
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = OceanIce
            ) {
              Text(
                text = "ID: ${student.id}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = OceanDeep,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Kartu digital resmi berisi QR code unik untuk scan presensi otomatis oleh pelatih.",
            fontSize = 12.sp,
            color = TextDarkSecondary
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = { onShowQrCard?.invoke(student) },
            colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_view_student_qr_card")
          ) {
            Icon(
              imageVector = Icons.Default.QrCode,
              contentDescription = null,
              tint = OceanCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Lihat & Bagikan Kartu Barcode Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Evaluasi & Feedback Card (if any present)
      if (student.pencapaian.isNotBlank() || student.kepuasanPelatih.isNotBlank() || student.kritikSaran.isNotBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Text(
              text = "EVALUASI & FEEDBACK ORANG TUA",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextDarkTertiary,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (student.pencapaian.isNotBlank()) {
              FeedbackItem(label = "Capaian / Perkembangan:", text = student.pencapaian)
            }
            if (student.kepuasanPelatih.isNotBlank()) {
              FeedbackItem(label = "Kepuasan Pelatih:", text = student.kepuasanPelatih)
            }
            if (student.penilaianFasilitas.isNotBlank()) {
              FeedbackItem(label = "Penilaian Fasilitas:", text = student.penilaianFasilitas)
            }
            if (student.kritikSaran.isNotBlank() && student.kritikSaran != "-") {
              FeedbackItem(label = "Kritik & Harapan:", text = student.kritikSaran, isHighlight = true)
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatusToggleButton(
  label: String,
  isSelected: Boolean,
  selectedColor: Color,
  selectedBg: Color,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(42.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(if (isSelected) selectedBg else Color(0xFFF1F5F9))
      .border(
        width = if (isSelected) 1.5.dp else 1.dp,
        color = if (isSelected) selectedColor else Color(0xFFE2EAF1),
        shape = RoundedCornerShape(10.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) selectedColor else TextDarkTertiary,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) selectedColor else TextDarkSecondary
      )
    }
  }
}

@Composable
fun DetailRow(
  icon: ImageVector,
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.Top
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = OceanAzure,
      modifier = Modifier
        .size(18.dp)
        .padding(top = 2.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        fontSize = 11.sp,
        color = TextDarkTertiary
      )
      Text(
        text = value,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDarkPrimary
      )
    }
  }
}

@Composable
fun FeedbackItem(
  label: String,
  text: String,
  isHighlight: Boolean = false,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (isHighlight) OceanBlue else TextDarkTertiary
    )
    Spacer(modifier = Modifier.height(2.dp))
    Surface(
      color = if (isHighlight) OceanIce else Color(0xFFF8FAFC),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = text,
        fontSize = 12.sp,
        color = TextDarkPrimary,
        modifier = Modifier.padding(10.dp)
      )
    }
  }
}

@Composable
fun RaportStrokeBox(
  label: String,
  time: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFFF1F5F9))
      .padding(vertical = 6.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDarkTertiary,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = time,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = OceanDeep,
        maxLines = 1
      )
    }
  }
}
