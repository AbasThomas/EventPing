'use client';

import { useState, useEffect } from 'react';
import { 
  Slack, 
  Calendar, 
  Mail, 
  MessageSquare, 
  Zap,
  Plus,
  Settings2,
  ExternalLink,
  Loader2
} from 'lucide-react';
import { useAuth } from '@/lib/auth-context';
import { api } from '@/lib/api';
import { toast } from 'sonner';

interface IntegrationStatus {
  enabled: boolean;
  configured: boolean;
}

interface IntegrationsState {
  whatsapp: IntegrationStatus;
  gmail: IntegrationStatus;
  discord: IntegrationStatus;
  googleCalendar: IntegrationStatus;
  slack: IntegrationStatus;
}

const INTEGRATION_CONFIG = {
  whatsapp: {
    name: 'WhatsApp',
    description: 'Participant messaging service.',
    icon: MessageSquare,
    color: 'text-green-400',
    bg: 'bg-green-400/10'
  },
  gmail: {
    name: 'Gmail',
    description: 'Automated email reminders.',
    icon: Mail,
    color: 'text-red-400',
    bg: 'bg-red-400/10'
  },
  googleCalendar: {
    name: 'Google Calendar',
    description: 'Sync your events automatically.',
    icon: Calendar,
    color: 'text-blue-400',
    bg: 'bg-blue-400/10'
  },
  slack: {
    name: 'Slack',
    description: 'Real-time event notifications.',
    icon: Slack,
    color: 'text-purple-400',
    bg: 'bg-purple-400/10'
  },
  discord: {
    name: 'Discord',
    description: 'Community event alerts.',
    icon: MessageSquare, // Using MessageSquare as placeholder for Discord icon if not available
    color: 'text-indigo-400',
    bg: 'bg-indigo-400/10'
  }
};

export default function IntegrationsPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [integrations, setIntegrations] = useState<IntegrationsState | null>(null);

  useEffect(() => {
    if (user) {
      fetchIntegrations();
    }
  }, [user]);

  const fetchIntegrations = async () => {
    try {
      const data = await api.get(`/users/${user?.id}/integrations/status`);
      setIntegrations(data);
    } catch (error) {
      console.error('Failed to fetch integrations:', error);
      toast.error('Failed to load integration status');
    } finally {
      setLoading(false);
    }
  };

  const parseKey = (key: string): keyof IntegrationsState => {
      return key as keyof IntegrationsState;
  };

  const handleToggle = async (key: string, enabled: boolean) => {
    try {
      // Optimistic update
      const keyTyped = parseKey(key);
      setIntegrations(prev => prev ? ({
        ...prev,
        [keyTyped]: { ...prev[keyTyped], enabled }
      }) : null);

      // In real app, mapper between key and exact field name might be needed
      // e.g. "whatsapp" -> "enableWhatsApp"
      const updatePayload: any = {};
      if (key === 'whatsapp') updatePayload.enableWhatsApp = enabled;
      if (key === 'gmail') updatePayload.enableGmail = enabled;
      if (key === 'discord') updatePayload.enableDiscord = enabled;
      if (key === 'googleCalendar') updatePayload.enableGoogleCalendar = enabled;
      if (key === 'slack') updatePayload.enableSlack = enabled;

      await api.patch(`/users/${user?.id}/integrations`, updatePayload);
      toast.success(`${INTEGRATION_CONFIG[keyTyped].name} ${enabled ? 'enabled' : 'disabled'}`);
    } catch (error) {
      console.error('Failed to update integration:', error);
      toast.error('Failed to update integration settings');
      fetchIntegrations(); // Revert on error
    }
  };

  const handleConfigure = (key: string) => {
    // Logic to open modal for specific config
    // For WhatsApp: "Enter phone number" modal
    // For others: OAuth flow trigger
    toast.info(`Configuration for ${key} coming soon!`);
    
    if (key === 'whatsapp') {
       const phone = prompt("Enter your phone number for WhatsApp alerts (e.g. 1234567890):");
       if (phone) {
           api.patch(`/users/${user?.id}/integrations`, { phoneNumber: phone })
              .then(() => toast.success("Phone number updated"))
              .catch(() => toast.error("Failed to update phone number"));
       }
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="w-8 h-8 text-indigo-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div>
        <h1 className="text-3xl font-bold text-white mb-2">Integrations</h1>
        <p className="text-slate-400">Manage connections between EventPing and your favorite tools.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {integrations && Object.entries(INTEGRATION_CONFIG).map(([key, config]) => {
          const status = integrations[key as keyof IntegrationsState];
          const isConnected = status?.enabled; // Or defined by 'configured' depending on logic.
          // Let's say 'Connected' means Enabled AND Configured? 
          // Or just Enabled for manual toggles.
          
          return (
            <div key={key} className="glass-panel p-6 rounded-2xl border border-white/10 flex flex-col sm:flex-row gap-6">
              <div className={`w-16 h-16 shrink-0 ${config.bg} rounded-2xl flex items-center justify-center`}>
                <config.icon className={`w-8 h-8 ${config.color}`} />
              </div>
              <div className="flex-1 space-y-4">
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <h3 className="text-xl font-bold text-white">{config.name}</h3>
                    <span className={`text-xs px-2 py-1 rounded-full ${
                      isConnected ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-800 text-slate-500'
                    }`}>
                      {isConnected ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                  <p className="text-slate-400 text-sm">{config.description}</p>
                </div>
                <div className="flex items-center gap-3">
                  {isConnected ? (
                    <>
                      <button 
                        onClick={() => handleConfigure(key)}
                        className="flex-1 bg-white/5 hover:bg-white/10 text-white px-4 py-2 rounded-xl transition-all border border-white/10 text-sm font-medium flex items-center justify-center gap-2"
                      >
                        <Settings2 className="w-4 h-4" />
                        Configure
                      </button>
                      <button 
                        onClick={() => handleToggle(key, false)}
                        className="p-2 bg-rose-500/10 text-rose-500 hover:bg-rose-500/20 rounded-xl transition-all border border-rose-500/20"
                        title="Disconnect"
                      >
                        <ExternalLink className="w-4 h-4 rotate-180" /> {/* Just an icon for disconnect */}
                      </button>
                    </>
                  ) : (
                    <button 
                        onClick={() => handleToggle(key, true)}
                        className="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-xl transition-all text-sm font-medium flex items-center justify-center gap-2"
                    >
                      <Plus className="w-4 h-4" />
                      Connect
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="glass-panel p-8 rounded-2xl border border-white/10 bg-slate-900/40">
        <div className="flex items-center gap-4 mb-6">
            <div className="p-3 rounded-xl bg-orange-400/20 text-orange-400">
                <Zap className="w-6 h-6" />
            </div>
            <div>
                <h3 className="text-xl font-bold text-white">Advanced Workflows</h3>
                <p className="text-slate-400">Connect to 5,000+ apps via Zapier or use our Webhooks.</p>
            </div>
            <button className="ml-auto bg-white/5 hover:bg-white/10 text-white px-6 py-2 rounded-xl transition-all border border-white/10 font-medium hidden sm:block">
                Get API Key
            </button>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            <div className="p-4 rounded-xl border border-white/5 bg-white/5 hover:bg-white/10 transition-colors cursor-pointer group">
                <h4 className="text-white font-medium mb-1 group-hover:text-indigo-400 transition-colors">Zapier</h4>
                <p className="text-xs text-slate-500">Automate any task with easy triggers.</p>
            </div>
            <div className="p-4 rounded-xl border border-white/5 bg-white/5 hover:bg-white/10 transition-colors cursor-pointer group">
                <h4 className="text-white font-medium mb-1 group-hover:text-indigo-400 transition-colors">Webhooks</h4>
                <p className="text-xs text-slate-500">Receive real-time data to your server.</p>
            </div>
            <div className="p-4 rounded-xl border border-white/5 bg-white/5 hover:bg-white/10 transition-colors cursor-pointer group">
                <h4 className="text-white font-medium mb-1 group-hover:text-indigo-400 transition-colors">REST API</h4>
                <p className="text-xs text-slate-500">Full control over your event data.</p>
            </div>
        </div>
      </div>
    </div>
  );
}
