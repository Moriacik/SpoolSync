import { collection, query, orderBy, getDocs, onSnapshot } from 'firebase/firestore';
import { db } from '../config/firebase';

export interface Filament {
  id: string;
  filamentId: string;
  type: string;
  brand: string;
  color: string;
  weight: number;
  remainingWeight: number;
  status: 'active' | 'empty' | 'paused' | 'archived';
  nfcTagId?: string;
  addedAt: any;
  lastUsedAt?: any;
  note?: string;
}

export interface FilamentStats {
  totalFilaments: number;
  totalWeight: number;
  remainingWeight: number;
  usedWeight: number;
  percentageUsed: number;
  activeFilaments: number;
  emptyFilaments: number;
}

export async function getSessionFilaments(sessionId: string): Promise<Filament[]> {
  try {
    const q = query(
      collection(db, `sessions/${sessionId}/filaments`),
      orderBy('addedAt', 'desc')
    );

    const snapshot = await getDocs(q);
    return snapshot.docs.map(doc => ({
      id: doc.id,
      filamentId: doc.id,
      ...doc.data()
    } as Filament));
  } catch (error) {
    console.error('❌ Error fetching filaments:', error);
    return [];
  }
}

export function listenToSessionFilaments(
  sessionId: string,
  onUpdate: (filaments: Filament[]) => void
): () => void {
  return onSnapshot(
    query(
      collection(db, `sessions/${sessionId}/filaments`),
      orderBy('addedAt', 'desc')
    ),
    (snapshot) => {
      const filaments = snapshot.docs.map(doc => ({
        id: doc.id,
        filamentId: doc.id,
        ...doc.data()
      } as Filament));

      onUpdate(filaments);
    },
    (error) => {
      console.error('❌ Error listening to filaments:', error);
    }
  );
}

export function calculateFilamentStats(filaments: Filament[]): FilamentStats {
  const totalWeight = filaments.reduce((sum, f) => sum + (f.weight || 0), 0);
  const remainingWeight = filaments.reduce((sum, f) => sum + (f.remainingWeight || 0), 0);
  const usedWeight = totalWeight - remainingWeight;

  return {
    totalFilaments: filaments.length,
    totalWeight,
    remainingWeight,
    usedWeight,
    percentageUsed: totalWeight > 0 ? Math.round((usedWeight / totalWeight) * 100) : 0,
    activeFilaments: filaments.filter(f => f.status === 'active').length,
    emptyFilaments: filaments.filter(f => f.status === 'empty').length
  };
}
