'use client';

import { PageLayout } from '@/components/layout/PageLayout';
import { Book, Code, Terminal, FileText } from 'lucide-react';

export default function DocsPage() {
  return (
    <PageLayout>
      <div className="max-w-7xl mx-auto px-4 py-16 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row gap-12">
          {/* Sidebar */}
          <div className="w-full md:w-64 flex-shrink-0">
            <div className="sticky top-24">
              <h3 className="text-sm font-semibold text-white uppercase tracking-wider mb-4">Getting Started</h3>
              <ul className="space-y-3 mb-8">
                <li><a href="#" className="text-indigo-400 font-medium">Introduction</a></li>
                <li><a href="#" className="text-slate-400 hover:text-white transition-colors">Installation</a></li>
                <li><a href="#" className="text-slate-400 hover:text-white transition-colors">Configuration</a></li>
              </ul>
              
              <h3 className="text-sm font-semibold text-white uppercase tracking-wider mb-4">Core Concepts</h3>
              <ul className="space-y-3">
                <li><a href="#" className="text-slate-400 hover:text-white transition-colors">Events</a></li>
                <li><a href="#" className="text-slate-400 hover:text-white transition-colors">Participants</a></li>
                <li><a href="#" className="text-slate-400 hover:text-white transition-colors">Reminders</a></li>
                <li><a href="#" className="text-slate-400 hover:text-white transition-colors">API Reference</a></li>
              </ul>
            </div>
          </div>

          {/* Main Content */}
          <div className="flex-1">
            <h1 className="text-4xl font-bold text-white mb-6">Documentation</h1>
            <p className="text-xl text-slate-400 mb-12">
              Welcome to the EventPing documentation. Here you'll find everything you need to integrate and use our platform.
            </p>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-12">
              <div className="p-6 rounded-xl border border-white/10 bg-slate-900/50 hover:border-indigo-500/30 transition-colors">
                <Book className="h-8 w-8 text-indigo-400 mb-4" />
                <h3 className="text-lg font-semibold text-white mb-2">Quick Start Guide</h3>
                <p className="text-slate-400 mb-4">Learn the basics and set up your first event in minutes.</p>
                <a href="#" className="text-indigo-400 hover:text-indigo-300 font-medium">Read Guide →</a>
              </div>

              <div className="p-6 rounded-xl border border-white/10 bg-slate-900/50 hover:border-indigo-500/30 transition-colors">
                <Code className="h-8 w-8 text-indigo-400 mb-4" />
                <h3 className="text-lg font-semibold text-white mb-2">API Reference</h3>
                <p className="text-slate-400 mb-4">Detailed documentation for our REST API endpoints.</p>
                <a href="#" className="text-indigo-400 hover:text-indigo-300 font-medium">View API →</a>
              </div>
            </div>

            <div className="prose prose-invert max-w-none">
              <h2>Introduction</h2>
              <p>
                EventPing is a comprehensive event management platform designed to simplify the process of organizing, tracking, and communicating with event participants.
              </p>
              
              <h3>Key Features</h3>
              <ul>
                <li>Real-time event tracking</li>
                <li>Automated email and SMS reminders</li>
                <li>Participant management dashboard</li>
                <li>Secure authentication and data protection</li>
              </ul>

              <h3>Authentication</h3>
              <p>
                All API requests must be authenticated using a JWT token. You can obtain a token by logging in via the <code>/api/auth/login</code> endpoint.
              </p>
              
              <div className="bg-slate-900 p-4 rounded-lg border border-white/10 font-mono text-sm text-slate-300 overflow-x-auto">
                <span className="text-indigo-400">curl</span> -X POST https://api.eventping.com/auth/login \<br/>
                &nbsp;&nbsp;-H <span className="text-green-400">"Content-Type: application/json"</span> \<br/>
                &nbsp;&nbsp;-d <span className="text-green-400">'{"{"}"email":"user@example.com", "password":"password"{"}"}'</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </PageLayout>
  );
}
