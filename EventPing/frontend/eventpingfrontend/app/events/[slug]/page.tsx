'use client';

import { useState, useEffect, use } from 'react';
import { apiFetch } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { useRouter } from 'next/navigation';
import { PageLayout } from '@/components/layout/PageLayout';
import { Calendar, Users, MapPin, Clock, ArrowLeft, Share2, Loader2, CheckCircle, AlertCircle } from 'lucide-react';
import Link from 'next/link';

interface Event {
  id: number;
  title: string;
  description: string;
  eventDateTime: string;
  status: string;
  slug: string;
  creatorEmail: string;
  participantCount: number;
  location?: string;
}

export default function EventPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params);
  const [event, setEvent] = useState<Event | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [joinEmail, setJoinEmail] = useState('');
  const [isJoining, setIsJoining] = useState(false);
  const [joinMessage, setJoinMessage] = useState('');
  const [joinError, setJoinError] = useState(false);
  const { user } = useAuth();
  const router = useRouter();

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        const data = await apiFetch(`/events/${slug}`);
        setEvent(data);
        if (user) {
          setJoinEmail(user.email);
        }
      } catch (error) {
        console.error('Failed to fetch event', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchEvent();
  }, [slug, user]);

  const handleJoin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsJoining(true);
    setJoinMessage('');
    setJoinError(false);

    try {
      await apiFetch(`/participants/events/${slug}/join`, {
        method: 'POST',
        body: JSON.stringify({ email: joinEmail }),
      });
      setJoinMessage('Successfully joined the event!');
      setJoinError(false);
      // Refresh event to update count
      const data = await apiFetch(`/events/${slug}`);
      setEvent(data);
    } catch (err: any) {
      setJoinMessage(err.message || 'Failed to join event');
      setJoinError(true);
    } finally {
      setIsJoining(false);
    }
  };

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    // Could add a toast notification here
    alert('Link copied to clipboard!');
  };

  if (isLoading) {
    return (
      <PageLayout>
        <div className="flex min-h-[60vh] items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-indigo-500" />
        </div>
      </PageLayout>
    );
  }

  if (!event) {
    return (
      <PageLayout>
        <div className="flex min-h-[60vh] flex-col items-center justify-center text-center px-4">
          <div className="w-16 h-16 bg-slate-800/50 rounded-full flex items-center justify-center mb-4">
            <AlertCircle className="h-8 w-8 text-slate-500" />
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">Event not found</h1>
          <p className="text-slate-400 mb-6">The event you are looking for does not exist or has been removed.</p>
          <Link
            href="/dashboard"
            className="text-indigo-400 hover:text-indigo-300 font-medium flex items-center"
          >
            <ArrowLeft className="mr-2 h-4 w-4" /> Back to Dashboard
          </Link>
        </div>
      </PageLayout>
    );
  }

  const isCreator = user?.email === event.creatorEmail;
  const eventDate = new Date(event.eventDateTime);

  return (
    <PageLayout>
      <div className="max-w-4xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <Link 
          href="/dashboard" 
          className="inline-flex items-center text-slate-400 hover:text-white mb-6 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Dashboard
        </Link>

        <div className="bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl overflow-hidden shadow-2xl">
          {/* Header Banner */}
          <div className="bg-indigo-900/20 border-b border-white/5 p-8 md:p-12 relative overflow-hidden">
            <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-500/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2 pointer-events-none"></div>
            
            <div className="relative z-10">
              <div className="flex flex-col md:flex-row md:items-start justify-between gap-4 mb-4">
                <h1 className="text-3xl md:text-4xl font-bold text-white leading-tight">{event.title}</h1>
                {isCreator && (
                  <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 whitespace-nowrap">
                    You are the host
                  </span>
                )}
              </div>
              
              <div className="flex flex-wrap gap-4 md:gap-8 text-slate-300">
                <div className="flex items-center gap-2">
                  <Calendar className="h-5 w-5 text-indigo-400" />
                  <span>{eventDate.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Clock className="h-5 w-5 text-indigo-400" />
                  <span>{eventDate.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}</span>
                </div>
                {event.location && (
                  <div className="flex items-center gap-2">
                    <MapPin className="h-5 w-5 text-indigo-400" />
                    <span>{event.location}</span>
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-white/10">
            {/* Main Content */}
            <div className="md:col-span-2 p-8">
              <h2 className="text-xl font-semibold text-white mb-4">About this Event</h2>
              <div className="prose prose-invert max-w-none">
                <p className="text-slate-300 whitespace-pre-wrap leading-relaxed">
                  {event.description || "No description provided."}
                </p>
              </div>

              <div className="mt-8 pt-8 border-t border-white/10 flex items-center justify-between">
                <div className="flex items-center gap-2 text-slate-400">
                  <Users className="h-5 w-5" />
                  <span><strong className="text-white">{event.participantCount}</strong> people attending</span>
                </div>
                <button 
                  onClick={handleShare}
                  className="flex items-center gap-2 text-sm text-indigo-400 hover:text-indigo-300 transition-colors"
                >
                  <Share2 className="h-4 w-4" />
                  Share Event
                </button>
              </div>
            </div>

            {/* Sidebar / Join Action */}
            <div className="p-8 bg-slate-950/30">
              <h3 className="text-lg font-semibold text-white mb-6">Join Event</h3>
              
              <form onSubmit={handleJoin} className="space-y-4">
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-slate-400 mb-2">
                    Email Address
                  </label>
                  <input
                    type="email"
                    id="email"
                    required
                    placeholder="Enter your email"
                    className="block w-full px-4 py-2.5 bg-slate-800/50 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                    value={joinEmail}
                    onChange={(e) => setJoinEmail(e.target.value)}
                    disabled={!!user || isJoining} 
                  />
                  {user && (
                    <p className="mt-2 text-xs text-slate-500">
                      You are signed in as {user.fullName}
                    </p>
                  )}
                </div>

                <button
                  type="submit"
                  disabled={isJoining}
                  className="w-full flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-medium py-2.5 px-4 rounded-lg transition-all shadow-[0_0_20px_-5px_rgba(99,102,241,0.5)] hover:shadow-[0_0_25px_-5px_rgba(99,102,241,0.6)] disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isJoining ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" />
                      Joining...
                    </>
                  ) : (
                    'RSVP Now'
                  )}
                </button>
              </form>

              {joinMessage && (
                <div className={`mt-4 p-3 rounded-lg text-sm flex items-start gap-2 ${
                  joinError 
                    ? 'bg-red-500/10 text-red-400 border border-red-500/20' 
                    : 'bg-green-500/10 text-green-400 border border-green-500/20'
                }`}>
                  {joinError ? <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" /> : <CheckCircle className="h-4 w-4 shrink-0 mt-0.5" />}
                  <span>{joinMessage}</span>
                </div>
              )}
              
              <div className="mt-8">
                <h4 className="text-sm font-medium text-white mb-2">Organizer</h4>
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-indigo-500/20 flex items-center justify-center text-indigo-300 font-bold text-xs">
                    {event.creatorEmail.substring(0, 2).toUpperCase()}
                  </div>
                  <div className="text-sm text-slate-400 truncate">
                    {event.creatorEmail}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </PageLayout>
  );
}
