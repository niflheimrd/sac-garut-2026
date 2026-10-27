package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassCategory
import com.example.data.model.CoachProfile
import com.example.data.model.Student
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.TextDarkTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStudentDialog(
  student: Student,
  availableClasses: List<ClassCategory>,
  availableCoaches: List<CoachProfile>,
  onDismiss: () -> Unit,
  onSave: (Student) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedStatus by remember { mutableStateOf(student.statusPembayaran) }
  var selectedClass by remember { mutableStateOf(student.kelas) }
  var selectedCoach by remember { mutableStateOf(student.coach) }
  var hariLatihan by remember { mutableStateOf(student.hariLatihan) }
  var jamLatihan by remember { mutableStateOf(student.jamLatihan) }
  var catatanKhusus by remember { mutableStateOf(student.catatanKhusus) }

  // Dynamically populated from management settings
  val classOptions = availableClasses.map { it.name }.ifEmpty {
    listOf(
      "Prestasi (> 1 Tahun)",
      "Pemula Moderat (6 - 12 Bulan)",
      "Pemula (3 - 6 Bulan)",
      "Pemula Basic (< 3 Bulan)"
    )
  }
  val coachOptions = availableCoaches.map { it.name }.ifEmpty {
    listOf("Coach Fahmi", "Coach Gilang", "Coach Dani", "Coach Asep", "Coach Sarah", "Coach Bayu")
  }
  val statusOptions = listOf("Lunas", "Belum Lunas", "Jatuh Tempo")

  var classExpanded by remember { mutableStateOf(false) }
  var coachExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier.testTag("dialog_edit_student"),
    containerColor = SurfaceWhite,
    shape = RoundedCornerShape(20.dp),
    title = {
      Column {
        Text(
          text = "Edit Data Siswa",
          style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = OceanDeep
        )
        Text(
          text = student.namaLengkap,
          fontSize = 13.sp,
          color = TextDarkSecondary
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp)
      ) {
        // Status Pembayaran Radios
        Text(
          text = "Status Pembayaran",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = OceanDeep
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          statusOptions.forEach { status ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { selectedStatus = status }
            ) {
              RadioButton(
                selected = selectedStatus == status,
                onClick = { selectedStatus = status },
                colors = RadioButtonDefaults.colors(selectedColor = OceanBlue)
              )
              Text(
                text = status,
                fontSize = 12.sp,
                color = TextDarkPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Kelas Dropdown
        Text(
          text = "Level & Kategori Kelas",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = OceanDeep
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
          expanded = classExpanded,
          onExpandedChange = { classExpanded = it }
        ) {
          OutlinedTextField(
            value = selectedClass,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
            modifier = Modifier
              .menuAnchor(MenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = classExpanded,
            onDismissRequest = { classExpanded = false }
          ) {
            classOptions.forEach { cls ->
              DropdownMenuItem(
                text = { Text(cls, fontWeight = if (cls == selectedClass) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                  selectedClass = cls
                  classExpanded = false
                  // Auto-suggest coach utama of that class if available
                  availableClasses.find { it.name == cls }?.let { cat ->
                    if (cat.coachUtama.isNotBlank()) selectedCoach = cat.coachUtama
                  }
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Coach Dropdown
        Text(
          text = "Pelatih / Lead Coach",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = OceanDeep
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
          expanded = coachExpanded,
          onExpandedChange = { coachExpanded = it }
        ) {
          OutlinedTextField(
            value = selectedCoach,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = coachExpanded) },
            modifier = Modifier
              .menuAnchor(MenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = coachExpanded,
            onDismissRequest = { coachExpanded = false }
          ) {
            coachOptions.forEach { cch ->
              DropdownMenuItem(
                text = { Text(cch, fontWeight = if (cch == selectedCoach) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                  selectedCoach = cch
                  coachExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hari Latihan
        OutlinedTextField(
          value = hariLatihan,
          onValueChange = { hariLatihan = it },
          label = { Text("Hari Latihan") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Jam Latihan
        OutlinedTextField(
          value = jamLatihan,
          onValueChange = { jamLatihan = it },
          label = { Text("Jam Latihan") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Catatan Khusus
        OutlinedTextField(
          value = catatanKhusus,
          onValueChange = { catatanKhusus = it },
          label = { Text("Catatan Khusus / Evaluasi") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          maxLines = 3
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val updated = student.copy(
            statusPembayaran = selectedStatus,
            kelas = selectedClass,
            coach = selectedCoach,
            hariLatihan = hariLatihan.trim(),
            jamLatihan = jamLatihan.trim(),
            catatanKhusus = catatanKhusus.trim()
          )
          onSave(updated)
        },
        colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_save_edit_student")
      ) {
        Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal", color = TextDarkSecondary)
      }
    }
  )
}
