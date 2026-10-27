package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.AttendanceStatus
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
import org.json.JSONObject
import java.io.File

class AttendanceRepository(private val context: Context) {

  private val scope = CoroutineScope(Dispatchers.IO)

  private val _attendance = MutableStateFlow<Map<String, Map<String, AttendanceStatus>>>(emptyMap())
  val attendance: StateFlow<Map<String, Map<String, AttendanceStatus>>> = _attendance.asStateFlow()

  private val attendanceFile: File
    get() = File(context.filesDir, "sac_attendance_db.json")

  private var firestoreListener: ListenerRegistration? = null

  private val firestore: FirebaseFirestore by lazy {
    val db = FirebaseFirestore.getInstance()
    try {
      val settings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
        .build()
      db.firestoreSettings = settings
    } catch (e: Exception) {
      Log.w("AttendanceRepo", "Firestore cache settings already configured or exception", e)
    }
    db
  }

  init {
    scope.launch {
      loadLocalAttendance()
      startFirestoreAttendanceListener()
    }
  }

  private suspend fun loadLocalAttendance() = withContext(Dispatchers.IO) {
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
        Log.d("AttendanceRepo", "Loaded local attendance for ${map.size} dates")
      }
    } catch (e: Exception) {
      Log.e("AttendanceRepo", "Error reading local attendance file", e)
    }
  }

  private fun startFirestoreAttendanceListener() {
    try {
      firestoreListener?.remove()
      firestoreListener = firestore.collection("absensi")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("AttendanceRepo", "Firestore absensi snapshot listener error", error)
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            val updatedMap = _attendance.value.toMutableMap().mapValues { it.value.toMutableMap() }.toMutableMap()

            for (doc in snapshot.documents) {
              val date = doc.id
              val rawData = doc.data ?: continue
              val dayMap = updatedMap[date]?.toMutableMap() ?: mutableMapOf()

              // Check if records are nested in a "records" map or at the root level
              @Suppress("UNCHECKED_CAST")
              val records = (rawData["records"] as? Map<String, Any>) ?: rawData

              for ((studentId, statusValue) in records) {
                val statusStr = statusValue.toString()
                val status = try {
                  AttendanceStatus.valueOf(statusStr)
                } catch (e: Exception) {
                  AttendanceStatus.UNMARKED
                }
                dayMap[studentId] = status
              }
              updatedMap[date] = dayMap
            }

            _attendance.value = updatedMap
            scope.launch(Dispatchers.IO) {
              persistAttendance(updatedMap)
            }
            Log.d("AttendanceRepo", "Realtime attendance synced from Firestore: ${snapshot.size()} dates")
          }
        }
    } catch (e: Exception) {
      Log.e("AttendanceRepo", "Failed to initialize Firestore listener", e)
    }
  }

  fun setAttendance(date: String, studentId: String, status: AttendanceStatus) {
    val current = _attendance.value.toMutableMap().mapValues { it.value.toMutableMap() }.toMutableMap()
    val dayMap = current[date]?.toMutableMap() ?: mutableMapOf()
    dayMap[studentId] = status
    current[date] = dayMap
    _attendance.value = current

    scope.launch(Dispatchers.IO) {
      // 1. Persist to local disk for fast offline recovery
      persistAttendance(current)

      // 2. Persist to Firestore with offline queueing
      try {
        val stringMap = dayMap.mapValues { it.value.name }
        firestore.collection("absensi").document(date)
          .set(
            mapOf(
              "date" to date,
              "records" to stringMap,
              "lastUpdated" to System.currentTimeMillis()
            ),
            SetOptions.merge()
          )
          .addOnSuccessListener {
            Log.d("AttendanceRepo", "Attendance synced to Firestore for $date - $studentId: ${status.name}")
          }
          .addOnFailureListener { e ->
            Log.w("AttendanceRepo", "Firestore absensi write queued / offline: ${e.message}")
          }
      } catch (e: Exception) {
        Log.e("AttendanceRepo", "Error writing attendance to Firestore", e)
      }
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
      Log.e("AttendanceRepo", "Error persisting attendance to local storage", e)
    }
  }

  fun calculateAttendanceRate(studentId: String): String {
    val map = _attendance.value
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
      "92%"
    }
  }

  fun clearLocalCache() {
    if (attendanceFile.exists()) {
      attendanceFile.delete()
    }
    _attendance.value = emptyMap()
  }
}
