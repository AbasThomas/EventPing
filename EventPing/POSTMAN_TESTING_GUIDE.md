# EventPing API Testing Guide

## 🚀 Quick Start

### 1. Import to Postman
- Open Postman
- Click **Import** → **File** → Select `EventPing_Postman_Collection.json`
- Collection will appear in left sidebar

### 2. Start Application
Make sure EventPing is running on `http://localhost:8080`

---

## 📋 Test Sequence (Follow in Order)

### Step 1: Check Health ✅
**GET** `http://localhost:8080/actuator/health`

**Expected Response:**
```json
{
  "status": "UP"
}
```

---

### Step 2: Create a User 👤
**POST** `http://localhost:8080/api/users`

**Body:**
```json
{
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

**Expected Response:**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe"
}
```

**💾 Save:** Copy the `id` value (you'll need it for creating events)

---

### Step 3: Create an Event 📅
**POST** `http://localhost:8080/api/events?userId=1`

**Body:**
```json
{
  "title": "Team Weekly Standup",
  "description": "Weekly sync meeting to discuss progress and blockers",
  "eventDateTime": "2026-01-25T10:00:00",
  "reminderOffsetMinutes": [60, 1440]
}
```

**Expected Response:**
```json
{
  "id": 1,
  "title": "Team Weekly Standup",
  "description": "Weekly sync meeting to discuss progress and blockers",
  "eventDateTime": "2026-01-25T10:00:00",
  "status": "ACTIVE",
  "slug": "a1b2c3d4",
  "creatorId": 1,
  "creatorEmail": "john.doe@example.com",
  "participantCount": 0,
  "createdAt": "2026-01-23T06:00:00"
}
```

**💾 Save:** Copy the `slug` value (e.g., "a1b2c3d4")

---

### Step 4: Get Event by Slug 🔍
**GET** `http://localhost:8080/api/events/{slug}`

Replace `{slug}` with the value from Step 3

**Expected:** Same response as Step 3

---

### Step 5: Join Event as Participant 🙋‍♀️
**POST** `http://localhost:8080/api/participants/events/{slug}/join?reminderOffsetMinutes=60,1440`

**Body:**
```json
{
  "email": "alice@example.com"
}
```

**Expected Response:**
```json
{
  "id": 1,
  "eventId": 1,
  "email": "alice@example.com",
  "joinedAt": "2026-01-23T06:05:00",
  "unsubscribed": false
}
```

**💾 Save:** Copy the `id` value (participant ID)

---

### Step 6: Add More Participants
Repeat Step 5 with different emails:
- `bob@example.com`
- `charlie@example.com`

---

### Step 7: Get Event Participants 👥
**GET** `http://localhost:8080/api/participants/events/{slug}`

**Expected Response:**
```json
[
  {
    "id": 1,
    "eventId": 1,
    "email": "alice@example.com",
    "joinedAt": "2026-01-23T06:05:00",
    "unsubscribed": false
  },
  {
    "id": 2,
    "eventId": 1,
    "email": "bob@example.com",
    "joinedAt": "2026-01-23T06:06:00",
    "unsubscribed": false
  }
]
```

---

### Step 8: Unsubscribe Participant ❌
**POST** `http://localhost:8080/api/participants/{participantId}/unsubscribe`

Replace `{participantId}` with ID from Step 5

**Expected:** `200 OK` (no body)

---

## 🧪 Verify in Database

```sql
-- Check event created
SELECT * FROM events;

-- Check participants
SELECT * FROM participants;

-- Check reminders (2 per participant)
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
```

**Expected Reminders:**
For 1 event with 2 participants and 2 reminder offsets (60min + 1440min):
- Total: 4 reminders
- 2 for alice@example.com (1h before, 1 day before)
- 2 for bob@example.com (1h before, 1 day before)

---

## 📊 More Test Scenarios

### Create Different Event Types

#### 1. Study Group
```json
{
  "title": "Frontend Study Group",
  "description": "Learn React hooks together",
  "eventDateTime": "2026-01-26T19:00:00",
  "reminderOffsetMinutes": [30, 180, 1440]
}
```

#### 2. Birthday Party
```json
{
  "title": "Sarah's 30th Birthday",
  "description": "Surprise party at The Garden. Bring gifts!",
  "eventDateTime": "2026-01-28T18:00:00",
  "reminderOffsetMinutes": [120, 2880]
}
```

#### 3. Project Deadline
```json
{
  "title": "Project Submission Deadline",
  "description": "Final presentation materials due",
  "eventDateTime": "2026-01-30T23:59:00",
  "reminderOffsetMinutes": [60, 360, 1440]
}
```

---

## 🎯 Rate Limiting Tests

### Test Event Creation Limit (FREE Plan: 3/day)

Try creating 4 events with the same userId in quick succession:

1. Event 1 ✅ Success
2. Event 2 ✅ Success  
3. Event 3 ✅ Success
4. Event 4 ❌ Should fail: "Event creation limit reached for today"

### Test Participant Capacity (FREE Plan: 50/event)

Join the same event 51 times with different emails:

Participant 1-50 ✅ Success
Participant 51 ❌ Should fail: "Event has reached maximum participant capacity"

---

## 🐛 Error Scenarios to Test

### 1. Invalid Event Date
```json
{
  "title": "Past Event",
  "eventDateTime": "2020-01-01T10:00:00",
  "reminderOffsetMinutes": [60]
}
```
**Note:** Currently allowed, but could add validation

### 2. Duplicate Participant
Try joining same event twice with same email:
```json
{
  "email": "alice@example.com"
}
```
**Expected:** `400 Bad Request` - "Email already registered for this event"

### 3. Non-existent Event
**GET** `http://localhost:8080/api/events/invalid-slug`

**Expected:** `500 Internal Server Error` - "Event not found"

---

## 📧 Email Reminder Testing

### Verify Email Sending

1. **Create event** with near-future date
2. **Join as participant** with your real email
3. **Set reminder** for 2 minutes: `reminderOffsetMinutes: [2]`
4. **Wait 2 minutes**
5. **Check email** - you should receive reminder!

**Example:**
```json
{
  "title": "Email Test Event",
  "eventDateTime": "2026-01-23T07:05:00",
  "reminderOffsetMinutes": [2]
}
```

### Check Logs
Look for in application console:
```
INFO - Found 1 due reminders to send
INFO - Email sent successfully to alice@example.com
INFO - Sent reminder 1 to alice@example.com
```

---

## 🔄 Cron Job Verification

### Test Reminder Sending
```sql
-- Find due reminders
SELECT * FROM reminders 
WHERE send_at <= NOW() 
AND sent = false;

-- After cron job runs (every minute)
SELECT * FROM reminders 
WHERE sent = true;
```

### Test Event Expiry
```sql
-- Create past event
-- Wait for hourly cron job
SELECT * FROM events WHERE status = 'EXPIRED';
```

---

## ✅ Success Criteria

- [ ] Health endpoint returns UP
- [ ] Can create users
- [ ] Can create events (get unique slug)
- [ ] Can join events (get participant ID)
- [ ] Reminders auto-created (2 per participant)
- [ ] Can unsubscribe
- [ ] Can list participants
- [ ] Rate limiting enforced after 3 events
- [ ] Duplicate email rejected
- [ ] Email reminders sent (check inbox or logs)

---

## 🎉 You're Done!

Your EventPing API is fully functional if all tests pass!
