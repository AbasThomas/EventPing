'use client';

import { 
  Slack, 
  Calendar, 
  Mail, 
  MessageSquare, 
  Globe, 
  Zap,
  Plus,
  Settings2,
  ExternalLink
} from 'lucide-react';

const integrations = [
  {
    name: 'Google Calendar',
    description: 'Sync your events automatically.',
    icon: Calendar,
    status: 'Connected',
    color: 'text-blue-400',
    bg: 'bg-blue-400/10'
  },
  {
    name: 'Slack',
    description: 'Real-time event notifications.',
    icon: Slack,
    status: 'Not Connected',
    color: 'text-purple-400',
    bg: 'bg-purple-400/10'
  },
  {
    name: 'Gmail',
    description: 'Automated email reminders.',
    icon: Mail,
    status: 'Connected',
    color: 'text-red-400',
    bg: 'bg-red-400/10'
  },
  {
    name: 'WhatsApp',
    description: 'Participant messaging service.',
    icon: MessageSquare,
    status: 'Not Connected',
    color: 'text-green-400',
    bg: 'bg-green-400/10'
  }
];

export default function IntegrationsPage() {
  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div>
        <h1 className="text-3xl font-bold text-white mb-2">Integrations</h1>
        <p className="text-slate-400">Manage connections between EventPing and your favorite tools.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {integrations.map((item) => (
          <div key={item.name} className="glass-panel p-6 rounded-2xl border border-white/10 flex flex-col sm:flex-row gap-6">
            <div className={`w-16 h-16 shrink-0 ${item.bg} rounded-2xl flex items-center justify-center`}>
              <item.icon className={`w-8 h-8 ${item.color}`} />
            </div>
            <div className="flex-1 space-y-4">
              <div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="text-xl font-bold text-white">{item.name}</h3>
                  <span className={`text-xs px-2 py-1 rounded-full ${
                    item.status === 'Connected' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-800 text-slate-500'
                  }`}>
                    {item.status}
                  </span>
                </div>
                <p className="text-slate-400 text-sm">{item.description}</p>
              </div>
              <div className="flex items-center gap-3">
                {item.status === 'Connected' ? (
                  <>
                    <button className="flex-1 bg-white/5 hover:bg-white/10 text-white px-4 py-2 rounded-xl transition-all border border-white/10 text-sm font-medium flex items-center justify-center gap-2">
                      <Settings2 className="w-4 h-4" />
                      Configure
                    </button>
                    <button className="p-2 bg-rose-500/10 text-rose-500 hover:bg-rose-500/20 rounded-xl transition-all border border-rose-500/20">
                      <ExternalLink className="w-4 h-4" />
                    </button>
                  </>
                ) : (
                  <button className="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-xl transition-all text-sm font-medium flex items-center justify-center gap-2">
                    <Plus className="w-4 h-4" />
                    Connect Integration
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
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
