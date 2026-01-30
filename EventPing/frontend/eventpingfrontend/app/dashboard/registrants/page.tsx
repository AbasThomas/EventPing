'use client';

import { useState, useEffect } from 'react';
import { apiFetch } from '@/lib/api';
import { 
  Users, 
  Search, 
  Calendar, 
  Mail, 
  Phone, 
  ArrowRight,
  Filter,
  Download,
  Loader2
} from 'lucide-react';

interface Participant {
  id: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  joinedAt: string;
  eventName: string;
}

interface Event {
  id: number;
  title: string;
  slug: string;
}

export default function RegistrantsPage() {
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedEvent, setSelectedEvent] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const eventsData = await apiFetch('/events');
        setEvents(eventsData);
        
        // Fetch participants for all events or the first one?
        // Let's assume we want to show a consolidated view if possible, 
        // but the backend endpoint is per event slug.
        // For now, if 'all' is selected, we'll fetch from all user events and flatten.
        if (eventsData && eventsData.length > 0) {
            const allParticipants = await Promise.all(
                eventsData.map(async (event: Event) => {
                    const p = await apiFetch(`/participants/events/${event.slug}`);
                    return p.map((item: any) => ({
                        ...item, 
                        eventName: event.title,
                        fullName: item.fullName || item.email?.split('@')[0] || 'Unknown',
                        phoneNumber: item.phoneNumber || 'N/A'
                    }));
                })
            );
            setParticipants(allParticipants.flat());
        }
      } catch (error) {
        console.error('Failed to fetch registrants', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const filteredParticipants = participants.filter(p => {
    if (!p) return false;
    const name = String(p.fullName || '');
    const email = String(p.email || '');
    const query = String(searchQuery || '').toLowerCase();
    
    const matchesSearch = name.toLowerCase().includes(query) || 
                         email.toLowerCase().includes(query);
    
    // Handle potential missing event info
    if (selectedEvent === 'all') return matchesSearch;
    
    const eventTitle = events.find(e => e.slug === selectedEvent)?.title;
    return matchesSearch && (p.eventName === eventTitle);
  });

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">Registrants</h1>
          <p className="text-slate-400">View and manage people who joined your events</p>
        </div>
        <div className="flex items-center gap-3">
            <button className="flex items-center gap-2 px-4 py-2 bg-white/5 hover:bg-white/10 border border-white/10 rounded-xl text-slate-300 transition-colors">
                <Download className="w-4 h-4" />
                Export CSV
            </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="md:col-span-1 space-y-6">
            <div className="glass-panel p-6 rounded-2xl border border-white/10">
                <div className="flex items-center gap-2 text-white font-semibold mb-4">
                    <Filter className="w-4 h-4 text-indigo-400" />
                    Filters
                </div>
                <div className="space-y-4">
                    <div>
                        <label className="text-xs text-slate-500 uppercase font-bold mb-2 block">Event</label>
                        <select 
                            value={selectedEvent}
                            onChange={(e) => setSelectedEvent(e.target.value)}
                            className="w-full bg-slate-900 border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                        >
                            <option value="all">All Events</option>
                            {events.map(event => (
                                <option key={event.id} value={event.slug}>{event.title}</option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>

            <div className="glass-panel p-6 rounded-2xl border border-white/10 bg-gradient-to-br from-indigo-500/10 to-transparent">
                <h4 className="text-white font-medium mb-1">Quick Stats</h4>
                <div className="space-y-3 mt-4">
                    <div className="flex justify-between items-center">
                        <span className="text-slate-400 text-sm">Total Registrants</span>
                        <span className="text-white font-bold">{participants.length}</span>
                    </div>
                    <div className="flex justify-between items-center">
                        <span className="text-slate-400 text-sm">Filtered</span>
                        <span className="text-indigo-400 font-bold">{filteredParticipants.length}</span>
                    </div>
                </div>
            </div>
        </div>

        <div className="md:col-span-3 space-y-6">
            <div className="relative group">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                    <Search className="h-5 w-5" />
                </div>
                <input 
                    type="text" 
                    placeholder="Search by name or email..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-12 pr-4 py-3 bg-slate-900/50 border border-white/10 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-all"
                />
            </div>

            {loading ? (
                <div className="flex flex-col items-center justify-center py-20">
                    <Loader2 className="w-10 h-10 text-indigo-500 animate-spin mb-4" />
                    <p className="text-slate-400">Loading registrants...</p>
                </div>
            ) : filteredParticipants.length === 0 ? (
                <div className="glass-panel p-20 rounded-2xl border border-dashed border-white/20 text-center">
                    <Users className="w-12 h-12 text-slate-600 mx-auto mb-4" />
                    <h3 className="text-white font-medium text-lg">No registrants found</h3>
                    <p className="text-slate-500 max-w-xs mx-auto mt-2">
                        {searchQuery ? "Try adjusting your search or filters." : "Start promoting your events to see people joining here."}
                    </p>
                </div>
            ) : (
                <div className="glass-panel rounded-2xl border border-white/10 overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="border-b border-white/5 bg-white/5">
                                    <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Participant</th>
                                    <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Contact</th>
                                    <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Event</th>
                                    <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Joined</th>
                                    <th className="px-6 py-4"></th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-white/5">
                                {filteredParticipants.map((p) => (
                                    <tr key={p.id} className="hover:bg-white/[0.02] transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-9 h-9 rounded-full bg-indigo-500/10 flex items-center justify-center text-indigo-400 font-bold border border-indigo-500/20 text-sm">
                                                    {p.fullName.charAt(0)}
                                                </div>
                                                <div className="font-medium text-white">{p.fullName}</div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="space-y-1">
                                                <div className="flex items-center gap-2 text-sm text-slate-300">
                                                    <Mail className="w-3.5 h-3.5 text-slate-500" />
                                                    {p.email}
                                                </div>
                                                <div className="flex items-center gap-2 text-sm text-slate-300">
                                                    <Phone className="w-3.5 h-3.5 text-slate-500" />
                                                    {p.phoneNumber}
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-2">
                                                <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                                                <span className="text-sm text-slate-300">{p.eventName}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-slate-400">
                                            {new Date(p.joinedAt).toLocaleDateString()}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <button className="p-2 text-slate-500 hover:text-white transition-colors">
                                                <ArrowRight className="w-4 h-4" />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
      </div>
    </div>
  );
}
