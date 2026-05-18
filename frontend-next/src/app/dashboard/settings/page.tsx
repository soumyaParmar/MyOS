'use client';

import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { motion } from 'framer-motion';
import { Settings, Save, Bell, DollarSign, Briefcase, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import { preferencesService } from '@/services/preferences.service';
import { UserPreferencesRequestDTO } from '@/types/preferences';

import ProtectedRoute from '@/components/auth/ProtectedRoute';
import { Sidebar } from '@/components/layout/Sidebar';
import { useAuth } from '@/providers/AuthProvider';

/**
 * preferencesSchema
 * 
 * Defines the validation rules for our preferences form.
 * We convert the jobTypes string to an array before sending it to the backend.
 */
const preferencesSchema = z.object({
  jobTypes: z.string().min(1, 'Please enter at least one job type (comma separated)'),
  monthlyBudgetLimit: z.number().min(0, 'Budget cannot be negative'),
  emailNotificationsEnabled: z.boolean(),
  pushNotificationsEnabled: z.boolean(),
});

type PreferencesFormValues = z.infer<typeof preferencesSchema>;

export default function PreferencesPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // Initialize the form with React Hook Form
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<PreferencesFormValues>({
    resolver: zodResolver(preferencesSchema),
    defaultValues: {
      jobTypes: '',
      monthlyBudgetLimit: 0,
      emailNotificationsEnabled: false,
      pushNotificationsEnabled: false,
    },
  });

  // Watch the switch values for real-time UI updates
  const emailEnabled = watch('emailNotificationsEnabled');
  const pushEnabled = watch('pushNotificationsEnabled');

  useEffect(() => {
    const fetchPreferences = async () => {
      try {
        const data = await preferencesService.getPreferences();
        // Reset the form with data from the backend
        reset({
          jobTypes: data.jobTypes.join(', '),
          monthlyBudgetLimit: data.monthlyBudgetLimit,
          emailNotificationsEnabled: data.emailNotificationsEnabled,
          pushNotificationsEnabled: data.pushNotificationsEnabled,
        });
      } catch (error) {
        console.error('Failed to load preferences:', error);
        toast.error('Failed to load preferences');
      } finally {
        setLoading(false);
      }
    };

    fetchPreferences();
  }, [reset]);

  const onSubmit = async (values: PreferencesFormValues) => {
    setSaving(true);
    try {
      // Prepare the request DTO by splitting the jobTypes string
      const request: UserPreferencesRequestDTO = {
        ...values,
        jobTypes: values.jobTypes.split(',').map((s) => s.trim()).filter((s) => s !== ''),
      };
      await preferencesService.updatePreferences(request);
      toast.success('Preferences updated successfully!');
    } catch (error) {
      console.error('Failed to update preferences:', error);
      toast.error('Failed to update preferences');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4 bg-slate-950 text-slate-100">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
        <p className="text-slate-400 animate-pulse">Loading preferences...</p>
      </div>
    );
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-slate-950 text-slate-100 flex">
        <Sidebar />

        {/* Main Content */}
        <main className="flex-1 p-6 md:p-10 overflow-auto">
          <header className="flex justify-between items-center mb-10">
            <div>
              <h2 className="text-3xl font-bold flex items-center gap-3">
                <Settings className="w-8 h-8 text-blue-500" />
                Settings
              </h2>
              <p className="text-slate-400">
                Configure your MyOS system preferences.
              </p>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-right hidden sm:block">
                <p className="text-sm font-medium">{user?.name}</p>
                <p className="text-xs text-slate-500">{user?.email}</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center font-bold">
                {user?.name?.charAt(0) || "U"}
              </div>
            </div>
          </header>

          <div className="max-w-4xl mx-auto space-y-8">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {/* Job Search Section */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4 }}
              >
                <Card className="bg-slate-900/40 border-slate-800 backdrop-blur-xl overflow-hidden">
                  <div className="absolute top-0 left-0 w-1 h-full bg-blue-500" />
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Briefcase className="w-5 h-5 text-blue-400" />
                      Job Search Settings
                    </CardTitle>
                    <CardDescription>
                      Define the types of jobs you want the Job Agent to track for you.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="jobTypes">Preferred Job Types / Keywords</Label>
                      <Input
                        id="jobTypes"
                        placeholder="e.g. Remote, Java, Spring Boot, React"
                        className="bg-slate-950/50 border-slate-700 focus:border-blue-500 transition-colors"
                        {...register('jobTypes')}
                      />
                      {errors.jobTypes && (
                        <p className="text-sm text-red-400">{errors.jobTypes.message}</p>
                      )}
                      <p className="text-xs text-slate-500">Separate multiple keywords with commas. These will be used for automated job matching.</p>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>

              {/* Financial Section */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: 0.1 }}
              >
                <Card className="bg-slate-900/40 border-slate-800 backdrop-blur-xl overflow-hidden">
                  <div className="absolute top-0 left-0 w-1 h-full bg-emerald-500" />
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <DollarSign className="w-5 h-5 text-emerald-400" />
                      Financial Limits
                    </CardTitle>
                    <CardDescription>
                      Set your spending boundaries. The Finance Agent will alert you if you approach these limits.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="monthlyBudgetLimit">Monthly Budget Limit ($)</Label>
                      <div className="relative">
                        <span className="absolute left-3 top-2.5 text-slate-500">$</span>
                        <Input
                          id="monthlyBudgetLimit"
                          type="number"
                          step="0.01"
                          className="bg-slate-950/50 border-slate-700 pl-7 focus:border-emerald-500 transition-colors"
                          {...register('monthlyBudgetLimit', { valueAsNumber: true })}
                        />
                      </div>
                      {errors.monthlyBudgetLimit && (
                        <p className="text-sm text-red-400">{errors.monthlyBudgetLimit.message}</p>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </motion.div>

              {/* Notifications Section */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: 0.2 }}
              >
                <Card className="bg-slate-900/40 border-slate-800 backdrop-blur-xl overflow-hidden">
                  <div className="absolute top-0 left-0 w-1 h-full bg-orange-500" />
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Bell className="w-5 h-5 text-orange-400" />
                      Notification Preferences
                    </CardTitle>
                    <CardDescription>
                      Choose how you want to be notified about agent insights and alerts.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-6">
                    <div className="flex items-center justify-between p-3 rounded-lg hover:bg-slate-800/30 transition-colors">
                      <div className="space-y-0.5">
                        <Label className="text-base">Email Notifications</Label>
                        <p className="text-sm text-slate-400">Receive daily digests and critical alerts via email.</p>
                      </div>
                      <Switch
                        checked={emailEnabled}
                        onCheckedChange={(checked) => setValue('emailNotificationsEnabled', checked)}
                      />
                    </div>
                    <div className="flex items-center justify-between p-3 rounded-lg hover:bg-slate-800/30 transition-colors">
                      <div className="space-y-0.5">
                        <Label className="text-base">Push Notifications</Label>
                        <p className="text-sm text-slate-400">Get real-time updates directly in your browser.</p>
                      </div>
                      <Switch
                        checked={pushEnabled}
                        onCheckedChange={(checked) => setValue('pushNotificationsEnabled', checked)}
                      />
                    </div>
                  </CardContent>
                </Card>
              </motion.div>

              {/* Action Button */}
              <div className="flex justify-end pt-4">
                <Button 
                  type="submit" 
                  disabled={saving}
                  className="bg-blue-600 hover:bg-blue-500 text-white px-8 h-12 rounded-xl transition-all shadow-lg shadow-blue-600/20 active:scale-95"
                >
                  {saving ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Saving...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4 mr-2" />
                      Save Preferences
                    </>
                  )}
                </Button>
              </div>
            </form>
          </div>
        </main>
      </div>
    </ProtectedRoute>
  );
}
