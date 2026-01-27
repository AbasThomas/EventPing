'use client';

import { useEffect, useState, use } from 'react';
import { apiFetch } from '@/lib/api';
import Link from 'next/link';
import { Calendar, Clock, MapPin, User, ArrowRight, Share2, Copy, Check } from 'lucide-react';

interface Event {
  id: number;
  title: string;
  description: string;
  eventDateTime: string;
  status: string;
  slug: string;
  creatorEmail: string;
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
       <div className="flex justify-center items-center min-h-screen">
         <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
       </div>
     );
  }

  if (error || !event) {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen p-4 text-center">
            <div className="w-16 h-16 bg-slate-800 rounded-full flex items-center justify-center mb-6 text-slate-500">
                <Calendar className="w-8 h-8" />
            </div>
            <h1 className="text-3xl font-bold text-white mb-2">Event Not Found</h1>
            <p className="text-slate-400 mb-8">The event you are looking for does not exist or has been removed.</p>
            <Link href="/" className="px-6 py-3 bg-white/10 hover:bg-white/20 rounded-xl text-white font-medium transition-colors">
                Go Home
            </Link>
        </div>
    );
  }

  const eventDate = new Date(event.eventDateTime);

  return (
    <div className="container max-w-4xl mx-auto px-4 py-12 md:py-24">
        <div className="absolute top-6 left-0 w-full px-6 flex justify-between items-center z-20">
             <Link href="/" className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-full border border-indigo-500 flex items-center justify-center shadow-[0_0_10px_rgba(99,102,241,0.5)]">
                    <div className="w-2.5 h-2.5 bg-indigo-500 rounded-full"></div>
                </div>
                <span className="font-bold tracking-tight text-white">EventPing</span>
             </Link>
        </div>

        <div className="glass-panel p-8 md:p-12 rounded-3xl border border-white/10 shadow-2xl relative overflow-hidden">
             {/* Decorative blob */}
            <div className="absolute top-0 right-0 -translate-y-1/2 translate-x-1/2 w-96 h-96 bg-indigo-600/20 blur-[100px] rounded-full pointer-events-none"></div>

            <div className="relative z-10">
                <div className="flex flex-col md:flex-row md:items-start justify-between gap-6 mb-8">
                    <div>
                        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-medium uppercase tracking-wider mb-4">
                            <Calendar className="w-3 h-3" />
                            Upcoming Event
                        </div>
                        <h1 className="text-4xl md:text-5xl font-bold text-white mb-4 leading-tight">
                            {event.title}
                        </h1>
                        <div className="flex items-center gap-3 text-slate-400">
                             <User className="w-4 h-4" />
                             Hosted by {event.creatorEmail.split('@')[0]}
                        </div>
                    </div>
                    
                    <button 
                        onClick={copyLink}
                        className="flex items-center gap-2 px-4 py-2 bg-white/5 hover:bg-white/10 rounded-lg text-sm text-slate-300 transition-colors"
                    >
                        {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Share2 className="w-4 h-4" />}
                        {copied ? 'Copied Link' : 'Share Event'}
                    </button>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
                    <div className="md:col-span-2 space-y-8">
                        <div>
                            <h3 className="text-lg font-semibold text-white mb-3">About this event</h3>
                            <p className="text-slate-300 leading-relaxed whitespace-pre-wrap">
                                {event.description || 'No description provided.'}
                            </p>
                        </div>
                    </div>

                    <div className="space-y-6">
                        <div className="p-6 rounded-2xl bg-white/5 border border-white/10 space-y-4">
                            <div className="flex items-start gap-4">
                                <div className="p-3 rounded-xl bg-indigo-500/20 text-indigo-400">
                                    <Calendar className="w-6 h-6" />
                                </div>
                                <div>
                                    <div className="text-sm text-slate-400 mb-1">Date</div>
                                    <div className="text-white font-semibold">
                                        {eventDate.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                                    </div>
                                </div>
                            </div>
                            
                            <div className="flex items-start gap-4">
                                <div className="p-3 rounded-xl bg-cyan-500/20 text-cyan-400">
                                    <Clock className="w-6 h-6" />
                                </div>
                                <div>
                                    <div className="text-sm text-slate-400 mb-1">Time</div>
                                    <div className="text-white font-semibold">
                                        {eventDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                    </div>
                                </div>
                            </div>
                        </div>

                         <Link 
                            href={`/events/${event.slug}/join`}
                            className="w-full block text-center py-4 bg-white text-slate-900 font-bold rounded-xl hover:bg-slate-200 transition-colors shadow-lg shadow-white/10"
                         >
                            I'm Attending
                            <ArrowRight className="w-4 h-4 inline-block ml-2" />
                         </Link>
                    </div>
                </div>
            </div>
        </div>
    </div>
  );
}
