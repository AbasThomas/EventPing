'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import Link from 'next/link';
import { PageLayout } from '@/components/layout/PageLayout';
import { Calendar, Plus, MapPin, Clock, Users, ArrowRight, Loader2, Search } from 'lucide-react';

interface Event {
  id: number;
  title: string;
  description: string;
  eventDateTime: string;
  location?: string;
  slug: string;
  participantCount: number;
  status: string;
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    if (user) {
      fetchEvents();
    }
  }, [user]);

  const fetchEvents = async () => {
    try {
      const data = await apiFetch('/events');
      setEvents(data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch events');
    } finally {
      setLoading(false);
    }
  };

  const filteredEvents = events.filter(event => 
    event.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    event.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (!user) {
    return null; // Auth context handles redirect
  }

  return (
    <PageLayout>
      <div className="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-bold text-white mb-1">Dashboard</h1>
            <p className="text-slate-400">Welcome back, {user.fullName}</p>
          </div>
          <Link
            href="/events/create"
            className="inline-flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-medium py-2.5 px-5 rounded-full transition-all shadow-[0_0_20px_-5px_rgba(99,102,241,0.5)] hover:shadow-[0_0_25px_-5px_rgba(99,102,241,0.6)]"
          >
            <Plus className="h-5 w-5" />
            Create Event
          </Link>
        </div>

        {/* Search */}
        <div className="relative max-w-md mb-8">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search className="h-5 w-5 text-slate-500" />
          </div>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="block w-full pl-10 pr-3 py-2.5 bg-slate-900/50 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
            placeholder="Search your events..."
          />
        </div>

        {/* Content */}
        {loading ? (
          <div className="flex justify-center py-20">
            <Loader2 className="h-8 w-8 animate-spin text-indigo-500" />
          </div>
        ) : error ? (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 px-4 py-3 rounded-lg">
            {error}
          </div>
        ) : filteredEvents.length === 0 ? (
          <div className="text-center py-20 bg-slate-900/30 rounded-2xl border border-white/5 border-dashed">
            <div className="w-16 h-16 bg-slate-800/50 rounded-full flex items-center justify-center mx-auto mb-4">
              <Calendar className="h-8 w-8 text-slate-500" />
            </div>
            <h3 className="text-xl font-medium text-white mb-2">No events found</h3>
            <p className="text-slate-400 mb-6 max-w-md mx-auto">
              {searchTerm ? "Try adjusting your search terms." : "You haven't created any events yet. Start planning your first event today!"}
            </p>
            {!searchTerm && (
              <Link
                href="/events/create"
                className="inline-flex items-center text-indigo-400 hover:text-indigo-300 font-medium"
              >
                Create your first event <ArrowRight className="ml-1 h-4 w-4" />
              </Link>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredEvents.map((event) => (
              <Link
                key={event.id}
                href={`/events/${event.slug}`}
                className="group bg-slate-900/50 hover:bg-slate-800/50 border border-white/10 hover:border-indigo-500/30 rounded-xl p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl flex flex-col h-full"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="w-10 h-10 bg-indigo-500/10 rounded-lg flex items-center justify-center group-hover:bg-indigo-500/20 transition-colors">
                    <Calendar className="h-5 w-5 text-indigo-400" />
                  </div>
                  <span className={`text-xs font-medium px-2 py-1 rounded ${
                    event.status === 'ACTIVE' 
                      ? 'bg-green-500/10 text-green-400 border border-green-500/20' 
                      : 'bg-slate-800 text-slate-400'
                  }`}>
                    {new Date(event.eventDateTime).toLocaleDateString()}
                  </span>
                </div>
                
                <h3 className="text-lg font-semibold text-white mb-2 line-clamp-1 group-hover:text-indigo-300 transition-colors">
                  {event.title}
                </h3>
                
                <p className="text-slate-400 text-sm mb-4 line-clamp-2 min-h-[40px] flex-grow">
                  {event.description || 'No description provided.'}
                </p>
                
                <div className="flex items-center gap-4 text-xs text-slate-500 border-t border-white/5 pt-4 mt-auto">
                  <div className="flex items-center gap-1">
                    <Clock className="h-3.5 w-3.5" />
                    {new Date(event.eventDateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </div>
                  {event.location && (
                    <div className="flex items-center gap-1">
                      <MapPin className="h-3.5 w-3.5" />
                      <span className="truncate max-w-[80px]">{event.location}</span>
                    </div>
                  )}
                  <div className="flex items-center gap-1 ml-auto">
                    <Users className="h-3.5 w-3.5" />
                    <span>{event.participantCount || 0}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </PageLayout>
  );
}
