# 🎯 SpoolSync Web - QR Autentifikácia

Webová aplikácia s Firebase QR autentifikáciou pre zobrazovanie štatistík z mobilnej aplikácie.

## 📋 Funkcie

- ✅ **QR Autentifikácia**: Prihlásenie cez QR kód bez hesla
- ✅ **Firebase Firestore**: Real-time synchronizácia medzi webom a mobilom
- ✅ **Bezpečnosť**: Tokenizované relácii s expiráciou
- ✅ **Responsive Design**: Kompatibilné s rôznymi zariadeniami
- ✅ **TypeScript**: Typová bezpečnosť

## 🚀 Rýchly štart

### 1. Príprava

```bash
# Klonuj projekt
git clone <repo-url>
cd SpoolSync-web

# Inštaluj závislosti
npm install
```

### 2. Firebase konfigurácia

1. Vytvor Firebase projekt na [https://console.firebase.google.com](https://console.firebase.google.com)
2. Skopíruj `.env.example` do `.env.local`
3. Vyplň Firebase config:

```env
VITE_FIREBASE_API_KEY=your_key
VITE_FIREBASE_AUTH_DOMAIN=your_domain
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_bucket
VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
VITE_FIREBASE_APP_ID=your_app_id
```

Detailný návod: 📖 [SETUP.md](./SETUP.md)

### 3. Spustenie

```bash
# Vývoj
npm run dev
# Otvorí: http://localhost:5173/

# Produkčný build
npm run build

# Testovanie build-u
npm run preview
```

## 🔐 Ako funguje QR autentifikácia?

### Sekvenčný diagram

```
┌─────────────────┐           Firestore          ┌──────────────────┐
│   Web Browser   │◄────────────────────────────►│ Android App      │
│ (bez login-u)   │                              │ (prihlásený)     │
└─────────────────┘                              └──────────────────┘
         │                                               │
         │ 1. "Prihlásiť sa cez QR"                     │
         ├───► Generate sessionId + requestToken       │
         │                                              │
         │ 2. Vygeneruj QR kód                         │
         │                                              │
         │ 3. Čakaj na Firestore zmenu                │
         │◄─────────────────────── 4. Skane QR kód    │
         │                    5. Dekóduj data          │
         │                    6. Overí expiráciu       │
         │◄─────────────────────── 7. Potvrd. Firestore│
         │                                              │
         │ 8. Recepcií update                          │
         │ 9. Ulož userId + sessionId                  │
         │ 10. Presmeruj na Dashboard                  │
```

### Detaily:

**Web (bez prihlásenia):**
1. Vygeneruje `sessionId` a `requestToken`
2. Uloží do Firestore s TTL 5 minút
3. Generuje QR kód s týmito údajmi
4. Čaká na zmenu dokumentu

**Android (prihlásený):**
1. Skane QR kód
2. Dekóduje JSON: `{sessionId, requestToken, expiresAt}`
3. Overí, že token nie je starší ako 5 minút
4. Potvrdí v Firestore: `confirmed=true, confirmedBy=userId`

**Web (po potvrdení):**
1. Dostane notifikáciu o zmene
2. Uloží `userId` a `sessionId` do localStorage
3. Presmeruje na dashboard so štatistikami

## 📁 Štruktúra projektu

```
src/
├── components/
│   └── LoginScreen.tsx          # QR login komponenta
├── config/
│   └── firebase.ts              # Firebase inicializácia
├── hooks/
│   └── useQRSession.ts          # Custom hook pre QR session
├── services/
│   └── qrService.ts             # QR generovanie a dáta
├── styles/
│   └── LoginScreen.css          # Štýly
├── App.tsx                       # Hlavná aplikácia
├── App.css                       # App štýly
├── main.tsx                      # Entry point
└── index.css                     # Globálne štýly
```

## 🔧 Technológie

| Technológia | Verzija | Účel |
|-------------|---------|------|
| React | 18+ | Frontend framework |
| TypeScript | 5+ | Typová bezpečnosť |
| Vite | 7+ | Build tool |
| Firebase | Latest | Backend + Auth + Firestore |
| qrcode | Latest | QR kód generátor |
| UUID | Latest | Jedinečné ID |

## 📱 Android implementácia

Ak chceš vytvoriť Android aplikáciu, ktorá bude pracovať s touto webkou:

- 📖 [Podrobný Android guide](./ANDROID_GUIDE.md)
- Musíš skenať QR kód a potvrdať v Firestore
- Musíš byť prihlásený do Firebase Auth

## 🔒 Bezpečnosť

- ✅ QR token platí len 5 minút
- ✅ Web nepotrebuje heslo
- ✅ Android musí byť prihlásený
- ✅ Firestore pravidlá kontrolujú oprávnenia
- ✅ localStorage sa zbavuje po čase

## 📖 Dokumentácia

- **[SETUP.md](./SETUP.md)** - Detailný setup Firebase
- **[ANDROID_GUIDE.md](./ANDROID_GUIDE.md)** - Android implementácia
- **.env.example** - Príklad env. premenných

## 🛠 Development

```bash
# Hot reload vývoj
npm run dev

# TypeScript build
npm run build

# Type checking
npx tsc --noEmit

# Linting (ak je skonfigurované)
npm run lint
```

## 🚀 Deploy

```bash
# Build aplikácie
npm run build

# Výsledky sú v `dist/` priečinku
# Nahraj na Firebase Hosting, Vercel, Netlify, atď.

# Firebase Hosting (ak máš Firebase CLI)
firebase deploy
```

## ⚠️ Troubleshooting

| Problém | Riešenie |
|---------|---------|
| "Cannot find module" | `npm install` |
| Firebase chyby | Skontroluj `.env.local` |
| QR kód sa negeneruje | Skontroluj Firestore prístup |
| Browser neaktualizuje | F5 refresh |

## 📞 Support

- Skontroluj [SETUP.md](./SETUP.md) pre Firebase setup
- Skontroluj [ANDROID_GUIDE.md](./ANDROID_GUIDE.md) pre Android
- Skontroluj console (F12) pre debugging

## 📄 Licencia

MIT

---

**Poznámka:** Pamätaj, že web aplikácia čaká na potvrdenie z mobilnej aplikácie. Bez mobilnej aplikácie, ktorá potvrdí QR, sa web prihlásenie nedokončí.
