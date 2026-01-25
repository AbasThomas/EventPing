'use client';

import { ArrowRight } from 'lucide-react';
import Link from 'next/link';
import { ShinyButton } from '../ui/shiny-button';

export function Hero() {
  return (
    <section className="text-center max-w-5xl mx-auto px-4 mb-24 pt-32">
      <div className="reveal" style={{ transitionDelay: '200ms' }}>
        <a
          href="#"
          className="inline-flex items-center gap-2 hover:bg-indigo-500/20 transition-colors text-xs text-indigo-300 bg-indigo-500/10 border-indigo-500/30 border rounded-full mb-8 pt-1 pr-3 pb-1 pl-3"
        >
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-indigo-500"></span>
          </span>
          API Studio is now in beta
          <ArrowRight className="w-3 h-3" />
        </a>
      </div>

      <div className="reveal" style={{ transitionDelay: '300ms' }}>
        <h1 className="md:text-6xl leading-[1.1] text-4xl text-white tracking-tight font-extralight mb-6">
          The Unified Interface <br />
          <span className="bg-clip-text font-light text-transparent bg-gradient-to-br from-[#d0d7fb] to-[#716afb]">
            For LLMs
          </span>
        </h1>

        <p className="text-lg md:text-xl text-slate-400 font-light tracking-wide mb-10 max-w-2xl mx-auto">
          Better prices, better uptime, no subscriptions.
        </p>
      </div>

      <div
        className="flex flex-col sm:flex-row gap-4 gap-x-4 gap-y-4 items-center justify-center reveal"
        style={{ transitionDelay: '500ms' }}
      >
        <ShinyButton>Get API Key</ShinyButton>
        <Link
          href="/models"
          className="glass-panel hover:bg-white/5 transition-all flex sm:w-auto text-lg font-medium bg-[#060a21]/0 w-full border-0 rounded-full pt-3.5 pr-6 pb-3.5 pl-6 gap-x-2 gap-y-2 items-center justify-center text-white"
        >
          Explore Models
        </Link>
      </div>
    </section>
  );
}
