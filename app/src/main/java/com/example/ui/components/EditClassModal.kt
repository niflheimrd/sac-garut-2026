package com.example.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClassModal(
  category: ClassCategory,
  availableCoaches: List<CoachProfile>,
  onDismiss: () -> Unit,
  onSave: (ClassCategory) -> Unit,
  modifier: Modifier = Modifier
) {
  var name by remember { mutableStateOf(category.name) }
  var duration by remember { mutableStateOf(category.duration) }
  var description by remember { mutableStateOf(category.description) }
  var targetFocus by remember { mutableStateOf(category.targetFocus) }

  var coachUtama by remember { mutableStateOf(category.coachUtama) }
  var asistenCoach1 by remember { mutableStateOf(category.asistenCoach1) }
  var asistenCoach2 by remember { mutableStateOf(category.asistenCoach2) }

  var expandedUtama by remember { mutableStateOf(false) }
  var expandedAsisten1 by remember { mutableStateOf(false) }
  var expandedAsisten2 by remember { mutableStateOf(false) }

  val coachNames = availableCoaches.map { it.name }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier.testTag("dialog_edit_class"),
    containerColor = SurfaceWhite,
    shape = RoundedCornerShape(20.dp),
    title = {
      Column {
        Text(
          text = "Kelola & Edit Kelas",
          style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = OceanDeep
        )
        Text(
          text = "Perbarui level durasi dan tim 3 coach",
          fontSize = 12.sp,
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
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nama Level / Kategori *") },
          modifier = Modifier.fillMaxWidth().testTag("input_edit_class_name"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = duration,
          onValueChange = { duration = it },
          label = { Text("Rentang Durasi (e.g. > 1 Tahun, 6 - 12 Bulan)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = targetFocus,
          onValueChange = { targetFocus = it },
          label = { Text("Fokus & Target Latihan") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "TIM 3 COACH KELAS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = OceanDeep
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Coach Utama Dropdown (Required)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "1. Head Coach (Wajib):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OceanDeep)
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "*", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        ExposedDropdownMenuBox(
          expanded = expandedUtama,
          onExpandedChange = { expandedUtama = it }
        ) {
          OutlinedTextField(
            value = coachUtama.ifBlank { "Pilih Head Coach" },
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUtama) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = expandedUtama,
            onDismissRequest = { expandedUtama = false }
          ) {
            coachNames.forEach { cName ->
              DropdownMenuItem(
                text = { Text(cName, fontWeight = if (cName == coachUtama) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                  coachUtama = cName
                  expandedUtama = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Asisten Coach 1 Dropdown (Optional with - None -)
        Text(text = "2. Assistant Coach 1 (Opsional):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDarkPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        ExposedDropdownMenuBox(
          expanded = expandedAsisten1,
          onExpandedChange = { expandedAsisten1 = it }
        ) {
          OutlinedTextField(
            value = if (asistenCoach1.isBlank()) "- None -" else asistenCoach1,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAsisten1) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = expandedAsisten1,
            onDismissRequest = { expandedAsisten1 = false }
          ) {
            DropdownMenuItem(
              text = { Text("- None -", color = TextDarkSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) },
              onClick = {
                asistenCoach1 = ""
                expandedAsisten1 = false
              }
            )
            coachNames.forEach { cName ->
              DropdownMenuItem(
                text = { Text(cName, fontWeight = if (cName == asistenCoach1) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                  asistenCoach1 = cName
                  expandedAsisten1 = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Asisten Coach 2 Dropdown (Optional with - None -)
        Text(text = "3. Assistant Coach 2 (Opsional):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDarkPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        ExposedDropdownMenuBox(
          expanded = expandedAsisten2,
          onExpandedChange = { expandedAsisten2 = it }
        ) {
          OutlinedTextField(
            value = if (asistenCoach2.isBlank()) "- None -" else asistenCoach2,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAsisten2) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = expandedAsisten2,
            onDismissRequest = { expandedAsisten2 = false }
          ) {
            DropdownMenuItem(
              text = { Text("- None -", color = TextDarkSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) },
              onClick = {
                asistenCoach2 = ""
                expandedAsisten2 = false
              }
            )
            coachNames.forEach { cName ->
              DropdownMenuItem(
                text = { Text(cName, fontWeight = if (cName == asistenCoach2) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                  asistenCoach2 = cName
                  expandedAsisten2 = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Deskripsi Lengkap") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          maxLines = 3
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalCoachUtama = if (coachUtama.isBlank()) coachNames.firstOrNull() ?: "Coach Fahmi" else coachUtama.trim()
          val updated = category.copy(
            name = name.trim(),
            duration = duration.trim(),
            targetFocus = targetFocus.trim(),
            description = description.trim(),
            coachUtama = finalCoachUtama,
            asistenCoach1 = asistenCoach1.trim(),
            asistenCoach2 = asistenCoach2.trim()
          )
          onSave(updated)
        },
        colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_save_class_settings")
      ) {
        Text("Simpan Kelas", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal", color = TextDarkSecondary)
      }
    }
  )
}
