import { useState, useRef, useEffect } from 'react';
import QRCode from 'qrcode';
import { generateQRSession, getQRData, cleanupUnconfirmedExpiredSessions } from '../services/qrService'
import type { QRSession } from '../services/qrService'
import { listenToQRConfirmation } from '../services/authService';
import { deleteDoc } from 'firebase/firestore';
import { db } from '../config/firebase';
import { doc } from 'firebase/firestore';
import '../styles/LoginScreen.css';

interface LoginScreenProps {
  onAuthSuccess: (androidUserId: string) => void;
}

export function LoginScreen({ onAuthSuccess }: LoginScreenProps) {
  const [session, setSession] = useState<QRSession | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [qrDataUrl, setQrDataUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<string>('');
  const qrCanvasRef = useRef<HTMLDivElement>(null);
  const unsubscribeRef = useRef<(() => void) | null>(null);

  useEffect(() => {
    if (!session?.sessionId) {

      return;
    }


    setStatus('Waiting for confirmation...');

    try {
      unsubscribeRef.current = listenToQRConfirmation(
        session.sessionId,
        (androidUserId) => {

          setStatus('Logged in!');
          localStorage.setItem('androidUserId', androidUserId);
          localStorage.setItem('sessionId', session.sessionId);
          
          setTimeout(() => {

            onAuthSuccess(androidUserId);
          }, 500);
        },
        () => {

          setError('QR code expired, please try again.');
          setSession(null);
          setStatus('');
        }
      );
    } catch (err) {
      console.error('Error setting up listener:', err);
      setError('Error setting up listener');
    }

    return () => {

      if (unsubscribeRef.current) {
        unsubscribeRef.current();
      }
    };
  }, [session?.sessionId, onAuthSuccess]);

  useEffect(() => {
    if (!session) {

      return;
    }


    setQrDataUrl(null);
    setError(null);

    const generateQR = async () => {
      try {
        const qrData = getQRData(session);


        const url = await QRCode.toDataURL(qrData, {
          errorCorrectionLevel: 'H',
          type: 'image/png',
          width: 256,
          margin: 1,
        });


        setQrDataUrl(url);
      } catch (err) {
        console.error('Error generating QR code:', err);
        setError(`Error generating QR: ${err instanceof Error ? err.message : 'Unknown error'}`);
      }
    };

    generateQR();
  }, [session]);

  // Cleanup expired QR sessions when page unloads
  useEffect(() => {
    const handleBeforeUnload = async () => {
      // Attempt cleanup of unconfirmed expired sessions
      await cleanupUnconfirmedExpiredSessions();
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, []);

  const handleGenerateQR = async () => {

    setIsLoading(true);
    setError(null);
    setStatus('');
    setQrDataUrl(null);

    try {
      // Clean up expired unconfirmed QR sessions before generating new one
      await cleanupUnconfirmedExpiredSessions();

      const newSession = await generateQRSession();

      setSession(newSession);
    } catch (err) {
      console.error('Failed to generate QR:', err);
      const errorMsg = err instanceof Error ? err.message : 'Unknown error';
      setError(`Error: ${errorMsg}. Check your Firebase configuration.`);
      setSession(null);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-screen">
      <div className="login-container">
        <h1>SpoolSync</h1>
        
        {error && (
          <div className="error-message">
            <p>{error}</p>
            <small>Try clicking the button again.</small>
          </div>
        )}

        {!session ? (
          <div className="login-section">
            <p>Sign in using the QR code from the mobile app</p>
            <button 
              onClick={handleGenerateQR}
              disabled={isLoading}
              className="btn-primary"
            >
              {isLoading ? 'Generating QR...' : 'Sign in with QR'}
            </button>
          </div>
        ) : (
          <div className="qr-section">
            <p>Scan the QR code with the mobile app:</p>
            
            {!qrDataUrl ? (
              <div className="qr-container" style={{ textAlign: 'center', padding: '20px' }}>
                <p>Generating QR code...</p>
              </div>
            ) : (
              <div className="qr-container" ref={qrCanvasRef}>
                <img src={qrDataUrl} alt="QR Code" />
              </div>
            )}

            <p className="session-id">Session ID: {session.sessionId.substring(0, 8)}...</p>
            
            {status && <p className="waiting">{status}</p>}
            
            <button 
              onClick={async () => {

                if (unsubscribeRef.current) {
                  unsubscribeRef.current();
                }
                // Delete QR session when cancelled
                if (session?.sessionId) {
                  try {
                    await deleteDoc(doc(db, 'qrSessions', session.sessionId));

                  } catch (error) {
                    console.error('Error deleting QR session:', error);
                  }
                }
                setSession(null);
                setQrDataUrl(null);
                setStatus('');
                setError(null);
              }}
              className="btn-secondary"
            >
              Zrušiť
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
