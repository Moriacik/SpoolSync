import { useState, useEffect } from 'react';
import { onSnapshot, doc } from 'firebase/firestore';
import { db } from '../config/firebase';
import type { QRSession } from '../services/qrService'

export function useQRSession(sessionId: string | null) {
  const [isConfirmed, setIsConfirmed] = useState(false);
  const [userId, setUserId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!sessionId) return;

    setLoading(true);

    const unsubscribe = onSnapshot(
      doc(db, 'qrSessions', sessionId),
      (snapshot) => {
        const data = snapshot.data() as QRSession | undefined;
        
        if (data && data.confirmed && data.confirmedBy) {
          setIsConfirmed(true);
          setUserId(data.confirmedBy);
          setLoading(false);
          
          // Store to localStorage
          localStorage.setItem('sessionId', sessionId);
          localStorage.setItem('userId', data.confirmedBy);
        }
      },
      (error) => {
        console.error('Error listening to session:', error);
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [sessionId]);

  return { isConfirmed, userId, loading };
}
