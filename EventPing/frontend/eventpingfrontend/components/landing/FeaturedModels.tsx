'use client';

import { ChevronRight, ArrowRight } from 'lucide-react';
import Link from 'next/link';

export function FeaturedModels() {
  return (
    <section className="max-w-7xl mx-auto px-4 mb-20 reveal">
      <div className="flex items-end justify-between mb-8">
        <div>
          <Link
            href="/models"
            className="text-2xl font-semibold tracking-tight text-white flex items-center gap-2 cursor-pointer hover:text-indigo-400 transition-colors"
          >
            Featured Models
            <ChevronRight className="w-5 h-5 text-slate-500" />
          </Link>
          <p className="text-sm text-slate-400 mt-2 font-light">
            300+ active models on 60+ providers
          </p>
        </div>
        <Link
          href="/models"
          className="hover:text-white transition-colors flex items-center gap-1 text-sm text-slate-400"
        >
          View all <ArrowRight className="w-4 h-4" />
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Card 1 */}
        <div
          className="glass-panel glass-panel-hover rounded-xl p-6 transition-all cursor-pointer reveal"
          style={{ transitionDelay: '100ms' }}
        >
          <div className="flex items-center gap-4 mb-6">
            <div
              className="flex text-lg font-bold text-slate-50 bg-slate-50 w-12 h-12 rounded-full items-center justify-center text-black"
            >
              AI
            </div>
            <div>
              <h3 className="text-lg font-medium text-white">Claude Opus 4.5</h3>
              <p className="text-xs text-slate-500">by anthropic</p>
            </div>
          </div>
          <div className="h-px bg-white/5 w-full mb-6"></div>
          <div className="flex items-end justify-between">
            <div>
              <p className="text-xs text-slate-500 mb-1">Tokens</p>
              <p className="text-lg font-medium text-white">366.9B</p>
            </div>
            <div className="text-right">
              <p className="text-xs text-slate-500 mb-1">Weekly Trend</p>
              <p className="text-sm font-medium text-emerald-400">+92.74%</p>
            </div>
          </div>
        </div>

        {/* Card 2 */}
        <div
          className="glass-panel glass-panel-hover rounded-xl p-6 transition-all cursor-pointer reveal"
          style={{ transitionDelay: '200ms' }}
        >
          <div className="flex items-center gap-4 mb-6">
            <div className="w-12 h-12 rounded-full bg-white text-black flex items-center justify-center font-bold">
              O
            </div>
            <div>
              <h3 className="text-lg font-medium text-white">GPT-5.2</h3>
              <p className="text-xs text-slate-500">by openai</p>
            </div>
          </div>
          <div className="h-px bg-white/5 w-full mb-6"></div>
          <div className="flex items-end justify-between">
            <div>
              <p className="text-xs text-slate-500 mb-1">Tokens</p>
              <p className="text-lg font-medium text-white">95.9B</p>
            </div>
            <div className="text-right">
              <p className="text-xs text-slate-500 mb-1">Weekly Trend</p>
              <p className="text-sm font-medium text-emerald-400">+17.49%</p>
            </div>
          </div>
        </div>

        {/* Card 3 */}
        <div
          className="glass-panel glass-panel-hover rounded-xl p-6 transition-all cursor-pointer reveal"
          style={{ transitionDelay: '300ms' }}
        >
          <div className="flex items-center gap-4 mb-6">
            <div className="w-12 h-12 rounded-full bg-white flex items-center justify-center p-2">
              <svg viewBox="0 0 24 24" fill="#000">
                <path d="M12 24c6.627 0 12-5.373 12-12S18.627 0 12 0 0 5.373 0 12s5.373 12 12 12z"></path>
              </svg>
            </div>
            <div>
              <h3 className="text-lg font-medium text-white">Gemini 3 Pro</h3>
              <p className="text-xs text-slate-500">by google</p>
            </div>
          </div>
          <div className="h-px bg-white/5 w-full mb-6"></div>
          <div className="flex items-end justify-between">
            <div>
              <p className="text-xs text-slate-500 mb-1">Tokens</p>
              <p className="text-lg font-medium text-white">132.5B</p>
            </div>
            <div className="text-right">
              <p className="text-xs text-slate-500 mb-1">Weekly Trend</p>
              <p className="text-sm font-medium text-emerald-400">+12.09%</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
