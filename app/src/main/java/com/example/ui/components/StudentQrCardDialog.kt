package com.example.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Student
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StudentQrCardDialog(
  student: Student,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isGenerating by remember { mutableStateOf(true) }

  LaunchedEffect(student.id) {
    isGenerating = true
    withContext(Dispatchers.IO) {
      val qr = QrCodeGenerator.generateQrBitmap(student.id, sizePx = 480)
      if (qr != null) {
        val card = QrCodeGenerator.generateStudentIdCardBitmap(student, qr)
        cardBitmap = card
      }
      isGenerating = false
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("dialog_student_qr_card")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Dialog Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OceanDeep),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                tint = OceanCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Kartu Barcode Siswa",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OceanDeep
              )
              Text(
                text = "ID: ${student.id}",
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

        // Card Preview
        if (isGenerating) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(320.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator(color = OceanDeep)
              Spacer(modifier = Modifier.height(12.dp))
              Text("Membuat Kartu Anggota...", fontSize = 12.sp, color = TextDarkSecondary)
            }
          }
        } else if (cardBitmap != null) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp,
            color = Color.White,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp)
          ) {
            Image(
              bitmap = cardBitmap!!.asImageBitmap(),
              contentDescription = "Kartu Absensi ${student.namaLengkap}",
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
              contentScale = ContentScale.FillWidth
            )
          }
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("Gagal membuat barcode kartu", color = Color.Red, fontSize = 13.sp)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WhatsApp Share Button
        Button(
          onClick = {
            cardBitmap?.let { bmp ->
              scope.launch(Dispatchers.IO) {
                val file = QrCodeGenerator.saveQrCardToFile(context, bmp, student.id)
                if (file != null) {
                  withContext(Dispatchers.Main) {
                    QrCodeGenerator.shareQrCardViaWhatsApp(context, file, student)
                  }
                } else {
                  withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal menyiapkan file kartu", Toast.LENGTH_SHORT).show()
                  }
                }
              }
            }
          },
          enabled = cardBitmap != null,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_share_qr_card_wa")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Kirim Kartu ke WA Orang Tua", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save / Download Card Button
        OutlinedButton(
          onClick = {
            cardBitmap?.let { bmp ->
              scope.launch(Dispatchers.IO) {
                val file = QrCodeGenerator.saveQrCardToFile(context, bmp, student.id)
                withContext(Dispatchers.Main) {
                  if (file != null) {
                    Toast.makeText(context, "Kartu berhasil disimpan di: ${file.name}", Toast.LENGTH_LONG).show()
                  } else {
                    Toast.makeText(context, "Gagal menyimpan kartu", Toast.LENGTH_SHORT).show()
                  }
                }
              }
            }
          },
          enabled = cardBitmap != null,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_download_qr_card")
        ) {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            tint = OceanDeep,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Simpan Kartu ke Galeri / File", color = OceanDeep, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
