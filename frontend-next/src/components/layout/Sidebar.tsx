'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { LayoutDashboard, Mail, Settings, User, LogOut, Brain } from 'lucide-react';
import { useAuth } from '@/providers/AuthProvider';

export function Sidebar() {
  const pathname = usePathname();
  const { logout } = useAuth();

  const navItems = [
    { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
    { name: 'Knowledge Base', href: '/dashboard/knowledge', icon: Brain },
    { name: 'Profile', href: '/profile', icon: User },
    { name: 'Messages', href: '#', icon: Mail },
    { name: 'Settings', href: '/dashboard/settings', icon: Settings },
  ];

  return (
    <aside className="w-64 bg-slate-900/50 border-r border-slate-800 p-6 hidden md:flex flex-col h-screen sticky top-0">
      <div className="flex items-center gap-3 mb-10">
        <div className="bg-blue-600 p-2 rounded-lg">
          <LayoutDashboard className="w-6 h-6 text-white" />
        </div>
        <h1 className="text-xl font-bold tracking-tight">MyOS</h1>
      </div>

      <nav className="flex-1 space-y-2">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link key={item.name} href={item.href}>
              <Button 
                variant="ghost" 
                className={`w-full justify-start gap-3 transition-all ${
                  isActive 
                    ? 'bg-slate-800 text-white' 
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <item.icon className="w-4 h-4" />
                {item.name}
              </Button>
            </Link>
          );
        })}
      </nav>

      <div className="pt-6 border-t border-slate-800">
        <Button 
          variant="ghost" 
          className="w-full justify-start gap-3 text-red-400 hover:text-red-300 hover:bg-red-400/10"
          onClick={logout}
        >
          <LogOut className="w-4 h-4" />
          Logout
        </Button>
      </div>
    </aside>
  );
}
