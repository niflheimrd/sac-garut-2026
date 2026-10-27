package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassCategory
import com.example.data.model.CoachProfile
import com.example.data.model.Student
import com.example.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MainTab(val title: String, val subtitle: String) {
  ABSENSI("Absensi", "Presensi & Kehadiran"),
  DAFTAR_SISWA("Daftar Siswa", "Direktori Siswa"),
  COACH("Coach", "Tim & Manajemen Pelatih"),
  KELAS("Kelas", "Level & Program Latihan"),
  PEMBAYARAN("Pembayaran", "Status Administrasi")
}

enum class SortOption(val label: String) {
  NAME_ASC("Nama (A-Z)"),
  NAME_DESC("Nama (Z-A)"),
  CLASS("Kelas"),
  COACH("Coach")
}

class StudentViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = StudentRepository(application.applicationContext)

  val allStudents: StateFlow<List<Student>> = repository.students
  val classes: StateFlow<List<ClassCategory>> = repository.classes
  val coaches: StateFlow<List<CoachProfile>> = repository.coaches
  val isLoading: StateFlow<Boolean> = repository.isLoading
  val attendanceMap: StateFlow<Map<String, Map<String, AttendanceStatus>>> = repository.attendance
  val parentSurveys: StateFlow<List<com.example.data.model.SurveyModel>> = repository.parentSurveys
  val isCloudSyncActive: StateFlow<Boolean> = repository.isCloudSyncActive

  // Active top navigation tab
  private val _currentTab = MutableStateFlow(MainTab.ABSENSI)
  val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

  // 1. Search & Filter for "Daftar Siswa"
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedClass = MutableStateFlow("Semua")
  val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

  private val _selectedDay = MutableStateFlow("Semua")
  val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

  private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
  val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

  // 2. Attendance (Absensi) State
  private val todayStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  private val _selectedAttendanceDate = MutableStateFlow(todayStr)
  val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()

  private val _attendanceClassFilter = MutableStateFlow("Semua")
  val attendanceClassFilter: StateFlow<String> = _attendanceClassFilter.asStateFlow()

  private val _attendanceSearchQuery = MutableStateFlow("")
  val attendanceSearchQuery: StateFlow<String> = _attendanceSearchQuery.asStateFlow()

  // Daily Schedule Attendance Filter (Default: true, only show students scheduled for the day)
  private val _attendanceFilterOnlyScheduledDay = MutableStateFlow(true)
  val attendanceFilterOnlyScheduledDay: StateFlow<Boolean> = _attendanceFilterOnlyScheduledDay.asStateFlow()

  // 3. Coach Tab State
  private val _coachSearchQuery = MutableStateFlow("")
  val coachSearchQuery: StateFlow<String> = _coachSearchQuery.asStateFlow()

  private val _studentToReassignCoach = MutableStateFlow<Student?>(null)
  val studentToReassignCoach: StateFlow<Student?> = _studentToReassignCoach.asStateFlow()

  // 4. Pembayaran Tab State
  private val _paymentFilterStatus = MutableStateFlow("Semua")
  val paymentFilterStatus: StateFlow<String> = _paymentFilterStatus.asStateFlow()

  private val _paymentClassFilter = MutableStateFlow("Semua")
  val paymentClassFilter: StateFlow<String> = _paymentClassFilter.asStateFlow()

  private val _paymentSearchQuery = MutableStateFlow("")
  val paymentSearchQuery: StateFlow<String> = _paymentSearchQuery.asStateFlow()

  private val _selectedPaymentMonth = MutableStateFlow("Oktober 2026")
  val selectedPaymentMonth: StateFlow<String> = _selectedPaymentMonth.asStateFlow()

  // 5. Raport Module State
  private val _selectedRaportStudent = MutableStateFlow<Student?>(null)
  val selectedRaportStudent: StateFlow<Student?> = _selectedRaportStudent.asStateFlow()

  // Management Settings modals
  private val _editingClassCategory = MutableStateFlow<ClassCategory?>(null)
  val editingClassCategory: StateFlow<ClassCategory?> = _editingClassCategory.asStateFlow()

  private val _editingCoachProfile = MutableStateFlow<CoachProfile?>(null)
  val editingCoachProfile: StateFlow<CoachProfile?> = _editingCoachProfile.asStateFlow()

  private val _showManageClasses = MutableStateFlow(false)
  val showManageClasses: StateFlow<Boolean> = _showManageClasses.asStateFlow()

  private val _showManageCoaches = MutableStateFlow(false)
  val showManageCoaches: StateFlow<Boolean> = _showManageCoaches.asStateFlow()

  // Selected student for detail sheet
  private val _selectedStudent = MutableStateFlow<Student?>(null)
  val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

  // Dialog & Sheet states
  private val _editingStudent = MutableStateFlow<Student?>(null)
  val editingStudent: StateFlow<Student?> = _editingStudent.asStateFlow()

  private val _showAddDialog = MutableStateFlow(false)
  val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

  private val _showStatsSheet = MutableStateFlow(false)
  val showStatsSheet: StateFlow<Boolean> = _showStatsSheet.asStateFlow()

  // Student Photo Zoom Preview modal state
  private val _zoomedStudent = MutableStateFlow<Student?>(null)
  val zoomedStudent: StateFlow<Student?> = _zoomedStudent.asStateFlow()

  // Add Coach Dialog state
  private val _showAddCoachDialog = MutableStateFlow(false)
  val showAddCoachDialog: StateFlow<Boolean> = _showAddCoachDialog.asStateFlow()

  // QR Barcode Scanner & Card Viewer state
  private val _showQrScannerModal = MutableStateFlow(false)
  val showQrScannerModal: StateFlow<Boolean> = _showQrScannerModal.asStateFlow()

  private val _studentForQrCard = MutableStateFlow<Student?>(null)
  val studentForQrCard: StateFlow<Student?> = _studentForQrCard.asStateFlow()

  // Filtered students for "Daftar Siswa"
  private data class DirectoryFilter(
    val query: String,
    val kelas: String,
    val day: String
  )

  private val directoryFilter = combine(
    _searchQuery,
    _selectedClass,
    _selectedDay
  ) { query, kelas, day ->
    DirectoryFilter(query, kelas, day)
  }

  val filteredStudents: StateFlow<List<Student>> = combine(
    allStudents,
    directoryFilter,
    _sortOption
  ) { list, filter, sort ->
    var result = list

    if (filter.kelas != "Semua") {
      result = result.filter { it.kelas.equals(filter.kelas, ignoreCase = true) }
    }

    if (filter.day != "Semua") {
      result = result.filter { it.hariLatihan.contains(filter.day, ignoreCase = true) }
    }

    if (filter.query.isNotBlank()) {
      val q = filter.query.trim().lowercase()
      result = result.filter { student ->
        student.namaLengkap.lowercase().contains(q) ||
            student.coach.lowercase().contains(q) ||
            student.asalSekolah.lowercase().contains(q) ||
            student.namaOrangTua.lowercase().contains(q) ||
            student.id.lowercase().contains(q)
      }
    }

    when (sort) {
      SortOption.NAME_ASC -> result.sortedBy { it.namaLengkap.lowercase() }
      SortOption.NAME_DESC -> result.sortedByDescending { it.namaLengkap.lowercase() }
      SortOption.CLASS -> result.sortedWith(
        compareBy<Student> { it.kelas }.thenBy { it.namaLengkap.lowercase() }
      )
      SortOption.COACH -> result.sortedWith(
        compareBy<Student> { it.coach }.thenBy { it.namaLengkap.lowercase() }
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dynamic class counts derived from current classes
  val classCounts: StateFlow<Map<String, Int>> = combine(allStudents, classes, _selectedDay) { list, classList, day ->
    val baseList = if (day == "Semua") list else list.filter { it.hariLatihan.contains(day, ignoreCase = true) }
    val map = mutableMapOf("Semua" to baseList.size)
    classList.forEach { cls ->
      map[cls.name] = baseList.count { it.kelas.equals(cls.name, ignoreCase = true) }
    }
    map
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  fun setCurrentTab(tab: MainTab) {
    _currentTab.value = tab
  }

  // Directory Actions
  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedClass(kelas: String) {
    _selectedClass.value = kelas
  }

  fun setSelectedDay(day: String) {
    _selectedDay.value = day
  }

  fun setSortOption(option: SortOption) {
    _sortOption.value = option
  }

  // Attendance Actions
  fun setAttendanceDate(date: String) {
    _selectedAttendanceDate.value = date
  }

  fun setAttendanceClassFilter(kelas: String) {
    _attendanceClassFilter.value = kelas
  }

  fun setAttendanceSearchQuery(query: String) {
    _attendanceSearchQuery.value = query
  }

  fun setAttendanceFilterOnlyScheduledDay(enabled: Boolean) {
    _attendanceFilterOnlyScheduledDay.value = enabled
  }

  fun toggleAttendanceFilterOnlyScheduledDay() {
    _attendanceFilterOnlyScheduledDay.value = !_attendanceFilterOnlyScheduledDay.value
  }

  fun getDayNameFromDate(dateStr: String): String {
    return try {
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      val date = sdf.parse(dateStr) ?: return ""
      val cal = Calendar.getInstance()
      cal.time = date
      when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Minggu"
        Calendar.MONDAY -> "Senin"
        Calendar.TUESDAY -> "Selasa"
        Calendar.WEDNESDAY -> "Selasa" // Rabu shifted to Selasa
        Calendar.THURSDAY -> "Kamis"
        Calendar.FRIDAY -> "Jum'at"
        Calendar.SATURDAY -> "Sabtu"
        else -> ""
      }
    } catch (e: Exception) {
      ""
    }
  }

  fun isStudentScheduledOnDay(student: Student, dayName: String): Boolean {
    if (dayName.isBlank()) return true
    val cleanDay = dayName.lowercase().replace("'", "")
    val studentDays = student.hariLatihan.lowercase().replace("'", "")
    return studentDays.contains(cleanDay)
  }

  fun markAttendance(studentId: String, status: AttendanceStatus) {
    val date = _selectedAttendanceDate.value
    repository.setAttendance(date, studentId, status)
  }

  fun markAllAttendance(status: AttendanceStatus, targetStudents: List<Student>) {
    val date = _selectedAttendanceDate.value
    targetStudents.forEach { student ->
      repository.setAttendance(date, student.id, status)
    }
  }

  // Coach Actions
  fun setCoachSearchQuery(query: String) {
    _coachSearchQuery.value = query
  }

  fun setStudentToReassignCoach(student: Student?) {
    _studentToReassignCoach.value = student
  }

  fun reassignCoach(studentId: String, newCoach: String) {
    repository.assignCoach(studentId, newCoach)
    _studentToReassignCoach.value = null
    if (_selectedStudent.value?.id == studentId) {
      _selectedStudent.value = _selectedStudent.value?.copy(coach = newCoach)
    }
  }

  // Class Management Actions
  fun setEditingClassCategory(category: ClassCategory?) {
    _editingClassCategory.value = category
  }

  fun setShowManageClasses(show: Boolean) {
    _showManageClasses.value = show
  }

  fun saveClassCategory(category: ClassCategory) {
    repository.updateClassCategory(category)
    _editingClassCategory.value = null
  }

  fun addClassCategory(category: ClassCategory) {
    repository.addClassCategory(category)
  }

  fun deleteClassCategory(id: String) {
    repository.deleteClassCategory(id)
  }

  // Coach Management Actions
  fun setEditingCoachProfile(coach: CoachProfile?) {
    _editingCoachProfile.value = coach
  }

  fun setShowManageCoaches(show: Boolean) {
    _showManageCoaches.value = show
  }

  fun saveCoachProfile(coach: CoachProfile, oldName: String?) {
    repository.updateCoachProfile(coach, oldName)
    _editingCoachProfile.value = null
  }

  fun addCoachProfile(coach: CoachProfile) {
    repository.addCoachProfile(coach)
  }

  fun deleteCoachProfile(id: String) {
    repository.deleteCoachProfile(id)
  }

  // Pembayaran Actions
  fun setPaymentFilterStatus(status: String) {
    _paymentFilterStatus.value = status
  }

  fun setPaymentClassFilter(kelas: String) {
    _paymentClassFilter.value = kelas
  }

  fun setPaymentSearchQuery(query: String) {
    _paymentSearchQuery.value = query
  }

  fun updatePaymentStatus(studentId: String, newStatus: String) {
    repository.updatePaymentStatus(studentId, newStatus)
    if (_selectedStudent.value?.id == studentId) {
      _selectedStudent.value = _selectedStudent.value?.copy(statusPembayaran = newStatus)
    }
  }

  // General Dialogs
  fun selectStudent(student: Student?) {
    _selectedStudent.value = student
  }

  fun setEditingStudent(student: Student?) {
    _editingStudent.value = student
  }

  fun setShowAddDialog(show: Boolean) {
    _showAddDialog.value = show
  }

  fun setShowStatsSheet(show: Boolean) {
    _showStatsSheet.value = show
  }

  fun setZoomedStudent(student: Student?) {
    _zoomedStudent.value = student
  }

  fun setShowAddCoachDialog(show: Boolean) {
    _showAddCoachDialog.value = show
  }

  fun updateStudent(student: Student) {
    repository.updateStudent(student)
    if (_selectedStudent.value?.id == student.id) {
      _selectedStudent.value = student
    }
  }

  fun addStudent(student: Student) {
    repository.addStudent(student)
  }

  fun deleteStudent(studentId: String) {
    repository.deleteStudent(studentId)
    if (_selectedStudent.value?.id == studentId) {
      _selectedStudent.value = null
    }
  }

  fun setSelectedPaymentMonth(month: String) {
    _selectedPaymentMonth.value = month
  }

  fun setSelectedRaportStudent(student: Student?) {
    _selectedRaportStudent.value = student
  }

  fun calculateAttendanceRate(studentId: String): String {
    val map = attendanceMap.value
    var totalMarked = 0
    var hadirCount = 0
    for ((_, studentStatusMap) in map) {
      val status = studentStatusMap[studentId]
      if (status != null && status != AttendanceStatus.UNMARKED) {
        totalMarked++
        if (status == AttendanceStatus.HADIR) {
          hadirCount++
        }
      }
    }
    return if (totalMarked > 0) {
      "${((hadirCount.toFloat() / totalMarked) * 100).toInt()}%"
    } else {
      val fallback = allStudents.value.find { it.id == studentId }?.attendanceRate
      if (!fallback.isNullOrBlank()) fallback else "92%"
    }
  }

  fun saveRaportData(
    studentId: String,
    gayaBebas: String,
    gayaDada: String,
    gayaPunggung: String,
    gayaKupu: String,
    ratingTeknik: Int,
    ratingDisiplin: Int,
    catatanPelatih: String,
    rekomendasiLevel: String,
    attendanceRate: String
  ) {
    val current = allStudents.value.toMutableList()
    val index = current.indexOfFirst { it.id == studentId }
    if (index != -1) {
      val updated = current[index].copy(
        gayaBebasTime = gayaBebas.trim(),
        gayaDadaTime = gayaDada.trim(),
        gayaPunggungTime = gayaPunggung.trim(),
        gayaKupuTime = gayaKupu.trim(),
        ratingTeknik = ratingTeknik,
        ratingDisiplin = ratingDisiplin,
        catatanPelatihRaport = catatanPelatih.trim(),
        rekomendasiLevel = rekomendasiLevel.trim(),
        attendanceRate = attendanceRate,
        tanggalRaport = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
      )
      updateStudent(updated)
      _selectedRaportStudent.value = updated
      if (_selectedStudent.value?.id == studentId) {
        _selectedStudent.value = updated
      }
    }
  }

  fun resetData() {
    repository.resetToInitialData()
  }

  fun setShowQrScannerModal(show: Boolean) {
    _showQrScannerModal.value = show
  }

  fun setStudentForQrCard(student: Student?) {
    _studentForQrCard.value = student
  }

  suspend fun recordBarcodeAttendance(barcode: String): Triple<Boolean, Student?, String> {
    return repository.recordBarcodeAttendance(barcode)
  }
}
