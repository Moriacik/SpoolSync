# Príklady a Best Practices

## 1. Ako spustiť lokálny vývoj

```bash
# 1. Inštaluj závislosti
npm install

# 2. Nakonfiguruj .env.local
cp .env.example .env.local
# Uprav .env.local s Firebase údajmi

# 3. Spusti dev server
npm run dev

# 4. Otvor v prehliadači
# http://localhost:5173/
```

## 2. Príklad: Pridanie novej funkcionality

### Prípad: Pridaj "Logout" gombík

**V App.tsx:**
```tsx
const handleLogout = () => {
  localStorage.removeItem('userId');
  localStorage.removeItem('sessionId');
  setIsAuthenticated(false);
  setUserId(null);
};

// V JSX:
<button onClick={handleLogout}>Odhlásiť sa</button>
```

### Prípad: Pridaj štatistiky

**Vytvor nový komponent `src/components/StatisticsPanel.tsx`:**
```tsx
import { useState, useEffect } from 'react';
import { collection, query, where, getDocs } from 'firebase/firestore';
import { db } from '../config/firebase';

interface Statistic {
  id: string;
  type: string;
  value: number;
  date: Date;
}

export function StatisticsPanel({ userId }: { userId: string }) {
  const [stats, setStats] = useState<Statistic[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const statsRef = collection(db, 'users', userId, 'statistics');
        const q = query(statsRef);
        const snapshot = await getDocs(q);
        
        const data = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        })) as Statistic[];
        
        setStats(data);
      } catch (error) {
        console.error('Error fetching statistics:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [userId]);

  if (loading) return <div>Načítavam štatistiky...</div>;

  return (
    <div className="statistics-panel">
      <h2>Tvoje štatistiky</h2>
      {stats.map(stat => (
        <div key={stat.id} className="stat-item">
          <span>{stat.type}: {stat.value}</span>
          <span>{new Date(stat.date).toLocaleDateString()}</span>
        </div>
      ))}
    </div>
  );
}
```

**Použitie v App.tsx:**
```tsx
import { StatisticsPanel } from './components/StatisticsPanel';

// V JSX:
{isAuthenticated && userId && (
  <>
    <StatisticsPanel userId={userId} />
  </>
)}
```

## 3. Firestore pravidlá - praktické príklady

### Prípustenie prístupu iba vlastným dátam:
```
allow read, write: if request.auth.uid == userId;
```

### Prípustenie vytvorenia dokumentu iba autentifikovaným používateľom:
```
allow create: if request.auth != null;
```

### Overenie, že pole má určitú hodnotu:
```
allow update: if request.data.status == 'approved';
```

### Overenie, že staré dáta existujú pred úpravou:
```
allow update: if resource.exists && 
                 resource.data.owner == request.auth.uid;
```

## 4. Debugging

### V Chrome DevTools:

```javascript
// V Console:

// 1. Skontroluj localStorage
console.log(localStorage.getItem('userId'));
console.log(localStorage.getItem('sessionId'));

// 2. Skontroluj Firestore listener
// Bude vidieť príchodzí/odchádzajúce dáta

// 3. Monitoruj chyby
// Otvor Console tab pre chyby
```

### Logovanie:

```typescript
// Lepšie logovanie:
const logger = {
  info: (msg: string, data?: any) => {
    console.log(`[INFO] ${msg}`, data);
  },
  error: (msg: string, error?: Error) => {
    console.error(`[ERROR] ${msg}`, error);
  },
  warn: (msg: string, data?: any) => {
    console.warn(`[WARN] ${msg}`, data);
  }
};

// Použitie:
logger.info('QR Session created', { sessionId, expiresAt });
logger.error('Firebase error', new Error('Access denied'));
```

## 5. Performance Tips

### Lazy Loading komponentov:

```tsx
import { lazy, Suspense } from 'react';

const StatisticsPanel = lazy(() => 
  import('./components/StatisticsPanel').then(m => ({ default: m.StatisticsPanel }))
);

// Použitie:
<Suspense fallback={<div>Načítavam...</div>}>
  <StatisticsPanel userId={userId} />
</Suspense>
```

### Optimizácia Firestore query-í:

```typescript
// ❌ Zle - číta všetky dokumenty
const snapshot = await getDocs(collection(db, 'qrSessions'));

// ✅ Dobre - filtruje na serveri
const q = query(
  collection(db, 'qrSessions'),
  where('confirmed', '==', true),
  orderBy('confirmedAt', 'desc'),
  limit(10)
);
const snapshot = await getDocs(q);
```

### Memoizácia komponentov:

```tsx
import { memo } from 'react';

const StatCard = memo(({ label, value }: Props) => (
  <div className="stat-card">
    <h3>{label}</h3>
    <p>{value}</p>
  </div>
));
```

## 6. Testing príklady

### Unit test pre QR Service:

```typescript
import { describe, it, expect } from 'vitest';
import { getQRData } from '../services/qrService';

describe('qrService', () => {
  it('should generate valid QR data', () => {
    const session = {
      sessionId: '123',
      requestToken: '456',
      expiresAt: Date.now() + 300000,
      createdAt: Date.now(),
    };

    const qrData = getQRData(session);
    const parsed = JSON.parse(qrData);

    expect(parsed.sessionId).toBe('123');
    expect(parsed.expiresAt).toBeGreaterThan(Date.now());
  });
});
```

### Integration test pre LoginScreen:

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { LoginScreen } from '../components/LoginScreen';

describe('LoginScreen', () => {
  it('should display QR code after clicking button', async () => {
    render(<LoginScreen />);
    
    const button = screen.getByText('Prihlásiť sa cez QR');
    fireEvent.click(button);

    await waitFor(() => {
      expect(screen.getByText(/Naskenujte QR kód/)).toBeInTheDocument();
    });
  });
});
```

## 7. Bezpečnostné opatrenia

```typescript
// 1. Validácia Firebase config
if (!import.meta.env.VITE_FIREBASE_API_KEY) {
  throw new Error('Firebase config is missing');
}

// 2. Validácia QR dát
const validateQRData = (data: unknown): boolean => {
  if (typeof data !== 'object' || data === null) return false;
  
  const obj = data as Record<string, unknown>;
  return (
    typeof obj.sessionId === 'string' &&
    typeof obj.requestToken === 'string' &&
    typeof obj.expiresAt === 'number'
  );
};

// 3. Bezpečné čítanie z localStorage
const safeGetStorage = (key: string): string | null => {
  try {
    return localStorage.getItem(key);
  } catch (e) {
    console.error('Storage access denied:', e);
    return null;
  }
};

// 4. CORS pre API volania (ak je potrebné)
const fetchWithCORS = async (url: string) => {
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'same-origin', // Alebo 'include' pre cross-origin
  });
  return response.json();
};
```

## 8. Deployment checklist

```
Pred deploymentom:
☐ Skontroluj .env.local v git .gitignore
☐ Spusti `npm run build` - žiadne chyby
☐ Spusti `npm run build` - build veľkosť OK
☐ Skontroluj Firestore pravidlá
☐ Skontroluj Firebase Auth (Email/Password)
☐ Skontroluj CORS v Firebase (ak je potrebné)
☐ Testuj QR skenovanie s mobilom
☐ Testuj logout a opätovný login
☐ Zmaž localStorage pred testovaním

Deploy:
☐ Build: npm run build
☐ Deploy na hosting (Firebase, Vercel, atď.)
☐ Skontroluj, že .env.local premenné sú nastavené na serveri
☐ Testuj na produkcii
```

## 9. Časté chyby

### Chyba: "Cannot read property 'uid' of null"
```typescript
// ❌ Zle
const userId = auth.currentUser.uid; // Môže byť null!

// ✅ Dobre
const user = auth.currentUser;
if (!user) {
  console.error('User not authenticated');
  return;
}
const userId = user.uid;
```

### Chyba: "Firestore: Missing or insufficient permissions"
```typescript
// Skontroluj Firestore pravidlá!
// Pravidlo musí dovoliť danú operáciu

// Príklad problému:
// Pravidlo: allow read: if request.auth.uid == userId;
// Ale: userId nie je nastavený v dáte
```

### Chyba: "Invalid JSON in QR code"
```typescript
// ✅ Správny formát:
const qrData = JSON.stringify({
  sessionId: session.sessionId,
  requestToken: session.requestToken,
  expiresAt: session.expiresAt,
});

// ❌ Zle:
const qrData = `${sessionId},${requestToken},${expiresAt}`;
```

## 10. Git workflow

```bash
# 1. Vytvor feature branch
git checkout -b feature/add-statistics

# 2. Pracuj na features
npm run dev

# 3. Commit
git add .
git commit -m "feat: add statistics panel"

# 4. Push
git push origin feature/add-statistics

# 5. Pull request na main
# (GitHub/GitLab)

# 6. Merge a delete branch
git checkout main
git pull origin main
git branch -d feature/add-statistics
```
