'use client';

import { useEffect, useState, use } from 'react';
import { apiFetch } from '@/lib/api';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Calendar, Clock, ArrowLeft, Users, Edit, Trash2, Link as LinkIcon, Share2, Circle, AlertTriangle, Loader2 } from 'lucide-react';

interface Event {
  id: number;
  title: string;
  description: string;
  eventDateTime: string;
  status: string;
  slug: string;
  participantCount: number;
}

interface Participant {
  id: number;
  email: string;
  joinedAt: string;
  unsubscribed: boolean;
}

export default function EventDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const router = useRouter();
  const [event, setEvent] = useState<Event | null>(null);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);
  const [copied, setCopied] = useState(false);

  // Unwrap params using React.use()
  const resolvedParams = use(params);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const eventData = await apiFetch(`/events/${resolvedParams.id}/details`);
        setEvent(eventData);

        if (eventData && eventData.slug) {
             try {
                const participantsData = await apiFetch(`/participants/events/${eventData.slug}`);
                setParticipants(Array.isArray(participantsData) ? participantsData : []);
             } catch (pErr) {
                 console.error("Failed to fetch participants", pErr);
             }
        }
      } catch (err) {
        console.error('Failed to fetch event details', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [resolvedParams.id]);

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this event? This action cannot be undone.')) {
        return;
    }

    setDeleting(true);
    try {
        await apiFetch(`/events/${resolvedParams.id}`, {
            method: 'DELETE'
        });
        router.push('/dashboard/events');
    } catch (err) {
        alert('Failed to delete event');
        setDeleting(false);
    }
  };

  const copyPublicLink = () => {
    if (!event) return;
    const url = `${window.location.origin}/events/${event.slug}`;
    navigator.clipboard.writeText(url);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  if (!event) {
    return <div>Event not found</div>;
  }

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex items-center gap-4 mb-6">
        <Link href="/dashboard/events" className="p-2 rounded-lg hover:bg-white/5 text-slate-400 hover:text-white transition-colors">
            <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-white mb-1">{event.title}</h1>
          <div className="flex items-center gap-2 text-sm text-slate-400">
             <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                event.status === 'PUBLISHED' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-700 text-slate-300'
             }`}>
                <Circle className="w-1.5 h-1.5 fill-current" />
                {event.status}
             </span>
             <span>•</span>
             <span>Created {new Date().toLocaleDateString()}</span>
          </div>
        </div>
        <div className="ml-auto flex gap-2">
            <Link 
                href={`/dashboard/events/${event.id}/edit`}
                className="flex items-center gap-2 px-4 py-2 bg-white/5 hover:bg-white/10 text-white rounded-lg transition-colors border border-white/10"
            >
                <Edit className="w-4 h-4" />
                Edit
            </Link>
            <button 
                onClick={handleDelete}
                disabled={deleting}
                className="flex items-center gap-2 px-4 py-2 bg-red-500/10 hover:bg-red-500/20 text-red-400 rounded-lg transition-colors border border-red-500/20"
            >
                {deleting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Trash2 className="w-4 h-4" />}
                Delete
            </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-8">
            <div className="glass-panel p-6 rounded-2xl border border-white/10">
                <h2 className="text-lg font-semibold text-white mb-4">Event Details</h2>
                <div className="space-y-4">
                    <p className="text-slate-300 whitespace-pre-wrap">{event.description || 'No description provided.'}</p>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-white/5">
                        <div className="flex items-center gap-3 text-slate-400">
                            <Calendar className="w-5 h-5 text-indigo-400" />
                            <div>
                                <span className="block text-xs uppercase tracking-wider font-semibold text-slate-500">Date</span>
                                <span className="text-slate-200">{new Date(event.eventDateTime).toLocaleDateString()}</span>
                            </div>
                        </div>
                        <div className="flex items-center gap-3 text-slate-400">
                            <Clock className="w-5 h-5 text-cyan-400" />
                            <div>
                                <span className="block text-xs uppercase tracking-wider font-semibold text-slate-500">Time</span>
                                <span className="text-slate-200">{new Date(event.eventDateTime).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="glass-panel p-6 rounded-2xl border border-white/10">
                <div className="flex items-center justify-between mb-6">
                    <h2 className="text-lg font-semibold text-white flex items-center gap-2">
                        <Users className="w-5 h-5 text-indigo-400" />
                        Participants ({participants.length})
                    </h2>
                </div>

                {participants.length === 0 ? (
                    <div className="text-center py-8 text-slate-500">
                        <Users className="w-8 h-8 mx-auto mb-2 opacity-50" />
                        <p>No participants yet.</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left">
                            <thead className="text-xs text-slate-500 uppercase bg-slate-900/50">
                                <tr>
                                    <th className="px-4 py-3 rounded-l-lg">Email</th>
                                    <th className="px-4 py-3">Joined At</th>
                                    <th className="px-4 py-3 rounded-r-lg">Status</th>
                                </tr>
                            </thead>
                            <tbody className="text-sm">
                                {participants.map((p) => (
                                    <tr key={p.id} className="border-b border-white/5 last:border-0 hover:bg-white/5 transition-colors">
                                        <td className="px-4 py-3 font-medium text-slate-300">{p.email}</td>
                                        <td className="px-4 py-3 text-slate-500">{new Date(p.joinedAt).toLocaleDateString()}</td>
                                        <td className="px-4 py-3">
                                            {p.unsubscribed ? (
                                                <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-400/10 text-red-400">
                                                    Unsubscribed
                                                </span>
                                            ) : (
                                                <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-emerald-400/10 text-emerald-400">
                                                    Active
                                                </span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
            <div className="glass-panel p-6 rounded-2xl border border-white/10">
                <h3 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4">Quick Actions</h3>
                
                <div className="space-y-3">
                    <button 
                        onClick={copyPublicLink}
                        className="w-full flex items-center justify-between p-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white transition-all shadow-lg shadow-indigo-500/20 group"
                    >
                        <span className="flex items-center gap-2 font-medium">
                            <Share2 className="w-4 h-4" />
                            Share Event
                        </span>
                        {copied ? <span className="text-xs bg-white/20 px-2 py-0.5 rounded">Copied!</span> : <ArrowRight className="w-4 h-4 opacity-50 group-hover:opacity-100 transition-opacity" />}
                    </button>
                    
                    <a 
                        href={`/events/${event.slug}`}
                        target="_blank"
                        className="w-full flex items-center gap-2 p-3 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 transition-colors border border-white/5 hover:border-white/10"
                    >
                        <LinkIcon className="w-4 h-4 text-slate-500" />
                        View Public Page
                    </a>
                </div>
            </div>

            <div className="glass-panel p-6 rounded-2xl border border-white/10 bg-amber-500/5 border-amber-500/20">
                <h3 className="text-sm font-semibold text-amber-400 flex items-center gap-2 mb-2">
                    <AlertTriangle className="w-4 h-4" />
                    Reminder Status
                </h3>
                <p className="text-sm text-slate-400">
                    Reminders are scheduled successfully. Participants will receive notifications automatically.
                </p>
            </div>
        </div>
      </div>
    </div>
  );
}
