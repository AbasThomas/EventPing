'use client';

import { useEffect, useState, use } from 'react';
import { apiFetch } from '@/lib/api';
import Link from 'next/link';
import { Calendar, Clock, MapPin, User, ArrowRight, Share2, Copy, Check, Loader2 } from 'lucide-react';
import { PageLayout } from '@/components/layout/PageLayout'; // Ensure this is imported or PageBackground
import { ShinyButton } from '@/components/ui/shiny-button';

interface Event {
  id: number;
  title: string;
  description: string;
  eventDateTime: string;
  status: string;
  slug: string;
  creatorEmail: string;
  location?: string; // Hypothetically added if available in API, otherwise ignore
}

export default function PublicEventPage({ params }: { params: Promise<{ slug: string }> }) {
  const [event, setEvent] = useState<Event | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [copied, setCopied] = useState(false);
  
  // Unwrap params using React.use()
  const resolvedParams = use(params);

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        // Public endpoint
        const data = await apiFetch(`/events/${resolvedParams.slug}`);
         if (!data || !data.id) {
             setError(true);
         } else {
             setEvent(data);
         }
      } catch (err) {
        console.error('Failed to fetch event', err);
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    fetchEvent();
  }, [resolvedParams.slug]);

  const copyLink = () => {
    navigator.clipboard.writeText(window.location.href);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (loading) {
     return (
       <div className="flex justify-center items-center min-h-screen bg-[#0f172a]">
          <div className="relative">
              <div className="absolute inset-0 bg-indigo-500/20 blur-xl rounded-full"></div>
              <Loader2 className="w-10 h-10 text-indigo-400 animate-spin relative z-10" />
          </div>
       </div>
     );
  }

  if (error || !event) {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen p-4 text-center bg-[#0f172a]">
            {/* Background elements would be here if using PageLayout or similar */}
             <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-indigo-500/10 blur-[120px] rounded-full"></div>
            </div>
            
            <div className="w-20 h-20 bg-slate-800/50 backdrop-blur-md border border-white/10 rounded-full flex items-center justify-center mb-6 text-slate-500 shadow-xl">
                <Calendar className="w-10 h-10" />
            </div>
            <h1 className="text-4xl font-bold text-white mb-3">Event Not Found</h1>
            <p className="text-slate-400 mb-8 max-w-md mx-auto text-lg">The event you are looking for does not exist or has been removed.</p>
            <Link href="/">
                 <ShinyButton className="min-w-[150px]">
                    Go Home
                 </ShinyButton>
            </Link>
        </div>
    );
  }

  const eventDate = new Date(event.eventDateTime);

  return (
    <PageLayout showNavbar={false} showFooter={false}> 
        {/* Using PageLayout for background, disabling nav/footer for a focused landing page feel? 
            Or keep them. The original code had a custom header. 
            User said "redesign the public page to properly". Proper usually implies full layout + nice design.
            I'll use PageLayout but maybe custom header. 
            Actually PageLayout puts content in container.
            Let's keep the custom header inside PageLayout.
        */}
        
        <div className="container max-w-5xl mx-auto px-4 py-8 md:py-16 relative z-10">
            <div className="absolute top-6 left-6 z-20">
                 <Link href="/" className="flex items-center gap-2 group">
                    <div className="w-8 h-8 rounded-full border border-indigo-500/50 flex items-center justify-center shadow-[0_0_15px_rgba(99,102,241,0.3)] group-hover:shadow-[0_0_20px_rgba(99,102,241,0.5)] transition-shadow">
                        <div className="w-2.5 h-2.5 bg-indigo-500 rounded-full"></div>
                    </div>
                    <span className="font-bold tracking-tight text-white text-lg">EventPing</span>
                 </Link>
            </div>

            <div className="glass-panel p-8 md:p-12 rounded-[2.5rem] border border-white/10 shadow-2xl relative overflow-hidden mt-12 md:mt-8">
                 {/* Decorative blob inside card */}
                <div className="absolute top-0 right-0 -translate-y-1/2 translate-x-1/2 w-[500px] h-[500px] bg-indigo-600/20 blur-[120px] rounded-full pointer-events-none"></div>

                <div className="relative z-10 animate-in fade-in slide-in-from-bottom-5 duration-700">
                    <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-8 mb-12">
                        <div className="flex-1">
                            <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold uppercase tracking-wider mb-6 shadow-sm">
                                <Calendar className="w-3.5 h-3.5" />
                                Upcoming Event
                            </div>
                            <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-white mb-6 leading-[1.1] tracking-tight">
                                {event.title}
                            </h1>
                            <div className="flex items-center gap-3 text-slate-400 text-lg">
                                 <div className="p-2 bg-slate-800/50 rounded-full">
                                    <User className="w-4 h-4" />
                                 </div>
                                 <span>Hosted by <span className="text-slate-200 font-medium">{event.creatorEmail.split('@')[0]}</span></span>
                            </div>
                        </div>
                        
                        <button 
                            onClick={copyLink}
                            className="flex items-center gap-2 px-5 py-2.5 bg-slate-800/40 hover:bg-slate-800/60 rounded-xl text-sm font-medium text-slate-300 transition-all border border-white/5 hover:border-white/10"
                        >
                            {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Share2 className="w-4 h-4" />}
                            {copied ? 'Link Copied' : 'Share Event'}
                        </button>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
                        <div className="lg:col-span-2 space-y-8">
                            <div>
                                <h3 className="text-xl font-semibold text-white mb-4 flex items-center gap-2">
                                    About this event
                                </h3>
                                <div className="prose prose-invert max-w-none text-slate-300 leading-relaxed whitespace-pre-wrap">
                                    {event.description || 'No description provided.'}
                                </div>
                            </div>
                            
                            {/* Location placeholder if schema implies it, or just visual buffer */}
                            {event.location && (
                                <div>
                                     <h3 className="text-xl font-semibold text-white mb-4 flex items-center gap-2">
                                        Location
                                    </h3>
                                    <div className="flex items-center gap-3 text-slate-300">
                                        <MapPin className="w-5 h-5 text-indigo-400" />
                                        {event.location}
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="space-y-6">
                            <div className="p-6 rounded-2xl bg-gradient-to-b from-white/5 to-white/[0.02] border border-white/10 space-y-6 backdrop-blur-sm">
                                <div className="flex items-start gap-4">
                                    <div className="p-3.5 rounded-xl bg-indigo-500/20 text-indigo-400 shadow-inner ring-1 ring-inset ring-indigo-500/20">
                                        <Calendar className="w-6 h-6" />
                                    </div>
                                    <div>
                                        <div className="text-sm text-slate-400 font-medium mb-1">Date</div>
                                        <div className="text-white text-lg font-semibold tracking-tight">
                                            {eventDate.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                                        </div>
                                    </div>
                                </div>
                                
                                <div className="flex items-start gap-4">
                                    <div className="p-3.5 rounded-xl bg-cyan-500/20 text-cyan-400 shadow-inner ring-1 ring-inset ring-cyan-500/20">
                                        <Clock className="w-6 h-6" />
                                    </div>
                                    <div>
                                        <div className="text-sm text-slate-400 font-medium mb-1">Time</div>
                                        <div className="text-white text-lg font-semibold tracking-tight">
                                            {eventDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                        </div>
                                    </div>
                                </div>
                            </div>

                             <Link href={`/events/${event.slug}/join`} className="block group">
                                <ShinyButton className="w-full">
                                    <div className="flex items-center justify-center gap-2">
                                        I'm Attending
                                        <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                                    </div>
                                </ShinyButton>
                             </Link>
                             
                             <p className="text-center text-xs text-slate-500 mt-4">
                                limited spots available • secure registration
                             </p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div className="mt-12 text-center">
                 <p className="text-slate-500 text-sm">
                    Powered by <span className="text-slate-400 font-semibold">EventPing</span>
                 </p>
            </div>
        </div>
    </PageLayout>
  );
}
