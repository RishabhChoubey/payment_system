"use client";

import { useRouter } from 'next/navigation';

export default function RequestPage() {
  const router = useRouter();

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
      <div className="max-w-md w-full bg-white rounded-lg shadow p-6">
        <h1 className="text-2xl font-bold mb-4">Request Money</h1>
        <p className="mb-4 text-sm text-gray-600">This is a placeholder page for requesting money.</p>
        <button
          className="bg-blue-600 text-white px-4 py-2 rounded"
          onClick={() => router.push('/dashboard')}
        >
          Back to Dashboard
        </button>
      </div>
    </div>
  );
}

