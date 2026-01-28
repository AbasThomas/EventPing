import { Navbar } from '@/components/landing/Navbar';
import { Hero } from '@/components/landing/Hero';
import { KeyMetrics } from '@/components/landing/KeyMetrics';
import { HowItWorks } from '@/components/landing/HowItWorks';
import { FeaturedFeatures } from '@/components/landing/FeaturedFeatures';
import { DocsSection } from '@/components/landing/DocsSection';
import { Testimonials } from '@/components/landing/Testimonials';
import { Newsletter } from '@/components/landing/Newsletter';
import { Footer } from '@/components/landing/Footer';


export default function Home() {
  return (
    <main className="min-h-screen relative text-slate-200 antialiased selection:bg-indigo-500/30 selection:text-indigo-200 fade-in-page">
      <div className="fixed inset-0 -z-20 bg-slate-950"></div>
      <div className="fixed inset-0 -z-10 bg-[radial-gradient(circle_at_50%_0%,rgba(99,102,241,0.15),transparent_50%)]"></div>
      <Navbar />
      



      <div className="relative z-10">
        <Hero />
        <KeyMetrics />
        <HowItWorks />
        <FeaturedFeatures />
        <DocsSection />
        <Testimonials />
        <Newsletter />
      </div>
      
      <Footer />
    </main>
  );
}
