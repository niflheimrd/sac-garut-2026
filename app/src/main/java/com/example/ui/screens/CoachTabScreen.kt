package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.FloatingActionButton
import com.example.data.model.CoachProfile
import com.example.data.model.Student
import com.example.ui.components.CoachAvatar
import com.example.ui.components.EditCoachModal
import com.example.ui.components.StudentAvatar
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
import com.example.ui.viewmodel.StudentViewModel

import androidx.compose.material.icons.filled.Add

@Composable
fun CoachTabScreen(
  viewModel: StudentViewModel,
  onStudentClick: (Student) -> Unit,
  modifier: Modifier = Modifier
) {
  val allStudents by viewModel.allStudents.collectAsState()
  val coaches by viewModel.coaches.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val studentToReassign by viewModel.studentToReassignCoach.collectAsState()
  val editingCoach by viewModel.editingCoachProfile.collectAsState()
  val showAddCoach by viewModel.showAddCoachDialog.collectAsState()

  var expandedCoachName by remember { mutableStateOf<String?>(coaches.firstOrNull()?.name) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
  ) {
    // Header Info Card
    Surface(
      color = SurfaceWhite,
      shadowElevation = 1.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(OceanIce),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Sports,
              contentDescription = null,
              tint = OceanBlue,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Daftar Tim Pelatih SAC Garut",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
            Text(
              text = "${coaches.size} Pelatih Terdaftar • Dapat Diedit",
              fontSize = 12.sp,
              color = TextDarkSecondary
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = { viewModel.setShowQrScannerModal(true) },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("btn_coach_scan_absensi")
          ) {
            Icon(
              imageVector = Icons.Default.QrCodeScanner,
              contentDescription = null,
              tint = OceanDeep,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OceanDeep)
          }

          Button(
            onClick = { viewModel.setShowAddCoachDialog(true) },
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("btn_header_add_coach")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("coach_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(coaches, key = { it.id }) { coach ->
          // Students who have this coach or whose class has this coach
          val directlyAssigned = allStudents.filter { it.coach.equals(coach.name, ignoreCase = true) }
          val classesInvolved = classes.filter { it.assignedCoaches.contains(coach.name) }
          val isExpanded = expandedCoachName == coach.name

          CoachCardItem(
            coach = coach,
            students = directlyAssigned,
            classesInvolved = classesInvolved.map { it.name },
            isExpanded = isExpanded,
            onToggleExpand = {
              expandedCoachName = if (isExpanded) null else coach.name
            },
            onEditCoach = {
              viewModel.setEditingCoachProfile(coach)
            },
            onReassignStudent = { student ->
              viewModel.setStudentToReassignCoach(student)
            },
            onStudentClick = onStudentClick
          )
        }
      }

      FloatingActionButton(
        onClick = { viewModel.setShowAddCoachDialog(true) },
        containerColor = OceanBlue,
        contentColor = Color.White,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
          .testTag("fab_add_coach")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Tambah Coach Baru",
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }

  // Edit Coach Modal
  editingCoach?.let { cch ->
    EditCoachModal(
      coach = cch,
      onDismiss = { viewModel.setEditingCoachProfile(null) },
      onSave = { updatedCoach, oldName ->
        viewModel.saveCoachProfile(updatedCoach, oldName)
      },
      isNewCoach = false
    )
  }

  // Add New Coach Modal
  if (showAddCoach) {
    val newCoachTemplate = remember {
      CoachProfile(
        id = "cch-${System.currentTimeMillis()}",
        name = "",
        role = "Pelatih",
        phone = "",
        bio = ""
      )
    }
    EditCoachModal(
      coach = newCoachTemplate,
      onDismiss = { viewModel.setShowAddCoachDialog(false) },
      onSave = { newCoach, _ ->
        viewModel.addCoachProfile(newCoach)
        viewModel.setShowAddCoachDialog(false)
      },
      isNewCoach = true
    )
  }

  // Reassign Coach Dialog
  studentToReassign?.let { student ->
    ReassignCoachDialog(
      student = student,
      availableCoaches = coaches.map { it.name },
      onDismiss = { viewModel.setStudentToReassignCoach(null) },
      onConfirm = { newCoach ->
        viewModel.reassignCoach(student.id, newCoach)
      }
    )
  }
}

@Composable
fun CoachCardItem(
  coach: CoachProfile,
  students: List<Student>,
  classesInvolved: List<String>,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  onEditCoach: () -> Unit,
  onReassignStudent: (Student) -> Unit,
  onStudentClick: (Student) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header Section
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onToggleExpand)
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CoachAvatar(
          coach = coach,
          size = 50.dp,
          onClick = onEditCoach
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .clickable(onClick = onEditCoach)
        ) {
          Text(
            text = coach.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDarkPrimary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = coach.role.ifBlank { "Pelatih Renang" },
            fontSize = 11.sp,
            color = TextDarkTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (coach.phone.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = TextDarkTertiary,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = coach.phone,
                fontSize = 11.sp,
                color = TextDarkSecondary
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(OceanIce)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${students.size} Siswa Bimbingan",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OceanBlue
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEditCoach,
            modifier = Modifier.testTag("btn_edit_coach_${coach.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Coach",
              tint = OceanBlue,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(onClick = onToggleExpand) {
            Icon(
              imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = null,
              tint = TextDarkSecondary
            )
          }
        }
      }

      // Classes assigned info
      if (classesInvolved.isNotEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = "Ditugaskan di level: " + classesInvolved.joinToString(", "),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = OceanDeep
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
      }

      // Expanded Student Roster
      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FBFC))
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          if (coach.bio.isNotBlank()) {
            Text(
              text = coach.bio,
              fontSize = 12.sp,
              color = TextDarkSecondary,
              modifier = Modifier.padding(bottom = 8.dp)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Daftar Siswa Bimbingan (${students.size} anak):",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
            OutlinedButton(
              onClick = onEditCoach,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = OceanBlue)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Edit Profil Coach", fontSize = 11.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          students.forEach { student ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceWhite)
                .border(1.dp, Color(0xFFE2EAF1), RoundedCornerShape(10.dp))
                .clickable { onStudentClick(student) }
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              StudentAvatar(student = student, size = 36.dp)
              Spacer(modifier = Modifier.width(10.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = student.namaLengkap,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextDarkPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${student.kelas} • ${student.hariLatihan}",
                  fontSize = 11.sp,
                  color = TextDarkSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              OutlinedButton(
                onClick = { onReassignStudent(student) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_reassign_${student.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = null,
                  modifier = Modifier.size(12.dp),
                  tint = OceanBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Ganti",
                  fontSize = 11.sp,
                  color = OceanBlue,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReassignCoachDialog(
  student: Student,
  availableCoaches: List<String>,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var selectedCoach by remember { mutableStateOf(student.coach) }
  var expanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SurfaceWhite,
    shape = RoundedCornerShape(20.dp),
    title = {
      Text(
        text = "Ganti Pelatih / Coach",
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = OceanDeep
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Pilih pelatih baru untuk ananda:",
          fontSize = 12.sp,
          color = TextDarkSecondary
        )
        Text(
          text = student.namaLengkap,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = TextDarkPrimary
        )
        Text(
          text = "Kelas: ${student.kelas} • ${student.hariLatihan}",
          fontSize = 12.sp,
          color = OceanAzure
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Pilih Coach dari Dropdown",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = OceanDeep
        )
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = it }
        ) {
          OutlinedTextField(
            value = selectedCoach,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
              .menuAnchor(MenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
          ) {
            availableCoaches.forEach { coach ->
              DropdownMenuItem(
                text = { Text(coach, fontWeight = if (coach == selectedCoach) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                  selectedCoach = coach
                  expanded = false
                }
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(selectedCoach) },
        colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_confirm_reassign")
      ) {
        Text("Simpan Coach", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal", color = TextDarkSecondary)
      }
    }
  )
}
