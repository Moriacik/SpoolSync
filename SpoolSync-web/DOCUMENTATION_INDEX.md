# 📚 SpoolSync Web - Dokumentácia Index

## 🚀 Rýchly Start

**Čas: 5 minút**

Začni tu ak chceš rýchlo spustiť aplikáciu:
- 📄 [QUICKSTART.md](./QUICKSTART.md) - 5-minútový setup

## 📖 Hlavná Dokumentácia

**Základné informácie:**
- 📄 [PROJECT_SUMMARY.md](./PROJECT_SUMMARY.md) - Prehľad projektu
- 📄 [MAIN_README.md](./MAIN_README.md) - Komplexný README
- 📄 [COMPLETION.md](./COMPLETION.md) - Completion checklist

## 🔧 Setup & Configuration

**Firebase a Environment:**
- 📄 [SETUP.md](./SETUP.md) - Detailný Firebase setup (10 min)
- 📄 [.env.example](./.env.example) - Environment premenné template
- 📄 [firestore.rules](./firestore.rules) - Firestore bezpečnostné pravidlá

## 💻 Developer Guides

**Kódovanie a implementácia:**
- 📄 [EXAMPLES.md](./EXAMPLES.md) - Kódové príklady a best practices (20 min)
- 📄 [API_REFERENCE.md](./API_REFERENCE.md) - API dokumentácia (15 min)
- 📄 [ANDROID_GUIDE.md](./ANDROID_GUIDE.md) - Android app implementácia (15 min)

## 🛣️ Budúcnosť

**Plány a roadmap:**
- 📄 [ROADMAP.md](./ROADMAP.md) - Budúce features a timeline

## 📁 Projekt Štruktúra

```
├── src/
│   ├── components/LoginScreen.tsx     ← QR login UI
│   ├── config/firebase.ts              ← Firebase inicializácia
│   ├── hooks/useQRSession.ts           ← Real-time listener
│   ├── services/qrService.ts           ← QR logika
│   ├── styles/LoginScreen.css          ← QR štýly
│   ├── App.tsx                         ← Hlavná aplikácia
│   └── ...ostatné
├── QUICKSTART.md                       ← Začni tu! ⭐
├── SETUP.md                            ← Firebase setup
├── ANDROID_GUIDE.md                    ← Android app
├── EXAMPLES.md                         ← Príklady kódu
├── API_REFERENCE.md                    ← API docs
└── ...ostatná dokumentácia
```

## 🎯 Podľa Úlohy

### Chcem spustiť aplikáciu
1. Čítaj: [QUICKSTART.md](./QUICKSTART.md)
2. Spusti: `npm install && npm run dev`

### Chcem nakonfigurovať Firebase
1. Čítaj: [SETUP.md](./SETUP.md)
2. Skopíruj: `firestore.rules`
3. Nakonfiguruj: `.env.local`

### Chcem vytvoriť Android app
1. Čítaj: [ANDROID_GUIDE.md](./ANDROID_GUIDE.md)
2. Implementuj: QR skenovanie
3. Potvrd: v Firestore

### Chcem rozšíriť aplikáciu
1. Čítaj: [EXAMPLES.md](./EXAMPLES.md)
2. Čítaj: [API_REFERENCE.md](./API_REFERENCE.md)
3. Modifikuj: `src/` súbory

### Chcem vedieť budúcnosť projektu
1. Čítaj: [ROADMAP.md](./ROADMAP.md)
2. Čítaj: [COMPLETION.md](./COMPLETION.md)

## 🔑 Kľúčové Koncepty

### QR Autentifikácia
```
1. Web generuje sessionId + requestToken (5 min TTL)
2. Web zobrazuje QR kód
3. Android skane QR kód
4. Android potvrdí v Firestore
5. Web presmeruje na Dashboard
```

Detaily: [ANDROID_GUIDE.md](./ANDROID_GUIDE.md)

### Firestore Štruktúra
```
qrSessions/{sessionId}
  - sessionId
  - requestToken
  - expiresAt
  - confirmed (boolean)
  - confirmedBy (userId)
  - confirmedAt (timestamp)
```

Detaily: [API_REFERENCE.md](./API_REFERENCE.md)

### Tech Stack
```
Frontend:  React + TypeScript + Vite
Backend:   Firebase + Firestore
Libraries: qrcode, uuid
```

Detaily: [MAIN_README.md](./MAIN_README.md)

## 📊 Dokumentácia Map

```
Nový Používateľ?
    ↓
Čítaj: QUICKSTART.md (5 min)
    ↓
Spusti: npm run dev
    ↓
Potrebuješ Firebase?
    ├─→ Čítaj: SETUP.md (10 min)
    └─→ Aplikuj: firestore.rules
    ↓
Chceš kódovať?
    ├─→ Čítaj: EXAMPLES.md (20 min)
    ├─→ Čítaj: API_REFERENCE.md (15 min)
    └─→ Modifikuj: src/ súbory
    ↓
Chceš Android?
    └─→ Čítaj: ANDROID_GUIDE.md (15 min)
```

## ⏱️ Čas čítania

| Dokument | Čas | Typ |
|----------|-----|-----|
| QUICKSTART.md | 5 min | 🚀 Start |
| PROJECT_SUMMARY.md | 5 min | 📊 Overview |
| SETUP.md | 10 min | 🔧 Config |
| MAIN_README.md | 10 min | 📖 Guide |
| EXAMPLES.md | 20 min | 💻 Code |
| API_REFERENCE.md | 15 min | 📚 Docs |
| ANDROID_GUIDE.md | 15 min | 📱 Mobile |
| ROADMAP.md | 5 min | 🛣️ Future |

**Celkový čas:** ~85 minút pre komplexný prehľad

## 🎓 Odporúčaný Poradie

### Deň 1 - Setup (30 minút)
```
1. QUICKSTART.md (5 min)
2. SETUP.md (10 min)
3. npm run dev (5 min)
4. Test QR generovanie (10 min)
```

### Deň 2 - Development (1 hodina)
```
1. EXAMPLES.md (20 min)
2. API_REFERENCE.md (15 min)
3. Modifikuj komponenty (25 min)
4. Test na mobile (??  min)
```

### Deň 3 - Android (1 hodina)
```
1. ANDROID_GUIDE.md (15 min)
2. Setup Android projekt (30 min)
3. Implementuj QR scanning (15 min)
4. Testuj integraciju (??  min)
```

## 🔍 Rýchle References

### Commands
```bash
npm run dev          # Vývoj
npm run build        # Build
npm run preview      # Testovanie
```

### Firestore Rules
Skopíruj z: [firestore.rules](./firestore.rules)

### Firebase Config
Template v: [.env.example](./.env.example)

### QR Data Structure
```json
{
  "sessionId": "uuid",
  "requestToken": "uuid",
  "expiresAt": 1734289200000
}
```

## 📞 Otázky?

1. **Ako spustiť?** → [QUICKSTART.md](./QUICKSTART.md)
2. **Firebase error?** → [SETUP.md](./SETUP.md)
3. **Ako kódovať?** → [EXAMPLES.md](./EXAMPLES.md)
4. **Android app?** → [ANDROID_GUIDE.md](./ANDROID_GUIDE.md)
5. **API info?** → [API_REFERENCE.md](./API_REFERENCE.md)
6. **Budúcnosť?** → [ROADMAP.md](./ROADMAP.md)

## ✅ Checklist Pred Začatím

- [ ] Node.js je inštalovaný (`node --version`)
- [ ] npm je inštalovaný (`npm --version`)
- [ ] Máš Firebase projekt
- [ ] Prebraný `.env.example` → `.env.local`

Ak všetko OK, choď na [QUICKSTART.md](./QUICKSTART.md)! 🚀

---

**Obsah aktualizovaný:** December 15, 2025
**Status:** ✅ Hotovo a Testované
**Version:** 1.0.0-beta
