# Student Management API — Lab 2 + Lab 3

## เริ่มระบบด้วย Docker

เปิด Docker Desktop แล้วรันจากโฟลเดอร์ `student-api`:

สร้างไฟล์ `.env` ครั้งแรก (อย่าส่งไฟล์นี้พร้อมงาน):

```sh
python3 -c 'import secrets; from pathlib import Path; p=Path(".env"); p.exists() or p.write_text("JWT_SECRET=" + secrets.token_hex(32) + "\n")'
docker compose up -d --build
```

- Swagger: http://localhost:8000/docs
- API: http://localhost:8000/students
- PostgreSQL: localhost:5433, database `student_db`, user `postgres`, password `student_lab` (สำหรับ Lab ในเครื่องเท่านั้น)
- ตารางและข้อมูลตัวอย่าง 3 คนสร้างจาก `init.sql` เมื่อเริ่ม volume ครั้งแรก
- ข้อมูลคงอยู่ใน Docker volume เมื่อหยุดแล้วเริ่มใหม่

ตรวจข้อมูล:

```sh
docker compose exec db psql -U postgres -d student_db -c 'SELECT * FROM students ORDER BY id;'
docker compose exec api python smoke_test.py
```

หยุด/เริ่มเฉพาะ API เพื่อทดสอบ Offline:

```sh
docker compose stop api
docker compose start api
```

หยุดระบบทั้งหมดโดยเก็บข้อมูลไว้: `docker compose down`

## รันด้วย Python โดยไม่ใช้ container สำหรับ API

ใช้ Python 3.10 ขึ้นไป และ PostgreSQL ที่สร้าง database `student_db` แล้ว:

```sh
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
export DATABASE_URL='postgresql+psycopg2://postgres:student_lab@localhost:5433/student_db'
export JWT_SECRET="$(python3 -c 'import secrets; print(secrets.token_hex(32))')"
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

หาก PostgreSQL อยู่ port 5432 ให้ปรับ URL ตามเครื่องจริง ก่อนรันอาจใช้ `psql` นำเข้า `init.sql` เพื่อเตรียมข้อมูลตัวอย่าง API จะสร้างตารางที่ยังไม่มีตอนเริ่มระบบ แต่ไม่เติม seed ซ้ำทุกครั้ง

## REST endpoints

| Method | Path | ผลลัพธ์ |
| --- | --- | --- |
| GET | /students | รายการทั้งหมด |
| GET | /students/{id} | นักศึกษาหนึ่งคน หรือ 404 |
| POST | /students | เพิ่มนักศึกษา, HTTP 201 |
| PUT | /students/{id} | แก้ไขทั้งสามช่อง หรือ 404 |
| DELETE | /students/{id} | ลบ หรือ 404 |

POST/PUT ใช้ JSON เช่น:

```json
{"student_code":"66006001","name":"ชื่อของนักศึกษา","major":"Computer Science"}
```

ทุกช่องต้องไม่ว่าง จำกัด student_code 20 ตัวอักษร และ name/major 100 ตัวอักษร ข้อมูลไม่ผ่าน validation คืน HTTP 422


## Lab 3: Login + JWT

บัญชีทดสอบสำหรับ Lab: username `ronnakorn`, password `Lab3pass123!` (ไม่ใช่บัญชีส่วนตัว)

`POST /token` รับ form username/password และคืน access_token พร้อม token_type=bearer
JWT ใช้ HS256, sub=ชื่อผู้ใช้, iat=เวลาออก token และ exp=30 นาที
ทุก endpoint `/students` และ `/users/me` ต้องมี `Authorization: Bearer <token>`
Swagger ให้กด Authorize และ Login ด้วยบัญชีทดสอบก่อนเรียก Student API
Token หาย/ปลอม/หมดอายุคืน 401 และ WWW-Authenticate: Bearer
Android จะลบ token เมื่อได้ 401 และกลับหน้า Login; Logout ลบ token ในเครื่อง
การ Logout ไม่ revoke token ที่ถูกคัดลอกไปแล้ว ซึ่งจะใช้ได้จนหมดอายุ

ทดสอบ: `docker compose exec -T api python auth_smoke_test.py`
Token และรหัสผ่านไม่ถูกพิมพ์ในผลทดสอบหรือ HTTP log; Authorization ถูก redact
Android ใช้ SharedPreferences MODE_PRIVATE ตามใบงานและปิด backup; production ควรใช้ Keystore และ HTTPS
บัญชี Lab ถูกตรวจด้วย Argon2 password hash; ระบบนี้ยังไม่ใช่ระบบสมัครสมาชิกหรือสิทธิ์หลายบทบาท

## Lab 4: Offline-first และ Idempotent Create

Android บันทึกข้อมูลใหม่ใน Room ก่อนด้วย PENDING และใช้ Flow แสดงรายการ กด Sync Now เพื่อส่ง PENDING/FAILED ไป Server และบันทึก serverId พร้อม SYNCED หลังสำเร็จ หากส่งไม่สำเร็จเก็บ FAILED ให้ลองใหม่ได้

POST /students รับ `client_id` เพิ่มเติม (UUID จากมือถือ) สำหรับการลองส่งซ้ำ Backend สร้าง unique index และใช้ ON CONFLICT DO NOTHING: payload เดิม + client_id เดิมจะได้ id เดิม หากเปลี่ยน payload แต่ใช้ client_id เดิมคืน 409

เมื่อ API เริ่มทำงานจะเพิ่ม column client_id และ unique index ให้ฐานข้อมูล Lab 2–3 เดิมอัตโนมัติ โดยไม่ลบข้อมูล Room เปลี่ยนจาก version 1 เป็น 2 ด้วย Migration ที่รักษา id เดิมเป็น local id และคัดลอกไป serverId

ทดสอบ backend: `docker compose exec -T api python sync_smoke_test.py`

ขอบเขต Lab 4: queue เฉพาะการเพิ่มข้อมูล ไม่ได้ทำ background sync หรือ offline update/delete ของข้อมูลที่อยู่บน Server แล้ว รายการ PENDING ที่ยังไม่เคยส่งแก้ไข/ลบในเครื่องได้ ส่วน FAILED ต้อง Sync ให้ทราบผลก่อนแก้ไข/ลบ เพื่อไม่ให้คำขอที่เคยส่งสำเร็จแต่คำตอบหายเกิดความขัดแย้ง
