package com.example.ui.components

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.Student
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanIce
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.TextDarkTertiary
import com.example.ui.theme.WhatsAppGreen
import com.example.util.WhatsAppHelper

@Composable
fun StudentPhotoZoomDialog(
  student: Student,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val photoUrl = student.directPhotoUrl

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = true,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.75f))
        .clickable(onClick = onDismiss)
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      // Main Dialog Content Card
      Card(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .clickable(enabled = false) {} // Prevent closing when tapping inside the card
          .testTag("dialog_photo_zoom_${student.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Top Header Row with title & close button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(OceanIce),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ZoomIn,
                  contentDescription = null,
                  tint = OceanBlue,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Foto & Profil Siswa",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OceanDeep
              )
            }

            IconButton(
              onClick = onDismiss,
              modifier = Modifier.testTag("btn_close_photo_zoom")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                tint = TextDarkSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // High Resolution Image Frame
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1f)
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xFF0F172A))
              .border(2.dp, OceanBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
          ) {
            if (!photoUrl.isNullOrBlank()) {
              SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(photoUrl)
                  .crossfade(true)
                  .build(),
                contentDescription = "Foto Besar ${student.namaLengkap}",
                contentScale = ContentScale.Crop,
                loading = {
                  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OceanCyan, modifier = Modifier.size(36.dp))
                  }
                },
                error = {
                  LargeAvatarFallback(student)
                },
                modifier = Modifier.fillMaxSize()
              )
            } else {
              LargeAvatarFallback(student)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Student Details
          Text(
            text = student.namaLengkap,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkPrimary,
            textAlign = TextAlign.Center
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
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = TextDarkTertiary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = student.asalSekolah,
                fontSize = 12.sp,
                color = TextDarkSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Badges Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            ClassBadge(kelas = student.kelas)
            Spacer(modifier = Modifier.width(8.dp))
            CoachBadge(coach = student.coach)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Schedule Info Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF8FAFC))
              .border(1.dp, Color(0xFFE2EAF1), RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = OceanBlue,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Jadwal: ${student.hariLatihan} • ${student.jamLatihan}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = OceanDeep
                )
              }

              if (student.displayParentName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextDarkTertiary,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Orang Tua: ${student.displayParentName}",
                    fontSize = 11.sp,
                    color = TextDarkSecondary
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Quick Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                WhatsAppHelper.sendStudentInfo(context, student)
              },
              modifier = Modifier
                .weight(1f)
                .height(44.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
              Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = onDismiss,
              modifier = Modifier
                .weight(1f)
                .height(44.dp),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Tutup", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OceanDeep)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LargeAvatarFallback(student: Student) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.linearGradient(
          colors = listOf(OceanDeep, OceanBlue, OceanAzure)
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = student.initials,
        color = Color.White,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 64.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Foto profil belum tersedia",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 11.sp
      )
    }
  }
}
