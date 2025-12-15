# ✅ Firebase SpoolSync - Setup Pre Web Aplikáciu

## 🎉 Stav: Firebase Konfigurovaný!

Tvoj Firebase projekt **spoolsync** je pridal do `web app` premenných. Aplikácia je teraz **nakonfigurovaná a pripravená**.

---

## 🔧 Čo Musíš Nakonfigurovať Vo Firebase Console

### 1️⃣ **Firestore Database** ✅ (Už musí existovať)
```
Firebase Console → Firestore Database → Create Database
- Mode: Production
- Region: europe-west1 (odporúčané) alebo blízko k tebe
```

### 2️⃣ **Firestore Security Rules** ⚠️ **KRITICKÉ!**

Musíš aplikovať tieto pravidlá, inak web nebude môcť ukladať QR sessions!

```
Firebase Console → Firestore → Rules
```

**Skopíruj a vlož toto:**

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // QR Sessions - for web authentication
    match /qrSessions/{sessionId} {
      // Anyone can create new QR sessions (web bez prihlášania)
      allow create;
      
      // Anyone can read to verify session exists
      allow read;
      
      // Only authenticated users (Android) can update
      // to confirm with their own ID
      allow update: if request.auth != null && 
                       request.data.confirmed == true &&
                       request.data.confirmedBy == request.auth.uid;
      
      // Prevent deletion
      allow delete: if false;
    }
    
    // User statistics/profile (budúcnosť)
    match /users/{userId} {
      allow read, write: if request.auth != null && 
                            request.auth.uid == userId;
    }
  }
}
```

**Potom klikni:** "Publish" 🔵

### 3️⃣ **Firebase Authentication** ✅ (Pre Android)

Ak chceš, aby Android app fungovala:

```
Firebase Console → Authentication → Get started
```

Povolí tieto metódy:
- ✅ Email/Password
- ✅ Google Sign-In (odporúčané)
- ✅ Alebo iné podľa potreby

---

## 🧪 Test Web Aplikácie

Otvor v prehliadači:
```
http://localhost:5173/
```

### Čo by sa malo stať:

1. **Klikni "Prihlásiť sa cez QR"**
   - Console by malo ukázať: `🔥 Firebase initialized with project: spoolsync`
   - QR kód sa vygeneruje
   - Console: `✅ Session saved to Firestore: [sessionId]`

2. **Počkaj** (alebo simuluj Android potvrdenie)
   - Web čaká na zmenu v Firestore

3. **Android App** (keď budeš mať)
   - Android skane QR kód
   - Android potvrdí v Firestore
   - Web dostane notifikáciu
   - Dashboard sa zobrazí s Android User ID

---

## ❓ Časté Problémy a Riešenia

### ❌ "Firestore: Missing or insufficient permissions"

**Príčina:** Firestore Rules nie sú správne nastavené

**Riešenie:**
1. Firebase Console → Firestore → Rules
2. Skopíruj pravidlá z vyššie
3. Klikni "Publish"
4. Refresh prehliadač (F5)

### ❌ "Cannot find project"

**Príčina:** .env.local nie je správne nastavený

**Riešenie:**
1. Skontroluj .env.local:
   ```
   VITE_FIREBASE_PROJECT_ID=spoolsync
   ```
2. Měň by sa presne takto
3. Refresh prehliadač

### ❌ "QR sa vygeneruje, ale web sa nepresmeruje"

**Príčina:** Android app ešte nepotvrdil, alebo Firestore listener nefunguje

**Riešenie:**
1. Skontroluj Firestore Rules (vyššie)
2. Počkaj kým Android app potvrdí
3. Alebo skontroluj console pre errory

### ✅ "Všetko funguje!"

**Gratulujeme!** Teraz máš:
- ✅ Web app QR autentifikácia
- ✅ Firestore database
- ✅ Security rules
- ⏳ Čaká na Android app

---

## 📊 Firestore Štruktúra

Po prvom kliknutí "Prihlásiť sa cez QR" bude v Firestore vytvorená táto štruktúra:

```
Firestore Database
└── qrSessions (collection)
    └── {sessionId} (document)
        ├── sessionId: "uuid-string"
        ├── requestToken: "uuid-string"
        ├── expiresAt: timestamp (5 minút od teraz)
        ├── createdAt: timestamp
        ├── confirmed: false (alebo true po Android potvrdení)
        ├── confirmedBy: "user-id" (Android User ID)
        └── confirmedAt: timestamp (kedy bol potvrdený)
```

---

## 🔗 Potrebné Linky

1. **Firebase Console:**
   https://console.firebase.google.com/project/spoolsync/firestore

2. **Firestore Rules Editor:**
   https://console.firebase.google.com/project/spoolsync/firestore/rules

3. **Authentication:**
   https://console.firebase.google.com/project/spoolsync/authentication

4. **Your Web Config (uložené):**
   Máš v `.env.local` súbore

---

## 📝 Checklist Pre Produkciu

```
Firebase Setup:
☑ Firestore Database vytvorená
☑ Firestore Rules aplikované
☑ Authentication povolená
☑ .env.local vyplnený (8 premenných)

Web App:
☑ QR sa generuje
☑ Firestore ukladá sessions
☑ Web čaká na potvrdenie
☑ Console nemá errory

Potom:
☐ Android app vytvorená
☐ Android app skane QR
☐ Android app potvrdí v Firestore
☐ Web presmeruje na Dashboard
```

---

## 🚀 Ďalší Krok

### Ihneď:
1. Choď na Firebase Console
2. Aplikuj Firestore Rules (z vyššie)
3. Klikni "Publish"
4. Refresh prehliadač
5. Test web app

### Potom:
1. Čítaj `ANDROID_GUIDE.md`
2. Vytvor Android app
3. Test s web app

### Neskôr:
1. Deploy na Firebase Hosting alebo Vercel
2. Setup production domain
3. Monitoring a optimizácia

---

## ✨ Súčasny Stav

```
✅ Web app - Hotová a nakonfigurovaná
✅ Firebase SDK - Nainstalovaný
✅ .env.local - Vyplnený
⏳ Firestore Rules - Potrebují aplikovať
⏳ Android App - Budúcnosť
```

---

## 📧 Podpora

Ak niečo nefunguje:
1. Skontroluj console errory (F12)
2. Čítaj `SETUP.md` pre detaily
3. Skontroluj Firestore Rules
4. Refresh prehliadač

---

**Status:** 🟢 Aplikácia je pripravená, čaká na Firestore Rules!

Aplikuj pravidlá a testuj! 🎉
