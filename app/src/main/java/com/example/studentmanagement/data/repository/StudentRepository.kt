package com.example.studentmanagement.data.repository

import com.example.studentmanagement.data.local.StudentDao
import com.example.studentmanagement.data.remote.StudentApi
import com.example.studentmanagement.model.Student

// Repository Layer: Single source of truth (STEP 9, 20, 23)
// จัดการ Offline-First logic: UI ไม่ควรรู้ว่าข้อมูลมาจาก Room หรือ API (STEP 22)
class StudentRepository(
    private val studentDao: StudentDao,
    private val studentApi: StudentApi
) {
    // STEP 23: Offline-First — ลอง API ก่อน, ถ้าสำเร็จ -> บันทึก Room, ถ้าไม่สำเร็จ -> ใช้ Room
    // getStudents(): try API -> save every item to Room -> return remote list;
    //               catch Exception -> return Room data
    suspend fun getStudents(): List<Student> {
        return try {
            // เรียก API
            val remoteStudents = studentApi.getStudents()
            
            // Map StudentDto -> Student (เพราะ API ให้ข้อมูลคนละ format)
            val studentsToSave = remoteStudents.map { dto ->
                Student(
                    // Avoid ID collisions: API ให้ ID 1..10
                    // Student ที่เพิ่มในเครื่องใช้ autoGenerate (id=0)
                    // Room จะจัดการ ID ให้อัตโนมัติ ไม่ซ้ำกับ ID จาก API
                    // Note: นี่เป็น simplification สำหรับ Lab
                    id = dto.id,
                    studentCode = "6501${dto.id.toString().padStart(4, '0')}", // เช่น 65010001
                    name = dto.name,
                    major = dto.company?.name ?: "Unknown Major"
                )
            }
            
            // บันทึกทุก item ลง Room (OnConflictStrategy.REPLACE)
            studentsToSave.forEach { student ->
                studentDao.insertStudent(student)
            }
            
            // รวมข้อมูล remote + local ที่อาจเพิ่มในเครื่อง
            studentDao.getAllStudents()
        } catch (e: Exception) {
            // ถ้า API ใช้งานไม่ได้ -> ใช้ข้อมูลจาก Room
            studentDao.getAllStudents()
        }
    }

    // Exercise 1: เพิ่ม Student ลง Room
    suspend fun addStudent(student: Student) {
        studentDao.insertStudent(student)
    }

    // อัปเดต Student ใน Room
    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    // Exercise 2: ลบ Student จาก Room
    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    // Bonus: ดึง Student ตาม ID สำหรับหน้า Detail
    suspend fun getStudentById(id: Int): Student? {
        return studentDao.getStudentById(id)
    }
}
