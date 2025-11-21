"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";

export default function SendTransactionPage() {
  const [recipient, setRecipient] = useState("");
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      // Get token from localStorage (adjust if you use cookies or context)
      const token = localStorage.getItem("token");
      if (!token) {
        setError("You must be logged in to send money.");
        setLoading(false);
        return;
      }
      const response = await axios.post(
        "http://localhost:8080/api/transactions/create",
        {
          receiverEmail: recipient,
          amount: parseFloat(amount),
          note: note,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSuccess("Transaction successful!");
      setLoading(false);
      setRecipient("");
      setAmount("");
      setNote("");
      // Optionally redirect to dashboard or transactions page
      setTimeout(() => router.push("/dashboard"), 1500);
    } catch (err: any) {
      setError(
        err?.response?.data?.message || "Failed to send money. Please try again."
      );
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-6 bg-white rounded shadow">
      <h2 className="text-2xl font-bold mb-4">Send Money</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block mb-1 font-medium">Recipient Email</label>
          <input
            type="email"
            className="w-full border px-3 py-2 rounded"
            value={recipient}
            onChange={e => setRecipient(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="block mb-1 font-medium">Amount</label>
          <input
            type="number"
            className="w-full border px-3 py-2 rounded"
            value={amount}
            onChange={e => setAmount(e.target.value)}
            min="0.01"
            step="0.01"
            required
          />
        </div>
        <div>
          <label className="block mb-1 font-medium">Note (optional)</label>
          <input
            type="text"
            className="w-full border px-3 py-2 rounded"
            value={note}
            onChange={e => setNote(e.target.value)}
          />
        </div>
        {error && <div className="text-red-600">{error}</div>}
        {success && <div className="text-green-600">{success}</div>}
        <button
          type="submit"
          className="w-full bg-blue-600 text-white py-2 rounded font-semibold hover:bg-blue-700"
          disabled={loading}
        >
          {loading ? "Sending..." : "Send Money"}
        </button>
      </form>
    </div>
  );
}

