"""Test real PostgreSQL idempotent creation, including concurrent retries."""
from auth_smoke_test import request
from concurrent.futures import ThreadPoolExecutor
from uuid import uuid4

if __name__ == '__main__':
    status, login = request('POST', '/token', form={'username': 'ronnakorn', 'password': 'Lab3pass123!'})
    assert status == 200
    token = login['access_token']
    payload = {'student_code': 'SYNC-CHECK', 'name': 'Sync Test', 'major': 'CS', 'client_id': str(uuid4())}
    with ThreadPoolExecutor(max_workers=4) as pool:
        results = list(pool.map(lambda _: request('POST', '/students', token, data=payload), range(4)))
    assert all(status == 201 for status, _ in results)
    ids = {data['id'] for _, data in results}
    assert len(ids) == 1
    sid = ids.pop()
    try:
        rows = request('GET', '/students', token)[1]
        assert sum(row.get('client_id') == payload['client_id'] for row in rows) == 1
        changed = dict(payload, name='Different data')
        assert request('POST', '/students', token, data=changed)[0] == 409
        assert request('GET', f'/students/{sid}', token)[1]['name'] == 'Sync Test'
        print('PASS: 4 concurrent retries produce one PostgreSQL row and the same serverId')
        print('PASS: same client_id with changed payload returns 409 without overwriting')
    finally:
        assert request('DELETE', f'/students/{sid}', token)[0] == 200
    print('PASS: cleanup complete; no JWT values logged')
