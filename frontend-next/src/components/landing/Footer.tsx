"use client";

import { Zap, Globe, MessageSquare, Briefcase } from "lucide-react";
import Link from "next/link";

export default function Footer() {
  return (
    <footer className="bg-slate-950 border-t border-white/5 py-12">
      <div className="container px-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
          <div className="col-span-1 md:col-span-2">
            <Link href="/" className="flex items-center gap-2 mb-6">
              <div className="bg-blue-600 p-1.5 rounded-lg">
                <Zap className="w-4 h-4 text-white fill-white" />
              </div>
              <span className="text-xl font-bold tracking-tighter text-white">MyOS</span>
            </Link>
            <p className="text-slate-400 max-w-sm mb-6 leading-relaxed">
              Your Personal AI Operating System. Orchestrating your digital life with specialized agentic workflows.
            </p>
            <div className="flex items-center gap-4">
              <Link href="#" className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-slate-400 hover:text-white hover:bg-white/10 transition-all">
                <Globe className="w-5 h-5" />
              </Link>
              <Link href="#" className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-slate-400 hover:text-white hover:bg-white/10 transition-all">
                <MessageSquare className="w-5 h-5" />
              </Link>
              <Link href="#" className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-slate-400 hover:text-white hover:bg-white/10 transition-all">
                <Briefcase className="w-5 h-5" />
              </Link>
            </div>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-6">Platform</h4>
            <ul className="space-y-4 text-sm">
              <li><Link href="#features" className="text-slate-400 hover:text-blue-400 transition-colors">Agents</Link></li>
              <li><Link href="#how-it-works" className="text-slate-400 hover:text-blue-400 transition-colors">Infrastructure</Link></li>
              <li><Link href="/login" className="text-slate-400 hover:text-blue-400 transition-colors">Login</Link></li>
              <li><Link href="/signup" className="text-slate-400 hover:text-blue-400 transition-colors">Get Started</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-6">Resources</h4>
            <ul className="space-y-4 text-sm">
              <li><Link href="#" className="text-slate-400 hover:text-blue-400 transition-colors">Documentation</Link></li>
              <li><Link href="#" className="text-slate-400 hover:text-blue-400 transition-colors">API Reference</Link></li>
              <li><Link href="#" className="text-slate-400 hover:text-blue-400 transition-colors">Community</Link></li>
              <li><Link href="#" className="text-slate-400 hover:text-blue-400 transition-colors">Github</Link></li>
            </ul>
          </div>
        </div>

        <div className="pt-8 border-t border-white/5 flex flex-col md:row items-center justify-between gap-4">
          <p className="text-slate-500 text-xs">
            &copy; {new Date().getFullYear()} MyOS Personal AI. Built for the future of agentic productivity.
          </p>
          <div className="flex gap-6 text-xs text-slate-500">
            <Link href="#" className="hover:text-slate-300 transition-colors">Privacy Policy</Link>
            <Link href="#" className="hover:text-slate-300 transition-colors">Terms of Service</Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
