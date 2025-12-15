import { v4 as uuidv4 } from 'uuid';
import { doc, setDoc, Timestamp } from 'firebase/firestore';
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
    console.log('✅ Session saved to Firestore:', sessionId);
  } catch (error) {
    console.error('❌ Error saving to Firestore:', error);
    throw error;
  }

  return sessionData;
}

export function getQRData(session: QRSession): string {
  return JSON.stringify({
    sessionId: session.sessionId,
    requestToken: session.requestToken,
    expiresAt: session.expiresAt,
  });
}
