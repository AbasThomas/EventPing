'use client';

import { Book, Code, Terminal, FileText, ArrowRight } from 'lucide-react';
import Link from 'next/link';

export function DocsSection() {
  const steps = [
    {
      title: 'Quick Setup',
      icon: Terminal,
      description: 'Get your account ready and workspace configured in under 2 minutes.',
      link: '/docs#setup'
    },
    {
      title: 'API Integration',
      icon: Code,
      description: 'Seamlessly connect your existing tools with our robust REST API.',
      link: '/docs#api'
    },
    {
      title: 'Smart Scheduling',
      icon: FileText,
      description: 'Master the art of automated timing and participant engagement.',
      link: '/docs#scheduling'
    }
  ];

  return (
    <section className="max-w-7xl mx-auto px-4 mb-32 reveal">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
        <div>
          <h2 className="text-3xl md:text-4xl font-light text-white mb-6 leading-tight">
            Developer First <br />
            <span className="text-indigo-400 font-normal italic">Documentation</span>
          </h2>
          <p className="text-slate-400 text-lg font-light mb-8 max-w-lg">
            Whether you're a small team or an enterprise, our documentation provides the clear path to event management excellence.
          </p>
          
          <Link 
            href="/docs"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-full bg-white/5 border border-white/10 text-white hover:bg-white/10 transition-all group"
          >
            Explore Full Documentation
            <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </Link>
        </div>

        <div className="grid grid-cols-1 gap-4">
          {steps.map((step, index) => (
            <Link 
              key={step.title}
              href={step.link}
              className="glass-panel p-6 rounded-2xl border border-white/5 hover:border-indigo-500/30 transition-all group flex items-start gap-4"
              style={{ transitionDelay: `${index * 100}ms` }}
            >
              <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 group-hover:bg-indigo-500 group-hover:text-white transition-all">
                <step.icon className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-white font-medium text-lg mb-1">{step.title}</h3>
                <p className="text-slate-500 text-sm leading-relaxed">{step.description}</p>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
}
