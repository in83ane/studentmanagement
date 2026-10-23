# ชุดส่ง Lab 11 — ronnakorn muangkan

ส่ง ZIP รายงาน (Markdown + ภาพ + คลิป) และ ZIP โค้ดตามที่อาจารย์กำหนด ไม่ต้องติดตั้ง APK เพื่อส่งงาน
แตก ZIP ก่อนเปิด LAB11_Report.md โดยเก็บ evidence ไว้คู่กัน

- Android source: StudentManagement/app/src
- APK สำหรับทดสอบ: APK/StudentLife-Lab11.apk
- เปิดโปรเจกต์: Android Studio, JDK 17, SDK 34
- Backend: ทำตาม student-api/README.md และสร้าง .env/JWT_SECRET ของเครื่องตนเอง
- Login สาธิต: ronnakorn / Lab3pass123!
- คำสั่งตรวจ: ./gradlew assembleDebug testDebugUnitTest

Lab 11 ใช้โปรเจกต์จาก Lab 10 ต่อ รวม Property Animation, AnimatedVisibility, updateTransition, Lottie และ Screen Transition บน Home เดียวกัน
Build ผ่านและ 15 Unit Tests ผ่านวันที่ 24 ตุลาคม 2026 ภาพ/คลิป Lab 11 เก็บจาก Emulator รุ่นนี้ใหม่

Emulator ไม่มี Step Counter ค่า 1,200 บน Dashboard มีป้าย DEMO และใช้ทดสอบ animation ไม่ใช่ผลการเดินจริง ต้องใช้โทรศัพท์ที่มี sensor เพื่อทดสอบการเดิน
สื่อมี MEDIA_LICENSE.txt โค้ดรวมยังมีฟีเจอร์ Labs เดิม โดยไม่รวม .env, JWT จริง, local.properties หรือ build cache
