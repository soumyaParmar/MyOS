'use client';

import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Settings, 
  Save, 
  Bell, 
  DollarSign, 
  Briefcase, 
  Loader2, 
  Cpu, 
  Plus, 
  Trash2, 
  Sparkles, 
  Eye, 
  EyeOff, 
  Globe, 
  Key,
  CheckCircle2,
  X
} from 'lucide-react';
import { toast } from 'sonner';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import { preferencesService } from '@/services/preferences.service';
import { aiModelService, AiModelConfig } from '@/services/ai-model.service';
import { UserPreferencesRequestDTO } from '@/types/preferences';

import ProtectedRoute from '@/components/auth/ProtectedRoute';
import { Sidebar } from '@/components/layout/Sidebar';
import { useAuth } from '@/providers/AuthProvider';

/**
 * preferencesSchema
 * 
 * Defines the validation rules for our preferences form.
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
  const [activeTab, setActiveTab] = useState<'preferences' | 'ai-models'>('preferences');

  // AI Models state variables
  const [models, setModels] = useState<AiModelConfig[]>([]);
  const [modelsLoading, setModelsLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [savingModel, setSavingModel] = useState(false);
  const [showApiKey, setShowApiKey] = useState(false);
  const [activatingId, setActivatingId] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Dynamic Add Model form state variables
  const [provider, setProvider] = useState<'OLLAMA' | 'OPENAI' | 'ANTHROPIC'>('OLLAMA');
  const [modelName, setModelName] = useState('llama3');
  const [baseUrl, setBaseUrl] = useState('http://localhost:11434');
  const [apiKey, setApiKey] = useState('');

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

  // Fetch standard preferences
  useEffect(() => {
    const fetchPreferences = async () => {
      try {
        const data = await preferencesService.getPreferences();
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

  // Fetch AI model configurations
  const loadAiModels = async (silent = false) => {
    try {
      if (!silent) setModelsLoading(true);
      const data = await aiModelService.getAllModels();
      setModels(data);
    } catch (err: any) {
      console.error('Failed to load AI models:', err);
      toast.error('Failed to load registered AI models');
    } finally {
      setModelsLoading(false);
    }
  };

  // Watch tab selection to load secondary models dynamically
  useEffect(() => {
    if (activeTab === 'ai-models') {
      loadAiModels();
    }
  }, [activeTab]);

  const onSubmitPreferences = async (values: PreferencesFormValues) => {
    setSaving(true);
    try {
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

  // Handle pre-fill values dynamically when changing provider dropdown
  const handleProviderChange = (selected: 'OLLAMA' | 'OPENAI' | 'ANTHROPIC') => {
    setProvider(selected);
    setShowApiKey(false);
    if (selected === 'OLLAMA') {
      setModelName('llama3');
      setBaseUrl('http://localhost:11434');
      setApiKey('');
    } else if (selected === 'OPENAI') {
      setModelName('gpt-4o');
      setBaseUrl('');
      setApiKey('');
    } else if (selected === 'ANTHROPIC') {
      setModelName('claude-3-5-sonnet-20241022');
      setBaseUrl('');
      setApiKey('');
    }
  };

  // Handle saving new AI model
  const handleSaveModel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (provider !== 'OLLAMA' && (!apiKey || apiKey.trim() === '')) {
      toast.error('API Key is required for cloud providers.');
      return;
    }
    
    setSavingModel(true);
    try {
      await aiModelService.saveModel({
        provider,
        modelName,
        baseUrl: provider === 'OLLAMA' ? baseUrl : undefined,
        apiKey: provider !== 'OLLAMA' ? apiKey : undefined,
        active: false,
      });
      toast.success(`${provider} configuration added successfully!`);
      setModalOpen(false);
      loadAiModels();
    } catch (err: any) {
      console.error('Failed to save AI model:', err);
      toast.error(err.response?.data?.message || 'Failed to save model configuration.');
    } finally {
      setSavingModel(false);
    }
  };

  // Handle activating dynamic AI model
  const handleActivateModel = async (id: string) => {
    setActivatingId(id);
    try {
      await aiModelService.activateModel(id);
      toast.success('System AI Model active switch completed.');
      await loadAiModels(true); // silent refresh
    } catch (err: any) {
      console.error('Failed to activate model:', err);
      toast.error('Failed to activate the selected model configuration.');
    } finally {
      setActivatingId(null);
    }
  };

  // Handle deleting model configuration
  const handleDeleteModel = async (id: string) => {
    setDeletingId(id);
    try {
      await aiModelService.deleteModel(id);
      toast.success('AI Model configuration deleted successfully.');
      await loadAiModels(true);
    } catch (err: any) {
      console.error('Failed to delete model:', err);
      toast.error(err.response?.data?.message || 'Failed to delete configuration.');
    } finally {
      setDeletingId(null);
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
                Configure your MyOS system preferences and AI brain providers.
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

          {/* Premium Animated Tab Selector */}
          <div className="flex border-b border-slate-800 mb-8 max-w-4xl mx-auto">
            <button
              onClick={() => setActiveTab('preferences')}
              className={`px-6 py-3.5 font-semibold text-sm transition-all relative outline-none flex items-center gap-2 ${
                activeTab === 'preferences' ? 'text-blue-500' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Settings className="w-4 h-4" />
              System Preferences
              {activeTab === 'preferences' && (
                <motion.div
                  layoutId="activeTabUnderline"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-500"
                  transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                />
              )}
            </button>
            <button
              onClick={() => setActiveTab('ai-models')}
              className={`px-6 py-3.5 font-semibold text-sm transition-all relative outline-none flex items-center gap-2 ${
                activeTab === 'ai-models' ? 'text-blue-500' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Cpu className="w-4 h-4" />
              AI Model configurations
              {activeTab === 'ai-models' && (
                <motion.div
                  layoutId="activeTabUnderline"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-500"
                  transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                />
              )}
            </button>
          </div>

          <div className="max-w-4xl mx-auto">
            <AnimatePresence mode="wait">
              {/* Tab 1: System Preferences Form */}
              {activeTab === 'preferences' && (
                <motion.div
                  key="preferences-tab"
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: 10 }}
                  transition={{ duration: 0.2 }}
                  className="space-y-6"
                >
                  <form onSubmit={handleSubmit(onSubmitPreferences)} className="space-y-6">
                    {/* Job Search Section */}
                    <Card className="bg-slate-900/40 border-slate-800 backdrop-blur-xl overflow-hidden relative">
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

                    {/* Financial Section */}
                    <Card className="bg-slate-900/40 border-slate-800 backdrop-blur-xl overflow-hidden relative">
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

                    {/* Notifications Section */}
                    <Card className="bg-slate-900/40 border-slate-800 backdrop-blur-xl overflow-hidden relative">
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

                    {/* Action Button */}
                    <div className="flex justify-end pt-4">
                      <Button 
                        type="submit" 
                        disabled={saving}
                        className="bg-blue-600 hover:bg-blue-500 text-white px-8 h-12 rounded-xl transition-all shadow-lg shadow-blue-600/20 active:scale-95 cursor-pointer"
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
                </motion.div>
              )}

              {/* Tab 2: AI Model Configurations Panel */}
              {activeTab === 'ai-models' && (
                <motion.div
                  key="ai-models-tab"
                  initial={{ opacity: 0, x: 10 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -10 }}
                  transition={{ duration: 0.2 }}
                  className="space-y-6"
                >
                  <div className="flex justify-between items-center mb-4">
                    <div>
                      <h3 className="text-xl font-bold flex items-center gap-2">
                        <Cpu className="w-5 h-5 text-indigo-400" />
                        AI Engines & LLM Providers
                      </h3>
                      <p className="text-sm text-slate-400 mt-1">Connect your local Ollama server or configure external API credentials dynamically.</p>
                    </div>
                    <Button
                      onClick={() => setModalOpen(true)}
                      className="bg-indigo-600 hover:bg-indigo-500 text-white gap-2 h-10 px-5 rounded-lg shadow-lg hover:shadow-indigo-500/20 transition-all active:scale-95 cursor-pointer"
                    >
                      <Plus className="w-4 h-4" />
                      Add Model
                    </Button>
                  </div>

                  {modelsLoading ? (
                    <div className="flex flex-col items-center justify-center py-20 gap-4 bg-slate-900/10 border border-slate-800/40 rounded-xl backdrop-blur-xl">
                      <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
                      <p className="text-slate-400">Fetching active models...</p>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {models.length === 0 ? (
                        <div className="col-span-full py-16 text-center bg-slate-900/20 border border-dashed border-slate-800 rounded-xl">
                          <Cpu className="w-12 h-12 text-slate-600 mx-auto mb-4" />
                          <p className="text-slate-400 font-semibold">No model configurations found</p>
                          <p className="text-slate-500 text-sm mt-1">Click the button above to add your first LLM provider configuration.</p>
                        </div>
                      ) : (
                        models.map((model) => (
                          <motion.div
                            key={model.id}
                            layout
                            className={`relative rounded-xl border bg-slate-900/40 backdrop-blur-xl p-6 flex flex-col justify-between transition-all duration-300 hover:-translate-y-0.5 ${
                              model.active
                                ? 'border-indigo-500/80 shadow-lg shadow-indigo-500/5 ring-1 ring-indigo-500/30'
                                : 'border-slate-800 hover:border-slate-700'
                            }`}
                          >
                            {/* Exclusive Glow Stripe for Active Models */}
                            {model.active && (
                              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-violet-500 via-indigo-500 to-cyan-500 rounded-t-xl" />
                            )}

                            <div>
                              <div className="flex justify-between items-start mb-4">
                                <span className={`px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider ${
                                  model.provider === 'OLLAMA' ? 'bg-slate-800/80 text-slate-300 border border-slate-700' :
                                  model.provider === 'OPENAI' ? 'bg-teal-950/70 text-teal-300 border border-teal-800' :
                                  'bg-amber-950/70 text-amber-300 border border-amber-800'
                                }`}>
                                  {model.provider}
                                </span>
                                
                                {model.active && (
                                  <span className="flex items-center gap-1 text-[10px] text-indigo-400 font-extrabold bg-indigo-950/60 border border-indigo-900/60 px-2 py-0.5 rounded-full animate-pulse">
                                    <Sparkles className="w-3 h-3" />
                                    Active System LLM
                                  </span>
                                )}
                              </div>

                              <h4 className="text-lg font-bold text-slate-100 flex items-center gap-2">
                                {model.modelName}
                              </h4>
                              <p className="text-[10px] text-slate-500 font-mono mt-1 mb-4 select-all">UUID: {model.id}</p>

                              <div className="space-y-2.5 text-xs mb-6 border-t border-slate-800/50 pt-4">
                                {model.baseUrl && (
                                  <div className="flex justify-between items-center">
                                    <span className="text-slate-500 flex items-center gap-1.5">
                                      <Globe className="w-3.5 h-3.5 text-slate-500" />
                                      Base URL:
                                    </span>
                                    <span className="font-mono text-slate-300 truncate max-w-[200px]" title={model.baseUrl}>
                                      {model.baseUrl}
                                    </span>
                                  </div>
                                )}
                                <div className="flex justify-between items-center">
                                  <span className="text-slate-500 flex items-center gap-1.5">
                                    <Key className="w-3.5 h-3.5 text-slate-500" />
                                    Credentials:
                                  </span>
                                  <span className="font-mono text-slate-300">
                                    {model.provider === 'OLLAMA' ? (
                                      <span className="text-slate-500">None (Local Host)</span>
                                    ) : (
                                      <span className="text-indigo-400/80">AES Encrypted</span>
                                    )}
                                  </span>
                                </div>
                              </div>
                            </div>

                            <div className="flex items-center justify-between gap-4 mt-auto pt-4 border-t border-slate-850">
                              {!model.active ? (
                                <Button
                                  onClick={() => model.id && handleActivateModel(model.id)}
                                  disabled={activatingId === model.id}
                                  className="flex-1 bg-slate-800/80 hover:bg-indigo-600/90 text-slate-300 hover:text-white font-semibold py-2 rounded-lg text-xs transition-all border border-slate-700 hover:border-indigo-500 active:scale-97 cursor-pointer"
                                >
                                  {activatingId === model.id ? (
                                    <>
                                      <Loader2 className="w-3.5 h-3.5 mr-1.5 animate-spin" />
                                      Activating...
                                    </>
                                  ) : (
                                    'Activate Model'
                                  )}
                                </Button>
                              ) : (
                                <span className="flex-1 text-center bg-indigo-950/20 text-indigo-400 font-semibold py-2 rounded-lg text-xs border border-indigo-950/40">
                                  Primary Engine
                                </span>
                              )}

                              {!model.active && (
                                <Button
                                  onClick={() => model.id && handleDeleteModel(model.id)}
                                  disabled={deletingId === model.id}
                                  variant="ghost"
                                  className="p-2 bg-slate-800/30 hover:bg-rose-950/70 text-slate-400 hover:text-rose-400 rounded-lg border border-slate-800 hover:border-rose-900/40 transition-all cursor-pointer"
                                >
                                  {deletingId === model.id ? (
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                  ) : (
                                    <Trash2 className="w-4 h-4" />
                                  )}
                                </Button>
                              )}
                            </div>
                          </motion.div>
                        ))
                      )}
                    </div>
                  )}
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Modal Overlay for Adding New Model */}
          <AnimatePresence>
            {modalOpen && (
              <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
                <motion.div
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.95 }}
                  transition={{ duration: 0.2 }}
                  className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 relative overflow-hidden"
                >
                  <button
                    onClick={() => setModalOpen(false)}
                    className="absolute top-4 right-4 text-slate-400 hover:text-white transition-colors cursor-pointer"
                  >
                    <X className="w-5 h-5" />
                  </button>

                  <h2 className="text-2xl font-extrabold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-violet-400 to-indigo-300">
                    Configure LLM Provider
                  </h2>
                  <p className="text-slate-400 text-xs mb-6">Select your provider and configure credentials safely. Data is stored securely in PostgreSQL.</p>
                  
                  <form onSubmit={handleSaveModel} className="space-y-4">
                    {/* Provider Selection */}
                    <div className="space-y-1.5">
                      <Label htmlFor="modalProvider" className="text-slate-400 text-sm">Provider Type</Label>
                      <select
                        id="modalProvider"
                        value={provider}
                        onChange={(e) => handleProviderChange(e.target.value as any)}
                        className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:border-indigo-500/80 transition-colors text-sm font-medium"
                      >
                        <option value="OLLAMA">Ollama (Free Local Host)</option>
                        <option value="OPENAI">OpenAI (GPT Engine)</option>
                        <option value="ANTHROPIC">Anthropic (Claude Engine)</option>
                      </select>
                    </div>

                    {/* Model Name */}
                    <div className="space-y-1.5">
                      <Label htmlFor="modalModelName" className="text-slate-400 text-sm">Model Choice</Label>
                      {provider === 'OLLAMA' ? (
                        <Input
                          id="modalModelName"
                          type="text"
                          required
                          value={modelName}
                          onChange={(e) => setModelName(e.target.value)}
                          placeholder="e.g. llama3, mistral, deepseek"
                          className="bg-slate-950 border-slate-800 text-white focus:border-indigo-500/80 transition-colors py-5"
                        />
                      ) : (
                        <select
                          id="modalModelName"
                          value={modelName}
                          onChange={(e) => setModelName(e.target.value)}
                          className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:border-indigo-500/80 transition-colors text-sm font-medium"
                        >
                          {provider === 'OPENAI' ? (
                            <>
                              <option value="gpt-4o">gpt-4o (Premium Multi-modal)</option>
                              <option value="gpt-4-turbo">gpt-4-turbo</option>
                              <option value="gpt-3.5-turbo">gpt-3.5-turbo</option>
                            </>
                          ) : (
                            <>
                              <option value="claude-3-5-sonnet-20241022">claude-3-5-sonnet-20241022 (Fast & Capable)</option>
                              <option value="claude-3-opus-20240229">claude-3-opus (Deep Reasoning)</option>
                              <option value="claude-3-haiku-20240307">claude-3-haiku (Ultra Lightweight)</option>
                            </>
                          )}
                        </select>
                      )}
                    </div>

                    {/* Conditional Base URL (Ollama Only) */}
                    {provider === 'OLLAMA' && (
                      <div className="space-y-1.5">
                        <Label htmlFor="modalBaseUrl" className="text-slate-400 text-sm font-semibold">Ollama Server API Base URL</Label>
                        <Input
                          id="modalBaseUrl"
                          type="text"
                          required
                          value={baseUrl}
                          onChange={(e) => setBaseUrl(e.target.value)}
                          placeholder="http://localhost:11434"
                          className="bg-slate-950 border-slate-800 text-white focus:border-indigo-500/80 transition-colors font-mono py-5"
                        />
                      </div>
                    )}

                    {/* Conditional API Key (Cloud Only) */}
                    {provider !== 'OLLAMA' && (
                      <div className="space-y-1.5">
                        <Label htmlFor="modalApiKey" className="text-slate-400 text-sm">Provider API Secret Key</Label>
                        <div className="relative">
                          <Input
                            id="modalApiKey"
                            type={showApiKey ? 'text' : 'password'}
                            required
                            value={apiKey}
                            onChange={(e) => setApiKey(e.target.value)}
                            placeholder="sk-proj-..."
                            className="bg-slate-950 border-slate-800 text-white focus:border-indigo-500/80 transition-colors font-mono pr-10 py-5"
                          />
                          <button
                            type="button"
                            onClick={() => setShowApiKey(!showApiKey)}
                            className="absolute right-3 top-3 text-slate-500 hover:text-white transition-colors cursor-pointer"
                          >
                            {showApiKey ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                          </button>
                        </div>
                      </div>
                    )}

                    {/* Form Buttons */}
                    <div className="flex gap-3 justify-end pt-4 border-t border-slate-800/60 mt-6">
                      <Button
                        type="button"
                        onClick={() => setModalOpen(false)}
                        className="bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold px-4 py-2 rounded-lg transition-colors text-xs cursor-pointer"
                      >
                        Cancel
                      </Button>
                      <Button
                        type="submit"
                        disabled={savingModel}
                        className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold px-5 py-2 rounded-lg transition-all text-xs shadow-md hover:shadow-indigo-500/25 cursor-pointer"
                      >
                        {savingModel ? (
                          <>
                            <Loader2 className="w-3 h-3 mr-1.5 animate-spin" />
                            Saving Model...
                          </>
                        ) : (
                          'Save Configuration'
                        )}
                      </Button>
                    </div>
                  </form>
                </motion.div>
              </div>
            )}
          </AnimatePresence>
        </main>
      </div>
    </ProtectedRoute>
  );
}
