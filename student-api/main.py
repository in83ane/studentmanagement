from contextlib import asynccontextmanager
from fastapi import Depends, FastAPI, HTTPException
from sqlalchemy import select, text
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session
from database import Base, engine, get_db
from models import Student
from schemas import StudentCreate, StudentResponse
from fastapi.security import OAuth2PasswordRequestForm
from auth import authenticate, current_user

@asynccontextmanager
async def lifespan(app):
    Base.metadata.create_all(bind=engine)
    with engine.begin() as connection:
        connection.execute(text("ALTER TABLE students ADD COLUMN IF NOT EXISTS client_id VARCHAR(36)"))
        connection.execute(text("CREATE UNIQUE INDEX IF NOT EXISTS students_client_id_unique ON students (client_id)"))
    yield

app = FastAPI(title="Student Management API", version="1.0", lifespan=lifespan)

@app.post("/token")
def login(form: OAuth2PasswordRequestForm = Depends()):
    return {"access_token": authenticate(form.username, form.password), "token_type": "bearer"}

@app.get("/users/me")
def me(username: str = Depends(current_user)):
    return {"username": username}

@app.get("/")
def root():
    return {"message": "Student Management API"}

def find_student(db, student_id):
    student = db.get(Student, student_id)
    if student is None:
        raise HTTPException(status_code=404, detail="Student not found")
    return student

@app.get("/students", response_model=list[StudentResponse], dependencies=[Depends(current_user)])
def get_students(db: Session = Depends(get_db)):
    return db.scalars(select(Student).order_by(Student.id)).all()

@app.get("/students/{student_id}", response_model=StudentResponse, dependencies=[Depends(current_user)])
def get_student(student_id: int, db: Session = Depends(get_db)):
    return find_student(db, student_id)

@app.post("/students", response_model=StudentResponse, status_code=201, dependencies=[Depends(current_user)])
def create_student(student: StudentCreate, db: Session = Depends(get_db)):
    values = student.model_dump()
    statement = insert(Student).values(**values).on_conflict_do_nothing(index_elements=[Student.client_id]).returning(Student.id)
    inserted_id = db.execute(statement).scalar_one_or_none()
    if inserted_id is None:
        record = db.scalar(select(Student).where(Student.client_id == student.client_id))
        if any(getattr(record, key) != values[key] for key in ("student_code", "name", "major")):
            raise HTTPException(status_code=409, detail="client_id already used with different data")
    else:
        record = db.get(Student, inserted_id)
    db.commit()
    db.refresh(record)
    return record

@app.put("/students/{student_id}", response_model=StudentResponse, dependencies=[Depends(current_user)])
def update_student(student_id: int, student: StudentCreate, db: Session = Depends(get_db)):
    record = find_student(db, student_id)
    for field, value in student.model_dump(exclude={"client_id"}).items():
        setattr(record, field, value)
    db.commit()
    db.refresh(record)
    return record

@app.delete("/students/{student_id}", dependencies=[Depends(current_user)])
def delete_student(student_id: int, db: Session = Depends(get_db)):
    db.delete(find_student(db, student_id))
    db.commit()
    return {"message": "Student deleted"}
