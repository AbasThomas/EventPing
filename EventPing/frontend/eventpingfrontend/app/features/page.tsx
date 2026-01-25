'use client';

import { Navbar } from '@/components/landing/Navbar';
import { Footer } from '@/components/landing/Footer';

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
              Discover tools designed to make your events a success.
            </p>
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
              </div>
              <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">
                Set it and forget it. We send timely notifications to all
                confirmed guests.
              </p>
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
                Easy import and export of guest lists. Track RSVPs in real-time.
              </p>
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
            </div>
             {/* More feature cards could be added here mirroring the original design intention if they had more data */}
          </div>
        </div>
      </div>
      <Footer />
    </main>
  );
}
