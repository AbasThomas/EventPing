'use client';

import { ChevronRight, ArrowRight, User } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

export function Testimonials() {
  const router = useRouter();

  return (
    <section className="max-w-7xl mx-auto px-4 mb-32 reveal">
      <div className="flex items-end justify-between mb-8">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight text-white flex items-center gap-2 cursor-pointer hover:text-indigo-400 transition-colors">
            Trusted by Planners
            <ChevronRight className="w-5 h-5 text-slate-500" />
          </h2>
          <p className="text-sm text-slate-400 mt-2 font-light">
            Join thousands of satisfied event organizers
          </p>
        </div>
        <Link
          href="/features"
          className="text-sm text-slate-400 hover:text-white transition-colors flex items-center gap-1"
        >
          View all <ArrowRight className="w-4 h-4" />
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Testimonial 1 */}
        <div
          className="glass-panel rounded-xl overflow-hidden group cursor-pointer border border-white/5 hover:border-white/10 transition-colors reveal"
          style={{ transitionDelay: '100ms' }}
        >
          <div className="h-48 bg-slate-900 relative flex items-center justify-center p-6 border-b border-white/5 group-hover:bg-slate-800/80 transition-colors">
            <div className="w-full bg-slate-950 rounded-lg p-4 shadow-xl border border-white/5 text-sm font-mono text-slate-400">
              <p>
                <span className="text-slate-500">"EventPing saved</span>{' '}
                <span className="text-indigo-300 border-b border-indigo-500/50">
                  our conference"
                </span>
              </p>
              <p className="mt-2">
                <span className="text-slate-500">No more</span>{' '}
                <span className="text-indigo-300 border-b border-indigo-500/50">
                  missed deadlines.
                </span>
              </p>
            </div>
          </div>
          <div className="p-5 flex items-start gap-4 bg-slate-950/30">
            <div className="w-10 h-10 rounded-lg bg-slate-900 border border-white/10 flex items-center justify-center shrink-0">
              <div className="grid grid-cols-2 gap-0.5">
                <div className="w-1.5 h-1.5 bg-indigo-500"></div>
                <div className="w-1.5 h-1.5 bg-indigo-500"></div>
                <div className="w-1.5 h-1.5 bg-indigo-500"></div>
                <div className="w-1.5 h-1.5 opacity-0"></div>
              </div>
            </div>
            <div>
              <h3 className="text-base font-medium text-white mb-1">
                Sarah Jenkins
              </h3>
              <p className="text-xs text-slate-500 font-light leading-relaxed">
                Event Coordinator
              </p>
            </div>
          </div>
        </div>

        {/* Testimonial 2 */}
        <div
          className="glass-panel rounded-xl overflow-hidden group cursor-pointer border border-white/5 hover:border-white/10 transition-colors reveal"
          style={{ transitionDelay: '200ms' }}
        >
          <div className="h-48 bg-black relative flex flex-col items-center justify-center p-6 border-b border-white/5 group-hover:bg-slate-950 transition-colors">
            <div className="w-full bg-slate-900/50 rounded-lg p-3 border border-white/10 text-[10px] text-slate-500">
              <p className="mb-4">
                "The automated reminders are a game changer regarding
                attendance."
              </p>
            </div>
          </div>
          <div className="p-5 flex items-start gap-4 bg-slate-950/30">
            <div className="w-10 h-10 rounded-lg bg-black border border-white/10 flex items-center justify-center shrink-0">
              <div className="w-4 h-4 border-2 border-white rounded-sm rotate-45"></div>
            </div>
            <div>
              <h3 className="text-base font-medium text-white mb-1">
                Mike Ross
              </h3>
              <p className="text-xs text-slate-500 font-light leading-relaxed">
                Community Manager
              </p>
            </div>
          </div>
        </div>

        {/* Testimonial 3 */}
        <div
          className="glass-panel rounded-xl overflow-hidden group cursor-pointer border border-white/5 hover:border-white/10 transition-colors reveal"
          style={{ transitionDelay: '300ms' }}
        >
          <div className="h-48 bg-[#111] relative flex items-center justify-center p-6 border-b border-white/5 group-hover:bg-black transition-colors">
            <div className="border border-yellow-400/30 bg-yellow-400/5 px-4 py-2 rounded flex items-center gap-2">
              <span className="text-yellow-400 font-mono text-sm">★★★★★</span>
            </div>
          </div>
          <div className="p-5 flex items-start gap-4 bg-slate-950/30">
            <div className="w-10 h-10 rounded-lg bg-white flex items-center justify-center shrink-0">
              <User className="w-5 h-5 text-black" />
            </div>
            <div>
              <h3 className="text-base font-medium text-white mb-1">
                Emily Tao
              </h3>
              <p className="text-xs text-slate-500 font-light leading-relaxed">
                Freelance Planner
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
