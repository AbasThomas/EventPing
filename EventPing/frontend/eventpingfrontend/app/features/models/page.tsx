'use client';

import { Navbar } from '@/components/landing/Navbar';
import { Footer } from '@/components/landing/Footer';
import { Search } from 'lucide-react';

export default function Models() {
  return (
    <main className="min-h-screen relative bg-slate-950 text-slate-200 antialiased selection:bg-indigo-500/30 selection:text-indigo-200 fade-in-page">
      <Navbar />

       {/* Static Utility Background (Models Page) */}
       <div className="fixed inset-0 z-0 pointer-events-none bg-slate-950">
           <div className="absolute inset-0 bg-gradient-to-b from-slate-900/50 to-slate-950"></div>
       </div>

      <div className="z-10 pt-32 pb-20 relative min-h-screen">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          {/* Header */}
          <div className="mb-10 reveal-visible">
            <h1 className="text-3xl md:text-4xl font-light text-white tracking-tight mb-4">
              Models
            </h1>
            <p className="text-slate-400 font-light text-lg max-w-2xl">
              Browse, compare, and test over 300 models from the world's best AI labs.
            </p>
          </div>

          {/* Filters & Search */}
            <div className="flex flex-col md:flex-row gap-4 mb-8 reveal-visible">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                    <input type="text" placeholder="Search models..." className="w-full bg-slate-900/50 border border-white/10 rounded-lg pl-10 pr-4 py-2.5 text-sm text-white focus:outline-none focus:border-indigo-500 focus:bg-slate-900 transition-all placeholder:text-slate-600" />
                </div>
                <div className="flex gap-2 overflow-x-auto pb-2 md:pb-0">
                    <button className="px-4 py-2 rounded-lg bg-indigo-600 text-white text-xs font-medium border border-indigo-500 whitespace-nowrap">All Models</button>
                    <button className="px-4 py-2 rounded-lg bg-slate-900/50 text-slate-400 hover:text-white text-xs font-medium border border-white/10 hover:border-white/20 whitespace-nowrap transition-colors">Popular</button>
                    <button className="px-4 py-2 rounded-lg bg-slate-900/50 text-slate-400 hover:text-white text-xs font-medium border border-white/10 hover:border-white/20 whitespace-nowrap transition-colors">New</button>
                    <button className="px-4 py-2 rounded-lg bg-slate-900/50 text-slate-400 hover:text-white text-xs font-medium border border-white/10 hover:border-white/20 whitespace-nowrap transition-colors">Free</button>
                    <button className="px-4 py-2 rounded-lg bg-slate-900/50 text-slate-400 hover:text-white text-xs font-medium border border-white/10 hover:border-white/20 whitespace-nowrap transition-colors">Vision</button>
                </div>
            </div>

          {/* Models Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-20">
            {/* Model Card 1 */}
            <div
              className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible"
              style={{ transitionDelay: '50ms' }}
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-slate-50 text-black flex items-center justify-center font-bold text-sm">
                    AI
                  </div>
                  <div>
                    <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">
                      Claude 3.5 Sonnet
                    </h3>
                    <p className="text-xs text-slate-500">Anthropic</p>
                  </div>
                </div>
                <span className="px-2 py-1 rounded bg-indigo-500/10 text-indigo-300 text-[10px] font-medium border border-indigo-500/20">Popular</span>
              </div>
              <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">
                Anthropic's most intelligent model, delivering top-tier performance on highly complex tasks.
              </p>
               <div className="flex items-center justify-between border-t border-white/5 pt-4">
                    <div className="text-[10px] text-slate-500 font-mono">$3.00 / 1M</div>
                    <div className="text-[10px] text-slate-500">200k context</div>
               </div>
            </div>

            {/* Model Card 2 */}
            <div
              className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible"
              style={{ transitionDelay: '100ms' }}
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-white text-black flex items-center justify-center font-bold text-sm">
                    O
                  </div>
                  <div>
                    <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">
                      GPT-4o
                    </h3>
                    <p className="text-xs text-slate-500">OpenAI</p>
                  </div>
                </div>
              </div>
              <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">
                High-intelligence flagship model for complex, multi-step tasks. Multimodal capabilities.
              </p>
               <div className="flex items-center justify-between border-t border-white/5 pt-4">
                    <div className="text-[10px] text-slate-500 font-mono">$5.00 / 1M</div>
                    <div className="text-[10px] text-slate-500">128k context</div>
               </div>
            </div>

             {/* Model Card 3 */}
             <div className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible" style={{transitionDelay: "150ms"}}>
                <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-white text-black flex items-center justify-center font-bold text-sm p-2"><svg viewBox="0 0 24 24" fill="#000"><path d="M12 24c6.627 0 12-5.373 12-12S18.627 0 12 0 0 5.373 0 12s5.373 12 12 12z"></path></svg></div>
                        <div>
                            <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">Gemini 1.5 Pro</h3>
                            <p className="text-xs text-slate-500">Google</p>
                        </div>
                    </div>
                </div>
                <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">Mid-size multimodal model that scales across a wide range of tasks.</p>
                <div className="flex items-center justify-between border-t border-white/5 pt-4">
                     <div className="text-[10px] text-slate-500 font-mono">$3.50 / 1M</div>
                     <div className="text-[10px] text-slate-500">2M context</div>
                </div>
            </div>

             {/* Model Card 4 */}
             <div className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible" style={{transitionDelay: "200ms"}}>
                <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-orange-500 text-white flex items-center justify-center font-bold text-sm">M</div>
                        <div>
                            <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">Mistral Large</h3>
                            <p className="text-xs text-slate-500">Mistral AI</p>
                        </div>
                    </div>
                </div>
                <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">Top-tier reasoning capabilities. Fluent in English, French, Spanish, German, and Italian.</p>
                <div className="flex items-center justify-between border-t border-white/5 pt-4">
                     <div className="text-[10px] text-slate-500 font-mono">$4.00 / 1M</div>
                     <div className="text-[10px] text-slate-500">32k context</div>
                </div>
            </div>

            {/* Model Card 5 */}
            <div className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible" style={{transitionDelay: "250ms"}}>
                <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-blue-600 text-white flex items-center justify-center font-bold text-sm">L</div>
                        <div>
                            <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">Llama 3 70B</h3>
                            <p className="text-xs text-slate-500">Meta</p>
                        </div>
                    </div>
                    <span className="px-2 py-1 rounded bg-green-500/10 text-green-300 text-[10px] font-medium border border-green-500/20">Open</span>
                </div>
                <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">The most capable open-weights model available today, rivaling top proprietary models.</p>
                <div className="flex items-center justify-between border-t border-white/5 pt-4">
                     <div className="text-[10px] text-slate-500 font-mono">$0.70 / 1M</div>
                     <div className="text-[10px] text-slate-500">8k context</div>
                </div>
            </div>

            {/* Model Card 6 */}
            <div className="glass-panel glass-panel-hover rounded-xl p-5 cursor-pointer group reveal-visible" style={{transitionDelay: "300ms"}}>
                <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-indigo-600 text-white flex items-center justify-center font-bold text-sm">P</div>
                        <div>
                            <h3 className="font-medium text-white group-hover:text-indigo-400 transition-colors">Perplexity Online</h3>
                            <p className="text-xs text-slate-500">Perplexity</p>
                        </div>
                    </div>
                </div>
                <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">Online model with up-to-date knowledge from the internet.</p>
                <div className="flex items-center justify-between border-t border-white/5 pt-4">
                     <div className="text-[10px] text-slate-500 font-mono">$5.00 / 1M</div>
                     <div className="text-[10px] text-slate-500">N/A context</div>
                </div>
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </main>
  );
}
