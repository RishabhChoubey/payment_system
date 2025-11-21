'use client';

import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { transactionApi } from '@/lib/api';
import { useRouter } from 'next/navigation';
import Header from '@/components/Header';

interface Transaction {
  id: number;
  senderId: number;
  receiverId: number;
  amount: number;
  timestamp: string;
  status: string;
}

export default function DashboardPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [txLoading, setTxLoading] = useState(true);
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    // Wait for auth check to finish. While auth provider is loading do nothing.
    if (loading) return;

    // If auth finished and there's no user, redirect to login.
    if (!user) {
      router.push('/login');
      return;
    }

    loadTransactions();
  }, [user, router, loading]);

  const loadTransactions = async () => {
    try {
      const response = await transactionApi.getTransactions();
      setTransactions(response.data);
    } catch (error) {
      console.error('Error loading transactions:', error);
    } finally {
      setTxLoading(false);
    }
  };

  const handleSend = () => router.push('/send');
  const handleRequest = () => router.push('/request');
  const handleRewards = () => router.push('/rewards');

  if (loading || txLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {/* Balance Card */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Your Balance</h2>
          <p className="text-4xl font-bold text-blue-600 mt-2">$1,234.56</p>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
          <button onClick={handleSend} className="bg-blue-600 text-white p-4 rounded-lg shadow-sm hover:bg-blue-700">
            Send Money
          </button>
          <button onClick={handleRequest} className="bg-green-600 text-white p-4 rounded-lg shadow-sm hover:bg-green-700">
            Request Money
          </button>
          <button onClick={handleRewards} className="bg-purple-600 text-white p-4 rounded-lg shadow-sm hover:bg-purple-700">
            View Rewards
          </button>
        </div>

        {/* Recent Transactions */}
        <div className="bg-white rounded-lg shadow-sm">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">Recent Transactions</h2>
          </div>
          <div className="divide-y divide-gray-200">
            {transactions.length === 0 ? (
              <p className="p-6 text-gray-500">No transactions found</p>
            ) : (
              transactions.map((transaction) => (
                <div key={transaction.id} className="p-6 flex items-center justify-between hover:bg-gray-50">
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {transaction.senderId === user?.id ? 'Sent to' : 'Received from'} ID: {
                        transaction.senderId === user?.id ? transaction.receiverId : transaction.senderId
                      }
                    </p>
                    <p className="text-sm text-gray-500">
                      {new Date(transaction.timestamp).toLocaleDateString()}
                    </p>
                  </div>
                  <div className={`text-sm font-medium ${
                    transaction.senderId === user?.id ? 'text-red-600' : 'text-green-600'
                  }`}>
                    {transaction.senderId === user?.id ? '-' : '+'} ${transaction.amount}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
