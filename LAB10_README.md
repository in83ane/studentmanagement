# Lab 10 — ronnakorn muangkan

อ่าน LAB10_Report.md หลังแตก ZIP เพื่อให้ภาพแสดงครบ ใช้โปรเจกต์ StudentManagement เดิมต่อจาก Labs 3–9

- Android Studio: JDK 17 / SDK 34
- Backend: ดู student-api/README.md สร้าง .env ของเครื่องตัวเองก่อน docker compose up -d --build
- Login สาธิต: ronnakorn / Lab3pass123!
- APK: APK/StudentLife-Lab10.apk
- ทดสอบ: ./gradlew assembleDebug testDebugUnitTest

ผลวันที่ 24 ตุลาคม 2026: Build ผ่าน, 15 tests ผ่าน, ติดตั้งและเปิด Home รุ่น LAB 10 ได้
ภาพ Camera/Music/Video/Sensor ใช้หลักฐานเดิมวันที่ 15 ตุลาคม 2026 เนื่องจาก implementation เดียวกัน ระบุไว้ในรายงาน
Emulator ไม่มี Step Counter ต้องทดสอบก้าวจริงบนโทรศัพท์ที่มี sensor ค่า DEMO ใช้สำหรับ animation เท่านั้น
สื่อ MP3/MP4/Lottie สร้างสำหรับงานนี้ ดู MEDIA_LICENSE.txt
ไม่รวม .env, Token จริง, build cache หรือการตั้งค่าเฉพาะเครื่อง
