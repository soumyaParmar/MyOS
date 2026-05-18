"use client";

import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { ArrowRight, Sparkles } from "lucide-react";
import Link from "next/link";

export default function Hero() {
  return (
    <section className="relative pt-32 pb-20 overflow-hidden flex flex-col items-center">
      {/* Background Glows */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-4xl h-full -z-10 pointer-events-none">
        <div className="absolute top-20 left-1/4 w-72 h-72 bg-blue-600/20 rounded-full blur-[128px] animate-pulse" />
        <div className="absolute top-40 right-1/4 w-72 h-72 bg-purple-600/20 rounded-full blur-[128px] animate-pulse delay-700" />
      </div>

      <div className="container px-6 text-center">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 text-sm font-medium mb-8"
        >
          <Sparkles className="w-4 h-4" />
          <span>The future of personal productivity is here</span>
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="text-5xl md:text-7xl font-bold tracking-tight text-white mb-6"
        >
          Your Personal <br />
          <span className="bg-gradient-to-r from-blue-400 to-indigo-500 bg-clip-text text-transparent">
            AI Operating System
          </span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="text-lg md:text-xl text-slate-400 max-w-2xl mx-auto mb-10 leading-relaxed"
        >
          MyOS is a state-of-the-art agentic ecosystem that manages your jobs, social media, 
          finances, and habits—so you can focus on what matters.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="flex flex-col sm:flex-row items-center justify-center gap-4"
        >
          <Link href="/signup">
            <Button size="lg" className="bg-blue-600 hover:bg-blue-700 text-white px-8 h-12 text-lg">
              Get Started for Free
              <ArrowRight className="ml-2 w-5 h-5" />
            </Button>
          </Link>
          <Link href="#features">
            <Button size="lg" variant="outline" className="border-slate-800 text-slate-300 hover:bg-slate-900 px-8 h-12 text-lg">
              Explore Agents
            </Button>
          </Link>
        </motion.div>
      </div>

      {/* Floating UI Elements Mockup */}
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 1, delay: 0.5 }}
        className="mt-16 relative w-full max-w-5xl px-6"
      >
        <div className="relative rounded-2xl border border-white/10 bg-slate-900/50 backdrop-blur-sm p-2 shadow-2xl overflow-hidden aspect-video md:aspect-auto md:h-[400px]">
           <div className="absolute inset-0 bg-gradient-to-tr from-blue-500/10 to-transparent" />
           <div className="flex flex-col h-full">
              <div className="flex items-center gap-2 p-4 border-b border-white/5">
                <div className="flex gap-1.5">
                  <div className="w-3 h-3 rounded-full bg-red-500/50" />
                  <div className="w-3 h-3 rounded-full bg-yellow-500/50" />
                  <div className="w-3 h-3 rounded-full bg-green-500/50" />
                </div>
                <div className="ml-4 h-6 w-48 bg-white/5 rounded-md" />
              </div>
              <div className="flex-1 p-6 grid grid-cols-12 gap-6">
                <div className="col-span-3 space-y-4">
                  <div className="h-8 w-full bg-blue-600/20 rounded-lg border border-blue-500/30" />
                  <div className="h-8 w-full bg-white/5 rounded-lg" />
                  <div className="h-8 w-full bg-white/5 rounded-lg" />
                  <div className="h-8 w-full bg-white/5 rounded-lg" />
                </div>
                <div className="col-span-9 bg-black/20 rounded-xl border border-white/5 p-6 relative overflow-hidden">
                   <div className="absolute top-0 right-0 p-4">
                      <div className="flex items-center gap-2 text-blue-400 text-xs font-mono">
                        <div className="w-2 h-2 rounded-full bg-blue-500 animate-pulse" />
                        AGENT_ACTIVE: JOB_CRON
                      </div>
                   </div>
                   <div className="space-y-4">
                      <div className="h-4 w-1/3 bg-white/10 rounded" />
                      <div className="h-20 w-full bg-white/5 rounded-lg" />
                      <div className="grid grid-cols-3 gap-4">
                        <div className="h-24 bg-white/5 rounded-lg" />
                        <div className="h-24 bg-white/5 rounded-lg" />
                        <div className="h-24 bg-white/5 rounded-lg" />
                      </div>
                   </div>
                </div>
              </div>
           </div>
        </div>
      </motion.div>
    </section>
  );
}
