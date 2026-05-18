"use client";

import { useState, useEffect } from "react";
import axios from "axios";
import { useAuth } from "@/providers/AuthProvider";
import { useRouter } from "next/navigation";
import Navbar from "@/components/landing/Navbar";
import Hero from "@/components/landing/Hero";
import Features from "@/components/landing/Features";
import Footer from "@/components/landing/Footer";
import { motion } from "framer-motion";
import { Activity } from "lucide-react";

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
    <main className="min-h-screen bg-slate-950 font-sans selection:bg-blue-500/30">
      <Navbar />
      
      <Hero />
      
      <Features />

      {/* Status Section */}
      <section id="status" className="py-20 bg-slate-950/50 border-t border-white/5">
        <div className="container px-6 flex flex-col items-center">
          <div className="bg-slate-900/50 border border-slate-800 p-8 rounded-3xl max-w-xl w-full">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center">
                  <Activity className={loading ? "text-slate-500 animate-pulse" : health?.status === "UP" ? "text-green-500" : "text-red-500"} />
                </div>
                <div>
                  <h3 className="font-bold text-white text-lg">System Nexus</h3>
                  <p className="text-slate-500 text-sm">Real-time backend connectivity</p>
                </div>
              </div>
              {loading ? (
                <div className="h-6 w-24 bg-slate-800 rounded animate-pulse" />
              ) : (
                <div className={`px-3 py-1 rounded-full text-xs font-bold ${health?.status === "UP" ? "bg-green-500/10 text-green-400 border border-green-500/20" : "bg-red-500/10 text-red-400 border border-red-500/20"}`}>
                  {health?.status || "UNKNOWN"}
                </div>
              )}
            </div>
            
            <div className="space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-slate-400">API Gateway</span>
                <span className="text-white">Active</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-slate-400">Database Cluster</span>
                <span className="text-white">Connected</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-slate-400">AI Compute Node</span>
                <span className="text-white">Standby</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </main>
  );
}
