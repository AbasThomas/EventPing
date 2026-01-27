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
