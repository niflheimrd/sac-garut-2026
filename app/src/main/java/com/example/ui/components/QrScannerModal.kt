package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.viewmodel.StudentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerModal(
  viewModel: StudentViewModel,
  onDismiss: () -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var scannedStudent by remember { mutableStateOf<Student?>(null) }
  var scannedCheckInTime by remember { mutableStateOf("") }
  var showSuccessDialog by remember { mutableStateOf(false) }
  var scanKey by remember { mutableStateOf(0) }

  val playBeep = remember {
    {
      try {
        val toneG = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        toneG.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
      } catch (e: Exception) {
        // Fallback silently if tone generator unavailable
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF0A0F1D),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = modifier.testTag("modal_qr_attendance_scanner")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 16.dp)
    ) {
      // Top Modal Header Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(OceanDeep),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = OceanCyan,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Scan Absensi Siswa",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Arahkan kamera ke barcode kartu identitas siswa",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f)
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("btn_close_scanner_modal")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Tutup Scanner",
            tint = Color.White
          )
        }
      }

      // Camera Scanner Viewport
      Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.runtime.key(scanKey) {
          CameraBarcodeScannerView(
            onBarcodeDetected = { rawCode ->
              scope.launch {
                val (success, student, checkInTime) = viewModel.recordBarcodeAttendance(rawCode)
                if (success && student != null) {
                  playBeep()
                  scannedStudent = student
                  scannedCheckInTime = checkInTime
                  showSuccessDialog = true
                } else {
                  Toast.makeText(
                    context,
                    "Barcode tidak terdaftar: $rawCode",
                    Toast.LENGTH_SHORT
                  ).show()
                  // Unlock after short delay
                  kotlinx.coroutines.delay(1800)
                  scanKey++
                }
              }
            },
            modifier = Modifier.fillMaxSize()
          )
        }

        // Attendance Success Dialog
        if (showSuccessDialog && scannedStudent != null) {
          AttendanceSuccessDialog(
            student = scannedStudent!!,
            checkInTime = scannedCheckInTime,
            onScanAnother = {
              showSuccessDialog = false
              scannedStudent = null
              scanKey++
            },
            onDismiss = {
              showSuccessDialog = false
              scannedStudent = null
              onDismiss()
            }
          )
        }
      }
    }
  }
}
