'use client';

import { 
  BarChart3, 
  TrendingUp, 
  Users, 
  Calendar, 
  ArrowUpRight, 
  ArrowDownRight,
  Filter,
  Download
} from 'lucide-react';

const stats = [
  { name: 'Total Views', value: '2,845', change: '+12.5%', trend: 'up', icon: Users },
  { name: 'Avg. Engagement', value: '64.2%', change: '+5.2%', trend: 'up', icon: TrendingUp },
  { name: 'Completion Rate', value: '88.4%', change: '-2.1%', trend: 'down', icon: BarChart3 },
  { name: 'Active Reminders', value: '142', change: '+18.7%', trend: 'up', icon: Calendar },
];

export default function AnalyticsPage() {
  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">Analytics</h1>
          <p className="text-slate-400">Track your event performance and participant engagement.</p>
        </div>
        <div className="flex items-center gap-3">
          <button className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-white px-4 py-2 rounded-xl transition-all border border-white/10">
            <Filter className="w-4 h-4" />
            Filter
          </button>
          <button className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-xl transition-all">
            <Download className="w-4 h-4" />
            Export Data
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => (
          <div key={stat.name} className="glass-panel p-6 rounded-2xl border border-white/10">
            <div className="flex items-center justify-between mb-4">
              <div className="p-2 rounded-lg bg-slate-800 text-slate-400">
                <stat.icon className="w-5 h-5" />
              </div>
              <div className={`flex items-center gap-1 text-sm font-medium ${
                stat.trend === 'up' ? 'text-emerald-400' : 'text-rose-400'
              }`}>
                {stat.trend === 'up' ? <ArrowUpRight className="w-4 h-4" /> : <ArrowDownRight className="w-4 h-4" />}
                {stat.change}
              </div>
            </div>
            <div className="text-2xl font-bold text-white mb-1">{stat.value}</div>
            <div className="text-sm text-slate-400">{stat.name}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="glass-panel p-6 rounded-2xl border border-white/10 h-80 flex flex-col items-center justify-center text-center">
            <BarChart3 className="w-12 h-12 text-slate-700 mb-4" />
            <p className="text-slate-500 font-medium">Engagement over time</p>
            <p className="text-xs text-slate-600 mt-1">Interactive chart will appear here as more data is collected.</p>
        </div>
        <div className="glass-panel p-6 rounded-2xl border border-white/10 h-80 flex flex-col items-center justify-center text-center">
            <Users className="w-12 h-12 text-slate-700 mb-4" />
            <p className="text-slate-500 font-medium">Participant Demographics</p>
            <p className="text-xs text-slate-600 mt-1">Detailed demographic breakdown will be available soon.</p>
        </div>
      </div>
    </div>
  );
}
