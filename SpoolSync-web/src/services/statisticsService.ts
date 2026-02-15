import { db } from '../config/firebase';
import { 
  collection, 
  query, 
  where, 
  getDocs,
  doc,
  getDoc,
  Timestamp
} from 'firebase/firestore';

export interface Filament {
  filamentId: string;
  type: string;
  brand: string;
  color: string;
  weight: number;  // Current weight
  originalWeight: number;  // Original weight
  status: string;  // 'active', 'empty', 'All', etc.
  activeNfc: boolean;
  expirationDate?: string;  // Format: YYYY-MM-DD
  ownerId: string;
  note?: string;
}

export interface Member {
  userId: string;
  name: string;
  role: 'owner' | 'editor' | 'viewer';
  joinedAt: Timestamp;
  permissions?: string[];
}

export interface Session {
  sessionId: string;
  ownerId: string;
  sessionName: string;
  sessionCreatedAt: number;
  lastModifiedAt: number;
  participants: string[];
  status: 'active' | 'paused' | 'completed' | 'archived';
  members?: Record<string, Member>;
  filaments?: Record<string, Filament>;
}

export interface UserProfile {
  userId: string;
  email: string;
  displayName: string;
  photoURL?: string;
  createdAt: number;
  updatedAt: number;
}

export interface WeightStats {
  totalWeight: number;
  remainingWeight: number;
  usedWeight: number;
  usedPercent: number;
}

export interface SessionWithStats extends Session {
  weight: WeightStats;
  filamentCount: number;
  activeFilamentCount: number;
  emptyFilamentCount: number;
}

export interface UserStats extends WeightStats {
  totalSessions: number;
  activeSessions: number;
  totalFilaments: number;
  activeFilaments: number;
  emptyFilaments: number;
  filamentsByStatus: {
    All: number;
    Opened: number;
    Closed: number;
    Using: number;
  };
  mostUsedBrand: string;
  mostUsedType: string;
}

export interface Statistics {
  user: UserStats;
  sessions: SessionWithStats[];
}

export async function getUserProfile(userId: string): Promise<UserProfile | null> {
  try {
    const userDoc = await getDoc(doc(db, 'users', userId));
    if (userDoc.exists()) {
      return {
        userId,
        ...userDoc.data() as Omit<UserProfile, 'userId'>
      };
    }

    return null;
  } catch (error) {
    console.error('❌ Error fetching user profile:', error);
    // Return minimal profile if error occurs
    return {
      userId,
      email: 'unknown@example.com',
      displayName: 'Android User',
      createdAt: Date.now(),
      updatedAt: Date.now()
    };
  }
}

export async function getUserFilaments(userId: string): Promise<Filament[]> {
  try {
    const userDoc = await getDoc(doc(db, 'users', userId));
    if (!userDoc.exists()) {
      return [];
    }

    const userData = userDoc.data();
    const filamentsMap = userData?.filaments as Record<string, any> || {};
    const filaments: Filament[] = [];

    Object.entries(filamentsMap).forEach(([key, filData]: [string, any]) => {
      let color = filData.color ? String(filData.color).trim() : '#CCCCCC';
      if (color.length === 9 && color.startsWith('#')) {
        color = '#' + color.substring(3);
      }

      filaments.push({
        filamentId: filData.id || key,
        type: filData.type || '',
        brand: filData.brand || '',
        color: color,
        weight: filData.weight || 0,
        originalWeight: filData.originalWeight || 0,
        status: filData.status || 'unknown',
        activeNfc: filData.activeNfc || false,
        ownerId: filData.ownerId || '',
        expirationDate: filData.expirationDate,
        note: filData.note
      });
    });

    return filaments;
  } catch (error) {
    console.error('Error fetching user filaments:', error);
    return [];
  }
}

export async function getUserSessions(userId: string): Promise<Session[]> {
  try {
    // Find sessions where user is owner or participant
    const sessionsRef = collection(db, 'sessions');
    const q = query(
      sessionsRef,
      where('participants', 'array-contains', userId)
    );
    
    const querySnapshot = await getDocs(q);

    const sessions: Session[] = [];

    for (const docSnap of querySnapshot.docs) {
      const sessionData = docSnap.data();
      const sessionId = docSnap.id;

      try {
        // Fetch members subcollection
        const membersSnapshot = await getDocs(
          collection(db, 'sessions', sessionId, 'members')
        );
        const members: Record<string, Member> = {};
        membersSnapshot.forEach(memberDoc => {
          members[memberDoc.id] = {
            userId: memberDoc.data().userId,
            ...memberDoc.data() as Omit<Member, 'userId'>
          };
        });

        // Fetch filaments subcollection
        const filamentsSnapshot = await getDocs(
          collection(db, 'sessions', sessionId, 'filaments')
        );
        const filaments: Record<string, Filament> = {};
        filamentsSnapshot.forEach(filDoc => {
          const filData = filDoc.data();
          // Convert ARGB to RGB: #FFFFFFFF → #FFFFFF (remove first 2 chars)
          let color = filData.color ? String(filData.color).trim() : '#CCCCCC';
          if (color.length === 9 && color.startsWith('#')) {
            // ARGB format - remove alpha (first 2 chars after #)
            color = '#' + color.substring(3);
          }

          
          filaments[filDoc.id] = {
            filamentId: filData.id || filDoc.id,
            type: filData.type || '',
            brand: filData.brand || '',
            color: color,
            weight: filData.weight || 0,
            originalWeight: filData.originalWeight || 0,
            status: filData.status || 'unknown',
            activeNfc: filData.activeNfc || false,
            ownerId: filData.ownerId || '',
            expirationDate: filData.expirationDate,
            note: filData.note
          };
        });

        sessions.push({
          sessionId,
          members,
          filaments,
          ...sessionData as Omit<Session, 'sessionId' | 'members' | 'filaments'>
        });
      } catch (subError) {
        console.error('⚠️ Error loading subcollections for session', sessionId, ':', subError);
        // Continue with other sessions even if one fails
      }
    }

    return sessions;
  } catch (error) {
    console.error('❌ Error fetching user sessions:', error);
    // Return empty array instead of throwing
    return [];
  }
}

export function calculateStatistics(sessions: Session[], userFilaments: Filament[] = []): Statistics {
  // USER STATS - based only on user filaments
  let userTotalWeight = 0;
  let userRemainingWeight = 0;
  const userFilamentCount = userFilaments.length;
  let userActiveFilaments = 0;
  let userEmptyFilaments = 0;

  // Track filament status counts and brand/type frequency (user only)
  const filamentStatusCount = { All: 0, Opened: 0, Closed: 0, Using: 0 };
  const brandFrequency: Record<string, number> = {};
  const typeFrequency: Record<string, number> = {};

  // Count user filaments
  userFilaments.forEach(filament => {
    userTotalWeight += filament.originalWeight;
    userRemainingWeight += filament.weight;

    if (filament.status !== 'empty' && filament.weight > 0) {
      userActiveFilaments++;
    }

    if (filament.status === 'empty' || filament.weight <= 0) {
      userEmptyFilaments++;
    }

    const filamentStatus = filament.status as 'All' | 'Opened' | 'Closed' | 'Using';
    if (filamentStatus && filamentStatusCount.hasOwnProperty(filamentStatus)) {
      filamentStatusCount[filamentStatus]++;
    }

    if (filament.brand) {
      brandFrequency[filament.brand] = (brandFrequency[filament.brand] || 0) + 1;
    }
    if (filament.type) {
      typeFrequency[filament.type] = (typeFrequency[filament.type] || 0) + 1;
    }
  });

  // SESSION STATS - based on sessions filaments
  let totalSessions = 0;
  let activeSessions = 0;
  let sessionsTotalWeight = 0;
  let sessionsRemainingWeight = 0;

  // Convert sessions to sessions with stats
  const sessionsWithStats: SessionWithStats[] = sessions.map(session => {
    let sessionTotalWeight = 0;
    let sessionRemainingWeight = 0;
    let sessionActiveCount = 0;
    let sessionEmptyCount = 0;
    let sessionFilamentCount = 0;

    // Count filaments and weight for this session
    if (session.filaments) {
      Object.values(session.filaments).forEach(filament => {
        sessionFilamentCount++;
        sessionTotalWeight += filament.originalWeight;
        sessionRemainingWeight += filament.weight;

        // Check if filament is active (not empty)
        if (filament.status !== 'empty' && filament.weight > 0) {
          sessionActiveCount++;
        }

        if (filament.status === 'empty' || filament.weight <= 0) {
          sessionEmptyCount++;
        }
      });
    }

    const sessionUsedWeight = sessionTotalWeight - sessionRemainingWeight;
    const sessionUsedPercent = sessionTotalWeight > 0 
      ? Math.round((sessionUsedWeight / sessionTotalWeight) * 100)
      : 0;

    // Add to sessions totals
    totalSessions++;
    
    if (session.status === 'active') {
      activeSessions++;
    }

    sessionsTotalWeight += sessionTotalWeight;
    sessionsRemainingWeight += sessionRemainingWeight;

    return {
      ...session,
      weight: {
        totalWeight: sessionTotalWeight,
        remainingWeight: sessionRemainingWeight,
        usedWeight: sessionUsedWeight,
        usedPercent: isNaN(sessionUsedPercent) ? 0 : sessionUsedPercent
      },
      filamentCount: sessionFilamentCount,
      activeFilamentCount: sessionActiveCount,
      emptyFilamentCount: sessionEmptyCount
    };
  });

  // Find most used brand and type
  const mostUsedBrand = Object.entries(brandFrequency).length > 0
    ? Object.entries(brandFrequency).sort(([, a], [, b]) => b - a)[0][0]
    : '-';

  const mostUsedType = Object.entries(typeFrequency).length > 0
    ? Object.entries(typeFrequency).sort(([, a], [, b]) => b - a)[0][0]
    : '-';

  // Calculate user-level weight stats
  const userUsedWeight = userTotalWeight - userRemainingWeight;
  const userUsedPercent = userTotalWeight > 0 
    ? Math.round((userUsedWeight / userTotalWeight) * 100)
    : 0;

  return {
    user: {
      totalWeight: userTotalWeight,
      remainingWeight: userRemainingWeight,
      usedWeight: userUsedWeight,
      usedPercent: isNaN(userUsedPercent) ? 0 : userUsedPercent,
      totalSessions,
      activeSessions,
      totalFilaments: userFilamentCount,
      activeFilaments: userActiveFilaments,
      emptyFilaments: userEmptyFilaments,
      filamentsByStatus: filamentStatusCount,
      mostUsedBrand,
      mostUsedType
    },
    sessions: sessionsWithStats
  };
}
