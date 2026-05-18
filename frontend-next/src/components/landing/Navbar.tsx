"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import { Zap } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function Navbar() {
  return (
    <motion.nav 
      initial={{ y: -100, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      className="fixed top-0 left-0 right-0 z-50 flex items-center justify-between px-6 py-4 backdrop-blur-md border-b border-white/10 bg-slate-950/50"
    >
      <Link href="/" className="flex items-center gap-2 group">
        <div className="bg-blue-600 p-2 rounded-lg group-hover:scale-110 transition-transform">
          <Zap className="w-5 h-5 text-white fill-white" />
        </div>
        <span className="text-xl font-bold tracking-tighter text-white">MyOS</span>
      </Link>

      <div className="hidden md:flex items-center gap-8">
        <Link href="#features" className="text-sm font-medium text-slate-400 hover:text-white transition-colors">Features</Link>
        <Link href="#how-it-works" className="text-sm font-medium text-slate-400 hover:text-white transition-colors">How it works</Link>
        <Link href="#status" className="text-sm font-medium text-slate-400 hover:text-white transition-colors">Status</Link>
      </div>

      <div className="flex items-center gap-4">
        <Link href="/login">
          <Button variant="ghost" className="text-slate-400 hover:text-white">Login</Button>
        </Link>
        <Link href="/signup">
          <Button className="bg-blue-600 hover:bg-blue-700 text-white border-none px-6">Get Started</Button>
        </Link>
      </div>
    </motion.nav>
  );
}
