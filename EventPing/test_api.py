import urllib.request
import urllib.error
import json

BASE_URL = "http://localhost:8080"

def make_request(url, method='GET', data=None, headers=None):
    if headers is None:
        headers = {}
    
    if data:
        data = json.dumps(data).encode('utf-8')
        headers['Content-Type'] = 'application/json'
    
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            status = response.getcode()
            body = response.read().decode('utf-8')
            return status, body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')
    except Exception as e:
        return 0, str(e)

def test_health():
    status, body = make_request(f"{BASE_URL}/actuator/health")
    print(f"Health Status: {status}")
    print(f"Response: {body}")

def register_user():
    payload = {
        "email": "test_user" + str(id(object())) + "@example.com",
        "fullName": "Test User",
        "phoneNumber": "+1234567890",
        "password": "SecurePassword123!"
    }
    status, body = make_request(f"{BASE_URL}/api/users/register", method='POST', data=payload)
    print(f"Register Status: {status}")
    print(f"Response: {body}")
    return status == 201, payload

def login_user(email):
    payload = {
        "email": email,
        "password": "SecurePassword123!"
    }
    status, body = make_request(f"{BASE_URL}/api/auth/login", method='POST', data=payload)
    print(f"Login Status: {status}")
    print(f"Response: {body}")
    if status == 200:
        return json.loads(body).get('token')
    return None

if __name__ == "__main__":
    print("--- Testing Health ---")
    test_health()
    print("\n--- Registering User ---")
    success, payload = register_user()
    if success:
        print("\n--- Logging In ---")
        token = login_user(payload['email'])
        if token:
            print(f"Token: {token[:20]}...")
    else:
        # Try login with a known user if registration failed (maybe already exists)
        print("\n--- Trying Login with default user ---")
        login_user("john.doe@example.com")
