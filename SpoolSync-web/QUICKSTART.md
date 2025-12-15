# 🎯 SpoolSync Web - Rýchly Start (5 minút)

## Inštalácia a spustenie

```bash
# 1. Inštaluj (1 minúta)
npm install

# 2. Nakonfiguruj Firebase (2 minúty)
cp .env.example .env.local
# UPRAV .env.local s Firebase údajmi

# 3. Spusti (1 minúta)
npm run dev

# 4. Otvor (30 sekúnd)
# http://localhost:5173/
```

## Firebase Setup (5 minút)

1. Choď na [https://console.firebase.google.com](https://console.firebase.google.com)
2. Vytvor nový projekt
3. Zaregistruj webovú aplikáciu
4. Skopíruj config do `.env.local`
5. Vytvor Firestore databázu
6. Nastav Firestore pravidlá (skopíruj z `firestore.rules`)

## Ako to funguje?

```
┌──────────────────────────────────────────────────────────┐
│                   WEB APLIKÁCIA                          │
├──────────────────────────────────────────────────────────┤
│ 1. Klikni "Prihlásiť sa cez QR"                          │
│ 2. Vygeneruje sa QR kód                                  │
│ 3. Čaká na potvrdenie z mobilnej aplikácie              │
│ 4. Keď mobil potrdí → Presmeruje na Dashboard           │
│ 5. Zobrazuje sa ID používateľa                           │
└──────────────────────────────────────────────────────────┘
                   ↕ Firestore
┌──────────────────────────────────────────────────────────┐
│            ANDROID APLIKÁCIA (BUDÚCNOSŤ)                │
├──────────────────────────────────────────────────────────┤
│ 1. Skane QR kód z webovej stránky                       │
│ 2. Overí, že token nie je expirovaný (5 minút)          │
│ 3. Potvrdí v Firestore s Android user ID                │
│ 4. Vráti sa na AccountScreen                             │
└──────────────────────────────────────────────────────────┘
```

## Súbory na prečítanie

| Súbor | Čas | Účel |
|-------|-----|------|
| MAIN_README.md | 5 min | Prehľad projektu |
| SETUP.md | 10 min | Firebase setup |
| ANDROID_GUIDE.md | 15 min | Android integrácia |
| EXAMPLES.md | 20 min | Kódové príklady |
| API_REFERENCE.md | 15 min | API dokumentácia |
| ROADMAP.md | 5 min | Budúcnosť projektu |

## Kľúčové súbory

```
src/components/LoginScreen.tsx      ← QR login
src/services/qrService.ts            ← QR generovanie
src/hooks/useQRSession.ts            ← Real-time listener
src/config/firebase.ts               ← Firebase config
src/App.tsx                          ← Hlavná logika
```

## Dôležité príkazy

```bash
npm run dev          # Vývoj (HMR)
npm run build        # Build aplikácie
npm run build:watch  # Build s watch-om
npm run preview      # Testovanie build-u
```

## Hesla a tokeny

- **QR Token:** Platí 5 minút
- **Session ID:** Jedinečný pre každý login
- **localStorage:** Uloží userId a sessionId
- **Firebase Auth:** Musí byť nastavená na Android

## Ďalšie kroky

1. ✅ Spusti `npm run dev`
2. ✅ Klikni "Prihlásiť sa cez QR"
3. ✅ Skopíruj si data z QR kódu
4. ✅ Čítaj dokumentáciu ako pokračovať

## Troubleshooting

```
❌ "Missing environment variables"
   → Skopíruj .env.example do .env.local
   → Vyplň Firebase údaje

❌ "Cannot read property 'db'"
   → Skontroluj, že .env.local má správne premenné
   → Skontroluj, že Firebase projekt existuje

❌ "Firestore: Missing permissions"
   → Skontroluj Firestore pravidlá v firestore.rules
   → Aplikuj ich v Firebase Console
```

## Firebase Pravidlá (Copy-Paste)

Choď do Firebase Console → Firestore → Rules a vlož:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /qrSessions/{sessionId} {
      allow create, read;
      allow update: if request.auth != null && 
                       request.data.confirmed == true &&
                       request.data.confirmedBy == request.auth.uid;
    }
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
  }
}
```

## Ďalšie zdroje

- 📖 [Firebase Dokumentácia](https://firebase.google.com/docs)
- 📚 [React Dokumentácia](https://react.dev)
- 🚀 [Vite Dokumentácia](https://vitejs.dev)
- 📱 [Firebase Firestore Guide](https://firebase.google.com/docs/firestore)

---

**Ready to go! 🎉**

Ako sa máš cítiť teraz:
- ✅ Projekt je spustený
- ✅ QR sa generuje
- ✅ Firestore je nakonfigurovaný
- ✅ Android aplikácia čaká

Ďalší krok: Prečítaj si ANDROID_GUIDE.md a implementuj Android app!
