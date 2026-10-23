# ชุดส่ง Lab 8–9 — ronnakorn muangkan

อ่าน LAB8_Report.md และ LAB9_Report.md หลังแตก ZIP โดยเก็บโฟลเดอร์ evidence ไว้คู่กัน ภาพเป็น screenshot ของ Emulator จริง และมีคลิป Motion ใน evidence/lab9

เปิด StudentManagement ใน Android Studio ใช้ JDK 17 / SDK 34 หรือใช้ APK ที่แนบ เปิด Backend ตาม student-api/README.md แล้ว Login ด้วยบัญชีสาธิต ronnakorn / Lab3pass123! หน้าแรกหลัง Login เป็น Student Life และยังเข้าหน้าจัดการนักศึกษาของ Lab 3–7 ได้

โค้ดเพิ่ม CameraX, MediaPlayer, VideoView, Sensor และ Animation 5 แบบ พร้อมเพลง วิดีโอ และ Lottie ที่สร้างขึ้นเอง อ่าน MEDIA_LICENSE.txt ได้ Build ผ่านและ Unit Test 15/15 ผ่าน

ข้อจำกัดการทดสอบ: Emulator ไม่มี Step Counter จึงแสดงสถานะไม่รองรับ การทดสอบก้าวจริงต้องใช้โทรศัพท์ที่มี sensor นี้ ค่า 1,200 บน Dashboard เป็น Demo preview ที่แยกจาก sensor จริงอย่างชัดเจน

ชุดโค้ดรวมมี Backend เดิม, APK, SQL backup ล่าสุดที่มีจาก Lab 5–7 (Lab 8–9 ไม่เปลี่ยน schema หรือข้อมูล server) ไม่รวม .env หรือ Token จริง ผู้ตรวจสร้าง JWT_SECRET ของเครื่องตนเองตาม README Backend

SQL backup ให้นำเข้าฐานข้อมูลว่างใหม่ ดู LAB567_README.md ส่วนการนำเข้า SQL
