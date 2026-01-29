import { Navbar } from '@/components/landing/Navbar';
import { Hero } from '@/components/landing/Hero';
import { KeyMetrics } from '@/components/landing/KeyMetrics';
import { HowItWorks } from '@/components/landing/HowItWorks';
import { FeaturedFeatures } from '@/components/landing/FeaturedFeatures';
import { DocsSection } from '@/components/landing/DocsSection';
import { Integrations } from '@/components/landing/Integrations';
import { Testimonials } from '@/components/landing/Testimonials';
import { Newsletter } from '@/components/landing/Newsletter';
import { Footer } from '@/components/landing/Footer';


export default function Home() {
  return (
    <main className="min-h-screen relative text-slate-200 antialiased selection:bg-indigo-500/30 selection:text-indigo-200 fade-in-page">
      <div className="fixed inset-0 -z-20 bg-slate-950"></div>
      <Navbar />
      



      <div className="relative z-10">
        <Hero />
        <KeyMetrics />
        <HowItWorks />
        <FeaturedFeatures />
        <Integrations />
        <DocsSection />
        <Testimonials />
        <Newsletter />
      </div>
      
      <Footer />
    </main>
  );
}
