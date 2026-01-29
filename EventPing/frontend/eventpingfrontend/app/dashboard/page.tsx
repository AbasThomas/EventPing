'use client';

import { useEffect, useState } from 'react';
import { useAuth } from '@/lib/auth-context';
import { apiFetch } from '@/lib/api';
import Link from 'next/link';
import { Plus, Calendar, Clock, Users, ArrowRight, Sparkles } from 'lucide-react';

interface Event {
  id: number;
  title: string;
  description: string;
  eventDateTime: string;
  status: string;
  slug: string;
  participantCount: number;
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const data = await apiFetch('/events');
        // If data is array directly or wrapped? Controller returns List<Dto> so it's direct array usually
        // But apiFetch usually parses JSON.
        // Let's assume response is array based on Spring Controller.
        // Wait, standard spring response might be wrapped if using a wrapper advice, but looking at controller:
        // return ResponseEntity.ok(events); -> Array
        setEvents(Array.isArray(data) ? data : []);
      } catch (error) {
        console.error('Failed to fetch events', error);
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">Dashboard</h1>
          <p className="text-slate-400">Welcome back, {user?.fullName}</p>
        </div>
        <Link 
          href="/dashboard/events/create" 
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2.5 rounded-xl font-medium transition-all"
        >
          <Plus className="w-5 h-5" />
          Create Event
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="glass-panel p-6 rounded-2xl border border-white/10">
            <div className="flex items-center gap-4 mb-4">
                <div className="p-3 rounded-lg bg-indigo-500/20 text-indigo-400">
                    <Calendar className="w-6 h-6" />
                </div>
                <div>
                    <div className="text-2xl font-bold text-white">{events.length}</div>
                    <div className="text-sm text-slate-400">Total Events</div>
                </div>
            </div>
        </div>
         <div className="glass-panel p-6 rounded-2xl border border-white/10">
            <div className="flex items-center gap-4 mb-4">
                <div className="p-3 rounded-lg bg-cyan-500/20 text-cyan-400">
                    <Users className="w-6 h-6" />
                </div>
                <div>
                    <div className="text-2xl font-bold text-white">
                        {events.reduce((acc, curr) => acc + (curr.participantCount || 0), 0)}
                    </div>
                    <div className="text-sm text-slate-400">Total Participants</div>
                </div>
            </div>
        </div>
        <div className="glass-panel p-6 rounded-2xl border border-white/10 bg-slate-800/20">
             <div className="h-full flex flex-col justify-center">
                <h3 className="font-semibold text-white mb-1 flex items-center gap-2">
                    <Sparkles className="w-4 h-4 text-emerald-400" />
                    Growth Tip
                </h3>
                <p className="text-sm text-slate-300">
                    Events with detailed descriptions and custom reminders get 20% more engagement.
                </p>
             </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <div className="lg:col-span-3">
          <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-white">Your Events</h2>
              <Link href="/dashboard/events" className="text-sm text-indigo-400 hover:text-indigo-300 flex items-center gap-1">
                  View All <ArrowRight className="w-4 h-4" />
              </Link>
          </div>

          {events.length === 0 ? (
            <div className="glass-panel p-12 rounded-2xl border border-dashed border-white/20 text-center">
              <div className="w-16 h-16 bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-500">
                <Calendar className="w-8 h-8" />
              </div>
              <h3 className="text-lg font-medium text-white mb-2">No events yet</h3>
              <p className="text-slate-400 mb-6">Create your first event to start accepting participants.</p>
              <Link 
                href="/dashboard/events/create" 
                className="inline-flex items-center gap-2 text-indigo-400 hover:text-indigo-300 font-medium"
              >
                Create Event <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {events.slice(0, 4).map((event) => (
                <Link 
                  key={event.id} 
                  href={`/dashboard/events/${event.id}/details`}
                  className="glass-panel p-6 rounded-2xl border border-white/10 hover:border-indigo-500/50 transition-all group glass-panel-hover"
                >
                  <div className="flex justify-between items-start mb-4">
                    <div className={`px-2 py-1 rounded-md text-xs font-medium uppercase tracking-wider ${
                      event.status === 'PUBLISHED' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-700 text-slate-300'
                    }`}>
                      {event.status}
                    </div>
                    <div className="text-slate-500 text-xs flex items-center gap-1">
                      <Users className="w-3.5 h-3.5" />
                      {event.participantCount || 0}
                    </div>
                  </div>
                  <h3 className="text-lg font-bold text-white mb-2 line-clamp-1 group-hover:text-indigo-400 transition-colors">
                      {event.title}
                  </h3>
                  <p className="text-sm text-slate-400 mb-4 line-clamp-2 h-10">
                      {event.description}
                  </p>
                  <div className="flex items-center gap-2 text-xs text-slate-500 border-t border-white/5 pt-4">
                      <Clock className="w-3.5 h-3.5" />
                      {new Date(event.eventDateTime).toLocaleDateString()}
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>

        <div className="lg:col-span-1">
          <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-white">Integrations</h2>
              <Link href="/dashboard/integrations" className="text-sm text-indigo-400 hover:text-indigo-300 flex items-center gap-1">
                  Manage
              </Link>
          </div>
          <div className="space-y-4">
            <div className="glass-panel p-4 rounded-xl border border-white/5 flex items-center gap-4">
              <div className="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-400">
                <Calendar className="w-5 h-5" />
              </div>
              <div>
                <p className="text-sm font-medium text-white">G-Calendar</p>
                <p className="text-xs text-emerald-500">Connected</p>
              </div>
            </div>
            <div className="glass-panel p-4 rounded-xl border border-white/5 flex items-center gap-4">
              <div className="w-10 h-10 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-400">
                <Sparkles className="w-5 h-5" />
              </div>
              <div>
                <p className="text-sm font-medium text-white">Slack</p>
                <p className="text-xs text-slate-500">Not connected</p>
              </div>
            </div>
            <div className="glass-panel p-4 rounded-xl border border-white/5 flex items-center gap-4">
              <div className="w-10 h-10 rounded-lg bg-orange-500/10 flex items-center justify-center text-orange-400">
                <Zap className="w-5 h-5" />
              </div>
              <div>
                <p className="text-sm font-medium text-white">Zapier</p>
                <p className="text-xs text-slate-500">Not connected</p>
              </div>
            </div>
            <button className="w-full py-3 bg-white/5 hover:bg-white/10 text-white rounded-xl border border-white/10 transition-all text-sm font-medium">
              Browse More
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
