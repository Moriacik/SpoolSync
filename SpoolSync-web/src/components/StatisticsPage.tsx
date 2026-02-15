import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useStatistics } from '../hooks/useStatistics';
import type { Filament, SessionWithStats } from '../services/statisticsService';
import '../styles/StatisticsPage.css';

interface StatisticsPageProps {
  onLogout: () => void;
}

export function StatisticsPage({ onLogout }: StatisticsPageProps) {
  const { userId } = useParams<{ userId: string }>();
  const { profile, statistics, loading, error } = useStatistics(userId || '');
  const [expandedSession, setExpandedSession] = useState<string | null>(
    statistics?.sessions[0]?.sessionId || null
  );

  if (loading) {
    return (
      <div className="stats-page">
        <div className="loading">
          <p>Loading statistics...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="stats-page">
        <div className="error-container">
          <h2>❌ Error Loading</h2>
          <p>{error.message}</p>
          <p style={{ fontSize: '12px', color: '#999' }}>
            Try refreshing the page (F5)
          </p>
          <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
            <button 
              onClick={() => window.location.reload()} 
              className="btn-primary"
            >
              🔄 Refresh
            </button>
            <button onClick={onLogout} className="btn-secondary">
              Logout
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!statistics || !profile) {
    return (
      <div className="stats-page">
        <div className="empty-state">
          <h2>No Data</h2>
          <p>You have no active sessions. Start by creating a session in the mobile app.</p>
          <button onClick={onLogout} className="btn-back">
            Logout
          </button>
        </div>
      </div>
    );
  }

  const formatDate = (timestamp: any) => {
    if (!timestamp) return '-';
    try {
      // Handle both milliseconds (number) and Firestore Timestamp
      let ms = timestamp;
      if (timestamp.seconds) {
        // Firestore Timestamp
        ms = timestamp.seconds * 1000;
      } else if (typeof timestamp === 'number') {
        // Milliseconds
        ms = timestamp;
      }
      const date = new Date(ms);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return '-';
    }
  };

  const formatWeight = (grams: number) => {
    return grams >= 1000 ? `${(grams / 1000).toFixed(2)} kg` : `${grams} g`;
  };

  const convertARGBtoRGB = (color: string): string => {
    if (!color) return '#CCCCCC';
    const trimmed = String(color).trim();
    // If 9 chars (#FFFFFFFF), remove alpha (first 2 chars after #)
    if (trimmed.length === 9 && trimmed.startsWith('#')) {
      return '#' + trimmed.substring(3);
    }
    return trimmed;
  };

  return (
    <div className="stats-page">
      <header className="stats-header">
        <div className="header-content">
          <div className="header-left">
            <img src="/icons/logo.svg" alt="SpoolSync Logo" className="header-logo" />
            <div className="header-title">
              <h1>SpoolSync</h1>
              <p className="user-info">Logged in as: <strong>{profile.displayName || profile.email}</strong></p>
            </div>
          </div>
          <button onClick={onLogout} className="btn-logout">
            Logout
          </button>
        </div>
      </header>

      <div className="stats-container">
        {/* ============== USER STATISTICS SECTION ============== */}
        <div className="user-stats-section">
          <h2>User Statistics</h2>
          
          {/* Overview Cards */}
          <section className="overview-section">
            <h3>Overview</h3>
            <div className="overview-grid">
              <div className="overview-card">
                <div className="overview-label">Sessions</div>
                <div className="overview-value">{statistics.user.totalSessions}</div>
              </div>

              <div className="overview-card">
                <div className="overview-label">Filaments</div>
                <div className="overview-value">{statistics.user.totalFilaments}</div>
              </div>

              <div className="overview-card">
                <div className="overview-label">Used</div>
                <div className="overview-value">{statistics.user.usedPercent}%</div>
              </div>
            </div>
          </section>

          <div className="section-divider"></div>

          {/* Weight Summary */}
          <section className="weight-section">
            <h3>Total Weight</h3>
            <div className="weight-cards">
              <div className="weight-card">
                <div className="weight-label">Total Weight</div>
                <div className="weight-value">{formatWeight(statistics.user.totalWeight)}</div>
              </div>

              <div className="weight-card">
                <div className="weight-label">Remaining Weight</div>
                <div className="weight-value remaining">
                  {formatWeight(statistics.user.remainingWeight)}
                </div>
              </div>

              <div className="weight-card">
                <div className="weight-label">Used Weight</div>
                <div className="weight-value used">
                  {formatWeight(statistics.user.usedWeight)}
                </div>
              </div>
            </div>
          </section>

          <div className="section-divider"></div>

          {/* Status Breakdown */}
          <section className="status-section">
            <h3>Filaments by Status</h3>
            <div className="weight-cards">
              <div className="weight-card">
                <div className="weight-label">All</div>
                <div className="weight-value">{statistics.user.filamentsByStatus.All}</div>
              </div>
              <div className="weight-card">
                <div className="weight-label">Opened</div>
                <div className="weight-value">{statistics.user.filamentsByStatus.Opened}</div>
              </div>
              <div className="weight-card">
                <div className="weight-label">Closed</div>
                <div className="weight-value">{statistics.user.filamentsByStatus.Closed}</div>
              </div>
              <div className="weight-card">
                <div className="weight-label">Using</div>
                <div className="weight-value">{statistics.user.filamentsByStatus.Using}</div>
              </div>
            </div>
          </section>

          <div className="section-divider"></div>

          {/* Most Used Materials */}
          <section className="materials-section">
            <h3>Most Used</h3>
            <div className="materials-grid">
              <div className="material-card">
                <div className="material-label">Brand</div>
                <div className="material-value">{statistics.user.mostUsedBrand}</div>
              </div>
              <div className="material-card">
                <div className="material-label">Type</div>
                <div className="material-value">{statistics.user.mostUsedType}</div>
              </div>
            </div>
          </section>
        </div>

        {/* ============== SESSIONS STATISTICS SECTION ============== */}
        <div className="sessions-stats-section">
          <h2>Sessions Statistics</h2>
          
          {statistics.sessions.length === 0 ? (
            <div className="no-sessions">
              <p>No sessions to display</p>
            </div>
          ) : (
            <>
              {/* Sessions Overview */}
              <section className="sessions-overview-section">
                <h3>Overview</h3>
                <div className="sessions-overview-grid">
                  <div className="overview-card">
                    <div className="overview-label">Total Sessions</div>
                    <div className="overview-value">{statistics.sessions.length}</div>
                  </div>
                  <div className="overview-card">
                    <div className="overview-label">Total Filaments</div>
                    <div className="overview-value">
                      {statistics.sessions.reduce((sum, session) => sum + session.filamentCount, 0)}
                    </div>
                  </div>
                </div>
              </section>

              <div className="section-divider"></div>

              {/* Overall Sessions Weight Summary */}
              {statistics.sessions.length > 0 && (() => {
                const sessionsTotalWeight = statistics.sessions.reduce((sum, session) => sum + session.weight.totalWeight, 0);
                const sessionsRemainingWeight = statistics.sessions.reduce((sum, session) => sum + session.weight.remainingWeight, 0);
                const sessionsUsedWeight = sessionsTotalWeight - sessionsRemainingWeight;

                return (
                  <section className="overall-sessions-weight">
                    <h3>All Sessions - Total Weight</h3>
                    <div className="weight-cards">
                      <div className="weight-card">
                        <div className="weight-label">Total Weight</div>
                        <div className="weight-value">{formatWeight(sessionsTotalWeight)}</div>
                      </div>

                      <div className="weight-card">
                        <div className="weight-label">Remaining Weight</div>
                        <div className="weight-value remaining">
                          {formatWeight(sessionsRemainingWeight)}
                        </div>
                      </div>

                      <div className="weight-card">
                        <div className="weight-label">Used Weight</div>
                        <div className="weight-value used">
                          {formatWeight(sessionsUsedWeight)}
                        </div>
                      </div>
                    </div>
                  </section>
                );
              })()}

              <div className="section-divider"></div>

              {/* Sessions List */}
              <section className="sessions-list-section">
                <h3>Sessions</h3>
                <div className="sessions-list">
                  {statistics.sessions.map((session: SessionWithStats) => (
                <div key={session.sessionId} className="session-item">
                  <div
                    className="session-header"
                    onClick={() =>
                      setExpandedSession(
                        expandedSession === session.sessionId ? null : session.sessionId
                      )
                    }
                  >
                    <div className="session-title">
                      <span className={`status-badge status-${session.status}`}>
                        {session.status === 'active' && 'Active'}
                        {session.status === 'paused' && 'Paused'}
                        {session.status === 'completed' && 'Completed'}
                        {session.status === 'archived' && 'Archived'}
                      </span>
                      <h3>{session.sessionName}</h3>
                      <span className={`role-badge ${session.ownerId === userId ? 'owner' : 'member'}`}>
                        {session.ownerId === userId ? 'Owner' : 'Member'}
                      </span>
                    </div>
                    <div className="session-meta">
                      <span className="expand-icon">
                        {expandedSession === session.sessionId ? '▼' : '▶'}
                      </span>
                    </div>
                  </div>

                  {expandedSession === session.sessionId && (
                    <div className="session-details">
                      <div className="detail-grid">
                        <div className="detail-item">
                          <span className="detail-label">Created:</span>
                          <span className="detail-value">
                            {formatDate(session.sessionCreatedAt)}
                          </span>
                        </div>

                        <div className="detail-item">
                          <span className="detail-label">Last Modified:</span>
                          <span className="detail-value">
                            {formatDate(session.lastModifiedAt)}
                          </span>
                        </div>

                        <div className="detail-item">
                          <span className="detail-label">Members:</span>
                          <span className="detail-value">
                            {session.participants.length}
                          </span>
                        </div>

                        <div className="detail-item">
                          <span className="detail-label">Filaments:</span>
                          <span className="detail-value">
                            {session.filamentCount}
                          </span>
                        </div>
                      </div>

                      {/* Session Weight Stats */}
                      <div className="session-weight-stats">
                        <h4>Session Weight</h4>
                        <div className="weight-cards">
                          <div className="weight-card">
                            <div className="weight-label">Total Weight</div>
                            <div className="weight-value">{formatWeight(session.weight.totalWeight)}</div>
                          </div>

                          <div className="weight-card">
                            <div className="weight-label">Remaining Weight</div>
                            <div className="weight-value remaining">{formatWeight(session.weight.remainingWeight)}</div>
                          </div>

                          <div className="weight-card">
                            <div className="weight-label">Used Weight</div>
                            <div className="weight-value used">{formatWeight(session.weight.usedWeight)}</div>
                          </div>

                          <div className="weight-card">
                            <div className="weight-label">Usage</div>
                            <div className="weight-value">{session.weight.usedPercent}%</div>
                          </div>
                        </div>
                      </div>

                      {/* Filaments in Session */}
                      {session.filaments && Object.keys(session.filaments).length > 0 && (
                        <div className="filaments-subsection">
                          <h4>Filaments</h4>
                          <div className="filaments-list">
                            {Array.isArray(session.filaments) 
                              ? session.filaments.map((filament: any, index: number) => {
                                  const filKey = `${session.sessionId}-${index}-${filament?.id || filament?.filamentId || 'unknown'}`;
                                  return (
                                  <div key={filKey} className="filament-item">
                                    <div className="filament-color-preview">
                                      <div
                                        className="color-box"
                                        style={{
                                          backgroundColor: convertARGBtoRGB(filament?.color)
                                        }}
                                        title={`Color: ${filament?.color || 'Not set'}`}
                                      />
                                    </div>

                                    <div className="filament-info">
                                      <div className="filament-name">
                                        <strong>{filament?.brand || 'Unknown'}</strong> {filament?.type || ''}
                                      </div>
                                      <div className="filament-specs">
                                        <span>
                                          Weight: {filament?.weight || 0} / {filament?.originalWeight || filament?.weight || 0}g
                                        </span>
                                      </div>

                                      {filament?.note && (
                                        <div className="filament-note">
                                          {filament.note}
                                        </div>
                                      )}
                                    </div>

                                    <div className="filament-progress">
                                      <div className="progress-bar-small">
                                        <div
                                          className="progress-fill"
                                          style={{
                                            width: `${
                                              (filament?.originalWeight || filament?.weight || 0) > 0
                                                ? Math.round(((filament?.weight || 0) / (filament?.originalWeight || filament?.weight || 1)) * 100)
                                                : 0
                                            }%`
                                          }}
                                        />
                                      </div>
                                      <span className="progress-percent">
                                        {
                                          (filament?.originalWeight || filament?.weight || 0) > 0
                                            ? Math.round(((filament?.weight || 0) / (filament?.originalWeight || filament?.weight || 1)) * 100)
                                            : 0
                                        }%
                                      </span>
                                    </div>
                                  </div>
                                  );
                                })
                              : Object.values(session.filaments).map((filament: Filament) => (
                                  <div key={filament.filamentId} className="filament-item">
                                    <div className="filament-color-preview">
                                      <div
                                        className="color-box"
                                        style={{
                                          backgroundColor: filament.color?.trim() ? filament.color.trim() : '#CCCCCC',
                                          border: filament.color === '#FFFFFF' || filament.color === '#FFF' ? '2px solid #999' : '2px solid #ddd'
                                        }}
                                        title={`Color: ${filament.color || 'Not set'}`}
                                      />
                                    </div>

                                    <div className="filament-info">
                                      <div className="filament-name">
                                        <strong>{filament.brand}</strong> {filament.type}
                                      </div>
                                      <div className="filament-specs">
                                        <span>
                                          Weight: {filament.weight} / {filament.originalWeight}g
                                        </span>
                                      </div>

                                      {filament.note && (
                                        <div className="filament-note">
                                          {filament.note}
                                        </div>
                                      )}
                                    </div>

                                    <div className="filament-progress">
                                      <div className="progress-bar-small">
                                        <div
                                          className="progress-fill"
                                          style={{
                                            width: `${
                                              filament.originalWeight > 0
                                                ? Math.round((filament.weight / filament.originalWeight) * 100)
                                                : 0
                                            }%`
                                          }}
                                        />
                                      </div>
                                      <span className="progress-percent">
                                        {
                                          filament.originalWeight > 0
                                            ? Math.round((filament.weight / filament.originalWeight) * 100)
                                            : 0
                                        }%
                                      </span>
                                    </div>
                                  </div>
                                ))}
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              ))}
                </div>
              </section>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
