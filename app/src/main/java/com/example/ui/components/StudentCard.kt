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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassCategory
import com.example.data.model.Student
import com.example.ui.theme.BadgePrestasi
import com.example.ui.theme.BadgePrestasiBg
import com.example.ui.theme.BadgePrivat
import com.example.ui.theme.BadgePrivatBg
import com.example.ui.theme.BadgeRegulerA
import com.example.ui.theme.BadgeRegulerABg
import com.example.ui.theme.BadgeRegulerB
import com.example.ui.theme.BadgeRegulerBBg
import com.example.ui.theme.BadgeTerapi
import com.example.ui.theme.BadgeTerapiBg
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanIce
import com.example.ui.theme.StatusBelumLunasBg
import com.example.ui.theme.StatusBelumLunasBorder
import com.example.ui.theme.StatusBelumLunasText
import com.example.ui.theme.StatusJatuhTempoBg
import com.example.ui.theme.StatusJatuhTempoBorder
import com.example.ui.theme.StatusJatuhTempoText
import com.example.ui.theme.StatusLunasBg
import com.example.ui.theme.StatusLunasBorder
import com.example.ui.theme.StatusLunasText
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.TextDarkTertiary
import com.example.ui.theme.WhatsAppGreen
import java.net.URLEncoder

@Composable
fun StudentCard(
  student: Student,
  onClick: () -> Unit,
  onWhatsAppClick: () -> Unit,
  modifier: Modifier = Modifier,
  classes: List<ClassCategory> = emptyList(),
  showPaymentBadge: Boolean = false, // Strict separation: false on main list, true on Pembayaran screen
  onAvatarClick: ((Student) -> Unit)? = null
) {
  val context = LocalContext.current
  val assignedCoaches = student.getAssignedCoaches(classes)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("student_card_${student.id}")
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top row: Photo Avatar + Name + Class Pill + WhatsApp icon
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        StudentAvatar(
          student = student,
          size = 48.dp,
          onClick = if (onAvatarClick != null) { { onAvatarClick(student) } } else null
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name, ID & School
        Column(
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = student.namaLengkap,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDarkPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = student.id,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = OceanAzure
            )
            if (student.asalSekolah.isNotBlank() && student.asalSekolah != "-") {
              Text(
                text = " • ${student.asalSekolah}",
                fontSize = 11.sp,
                color = TextDarkTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        // WhatsApp Quick Action Button
        Surface(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .testTag("whatsapp_button_${student.id}")
            .clickable {
              openWhatsApp(context, student)
              onWhatsAppClick()
            },
          color = Color(0xFFE7F9ED),
          shape = CircleShape
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = "Hubungi WhatsApp",
              tint = WhatsAppGreen,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Middle Row: Schedule (Hari & Jam)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.AccessTime,
          contentDescription = null,
          tint = OceanBlue,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "${student.hariLatihan} • ${student.jamLatihan}",
          fontSize = 12.sp,
          color = TextDarkSecondary,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Multi-Coach Display: Shows all 3 assigned coaches for the student's class
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(OceanIce)
          .padding(horizontal = 8.dp, vertical = 5.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Sports,
            contentDescription = null,
            tint = OceanBlue,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Tim Pelatih (3 Coach): " + assignedCoaches.joinToString(", "),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = OceanDeep,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Bottom Row: Class Level Badge (+ Optional Payment Status only in Pembayaran tab)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        ClassBadge(kelas = student.kelas)

        if (showPaymentBadge) {
          PaymentStatusBadge(status = student.statusPembayaran)
        } else {
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextDarkTertiary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ClassBadge(kelas: String, modifier: Modifier = Modifier) {
  val (bgColor, textColor) = when {
    kelas.contains("Prestasi") -> Color(0xFFFEF3C7) to Color(0xFFB45309)
    kelas.contains("Moderat") -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
    kelas.contains("Basic") -> Color(0xFFE6F7EE) to Color(0xFF087F47)
    kelas.contains("Pemula") -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
    else -> Color(0xFFF1F5F9) to OceanDeep
  }

  Surface(
    modifier = modifier,
    color = bgColor,
    shape = RoundedCornerShape(8.dp)
  ) {
    Text(
      text = kelas,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun CoachBadge(coach: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    color = Color(0xFFF1F5F9),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Person,
        contentDescription = null,
        tint = OceanBlue,
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(3.dp))
      Text(
        text = coach,
        color = TextDarkSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
fun PaymentStatusBadge(status: String, modifier: Modifier = Modifier) {
  val (bgColor, textColor, borderColor) = when (status) {
    "Lunas" -> Triple(StatusLunasBg, StatusLunasText, StatusLunasBorder)
    "Belum Lunas" -> Triple(StatusBelumLunasBg, StatusBelumLunasText, StatusBelumLunasBorder)
    "Jatuh Tempo" -> Triple(StatusJatuhTempoBg, StatusJatuhTempoText, StatusJatuhTempoBorder)
    else -> Triple(Color(0xFFF1F5F9), TextDarkSecondary, Color(0xFFCBD5E1))
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(20.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = status,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

fun openWhatsApp(context: Context, student: Student) {
  com.example.util.WhatsAppHelper.sendStudentInfo(context, student)
}
