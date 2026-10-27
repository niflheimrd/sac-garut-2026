package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
  availableClasses: List<ClassCategory>,
  availableCoaches: List<CoachProfile>,
  onDismiss: () -> Unit,
  onAdd: (Student) -> Unit,
  modifier: Modifier = Modifier
) {
  val defaultClass = availableClasses.firstOrNull()?.name ?: "Pemula Basic (< 3 Bulan)"
  val defaultCoach = availableCoaches.firstOrNull()?.name ?: "Coach Fahmi"

  var namaLengkap by remember { mutableStateOf("") }
  var selectedClass by remember { mutableStateOf(defaultClass) }
  var selectedCoach by remember { mutableStateOf(defaultCoach) }
  var hariLatihan by remember { mutableStateOf("Minggu") }
  var jamLatihan by remember { mutableStateOf("08.00 - 09.30 WIB") }
  var namaOrangTua by remember { mutableStateOf("") }
  var noHp by remember { mutableStateOf("") }
  var asalSekolah by remember { mutableStateOf("") }
  var selectedStatus by remember { mutableStateOf("Lunas") }

  var errorMessage by remember { mutableStateOf("") }

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
    modifier = modifier.testTag("dialog_add_student"),
    containerColor = SurfaceWhite,
    shape = RoundedCornerShape(20.dp),
    title = {
      Text(
        text = "Tambah Siswa Baru",
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = OceanDeep
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp)
      ) {
        if (errorMessage.isNotBlank()) {
          Text(
            text = errorMessage,
            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }

        OutlinedTextField(
          value = namaLengkap,
          onValueChange = {
            namaLengkap = it
            errorMessage = ""
          },
          label = { Text("Nama Lengkap Anak *") },
          modifier = Modifier.fillMaxWidth().testTag("input_student_name"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Kelas Dropdown
        Text(text = "Level & Kategori Kelas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OceanDeep)
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
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = classExpanded,
            onDismissRequest = { classExpanded = false }
          ) {
            classOptions.forEach { cls ->
              DropdownMenuItem(
                text = { Text(cls) },
                onClick = {
                  selectedClass = cls
                  classExpanded = false
                  availableClasses.find { it.name == cls }?.let { cat ->
                    if (cat.coachUtama.isNotBlank()) selectedCoach = cat.coachUtama
                  }
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Coach Dropdown
        Text(text = "Pelatih / Lead Coach", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OceanDeep)
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
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = coachExpanded,
            onDismissRequest = { coachExpanded = false }
          ) {
            coachOptions.forEach { cch ->
              DropdownMenuItem(
                text = { Text(cch) },
                onClick = {
                  selectedCoach = cch
                  coachExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = hariLatihan,
          onValueChange = { hariLatihan = it },
          label = { Text("Hari Latihan (e.g. Minggu, Sabtu)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = jamLatihan,
          onValueChange = { jamLatihan = it },
          label = { Text("Jam Latihan") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = namaOrangTua,
          onValueChange = { namaOrangTua = it },
          label = { Text("Nama Orang Tua (Ibu/Ayah)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = noHp,
          onValueChange = { noHp = it },
          label = { Text("No. WhatsApp / HP") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = asalSekolah,
          onValueChange = { asalSekolah = it },
          label = { Text("Asal Sekolah") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Status Pembayaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OceanDeep)
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
              Text(text = status, fontSize = 12.sp, color = TextDarkPrimary)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (namaLengkap.isBlank()) {
            errorMessage = "Nama siswa tidak boleh kosong"
            return@Button
          }
          val randId = "SAC-2026-${(100..999).random()}"
          val timeStr = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
          val newStudent = Student(
            id = randId,
            timestamp = timeStr,
            namaLengkap = namaLengkap.trim(),
            asalSekolah = asalSekolah.trim(),
            kelas = selectedClass,
            hariLatihan = hariLatihan.trim(),
            jamLatihan = jamLatihan.trim(),
            coach = selectedCoach,
            namaOrangTua = namaOrangTua.trim(),
            noHp = noHp.trim(),
            statusPembayaran = selectedStatus
          )
          onAdd(newStudent)
        },
        colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_confirm_add_student")
      ) {
        Text("Daftarkan Siswa", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal", color = TextDarkSecondary)
      }
    }
  )
}
