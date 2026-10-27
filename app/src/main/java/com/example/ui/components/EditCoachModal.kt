package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.CoachProfile
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary

@Composable
fun EditCoachModal(
  coach: CoachProfile,
  onDismiss: () -> Unit,
  onSave: (CoachProfile, String) -> Unit, // returns updated coach + original old name
  modifier: Modifier = Modifier,
  isNewCoach: Boolean = false
) {
  val originalName = coach.name
  var name by remember { mutableStateOf(coach.name) }
  var role by remember { mutableStateOf(coach.role) }
  var phone by remember { mutableStateOf(coach.phone) }
  var bio by remember { mutableStateOf(coach.bio) }
  var photoUrl by remember { mutableStateOf(coach.photoUrl) }
  var errorMessage by remember { mutableStateOf("") }

  // Zero-permission modern Android Photo Picker for Coach Avatar
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      photoUrl = uri.toString()
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier.testTag("dialog_edit_coach"),
    containerColor = SurfaceWhite,
    shape = RoundedCornerShape(20.dp),
    title = {
      Column {
        Text(
          text = if (isNewCoach) "Tambah Coach Baru" else "Kelola & Edit Pelatih",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = OceanDeep
        )
        Text(
          text = if (isNewCoach) "Daftarkan pelatih baru beserta foto profil ke SAC Garut" else "Perbarui foto, nama, peran, & nomor telepon coach",
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
        if (errorMessage.isNotBlank()) {
          Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }

        // Coach Avatar & Photo Picker section
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val previewCoach = coach.copy(name = name.ifBlank { "Coach" }, photoUrl = photoUrl)
          CoachAvatar(
            coach = previewCoach,
            size = 64.dp,
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
          )

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Foto Profil Coach",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
            Text(
              text = if (photoUrl.isNotBlank()) "Foto kustom terpasang" else "Gunakan avatar inisial atau pilih foto",
              fontSize = 11.sp,
              color = TextDarkSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_pick_coach_photo")
              ) {
                Icon(
                  imageVector = Icons.Default.AddPhotoAlternate,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = OceanBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (photoUrl.isBlank()) "Pilih Foto" else "Ganti Foto",
                  fontSize = 11.sp,
                  color = OceanBlue,
                  fontWeight = FontWeight.Bold
                )
              }

              if (photoUrl.isNotBlank()) {
                TextButton(
                  onClick = { photoUrl = "" },
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = "Hapus",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            errorMessage = ""
          },
          label = { Text("Nama Lengkap Coach *") },
          placeholder = { Text("e.g. Coach Bayu / Coach Sarah") },
          modifier = Modifier.fillMaxWidth().testTag("input_edit_coach_name"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = role,
          onValueChange = { role = it },
          label = { Text("Peran / Spesialisasi Melatih") },
          placeholder = { Text("e.g. Pelatih Kepala, Gaya Bebas & Dada") },
          modifier = Modifier.fillMaxWidth().testTag("input_edit_coach_role"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Nomor Handphone / WhatsApp") },
          placeholder = { Text("e.g. 08123456789") },
          modifier = Modifier.fillMaxWidth().testTag("input_edit_coach_phone"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = bio,
          onValueChange = { bio = it },
          label = { Text("Kualifikasi / Pengalaman") },
          placeholder = { Text("Pengalaman kepelatihan, sertifikasi, dll") },
          modifier = Modifier.fillMaxWidth().testTag("input_edit_coach_bio"),
          shape = RoundedCornerShape(10.dp),
          maxLines = 3
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) {
            errorMessage = "Nama coach tidak boleh kosong!"
            return@Button
          }
          val updated = coach.copy(
            name = name.trim(),
            role = role.trim(),
            phone = phone.trim(),
            bio = bio.trim(),
            photoUrl = photoUrl.trim()
          )
          onSave(updated, originalName)
        },
        colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_save_coach_settings")
      ) {
        Text(if (isNewCoach) "Tambah Pelatih" else "Simpan Pelatih", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal", color = TextDarkSecondary)
      }
    }
  )
}
