'use client';

import { PageLayout } from '@/components/layout/PageLayout';
import Link from 'next/link';

export default function AboutPage() {
  return (
    <PageLayout>
      <div className="max-w-7xl mx-auto px-4 py-16 sm:px-6 lg:px-8">
        <div className="max-w-3xl mx-auto text-center mb-16">
          <h1 className="text-4xl font-bold text-white mb-6">Making Events Simple & Meaningful</h1>
          <p className="text-xl text-slate-400">
            We believe that organizing an event shouldn't be stressful. EventPing was built to bring people together with ease.
          </p>
        </div>

        <div className="relative rounded-3xl overflow-hidden bg-slate-900 border border-white/10 mb-20">
          <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/10 to-purple-500/10"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-12 p-12 relative z-10 items-center">
            <div>
              <h2 className="text-2xl font-bold text-white mb-4">Our Mission</h2>
              <p className="text-slate-300 leading-relaxed mb-6">
                EventPing started with a simple idea: event management tools are often too complex or too expensive for most people. We wanted to create a platform that is powerful enough for professionals but simple enough for everyone.
              </p>
              <p className="text-slate-300 leading-relaxed">
                Whether you're organizing a small meetup, a company workshop, or a large conference, our goal is to handle the logistics so you can focus on the experience.
              </p>
            </div>
            <div className="h-64 md:h-full bg-slate-800 rounded-xl overflow-hidden border border-white/5 flex items-center justify-center">
               <span className="text-slate-600 font-mono text-sm">[Team Photo Placeholder]</span>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-20">
          <div className="text-center">
            <div className="text-4xl font-bold text-indigo-400 mb-2">10k+</div>
            <div className="text-slate-400">Events Created</div>
          </div>
          <div className="text-center">
            <div className="text-4xl font-bold text-indigo-400 mb-2">500k+</div>
            <div className="text-slate-400">Participants</div>
          </div>
          <div className="text-center">
            <div className="text-4xl font-bold text-indigo-400 mb-2">99.9%</div>
            <div className="text-slate-400">Uptime</div>
          </div>
        </div>

        <div className="text-center">
          <h2 className="text-2xl font-bold text-white mb-6">Ready to get started?</h2>
          <Link
            href="/auth/register"
            className="inline-flex items-center justify-center bg-indigo-600 hover:bg-indigo-500 text-white font-medium py-3 px-8 rounded-full transition-all shadow-lg hover:shadow-indigo-500/25"
          >
            Create Your First Event
          </Link>
        </div>
      </div>
    </PageLayout>
  );
}
