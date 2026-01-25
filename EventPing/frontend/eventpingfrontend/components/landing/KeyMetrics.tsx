'use client';

export function KeyMetrics() {
  return (
    <section className="max-w-7xl mx-auto px-4 mb-32 reveal">
      <div className="grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-12 border-y bg-slate-900/20 border-white/5 pt-12 pb-12 gap-x-8 gap-y-8">
        <div className="text-center group reveal" style={{ transitionDelay: '0ms' }}>
          <h3 className="text-3xl font-light text-white mb-1 group-hover:text-indigo-400 transition-colors">
            10k+
          </h3>
          <p className="uppercase text-sm font-light text-slate-500">
            Events Created
          </p>
        </div>
        <div
          className="group text-center reveal"
          style={{ transitionDelay: '100ms' }}
        >
          <h3 className="text-3xl font-light text-white mb-1 group-hover:text-indigo-400 transition-colors">
            50k+
          </h3>
          <p className="uppercase text-sm font-light text-slate-500">
            Reminders Sent
          </p>
        </div>
        <div className="text-center group reveal" style={{ transitionDelay: '200ms' }}>
          <h3 className="text-3xl font-light text-white mb-1 group-hover:text-indigo-400 transition-colors">
            99.9%
          </h3>
          <p className="uppercase text-sm font-light text-slate-500">
            Uptime
          </p>
        </div>
        <div className="text-center group reveal" style={{ transitionDelay: '300ms' }}>
          <h3 className="text-3xl font-light text-white mb-1 group-hover:text-indigo-400 transition-colors">
            5k+
          </h3>
          <p className="uppercase text-sm font-light text-slate-500">
            Active Users
          </p>
        </div>
      </div>
    </section>
  );
}
