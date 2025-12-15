# SpoolSync Web - Setup Guide

## Firebase Configuration

Aby aplikácia fungovala, musíš nakonfigurovať Firebase projekt.

### 1. Vytvorenie Firebase projektu

1. Choď na [Firebase Console](https://console.firebase.google.com)
2. Klikni na "Add project"
3. Zadaj meno projektu (napr. "SpoolSync")
4. Povolí Google Analytics (voliteľné)
5. Vytvor projekt

### 2. Webová aplikácia

1. V Firebase Console, klikni na ikonku webovej aplikácie (na stránke projektu)
2. Zaregistruj aplikáciu s menom (napr. "SpoolSync Web")
3. Firebase ti poskytne konfiguráciu - skopíruj si ju

### 3. Firestore Database

1. V ľavom menu choď na "Firestore Database"
2. Klikni "Create database"
3. Vyber "Start in production mode"
4. Vyber región (napr. "europe-west1")
5. Vytvor databázu

### 4. Firestore Rules (bezpečnosť)

V "Rules" tabuľke, nahraď existujúce pravidlá s týmito:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow QR sessions to be created and read by anyone
    match /qrSessions/{sessionId} {
      allow create;
      allow read;
      // Allow only the user who confirmed to update
      allow update: if request.auth != null;
    }
    
    // User statistics - only authenticated users can read/write their own
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
  }
}
```

### 5. Environment Variables

1. V koreňovom priečinku projektu vytvor `.env.local` súbor
2. Skopíruj konfiguráciu z `.env.example`
3. Vyplň hodnoty z Firebase Console:

```env
VITE_FIREBASE_API_KEY=<your-api-key>
VITE_FIREBASE_AUTH_DOMAIN=<your-project>.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=<your-project-id>
VITE_FIREBASE_STORAGE_BUCKET=<your-bucket>
VITE_FIREBASE_MESSAGING_SENDER_ID=<your-sender-id>
VITE_FIREBASE_APP_ID=<your-app-id>
```

## Firebase Auth Setup (pre mobilnú aplikáciu)

1. V Firebase Console choď na "Authentication"
2. Klikni na "Get started"
3. Povolí "Email/Password" alebo iný spôsob autentifikácie

## Spustenie aplikácie

```bash
# Inštalácia závislostí
npm install

# Vývoj
npm run dev

# Build
npm run build

# Preview produkčnej verzie
npm run preview
```

## Mobilná aplikácia - Android implementation

Pre Android aplikáciu, ktorá bude pracovať s touto webovou aplikáciou:

### Dáta v QR kóde:
```json
{
  "sessionId": "uuid-string",
  "requestToken": "uuid-string",
  "expiresAt": 1234567890
}
```

### Potvrdenie v Firestore:

```javascript
// Po skenovaní QR kódu
const sessionId = qrData.sessionId;
const requestToken = qrData.requestToken;
const expiresAt = qrData.expiresAt;

// Overenie, že token nie je expirovaný
if (Date.now() > expiresAt) {
  // Token vypršal
  return;
}

// Potvrdenie v Firestore
const currentUser = FirebaseAuth.getInstance().getCurrentUser();
const db = FirebaseFirestore.getInstance();

db.collection("qrSessions")
  .document(sessionId)
  .update(
    "confirmed", true,
    "confirmedBy", currentUser.getUid(),
    "confirmedAt", System.currentTimeMillis()
  )
  .addOnSuccessListener {
    // Úspešné potvrdenie
    navigateToAccountScreen()
  }
  .addOnFailureListener { e ->
    // Chyba pri potvrdení
    Log.e("QR", "Error confirming session", e)
  }
```

## Bezpečnostné poznámky

- `requestToken` platí len 5 minút
- Web sesiu ukladá v `localStorage` s ID session a User ID
- Firestore pravidlá chránia prístup k dátam
- Mobilná aplikácia musí byť prihlásená do Firebase Auth

## Troubleshooting

### Projekt nezačína
- Skontroluj, či je `.env.local` nakonfigurovaný správne
- Spustite `npm install` znova
- Skontroluj konzolu pre chyby

### QR kód sa negeneruje
- Overí si, že Firestore databáza je dostupná
- Skontroluj Firestore pravidlá

### Potvrdenie z mobilnej aplikácie nefunguje
- Overí si, že mobilná aplikácia má prístup k Firestore
- Skontroluj Firestore pravidlá a ich oprávnenia
