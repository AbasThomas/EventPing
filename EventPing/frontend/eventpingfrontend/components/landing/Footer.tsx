'use client';

import { Twitter, Github, Linkedin, MessageSquare } from 'lucide-react';
import Link from 'next/link';

export function Footer() {
  const navigateToHome = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    window.location.href = '/';
  };

  return (
    <footer className="border-t border-white/5 bg-slate-950 pt-16 pb-12 relative z-10 reveal">
      <div className="max-w-7xl mx-auto px-4 md:px-6">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-12">
          <div className="col-span-2 md:col-span-1">
            <div
              className="flex items-center gap-2 mb-4 cursor-pointer"
              onClick={navigateToHome}
            >
              <div className="w-5 h-5 rounded-full border border-indigo-500 flex items-center justify-center">
                <div className="w-1.5 h-1.5 bg-indigo-500 rounded-full"></div>
              </div>
              <span className="font-medium tracking-tight text-white">
                OpenRouter
              </span>
            </div>
            <div className="flex gap-4 mt-6">
              <a
                href="#"
                className="text-slate-500 hover:text-indigo-400 transition-colors"
                aria-label="Twitter"
              >
                <Twitter className="w-5 h-5" />
              </a>
              <a
                href="#"
                className="text-slate-500 hover:text-indigo-400 transition-colors"
                aria-label="GitHub"
              >
                <Github className="w-5 h-5" />
              </a>
              <a
                href="#"
                className="text-slate-500 hover:text-indigo-400 transition-colors"
                aria-label="LinkedIn"
              >
                <Linkedin className="w-5 h-5" />
              </a>
              <a
                href="#"
                className="text-slate-500 hover:text-indigo-400 transition-colors"
                aria-label="Message"
              >
                <MessageSquare className="w-5 h-5" />
              </a>
            </div>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-white mb-4">Product</h4>
            <ul className="space-y-3 text-sm font-light text-slate-400">
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Pricing
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Enterprise
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Status
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Partners
                </a>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-white mb-4">Resources</h4>
            <ul className="space-y-3 text-sm font-light text-slate-400">
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Docs
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Announcements
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Support
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  About
                </a>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-white mb-4">Legal</h4>
            <ul className="space-y-3 text-sm font-light text-slate-400">
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Privacy
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Terms
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-indigo-400 transition-colors">
                  Careers
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-white/5 pt-8 flex flex-col md:flex-row justify-between items-center text-xs text-slate-600 font-light">
          <p>© 2025 OpenRouter.ai. All rights reserved.</p>
          <div className="flex gap-6 mt-4 md:mt-0">
            <span className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-green-500"></span>
              All systems operational
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
}
