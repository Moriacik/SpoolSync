import { query, collection, where, getDocs, onSnapshot } from 'firebase/firestore';
import { db } from '../config/firebase';

export interface Session {
  id: string;
  ownerId: string;
  sessionName: string;
  sessionCreatedAt: number;
  lastModifiedAt: number;
  participants: string[];
  status: 'active' | 'paused' | 'completed' | 'archived';
}

export async function getAndroidUserSessions(androidUserId: string): Promise<Session[]> {
  try {
    const q = query(
      collection(db, 'sessions'),
      where('ownerId', '==', androidUserId)
    );

    const snapshot = await getDocs(q);
    return snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    } as Session));
  } catch (error) {
    console.error('❌ Error fetching sessions:', error);
    throw error;
  }
}

export function listenToAndroidUserSessions(
  androidUserId: string,
  onUpdate: (sessions: Session[]) => void
): () => void {
  
  const q = query(
    collection(db, 'sessions'),
    where('ownerId', '==', androidUserId)
  );

  return onSnapshot(
    q,
    (snapshot) => {
      const sessions = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      } as Session));

      onUpdate(sessions);
    },
    (error) => {
      console.error('❌ Error listening to sessions:', error);
    }
  );
}

export function listenToSessionMembers(sessionId: string, onUpdate: (members: any[]) => void): () => void {
  return onSnapshot(
    collection(db, `sessions/${sessionId}/members`),
    (snapshot) => {
      const members = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      onUpdate(members);
    }
  );
}
