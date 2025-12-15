import { useState, useRef, useEffect } from 'react';
import QRCode from 'qrcode';
import { generateQRSession, getQRData } from '../services/qrService'
import type { QRSession } from '../services/qrService'
import { useQRSession } from '../hooks/useQRSession';
import '../styles/LoginScreen.css';

export function LoginScreen() {
  const [session, setSession] = useState<QRSession | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [qrDataUrl, setQrDataUrl] = useState<string | null>(null);
  const qrCanvasRef = useRef<HTMLDivElement>(null);
  const { isConfirmed, userId, loading: qrLoading } = useQRSession(session?.sessionId ?? null);

  useEffect(() => {
    if (session && qrCanvasRef.current) {
      QRCode.toDataURL(getQRData(session), {
        errorCorrectionLevel: 'H',
        type: 'image/png',
        width: 256,
        margin: 1,
      })
        .then((url: string) => setQrDataUrl(url))
        .catch((err: Error) => console.error('Error generating QR code:', err));
    }
  }, [session]);

  const handleGenerateQR = async () => {
    setIsLoading(true);
    try {
      const newSession = await generateQRSession();
      setSession(newSession);
    } catch (error) {
      console.error('Failed to generate QR:', error);
      alert('Chyba pri generovaní QR kódu. Skontroluj Firebase konfiguráciu.');
    } finally {
      setIsLoading(false);
    }
  };

  if (isConfirmed && userId) {
    return (
      <div className="login-screen">
        <div className="success-container">
          <h1>✓ Úspešne prihlásený</h1>
          <p>ID používateľa: <strong>{userId}</strong></p>
          <p>Presmerovávam na štatistiky...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="login-screen">
      <div className="login-container">
        <h1>SpoolSync - Štatistiky</h1>
        
        {!session ? (
          <div className="login-section">
            <p>Prihláste sa pomocou QR kódu z mobilnej aplikácie</p>
            <button 
              onClick={handleGenerateQR}
              disabled={isLoading}
              className="btn-primary"
            >
              {isLoading ? 'Generujem QR...' : 'Prihlásiť sa cez QR'}
            </button>
          </div>
        ) : (
          <div className="qr-section">
            <p>Naskenujte QR kód mobilnou aplikáciou:</p>
            <div className="qr-container" ref={qrCanvasRef}>
              {qrDataUrl && <img src={qrDataUrl} alt="QR Code" />}
            </div>
            <p className="session-id">Session ID: {session.sessionId.substring(0, 8)}...</p>
            {qrLoading && <p className="waiting">Čakám na potvrdenie...</p>}
            
            <button 
              onClick={() => setSession(null)}
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
