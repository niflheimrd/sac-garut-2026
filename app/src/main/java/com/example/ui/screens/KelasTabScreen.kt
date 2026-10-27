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
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassCategory
import com.example.data.model.Student
import com.example.ui.components.ClassBadge
import com.example.ui.components.EditClassModal
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

@Composable
fun KelasTabScreen(
  viewModel: StudentViewModel,
  onStudentClick: (Student) -> Unit,
  modifier: Modifier = Modifier
) {
  val allStudents by viewModel.allStudents.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val coaches by viewModel.coaches.collectAsState()
  val editingClass by viewModel.editingClassCategory.collectAsState()

  var expandedClassId by remember { mutableStateOf<String?>(classes.firstOrNull()?.id) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
  ) {
    // Header Info Banner
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
              imageVector = Icons.Default.Pool,
              contentDescription = null,
              tint = OceanBlue,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Kategori & Program Kelas",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
            Text(
              text = "${classes.size} Level Durasi • 3 Coach per Kelas",
              fontSize = 12.sp,
              color = TextDarkSecondary
            )
          }
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("kelas_lazy_column"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(classes, key = { it.id }) { cat ->
        val studentsInClass = allStudents.filter {
          it.kelas.equals(cat.name, ignoreCase = true) || it.kelas.equals(cat.shortName, ignoreCase = true)
        }
        val isExpanded = expandedClassId == cat.id

        ClassCategoryCard(
          category = cat,
          students = studentsInClass,
          isExpanded = isExpanded,
          onToggleExpand = {
            expandedClassId = if (isExpanded) null else cat.id
          },
          onEditClick = {
            viewModel.setEditingClassCategory(cat)
          },
          onStudentClick = onStudentClick
        )
      }
    }
  }

  // Edit Class Modal
  editingClass?.let { catToEdit ->
    EditClassModal(
      category = catToEdit,
      availableCoaches = coaches,
      onDismiss = { viewModel.setEditingClassCategory(null) },
      onSave = { updated ->
        viewModel.saveClassCategory(updated)
      }
    )
  }
}

@Composable
fun ClassCategoryCard(
  category: ClassCategory,
  students: List<Student>,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  onEditClick: () -> Unit,
  onStudentClick: (Student) -> Unit,
  modifier: Modifier = Modifier
) {
  val badgeColor = when {
    category.name.contains("Prestasi") -> Color(0xFFD97706)
    category.name.contains("Moderat") -> Color(0xFF0369A1)
    category.name.contains("Basic") -> Color(0xFF0D9488)
    category.name.contains("Pemula") -> Color(0xFF7C3AED)
    else -> OceanBlue
  }

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
        Box(
          modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(badgeColor.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = category.shortName.take(2).uppercase(),
            color = badgeColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = category.name,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = TextDarkPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Durasi: ${category.duration}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = OceanAzure
          )

          Spacer(modifier = Modifier.height(4.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(badgeColor.copy(alpha = 0.12f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${students.size} Siswa Terdaftar",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = badgeColor
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEditClick,
            modifier = Modifier.testTag("btn_edit_class_${category.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Kelas",
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

      // Display the 3 Assigned Coaches Bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(OceanIce)
          .padding(10.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Sports,
              contentDescription = null,
              tint = OceanBlue,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "TIM 3 COACH KELAS:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            CoachPill("Utama", category.coachUtama, Modifier.weight(1f))
            CoachPill("Asisten 1", category.asistenCoach1, Modifier.weight(1f))
            CoachPill("Asisten 2", category.asistenCoach2, Modifier.weight(1f))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Expanded student list and description
      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FBFC))
            .padding(16.dp)
        ) {
          if (category.targetFocus.isNotBlank()) {
            Text(
              text = "Target Pembelajaran & Fokus:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )
            Text(
              text = category.targetFocus,
              fontSize = 12.sp,
              color = TextDarkSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          if (category.description.isNotBlank()) {
            Text(
              text = category.description,
              fontSize = 12.sp,
              color = TextDarkSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Daftar Siswa (${students.size} anak):",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = OceanDeep
            )

            OutlinedButton(
              onClick = onEditClick,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = OceanBlue)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Edit Detail Kelas", fontSize = 11.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          students.forEach { student ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
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
                  text = "${student.hariLatihan} • ${student.jamLatihan}",
                  fontSize = 11.sp,
                  color = TextDarkSecondary
                )
              }
              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextDarkTertiary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CoachPill(role: String, name: String, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(SurfaceWhite)
      .border(1.dp, Color(0xFFE2EAF1), RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 4.dp)
  ) {
    Column {
      Text(
        text = role,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = OceanAzure
      )
      Text(
        text = name.ifBlank { "-" },
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDarkPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
