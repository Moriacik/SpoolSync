import { onSnapshot, doc, Timestamp, deleteDoc } from 'firebase/firestore';
import { db } from '../config/firebase';
import type { QRSession } from './qrService';
import { cleanupOwnExpiredSessions } from './qrService';

async function deleteQRSession(sessionId: string): Promise<void> {
  try {
    await deleteDoc(doc(db, 'qrSessions', sessionId));
  } catch (error) {
    // Silently ignore permission errors - QR sessions expire automatically
    // Anonymous web users may not have permission to delete, but that's okay
    if (error instanceof Error && !error.message.includes('permission')) {
      console.error('Error deleting QR session:', error);
    }
  }
}

export function listenToQRConfirmation(
  sessionId: string,
  onConfirmed: (androidUserId: string) => void,
  onExpired: () => void
): () => void {
  
  const unsubscribe = onSnapshot(
    doc(db, 'qrSessions', sessionId),
    (snapshot) => {
      const data = snapshot.data();
      
      if (!data) {

        return;
      }


      
      // Check if confirmed
      if (data.confirmed === true && data.confirmedBy) {

        unsubscribe();
        onConfirmed(data.confirmedBy);
        
        // Attempt to delete confirmed QR session as authenticated user
        // This will succeed because user owns the confirmed session
        deleteQRSession(sessionId);
        
        // Also cleanup other expired sessions for this user
        cleanupOwnExpiredSessions(data.confirmedBy);
        
        return;
      }
      
      // Check expiration - handle both Timestamp and number
      let expiresAtMs = data.expiresAt;
      if (data.expiresAt instanceof Timestamp) {
        expiresAtMs = data.expiresAt.toMillis();
      }
      
      if (Date.now() > expiresAtMs) {

        unsubscribe();
        onExpired();
        // Delete expired QR session from database
        deleteQRSession(sessionId);
        return;
      }
    },
    (error) => {
      console.error('❌ Error listening to QR confirmation:', error);
    }
  );

  return unsubscribe;
}
