# ✅ SpoolSync Web - Stav a Požiadavky

## 🟢 Aktuálny Stav

Aplikácia **JE FUNKČNÁ** a beží na `http://localhost:5173/`

### Čo Funguje Teraz:
- ✅ QR generovanie
- ✅ QR zobrazovanie
- ✅ Demo režim (bez Firebase)
- ✅ Simulácia Android potvrdenia (tlačidlo)
- ✅ Dashboard po prihlásení
- ✅ localStorage persistence
- ✅ Responsive design

### Demo Test:
1. Klikni **"Prihlásiť sa cez QR"** → QR sa vygeneruje
2. Klikni **"📱 Simulovať Android"** → Simuluješ Android potvrdenie
3. Automaticky sa presmeruje na Dashboard
4. Vidíš ID používateľa

---

## 📋 Na Plnú Funkciu Potrebuješ:

### 1️⃣ Firebase Projekt (Zadarmo)
```
⏱️ Čas: 5-10 minút
📍 Stránka: https://console.firebase.google.com
```

**Kroki:**
1. Vytvor nový projekt
2. Zaregistruj webovú aplikáciu
3. Skopíruj Firebase config do `.env.local`
4. Vytvor Firestore databázu
5. Nastav Firestore pravidlá (z `firestore.rules`)

**Výsledok:** Web bude môcť ukladať QR sessions do Firestore

### 2️⃣ Firebase Konfigurácia v `.env.local`
```
⏱️ Čas: 2-5 minút
📍 Súbor: .env.local
```

```env
VITE_FIREBASE_API_KEY=your_actual_key
VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_bucket.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
VITE_FIREBASE_APP_ID=your_app_id
```

**Ako dostať tieto údaje:**
1. Choď na Firebase Console
2. Project Settings
3. Skopíruj konfiguráciu

### 3️⃣ Firestore Bezpečnostné Pravidlá
```
⏱️ Čas: 2 minúty
📍 Súbor: firestore.rules
```

**Ako aplikovať:**
1. Firebase Console → Firestore → Rules
2. Skopíruj obsah z `firestore.rules`
3. Vlož do Rules editora
4. Klikni "Publish"

### 4️⃣ Android Aplikácia (Budúcnosť)
```
⏱️ Čas: 2-3 dni
📍 Súbor: ANDROID_GUIDE.md
```

Na to aby fungoval **skutočný** login:
- Android app musí skenať QR kód
- Potvrdí v Firestore
- Web dostane notifikáciu

**Zatiaľ:** Môžeš testovať tlačidlom "📱 Simulovať Android"

---

## 🚀 Ako Spustiť Teraz

```bash
# Ak ešte nebeží
npm run dev

# Otvor prehliadač
http://localhost:5173/

# Klikni "Prihlásiť sa cez QR"
# Klikni "📱 Simulovať Android"
# Vidíš dashboard ✅
```

---

## 📊 Požiadavky podľa Fázy

### 🔵 FÁZA 1: DEMO (TERAZ HOTOVO)
```
✅ Web aplikácia funguje
✅ QR sa generuje
✅ Simulácia Android funguje
✅ Dashboard funguje
```

### 🟡 FÁZA 2: FIREBASE (3-5 MINÚT)
```
❌ → ✅ Firebase projekt
❌ → ✅ .env.local konfigurácia
❌ → ✅ Firestore pravidlá
```

### 🟠 FÁZA 3: ANDROID APP (2-3 DNI)
```
❌ → ✅ Android projektu
❌ → ✅ QR skenovanie
❌ → ✅ Firestore integrácia
❌ → ✅ Firebase Auth
```

### 🟢 FÁZA 4: PRODUKCIJA (1 DAŇ)
```
❌ → ✅ Build aplikácie
❌ → ✅ Deploy (Firebase Hosting / Vercel / Netlify)
```

---

## 🔧 Čo Potrebuješ na Začiatok

| Položka | Čas | Dopad |
|---------|-----|-------|
| Node.js + npm | ✅ Máš | - |
| React + TypeScript | ✅ Máš | - |
| Firebase Account | ❌ Potrebuješ | **KRITICKÉ** |
| Firebase Project | ❌ Potrebuješ | **KRITICKÉ** |
| Firestore Database | ❌ Potrebuješ | **KRITICKÉ** |
| Firestore Rules | ❌ Potrebuješ | **KRITICKÉ** |
| .env.local | ❌ Potrebuješ | **KRITICKÉ** |
| Android App | ❌ Potrebuješ (neskôr) | ODPORÚČANÉ |

---

## 📁 Štruktúra Projektu

```
SpoolSync-web/
├── src/
│   ├── components/LoginScreen.tsx     ✅ HOTOVO
│   ├── services/qrService.ts          ✅ HOTOVO (demo + Firebase)
│   ├── hooks/useQRSession.ts          ✅ HOTOVO (demo + Firebase)
│   ├── config/firebase.ts             ✅ HOTOVO
│   └── App.tsx                        ✅ HOTOVO
├── .env.local                         ❌ POTREBUJEŠ VYPLNIŤ
├── SETUP.md                           📖 Návod
├── ANDROID_GUIDE.md                   📖 Návod
└── firestore.rules                    🔒 Bezpečnosť
```

---

## ✅ Firebase Setup Checklist

```
Ak chceš plnú funkciu, musíš:

☐ 1. Mať Firebase Account (free)
     https://console.firebase.google.com
     
☐ 2. Vytvoril Firebase Project
     Name: SpoolSync (alebo ľubovoľný)
     
☐ 3. Registroval Web App
     Skopíruj config
     
☐ 4. Vyplnil .env.local
     VITE_FIREBASE_API_KEY=...
     ... (ostatné 5 premenných)
     
☐ 5. Vytvoril Firestore Database
     Mode: Production
     
☐ 6. Aplikoval Firestore Rules
     Skopíruj z firestore.rules
     
☐ 7. Spustil: npm run dev
     Testuje QR - bez demo tlačidla
     
☐ 8. Aplikácia funguje s Firestore
```

---

## 📱 Android App - Čo Treba Vedieť

**Zatiaľ nie je potrebná** na testovanie demo.

Keď ju vytváraš:
1. Čítaj: `ANDROID_GUIDE.md`
2. Android musí byť prihlásený v Firebase
3. Android skane QR kód
4. Android potvrdí v Firestore
5. Web dostane notifikáciu

**Demo režim:** Tlačidlo "📱 Simulovať Android" robí presne to isté

---

## 🎯 Rýchly Plán

### Dnes (30 minút):
```
1. Firebase Account vytvorenie (5 min)
2. Firebase Project setup (10 min)
3. Firestore Database (5 min)
4. Firestore Rules (5 min)
5. .env.local vyplnenie (5 min)
```

### Čoskoro (1-2 dni):
```
1. Android app setup
2. QR skenovanie
3. Firestore integrácia
```

### Neskôr:
```
1. Štatistiky
2. Deploy na produkciu
3. Monitoring
```

---

## 💡 Súčasne

**Teraz môžeš:**
- ✅ Testovať frontend bez Firebase
- ✅ Vidieť ako funguje QR
- ✅ Vidieť ako funguje prihlásenie
- ✅ Vidieť ako funguje dashboard
- ✅ Simulovať Android prihlásenie

**Potrebuješ na produkciu:**
- ❌ Firebase
- ❌ Android app

---

## 📖 Dokumentácia

| Dokument | Prečo |
|----------|-------|
| [QUICKSTART.md](./QUICKSTART.md) | Rýchly start |
| [SETUP.md](./SETUP.md) | Firebase setup |
| [ANDROID_GUIDE.md](./ANDROID_GUIDE.md) | Android app |
| [API_REFERENCE.md](./API_REFERENCE.md) | API details |
| [EXAMPLES.md](./EXAMPLES.md) | Code examples |

---

## 🔗 Užitočné Linky

- 🎮 [Firebase Console](https://console.firebase.google.com)
- 📚 [Firebase Docs](https://firebase.google.com/docs)
- ⚛️ [React Docs](https://react.dev)
- 🚀 [Vite Docs](https://vitejs.dev)

---

## ❓ Časté Otázky

**Q: Potrebujem Firebase aby som testoval?**
A: Nie, demo režim funguje bez Firebase. Ale na production potrebuješ.

**Q: Ako viem, či som v demo režime?**
A: V Console vidíš "📝 DEMO MODE" správy. Ak máš Firebase, správy budú o Firestore.

**Q: Keď si skopírujem .env.local, čo sa zmení?**
A: Web sa prepne z DEMO do PROD režimu. QR sessions budú v Firestore.

**Q: Ako testujem bez Android app?**
A: Tlačidlo "📱 Simulovať Android" robí presne to isté.

**Q: Keď nasadím na produkciu, čo všetko treba?**
A: Firebase Hosting + Android app + Database backup

---

**Status:** 🟢 Aplikácia je funkčná

Ďalší krok: Firebase setup (5-10 minút) alebo Android app (2-3 dni)
