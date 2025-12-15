# ✅ Firestore Rules - Integrácia QR + Android

## 📋 Čo Som Urobil

Kombinoval som tvoje **existujúce Android pravidlá** s **novými QR pravidlami** pre web.

---

## 🔗 Nové Pravidlá

Skopíruj a vlož do Firebase Console:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // ============================================
    // QR SESSIONS - FOR WEB AUTHENTICATION
    // ============================================
    match /qrSessions/{sessionId} {
      // Anyone (anonymous web users) can create QR sessions
      allow create: if true;
      
      // Anyone can read to verify session exists
      allow read: if true;
      
      // Only authenticated users (Android) can update
      // to confirm with their own ID
      allow update: if request.auth != null && 
                       request.data.confirmed == true &&
                       request.data.confirmedBy == request.auth.uid;
      
      // Prevent deletion
      allow delete: if false;
    }

    // ============================================
    // EXISTING RULES - SESSIONS (ANDROID)
    // ============================================
    match /sessions/{sessionId} {
        allow read: if request.auth != null;
        allow create: if request.auth != null;
        allow update: if request.auth != null && (
            resource.data.ownerId == request.auth.uid || 
            request.resource.data.participants.hasAny([request.auth.uid])
        );
        allow delete: if request.auth != null && resource.data.ownerId == request.auth.uid;

        match /members/{memberId} {
            allow read: if request.auth != null;
            allow write: if request.auth != null;
        }

        match /filaments/{filamentId} {
            allow read: if request.auth != null;
            allow write: if request.auth != null && get(/databases/$(database)/documents/sessions/$(sessionId)).data.ownerId == request.auth.uid;
        }
    }

    // ============================================
    // EXISTING RULES - USERS (ANDROID)
    // ============================================
    match /users/{userId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;

        match /sessions/{sessionId} {
            allow read, write: if request.auth != null && request.auth.uid == userId;
        }
    }
  }
}
```

---

## 🚀 Ako Aplikovať

1. **Otvor Firebase Console:**
   https://console.firebase.google.com/project/spoolsync/firestore/rules

2. **Skopíruj vyššie pravidlá** (všetko čo je v `code` bloku)

3. **Vymeniť v Firebase Console:**
   - Vyznač všetky staré pravidlá (Ctrl+A)
   - Vymeniť za nové (Ctrl+V)

4. **Klikni "Publish"** 🔵

5. **Čakaj na publikovanie** (~30 sekúnd)

---

## ✨ Čo Se Zmienilo

### ✅ Pridané (Pre Web):
```
match /qrSessions/{sessionId} {
  allow create: if true;                    // Web bez auth
  allow read: if true;                      // Web bez auth
  allow update: if request.auth != null;    // Android s auth
}
```

### ✅ Zachované (Pre Android):
```
match /sessions/{sessionId}       // Tvoj session management
match /members/{memberId}         // Tvoji medlemovia
match /filaments/{filamentId}     // Tvoj filament tracking
match /users/{userId}             // Tvoji user data
```

---

## 🧪 Test

### Web App:
```
1. Otvor: http://localhost:5173/
2. Klikni "Prihlásiť sa cez QR"
3. QR sa vygeneruje
4. Console: ✅ Session saved to Firestore
5. Web čaká na Android potvrdenie
```

### Android App:
```
1. Android skane QR kód
2. Android potvrdí v Firestore
3. Web dostane notifikáciu
4. Dashboard sa zobrazí
```

---

## 🔒 Bezpečnosť

| Kolekcia | Web | Android |
|----------|-----|---------|
| qrSessions | ✅ Create, Read | ✅ Update (confirm) |
| sessions | ❌ Nič | ✅ Podľa vlastníka |
| members | ❌ Nič | ✅ Autentifikovaní |
| filaments | ❌ Nič | ✅ Podľa vlastníka |
| users | ❌ Nič | ✅ Svojí user |

---

## ✅ Checklist

```
☑ Skopíroval som nové pravidlá
☑ Publikoval som ich vo Firebase
☑ Web app testuje QR generovanie
☑ Android app je prihlásený
☑ All is working ✨
```

---

## 📞 Ak Niečo Nefunguje

### ❌ "Permission denied"
- Čaká na publikovanie pravidiel (~30 sec)
- Refresh prehliadač

### ❌ "Web negeneruje QR"
- Skontroluj console errory (F12)
- Skontroluj .env.local
- Skontroluj Firestore je dostupný

### ❌ "Android nemôže potvrdiť"
- Android musí byť prihlásený v Firebase Auth
- Pravidlá musia byť publikované
- Update musí mať: `confirmed: true, confirmedBy: userId`

---

**Status:** 🟢 Pravidlá sú pripravené na publikovanie!

Publikuj a testuj! 🚀
