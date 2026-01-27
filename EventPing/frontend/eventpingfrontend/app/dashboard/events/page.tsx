'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { apiFetch } from '@/lib/api';
import { Plus, Search, Filter, Calendar, Users, Clock, Loader2, Play } from 'lucide-react';

interface Event {
  id: number;
  title: string;
  description: string;
  eventDateTime: string;
  status: string;
  slug: string;
  participantCount: number;
}

export default function EventsPage() {
  const [events, setEvents] = useState<Event[]>([]);
  const [filteredEvents, setFilteredEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const data = await apiFetch('/events');
        setEvents(Array.isArray(data) ? data : []);
        setFilteredEvents(Array.isArray(data) ? data : []);
      } catch (error) {
        console.error('Failed to fetch events', error);
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  useEffect(() => {
    let result = events;

    if (searchTerm) {
      result = result.filter(e => 
        e.title.toLowerCase().includes(searchTerm.toLowerCase()) || 
        (e.description && e.description.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    if (filterStatus !== 'ALL') {
      result = result.filter(e => e.status === filterStatus);
    }

    setFilteredEvents(result);
  }, [searchTerm, filterStatus, events]);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">My Events</h1>
          <p className="text-slate-400">Manage your scheduled events and participants</p>
        </div>
        <Link 
          href="/dashboard/events/create" 
          className="flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-cyan-600 hover:from-indigo-500 hover:to-cyan-500 text-white px-5 py-2.5 rounded-xl font-medium shadow-lg shadow-indigo-500/20 hover:shadow-indigo-500/30 transition-all justify-center md:justify-start"
        >
          <Plus className="w-5 h-5" />
          Create Event
        </Link>
      </div>

      <div className="glass-panel p-4 rounded-2xl border border-white/10 flex flex-col md:flex-row gap-4 items-center">
        <div className="relative w-full md:flex-1">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
            <Search className="h-4 w-4" />
          </div>
          <input
            type="text"
            placeholder="Search events..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="block w-full pl-10 pr-3 py-2 bg-slate-900/50 border border-slate-700/50 rounded-lg text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 transition-all text-sm"
          />
        </div>
        <div className="flex items-center gap-2 w-full md:w-auto">
          <Filter className="w-4 h-4 text-slate-500" />
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="block w-full md:w-40 px-3 py-2 bg-slate-900/50 border border-slate-700/50 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 text-sm appearance-none"
          >
            <option value="ALL">All Status</option>
            <option value="DRAFT">Draft</option>
            <option value="PUBLISHED">Published</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>
      </div>

      {filteredEvents.length === 0 ? (
        <div className="text-center py-20">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-800 text-slate-400 mb-4">
            <Calendar className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-medium text-white mb-2">No events found</h3>
          <p className="text-slate-400 mb-6 max-w-sm mx-auto">
            {searchTerm || filterStatus !== 'ALL' 
              ? 'Try adjusting your search or filters to find what you looking for.' 
              : 'Get started by creating your first event.'}
          </p>
          {!searchTerm && filterStatus === 'ALL' && (
            <Link 
              href="/dashboard/events/create" 
              className="text-indigo-400 hover:text-indigo-300 font-medium inline-flex items-center gap-2"
            >
              Create New Event <ArrowRight className="w-4 h-4" />
            </Link>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {filteredEvents.map((event) => (
            <Link 
              key={event.id} 
              href={`/dashboard/events/${event.id}/details`}
              className="glass-panel p-5 rounded-xl border border-white/10 hover:border-indigo-500/30 transition-all group glass-panel-hover flex flex-col md:flex-row md:items-center gap-4 md:gap-8"
            >
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                    <h3 className="text-lg font-bold text-white group-hover:text-indigo-400 transition-colors">
                        {event.title}
                    </h3>
                    <div className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                        event.status === 'PUBLISHED' ? 'bg-emerald-500/20 text-emerald-400' : 
                        event.status === 'DRAFT' ? 'bg-slate-700 text-slate-300' :
                        event.status === 'CANCELLED' ? 'bg-red-500/20 text-red-400' :
                        'bg-blue-500/20 text-blue-400'
                    }`}>
                        {event.status}
                    </div>
                </div>
                <p className="text-sm text-slate-400 line-clamp-1 mb-3 md:mb-0">
                    {event.description}
                </p>
              </div>
              
              <div className="flex items-center gap-6 text-sm text-slate-400">
                <div className="flex items-center gap-2 min-w-[140px]">
                    <Clock className="w-4 h-4 text-indigo-400" />
                    <div className="flex flex-col">
                        <span className="text-slate-200 font-medium">{new Date(event.eventDateTime).toLocaleDateString()}</span>
                        <span className="text-xs">{new Date(event.eventDateTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                    </div>
                </div>
                
                <div className="flex items-center gap-2 min-w-[80px]">
                    <Users className="w-4 h-4 text-cyan-400" />
                    <div>
                        <span className="text-slate-200 font-medium">{event.participantCount}</span>
                        <span className="text-xs hidden md:inline ml-1">joined</span>
                    </div>
                </div>

                <div className="ml-auto md:ml-0 p-2 rounded-full bg-slate-800 text-slate-400 group-hover:bg-indigo-600 group-hover:text-white transition-all">
                    <Play className="w-4 h-4 fill-current" />
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

function ArrowRight({ className }: { className?: string }) {
  return (
    <svg 
      xmlns="http://www.w3.org/2000/svg" 
      width="24" 
      height="24" 
      viewBox="0 0 24 24" 
      fill="none" 
      stroke="currentColor" 
      strokeWidth="2" 
      strokeLinecap="round" 
      strokeLinejoin="round" 
      className={className}
    >
      <path d="M5 12h14" />
      <path d="m12 5 7 7-7 7" />
    </svg>
  );
}
