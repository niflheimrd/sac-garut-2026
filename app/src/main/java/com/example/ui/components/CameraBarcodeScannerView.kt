package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraBarcodeScannerView(
  onBarcodeDetected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  var isTorchOn by remember { mutableStateOf(false) }
  var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
  var activeCamera by remember { mutableStateOf<Camera?>(null) }
  var manualCodeInput by remember { mutableStateOf("") }
  var isScanningLocked by remember { mutableStateOf(false) }

  // Infinite laser scan line animation
  val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
  val laserProgress by infiniteTransition.animateFloat(
    initialValue = 0.1f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(1600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_progress"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    if (hasCameraPermission) {
      // Camera Preview
      AndroidView(
        factory = { ctx ->
          val previewView = PreviewView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
          }

          val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
          val cameraExecutor = Executors.newSingleThreadExecutor()

          val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
              Barcode.FORMAT_QR_CODE,
              Barcode.FORMAT_CODE_128,
              Barcode.FORMAT_CODE_39,
              Barcode.FORMAT_EAN_13,
              Barcode.FORMAT_UPC_A
            )
            .build()
          val scanner = BarcodeScanning.getClient(options)

          cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
              it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalysis = ImageAnalysis.Builder()
              .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
              .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
              val mediaImage = imageProxy.image
              if (mediaImage != null && !isScanningLocked) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                  .addOnSuccessListener { barcodes ->
                    val first = barcodes.firstOrNull()
                    val rawValue = first?.rawValue ?: first?.displayValue
                    if (!rawValue.isNullOrBlank() && !isScanningLocked) {
                      isScanningLocked = true
                      onBarcodeDetected(rawValue)
                    }
                  }
                  .addOnCompleteListener {
                    imageProxy.close()
                  }
              } else {
                imageProxy.close()
              }
            }

            try {
              cameraProvider.unbindAll()
              val cam = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
              )
              activeCamera = cam
            } catch (e: Exception) {
              Log.e("CameraBarcodeScanner", "Camera binding failed", e)
            }
          }, ContextCompat.getMainExecutor(ctx))

          previewView
        },
        modifier = Modifier.fillMaxSize()
      )

      // Target Framing & Animated Laser Box
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(260.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(3.dp, OceanCyan, RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val yPos = size.height * laserProgress
            drawLine(
              brush = Brush.horizontalGradient(
                colors = listOf(
                  Color.Transparent,
                  Color(0xFF00E5FF),
                  Color.White,
                  Color(0xFF00E5FF),
                  Color.Transparent
                )
              ),
              start = Offset(0f, yPos),
              end = Offset(size.width, yPos),
              strokeWidth = 6f
            )
          }
        }
      }

      // Scanner Controls: Flashlight & Camera Switch
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color.Black.copy(alpha = 0.6f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.QrCodeScanner,
              contentDescription = null,
              tint = OceanCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Arahkan Barcode ke Kotak",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // Torch Button
          IconButton(
            onClick = {
              val newState = !isTorchOn
              isTorchOn = newState
              activeCamera?.cameraControl?.enableTorch(newState)
            },
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.6f))
          ) {
            Icon(
              imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
              contentDescription = "Senter",
              tint = if (isTorchOn) Color(0xFFFFD700) else Color.White
            )
          }

          // Switch Camera Button
          IconButton(
            onClick = {
              cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
              } else {
                CameraSelector.DEFAULT_BACK_CAMERA
              }
            },
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.6f))
          ) {
            Icon(
              imageVector = Icons.Default.Cameraswitch,
              contentDescription = "Ganti Kamera",
              tint = Color.White
            )
          }
        }
      }

    } else {
      // Permission Denied / Prompt State
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(OceanCyan.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = null,
            tint = OceanCyan,
            modifier = Modifier.size(36.dp)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Izin Kamera Diperlukan",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Aplikasi membutuhkan akses kamera untuk memindai kode barcode/QR kartu identitas siswa SAC Garut secara langsung.",
          fontSize = 13.sp,
          color = Color.White.copy(alpha = 0.7f),
          textAlign = TextAlign.Center,
          lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
          colors = ButtonDefaults.buttonColors(containerColor = OceanCyan),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Aktifkan Kamera", color = OceanDeep, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Bottom Fallback / Manual ID Input bar
    // Useful for testing in emulators or if barcode is damaged
    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
      color = Color(0xFF1E293B).copy(alpha = 0.95f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        Text(
          text = "Input Manual / Uji Kode Siswa:",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = manualCodeInput,
            onValueChange = { manualCodeInput = it },
            placeholder = { Text("Contoh: SAC-2026-001", fontSize = 12.sp, color = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = OceanCyan,
              unfocusedBorderColor = Color(0xFF475569)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
              .testTag("input_manual_barcode")
          )
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (manualCodeInput.isNotBlank()) {
                onBarcodeDetected(manualCodeInput.trim())
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = OceanCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .height(50.dp)
              .testTag("btn_submit_manual_barcode")
          ) {
            Text("Proses", color = OceanDeep, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}
