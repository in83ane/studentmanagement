# ใบงาน Data Storage and Access Part 2

## สิ่งที่เพิ่มจาก Part 1

ต่อยอดโปรเจกต์เดิมโดยใช้โครงสร้าง:

```text
Compose → StudentViewModel → StudentRepository → Retrofit → FastAPI → PostgreSQL
                                  ↓
                                Room → SQLite
```

- Backend อยู่ใน `student-api`: SQL สร้างตาราง/seed, FastAPI CRUD, validation และ 404 เมื่อไม่พบข้อมูล
- Retrofit ใช้ `/students` และข้อมูลจริง `id`, `student_code`, `name`, `major`
- เพิ่ม/แก้ไข/ลบส่งไป Server ก่อน และใช้ ID ที่ PostgreSQL สร้าง
- Refresh โหลด Server และแทนที่ cache ใน Room ภายใน transaction เดียว รวมถึงลบ cache เมื่อ Server ส่งรายการว่าง
- เมื่อ API ติดต่อไม่ได้ อ่าน Room และแสดง Offline แม้ Wi-Fi ยังทำงาน
- Offline รองรับอ่าน/ค้นหาข้อมูลเดิม การเขียนต้องเชื่อมต่อ API; ไม่มีคิว sync การเขียนแบบ Offline
- Search ค้นรหัส ชื่อ และสาขาแบบไม่แยกตัวพิมพ์ใหญ่เล็ก
- ฟอร์มไม่ปิดจนกว่าการบันทึกสำเร็จ และแสดงข้อผิดพลาดเมื่อบันทึกไม่ได้

## วิธีเปิดใช้งาน

1. เปิด Docker Desktop และทำตาม `student-api/README.md`
2. เปิดโฟลเดอร์ `StudentManagement` ใน Android Studio ใช้ JDK 17 และ Android SDK 34
3. เลือก Android Emulator แล้ว Run แอป
4. ค่า API ใน `RetrofitClient.kt` คือ `http://10.0.2.2:8000/`
5. ถ้าใช้มือถือจริง เปลี่ยนเป็น IP ของคอมพิวเตอร์ โทรศัพท์ต้องอยู่เครือข่ายเดียวกัน
6. Debug build อนุญาต HTTP สำหรับ Lab; release ต้องใช้ HTTPS

## แบบฝึกหัดท้าย Lab และวิธีตรวจ

### Exercise 1 — เพิ่มข้อมูลผ่าน Android

กด `+` แล้วกรอก `66006001`, `ronnakorn muangkan`, `Computer Science` และกด SAVE
ตรวจ PostgreSQL ด้วย:

```sh
cd student-api
docker compose exec db psql -U postgres -d student_db -c "SELECT * FROM students WHERE student_code = '66006001';"
```

ชื่อสำหรับ Exercise 1 ที่ผู้ใช้ระบุ: `ronnakorn muangkan`

### Exercise 2 — Refresh

กด Refresh → GET /students → แปลง DTO → บันทึก Room → แสดงรายการล่าสุด

### Exercise 3 — Delete

กดถังขยะในรายการ หรือ Delete ในหน้ารายละเอียด → DELETE /students/{id} → ลบ PostgreSQL และ Room หลัง Server สำเร็จ

### Exercise 4 — Offline

โหลดข้อมูลให้สำเร็จหนึ่งครั้ง จากนั้น `docker compose stop api` ปิดแล้วเปิดแอปใหม่ หรือกด Refresh
ต้องเห็นรายการจาก Room และข้อความ Offline จากนั้น `docker compose start api` และ Refresh กลับสู่ Online
ตรวจ SQLite ใน Android Studio: App Inspection → Database Inspector → student_database → students

### Exercise 5 — Search

กรอก `Computer` ใน Search Students ต้องเห็นเฉพาะรายการที่รหัส ชื่อ หรือสาขามีคำนี้ โดยข้อมูล seed จะเหลือ Somchai Jaidee สาขา Computer Science

## ตรวจสอบอัตโนมัติ

```sh
./gradlew assembleDebug testDebugUnitTest
cd student-api
docker compose exec api python smoke_test.py
```

Repository tests ตรวจการแทนที่ cache, Server ส่งรายการว่าง, อ่าน cache เมื่อ API ล้มเหลว, cancellation, ID จาก Server, แก้ไข และไม่ลบ cache เมื่อคำขอลบไม่สำเร็จ
API smoke test ตรวจ CRUD, validation, 404 และลบข้อมูลทดสอบของตัวเอง

## เอกสารอ้างอิง

- https://fastapi.tiangolo.com/tutorial/sql-databases/
- https://developer.android.com/reference/androidx/room/Transaction

## ผลทดสอบจริง (15 ตุลาคม 2026)

- `assembleDebug` และ `testDebugUnitTest`: BUILD SUCCESSFUL; 3 tests ผ่าน ไม่มี failure
- FastAPI smoke test กับ PostgreSQL ใน Docker: CRUD, 404, validation ผ่าน
- เพิ่มผ่าน Android Emulator: `66006001 / ronnakorn muangkan / Computer Science` พบใน PostgreSQL เป็น id 5
- Edit ผ่านหน้ารายละเอียด: แก้ชื่อและตรวจ PostgreSQL ตรงกัน
- Delete ผ่าน Android: ลบรายการชั่วคราว `DELETE-CHECK` แล้วตรวจฐานข้อมูลไม่พบรายการนั้น ข้อมูลนักศึกษาจริงยังอยู่
- หยุด FastAPI, force-stop แอปแล้วเปิดใหม่: แสดง Offline และข้อมูลครบ 4 คนจาก Room
- เริ่ม API ใหม่แล้ว Refresh: กลับ Online
- Search `Computer`: พบ Somchai Jaidee และ ronnakorn muangkan ทั้งคู่สาขา Computer Science
- ภาพหลักฐาน: `evidence/offline.png`, `evidence/search.png`, `evidence/online.png`

Emulator เดิมค้างในโหมด GPU software; ทดสอบสำเร็จหลังเปิดด้วย `-gpu host` ข้อมูลอยู่ใน Docker volume `student-management-lab_student_data` และ Room ของ emulator นี้ ถ้ารันเครื่องใหม่จะมี seed 3 คนและต้องเพิ่ม Exercise 1 อีกครั้ง
