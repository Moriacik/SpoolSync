import { useEffect, useState } from 'react'
import { signInAnonymously } from 'firebase/auth'
import { auth } from './config/firebase'
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import { LoginScreen } from './components/LoginScreen'
import { StatisticsPage } from './components/StatisticsPage'
import './App.css'

function AppContent() {
  const [androidUserId, setAndroidUserId] = useState<string | null>(null)
  const [firebaseReady, setFirebaseReady] = useState(false)
  const navigate = useNavigate()

  // Initialize Firebase anonymous auth
  useEffect(() => {
    const initAuth = async () => {
      try {
        if (!auth.currentUser) {
          await signInAnonymously(auth)
          console.log('Web app authenticated anonymously')
        }
        setFirebaseReady(true)
      } catch (error) {
        console.error('Anonymous auth failed:', error)
        setFirebaseReady(true)
      }
    }

    initAuth()
  }, [])

  // Check for existing session and redirect
  useEffect(() => {
    if (!firebaseReady) return;
    
    const storedAndroidUserId = localStorage.getItem('androidUserId')
    
    if (storedAndroidUserId) {
      setAndroidUserId(storedAndroidUserId)
      // Redirect to stats page if already authenticated and on login page
      navigate(`/stats/${storedAndroidUserId}`, { replace: true })
    } else {
      // Redirect to login if not authenticated
      navigate('/login', { replace: true })
    }
  }, [firebaseReady, navigate])

  const handleAuthSuccess = (newAndroidUserId: string) => {
    console.log('Auth success, Android user:', newAndroidUserId);
    setAndroidUserId(newAndroidUserId)
    localStorage.setItem('androidUserId', newAndroidUserId)
    navigate(`/stats/${newAndroidUserId}`, { replace: true })
  }

  const handleLogout = () => {
    console.log('🚪 Logout clicked');
    localStorage.removeItem('androidUserId')
    localStorage.removeItem('sessionId')
    // Clear any cached data
    sessionStorage.clear()
    setAndroidUserId(null)
    console.log('Cleared localStorage, sessionStorage and state, redirecting to login...');
    navigate('/login', { replace: true })
  }

  if (!firebaseReady) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#667eea' }}>
        <div style={{ color: 'white', textAlign: 'center' }}>
          <p>Inicializujem aplikáciu...</p>
        </div>
      </div>
    )
  }

  return (
    <Routes>
      <Route path="/login" element={<LoginScreen onAuthSuccess={handleAuthSuccess} />} />
      <Route path="/stats/:userId" element={<StatisticsPage onLogout={handleLogout} />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  )
}

export default App
