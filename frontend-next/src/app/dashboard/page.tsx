'use client';

import React from 'react';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import { useAuth } from '@/providers/AuthProvider';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { User, Shield, LogOut, LayoutDashboard, Settings, Mail } from 'lucide-react';

export default function DashboardPage() {
  const { user, logout } = useAuth();

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-slate-950 text-slate-100 flex">
        {/* Sidebar */}
        <aside className="w-64 bg-slate-900/50 border-r border-slate-800 p-6 hidden md:flex flex-col">
          <div className="flex items-center gap-3 mb-10">
            <div className="bg-blue-600 p-2 rounded-lg">
              <LayoutDashboard className="w-6 h-6 text-white" />
            </div>
            <h1 className="text-xl font-bold tracking-tight">MyOS</h1>
          </div>

          <nav className="flex-1 space-y-2">
            <Button variant="ghost" className="w-full justify-start gap-3 bg-slate-800 text-white">
              <LayoutDashboard className="w-4 h-4" />
              Dashboard
            </Button>
            <Button variant="ghost" className="w-full justify-start gap-3 text-slate-400 hover:text-white">
              <Mail className="w-4 h-4" />
              Messages
            </Button>
            <Button variant="ghost" className="w-full justify-start gap-3 text-slate-400 hover:text-white">
              <Settings className="w-4 h-4" />
              Settings
            </Button>
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

        {/* Main Content */}
        <main className="flex-1 p-6 md:p-10 overflow-auto">
          <header className="flex justify-between items-center mb-10">
            <div>
              <h2 className="text-3xl font-bold">Welcome back, {user?.name}!</h2>
              <p className="text-slate-400">Here's what's happening with your MyOS.</p>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-right hidden sm:block">
                <p className="text-sm font-medium">{user?.name}</p>
                <p className="text-xs text-slate-500">{user?.email}</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center font-bold">
                {user?.name?.charAt(0) || 'U'}
              </div>
            </div>
          </header>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {/* Profile Info Card */}
            <Card className="bg-slate-900/50 border-slate-800">
              <CardHeader className="flex flex-row items-center gap-4">
                <div className="p-2 bg-blue-500/10 rounded-lg">
                  <User className="w-5 h-5 text-blue-500" />
                </div>
                <div>
                  <CardTitle className="text-lg">Profile Details</CardTitle>
                  <CardDescription>Your personal information</CardDescription>
                </div>
              </CardHeader>
              <CardContent className="space-y-4 pt-2">
                <div className="flex justify-between items-center">
                  <span className="text-slate-500 text-sm">Email</span>
                  <span className="text-sm">{user?.email}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-slate-500 text-sm">Name</span>
                  <span className="text-sm">{user?.name}</span>
                </div>
              </CardContent>
            </Card>

            {/* Roles Card */}
            <Card className="bg-slate-900/50 border-slate-800">
              <CardHeader className="flex flex-row items-center gap-4">
                <div className="p-2 bg-purple-500/10 rounded-lg">
                  <Shield className="w-5 h-5 text-purple-500" />
                </div>
                <div>
                  <CardTitle className="text-lg">Security & Roles</CardTitle>
                  <CardDescription>System access levels</CardDescription>
                </div>
              </CardHeader>
              <CardContent className="pt-2">
                <div className="flex flex-wrap gap-2">
                  {user?.roles?.map((role) => (
                    <span 
                      key={role} 
                      className="px-2 py-1 bg-purple-500/20 text-purple-400 text-xs rounded-md border border-purple-500/30"
                    >
                      {role}
                    </span>
                  )) || <span className="text-slate-500 text-sm italic">No roles assigned</span>}
                </div>
              </CardContent>
            </Card>

            {/* Quick Actions Card */}
            <Card className="bg-slate-900/50 border-slate-800">
              <CardHeader>
                <CardTitle className="text-lg">Quick Actions</CardTitle>
                <CardDescription>Commonly used features</CardDescription>
              </CardHeader>
              <CardContent className="space-y-2 pt-0">
                <Button variant="outline" className="w-full justify-start border-slate-700 hover:bg-slate-800">Update Profile</Button>
                <Button variant="outline" className="w-full justify-start border-slate-700 hover:bg-slate-800">Manage API Keys</Button>
              </CardContent>
            </Card>
          </div>

          {/* Phase Notice */}
          <div className="mt-10 p-6 rounded-2xl bg-gradient-to-r from-blue-600/10 to-purple-600/10 border border-blue-500/20">
            <h3 className="text-xl font-bold mb-2">MyOS Phase 1: Foundation Complete</h3>
            <p className="text-slate-400">
              You have successfully authenticated via JWT / SSO. The next phase will involve setting up the AI Brain and Knowledge Base.
            </p>
          </div>
        </main>
      </div>
    </ProtectedRoute>
  );
}
