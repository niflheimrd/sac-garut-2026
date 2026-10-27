package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassCategory
import com.example.data.model.CoachProfile
import com.example.data.model.Student
import com.example.data.model.SurveyModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentRepository(private val context: Context) {

  private val scope = CoroutineScope(Dispatchers.IO)

  private val _students = MutableStateFlow<List<Student>>(emptyList())
  val students: StateFlow<List<Student>> = _students.asStateFlow()

  private val _classes = MutableStateFlow<List<ClassCategory>>(emptyList())
  val classes: StateFlow<List<ClassCategory>> = _classes.asStateFlow()

  private val _coaches = MutableStateFlow<List<CoachProfile>>(emptyList())
  val coaches: StateFlow<List<CoachProfile>> = _coaches.asStateFlow()

  private val _attendance = MutableStateFlow<Map<String, Map<String, AttendanceStatus>>>(emptyMap())
  val attendance: StateFlow<Map<String, Map<String, AttendanceStatus>>> = _attendance.asStateFlow()

  private val _parentSurveys = MutableStateFlow<List<SurveyModel>>(emptyList())
  val parentSurveys: StateFlow<List<SurveyModel>> = _parentSurveys.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _isCloudSyncActive = MutableStateFlow(false)
  val isCloudSyncActive: StateFlow<Boolean> = _isCloudSyncActive.asStateFlow()

  private val studentsFile: File
    get() = File(context.filesDir, "sac_students_db.json")

  private val classesFile: File
    get() = File(context.filesDir, "sac_classes_db.json")

  private val coachesFile: File
    get() = File(context.filesDir, "sac_coaches_db.json")

  private val attendanceFile: File
    get() = File(context.filesDir, "sac_attendance_db.json")

  private val parentSurveysFile: File
    get() = File(context.filesDir, "sac_parent_surveys_db.json")

  private var studentsListener: ListenerRegistration? = null
  private var surveysListener: ListenerRegistration? = null
  private var attendanceListener: ListenerRegistration? = null
  private var attendanceLogsListener: ListenerRegistration? = null

  private val firestore: FirebaseFirestore by lazy {
    val db = FirebaseFirestore.getInstance()
    try {
      val settings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
        .build()
      db.firestoreSettings = settings
      _isCloudSyncActive.value = true
    } catch (e: Exception) {
      Log.w("StudentRepository", "Firestore settings configuration notice", e)
      _isCloudSyncActive.value = true
    }
    db
  }

  init {
    scope.launch {
      loadClasses()
      loadCoaches()
      loadLocalSurveys()
      loadStudents()
      loadAttendance()
      startFirestoreListeners()
    }
  }

  private fun startFirestoreListeners() {
    try {
      // 1. Realtime Students Sync
      studentsListener?.remove()
      studentsListener = firestore.collection("students")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("StudentRepository", "Firestore students listen error: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            val cloudList = snapshot.documents.mapNotNull { doc ->
              try {
                val s = doc.toObject(Student::class.java)
                if (s != null && s.id.isNotBlank()) s else null
              } catch (e: Exception) {
                Log.w("StudentRepository", "Error parsing student doc ${doc.id}", e)
                null
              }
            }

            if (cloudList.isNotEmpty()) {
              _students.value = cloudList
              scope.launch(Dispatchers.IO) {
                persistList(cloudList)
              }
              Log.d("StudentRepository", "Synced ${cloudList.size} students from Firestore")
            }
          } else if (snapshot != null && snapshot.isEmpty && _students.value.isNotEmpty()) {
            // First-time sync: push local initial dataset to Firestore
            uploadStudentsToFirestore(_students.value)
          }
        }

      // 2. Realtime Parent Survey Listener (survei_orangtua)
      // db.collection("survei_orangtua").addSnapshotListener { snapshot, e -> ... }
      surveysListener?.remove()
      surveysListener = firestore.collection("survei_orangtua")
        .addSnapshotListener { snapshot, e ->
          if (e != null) {
            Log.w("StudentRepository", "Firestore survei_orangtua error: ${e.message}")
            return@addSnapshotListener
          }
          // Setiap ada form masuk, kode di bawah ini otomatis jalan
          val daftarRespon = snapshot?.documents?.mapNotNull { doc ->
            try {
              val survey = doc.toObject(SurveyModel::class.java)
              if (survey != null) {
                if (survey.id.isBlank()) survey.copy(id = doc.id) else survey
              } else null
            } catch (err: Exception) {
              null
            }
          } ?: emptyList()

          updateUI(daftarRespon) // Tampilan otomatis update
        }

      // 3. Realtime Attendance Listener (absensi)
      attendanceListener?.remove()
      attendanceListener = firestore.collection("absensi")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("StudentRepository", "Firestore absensi error: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            val currentMap = _attendance.value.toMutableMap().mapValues { it.value.toMutableMap() }.toMutableMap()
            for (doc in snapshot.documents) {
              val date = doc.id
              val raw = doc.data ?: continue
              @Suppress("UNCHECKED_CAST")
              val records = (raw["records"] as? Map<String, Any>) ?: raw
              val dayMap = currentMap[date]?.toMutableMap() ?: mutableMapOf()
              for ((studentId, statusValue) in records) {
                val status = try {
                  AttendanceStatus.valueOf(statusValue.toString())
                } catch (e: Exception) {
                  AttendanceStatus.UNMARKED
                }
                dayMap[studentId] = status
              }
              currentMap[date] = dayMap
            }
            _attendance.value = currentMap
            scope.launch(Dispatchers.IO) {
              persistAttendance(currentMap)
            }
          }
        }

      // 4. Realtime Barcode/QR Attendance Collection Listener (attendance)
      attendanceLogsListener?.remove()
      attendanceLogsListener = firestore.collection("attendance")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("StudentRepository", "Firestore attendance logs error: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            val currentMap = _attendance.value.toMutableMap().mapValues { it.value.toMutableMap() }.toMutableMap()
            var changed = false
            for (doc in snapshot.documents) {
              val date = doc.getString("date") ?: continue
              val studentId = doc.getString("studentId") ?: continue
              val statusStr = doc.getString("status") ?: "HADIR"
              val status = try {
                AttendanceStatus.valueOf(statusStr)
              } catch (e: Exception) {
                AttendanceStatus.HADIR
              }
              val dayMap = currentMap[date]?.toMutableMap() ?: mutableMapOf()
              if (dayMap[studentId] != status) {
                dayMap[studentId] = status
                currentMap[date] = dayMap
                changed = true
              }
            }

            if (changed) {
              _attendance.value = currentMap
              scope.launch(Dispatchers.IO) {
                persistAttendance(currentMap)
              }
              Log.d("StudentRepository", "Synced realtime attendance check-in logs from Firestore")
            }
          }
        }
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error starting Firestore listeners", e)
    }
  }

  private fun updateUI(daftarRespon: List<SurveyModel>) {
    _parentSurveys.value = daftarRespon
    scope.launch(Dispatchers.IO) {
      persistParentSurveys(daftarRespon)
    }

    // Automatically enrich student records with latest parent responses if matched
    if (daftarRespon.isNotEmpty() && _students.value.isNotEmpty()) {
      val currentStudents = _students.value.toMutableList()
      var modified = false

      for (survey in daftarRespon) {
        val studentIndex = currentStudents.indexOfFirst {
          it.namaLengkap.equals(survey.namaSiswa.trim(), ignoreCase = true) ||
              it.id.equals(survey.namaSiswa.trim(), ignoreCase = true)
        }
        if (studentIndex != -1) {
          val s = currentStudents[studentIndex]
          val updated = s.copy(
            pencapaian = if (survey.pencapaian.isNotBlank()) survey.pencapaian else s.pencapaian,
            kepuasanPelatih = if (survey.kepuasanPelatih.isNotBlank()) survey.kepuasanPelatih else s.kepuasanPelatih,
            penilaianFasilitas = if (survey.penilaianFasilitas.isNotBlank()) survey.penilaianFasilitas else s.penilaianFasilitas,
            pendapatTiket = if (survey.pendapatTiket.isNotBlank()) survey.pendapatTiket else s.pendapatTiket,
            kritikSaran = if (survey.saranKritik.isNotBlank()) survey.saranKritik else s.kritikSaran
          )
          if (updated != s) {
            currentStudents[studentIndex] = updated
            modified = true
          }
        }
      }

      if (modified) {
        _students.value = currentStudents
        scope.launch(Dispatchers.IO) {
          persistList(currentStudents)
        }
      }
    }
  }

  private fun uploadStudentsToFirestore(list: List<Student>) {
    scope.launch(Dispatchers.IO) {
      try {
        val batch = firestore.batch()
        for (student in list) {
          val ref = firestore.collection("students").document(student.id)
          batch.set(ref, student, SetOptions.merge())
        }
        batch.commit()
          .addOnSuccessListener {
            Log.d("StudentRepository", "Initial students seed synced to Firestore successfully")
          }
          .addOnFailureListener { e ->
            Log.w("StudentRepository", "Initial seed queued offline: ${e.message}")
          }
      } catch (e: Exception) {
        Log.e("StudentRepository", "Error uploading students batch", e)
      }
    }
  }

  suspend fun loadClasses() = withContext(Dispatchers.IO) {
    try {
      val jsonString = if (classesFile.exists() && classesFile.length() > 0) {
        classesFile.readText()
      } else {
        context.assets.open("sac_classes.json").bufferedReader().use { it.readText() }
      }
      val list = parseClasses(jsonString)
      _classes.value = list
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error loading classes", e)
      val defaultClasses = listOf(
        ClassCategory("cls-prestasi", "Prestasi", "> 1 Tahun", "Pembinaan atlit renang prestasi", "4 gaya & kompetisi", "Coach Fatur", "Coach Agung", "Coach Gema"),
        ClassCategory("cls-lanjutan", "Lanjutan", "6 - 12 Bulan", "Pengembangan renang tingkat lanjut", "Penyempurnaan 4 gaya", "Coach Gema", "Coach Agis", "Coach Bin"),
        ClassCategory("cls-moderat", "Pemula Moderat", "3 - 6 Bulan", "Penguasaan renang mandiri", "Meluncur mandiri & kayuhan", "Coach Ayu", "Coach Amey", "Coach Amal"),
        ClassCategory("cls-basic", "Pemula Basic", "< 3 Bulan", "Pengenalan air & dasar meluncur", "Adaptasi air & keselamatan", "Coach Ryu", "Coach Rofiq", "Coach Ayu")
      )
      _classes.value = defaultClasses
    }
  }

  suspend fun loadCoaches() = withContext(Dispatchers.IO) {
    try {
      val jsonString = if (coachesFile.exists() && coachesFile.length() > 0) {
        coachesFile.readText()
      } else {
        context.assets.open("sac_coaches.json").bufferedReader().use { it.readText() }
      }
      val list = parseCoaches(jsonString)
      _coaches.value = list
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error loading coaches", e)
      val defaultCoaches = listOf(
        CoachProfile("cch-Fatur", "Coach Fatur", "Pelatih Kepala & Koordinator"),
        CoachProfile("cch-Agung", "Coach Agung", "Pelatih Lanjutan"),
        CoachProfile("cch-Gema", "Coach Gema", "Pelatih Prestasi & Atlet"),
        CoachProfile("cch-Ayu", "Coach Ayu", "Pelatih Pemula Basic"),
        CoachProfile("cch-Amey", "Coach Amey", "Pelatih Pemula Basic"),
        CoachProfile("cch-Agis", "Coach Agis", "Pelatih Pemula Moderate"),
        CoachProfile("cch-Ryu", "Coach Ryu", "Pelatih Pemula Basic"),
        CoachProfile("cch-Rofiq", "Coach Rofiq", "Pelatih Pemula Basic"),
        CoachProfile("cch-Bin", "Coach Bin", "Pelatih Pemula Moderate"),
        CoachProfile("cch-Amal", "Coach Amal", "Pelatih Pemula Moderate")
      )
      _coaches.value = defaultCoaches
    }
  }

  private suspend fun loadLocalSurveys() = withContext(Dispatchers.IO) {
    try {
      val jsonString = if (parentSurveysFile.exists() && parentSurveysFile.length() > 0) {
        parentSurveysFile.readText()
      } else {
        context.assets.open("sac_parent_surveys.json").bufferedReader().use { it.readText() }
      }
      val array = JSONArray(jsonString)
      val list = mutableListOf<SurveyModel>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          SurveyModel(
            id = obj.optString("id", "SRV-2026-${i + 1}"),
            namaSiswa = obj.optString("namaSiswa", ""),
            namaOrangTua = obj.optString("namaOrangTua", ""),
            pencapaian = obj.optString("pencapaian", ""),
            kepuasanPelatih = obj.optString("kepuasanPelatih", ""),
            penilaianFasilitas = obj.optString("penilaianFasilitas", ""),
            pendapatTiket = obj.optString("pendapatTiket", ""),
            saranKritik = obj.optString("saranKritik", ""),
            timestamp = obj.optString("timestamp", "")
          )
        )
      }
      _parentSurveys.value = list
      if (!parentSurveysFile.exists() && list.isNotEmpty()) {
        persistParentSurveys(list)
      }
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error loading local surveys", e)
    }
  }

  suspend fun loadStudents() = withContext(Dispatchers.IO) {
    _isLoading.value = true
    try {
      val assetJson = context.assets.open("sac_students.json").bufferedReader().use { it.readText() }
      val assetList = parseJsonArray(assetJson)

      val studentList = if (studentsFile.exists() && studentsFile.length() > 0) {
        val localList = parseJsonArray(studentsFile.readText())
        val localMap = localList.associateBy { it.id }.toMutableMap()
        for (assetStudent in assetList) {
          val local = localMap[assetStudent.id]
          if (local == null) {
            localMap[assetStudent.id] = assetStudent
          } else {
            // Update contact, photo, address, survey if local was missing or to ensure latest
            localMap[assetStudent.id] = local.copy(
              alamat = if (local.alamat.isBlank()) assetStudent.alamat else local.alamat,
              noHp = if (local.noHp.isBlank()) assetStudent.noHp else local.noHp,
              waNumber = if (local.waNumber.isBlank()) assetStudent.waNumber else local.waNumber,
              fotoUrl = if (local.fotoUrl.isBlank()) assetStudent.fotoUrl else local.fotoUrl,
              drivePhotoId = if (local.drivePhotoId.isBlank()) assetStudent.drivePhotoId else local.drivePhotoId,
              pencapaian = if (local.pencapaian.isBlank()) assetStudent.pencapaian else local.pencapaian,
              kepuasanPelatih = if (local.kepuasanPelatih.isBlank()) assetStudent.kepuasanPelatih else local.kepuasanPelatih,
              penilaianFasilitas = if (local.penilaianFasilitas.isBlank()) assetStudent.penilaianFasilitas else local.penilaianFasilitas,
              pendapatTiket = if (local.pendapatTiket.isBlank()) assetStudent.pendapatTiket else local.pendapatTiket,
              kritikSaran = if (local.kritikSaran.isBlank()) assetStudent.kritikSaran else local.kritikSaran,
              hariLatihan = if (local.hariLatihan.contains("Rabu", ignoreCase = true)) {
                local.hariLatihan.replace("Rabu", "Selasa", ignoreCase = true)
              } else {
                local.hariLatihan
              }
            )
          }
        }
        val merged = assetList.map { localMap[it.id] ?: it } + localList.filter { !assetList.any { a -> a.id == it.id } }
        persistList(merged)
        merged
      } else {
        persistList(assetList)
        assetList
      }

      _students.value = studentList
      Log.d("StudentRepository", "Loaded ${studentList.size} students from storage")
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error loading students", e)
      try {
        val assetJson = context.assets.open("sac_students.json").bufferedReader().use { it.readText() }
        _students.value = parseJsonArray(assetJson)
      } catch (assetErr: Exception) {
        Log.e("StudentRepository", "Fatal error loading asset JSON", assetErr)
      }
    } finally {
      _isLoading.value = false
    }
  }

  private suspend fun loadAttendance() = withContext(Dispatchers.IO) {
    try {
      if (attendanceFile.exists() && attendanceFile.length() > 0) {
        val jsonStr = attendanceFile.readText()
        val root = JSONObject(jsonStr)
        val map = mutableMapOf<String, MutableMap<String, AttendanceStatus>>()
        val dateKeys = root.keys()
        while (dateKeys.hasNext()) {
          val date = dateKeys.next()
          val dayObj = root.getJSONObject(date)
          val studentMap = mutableMapOf<String, AttendanceStatus>()
          val studentKeys = dayObj.keys()
          while (studentKeys.hasNext()) {
            val studentId = studentKeys.next()
            val statusStr = dayObj.optString(studentId, "UNMARKED")
            val status = try {
              AttendanceStatus.valueOf(statusStr)
            } catch (e: Exception) {
              AttendanceStatus.UNMARKED
            }
            studentMap[studentId] = status
          }
          map[date] = studentMap
        }
        _attendance.value = map
      }
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error loading attendance", e)
    }
  }

  private fun parseClasses(jsonString: String): List<ClassCategory> {
    val array = JSONArray(jsonString)
    val list = ArrayList<ClassCategory>(array.length())
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      list.add(
        ClassCategory(
          id = obj.optString("id", "cls-$i"),
          name = obj.optString("name", ""),
          duration = obj.optString("duration", ""),
          description = obj.optString("description", ""),
          targetFocus = obj.optString("targetFocus", ""),
          coachUtama = obj.optString("coachUtama", ""),
          asistenCoach1 = obj.optString("asistenCoach1", ""),
          asistenCoach2 = obj.optString("asistenCoach2", "")
        )
      )
    }
    return list
  }

  private fun parseCoaches(jsonString: String): List<CoachProfile> {
    val array = JSONArray(jsonString)
    val list = ArrayList<CoachProfile>(array.length())
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      list.add(
        CoachProfile(
          id = obj.optString("id", "cch-$i"),
          name = obj.optString("name", ""),
          role = obj.optString("role", ""),
          phone = obj.optString("phone", ""),
          bio = obj.optString("bio", ""),
          photoUrl = obj.optString("photoUrl", "")
        )
      )
    }
    return list
  }

  private fun parseJsonArray(jsonString: String): List<Student> {
    val array = JSONArray(jsonString)
    val list = ArrayList<Student>(array.length())
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      val rawClass = obj.optString("kelas", "")
      val lama = obj.optString("lamaLatihan", "")

      val normalizedClass = when {
        rawClass.contains("Prestasi") || rawClass.contains("> 1 Tahun") -> "Prestasi (> 1 Tahun)"
        rawClass.contains("Moderat") || rawClass.contains("6-12") || rawClass.contains("6 - 12") -> "Pemula Moderat (6 - 12 Bulan)"
        rawClass.contains("3 - 6") || rawClass.contains("3-6") -> "Pemula (3 - 6 Bulan)"
        rawClass.contains("Basic") || rawClass.contains("< 3 Bulan") || rawClass.contains("Kurang dari 3") -> "Pemula Basic (< 3 Bulan)"
        lama.contains("1 Tahun") -> "Prestasi (> 1 Tahun)"
        lama.contains("6-12") -> "Pemula Moderat (6 - 12 Bulan)"
        lama.contains("3-6") -> "Pemula (3 - 6 Bulan)"
        else -> "Pemula Basic (< 3 Bulan)"
      }

      list.add(
        Student(
          id = obj.optString("id", "SAC-${1000 + i}"),
          timestamp = obj.optString("timestamp", ""),
          namaLengkap = obj.optString("namaLengkap", ""),
          ttl = obj.optString("ttl", ""),
          asalSekolah = obj.optString("asalSekolah", ""),
          lamaLatihan = lama,
          tujuan = obj.optString("tujuan", ""),
          kelas = normalizedClass,
          hariLatihan = obj.optString("hariLatihan", "Minggu").replace("Rabu", "Selasa", ignoreCase = true),
          jamLatihan = obj.optString("jamLatihan", "08.00 - 09.30 WIB"),
          ukuranJersey = obj.optString("ukuranJersey", "L"),
          namaIbu = obj.optString("namaIbu", ""),
          namaAyah = obj.optString("namaAyah", ""),
          namaOrangTua = obj.optString("namaOrangTua", ""),
          alamat = obj.optString("alamat", ""),
          noHp = obj.optString("noHp", ""),
          waNumber = obj.optString("waNumber", ""),
          fotoUrl = obj.optString("fotoUrl", ""),
          drivePhotoId = obj.optString("drivePhotoId", ""),
          statusPembayaran = obj.optString("statusPembayaran", "Lunas"),
          coach = obj.optString("coach", "Coach Fahmi"),
          pencapaian = obj.optString("pencapaian", ""),
          kepuasanPelatih = obj.optString("kepuasanPelatih", ""),
          penilaianFasilitas = obj.optString("penilaianFasilitas", ""),
          pendapatTiket = obj.optString("pendapatTiket", ""),
          kritikSaran = obj.optString("kritikSaran", ""),
          catatanKhusus = obj.optString("catatanKhusus", ""),
          gayaBebasTime = obj.optString("gayaBebasTime", ""),
          gayaDadaTime = obj.optString("gayaDadaTime", ""),
          gayaPunggungTime = obj.optString("gayaPunggungTime", ""),
          gayaKupuTime = obj.optString("gayaKupuTime", ""),
          attendanceRate = obj.optString("attendanceRate", "92%"),
          ratingTeknik = obj.optInt("ratingTeknik", 4),
          ratingDisiplin = obj.optInt("ratingDisiplin", 5),
          catatanPelatihRaport = obj.optString("catatanPelatihRaport", ""),
          rekomendasiLevel = obj.optString("rekomendasiLevel", ""),
          tanggalRaport = obj.optString("tanggalRaport", "")
        )
      )
    }
    return list
  }

  // --- CRUD for Classes ---
  fun updateClassCategory(category: ClassCategory) {
    scope.launch {
      val current = _classes.value.toMutableList()
      val index = current.indexOfFirst { it.id == category.id }
      if (index != -1) {
        val oldName = current[index].name
        current[index] = category
        _classes.value = current
        persistClasses(current)

        if (oldName != category.name) {
          val currentStudents = _students.value.toMutableList()
          var changed = false
          for (i in currentStudents.indices) {
            if (currentStudents[i].kelas == oldName) {
              val updated = currentStudents[i].copy(kelas = category.name)
              currentStudents[i] = updated
              syncStudentToFirestore(updated)
              changed = true
            }
          }
          if (changed) {
            _students.value = currentStudents
            persistList(currentStudents)
          }
        }
      }
    }
  }

  fun addClassCategory(category: ClassCategory) {
    scope.launch {
      val current = _classes.value.toMutableList()
      current.add(category)
      _classes.value = current
      persistClasses(current)
    }
  }

  fun deleteClassCategory(id: String) {
    scope.launch {
      val current = _classes.value.toMutableList()
      current.removeAll { it.id == id }
      _classes.value = current
      persistClasses(current)
    }
  }

  private suspend fun persistClasses(list: List<ClassCategory>) = withContext(Dispatchers.IO) {
    try {
      val array = JSONArray()
      for (c in list) {
        val obj = JSONObject().apply {
          put("id", c.id)
          put("name", c.name)
          put("duration", c.duration)
          put("description", c.description)
          put("targetFocus", c.targetFocus)
          put("coachUtama", c.coachUtama)
          put("asistenCoach1", c.asistenCoach1)
          put("asistenCoach2", c.asistenCoach2)
        }
        array.put(obj)
      }
      classesFile.writeText(array.toString(2))
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error persisting classes", e)
    }
  }

  // --- CRUD for Coaches ---
  fun updateCoachProfile(coach: CoachProfile, oldName: String? = null) {
    scope.launch {
      val current = _coaches.value.toMutableList()
      val index = current.indexOfFirst { it.id == coach.id }
      if (index != -1) {
        val previousName = oldName ?: current[index].name
        current[index] = coach
        _coaches.value = current
        persistCoaches(current)

        if (previousName != coach.name) {
          val currentClasses = _classes.value.toMutableList()
          var classChanged = false
          for (i in currentClasses.indices) {
            var c = currentClasses[i]
            var updated = false
            if (c.coachUtama == previousName) { c = c.copy(coachUtama = coach.name); updated = true }
            if (c.asistenCoach1 == previousName) { c = c.copy(asistenCoach1 = coach.name); updated = true }
            if (c.asistenCoach2 == previousName) { c = c.copy(asistenCoach2 = coach.name); updated = true }
            if (updated) {
              currentClasses[i] = c
              classChanged = true
            }
          }
          if (classChanged) {
            _classes.value = currentClasses
            persistClasses(currentClasses)
          }

          val currentStudents = _students.value.toMutableList()
          var studentChanged = false
          for (i in currentStudents.indices) {
            if (currentStudents[i].coach == previousName) {
              val updated = currentStudents[i].copy(coach = coach.name)
              currentStudents[i] = updated
              syncStudentToFirestore(updated)
              studentChanged = true
            }
          }
          if (studentChanged) {
            _students.value = currentStudents
            persistList(currentStudents)
          }
        }
      }
    }
  }

  fun addCoachProfile(coach: CoachProfile) {
    val current = _coaches.value.toMutableList()
    current.add(coach)
    _coaches.value = current
    scope.launch(Dispatchers.IO) {
      persistCoaches(current)
    }
  }

  fun deleteCoachProfile(id: String) {
    val current = _coaches.value.toMutableList()
    current.removeAll { it.id == id }
    _coaches.value = current
    scope.launch(Dispatchers.IO) {
      persistCoaches(current)
    }
  }

  private suspend fun persistCoaches(list: List<CoachProfile>) = withContext(Dispatchers.IO) {
    try {
      val array = JSONArray()
      for (c in list) {
        val obj = JSONObject().apply {
          put("id", c.id)
          put("name", c.name)
          put("role", c.role)
          put("phone", c.phone)
          put("bio", c.bio)
          put("photoUrl", c.photoUrl)
        }
        array.put(obj)
      }
      coachesFile.writeText(array.toString(2))
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error persisting coaches", e)
    }
  }

  // --- CRUD for Students ---
  private suspend fun persistList(list: List<Student>) = withContext(Dispatchers.IO) {
    try {
      val array = JSONArray()
      for (student in list) {
        val obj = JSONObject().apply {
          put("id", student.id)
          put("timestamp", student.timestamp)
          put("namaLengkap", student.namaLengkap)
          put("ttl", student.ttl)
          put("asalSekolah", student.asalSekolah)
          put("lamaLatihan", student.lamaLatihan)
          put("tujuan", student.tujuan)
          put("kelas", student.kelas)
          put("hariLatihan", student.hariLatihan)
          put("jamLatihan", student.jamLatihan)
          put("ukuranJersey", student.ukuranJersey)
          put("namaIbu", student.namaIbu)
          put("namaAyah", student.namaAyah)
          put("namaOrangTua", student.namaOrangTua)
          put("alamat", student.alamat)
          put("noHp", student.noHp)
          put("waNumber", student.waNumber)
          put("fotoUrl", student.fotoUrl)
          put("drivePhotoId", student.drivePhotoId)
          put("statusPembayaran", student.statusPembayaran)
          put("coach", student.coach)
          put("pencapaian", student.pencapaian)
          put("kepuasanPelatih", student.kepuasanPelatih)
          put("penilaianFasilitas", student.penilaianFasilitas)
          put("pendapatTiket", student.pendapatTiket)
          put("kritikSaran", student.kritikSaran)
          put("catatanKhusus", student.catatanKhusus)
          put("gayaBebasTime", student.gayaBebasTime)
          put("gayaDadaTime", student.gayaDadaTime)
          put("gayaPunggungTime", student.gayaPunggungTime)
          put("gayaKupuTime", student.gayaKupuTime)
          put("attendanceRate", student.attendanceRate)
          put("ratingTeknik", student.ratingTeknik)
          put("ratingDisiplin", student.ratingDisiplin)
          put("catatanPelatihRaport", student.catatanPelatihRaport)
          put("rekomendasiLevel", student.rekomendasiLevel)
          put("tanggalRaport", student.tanggalRaport)
        }
        array.put(obj)
      }
      studentsFile.writeText(array.toString(2))
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error persisting students", e)
    }
  }

  private suspend fun persistParentSurveys(list: List<SurveyModel>) = withContext(Dispatchers.IO) {
    try {
      val array = JSONArray()
      for (s in list) {
        val obj = JSONObject().apply {
          put("id", s.id)
          put("namaSiswa", s.namaSiswa)
          put("namaOrangTua", s.namaOrangTua)
          put("pencapaian", s.pencapaian)
          put("kepuasanPelatih", s.kepuasanPelatih)
          put("penilaianFasilitas", s.penilaianFasilitas)
          put("pendapatTiket", s.pendapatTiket)
          put("saranKritik", s.saranKritik)
          put("timestamp", s.timestamp)
        }
        array.put(obj)
      }
      parentSurveysFile.writeText(array.toString(2))
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error persisting surveys", e)
    }
  }

  private suspend fun persistAttendance(att: Map<String, Map<String, AttendanceStatus>>) = withContext(Dispatchers.IO) {
    try {
      val root = JSONObject()
      for ((date, studentMap) in att) {
        val dayObj = JSONObject()
        for ((studentId, status) in studentMap) {
          dayObj.put(studentId, status.name)
        }
        root.put(date, dayObj)
      }
      attendanceFile.writeText(root.toString(2))
    } catch (e: Exception) {
      Log.e("StudentRepository", "Error persisting attendance", e)
    }
  }

  private fun syncStudentToFirestore(student: Student) {
    scope.launch(Dispatchers.IO) {
      try {
        firestore.collection("students").document(student.id)
          .set(student, SetOptions.merge())
          .addOnFailureListener { e ->
            Log.w("StudentRepository", "Firestore write queued offline for ${student.id}: ${e.message}")
          }
      } catch (e: Exception) {
        Log.e("StudentRepository", "Error updating student in Firestore", e)
      }
    }
  }

  fun setAttendance(date: String, studentId: String, status: AttendanceStatus) {
    val current = _attendance.value.toMutableMap()
    val dayMap = current[date]?.toMutableMap() ?: mutableMapOf()
    dayMap[studentId] = status
    current[date] = dayMap
    _attendance.value = current

    scope.launch(Dispatchers.IO) {
      persistAttendance(current)
      try {
        val stringMap = dayMap.mapValues { it.value.name }
        firestore.collection("absensi").document(date)
          .set(mapOf("date" to date, "records" to stringMap), SetOptions.merge())
      } catch (e: Exception) {
        Log.w("StudentRepository", "Firestore absensi write error: ${e.message}")
      }
    }
  }

  suspend fun recordBarcodeAttendance(barcode: String): Triple<Boolean, Student?, String> = withContext(Dispatchers.IO) {
    val clean = barcode.trim()
    val student = _students.value.firstOrNull { s ->
      s.id.equals(clean, ignoreCase = true) ||
      s.id.replace("-", "").equals(clean.replace("-", ""), ignoreCase = true) ||
      clean.contains(s.id, ignoreCase = true) ||
      s.namaLengkap.equals(clean, ignoreCase = true)
    }

    if (student != null) {
      val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
      val timeFormat = SimpleDateFormat("HH:mm:ss 'WIB'", Locale("id", "ID")).format(Date())
      val timestampEpoch = System.currentTimeMillis()

      // 1. Update local & absensi map
      setAttendance(today, student.id, AttendanceStatus.HADIR)

      // 2. Write to Firestore 'attendance' collection
      try {
        val attendanceDoc = mapOf(
          "attendanceId" to "${today}_${student.id}",
          "studentId" to student.id,
          "studentName" to student.namaLengkap,
          "classCategory" to student.kelas,
          "date" to today,
          "timestamp" to timeFormat,
          "timestampEpoch" to timestampEpoch,
          "status" to "HADIR",
          "scannedBy" to "Scanner Absensi Kamera",
          "coach" to student.coach
        )
        firestore.collection("attendance").document("${today}_${student.id}")
          .set(attendanceDoc, SetOptions.merge())
      } catch (e: Exception) {
        Log.w("StudentRepository", "Firestore attendance write queued/error: ${e.message}")
      }

      Triple(true, student, timeFormat)
    } else {
      Triple(false, null, "")
    }
  }

  fun assignCoach(studentId: String, coachName: String) {
    val current = _students.value.toMutableList()
    val index = current.indexOfFirst { it.id == studentId }
    if (index != -1) {
      val updated = current[index].copy(coach = coachName)
      current[index] = updated
      _students.value = current
      syncStudentToFirestore(updated)
      scope.launch(Dispatchers.IO) {
        persistList(current)
      }
    }
  }

  fun updatePaymentStatus(studentId: String, newStatus: String) {
    val current = _students.value.toMutableList()
    val index = current.indexOfFirst { it.id == studentId }
    if (index != -1) {
      val updated = current[index].copy(statusPembayaran = newStatus)
      current[index] = updated
      _students.value = current
      syncStudentToFirestore(updated)
      scope.launch(Dispatchers.IO) {
        persistList(current)
      }
    }
  }

  fun updateStudent(updatedStudent: Student) {
    val current = _students.value.toMutableList()
    val index = current.indexOfFirst { it.id == updatedStudent.id }
    if (index != -1) {
      current[index] = updatedStudent
      _students.value = current
      syncStudentToFirestore(updatedStudent)
      scope.launch(Dispatchers.IO) {
        persistList(current)
      }
    }
  }

  fun addStudent(student: Student) {
    val current = _students.value.toMutableList()
    current.add(0, student)
    _students.value = current
    syncStudentToFirestore(student)
    scope.launch(Dispatchers.IO) {
      persistList(current)
    }
  }

  fun deleteStudent(studentId: String) {
    val current = _students.value.toMutableList()
    current.removeAll { it.id == studentId }
    _students.value = current
    scope.launch(Dispatchers.IO) {
      try {
        firestore.collection("students").document(studentId).delete()
      } catch (e: Exception) {
        Log.w("StudentRepository", "Error deleting student from Firestore", e)
      }
      persistList(current)
    }
  }

  fun resetToInitialData() {
    scope.launch {
      try {
        if (studentsFile.exists()) studentsFile.delete()
        if (classesFile.exists()) classesFile.delete()
        if (coachesFile.exists()) coachesFile.delete()
        if (attendanceFile.exists()) attendanceFile.delete()
        if (parentSurveysFile.exists()) parentSurveysFile.delete()

        loadClasses()
        loadCoaches()
        loadLocalSurveys()
        loadStudents()
        _attendance.value = emptyMap()
      } catch (e: Exception) {
        Log.e("StudentRepository", "Error resetting data", e)
      }
    }
  }
}
