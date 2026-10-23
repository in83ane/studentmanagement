from pydantic import BaseModel, ConfigDict, Field

class StudentCreate(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True)
    student_code: str = Field(min_length=1, max_length=20)
    name: str = Field(min_length=1, max_length=100)
    major: str = Field(min_length=1, max_length=100)
    client_id: str | None = Field(default=None, min_length=1, max_length=36)

class StudentResponse(StudentCreate):
    model_config = ConfigDict(from_attributes=True, str_strip_whitespace=True)
    id: int
