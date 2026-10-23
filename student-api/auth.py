import os
from datetime import datetime, timedelta, timezone
import jwt
from jwt.exceptions import InvalidTokenError
from pwdlib import PasswordHash
from fastapi import Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer

SECRET = os.environ["JWT_SECRET"]
if len(SECRET) < 32:
    raise RuntimeError("JWT_SECRET must contain at least 32 characters")
ALGORITHM = "HS256"
USERNAME = "ronnakorn"
# Demonstration account only; production users belong in a database.
password_hash = PasswordHash.recommended()
DEMO_PASSWORD_HASH = password_hash.hash("Lab3pass123!")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

def unauthorized():
    return HTTPException(status_code=401, detail="Invalid credentials or expired token", headers={"WWW-Authenticate": "Bearer"})

def authenticate(username, password):
    valid = password_hash.verify(password, DEMO_PASSWORD_HASH)
    if username != USERNAME or not valid:
        raise unauthorized()
    now = datetime.now(timezone.utc)
    return jwt.encode({"sub": username, "iat": now, "exp": now + timedelta(minutes=30)}, SECRET, algorithm=ALGORITHM)

def current_user(token: str = Depends(oauth2_scheme)):
    try:
        payload = jwt.decode(token, SECRET, algorithms=[ALGORITHM], options={"require": ["sub", "iat", "exp"]})
        if payload["sub"] != USERNAME:
            raise unauthorized()
        return payload["sub"]
    except InvalidTokenError:
        raise unauthorized()
