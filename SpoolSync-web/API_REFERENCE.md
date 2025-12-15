// API Endpoints and Firestore Reference Guide

## Firestore Collections

### 1. qrSessions Collection

**Path:** `/qrSessions/{sessionId}`

**Structure:**
```json
{
  "sessionId": "string (uuid)",
  "requestToken": "string (uuid)",
  "expiresAt": "timestamp (Firebase Timestamp)",
  "createdAt": "timestamp (Firebase Timestamp)",
  "confirmed": "boolean (default: false)",
  "confirmedBy": "string (userId, optional)",
  "confirmedAt": "timestamp (optional)"
}
```

**Example:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "requestToken": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
  "expiresAt": "2024-12-15T15:35:00Z",
  "createdAt": "2024-12-15T15:30:00Z",
  "confirmed": true,
  "confirmedBy": "user_123",
  "confirmedAt": "2024-12-15T15:32:00Z"
}
```

### 2. users Collection

**Path:** `/users/{userId}`

**Structure:**
```json
{
  "userId": "string (Firebase Auth UID)",
  "email": "string",
  "displayName": "string",
  "photoURL": "string (optional)",
  "createdAt": "timestamp",
  "lastLogin": "timestamp"
}
```

### 3. users/{userId}/statistics Collection

**Path:** `/users/{userId}/statistics/{statId}`

**Structure:**
```json
{
  "type": "string (e.g., 'prints', 'filament_used')",
  "value": "number",
  "date": "timestamp",
  "details": "object (optional)"
}
```

## Web API Calls

### Create QR Session

```typescript
import { generateQRSession } from '../services/qrService';

const session = await generateQRSession();
// Returns: QRSession with sessionId, requestToken, expiresAt
```

### Listen to QR Session Updates

```typescript
import { onSnapshot, doc } from 'firebase/firestore';
import { db } from '../config/firebase';

const sessionId = 'your-session-id';

const unsubscribe = onSnapshot(
  doc(db, 'qrSessions', sessionId),
  (snapshot) => {
    const data = snapshot.data();
    if (data?.confirmed) {
      console.log('User confirmed:', data.confirmedBy);
      // Redirect to dashboard
    }
  }
);

// Clean up listener when done
// unsubscribe();
```

### Store Session in localStorage

```typescript
// After successful confirmation
localStorage.setItem('sessionId', sessionId);
localStorage.setItem('userId', userId);
```

### Retrieve from localStorage

```typescript
const sessionId = localStorage.getItem('sessionId');
const userId = localStorage.getItem('userId');

if (!sessionId || !userId) {
  // Redirect to login
}
```

## Android API Calls

### Scan QR Code

```kotlin
import com.google.mlkit.vision.barcode.BarcodeScanner
import org.json.JSONObject

val qrContent = scanQRCode() // Returns JSON string

try {
  val qrData = JSONObject(qrContent)
  val sessionId = qrData.getString("sessionId")
  val requestToken = qrData.getString("requestToken")
  val expiresAt = qrData.getLong("expiresAt")
} catch (e: Exception) {
  Log.e("QR", "Error parsing QR: ${e.message}")
}
```

### Confirm QR in Firestore

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

val db = FirebaseFirestore.getInstance()
val currentUser = FirebaseAuth.getInstance().currentUser

db.collection("qrSessions")
  .document(sessionId)
  .update(
    mapOf(
      "confirmed" to true,
      "confirmedBy" to currentUser?.uid,
      "confirmedAt" to Timestamp.now()
    )
  )
  .addOnSuccessListener {
    Log.d("QR", "Session confirmed successfully")
  }
  .addOnFailureListener { e ->
    Log.e("QR", "Error confirming session: ${e.message}")
  }
```

### Fetch User Statistics

```kotlin
import com.google.firebase.firestore.Query

db.collection("users")
  .document(currentUser.uid)
  .collection("statistics")
  .orderBy("date", Query.Direction.DESCENDING)
  .limit(100)
  .get()
  .addOnSuccessListener { snapshot ->
    for (doc in snapshot) {
      val statType = doc.getString("type")
      val statValue = doc.getDouble("value")
      val statDate = doc.getTimestamp("date")
      
      // Process statistics
    }
  }
```

## Timeouts and Expiry

- QR Session TTL: **5 minutes** (300,000 ms)
- localStorage Session: **24 hours** (or until manually cleared)
- Firebase Auth Token: **1 hour** (auto-refreshed)

## Error Codes

```typescript
// Firestore Error Codes
'permission-denied' = Access denied by Firestore rules
'not-found' = Document doesn't exist
'already-exists' = Document already exists
'invalid-argument' = Invalid data format
'internal' = Internal server error
'unavailable' = Service temporarily unavailable
```

## CORS Headers (if needed)

If hosting on different domain, ensure CORS is configured:

```javascript
// Firebase automatically handles CORS for web
// No additional configuration needed for same-origin requests
```

## Rate Limiting

- Firestore: 50,000 reads/day on free tier
- No rate limiting on auth operations
- Consider implementing request throttling on web

## Testing with Postman/cURL

### Create Session (Cloud Function call - if using)

```bash
curl -X POST https://your-region-your-project.cloudfunctions.net/createQRSession \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Check Session Status

```bash
# This requires reading Firestore through API
# Use Firebase REST API:
curl -X GET "https://firestore.googleapis.com/v1/projects/YOUR_PROJECT/databases/(default)/documents/qrSessions/{sessionId}" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Monitoring

**Firebase Console:**
1. Go to Firestore Database
2. Check "Usage" tab for read/write stats
3. Check "Rules" tab for errors
4. Check "Authentication" for user status

**Useful Queries:**

```typescript
// Find unconfirmed sessions older than 5 minutes
db.collection('qrSessions')
  .where('confirmed', '==', false)
  .where('expiresAt', '<', new Date())
  .get()

// Find all sessions confirmed by a user
db.collection('qrSessions')
  .where('confirmedBy', '==', userId)
  .get()
```

## Cleanup

**Recommended:** Set up a Cloud Function or cron job to delete expired sessions:

```typescript
// Pseudo-code for Cloud Function
export const cleanupExpiredSessions = functions
  .pubsub.schedule('every 5 minutes')
  .onRun(async (context) => {
    const now = new Date();
    const snapshot = await admin
      .firestore()
      .collection('qrSessions')
      .where('expiresAt', '<', now)
      .get();
    
    const batch = admin.firestore().batch();
    snapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    await batch.commit();
  });
```
