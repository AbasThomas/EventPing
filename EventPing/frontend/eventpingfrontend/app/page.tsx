import { Navbar } from '@/components/landing/Navbar';
import { Hero } from '@/components/landing/Hero';
import { KeyMetrics } from '@/components/landing/KeyMetrics';
import { HowItWorks } from '@/components/landing/HowItWorks';
import { FeaturedFeatures } from '@/components/landing/FeaturedFeatures';
import { Testimonials } from '@/components/landing/Testimonials';
import { Newsletter } from '@/components/landing/Newsletter';
import { Footer } from '@/components/landing/Footer';

export default function Home() {
  return (
    <main className="min-h-screen relative overflow-hidden bg-slate-950 text-slate-200 antialiased selection:bg-indigo-500/30 selection:text-indigo-200 fade-in-page">
      <Navbar />
      
      {/* Marketing Backgrounds */}
      <div className="fixed inset-0 z-0 pointer-events-none">
        <div className="absolute top-0 w-full h-screen" style={{ maskImage: 'linear-gradient(to bottom, transparent, black 0%, black 80%, transparent)' }}>
            <div className="absolute w-full top-0 saturate-150 h-[800px]" style={{ maskImage: 'linear-gradient(transparent, black 0%, black 80%, transparent)' }}>
                <div className="fixed top-0 left-0 right-0 h-[800px] hero-glow pointer-events-none z-0"></div>
                <div className="fixed top-[-200px] left-1/2 -translate-x-1/2 w-[600px] h-[400px] bg-indigo-600/30 blur-[120px] rounded-full pointer-events-none z-0 animate-pulse"></div>
            </div>
        </div>
      </div>

      <div className="relative z-10">
        <Hero />
        <KeyMetrics />
        <HowItWorks />
        <FeaturedFeatures />
        <Testimonials />
        <Newsletter />
      </div>
      
      <Footer />
    </main>
  );
}
