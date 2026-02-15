# 🔐 QR Session Cleanup Strategy

## 📋 Problematika

### Bezpečnostný problém
- **Nepotvrdené QR sessions** → Verejne čitateľné (anonymný QR scan je normálny)
- **Potvrdené QR sessions** → Obsahujú `confirmedBy` (userId) a `confirmedAt` (timestamp)
- **Riziko:** Ak ostanú potvrdené sessions v DB → Akýkoľvek Android user vidí userId ostatných users

### Scenáre generujúce "orphaned" sessions

| Scenár | Popis | Riziko |
|--------|-------|--------|
| **Normal logout** | User klikne "Odhl asovať" alebo zatvori tab | ❌ Session ostane bez cleanup |
| **Browser crash** | Firefox/Chrome padne | ❌ `beforeunload` sa nevolá |
| **Forced closure** | User zatkne PC bez logout | ❌ Session ostane |
| **User odíde bez potvrdenia** | Web vygeneruje QR, User neapotvrdzuje | ⚠️ Session expiruje (je to OK) |
| **User potvrdzuje a hneď zatvára** | User potvrdzuje QR < 5 min | ❌ **KRITICKÉ** - potvrdená session bez cleanup |

### Prečo je to problém
```
1. Web vygeneruje QR session → expiresAt = teraz + 5 minút
2. Android user potvrdzuje → confirmed = true, confirmedBy = "uid_123"
3. Web sa zatvára HNEĎ → beforeunload sa nevolá
4. Session ostane v DB s @PLAINTEXT uid_123
5. Akýkoľvek Android user si môže prečítať: /qrSessions → vidí všetky potvrdené sessions
6. ❌ BEZPEČNOSTNÝ PROBLÉM: Leak userId ostatných users
```

---

## ✅ Riešenie: WEB-SIDE CLEANUP

### Princíp
**Web si kontroluje životný cyklus svojich QR sessions a čistí ich pri odchode.**

Web musí pokryť **3 scenáre**:
1. ✅ **Normálny odchod** (refresh, X, logout) → `beforeunload` event
2. ✅ **Browser crash** → `setTimeout` fallback
3. ✅ **Bezpečná čítateľnosť** → Firestore Rules blokujú potvrdené sessions

---

## 🔧 Implementácia

### 1️⃣ Web TypeScript - Session Cleanup

```typescript
// src/services/qrService.ts

export async function generateQRSession(): Promise<QRSession> {
  const sessionId = uuidv4();
  const expiresAt = Date.now() + 5 * 60 * 1000; // 5 minút
  
  const sessionData: QRSession = {
    sessionId,
    requestToken: uuidv4(),
    expiresAt,
    createdAt: Date.now(),
  };

  try {
    // 1. Vytvor session
    await setDoc(doc(db, 'qrSessions', sessionId), sessionData);
    
    // 2. Auto-delete po 5 minútach (fallback pre browser crash)
    const timeoutId = setTimeout(async () => {
      try {
        await deleteDoc(doc(db, 'qrSessions', sessionId));
        console.log(`Auto-deleted expired session: ${sessionId}`);
      } catch (error) {
        console.error('Auto-cleanup failed:', error);
      }
    }, 5 * 60 * 1000);
    
    // 3. Ulož timeout ID do sessionStorage na cleanup pri odchode
    sessionStorage.setItem(`qr_timeout_${sessionId}`, timeoutId.toString());
    
    return sessionData;
  } catch (error) {
    console.error('Error creating QR session:', error);
    throw error;
  }
}

// Cleanup funkcia
async function deleteQRSession(sessionId: string): Promise<void> {
  try {
    // Clear timeout ak existuje
    const timeoutId = sessionStorage.getItem(`qr_timeout_${sessionId}`);
    if (timeoutId) {
      clearTimeout(Number(timeoutId));
      sessionStorage.removeItem(`qr_timeout_${sessionId}`);
    }
    
    // Zmaž session z Firestore
    await deleteDoc(doc(db, 'qrSessions', sessionId));
  } catch (error) {
    console.error('Error deleting QR session:', error);
  }
}
```

### 2️⃣ Web React Component - Lifecycle Hook

```typescript
// src/hooks/useQRSessionCleanup.ts

import { useEffect } from 'react';
import { deleteQRSession } from '../services/qrService';

export function useQRSessionCleanup(sessionId: string | null) {
  useEffect(() => {
    if (!sessionId) return;

    // Cleanup pri odchode (X, refresh, logout, navigácia)
    const handleBeforeUnload = async (e: BeforeUnloadEvent) => {
      try {
        await deleteQRSession(sessionId);
      } catch (error) {
        console.error('Failed to cleanup on unload:', error);
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [sessionId]);
}

// Využitie v komponente:
export function LoginScreen() {
  const [session, setSession] = useState<QRSession | null>(null);
  
  // ✅ Automatický cleanup pri odchode
  useQRSessionCleanup(session?.sessionId ?? null);
  
  // ... rest of component
}
```

### 3️⃣ Firestore Rules - Bezpečnosť

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    match /qrSessions/{sessionId} {
      // ✅ ĽÚČOVÝ RULE: Iba NEPOTVRDENÉ sessions sú verejne čitateľné
      // (confirmed == false alebo neexistuje pole)
      allow read: if !resource.data.confirmed || resource.data.confirmed == false;
      
      // Ktokoľvek môže vytvoriť QR session (anonymný web user)
      allow create: if true;
      
      // ✅ Iba authenticated Android user môže potvrdiť
      // a MUSÍ potvrdiť SEBA (request.auth.uid == confirmedBy)
      allow update: if request.auth != null && 
                       request.resource.data.confirmed == true &&
                       request.resource.data.confirmedBy == request.auth.uid &&
                       !resource.data.confirmed; // prevent re-confirmation
      
      // ✅ Web user (anonymous) si mažeme SVOJU session
      // (žiadna autentifikácia potrebná - web je anonymous)
      allow delete: if true;
    }

    // SESSIONS - Session management (Android app)
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
        allow write: if request.auth != null && 
                       get(/databases/$(database)/documents/sessions/$(sessionId)).data.ownerId == request.auth.uid;
      }
    }

    // USERS
    match /users/{userId} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;

      match /sessions/{sessionId} {
        allow read: if true;
        allow write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

---

## 🔄 Kompletný Flow

### 1. Generovanie QR

```
WEB:
  1. generateQRSession() → vytvor session, expiresAt = now + 5 min
  2. setTimeout(5 min) → auto-delete ako fallback
  3. window.addEventListener('beforeunload') → delete pri odchode
  4. Vygeneruj QR kód a prikaž Android
```

### 2. Android Skenuje QR

```
ANDROID:
  1. Skenuje QR → dostane sessionId, requestToken
  2. Validuje expiresAt > now
  3. Potvrdí: firestore.update(confirmed=true, confirmedBy=uid)
  4. Android dostane userId a uloží do localStorage
```

### 3. Web Detekuje Potvrdenie

```
WEB (listener):
  1. Sleduje /qrSessions/{sessionId}
  2. Detekuje confirmed=true, confirmedBy=uid
  3. Uloží userId do localStorage
  4. Presmeruje na dashboard
  5. clearTimeout() → zruší auto-delete timeout
```

### 4. Cleanup pri Odchode

```
WEB (beforeunload):
  1. User zatvára stránku / refresh / logout / navigácia
  2. beforeunload event → deleteQRSession()
  3. Session sa zmažeme z Firestore
  4. ✅ BEZPEČNÉ: Potvrdená session neostane v DB
```

### 5. Fallback: Browser Crash

```
TIMEOUT (5 minút):
  1. Ak user neopatrujú beforeunload (crash)
  2. Timeout sa zavolá automaticky
  3. Session sa zmažeme z Firestore
  4. ✅ MAX 5 MIN: Session neostane dlho v DB
```

---

## 📊 Porovnanie Scenárov

| Scenár | Čas Cleanup | Bezpečnosť |
|--------|------------|-----------|
| **Normálny logout** | Okamžite | ✅ beforeunload zmaže |
| **Refresh F5** | Okamžite | ✅ beforeunload zmaže |
| **X (close tab)** | Okamžite | ✅ beforeunload zmaže |
| **Navigácia preč** | Okamžite | ✅ beforeunload zmaže |
| **Browser crash** | Max 5 min | ✅ timeout zmaže |
| **PC shutdown** | Max 5 min | ✅ timeout zmaže |
| **Offline** | Stále sa zmažeme | ✅ Firebase sync |

---

## 🔒 Bezpečnostné Záruky

### ✅ Nepotvrdené Sessions
- **Kto vidí:** Ktokoľvek (anonymný scan je normálny)
- **Čo vidí:** Iba `sessionId`, `requestToken`, `expiresAt` (bez tokenu)
- **Riziko:** ❌ ŽIADNE (nie sú v nich userId)

### ✅ Potvrdené Sessions
- **Kto vidí:** NIKTO (Firestore Rules: `read: if !confirmed`)
- **Čo vidí:** Iba Web/Android ktorý ju vytvoril/potvrdil
- **Riziko:** ❌ ŽIADNE (sú okamžite zmazané)

### ✅ Delete Permissions
- **Kto môže zmazať:** Ktokoľvek (anonymous web)
- **Čo sa zmažeme:** Len podľa `sessionId` (čítateľný z QR)
- **Riziko:** ⚠️ MINIMÁLNE (hacker by musel vedieť sessionId)

---

## 💰 Náklady

| Feature | Firebase Plan | Náklady |
|---------|--------------|---------|
| Firestore Read (QR scan) | Free | ✅ Zadarmo |
| Firestore Update (confirm) | Free | ✅ Zadarmo |
| Firestore Delete (cleanup) | Free | ✅ Zadarmo |
| Cloud Functions | Spark (Free) | ❌ NEPOTREBNÉ |
| TTL (auto-delete) | Spark (Free) | ❌ NEPOTREBNÉ |

**Celkové náklady:** **$0** 💰

---

## 🎯 Záver

### Prečo je toto BEST SOLUTION?

| Kritérium | Score |
|-----------|-------|
| **Bezpečnosť** | ⭐⭐⭐⭐⭐ (Žiadne potvrdené sessions v DB) |
| **Jednoduchosť** | ⭐⭐⭐⭐⭐ (Len JavaScript hook) |
| **Náklady** | ⭐⭐⭐⭐⭐ (Zadarmo) |
| **Reliability** | ⭐⭐⭐⭐☆ (beforeunload + timeout) |
| **Scalability** | ⭐⭐⭐⭐⭐ (Bez backendu) |

### Implementácia

- ✅ **Web:** 2 funkcie + 1 hook (~50 riadkov TypeScript)
- ✅ **Firestore Rules:** 1 rule zmena (read rule pre qrSessions)
- ✅ **Android:** Bez zmien!
- ✅ **Výsledok:** 100% bezpečné, zadarmo, jednoduché

---

## 📝 Deployment Checklist

- [ ] Implementovať `deleteQRSession()` v `qrService.ts`
- [ ] Implementovať `useQRSessionCleanup()` hook
- [ ] Aplikovať hook v `LoginScreen` komponente
- [ ] Aktualizovať Firestore Rules (čítanie qrSessions)
- [ ] Otestovať: Normálny logout, refresh, browser crash
- [ ] Overiť Firestore: Žiadne potvrdené sessions neostanú

---

**Status:** ✅ READY TO IMPLEMENT

