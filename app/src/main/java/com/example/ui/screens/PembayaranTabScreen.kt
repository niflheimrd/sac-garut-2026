package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.ClassBadge
import com.example.ui.components.CoachBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.BackgroundLight
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
import com.example.ui.viewmodel.StudentViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PembayaranTabScreen(
  viewModel: StudentViewModel,
  onStudentClick: (Student) -> Unit,
  modifier: Modifier = Modifier,
  onAvatarClick: ((Student) -> Unit)? = null
) {
  val context = LocalContext.current
  val allStudents by viewModel.allStudents.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val filterStatus by viewModel.paymentFilterStatus.collectAsState()
  val filterClass by viewModel.paymentClassFilter.collectAsState()
  val searchQuery by viewModel.paymentSearchQuery.collectAsState()
  val selectedMonth by viewModel.selectedPaymentMonth.collectAsState()

  var monthMenuExpanded by remember { mutableStateOf(false) }
  val availableMonths = listOf(
    "Oktober 2026",
    "November 2026",
    "Desember 2026",
    "Januari 2027",
    "Februari 2027"
  )

  // Standard Monthly Tuition Fee per Student at SAC Garut
  val monthlyFeePerStudent = 350_000L

  val total = allStudents.size
  val lunasCount = allStudents.count { it.statusPembayaran.equals("Lunas", ignoreCase = true) }
  val belumLunasCount = allStudents.count { it.statusPembayaran.equals("Belum Lunas", ignoreCase = true) }
  val jatuhTempoCount = allStudents.count { it.statusPembayaran.equals("Jatuh Tempo", ignoreCase = true) }
  val lunasRatio = if (total > 0) lunasCount.toFloat() / total else 0f

  val totalCollectedRevenue = lunasCount * monthlyFeePerStudent
  val totalOutstandingDebt = (belumLunasCount + jatuhTempoCount) * monthlyFeePerStudent

  val indonesianLocale = Locale("id", "ID")
  val currencyFormat = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
    maximumFractionDigits = 0
  }
  val formattedRevenue = currencyFormat.format(totalCollectedRevenue).replace("Rp", "Rp ")
  val formattedDebt = currencyFormat.format(totalOutstandingDebt).replace("Rp", "Rp ")

  val paymentFilterOptions = listOf(
    Triple("Semua", "Semua ($total)", OceanDeep),
    Triple("Lunas", "Lunas ($lunasCount)", StatusLunasText),
    Triple("Belum Lunas", "Belum Lunas (${belumLunasCount + jatuhTempoCount})", StatusBelumLunasText)
  )

  val classOptions = remember(classes) { listOf("Semua") + classes.map { it.name } }

  val filteredStudents = remember(allStudents, filterStatus, filterClass, searchQuery) {
    allStudents.filter { student ->
      val matchesStatus = when (filterStatus) {
        "Semua" -> true
        "Lunas" -> student.statusPembayaran.equals("Lunas", ignoreCase = true)
        "Belum Lunas" -> !student.statusPembayaran.equals("Lunas", ignoreCase = true)
        else -> student.statusPembayaran.equals(filterStatus, ignoreCase = true)
      }
      val matchesClass = filterClass == "Semua" || student.kelas.equals(filterClass, ignoreCase = true)
      val matchesQuery = searchQuery.isBlank() ||
          student.namaLengkap.contains(searchQuery.trim(), ignoreCase = true) ||
          student.coach.contains(searchQuery.trim(), ignoreCase = true) ||
          student.id.contains(searchQuery.trim(), ignoreCase = true)
      matchesStatus && matchesClass && matchesQuery
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
  ) {
    // Top Dashboard Summary Card with Month/Year Selector
    Surface(
      color = SurfaceWhite,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Month Selector Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(OceanIce),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = OceanBlue,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Rekap Keuangan Bulanan",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OceanDeep
              )
              Text(
                text = "Iuran les renang siswa SAC Garut",
                fontSize = 11.sp,
                color = TextDarkSecondary
              )
            }
          }

          // Interactive Month/Year Dropdown Selector
          Box {
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, OceanBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .clickable { monthMenuExpanded = true }
                .testTag("dropdown_month_selector"),
              color = OceanIce
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = OceanBlue,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = selectedMonth,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = OceanDeep
                )
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = null,
                  tint = OceanBlue,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            DropdownMenu(
              expanded = monthMenuExpanded,
              onDismissRequest = { monthMenuExpanded = false }
            ) {
              availableMonths.forEach { month ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = month,
                      fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                      color = if (month == selectedMonth) OceanBlue else TextDarkPrimary
                    )
                  },
                  onClick = {
                    viewModel.setSelectedPaymentMonth(month)
                    monthMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Dashboard Financial Summary Cards (Collected Revenue & Outstanding Debt)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Total Collected Revenue Card
          Card(
            modifier = Modifier
              .weight(1f)
              .testTag("card_total_revenue"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = StatusLunasText,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Total Masuk (Lunas)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = StatusLunasText
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = formattedRevenue,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StatusLunasText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "$lunasCount Siswa Lunas",
                fontSize = 10.sp,
                color = TextDarkSecondary
              )
            }
          }

          // Total Outstanding Debt Card
          Card(
            modifier = Modifier
              .weight(1f)
              .testTag("card_total_debt"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.HourglassTop,
                  contentDescription = null,
                  tint = StatusBelumLunasText,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Tunggakan / Belum",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = StatusBelumLunasText
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = formattedDebt,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StatusBelumLunasText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${belumLunasCount + jatuhTempoCount} Siswa Belum Lunas",
                fontSize = 10.sp,
                color = TextDarkSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Payment Progress Bar (% of students paid)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Progress Pembayaran $selectedMonth:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDarkPrimary
          )
          Text(
            text = "${(lunasRatio * 100).toInt()}% Selesai ($lunasCount / $total)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = StatusLunasText
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
          progress = { lunasRatio },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = StatusLunasText,
          trackColor = Color(0xFFE2EAF1),
        )
      }
    }

    // Filter Chips: "All", "Paid", "Unpaid" + Search Bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setPaymentSearchQuery(it) },
        placeholder = { Text("Cari nama siswa, coach, atau ID...", fontSize = 12.sp, color = TextDarkTertiary) },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OceanAzure, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setPaymentSearchQuery("") }) {
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
          .testTag("input_search_pembayaran")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Tab/Filter Chips: "All", "Paid", "Unpaid"
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        paymentFilterOptions.forEach { (key, label, accentColor) ->
          val isSelected = filterStatus == key
          val bg = when (key) {
            "Lunas" -> Color(0xFFE6F7EE)
            "Belum Lunas" -> Color(0xFFFDE8E8)
            else -> Color(0xFFE2ECF4)
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) bg else SurfaceWhite)
              .border(1.5.dp, if (isSelected) accentColor else Color(0xFFD8E2EC), RoundedCornerShape(16.dp))
              .clickable { viewModel.setPaymentFilterStatus(key) }
              .padding(horizontal = 14.dp, vertical = 7.dp)
              .testTag("filter_chip_$key")
          ) {
            Text(
              text = label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) accentColor else TextDarkSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Class Filter Pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Kelas:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDarkTertiary)
        classOptions.forEach { cls ->
          val isSelected = filterClass == cls
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(if (isSelected) OceanBlue else SurfaceWhite)
              .border(1.dp, if (isSelected) OceanBlue else Color(0xFFD8E2EC), RoundedCornerShape(14.dp))
              .clickable { viewModel.setPaymentClassFilter(cls) }
              .padding(horizontal = 9.dp, vertical = 4.dp)
          ) {
            Text(
              text = cls,
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else TextDarkSecondary
            )
          }
        }
      }
    }

    // Results Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Menampilkan ${filteredStudents.size} siswa ($selectedMonth)",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDarkSecondary
      )
    }

    // List of Students with 1-tap Status Switcher and WhatsApp Action
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("pembayaran_lazy_column"),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(
        items = filteredStudents,
        key = { it.id }
      ) { student ->
        PaymentStudentCard(
          student = student,
          onStatusChange = { newStatus ->
            viewModel.updatePaymentStatus(student.id, newStatus)
          },
          onClick = { onStudentClick(student) },
          onWhatsAppReminder = {
            openWhatsAppPaymentReminder(context, student)
          },
          onAvatarClick = onAvatarClick
        )
      }
    }
  }
}

@Composable
fun PaymentStudentCard(
  student: Student,
  onStatusChange: (String) -> Unit,
  onClick: () -> Unit,
  onWhatsAppReminder: () -> Unit,
  modifier: Modifier = Modifier,
  onAvatarClick: ((Student) -> Unit)? = null
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        StudentAvatar(
          student = student,
          size = 44.dp,
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
        }

        // WhatsApp Reminder Button
        Surface(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(onClick = onWhatsAppReminder),
          color = Color(0xFFE7F9ED),
          shape = CircleShape
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = "Kirim Pengingat WA",
              tint = WhatsAppGreen,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Current Status and 1-tap Status Switcher Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Lunas Button
        PaymentStatusSelectButton(
          label = "Lunas",
          isSelected = student.statusPembayaran.equals("Lunas", ignoreCase = true),
          activeColor = StatusLunasText,
          activeBg = StatusLunasBg,
          activeBorder = StatusLunasBorder,
          onClick = { onStatusChange("Lunas") },
          modifier = Modifier.weight(1f)
        )

        // Belum Lunas Button
        PaymentStatusSelectButton(
          label = "Belum Lunas",
          isSelected = student.statusPembayaran.equals("Belum Lunas", ignoreCase = true),
          activeColor = StatusBelumLunasText,
          activeBg = StatusBelumLunasBg,
          activeBorder = StatusBelumLunasBorder,
          onClick = { onStatusChange("Belum Lunas") },
          modifier = Modifier.weight(1f)
        )

        // Jatuh Tempo Button
        PaymentStatusSelectButton(
          label = "Jatuh Tempo",
          isSelected = student.statusPembayaran.equals("Jatuh Tempo", ignoreCase = true),
          activeColor = StatusJatuhTempoText,
          activeBg = StatusJatuhTempoBg,
          activeBorder = StatusJatuhTempoBorder,
          onClick = { onStatusChange("Jatuh Tempo") },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
fun PaymentStatusSelectButton(
  label: String,
  isSelected: Boolean,
  activeColor: Color,
  activeBg: Color,
  activeBorder: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(34.dp)
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
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) activeColor else TextDarkSecondary
    )
  }
}

fun openWhatsAppPaymentReminder(context: Context, student: Student) {
  com.example.util.WhatsAppHelper.sendPaymentReminder(context, student)
}
