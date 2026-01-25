'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';

export function Navbar() {
  const router = useRouter();
  const pathname = usePathname();
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navigateTo = (path: string) => {
    router.push(path);
  };

  const isFeatures = pathname === '/features';

  return (
    <nav
      className={`fixed top-0 w-full z-50 border-b transition-all duration-300 ${
        scrolled
          ? 'border-white/5 bg-slate-950/70 backdrop-blur-xl'
          : 'border-transparent bg-transparent'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 md:px-6 h-16 flex items-center justify-between">
        <div
          className="flex items-center gap-2 cursor-pointer"
          onClick={() => navigateTo('/')}
        >
          {/* Logo */}
          <div className="w-6 h-6 rounded-full border border-indigo-500 flex items-center justify-center shadow-[0_0_10px_rgba(99,102,241,0.5)]">
            <div className="w-2 h-2 bg-indigo-500 rounded-full"></div>
          </div>
          <span className="font-medium tracking-tight text-white">OpenRouter</span>
        </div>

        {/* Desktop Nav Items */}
        <div className="hidden xl:flex items-center gap-6 text-sm text-slate-400 font-light">
          <Link
            href="/models"
            className={`transition-colors hover:text-white ${
              pathname === '/models' ? 'text-white' : ''
            }`}
          >
            Models
          </Link>
          <a href="#" className="hover:text-white transition-colors">
            Status
          </a>
          <a href="#" className="hover:text-white transition-colors">
            Announcements
          </a>
          <a href="#" className="hover:text-white transition-colors">
            Docs
          </a>
          <a href="#" className="hover:text-white transition-colors">
            Support
          </a>
          <a href="#" className="hover:text-white transition-colors">
            About
          </a>
          <a href="#" className="hover:text-white transition-colors">
            Partners
          </a>
          <a href="#" className="hover:text-white transition-colors">
            Enterprise
          </a>
          <a href="#" className="hover:text-white transition-colors">
            Pricing
          </a>
        </div>

        <div className="flex items-center gap-4">
          <button className="text-sm font-light text-slate-300 hover:text-white transition-colors">
            Sign in
          </button>
          <button className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs px-4 py-2 rounded-full font-medium transition-all shadow-[0_0_15px_-3px_rgba(99,102,241,0.6)] cursor-pointer">
            Sign up
          </button>
        </div>
      </div>
    </nav>
  );
}
