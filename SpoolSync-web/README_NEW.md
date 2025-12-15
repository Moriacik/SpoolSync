# SpoolSync Web - QR Authentication Dashboard

React + TypeScript aplikácia s Firebase QR autentifikáciou pre zobrazovanie štatistík.

## Funkcie

- **QR Autentifikácia**: Prihlásenie pomocou QR kódu zo mobilnej aplikácie
- **Firebase Firestore**: Synchronizácia dát medzi webom a mobilnou aplikáciou
- **Responsive Design**: Kompatibilné s rôznymi veľkosťami obrazovky
- **TypeScript**: Typová bezpečnosť v celom projekte

## Inštalácia

1. Klonuj repozitár:
```bash
git clone <repo-url>
cd SpoolSync-web
```

2. Inštaluj závislosti:
```bash
npm install
```

3. Vytvor `.env.local` súbor na základe `.env.example`:
```bash
cp .env.example .env.local
```

4. Vyplň Firebase konfiguráciu v `.env.local`:
```env
VITE_FIREBASE_API_KEY=your_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_auth_domain
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_storage_bucket
VITE_FIREBASE_MESSAGING_SENDER_ID=your_messaging_sender_id
VITE_FIREBASE_APP_ID=your_app_id
```

## Vývoj

Spustenie vývojového servera:
```bash
npm run dev
```

Server bude dostupný na `http://localhost:5173/`

## Build

Vytvorenie produkčnej verzie:
```bash
npm run build
```

## Štruktúra projektu

```
src/
├── components/        # React komponenty
│   └── LoginScreen.tsx
├── config/           # Konfigurácia (Firebase)
│   └── firebase.ts
├── hooks/            # Custom React hooks
│   └── useQRSession.ts
├── services/         # Obchodné logiky a API volania
│   └── qrService.ts
├── styles/           # CSS štýly
│   └── LoginScreen.css
├── App.tsx           # Hlavná aplikácia
├── App.css
├── main.tsx          # Entry point
└── index.css         # Globálne štýly
```

## Flow QR Autentifikácie

### Na webe (bez prihlásenia):
1. User navštívi web stránku
2. Klikne na "Prihlásiť sa cez QR"
3. Web generuje `sessionId` a `requestToken`
4. QR kód sa zobrazí na webe
5. Web čaká na zmenu v Firestore

### Na mobilnej aplikácii:
1. User skane QR kód
2. Aplikácia dekóduje údaje
3. Aplikácia potvrdí v Firestore s `userId`
4. Aplikácia sa vráti na AccountScreen

### Späť na webe:
1. Web dostane notifikáciu
2. Presmeruje na stránku so štatistikami
3. ID používateľa sa zobrazí

## Bezpečnosť

- `requestToken` platí len 5 minút
- User sa na webe neprihlasuje (nevyžaduje heslo)
- Mobile musí byť prihlásený do Firebase Auth
- Firestore dáta sú overené na backendu

## Technológie

- **React 18** - Frontend framework
- **TypeScript** - Typová bezpečnosť
- **Vite** - Build tool
- **Firebase** - Backend a autentifikácia
- **qrcode.react** - QR generátor
- **UUID** - Jedinečné identifikátory

## Licencia

MIT
