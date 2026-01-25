'use client';

import { ChevronRight, ArrowRight } from 'lucide-react';
import Link from 'next/link';

export function FeaturedFeatures() {
  return (
    <section className="max-w-7xl mx-auto px-4 mb-20 reveal">
      <div className="flex items-end justify-between mb-8">
        <div>
          <Link
            href="/features"
            className="text-2xl font-semibold tracking-tight text-white flex items-center gap-2 cursor-pointer hover:text-indigo-400 transition-colors"
          >
            Powerful Features
            <ChevronRight className="w-5 h-5 text-slate-500" />
          </Link>
          <p className="text-sm text-slate-400 mt-2 font-light">
            Everything you need to manage events successfully
          </p>
        </div>
        <Link
          href="/features"
          className="hover:text-white transition-colors flex items-center gap-1 text-sm text-slate-400"
        >
          View all <ArrowRight className="w-4 h-4" />
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Feature Card 1 */}
        <div
          className="glass-panel glass-panel-hover rounded-xl p-6 transition-all cursor-pointer reveal"
          style={{ transitionDelay: '100ms' }}
        >
          <div className="flex items-center gap-4 mb-6">
            <div className="flex text-lg font-bold text-slate-50 bg-slate-50 w-12 h-12 rounded-full items-center justify-center text-black">
              SR
            </div>
            <div>
              <h3 className="text-lg font-medium text-white">
                Smart Reminders
              </h3>
              <p className="text-xs text-slate-500">Automated scheduling</p>
            </div>
          </div>
          <div className="h-px bg-white/5 w-full mb-6"></div>
          <div className="flex items-end justify-between">
            <div>
              <p className="text-xs text-slate-500 mb-1">Efficiency</p>
              <p className="text-lg font-medium text-white">100%</p>
            </div>
            <div className="text-right">
              <p className="text-xs text-slate-500 mb-1">Impact</p>
              <p className="text-sm font-medium text-emerald-400">High</p>
            </div>
          </div>
        </div>

        {/* Feature Card 2 */}
        <div
          className="glass-panel glass-panel-hover rounded-xl p-6 transition-all cursor-pointer reveal"
          style={{ transitionDelay: '200ms' }}
        >
          <div className="flex items-center gap-4 mb-6">
            <div className="w-12 h-12 rounded-full bg-white text-black flex items-center justify-center font-bold">
              CP
            </div>
            <div>
              <h3 className="text-lg font-medium text-white">
                Collaborative Plans
              </h3>
              <p className="text-xs text-slate-500">Team coordination</p>
            </div>
          </div>
          <div className="h-px bg-white/5 w-full mb-6"></div>
          <div className="flex items-end justify-between">
            <div>
              <p className="text-xs text-slate-500 mb-1">Collaboration</p>
              <p className="text-lg font-medium text-white">Real-time</p>
            </div>
            <div className="text-right">
              <p className="text-xs text-slate-500 mb-1">Users</p>
              <p className="text-sm font-medium text-emerald-400">
                Unlimited
              </p>
            </div>
          </div>
        </div>

        {/* Feature Card 3 */}
        <div
          className="glass-panel glass-panel-hover rounded-xl p-6 transition-all cursor-pointer reveal"
          style={{ transitionDelay: '300ms' }}
        >
          <div className="flex items-center gap-4 mb-6">
            <div className="w-12 h-12 rounded-full bg-white flex items-center justify-center p-2">
              <svg viewBox="0 0 24 24" fill="#000">
                <circle cx="12" cy="12" r="10" />
              </svg>
            </div>
            <div>
              <h3 className="text-lg font-medium text-white">
                Instant Alerts
              </h3>
              <p className="text-xs text-slate-500">Never miss a beat</p>
            </div>
          </div>
          <div className="h-px bg-white/5 w-full mb-6"></div>
          <div className="flex items-end justify-between">
            <div>
              <p className="text-xs text-slate-500 mb-1">Speed</p>
              <p className="text-lg font-medium text-white">Instant</p>
            </div>
            <div className="text-right">
              <p className="text-xs text-slate-500 mb-1">Reliability</p>
              <p className="text-sm font-medium text-emerald-400">99.9%</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
