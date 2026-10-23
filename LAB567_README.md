# ชุดส่งงาน Lab 5–7 — ronnakorn muangkan

## ไฟล์ส่งงาน

- LAB5_Report.md + evidence/lab5: ปุ่ม Background Sync, Logcat จริง, PostgreSQL จริง, Retry และคำตอบ
- LAB6_Report.md + evidence/lab6: di/, @HiltAndroidApp, @HiltViewModel และคำตอบ
- LAB7_Report.md + evidence/lab7: โครงสร้าง 3 ชั้น, Use Case, Repository, ผล Unit Test และคำตอบ
- app/src: source Android ทั้งหมด รวม Unit Test
- student-api/: FastAPI + Docker Compose + PostgreSQL schema + วิธีเริ่มระบบ
- APK/StudentManagement-Lab567.apk: APK สำหรับทดสอบ (debug)
- student_db_lab567_backup.sql: SQL backup ข้อมูลหลังทดสอบ

ภาพใน Markdown ใช้พาธสัมพัทธ์ กรุณาแตก ZIP ทั้งชุดก่อนเปิด Markdown เพื่อให้เห็นภาพ รายงาน Lab 5–7 เป็นรุ่นรวมระบบเดียวตามใบงานการรวม Lab 3–7 ไม่ใช่สามแอปแยกกัน

## เริ่มใช้งาน

1. เปิด StudentManagement ใน Android Studio กำหนด JDK 17 และ SDK 34
2. ทำตาม student-api/README.md เพื่อสร้าง .env และ JWT_SECRET ของเครื่องผู้ตรวจ แล้วรัน docker compose up -d --build จากโฟลเดอร์ student-api
3. เปิด Android Emulator และรันแอป (API URL http://10.0.2.2:8000/)
4. Login บัญชีสาธิต ronnakorn / Lab3pass123!
5. เพิ่มข้อมูลขณะ Offline แล้วกด Sync in Background เปิดเครือข่ายกลับและดูรายการเปลี่ยนเป็น SYNCED
6. รัน ./gradlew testDebugUnitTest เพื่อตรวจ 13 Unit Tests

เครื่องอื่นไม่สามารถเปิด localhost ของเครื่องผู้จัดทำได้ ต้องรัน Backend/PostgreSQL ของตนเองจากโค้ดในชุดนี้ ข้อมูล PostgreSQL ถูกแนบเป็น SQL เพื่อย้ายไปเครื่องอื่น ไม่ได้เก็บเป็นฐานข้อมูลร่วมผ่านอินเทอร์เน็ต

## นำเข้า SQL (ถ้าต้องการข้อมูลตรงกับภาพ)

เริ่ม Docker Compose ก่อน จากโฟลเดอร์ student-api สร้างฐานข้อมูลว่างใหม่และนำเข้า:

```sh
docker compose exec -T db createdb -U postgres student_db_restore
docker compose exec -T db psql -v ON_ERROR_STOP=1 -U postgres -d student_db_restore < ../student_db_lab567_backup.sql
```

จากนั้นแก้ DATABASE_URL ของบริการ api ใน compose.yaml ให้ชี้ student_db_restore แทน student_db แล้วรัน docker compose up -d api การใช้ฐานข้อมูลใหม่ช่วยไม่ให้ SQL ชนตาราง students ที่ init.sql สร้างไว้ในฐานข้อมูลเริ่มต้น

ชุดส่งไม่รวม .env, Token จริง, local.properties และโฟลเดอร์ build/.gradle/.idea ใช้บัญชีและรหัสผ่านสาธิตตาม README เฉพาะการทดสอบใน Lab

## หลักฐาน

- Screenshot แอปเป็นภาพจาก Android Emulator จริง
- ภาพโครงสร้าง source, annotation, Logcat, SQL และ Unit Test จัดรูปแบบจากไฟล์/ผลลัพธ์จริงเพื่อให้อ่านง่าย ไม่ใช่ภาพ IDE
- ผล Unit Test: 13 ผ่าน, 0 failures/errors/skipped
- Background Worker: รอ Offline แล้ว Sync สำเร็จ, Backend หยุดแล้ว Retry งานเดิมจนสำเร็จ
- Periodic: ตั้ง interval 15 นาทีและตรวจการลงทะเบียน ไม่อ้างผลการรอครบหนึ่งรอบ
- Backend: ตรวจ JWT และ concurrent idempotent Sync ผ่าน
