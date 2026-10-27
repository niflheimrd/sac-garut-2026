package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.ui.components.ClassBadge
import com.example.ui.components.CoachBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanCyan
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
import com.example.ui.viewmodel.StudentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AbsensiTabScreen(
  viewModel: StudentViewModel,
  onStudentClick: (Student) -> Unit,
  modifier: Modifier = Modifier,
  onAvatarClick: ((Student) -> Unit)? = null
) {
  val allStudents by viewModel.allStudents.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val attendanceMap by viewModel.attendanceMap.collectAsState()
  val selectedDate by viewModel.selectedAttendanceDate.collectAsState()
  val selectedClass by viewModel.attendanceClassFilter.collectAsState()
  val searchQuery by viewModel.attendanceSearchQuery.collectAsState()
  val filterOnlyScheduledDay by viewModel.attendanceFilterOnlyScheduledDay.collectAsState()

  val classFilters = remember(classes) { listOf("Semua") + classes.map { it.name } }

  // Derive day of the week in Indonesian from selectedDate (e.g. "Selasa", "Kamis", "Jum'at", "Sabtu", "Minggu")
  val selectedDayName = remember(selectedDate) { viewModel.getDayNameFromDate(selectedDate) }

  // Total students scheduled specifically on this day across all classes
  val scheduledTodayTotal = remember(allStudents, selectedDayName) {
    allStudents.count { viewModel.isStudentScheduledOnDay(it, selectedDayName) }
  }

  // Generate 7-day quick date picker items
  val dateOptions = remember {
    val list = mutableListOf<Pair<String, String>>()
    val cal = Calendar.getInstance()
    // 2 days back, today, 4 days forward
    cal.add(Calendar.DAY_OF_YEAR, -2)
    val dayFmt = SimpleDateFormat("EEE, dd MMM", Locale("id", "ID"))
    val keyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    for (i in 0..6) {
      list.add(Pair(keyFmt.format(cal.time), dayFmt.format(cal.time)))
      cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    list
  }

  // Dynamic filtering:
  // 1. Matches Day of Week (when filterOnlyScheduledDay is enabled)
  // 2. Matches Enrolled Class Category
  // 3. Matches Search Query
  val filteredStudents = remember(allStudents, selectedDayName, selectedClass, searchQuery, filterOnlyScheduledDay) {
    allStudents.filter { student ->
      val matchesDay = !filterOnlyScheduledDay || viewModel.isStudentScheduledOnDay(student, selectedDayName)
      val matchesClass = selectedClass == "Semua" || student.kelas.equals(selectedClass, ignoreCase = true)
      val matchesQuery = searchQuery.isBlank() ||
          student.namaLengkap.contains(searchQuery.trim(), ignoreCase = true) ||
          student.coach.contains(searchQuery.trim(), ignoreCase = true) ||
          student.asalSekolah.contains(searchQuery.trim(), ignoreCase = true) ||
          student.id.contains(searchQuery.trim(), ignoreCase = true)
      matchesDay && matchesClass && matchesQuery
    }
  }

  // Attendance stats for selected date
  val dateAttendance = attendanceMap[selectedDate] ?: emptyMap()
  val hadirCount = filteredStudents.count { dateAttendance[it.id] == AttendanceStatus.HADIR }
  val izinCount = filteredStudents.count { dateAttendance[it.id] == AttendanceStatus.IZIN }
  val alphaCount = filteredStudents.count { dateAttendance[it.id] == AttendanceStatus.ALPHA }
  val belumCount = filteredStudents.size - hadirCount - izinCount - alphaCount

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
  ) {
    // Top Date Selector Strip
    Surface(
      color = SurfaceWhite,
      shadowElevation = 1.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = null,
              tint = OceanBlue,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Pilih Tanggal Latihan",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
          }

          // Batch mark button
          OutlinedButton(
            onClick = {
              viewModel.markAllAttendance(AttendanceStatus.HADIR, filteredStudents)
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("btn_mark_all_hadir")
          ) {
            Icon(
              imageVector = Icons.Default.DoneAll,
              contentDescription = null,
              tint = StatusLunasText,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Semua Hadir",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = StatusLunasText
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal date selector
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          dateOptions.forEach { (key, label) ->
            val isSelected = selectedDate == key
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) OceanDeep else Color(0xFFF1F5F9))
                .border(
                  width = 1.dp,
                  color = if (isSelected) OceanDeep else Color(0xFFE2EAF1),
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { viewModel.setAttendanceDate(key) }
                .padding(horizontal = 12.dp, vertical = 7.dp)
                .testTag("date_chip_$key")
            ) {
              Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextDarkSecondary
              )
            }
          }
        }
      }
    }

    // Dedicated Scan Absensi Action Card
    Card(
      onClick = { viewModel.setShowQrScannerModal(true) },
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = OceanDeep),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .testTag("card_scan_absensi_action")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(OceanCyan.copy(alpha = 0.2f)),
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
          Column {
            Text(
              text = "Scan Barcode / QR Siswa",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Presensi otomatis via kamera langsung ke Firestore",
              color = Color.White.copy(alpha = 0.75f),
              fontSize = 10.sp
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = OceanCyan
        ) {
          Text(
            text = "Buka Scanner",
            color = OceanDeep,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    // Schedule Day Filter Banner
    Surface(
      color = if (filterOnlyScheduledDay) OceanIce else Color(0xFFF1F5F9),
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(
        1.dp,
        if (filterOnlyScheduledDay) OceanAzure.copy(alpha = 0.4f) else Color(0xFFE2EAF1)
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .testTag("banner_schedule_filter")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .background(if (filterOnlyScheduledDay) OceanBlue else Color(0xFFCBD5E1)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (filterOnlyScheduledDay) {
                "Jadwal Hari $selectedDayName ($scheduledTodayTotal Siswa)"
              } else {
                "Semua Siswa Terdaftar (${allStudents.size} Siswa)"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (filterOnlyScheduledDay) OceanDeep else TextDarkPrimary
            )
            Text(
              text = if (filterOnlyScheduledDay) {
                if (scheduledTodayTotal > 0) "Menampilkan siswa terjadwal hari ini" else "Tidak ada jadwal rutin hari $selectedDayName"
              } else {
                "Filter harian dinonaktifkan (semua jadwal)"
              },
              fontSize = 10.sp,
              color = TextDarkSecondary
            )
          }
        }

        // Toggle Switch / Pill Button
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (filterOnlyScheduledDay) OceanDeep else SurfaceWhite,
          border = BorderStroke(
            1.dp,
            if (filterOnlyScheduledDay) OceanDeep else Color(0xFFCBD5E1)
          ),
          modifier = Modifier
            .clickable { viewModel.toggleAttendanceFilterOnlyScheduledDay() }
            .testTag("btn_toggle_schedule_filter")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (filterOnlyScheduledDay) Icons.Default.Check else Icons.Default.FilterList,
              contentDescription = null,
              tint = if (filterOnlyScheduledDay) OceanCyan else TextDarkSecondary,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (filterOnlyScheduledDay) "Khusus $selectedDayName" else "Semua Siswa",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (filterOnlyScheduledDay) Color.White else TextDarkSecondary
            )
          }
        }
      }
    }

    // Quick Stats Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      AbsensiStatCard(
        label = "Hadir",
        count = hadirCount,
        color = StatusLunasText,
        bgColor = Color(0xFFE6F7EE),
        modifier = Modifier.weight(1f)
      )
      AbsensiStatCard(
        label = "Izin",
        count = izinCount,
        color = StatusJatuhTempoText,
        bgColor = Color(0xFFFEF3C7),
        modifier = Modifier.weight(1f)
      )
      AbsensiStatCard(
        label = "Alpha",
        count = alphaCount,
        color = StatusBelumLunasText,
        bgColor = Color(0xFFFDE8E8),
        modifier = Modifier.weight(1f)
      )
      AbsensiStatCard(
        label = "Belum",
        count = belumCount,
        color = TextDarkSecondary,
        bgColor = Color(0xFFF1F5F9),
        modifier = Modifier.weight(1f)
      )
    }

    // Search & Class filter pills
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setAttendanceSearchQuery(it) },
        placeholder = { Text("Cari nama siswa di kelas...", fontSize = 12.sp, color = TextDarkTertiary) },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OceanAzure, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setAttendanceSearchQuery("") }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = TextDarkTertiary, modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = SurfaceWhite,
          unfocusedContainerColor = SurfaceWhite,
          focusedBorderColor = OceanBlue,
          unfocusedBorderColor = Color(0xFFE2EAF1)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("input_search_absensi")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Class Filter Pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        classFilters.forEach { cls ->
          val isSelected = selectedClass == cls
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) OceanBlue else SurfaceWhite)
              .border(1.dp, if (isSelected) OceanBlue else Color(0xFFD8E2EC), RoundedCornerShape(16.dp))
              .clickable { viewModel.setAttendanceClassFilter(cls) }
              .padding(horizontal = 10.dp, vertical = 4.dp)
              .testTag("absensi_class_filter_$cls")
          ) {
            Text(
              text = cls,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else TextDarkSecondary
            )
          }
        }
      }
    }

    // Student Attendance List or Empty State
    if (filteredStudents.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = null,
              tint = OceanBlue,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (scheduledTodayTotal == 0) {
                "Tidak Ada Jadwal Latihan Hari $selectedDayName"
              } else {
                "Tidak Ada Siswa Terdaftar"
              },
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = TextDarkPrimary,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (scheduledTodayTotal == 0) {
                "SAC Garut tidak memiliki jadwal latihan rutin pada hari $selectedDayName. Jadwal latihan aktif: Selasa, Kamis, Jum'at, Sabtu, dan Minggu."
              } else {
                "Tidak ada siswa yang sesuai dengan filter kelas '$selectedClass' pada hari $selectedDayName."
              },
              fontSize = 12.sp,
              color = TextDarkSecondary,
              textAlign = TextAlign.Center,
              lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
              onClick = {
                if (filterOnlyScheduledDay) {
                  viewModel.setAttendanceFilterOnlyScheduledDay(false)
                }
                viewModel.setAttendanceClassFilter("Semua")
                viewModel.setAttendanceSearchQuery("")
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("btn_reset_attendance_filters")
            ) {
              Text("Tampilkan Semua Siswa (${allStudents.size})")
            }
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("absensi_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(
          items = filteredStudents,
          key = { it.id }
        ) { student ->
          val currentStatus = dateAttendance[student.id] ?: AttendanceStatus.UNMARKED

          StudentAttendanceCard(
            student = student,
            currentStatus = currentStatus,
            onStatusChange = { newStatus ->
              viewModel.markAttendance(student.id, newStatus)
            },
            onClick = { onStudentClick(student) },
            onAvatarClick = onAvatarClick
          )
        }
      }
    }
  }
}

@Composable
fun AbsensiStatCard(
  label: String,
  count: Int,
  color: Color,
  bgColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor)
      .padding(vertical = 6.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "$count",
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color
      )
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color.copy(alpha = 0.85f)
      )
    }
  }
}

@Composable
fun StudentAttendanceCard(
  student: Student,
  currentStatus: AttendanceStatus,
  onStatusChange: (AttendanceStatus) -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onAvatarClick: ((Student) -> Unit)? = null
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("attendance_item_${student.id}")
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Photo Avatar placeholder with click zoom
        StudentAvatar(
          student = student,
          size = 42.dp,
          onClick = if (onAvatarClick != null) { { onAvatarClick(student) } } else null
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = student.namaLengkap,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            ClassBadge(kelas = student.kelas)
            Spacer(modifier = Modifier.width(6.dp))
            CoachBadge(coach = student.coach)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = OceanIce,
            border = BorderStroke(0.5.dp, OceanBlue.copy(alpha = 0.3f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = OceanBlue,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Jadwal: ${student.hariLatihan}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = OceanDeep
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Single-Tap Quick Attendance Buttons (Hadir, Izin, Alpha)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // HADIR
        AttendanceButton(
          label = "Hadir",
          isSelected = currentStatus == AttendanceStatus.HADIR,
          activeColor = StatusLunasText,
          activeBg = StatusLunasBg,
          activeBorder = StatusLunasBorder,
          icon = Icons.Default.Check,
          onClick = {
            onStatusChange(if (currentStatus == AttendanceStatus.HADIR) AttendanceStatus.UNMARKED else AttendanceStatus.HADIR)
          },
          modifier = Modifier.weight(1f)
        )

        // IZIN
        AttendanceButton(
          label = "Izin",
          isSelected = currentStatus == AttendanceStatus.IZIN,
          activeColor = StatusJatuhTempoText,
          activeBg = StatusJatuhTempoBg,
          activeBorder = StatusJatuhTempoBorder,
          icon = Icons.Default.Info,
          onClick = {
            onStatusChange(if (currentStatus == AttendanceStatus.IZIN) AttendanceStatus.UNMARKED else AttendanceStatus.IZIN)
          },
          modifier = Modifier.weight(1f)
        )

        // ALPHA
        AttendanceButton(
          label = "Alpha",
          isSelected = currentStatus == AttendanceStatus.ALPHA,
          activeColor = StatusBelumLunasText,
          activeBg = StatusBelumLunasBg,
          activeBorder = StatusBelumLunasBorder,
          icon = Icons.Default.Close,
          onClick = {
            onStatusChange(if (currentStatus == AttendanceStatus.ALPHA) AttendanceStatus.UNMARKED else AttendanceStatus.ALPHA)
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
fun AttendanceButton(
  label: String,
  isSelected: Boolean,
  activeColor: Color,
  activeBg: Color,
  activeBorder: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(36.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) activeBg else Color(0xFFF8FAFC))
      .border(
        width = if (isSelected) 1.5.dp else 1.dp,
        color = if (isSelected) activeBorder else Color(0xFFE2EAF1),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) activeColor else TextDarkTertiary,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) activeColor else TextDarkSecondary
      )
    }
  }
}
