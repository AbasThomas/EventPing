'use client';

import { 
  Slack, 
  Calendar, 
  Mail, 
  MessageSquare, 
  Globe, 
  Zap,
  CheckCircle2
} from 'lucide-react';

const integrations = [
  {
    name: 'Google Calendar',
    description: 'Sync your events automatically with Google Calendar.',
    icon: Calendar,
    color: 'text-blue-400',
    bg: 'bg-blue-400/10'
  },
  {
    name: 'Slack',
    description: 'Get real-time notifications for new registrants and event updates.',
    icon: Slack,
    color: 'text-purple-400',
    bg: 'bg-purple-400/10'
  },
  {
    name: 'Gmail',
    description: 'Send automated email reminders and invitations via Gmail.',
    icon: Mail,
    color: 'text-red-400',
    bg: 'bg-red-400/10'
  },
  {
    name: 'WhatsApp',
    description: 'Reach participants directly with WhatsApp integration.',
    icon: MessageSquare,
    color: 'text-green-400',
    bg: 'bg-green-400/10'
  },
  {
    name: 'Zoom',
    description: 'Automatically create Zoom meetings for virtual events.',
    icon: Globe,
    color: 'text-blue-500',
    bg: 'bg-blue-500/10'
  },
  {
    name: 'Zapier',
    description: 'Connect EventPing with over 5,000+ apps using Zapier.',
    icon: Zap,
    color: 'text-orange-400',
    bg: 'bg-orange-400/10'
  }
];

export function Integrations() {
  return (
    <section id="integrations" className="py-24 relative overflow-hidden">
      <div className="container mx-auto px-4 relative z-10">
        <div className="text-center mb-16 px-4">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
            Connect Your Favorite Tools
          </h2>
          <p className="text-slate-400 max-w-2xl mx-auto text-lg">
            EventPing works seamlessly with the tools you already use to make event management effortless.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
          {integrations.map((item, index) => (
            <div 
              key={index}
              className="glass-panel p-8 rounded-2xl border border-slate-800 hover:border-slate-700 transition-all duration-300 group"
            >
              <div className={`w-12 h-12 ${item.bg} rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300`}>
                <item.icon className={`w-6 h-6 ${item.color}`} />
              </div>
              <h3 className="text-xl font-semibold text-white mb-3 flex items-center gap-2">
                {item.name}
                <CheckCircle2 className="w-4 h-4 text-emerald-500 opacity-0 group-hover:opacity-100 transition-opacity" />
              </h3>
              <p className="text-slate-400 leading-relaxed">
                {item.description}
              </p>
            </div>
          ))}
        </div>

        <div className="mt-16 text-center">
          <button className="px-8 py-3 bg-white/5 hover:bg-white/10 text-white rounded-full border border-white/10 transition-all duration-300 font-medium">
            View All 50+ Integrations
          </button>
        </div>
      </div>
    </section>
  );
}
