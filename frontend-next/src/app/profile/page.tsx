'use client';

import React, { useState, useEffect } from 'react';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import { useAuth } from '@/providers/AuthProvider';
import { Sidebar } from '@/components/layout/Sidebar';
import { profileService } from '@/services/profile.service';
import { UserProfileResponseDTO } from '@/types/profile';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import { User, Save, Edit2, X, GraduationCap, Target, BookOpen, FileText } from 'lucide-react';

export default function ProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserProfileResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState<UserProfileResponseDTO>({
    bio: '',
    skills: '',
    goals: '',
    resumeText: ''
  });

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await profileService.getProfile();
      setProfile(data);
      setEditData(data);
    } catch (error) {
      console.error('Failed to fetch profile:', error);
      toast.error('Failed to load profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    try {
      const updatedProfile = await profileService.updateProfile(editData);
      setProfile(updatedProfile);
      setIsEditing(false);
      toast.success('Profile updated successfully!');
    } catch (error) {
      console.error('Failed to update profile:', error);
      toast.error('Failed to update profile. Please try again.');
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setEditData(prev => ({ ...prev, [name]: value }));
  };

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-slate-950 text-slate-100 flex">
        <Sidebar />

        <main className="flex-1 p-6 md:p-10 overflow-auto">
          <header className="mb-10">
            <h2 className="text-3xl font-bold">User Profile</h2>
            <p className="text-slate-400">Manage your personal information and AI context.</p>
          </header>

          <div className="max-w-4xl space-y-8">
            {/* Core User Info */}
            <Card className="bg-slate-900/50 border-slate-800">
              <CardContent className="flex items-center gap-6 p-6">
                <div className="w-20 h-20 rounded-full bg-blue-600 flex items-center justify-center text-3xl font-bold">
                  {user?.name?.charAt(0) || 'U'}
                </div>
                <div>
                  <h3 className="text-2xl font-bold">{user?.name}</h3>
                  <p className="text-slate-400">{user?.email}</p>
                </div>
              </CardContent>
            </Card>

            {/* Profile Context */}
            <Card className="bg-slate-900/50 border-slate-800">
              <CardHeader className="flex flex-row items-center justify-between border-b border-slate-800 pb-6">
                <div>
                  <CardTitle className="text-xl flex items-center gap-2">
                    <BookOpen className="w-5 h-5 text-blue-500" />
                    Personal Context
                  </CardTitle>
                  <CardDescription>
                    Information used by MyOS agents to personalize your experience.
                  </CardDescription>
                </div>
                {!isEditing && !loading && (
                  <Button variant="outline" size="sm" onClick={() => setIsEditing(true)} className="gap-2 border-slate-700">
                    <Edit2 className="w-4 h-4" />
                    Edit Profile
                  </Button>
                )}
              </CardHeader>

              <CardContent className="pt-6 space-y-6">
                {loading ? (
                  <div className="space-y-4">
                    <Skeleton className="h-4 w-1/4 bg-slate-800" />
                    <Skeleton className="h-20 w-full bg-slate-800" />
                    <Skeleton className="h-4 w-1/4 bg-slate-800" />
                    <Skeleton className="h-20 w-full bg-slate-800" />
                  </div>
                ) : isEditing ? (
                  <div className="space-y-6">
                    <div className="space-y-2">
                      <Label htmlFor="bio" className="text-slate-400">Bio / About You</Label>
                      <Textarea 
                        id="bio" 
                        name="bio"
                        value={editData.bio}
                        onChange={handleInputChange}
                        placeholder="Tell MyOS about yourself..."
                        className="bg-slate-950 border-slate-800 min-h-[100px] focus:ring-blue-600"
                      />
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="skills" className="text-slate-400">Skills</Label>
                      <Input 
                        id="skills" 
                        name="skills"
                        value={editData.skills}
                        onChange={handleInputChange}
                        placeholder="e.g. Java, Spring Boot, React, Design Patterns"
                        className="bg-slate-950 border-slate-800 focus:ring-blue-600"
                      />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="goals" className="text-slate-400">Current Goals</Label>
                      <Input 
                        id="goals" 
                        name="goals"
                        value={editData.goals}
                        onChange={handleInputChange}
                        placeholder="What are you working towards?"
                        className="bg-slate-950 border-slate-800 focus:ring-blue-600"
                      />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="resumeText" className="text-slate-400">Resume / Background (Text)</Label>
                      <Textarea 
                        id="resumeText" 
                        name="resumeText"
                        value={editData.resumeText}
                        onChange={handleInputChange}
                        placeholder="Paste your resume text here for the Job Agent..."
                        className="bg-slate-950 border-slate-800 min-h-[150px] focus:ring-blue-600"
                      />
                    </div>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="space-y-4">
                      <div>
                        <Label className="text-slate-500 text-xs uppercase tracking-wider mb-2 block">Bio</Label>
                        <p className="text-slate-200 bg-slate-950/30 p-4 rounded-lg border border-slate-800/50">
                          {profile?.bio || <span className="text-slate-600 italic">No bio provided.</span>}
                        </p>
                      </div>
                      <div>
                        <Label className="text-slate-500 text-xs uppercase tracking-wider mb-2 block flex items-center gap-2">
                          <GraduationCap className="w-3 h-3" />
                          Skills
                        </Label>
                        <div className="flex flex-wrap gap-2">
                          {profile?.skills ? (
                            profile.skills.split(',').map((skill, index) => (
                              <span key={index} className="px-2 py-1 bg-blue-500/10 text-blue-400 text-xs rounded border border-blue-500/20">
                                {skill.trim()}
                              </span>
                            ))
                          ) : (
                            <span className="text-slate-600 italic text-sm">No skills listed.</span>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="space-y-4">
                      <div>
                        <Label className="text-slate-500 text-xs uppercase tracking-wider mb-2 block flex items-center gap-2">
                          <Target className="w-3 h-3" />
                          Goals
                        </Label>
                        <p className="text-slate-200 bg-slate-950/30 p-4 rounded-lg border border-slate-800/50">
                          {profile?.goals || <span className="text-slate-600 italic">No goals defined.</span>}
                        </p>
                      </div>
                      <div>
                        <Label className="text-slate-500 text-xs uppercase tracking-wider mb-2 block flex items-center gap-2">
                          <FileText className="w-3 h-3" />
                          Resume / Experience
                        </Label>
                        <div className="text-slate-300 text-sm bg-slate-950/30 p-4 rounded-lg border border-slate-800/50 max-h-40 overflow-auto whitespace-pre-wrap">
                          {profile?.resumeText || <span className="text-slate-600 italic">No resume text provided.</span>}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </CardContent>

              {isEditing && (
                <CardFooter className="flex justify-end gap-4 border-t border-slate-800 pt-6 mt-6">
                  <Button variant="ghost" onClick={() => setIsEditing(false)} className="gap-2 text-slate-400">
                    <X className="w-4 h-4" />
                    Cancel
                  </Button>
                  <Button onClick={handleUpdate} className="gap-2 bg-blue-600 hover:bg-blue-500">
                    <Save className="w-4 h-4" />
                    Save Changes
                  </Button>
                </CardFooter>
              )}
            </Card>
          </div>
        </main>
      </div>
    </ProtectedRoute>
  );
}
