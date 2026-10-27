package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Student
import com.example.data.repository.StudentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SAC Garut", appName)
  }

  @Test
  fun `student repository loads 160 student records with 4 duration levels and 3 coaches per class`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StudentRepository(context)
    repository.loadClasses()
    repository.loadCoaches()
    repository.loadStudents()

    val list = repository.students.value
    assertEquals(160, list.size)

    val classes = repository.classes.value
    assertEquals(4, classes.size)

    // Each class has 3 coaches assigned
    classes.forEach { cat ->
      assertEquals(3, cat.assignedCoaches.size)
    }

    val first = list.first()
    assertEquals("Ziofan Hamizan Athaillah", first.namaLengkap)
    assertEquals("Pemula Basic (< 3 Bulan)", first.kelas)
    assertNotNull(first.initials)
    assertTrue(first.cleanWhatsAppNumber.startsWith("62"))

    val assignedCoaches = first.getAssignedCoaches(classes)
    assertEquals(3, assignedCoaches.size)
  }

  @Test
  fun `all Wednesday schedules are shifted to Tuesday and 0 students train on Rabu`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StudentRepository(context)
    repository.loadStudents()

    val list = repository.students.value
    assertEquals(160, list.size)

    val rabuStudents = list.filter { it.hariLatihan.contains("Rabu", ignoreCase = true) }
    assertEquals(0, rabuStudents.size)

    val selasaStudents = list.filter { it.hariLatihan.contains("Selasa", ignoreCase = true) }
    assertEquals(12, selasaStudents.size)
  }

  @Test
  fun `daily attendance schedule distribution matches active training days`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StudentRepository(context)
    repository.loadStudents()

    val list = repository.students.value
    fun countDay(day: String): Int {
      val cleanDay = day.lowercase().replace("'", "")
      return list.count { it.hariLatihan.lowercase().replace("'", "").contains(cleanDay) }
    }

    assertEquals(12, countDay("Selasa"))
    assertEquals(0, countDay("Rabu"))
    assertEquals(24, countDay("Kamis"))
    assertEquals(35, countDay("Jum'at"))
    assertEquals(43, countDay("Sabtu"))
    assertEquals(69, countDay("Minggu"))
  }
}
