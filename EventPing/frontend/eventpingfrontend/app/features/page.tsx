'use client';

import { Navbar } from '@/components/landing/Navbar';
import { Footer } from '@/components/landing/Footer';
import { Search } from 'lucide-react';

export default function Features() {
  return (
    <main className="min-h-screen relative bg-slate-950 text-slate-200 antialiased selection:bg-indigo-500/30 selection:text-indigo-200 fade-in-page">
      <Navbar />

      {/* Static Utility Background (Features Page) */}
      <div className="fixed inset-0 z-0 pointer-events-none bg-slate-950">
        <div className="absolute inset-0 bg-gradient-to-b from-slate-900/50 to-slate-950"></div>
      </div>

      <div className="z-10 pt-32 pb-20 relative min-h-screen">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          {/* Header */}
          <div className="mb-10 reveal-visible">
            <h1 className="text-3xl md:text-4xl font-light text-white tracking-tight mb-4">
              Features
            </h1>
            <p className="text-slate-400 font-light text-lg max-w-2xl">
              Discover the tools designed to make your event planning seamless.
            </p>
          </div>

          {/* Filters & Search */}
          <div className="flex flex-col md:flex-row gap-4 mb-8 reveal-visible">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                type="text"
                placeholder="Search features..."
                className="w-full bg-slate-900/50 border border-white/10 rounded-lg pl-10 pr-4 py-2.5 text-sm text-white focus:outline-none focus:border-indigo-500 focus:bg-slate-900 transition-all placeholder:text-slate-600"
              />
            </div>
            <div className="flex gap-2 overflow-x-auto pb-2 md:pb-0">
              <button className="px-4 py-2 rounded-lg bg-indigo-600 text-white text-xs font-medium border border-indigo-500 whitespace-nowrap">
                All Features
              </button>
              <button className="px-4 py-2 rounded-lg bg-slate-900/50 text-slate-400 hover:text-white text-xs font-medium border border-white/10 hover:border-white/20 whitespace-nowrap transition-colors">
                Popular
              </button>
              <button className="px-4 py-2 rounded-lg bg-slate-900/50 text-slate-400 hover:text-white text-xs font-medium border border-white/10 hover:border-white/20 whitespace-nowrap transition-colors">
                New
              </button>
              <button className="px-4 py-2 rounded-lg bg-slate-900/50 text-slate-400 hover:text-white text-xs font-medium border border-white/10 hover:border-white/20 whitespace-nowrap transition-colors">
                Free
              </button>
            </div>
          </div>

          {/* Features Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-20">
            {/* Feature Card 1 */}
            <div
              className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible"
              style={{ transitionDelay: '50ms' }}
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-slate-50 text-black flex items-center justify-center font-bold text-sm">
                    SR
                  </div>
                  <div>
                    <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">
                      Smart Reminders
                    </h3>
                    <p className="text-xs text-slate-500">Automated</p>
                  </div>
                </div>
                <span className="px-2 py-1 rounded bg-indigo-500/10 text-indigo-300 text-[10px] font-medium border border-indigo-500/20">
                  Popular
                </span>
              </div>
              <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">
                Set it and forget it. We send timely notifications to all
                confirmed guests via Email and SMS.
              </p>
              <div className="flex items-center justify-between border-t border-white/5 pt-4">
                <div className="text-[10px] text-slate-500 font-mono">
                  Included
                </div>
                <div className="text-[10px] text-slate-500">Unlimited</div>
              </div>
            </div>

            {/* Feature Card 2 */}
            <div
              className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible"
              style={{ transitionDelay: '100ms' }}
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-white text-black flex items-center justify-center font-bold text-sm">
                    G
                  </div>
                  <div>
                    <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">
                      Guest Lists
                    </h3>
                    <p className="text-xs text-slate-500">Management</p>
                  </div>
                </div>
              </div>
              <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">
                Import contacts, manage RSVPs, and track attendance real-time.
              </p>
              <div className="flex items-center justify-between border-t border-white/5 pt-4">
                <div className="text-[10px] text-slate-500 font-mono">
                  CSV Import
                </div>
                <div className="text-[10px] text-slate-500">Easy Export</div>
              </div>
            </div>

            {/* Feature Card 3 */}
            <div
              className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible"
              style={{ transitionDelay: '150ms' }}
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-white text-black flex items-center justify-center font-bold text-sm p-2">
                    <svg viewBox="0 0 24 24" fill="#000">
                      <circle cx="12" cy="12" r="10" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">
                      Analytics
                    </h3>
                    <p className="text-xs text-slate-500">Insights</p>
                  </div>
                </div>
              </div>
              <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">
                Understand attendance patterns and optimize your future events.
              </p>
              <div className="flex items-center justify-between border-t border-white/5 pt-4">
                <div className="text-[10px] text-slate-500 font-mono">
                  Real-time
                </div>
                <div className="text-[10px] text-slate-500">Detailed</div>
              </div>
            </div>

            {/* Feature Card 4 */}
            <div
              className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible"
              style={{ transitionDelay: '200ms' }}
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-orange-500 text-white flex items-center justify-center font-bold text-sm">
                    C
                  </div>
                  <div>
                    <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">
                      Calendar Sync
                    </h3>
                    <p className="text-xs text-slate-500">Integration</p>
                  </div>
                </div>
              </div>
              <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">
                Sync with Google Calendar, Outlook, and iCal automatically.
              </p>
              <div className="flex items-center justify-between border-t border-white/5 pt-4">
                <div className="text-[10px] text-slate-500 font-mono">
                  2-way Sync
                </div>
                <div className="text-[10px] text-slate-500">Instant</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </main>
  );
}
