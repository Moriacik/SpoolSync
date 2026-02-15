import { v4 as uuidv4 } from 'uuid';
import { doc, setDoc, Timestamp, collection, query, where, getDocs, deleteDoc } from 'firebase/firestore';
import { db } from '../config/firebase';

export interface QRSession {
  sessionId: string;
  requestToken: string;
  expiresAt: number;
  createdAt: number;
  confirmed?: boolean;
  confirmedBy?: string;
  confirmedAt?: number;
}

export async function generateQRSession(): Promise<QRSession> {
  const sessionId = uuidv4();
  const requestToken = uuidv4();
  const expiresAt = Date.now() + 5 * 60 * 1000; // 5 minutes

  const sessionData: QRSession = {
    sessionId,
    requestToken,
    expiresAt,
    createdAt: Date.now(),
  };

  try {

    await setDoc(doc(db, 'qrSessions', sessionId), {
      ...sessionData,
      expiresAt: Timestamp.fromMillis(expiresAt),
      createdAt: Timestamp.fromMillis(sessionData.createdAt),
    });

  } catch (error) {
    console.error('❌ Error saving to Firestore:', error);
    if (error instanceof Error) {
      console.error('Error details:', error.message);
    }
    throw error;
  }

  return sessionData;
}

export function getQRData(session: QRSession): string {
  const qrPayload = {
    sessionId: session.sessionId,
    requestToken: session.requestToken,
    expiresAt: session.expiresAt,
  };

  return JSON.stringify(qrPayload);
}

/**
 * Clean up unconfirmed expired QR sessions (anonymous user)
 * Called before generating new QR code or on page unload
 */
export async function cleanupUnconfirmedExpiredSessions(): Promise<void> {
  try {
    const now = Timestamp.now();
    const q = query(
      collection(db, 'qrSessions'),
      where('expiresAt', '<', now),
      where('confirmed', '!=', true)
    );

    const snapshot = await getDocs(q);
    const deletePromises = snapshot.docs.map(doc => deleteDoc(doc.ref));
    
    if (deletePromises.length > 0) {
      await Promise.all(deletePromises);
    }
  } catch (error) {
    // Silently fail - cleanup is best-effort
    if (error instanceof Error && !error.message.includes('permission')) {
      console.error('Error cleaning up expired unconfirmed QR sessions:', error);
    }
  }
}

/**
 * Clean up own expired QR sessions (authenticated user)
 * Called after confirming QR or on page unload when authenticated
 */
export async function cleanupOwnExpiredSessions(userId: string): Promise<void> {
  try {
    const now = Timestamp.now();
    const q = query(
      collection(db, 'qrSessions'),
      where('expiresAt', '<', now),
      where('confirmedBy', '==', userId)
    );

    const snapshot = await getDocs(q);
    const deletePromises = snapshot.docs.map(doc => deleteDoc(doc.ref));
    
    if (deletePromises.length > 0) {
      await Promise.all(deletePromises);
    }
  } catch (error) {
    // Silently fail - cleanup is best-effort
    if (error instanceof Error && !error.message.includes('permission')) {
      console.error('Error cleaning up expired own QR sessions:', error);
    }
  }
}
