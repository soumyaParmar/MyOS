"use client";

import { useState, useEffect } from "react";
import { Activity, Layout, ShieldCheck, Zap } from "lucide-react";
import axios from "axios";
import { useAuth } from "@/providers/AuthProvider";
import { useRouter } from "next/navigation";

export default function Home() {
  const [health, setHealth] = useState<{ status: string } | null>(null);
  const [loading, setLoading] = useState(true);
  const { isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isAuthLoading && isAuthenticated) {
      router.push("/dashboard");
    }
  }, [isAuthenticated, isAuthLoading, router]);

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const response = await axios.get("http://localhost:8080/health");
        setHealth(response.data);
      } catch (error) {
        console.error("Backend connection failed:", error);
        setHealth({ status: "DOWN" });
      } finally {
        setLoading(false);
      }
    };
    checkHealth();
  }, []);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center p-6 font-sans">
      <div className="max-w-2xl w-full space-y-8 animate-in fade-in duration-700">
        
        {/* Header */}
        <div className="text-center space-y-4">
          <div className="flex justify-center">
            <div className="bg-blue-600/20 p-4 rounded-2xl border border-blue-500/30">
              <Zap className="w-12 h-12 text-blue-500" />
            </div>
          </div>
          <h1 className="text-5xl font-bold tracking-tight bg-gradient-to-r from-white to-slate-400 bg-clip-text text-transparent">
            MyOS
          </h1>
          <p className="text-slate-400 text-lg">
            Personal AI Operating System
          </p>
        </div>

        {/* Status Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-slate-900/50 border border-slate-800 p-6 rounded-2xl hover:border-slate-700 transition-colors">
            <div className="flex items-center gap-4 mb-2">
              <Activity className="text-green-500" />
              <h3 className="font-semibold text-lg">System Status</h3>
            </div>
            {loading ? (
              <p className="text-slate-500 animate-pulse">Checking connection...</p>
            ) : (
              <p className={`font-medium ${health?.status === "UP" ? "text-green-400" : "text-red-400"}`}>
                Backend: {health?.status || "UNKNOWN"}
              </p>
            )}
          </div>

          <div className="bg-slate-900/50 border border-slate-800 p-6 rounded-2xl hover:border-slate-700 transition-colors">
            <div className="flex items-center gap-4 mb-2">
              <Layout className="text-blue-500" />
              <h3 className="font-semibold text-lg">Foundation</h3>
            </div>
            <p className="text-slate-400">Next.js 15 + Tailwind CSS</p>
          </div>
        </div>

        {/* Phase Info */}
        <div className="bg-blue-600/5 border border-blue-500/20 p-6 rounded-2xl">
          <div className="flex items-center gap-4 mb-4">
            <ShieldCheck className="text-blue-400" />
            <h3 className="font-semibold text-xl">Phase 1: Foundation</h3>
          </div>
          <ul className="space-y-2 text-slate-400">
            <li className="flex items-center gap-2">
              <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
              Backend Initialized
            </li>
            <li className="flex items-center gap-2">
              <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
              Next.js Migration Complete
            </li>
            <li className="flex items-center gap-2 italic">
              <div className="w-1.5 h-1.5 rounded-full bg-slate-600" />
              Next: PostgreSQL & Auth
            </li>
          </ul>
        </div>

        {/* Footer */}
        <div className="text-center pt-8">
          <p className="text-slate-600 text-sm">
            Ready to build the future of personal productivity.
          </p>
        </div>
      </div>
    </div>
  );
}
