import { useEffect, useState } from 'react'
import { LoginScreen } from './components/LoginScreen'
import './App.css'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [userId, setUserId] = useState<string | null>(null)

  useEffect(() => {
    // Check if user is already authenticated
    const storedUserId = localStorage.getItem('userId')
    const sessionId = localStorage.getItem('sessionId')
    
    if (storedUserId && sessionId) {
      setIsAuthenticated(true)
      setUserId(storedUserId)
    }
  }, [])

  const handleLogout = () => {
    localStorage.removeItem('userId')
    localStorage.removeItem('sessionId')
    setIsAuthenticated(false)
    setUserId(null)
  }

  if (isAuthenticated && userId) {
    return (
      <div className="app-container">
        <div className="dashboard-container">
          <header className="dashboard-header">
            <h1>SpoolSync - Štatistiky</h1>
            <button onClick={handleLogout} className="btn-logout">
              Odhlásiť sa
            </button>
          </header>
          <div className="stats-container">
            <div className="stat-card">
              <h2>Informácie o účte</h2>
              <p>User ID: <strong>{userId}</strong></p>
              <div className="placeholder">
                <p>Štatistiky budú zobrazené tu...</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return <LoginScreen />
}

export default App
