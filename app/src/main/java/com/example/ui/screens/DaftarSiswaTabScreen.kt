package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.StudentCard
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanIce
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.TextDarkTertiary
import com.example.ui.viewmodel.StudentViewModel

@Composable
fun DaftarSiswaTabScreen(
  viewModel: StudentViewModel,
  onStudentClick: (Student) -> Unit,
  modifier: Modifier = Modifier,
  onAvatarClick: ((Student) -> Unit)? = null
) {
  val allStudents by viewModel.allStudents.collectAsState()
  val filteredStudents by viewModel.filteredStudents.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedClass by viewModel.selectedClass.collectAsState()
  val selectedDay by viewModel.selectedDay.collectAsState()
  val classCounts by viewModel.classCounts.collectAsState()

  val classTabs = remember(classes) { listOf("Semua") + classes.map { it.name } }
  val dayFilters = listOf("Semua", "Selasa", "Kamis", "Jum'at", "Sabtu", "Minggu")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
  ) {
    // Search Bar
    Surface(
      color = SurfaceWhite,
      shadowElevation = 1.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = {
            Text(
              text = "Cari siswa (nama, coach, atau asal sekolah)...",
              color = TextDarkTertiary,
              fontSize = 13.sp
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Cari",
              tint = OceanAzure,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Hapus",
                  tint = TextDarkTertiary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8FAFC),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedBorderColor = OceanBlue,
            unfocusedBorderColor = Color(0xFFE2EAF1)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_bar_daftar_siswa")
        )
      }
    }

    // Class Tabs: Scrollable (NO payment tabs here)
    val selectedTabIndex = classTabs.indexOf(selectedClass).coerceAtLeast(0)
    ScrollableTabRow(
      selectedTabIndex = selectedTabIndex,
      containerColor = SurfaceWhite,
      contentColor = OceanDeep,
      edgePadding = 16.dp,
      indicator = { tabPositions ->
        if (selectedTabIndex < tabPositions.size) {
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
            height = 3.dp,
            color = OceanBlue
          )
        }
      },
      divider = {}
    ) {
      classTabs.forEachIndexed { index, title ->
        val isSelected = selectedClass == title
        val count = classCounts[title] ?: 0

        Tab(
          selected = isSelected,
          onClick = { viewModel.setSelectedClass(title) },
          modifier = Modifier.testTag("tab_class_$title")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
          ) {
            Text(
              text = title,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) OceanDeep else TextDarkSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) OceanBlue else Color(0xFFF1F5F9))
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "$count",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextDarkSecondary
              )
            }
          }
        }
      }
    }

    // Hari Latihan Filter Pills (NO payment pills here)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(BackgroundLight)
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Hari:",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextDarkTertiary
      )

      dayFilters.forEach { day ->
        val isSelected = selectedDay == day
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) OceanDeep else SurfaceWhite)
            .border(
              width = 1.dp,
              color = if (isSelected) OceanDeep else Color(0xFFD8E2EC),
              shape = RoundedCornerShape(20.dp)
            )
            .clickable { viewModel.setSelectedDay(day) }
            .padding(horizontal = 11.dp, vertical = 6.dp)
            .testTag("filter_day_$day")
        ) {
          Text(
            text = day,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextDarkSecondary
          )
        }
      }
    }

    // Results Summary & Reset
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Menampilkan ${filteredStudents.size} dari ${allStudents.size} siswa",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDarkSecondary
      )

      if (searchQuery.isNotEmpty() || selectedClass != "Semua" || selectedDay != "Semua") {
        TextButton(
          onClick = {
            viewModel.setSearchQuery("")
            viewModel.setSelectedClass("Semua")
            viewModel.setSelectedDay("Semua")
          },
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text("Reset Filter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OceanAzure)
        }
      }
    }

    // Student List
    if (isLoading) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = OceanBlue)
      }
    } else if (filteredStudents.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(OceanIce),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(32.dp))
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Siswa Tidak Ditemukan",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Coba ubah kata kunci atau filter kelas.",
            fontSize = 12.sp,
            color = TextDarkSecondary,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("daftar_siswa_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(
          items = filteredStudents,
          key = { it.id }
        ) { student ->
          // showPaymentBadge = false (Requirement 2: Strictly NO payment badge on main list)
          StudentCard(
            student = student,
            onClick = { onStudentClick(student) },
            onWhatsAppClick = {},
            classes = classes,
            showPaymentBadge = false,
            onAvatarClick = onAvatarClick
          )
        }
      }
    }
  }
}
