package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddStudentDialog
import com.example.ui.components.EditStudentDialog
import com.example.ui.components.QrScannerModal
import com.example.ui.components.StatsDashboardSheet
import com.example.ui.components.StudentDetailSheet
import com.example.ui.components.StudentPhotoZoomDialog
import com.example.ui.components.StudentQrCardDialog
import com.example.ui.components.StudentRaportSheet
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanIce
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.TextDarkTertiary
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: StudentViewModel,
  modifier: Modifier = Modifier
) {
  val currentTab by viewModel.currentTab.collectAsState()
  val allStudents by viewModel.allStudents.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val coaches by viewModel.coaches.collectAsState()
  val selectedStudent by viewModel.selectedStudent.collectAsState()
  val editingStudent by viewModel.editingStudent.collectAsState()
  val showAddDialog by viewModel.showAddDialog.collectAsState()
  val showStatsSheet by viewModel.showStatsSheet.collectAsState()
  val zoomedStudent by viewModel.zoomedStudent.collectAsState()
  val selectedRaportStudent by viewModel.selectedRaportStudent.collectAsState()
  val isCloudSyncActive by viewModel.isCloudSyncActive.collectAsState()
  val parentSurveys by viewModel.parentSurveys.collectAsState()
  val showQrScannerModal by viewModel.showQrScannerModal.collectAsState()
  val studentForQrCard by viewModel.studentForQrCard.collectAsState()

  val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val statsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val qrScannerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Handle system back navigation to return to the first tab (Absensi)
  BackHandler(enabled = currentTab != MainTab.ABSENSI) {
    viewModel.setCurrentTab(MainTab.ABSENSI)
  }

  // Define tabs in EXACT order specified:
  // 1) Absensi, 2) Daftar Siswa, 3) Coach, 4) Kelas, 5) Pembayaran
  val menuItems = listOf(
    Triple(MainTab.ABSENSI, "Absensi", Icons.Default.CheckCircle),
    Triple(MainTab.DAFTAR_SISWA, "Daftar Siswa", Icons.Default.Group),
    Triple(MainTab.COACH, "Coach", Icons.Default.Sports),
    Triple(MainTab.KELAS, "Kelas", Icons.Default.Pool),
    Triple(MainTab.PEMBAYARAN, "Pembayaran", Icons.Default.Payments)
  )

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = BackgroundLight,
    topBar = {
      // Ocean-Blue Brand Header with Official SAC Garut Logo
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(OceanDeep, OceanBlue)
            )
          )
          .padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
            bottom = 12.dp,
            start = 16.dp,
            end = 16.dp
          )
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                modifier = Modifier
                  .size(46.dp)
                  .testTag("app_header_logo"),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 4.dp
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.logo_sac_garut),
                    contentDescription = "Logo SAC Garut",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "SAC GARUT",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    color = OceanCyan.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "2026",
                      color = OceanCyan,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                  }
                  if (isCloudSyncActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      color = Color(0xFF10B981).copy(alpha = 0.22f),
                      shape = RoundedCornerShape(4.dp)
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                      ) {
                        Box(
                          modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                          text = "Cloud Sync",
                          color = Color(0xFFA7F3D0),
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }
                }
                Text(
                  text = "Swimming Academy Center • ${currentTab.subtitle}",
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              // Dedicated "Scan Absensi" button
              IconButton(
                onClick = { viewModel.setShowQrScannerModal(true) },
                modifier = Modifier.testTag("btn_top_scan_absensi")
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Absensi",
                    tint = OceanCyan,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              IconButton(
                onClick = { viewModel.setShowStatsSheet(true) },
                modifier = Modifier.testTag("btn_top_stats")
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Statistik",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      }
    },
    bottomBar = {
      // Distinct bottom navigation menu in EXACT order:
      // 1) Absensi, 2) Daftar Siswa, 3) Coach, 4) Kelas, 5) Pembayaran
      NavigationBar(
        containerColor = SurfaceWhite,
        tonalElevation = 6.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bottom_nav_bar")
      ) {
        menuItems.forEach { (tab, label, icon) ->
          val isSelected = currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.setCurrentTab(tab) },
            icon = {
              Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = OceanDeep,
              selectedTextColor = OceanDeep,
              indicatorColor = Color(0xFFE2ECF4),
              unselectedIconColor = TextDarkTertiary,
              unselectedTextColor = TextDarkTertiary
            ),
            modifier = Modifier.testTag("nav_item_${tab.name}")
          )
        }
      }
    },
    floatingActionButton = {
      when (currentTab) {
        MainTab.DAFTAR_SISWA, MainTab.ABSENSI -> {
          FloatingActionButton(
            onClick = { viewModel.setShowAddDialog(true) },
            containerColor = OceanBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("fab_add_student")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Siswa Baru")
          }
        }
        MainTab.COACH -> {
          FloatingActionButton(
            onClick = { viewModel.setShowAddCoachDialog(true) },
            containerColor = OceanBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("fab_add_coach")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Coach Baru")
          }
        }
        else -> {}
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        MainTab.ABSENSI -> {
          AbsensiTabScreen(
            viewModel = viewModel,
            onStudentClick = { viewModel.selectStudent(it) },
            onAvatarClick = { viewModel.setZoomedStudent(it) }
          )
        }
        MainTab.DAFTAR_SISWA -> {
          DaftarSiswaTabScreen(
            viewModel = viewModel,
            onStudentClick = { viewModel.selectStudent(it) },
            onAvatarClick = { viewModel.setZoomedStudent(it) }
          )
        }
        MainTab.COACH -> {
          CoachTabScreen(
            viewModel = viewModel,
            onStudentClick = { viewModel.selectStudent(it) }
          )
        }
        MainTab.KELAS -> {
          KelasTabScreen(
            viewModel = viewModel,
            onStudentClick = { viewModel.selectStudent(it) }
          )
        }
        MainTab.PEMBAYARAN -> {
          PembayaranTabScreen(
            viewModel = viewModel,
            onStudentClick = { viewModel.selectStudent(it) },
            onAvatarClick = { viewModel.setZoomedStudent(it) }
          )
        }
      }
    }
  }

  // Student Detail Bottom Sheet
  selectedStudent?.let { currentStudent ->
    StudentDetailSheet(
      student = currentStudent,
      onDismiss = { viewModel.selectStudent(null) },
      onEditClick = { viewModel.setEditingStudent(currentStudent) },
      onPaymentStatusChange = { newStatus ->
        viewModel.updatePaymentStatus(currentStudent.id, newStatus)
      },
      sheetState = detailSheetState,
      classes = classes,
      onAvatarClick = { viewModel.setZoomedStudent(it) },
      onOpenRaport = { viewModel.setSelectedRaportStudent(currentStudent) },
      onShowQrCard = { viewModel.setStudentForQrCard(it) }
    )
  }

  // Student Raport Bottom Sheet
  selectedRaportStudent?.let { currentRaportStudent ->
    StudentRaportSheet(
      student = currentRaportStudent,
      viewModel = viewModel,
      onDismiss = { viewModel.setSelectedRaportStudent(null) }
    )
  }

  // Edit Student Dialog
  editingStudent?.let { currentEditing ->
    EditStudentDialog(
      student = currentEditing,
      availableClasses = classes,
      availableCoaches = coaches,
      onDismiss = { viewModel.setEditingStudent(null) },
      onSave = { updated ->
        viewModel.updateStudent(updated)
        viewModel.setEditingStudent(null)
      }
    )
  }

  // Add Student Dialog
  if (showAddDialog) {
    AddStudentDialog(
      availableClasses = classes,
      availableCoaches = coaches,
      onDismiss = { viewModel.setShowAddDialog(false) },
      onAdd = { newStudent ->
        viewModel.addStudent(newStudent)
        viewModel.setShowAddDialog(false)
      }
    )
  }

  // Stats Dashboard Sheet
  if (showStatsSheet) {
    StatsDashboardSheet(
      students = allStudents,
      classes = classes,
      coaches = coaches,
      parentSurveys = parentSurveys,
      onDismiss = { viewModel.setShowStatsSheet(false) },
      onResetData = { viewModel.resetData() },
      sheetState = statsSheetState
    )
  }

  // Student Photo Zoom Preview Modal
  zoomedStudent?.let { currentZoomed ->
    StudentPhotoZoomDialog(
      student = currentZoomed,
      onDismiss = { viewModel.setZoomedStudent(null) }
    )
  }

  // QR Barcode Scanner Modal Sheet
  if (showQrScannerModal) {
    QrScannerModal(
      viewModel = viewModel,
      onDismiss = { viewModel.setShowQrScannerModal(false) },
      sheetState = qrScannerSheetState
    )
  }

  // Student QR Member Card Preview & Share Dialog
  studentForQrCard?.let { currentQrStudent ->
    StudentQrCardDialog(
      student = currentQrStudent,
      onDismiss = { viewModel.setStudentForQrCard(null) }
    )
  }
}
