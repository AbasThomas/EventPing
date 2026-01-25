'use client';

export function Newsletter() {
  return (
    <section className="max-w-3xl mx-auto px-4 mb-24 reveal">
      <div className="glass-panel rounded-2xl p-8 text-center relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-500/10 blur-[80px] rounded-full pointer-events-none"></div>
        <h3 className="text-xl font-light text-white mb-2 relative z-10">
          Stay updated with EventPing
        </h3>
        <p className="text-slate-400 text-sm mb-6 font-light relative z-10">
          Get the latest features and planning tips.
        </p>
        <form className="flex flex-col sm:flex-row gap-2 max-w-md mx-auto relative z-10">
          <input
            type="email"
            placeholder="Enter your email"
            className="flex-1 placeholder-slate-600 focus:outline-none focus:border-indigo-500 transition-colors text-sm text-white bg-slate-950/50 border-white/10 border rounded-lg pt-2 pr-4 pb-2 pl-4"
          />
          <button className="bg-indigo-600 hover:bg-indigo-500 text-white text-sm px-6 py-2 rounded-lg font-medium transition-colors shadow-lg shadow-indigo-900/20 cursor-pointer">
            Subscribe
          </button>
        </form>
      </div>
    </section>
  );
}
