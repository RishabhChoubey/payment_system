"use client";

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { userApi, rewardApi } from '@/lib/api';

interface Reward {
  id: number;
  userId: number;
  points: number;
  sentAt?: string;
  transactionId?: number;
}

export default function RewardsPage() {
  const router = useRouter();
  const [rewards, setRewards] = useState<Reward[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [totalPoints, setTotalPoints] = useState(0);
  const [showAll, setShowAll] = useState(false);
  const [userId, setUserId] = useState<number | null>(null);

  const loadRewards = async (mode: 'mine' | 'all' = 'mine') => {
    try {
      setLoading(true);
      setError(null);
      let data: Reward[] = [];
      if (mode === 'all') {
        const res = await rewardApi.getAllRewards();
        data = res.data;
      } else {
        if (userId == null) return; // wait for user
        const res = await rewardApi.getRewardsByUserId(userId);
        data = res.data;
      }
      setRewards(data || []);
      setTotalPoints(data.reduce((sum, r) => sum + (r.points || 0), 0));
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Failed to load rewards');
    } finally {
      setLoading(false);
    }
  };

  // Load current user first
  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await userApi.getCurrentUser();
        if (cancelled) return;
        const id = res.data?.id;
        if (typeof id === 'number') {
            setUserId(id);
        } else if (typeof id === 'string') {
            const parsed = parseInt(id, 10);
            if (!isNaN(parsed)) setUserId(parsed);
        }
      } catch (e: any) {
        setError('You must be logged in to view rewards');
      }
    })();
    return () => { cancelled = true; };
  }, []);

  // Load rewards when userId changes or showAll toggles
  useEffect(() => {
    if (showAll) {
      loadRewards('all');
    } else if (userId != null) {
      loadRewards('mine');
    }
  }, [userId, showAll]);

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-3xl mx-auto bg-white rounded-lg shadow p-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
          <h1 className="text-2xl font-bold">Rewards</h1>
          <div className="flex gap-2 flex-wrap">
            <button
              className={`px-4 py-2 rounded text-sm font-semibold ${showAll ? 'bg-gray-200 text-gray-700' : 'bg-blue-600 text-white'}`}
              onClick={() => setShowAll(false)}
              disabled={!userId}
            >My Rewards</button>
            <button
              className={`px-4 py-2 rounded text-sm font-semibold ${showAll ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700'}`}
              onClick={() => setShowAll(true)}
            >All Rewards</button>
            <button
              className="px-4 py-2 rounded text-sm font-semibold bg-green-600 text-white disabled:opacity-50"
              onClick={() => loadRewards(showAll ? 'all' : 'mine')}
              disabled={loading || (!showAll && userId == null)}
            >{loading ? 'Refreshing...' : 'Refresh'}</button>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded bg-red-50 text-red-700 text-sm border border-red-200">
            {error}
          </div>
        )}

        <div className="mb-4 p-4 rounded bg-indigo-50 border border-indigo-200 flex items-center justify-between">
          <span className="font-medium">Total Points:</span>
          <span className="text-xl font-bold text-indigo-700">{totalPoints}</span>
        </div>

        {loading && rewards.length === 0 && (
          <div className="text-center text-gray-500 py-8">Loading rewards...</div>
        )}

        {!loading && rewards.length === 0 && !error && (
          <div className="text-center text-gray-500 py-8">No rewards found.</div>
        )}

        {rewards.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm border border-gray-200 rounded">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-3 py-2 text-left">ID</th>
                  <th className="px-3 py-2 text-left">User</th>
                  <th className="px-3 py-2 text-left">Points</th>
                  <th className="px-3 py-2 text-left">Transaction</th>
                  <th className="px-3 py-2 text-left">Awarded At</th>
                </tr>
              </thead>
              <tbody>
                {rewards.map(r => (
                  <tr key={r.id} className="border-t hover:bg-gray-50">
                    <td className="px-3 py-2">{r.id}</td>
                    <td className="px-3 py-2">{r.userId}</td>
                    <td className="px-3 py-2 font-semibold">{r.points}</td>
                    <td className="px-3 py-2">{r.transactionId ?? '-'}</td>
                    <td className="px-3 py-2 text-xs">{r.sentAt ? new Date(r.sentAt).toLocaleString() : '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="mt-8 text-center">
          <button
            className="bg-gray-300 text-gray-800 px-4 py-2 rounded"
            onClick={() => router.push('/dashboard')}
          >Back to Dashboard</button>
        </div>
      </div>
    </div>
  );
}
