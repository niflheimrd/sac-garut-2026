package com.example.ui.components

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanIce
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.TextDarkTertiary
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.StudentViewModel
import com.example.util.RaportPdfGenerator
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRaportSheet(
  student: Student,
  viewModel: StudentViewModel,
  onDismiss: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Auto-calculated attendance rate from attendance logs
  val calculatedAttendance = remember(student.id) {
    viewModel.calculateAttendanceRate(student.id)
  }

  // Time trial records (seconds)
  var gayaBebas by remember { mutableStateOf(student.gayaBebasTime) }
  var gayaDada by remember { mutableStateOf(student.gayaDadaTime) }
  var gayaPunggung by remember { mutableStateOf(student.gayaPunggungTime) }
  var gayaKupu by remember { mutableStateOf(student.gayaKupuTime) }

  // Star Ratings (1-5)
  var ratingTeknik by remember { mutableIntStateOf(if (student.ratingTeknik in 1..5) student.ratingTeknik else 4) }
  var ratingDisiplin by remember { mutableIntStateOf(if (student.ratingDisiplin in 1..5) student.ratingDisiplin else 5) }

  // Evaluation notes & level recommendation
  var catatanPelatih by remember {
    mutableStateOf(
      if (student.catatanPelatihRaport.isNotBlank()) student.catatanPelatihRaport
      else "Ananda menunjukkan semangat berlatih tinggi. Teknik pernapasan dan luncuran semakin baik. Latihan kontinuitas sangat disarankan."
    )
  }
  var rekomendasiLevel by remember {
    mutableStateOf(
      if (student.rekomendasiLevel.isNotBlank()) student.rekomendasiLevel
      else "Siap melanjutkan ke jenjang ${student.kelas} tingkat lanjut"
    )
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = BackgroundLight,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = modifier.testTag("student_raport_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 36.dp)
    ) {
      // Top Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(OceanIce),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Assessment,
              contentDescription = null,
              tint = OceanBlue,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Raport Perkembangan Siswa",
              style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
            Text(
              text = "Evaluasi Teknik, Waktu Renang, & PDF Sharing",
              fontSize = 11.sp,
              color = TextDarkSecondary
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("btn_close_raport")
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextDarkSecondary)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Student Summary Card with Auto-Calculated Attendance
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          StudentAvatar(student = student, size = 52.dp)
          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = student.namaLengkap,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = TextDarkPrimary
            )
            Text(
              text = "${student.kelas} • Pelatih: ${student.coach}",
              fontSize = 12.sp,
              color = OceanAzure
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = Color(0xFFE6F7EE),
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF0E8A42),
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Kehadiran Otomatis: $calculatedAttendance",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0E8A42)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Section 1: Time Sheet Inputs (50m Time Trials)
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "TIME SHEET EVALUASI (50 METER)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OceanDeep,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "Catat rekor waktu dalam detik (e.g. 44.20 atau 00:44.20)",
            fontSize = 11.sp,
            color = TextDarkSecondary
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = gayaBebas,
              onValueChange = { gayaBebas = it },
              label = { Text("Gaya Bebas") },
              placeholder = { Text("44.20") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_time_bebas"),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = gayaDada,
              onValueChange = { gayaDada = it },
              label = { Text("Gaya Dada") },
              placeholder = { Text("51.10") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_time_dada"),
              shape = RoundedCornerShape(10.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = gayaPunggung,
              onValueChange = { gayaPunggung = it },
              label = { Text("Gaya Punggung") },
              placeholder = { Text("58.30") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_time_punggung"),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = gayaKupu,
              onValueChange = { gayaKupu = it },
              label = { Text("Gaya Kupu-kupu") },
              placeholder = { Text("56.00") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_time_kupu"),
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Section 2: Star Ratings (Teknik & Kedisiplinan)
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "PENILAIAN ASPEK BERENANG",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OceanDeep,
            letterSpacing = 0.8.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Technique Rating
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Penguasaan Teknik", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDarkPrimary)
              Text(text = "Gerakan tangan, kaki, & napas", fontSize = 11.sp, color = TextDarkSecondary)
            }
            StarRatingSelector(
              rating = ratingTeknik,
              onRatingSelected = { ratingTeknik = it }
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = Color(0xFFF1F5F9))
          Spacer(modifier = Modifier.height(12.dp))

          // Discipline Rating
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Disiplin & Fokus", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDarkPrimary)
              Text(text = "Kepatuhan instruksi pelatih", fontSize = 11.sp, color = TextDarkSecondary)
            }
            StarRatingSelector(
              rating = ratingDisiplin,
              onRatingSelected = { ratingDisiplin = it }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Section 3: Coach Notes & Level Recommendation
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "CATATAN EVALUASI & REKOMENDASI TINGKAT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OceanDeep,
            letterSpacing = 0.8.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = catatanPelatih,
            onValueChange = { catatanPelatih = it },
            label = { Text("Catatan Pelatih Kepala") },
            placeholder = { Text("Tuliskan evaluasi perkembangan gaya renang...") },
            modifier = Modifier.fillMaxWidth().testTag("input_coach_raport_notes"),
            shape = RoundedCornerShape(10.dp),
            maxLines = 4
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = rekomendasiLevel,
            onValueChange = { rekomendasiLevel = it },
            label = { Text("Rekomendasi Level / Kenaikan") },
            placeholder = { Text("e.g. Siap Lanjut ke Pemula Moderat") },
            modifier = Modifier.fillMaxWidth().testTag("input_recommendation_level"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Action 1: Save Data Button
      Button(
        onClick = {
          viewModel.saveRaportData(
            studentId = student.id,
            gayaBebas = gayaBebas,
            gayaDada = gayaDada,
            gayaPunggung = gayaPunggung,
            gayaKupu = gayaKupu,
            ratingTeknik = ratingTeknik,
            ratingDisiplin = ratingDisiplin,
            catatanPelatih = catatanPelatih,
            rekomendasiLevel = rekomendasiLevel,
            attendanceRate = calculatedAttendance
          )
          Toast.makeText(context, "Data Raport berhasil disimpan!", Toast.LENGTH_SHORT).show()
        },
        colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("btn_save_raport_data")
      ) {
        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Simpan Nilai Raport", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action 2: Direct WhatsApp PDF Share Button (Requirement 4)
      Button(
        onClick = {
          // First save current inputs
          viewModel.saveRaportData(
            studentId = student.id,
            gayaBebas = gayaBebas,
            gayaDada = gayaDada,
            gayaPunggung = gayaPunggung,
            gayaKupu = gayaKupu,
            ratingTeknik = ratingTeknik,
            ratingDisiplin = ratingDisiplin,
            catatanPelatih = catatanPelatih,
            rekomendasiLevel = rekomendasiLevel,
            attendanceRate = calculatedAttendance
          )

          val updatedStudent = student.copy(
            gayaBebasTime = gayaBebas.trim(),
            gayaDadaTime = gayaDada.trim(),
            gayaPunggungTime = gayaPunggung.trim(),
            gayaKupuTime = gayaKupu.trim(),
            ratingTeknik = ratingTeknik,
            ratingDisiplin = ratingDisiplin,
            catatanPelatihRaport = catatanPelatih.trim(),
            rekomendasiLevel = rekomendasiLevel.trim(),
            attendanceRate = calculatedAttendance
          )

          val pdfFile = RaportPdfGenerator.generateRaportPdf(context, updatedStudent, calculatedAttendance)
          if (pdfFile != null && pdfFile.exists()) {
            WhatsAppHelper.shareRaportPdfViaWhatsApp(context, pdfFile, updatedStudent)
          } else {
            Toast.makeText(context, "Gagal membuat dokumen PDF", Toast.LENGTH_SHORT).show()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("btn_share_raport_whatsapp")
      ) {
        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Kirim Raport via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action 3: Generate & View PDF locally
      OutlinedButton(
        onClick = {
          val updatedStudent = student.copy(
            gayaBebasTime = gayaBebas.trim(),
            gayaDadaTime = gayaDada.trim(),
            gayaPunggungTime = gayaPunggung.trim(),
            gayaKupuTime = gayaKupu.trim(),
            ratingTeknik = ratingTeknik,
            ratingDisiplin = ratingDisiplin,
            catatanPelatihRaport = catatanPelatih.trim(),
            rekomendasiLevel = rekomendasiLevel.trim(),
            attendanceRate = calculatedAttendance
          )
          val pdfFile = RaportPdfGenerator.generateRaportPdf(context, updatedStudent, calculatedAttendance)
          if (pdfFile != null && pdfFile.exists()) {
            try {
              val uri = RaportPdfGenerator.getFileProviderUri(context, pdfFile)
              val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              }
              context.startActivity(viewIntent)
            } catch (e: Exception) {
              Toast.makeText(context, "Raport tersimpan di cache: ${pdfFile.name}", Toast.LENGTH_LONG).show()
            }
          }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("btn_view_raport_pdf")
      ) {
        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Pratinjau / Ekspor PDF", fontWeight = FontWeight.SemiBold, color = OceanBlue, fontSize = 13.sp)
      }
    }
  }
}

@Composable
fun StarRatingSelector(
  rating: Int,
  onRatingSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 1..5) {
      val isFilled = i <= rating
      Icon(
        imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarBorder,
        contentDescription = "Bintang $i",
        tint = if (isFilled) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .clickable { onRatingSelected(i) }
      )
    }
  }
}
