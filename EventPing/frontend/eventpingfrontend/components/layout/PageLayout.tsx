'use client';

import { PageBackground } from '@/components/ui/PageBackground';
import { Navbar } from '@/components/landing/Navbar';
import { Footer } from '@/components/landing/Footer';

interface PageLayoutProps {
  children: React.ReactNode;
  showNavbar?: boolean;
  showFooter?: boolean;
}

export function PageLayout({ children, showNavbar = true, showFooter = true }: PageLayoutProps) {
  return (
    <main className="min-h-screen relative text-slate-200 antialiased selection:bg-indigo-500/30 selection:text-indigo-200 fade-in-page">
      {showNavbar && <Navbar />}

      <div className="relative z-10 pt-20 min-h-[calc(100vh-80px)]">
        {children}
      </div>
      
      {showFooter && <Footer />}
    </main>
  );
}
