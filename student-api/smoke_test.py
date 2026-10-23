"""CRUD smoke test against the running API and PostgreSQL; removes its own test row."""
import json
import urllib.request
import urllib.error
import urllib.parse

BASE = "http://127.0.0.1:8000"
login = urllib.request.Request(BASE + "/token", data=urllib.parse.urlencode({"username": "ronnakorn", "password": "Lab3pass123!"}).encode())
with urllib.request.urlopen(login, timeout=10) as response:
    token = json.load(response)["access_token"]
def request(method, path, data=None):
    req = urllib.request.Request(BASE + path, data=json.dumps(data).encode() if data else None,
        headers={"Content-Type": "application/json", "Authorization": "Bearer " + token}, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            return response.status, json.load(response)
    except urllib.error.HTTPError as error:
        return error.code, json.load(error)

assert request("GET", "/")[0] == 200
assert request("GET", "/students")[0] == 200
assert request("GET", "/students/2147483647")[0] == 404
assert request("POST", "/students", {"student_code": " ", "name": "A", "major": "B"})[0] == 422
payload = {"student_code": "SMOKE-TEST", "name": "API Test", "major": "Computer Science"}
status, student = request("POST", "/students", payload)
assert status == 201
sid = student["id"]
try:
    assert request("GET", f"/students/{sid}")[1]["name"] == "API Test"
    payload["name"] = "Updated Test"
    assert request("PUT", f"/students/{sid}", payload)[1]["name"] == "Updated Test"
    assert any(row["id"] == sid for row in request("GET", "/students")[1])
finally:
    assert request("DELETE", f"/students/{sid}")[0] == 200
assert request("GET", f"/students/{sid}")[0] == 404
assert request("PUT", f"/students/{sid}", payload)[0] == 404
assert request("DELETE", f"/students/{sid}")[0] == 404
print("PASS: CRUD, list, validation, missing records, cleanup")
