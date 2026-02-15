import { useState, useEffect } from 'react';
import { getUserProfile, getUserSessions, getUserFilaments, calculateStatistics, type Statistics, type UserProfile } from '../services/statisticsService';
import { cleanupUnconfirmedExpiredSessions, cleanupOwnExpiredSessions } from '../services/qrService';

interface UseStatisticsResult {
  profile: UserProfile | null;
  statistics: Statistics | null;
  loading: boolean;
  error: Error | null;
}

export function useStatistics(userId: string | null): UseStatisticsResult {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [statistics, setStatistics] = useState<Statistics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (!userId) {
      setProfile(null);
      setStatistics(null);
      return;
    }

    const fetchData = async () => {
      setLoading(true);
      setError(null);

      try {
        // Clean up expired unconfirmed QR sessions (as anonymous)
        await cleanupUnconfirmedExpiredSessions();

        // Clean up own expired QR sessions (as authenticated user)
        await cleanupOwnExpiredSessions(userId);

        // Fetch user profile
        const userProfile = await getUserProfile(userId);
        setProfile(userProfile);

        // Fetch user filaments
        const userFilaments = await getUserFilaments(userId);

        // Fetch sessions
        const sessions = await getUserSessions(userId);

        // Calculate statistics with user filaments
        const stats = calculateStatistics(sessions, userFilaments);
        setStatistics(stats);
      } catch (err) {
        // Don't treat as fatal error - show empty state instead
        setStatistics({
          user: {
            totalWeight: 0,
            remainingWeight: 0,
            usedWeight: 0,
            usedPercent: 0,
            totalSessions: 0,
            activeSessions: 0,
            totalFilaments: 0,
            activeFilaments: 0,
            emptyFilaments: 0,
            filamentsByStatus: {
              All: 0,
              Opened: 0,
              Closed: 0,
              Using: 0
            },
            mostUsedBrand: '-',
            mostUsedType: '-'
          },
          sessions: []
        });
        // Only set error if it's a real permission issue
        if (err instanceof Error && err.message.includes('permission')) {
          setError(err);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [userId]);

  return { profile, statistics, loading, error };
}
