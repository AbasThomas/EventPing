'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';

export function Navbar() {
  const router = useRouter();
  const pathname = usePathname();
  const [scrolled, setScrolled] = useState(false);
  const { user, logout } = useAuth();

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
          <div className="w-6 h-6 rounded-lg bg-indigo-600 flex items-center justify-center">
            <div className="w-2 h-2 bg-white rounded-full"></div>
          </div>
          <span className="font-medium tracking-tight text-white">EventPing</span>
        </div>

        {/* Desktop Nav Items */}
        <div className="hidden xl:flex items-center gap-6 text-sm text-slate-400 font-light">
          <Link
            href="/features"
            className={`transition-colors hover:text-white ${
              pathname === '/features' ? 'text-white' : ''
            }`}
          >
            Features
          </Link>
          <Link
            href="#integrations"
            className="transition-colors hover:text-white"
          >
            Integrations
          </Link>
          <Link
            href="/plans"
            className={`transition-colors hover:text-white ${
              pathname === '/plans' ? 'text-white' : ''
            }`}
          >
            Plans
          </Link>
          <Link
            href="/docs"
            className={`transition-colors hover:text-white ${
              pathname === '/docs' ? 'text-white' : ''
            }`}
          >
            Docs
          </Link>
          <Link
            href="/support"
            className={`transition-colors hover:text-white ${
              pathname === '/support' ? 'text-white' : ''
            }`}
          >
            Support
          </Link>
          <Link
            href="/about"
            className={`transition-colors hover:text-white ${
              pathname === '/about' ? 'text-white' : ''
            }`}
          >
            About
          </Link>
        </div>

        <div className="flex items-center gap-4">
          {user ? (
            <>
              <Link href="/dashboard" className="text-sm font-light text-slate-300 hover:text-white transition-colors">
                Dashboard
              </Link>
              <Link href="/profile" className="text-sm font-light text-slate-300 hover:text-white transition-colors">
                Profile
              </Link>
              <button 
                onClick={logout}
                className="bg-red-600 hover:bg-red-500 text-white text-xs px-4 py-2 rounded-full font-medium transition-all cursor-pointer"
              >
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link href="/auth/login" className="text-sm font-light text-slate-300 hover:text-white transition-colors">
                Sign in
              </Link>
              <Link href="/auth/register" className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs px-4 py-2 rounded-full font-medium transition-all cursor-pointer">
                Sign up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
