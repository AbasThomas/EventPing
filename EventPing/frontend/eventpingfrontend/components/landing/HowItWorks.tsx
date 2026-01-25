'use client';

import { UserPlus, CalendarPlus, Users, Bell } from 'lucide-react';

export function HowItWorks() {
  return (
    <section className="overflow-hidden bg-[#0c0d1c]/0 w-full mb-32 py-24 relative reveal">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,rgba(50,45,100,0.15)_0,rgba(15,23,42,0)_70%)] opacity-80 mix-blend-screen pointer-events-none"></div>

      <div className="z-10 max-w-7xl mx-auto px-4 relative">
        <div className="text-center mb-20 reveal">
          <h2 className="md:text-4xl text-3xl font-light text-white tracking-tight mb-4 drop-shadow-[0_0_12px_rgba(255,255,255,0.2)]">
            How It Works
          </h2>
          <p className="text-slate-400 font-light text-base md:text-lg">
            Plan your perfect event in 4 simple steps
          </p>
        </div>

        <div className="relative grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Dynamic Progress Connectors (Desktop Only) */}
          <div className="hidden md:block absolute top-[2.5rem] left-0 right-0 w-full h-1 z-0 px-[12%] pointer-events-none reveal">
            <div className="flex items-center w-full h-full justify-between">
              <div className="flex-1 h-px connector-dots text-cyan-500 mx-2"></div>
              <div className="w-16"></div>
              <div className="flex-1 h-px connector-dots text-purple-500 mx-2"></div>
              <div className="w-16"></div>
              <div className="flex-1 h-px connector-dots text-emerald-500 mx-2"></div>
            </div>
          </div>

          {/* Step 1 */}
          <div
            className="relative z-10 group flex flex-col items-center text-center p-6 rounded-2xl bg-slate-900/60 border border-cyan-500/10 backdrop-blur-md shadow-[0_8px_30px_-12px_rgba(6,182,212,0.15)] hover:border-cyan-500/30 hover:shadow-[0_12px_40px_-12px_rgba(6,182,212,0.25)] hover:-translate-y-1 transition-all duration-500 reveal"
            style={{ transitionDelay: '100ms' }}
          >
            <div className="absolute top-3 right-3 w-2 h-2 text-cyan-500/40 opacity-50">
              <svg
                viewBox="0 0 10 10"
                fill="currentColor"
                className="w-full h-full"
              >
                <path d="M5 0L10 10H0L5 0Z"></path>
              </svg>
            </div>
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-cyan-500/10 to-transparent border border-cyan-500/20 flex items-center justify-center mb-6 shadow-[inset_0_1px_1px_rgba(255,255,255,0.1)] group-hover:scale-110 transition-transform duration-500">
              <UserPlus className="w-7 h-7 text-cyan-300 drop-shadow-[0_2px_8px_rgba(34,211,238,0.5)]" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2 tracking-tight">
              Create Account
            </h3>
            <p className="text-sm text-cyan-200/50 font-normal leading-relaxed">
              Sign up in seconds
            </p>
          </div>

          {/* Step 2 */}
          <div
            className="relative z-10 group flex flex-col items-center text-center p-6 rounded-2xl bg-slate-900/60 border border-violet-500/10 backdrop-blur-md shadow-[0_8px_30px_-12px_rgba(139,92,246,0.15)] hover:border-violet-500/30 hover:shadow-[0_12px_40px_-12px_rgba(139,92,246,0.25)] hover:-translate-y-1 transition-all duration-500 reveal"
            style={{ transitionDelay: '200ms' }}
          >
            <div className="absolute top-3 right-3 w-1.5 h-1.5 rounded-full bg-violet-500/40 opacity-50"></div>
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-violet-500/10 to-transparent border border-violet-500/20 flex items-center justify-center mb-6 shadow-[inset_0_1px_1px_rgba(255,255,255,0.1)] group-hover:scale-110 transition-transform duration-500">
              <CalendarPlus className="w-7 h-7 text-violet-300 drop-shadow-[0_2px_8px_rgba(167,139,250,0.5)]" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2 tracking-tight">
              Create Event
            </h3>
            <p className="text-sm text-violet-200/50 font-normal leading-relaxed">
              Set date, time, and details
            </p>
          </div>

          {/* Step 3 */}
          <div
            className="relative z-10 group flex flex-col items-center text-center p-6 rounded-2xl bg-slate-900/60 border border-emerald-500/10 backdrop-blur-md shadow-[0_8px_30px_-12px_rgba(16,185,129,0.15)] hover:border-emerald-500/30 hover:shadow-[0_12px_40px_-12px_rgba(16,185,129,0.25)] hover:-translate-y-1 transition-all duration-500 reveal"
            style={{ transitionDelay: '300ms' }}
          >
            <div className="absolute top-3 right-3 w-1.5 h-1.5 bg-emerald-500/40 opacity-50 rotate-45"></div>
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-emerald-500/10 to-transparent border border-emerald-500/20 flex items-center justify-center mb-6 shadow-[inset_0_1px_1px_rgba(255,255,255,0.1)] group-hover:scale-110 transition-transform duration-500">
              <Users className="w-7 h-7 text-emerald-300 drop-shadow-[0_2px_8px_rgba(52,211,153,0.5)]" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2 tracking-tight">
              Invite Guests
            </h3>
            <p className="text-sm text-emerald-200/50 font-normal leading-relaxed">
              Share the link instantly
            </p>
          </div>

          {/* Step 4 */}
          <div
            className="relative z-10 group flex flex-col items-center text-center p-6 rounded-2xl bg-slate-900/60 border border-orange-500/10 backdrop-blur-md shadow-[0_8px_30px_-12px_rgba(249,115,22,0.15)] hover:border-orange-500/30 hover:shadow-[0_12px_40px_-12px_rgba(249,115,22,0.25)] hover:-translate-y-1 transition-all duration-500 reveal"
            style={{ transitionDelay: '400ms' }}
          >
            <div className="absolute top-3 right-3 w-1.5 h-1.5 bg-orange-500/40 opacity-50 rotate-45 border border-orange-400"></div>
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-orange-500/10 to-transparent border border-orange-500/20 flex items-center justify-center mb-6 shadow-[inset_0_1px_1px_rgba(255,255,255,0.1)] group-hover:scale-110 transition-transform duration-500">
              <Bell className="w-7 h-7 text-orange-300 drop-shadow-[0_2px_8px_rgba(251,146,60,0.5)]" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2 tracking-tight">
              Automated Reminders
            </h3>
            <p className="text-sm text-orange-200/50 font-normal leading-relaxed">
              We handle the nudges
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}

