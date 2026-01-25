'use client';

import { ChevronRight, ArrowRight, LayoutGrid } from 'lucide-react';
import Link from 'next/link';

export function FeaturedAgents() {
  return (
    <section className="max-w-7xl mx-auto px-4 mb-32 reveal">
      <div className="flex items-end justify-between mb-8">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight text-white flex items-center gap-2 cursor-pointer hover:text-indigo-400 transition-colors">
            Featured Agents
            <ChevronRight className="w-5 h-5 text-slate-500" />
          </h2>
          <p className="text-sm text-slate-400 mt-2 font-light">
            250k+ apps using OpenRouter with 4.2M+ users globally
          </p>
        </div>
        <Link
          href="/models"
          className="text-sm text-slate-400 hover:text-white transition-colors flex items-center gap-1"
        >
          View all <ArrowRight className="w-4 h-4" />
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Agent 1 */}
        <div
          className="glass-panel rounded-xl overflow-hidden group cursor-pointer border border-white/5 hover:border-white/10 transition-colors reveal"
          style={{ transitionDelay: '100ms' }}
        >
          <div className="h-48 bg-slate-900 relative flex items-center justify-center p-6 border-b border-white/5 group-hover:bg-slate-800/80 transition-colors">
            <div className="w-full bg-slate-950 rounded-lg p-4 shadow-xl border border-white/5 text-sm font-mono text-slate-400">
              <p>
                <span className="text-slate-500">Make me</span>{' '}
                <span className="text-indigo-300 border-b border-indigo-500/50">
                  a website
                </span>
              </p>
              <p className="mt-2">
                <span className="text-slate-500">for</span>{' '}
                <span className="text-indigo-300 border-b border-indigo-500/50">
                  local business customers
                </span>
              </p>
              <p className="mt-2 text-xs opacity-50 truncate">
                that helps subscribe to exclusive content...
              </p>
            </div>
          </div>
          <div className="p-5 flex items-start gap-4 bg-slate-950/30">
            <div className="w-10 h-10 rounded-lg bg-slate-900 border border-white/10 flex items-center justify-center shrink-0">
              <div className="grid grid-cols-2 gap-0.5">
                <div className="w-1.5 h-1.5 bg-orange-500"></div>
                <div className="w-1.5 h-1.5 bg-orange-500"></div>
                <div className="w-1.5 h-1.5 bg-orange-500"></div>
                <div className="w-1.5 h-1.5 opacity-0"></div>
              </div>
            </div>
            <div>
              <h3 className="text-base font-medium text-white mb-1">Replit</h3>
              <p className="text-xs text-slate-500 font-light leading-relaxed">
                The easiest way to go from idea to app
              </p>
            </div>
          </div>
        </div>

        {/* Agent 2 */}
        <div
          className="glass-panel rounded-xl overflow-hidden group cursor-pointer border border-white/5 hover:border-white/10 transition-colors reveal"
          style={{ transitionDelay: '200ms' }}
        >
          <div className="h-48 bg-black relative flex flex-col items-center justify-center p-6 border-b border-white/5 group-hover:bg-slate-950 transition-colors">
            <div className="text-xs text-slate-400 font-medium tracking-widest mb-4 flex items-center gap-2">
              <div className="w-3 h-3 border border-white/40 rotate-45"></div>{' '}
              BLACKBOX AI
            </div>
            <div className="w-full bg-slate-900/50 rounded-lg p-3 border border-white/10 text-[10px] text-slate-500">
              <p className="mb-4">Describe what you want the AI agent to do...</p>
              <div className="flex gap-2">
                <span className="px-1.5 py-0.5 rounded bg-white/5 border border-white/5">
                  Multi-Agent
                </span>
                <span className="px-1.5 py-0.5 rounded bg-white/5 border border-white/5">
                  Browser
                </span>
              </div>
            </div>
          </div>
          <div className="p-5 flex items-start gap-4 bg-slate-950/30">
            <div className="w-10 h-10 rounded-lg bg-black border border-white/10 flex items-center justify-center shrink-0">
              <div className="w-4 h-4 border-2 border-white rounded-sm rotate-45"></div>
            </div>
            <div>
              <h3 className="text-base font-medium text-white mb-1">
                BLACKBOXAI
              </h3>
              <p className="text-xs text-slate-500 font-light leading-relaxed">
                AI agent for builders
              </p>
            </div>
          </div>
        </div>

        {/* Agent 3 */}
        <div
          className="glass-panel rounded-xl overflow-hidden group cursor-pointer border border-white/5 hover:border-white/10 transition-colors reveal"
          style={{ transitionDelay: '300ms' }}
        >
          <div className="h-48 bg-[#111] relative flex items-center justify-center p-6 border-b border-white/5 group-hover:bg-black transition-colors">
            <div className="border border-yellow-400/30 bg-yellow-400/5 px-4 py-2 rounded flex items-center gap-2">
              <LayoutGrid className="w-4 h-4 text-yellow-400" />
              <span className="text-yellow-400 font-mono text-sm">
                Kilo Code
              </span>
            </div>
          </div>
          <div className="p-5 flex items-start gap-4 bg-slate-950/30">
            <div className="w-10 h-10 rounded-lg bg-white flex items-center justify-center shrink-0">
              <LayoutGrid className="w-5 h-5 text-black" />
            </div>
            <div>
              <h3 className="text-base font-medium text-white mb-1">
                Kilo Code
              </h3>
              <p className="text-xs text-slate-500 font-light leading-relaxed">
                Everything you need for agentic development
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
