"""Integration tests against running FastAPI. Never print credentials or JWT values."""
import json
import os
import urllib.request
import urllib.error
import urllib.parse
from datetime import datetime, timedelta, timezone
import jwt

BASE = "http://127.0.0.1:8000"

def request(method, path, token=None, form=None, data=None):
    headers = {}
    body = None
    if token:
        headers["Authorization"] = "Bearer " + token
    if form is not None:
        body = urllib.parse.urlencode(form).encode()
        headers["Content-Type"] = "application/x-www-form-urlencoded"
    if data is not None:
        body = json.dumps(data).encode()
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(BASE + path, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            return response.status, json.load(response)
    except urllib.error.HTTPError as error:
        return error.code, json.load(error)

if __name__ == "__main__":
    payload = {"student_code": "AUTH-CHECK", "name": "Auth Test", "major": "CS"}
    for method, path in [("GET", "/students"), ("GET", "/students/1"), ("POST", "/students"), ("PUT", "/students/1"), ("DELETE", "/students/1")]:
        assert request(method, path, data=payload if method in ("POST", "PUT") else None)[0] == 401
    print("PASS: all five student endpoints reject missing tokens (401)")
    assert request("POST", "/token", form={"username": "ronnakorn", "password": "wrong-password"})[0] == 401
    print("PASS: incorrect password rejected (401)")
    status, result = request("POST", "/token", form={"username": "ronnakorn", "password": "Lab3pass123!"})
    assert status == 200 and result["token_type"] == "bearer"
    token = result["access_token"]
    assert request("GET", "/users/me", token)[1]["username"] == "ronnakorn"
    assert request("GET", "/students", token)[0] == 200
    print("PASS: login and protected request with Bearer [REDACTED] (200)")
    pieces = token.split(".")
    pieces[2] = ("A" if pieces[2][0] != "A" else "B") + pieces[2][1:]
    assert request("GET", "/students", ".".join(pieces))[0] == 401
    now = datetime.now(timezone.utc)
    expired = jwt.encode({"sub": "ronnakorn", "iat": now - timedelta(hours=2), "exp": now - timedelta(hours=1)}, os.environ["JWT_SECRET"], algorithm="HS256")
    assert request("GET", "/students", expired)[0] == 401
    print("PASS: altered signature and expired token rejected (401)")
    assert request("GET", "/students")[0] == 401
    print("PASS: removing token from next request returns 401")
    status, created = request("POST", "/students", token, data=payload)
    assert status == 201
    sid = created["id"]
    try:
        assert request("GET", f"/students/{sid}", token)[0] == 200
        payload["name"] = "Auth Updated"
        assert request("PUT", f"/students/{sid}", token, data=payload)[1]["name"] == "Auth Updated"
    finally:
        assert request("DELETE", f"/students/{sid}", token)[0] == 200
    print("PASS: authenticated CRUD and cleanup")
