# EventPing Complete API Testing Guide

## 🚀 Quick Start

### 1. Start the Application
```bash
cd EventPing
mvn spring-boot:run
```

### 2. Import Postman Collection
- Open Postman
- Click **Import** → **File** → Select `EventPing_Complete_API_Collection.json`
- Collection will appear with organized folders

---

## 📋 Complete Testing Sequence

### Phase 1: System Health ✅
**GET** `http://localhost:8080/actuator/health`

**Expected Response:**
```json
{
  "status": "UP"
}
```

---

### Phase 2: Authentication & User Management 🔐

#### Step 1: Register New User
**POST** `/api/users/register`

**Body:**
```json
{
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890",
  "password": "SecurePassword123!"
}
```

**Expected Response:**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

#### Step 2: Login User
**POST** `/api/auth/login`

**Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "john.doe@example.com",
  "expiresIn": 3600
}
```

**💾 Save:** The token will be automatically saved to collection variables

#### Step 3: Get User Profile (Authenticated)
**GET** `/api/users/{userId}`
**Headers:** `Authorization: Bearer {token}`

**Expected:** User profile data

#### Step 4: Update User Profile
**PUT** `/api/users/{userId}`
**Headers:** `Authorization: Bearer {token}`

**Body:**
```json
{
  "fullName": "John Updated Doe",
  "phoneNumber": "+1234567899"
}
```

---

### Phase 3: Event Management 📅

#### Step 5: Create Event (Original Endpoint)
**POST** `/api/events?userId={userId}`

**Body:**
```json
{
  "title": "Team Weekly Standup",
  "description": "Weekly sync meeting to discuss progress and blockers",
  "eventDateTime": "2026-01-25T10:00:00",
  "reminderOffsetMinutes": [60, 1440]
}
```

**💾 Save:** Event slug and ID will be automatically saved

#### Step 6: Create Event (Authenticated Endpoint)
**POST** `/api/events`
**Headers:** `Authorization: Bearer {token}`

**Body:**
```json
{
  "title": "Secure Team Meeting",
  "description": "Authenticated team meeting with security",
  "eventDateTime": "2026-01-26T14:00:00",
  "reminderOffsetMinutes": [30, 120]
}
```

#### Step 7: Get Event by Slug
**GET** `/api/events/{eventSlug}`

**Expected:** Event details

#### Step 8: Get User's Events (Authenticated)
**GET** `/api/events`
**Headers:** `Authorization: Bearer {token}`

**Expected:** Array of user's events

#### Step 9: Update Event
**PUT** `/api/events/{eventId}`
**Headers:** `Authorization: Bearer {token}`

**Body:**
```json
{
  "title": "Updated Team Meeting",
  "description": "Updated description for the meeting",
  "eventDateTime": "2026-01-25T11:00:00"
}
```

---

### Phase 4: Participant Management 👥

#### Step 10: Join Event
**POST** `/api/participants/events/{eventSlug}/join?reminderOffsetMinutes=60,1440`

**Body:**
```json
{
  "email": "participant1@example.com"
}
```

**💾 Save:** Participant ID will be automatically saved

#### Step 11: Add More Participants
Repeat with different emails:
- `participant2@example.com`
- `participant3@example.com`

#### Step 12: Get Event Participants (Authenticated)
**GET** `/api/participants/events/{eventId}`
**Headers:** `Authorization: Bearer {token}`

**Expected:** Array of participants

#### Step 13: Remove Participant (Authenticated)
**DELETE** `/api/participants/{participantId}`
**Headers:** `Authorization: Bearer {token}`

#### Step 14: Unsubscribe Participant (Original)
**POST** `/api/participants/{participantId}/unsubscribe`

---

### Phase 5: Security Testing 🛡️

#### Step 15: Test XSS Protection
**POST** `/api/events`
**Headers:** `Authorization: Bearer {token}`

**Body:**
```json
{
  "title": "<script>alert('XSS')</script>",
  "description": "Testing XSS protection",
  "eventDateTime": "2026-01-27T10:00:00",
  "reminderOffsetMinutes": [60]
}
```

**Expected:** Input should be sanitized or rejected

#### Step 16: Test SQL Injection Protection
**POST** `/api/users/register`

**Body:**
```json
{
  "email": "test'; DROP TABLE users; --",
  "fullName": "SQL Injection Test",
  "phoneNumber": "+1234567890",
  "password": "SecurePassword123!"
}
```

**Expected:** Input should be rejected with validation error

#### Step 17: Test Unauthorized Access
**GET** `/api/users/1`
**Headers:** None (no Authorization header)

**Expected:** `401 Unauthorized`

---

### Phase 6: Rate Limiting Tests ⚡

#### Step 18: Test Event Creation Rate Limit
Run the "Test Event Creation Limit" request multiple times quickly:

1. First 3 requests: ✅ Success
2. 4th request: ❌ Should return `429 Too Many Requests`

#### Step 19: Test Registration Rate Limit
Run the "Test Registration Rate Limit" request multiple times quickly:

**Expected:** Rate limiting after configured threshold

---

### Phase 7: Token Management 🔄

#### Step 20: Refresh Token
**POST** `/api/auth/refresh`
**Headers:** `Authorization: Bearer {token}`

**Expected:** New token

#### Step 21: Logout
**POST** `/api/auth/logout`
**Headers:** `Authorization: Bearer {token}`

**Expected:** Token blacklisted

#### Step 22: Test Blacklisted Token
Try using the old token after logout:

**Expected:** `401 Unauthorized`

---

## 🧪 Advanced Testing Scenarios

### Duplicate Email Test
Try joining the same event twice with the same email:

**Expected:** `400 Bad Request` - "Email already registered for this event"

### Invalid Event Slug Test
**GET** `/api/events/invalid-slug-12345`

**Expected:** `404 Not Found` or `500 Internal Server Error`

### Past Event Creation Test
```json
{
  "title": "Past Event",
  "eventDateTime": "2020-01-01T10:00:00",
  "reminderOffsetMinutes": [60]
}
```

**Expected:** Currently allowed, but could add validation

---

## 📊 Response Format Examples

### Success Response
```json
{
  "id": 1,
  "title": "Team Meeting",
  "status": "ACTIVE",
  "createdAt": "2026-01-25T10:00:00"
}
```

### Error Response
```json
{
  "timestamp": "2026-01-25T10:30:00",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid credentials",
  "path": "/api/auth/login",
  "validationErrors": {}
}
```

### Rate Limiting Response
```
HTTP/1.1 429 Too Many Requests
Retry-After: 60

{
  "timestamp": "2026-01-25T10:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded",
  "path": "/api/events"
}
```

---

## 🔍 Database Verification

After testing, verify data in database:

```sql
-- Check users
SELECT id, email, full_name, created_at FROM users;

-- Check events
SELECT id, title, event_date_time, status, slug, creator_id FROM events;

-- Check participants
SELECT id, event_id, email, joined_at, unsubscribed FROM participants;

-- Check reminders
SELECT 
    r.id,
    e.title as event_title,
    p.email as participant_email,
    r.send_at,
    r.channel,
    r.sent
FROM reminders r
JOIN events e ON r.event_id = e.id
JOIN participants p ON r.participant_id = p.id
ORDER BY r.send_at;

-- Check audit logs (security events)
SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 10;
```

---

## ✅ Success Criteria Checklist

### Core Functionality
- [ ] Health endpoint returns UP
- [ ] User registration works
- [ ] User login returns JWT token
- [ ] Token refresh works
- [ ] User profile CRUD operations work
- [ ] Event creation works (both endpoints)
- [ ] Event retrieval works
- [ ] Participant joining works
- [ ] Participant management works

### Security Features
- [ ] Authentication required for protected endpoints
- [ ] Authorization prevents unauthorized access
- [ ] XSS protection blocks malicious scripts
- [ ] SQL injection protection works
- [ ] Rate limiting enforced
- [ ] Token blacklisting works on logout
- [ ] Proper error messages (no information disclosure)

### Data Integrity
- [ ] Reminders auto-created for participants
- [ ] Database constraints enforced
- [ ] Audit logs created for security events
- [ ] Duplicate email prevention works

---

## 🎯 Performance Testing

### Load Testing Commands
```bash
# Test authentication endpoint
ab -n 100 -c 10 -H "Content-Type: application/json" -p login.json http://localhost:8080/api/auth/login

# Test event creation (with auth header)
ab -n 50 -c 5 -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -p event.json http://localhost:8080/api/events
```

### Expected Performance
- Authentication: < 200ms response time
- Event creation: < 300ms response time
- Event retrieval: < 100ms response time
- Rate limiting: Immediate 429 response

---

## 🎉 Completion

Your EventPing API is fully functional if all tests pass! You now have:

- ✅ **Complete API coverage** - All endpoints tested
- ✅ **Security validation** - Authentication, authorization, input validation
- ✅ **Rate limiting verification** - Protection against abuse
- ✅ **Error handling validation** - Proper error responses
- ✅ **Data integrity checks** - Database constraints and relationships
- ✅ **Performance baseline** - Response time measurements

The API is ready for frontend integration and production deployment!