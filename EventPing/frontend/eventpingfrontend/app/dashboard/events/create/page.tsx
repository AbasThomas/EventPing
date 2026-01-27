'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch } from '@/lib/api';
import Link from 'next/link';
import { ArrowLeft, Calendar, Clock, Loader2, Save, Type, Bell } from 'lucide-react';

export default function CreateEventPage() {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    date: '',
    time: '',
    reminder1: '60', // 1 hour
    reminder2: '1440', // 1 day
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError('');

    try {
      // Combine date and time
      const eventDateTime = `${formData.date}T${formData.time}:00`;
      
      const reminders = [];
      if (formData.reminder1) reminders.push(parseInt(formData.reminder1));
      if (formData.reminder2) reminders.push(parseInt(formData.reminder2));

      await apiFetch('/events', {
        method: 'POST',
        body: JSON.stringify({
          title: formData.title,
          description: formData.description,
          eventDateTime,
          reminderOffsetMinutes: reminders
        }),
      });

      router.push('/dashboard/events');
    } catch (err: any) {
      setError(err.message || 'Failed to create event');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Get current date for min attribute
  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-in fade-in duration-500">
      <div className="flex items-center gap-4 mb-4">
        <Link href="/dashboard/events" className="p-2 rounded-lg hover:bg-white/5 text-slate-400 hover:text-white transition-colors">
            <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-3xl font-bold text-white mb-1">Create Event</h1>
          <p className="text-slate-400 text-sm">Schedule a new event and notify participants</p>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm flex items-center gap-2">
            <div className="w-1.5 h-1.5 rounded-full bg-red-500"></div>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="glass-panel p-6 rounded-2xl border border-white/10 space-y-6">
            <div className="space-y-2">
                <label className="text-sm font-medium text-slate-300 ml-1 flex items-center gap-2">
                    <Type className="w-4 h-4 text-indigo-400" />
                    Event Title
                </label>
                <input
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleChange}
                    required
                    maxLength={100}
                    className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                    placeholder="e.g. Quarterly Team Meeting"
                />
            </div>

            <div className="space-y-2">
                <label className="text-sm font-medium text-slate-300 ml-1">Description</label>
                <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows={4}
                    className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all resize-none"
                    placeholder="Add details about your event..."
                />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                    <label className="text-sm font-medium text-slate-300 ml-1 flex items-center gap-2">
                        <Calendar className="w-4 h-4 text-cyan-400" />
                        Date
                    </label>
                    <input
                        type="date"
                        name="date"
                        value={formData.date}
                        onChange={handleChange}
                        required
                        min={today}
                         className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all [color-scheme:dark]"
                    />
                </div>
                <div className="space-y-2">
                    <label className="text-sm font-medium text-slate-300 ml-1 flex items-center gap-2">
                        <Clock className="w-4 h-4 text-cyan-400" />
                        Time
                    </label>
                    <input
                        type="time"
                        name="time"
                        value={formData.time}
                        onChange={handleChange}
                        required
                        className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all [color-scheme:dark]"
                    />
                </div>
            </div>
        </div>

        <div className="glass-panel p-6 rounded-2xl border border-white/10 space-y-6">
            <div>
                <h3 className="text-lg font-semibold text-white mb-1 flex items-center gap-2">
                    <Bell className="w-5 h-5 text-amber-400" />
                    Automatic Reminders
                </h3>
                <p className="text-sm text-slate-400 mb-6">Conform when participants will receive email reminders before the event.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                    <label className="text-sm font-medium text-slate-300 ml-1">Reminder 1</label>
                    <select
                        name="reminder1"
                        value={formData.reminder1}
                        onChange={handleChange}
                        className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all appearance-none"
                    >
                        <option value="15">15 minutes before</option>
                        <option value="30">30 minutes before</option>
                        <option value="60">1 hour before</option>
                        <option value="120">2 hours before</option>
                    </select>
                </div>
                <div className="space-y-2">
                    <label className="text-sm font-medium text-slate-300 ml-1">Reminder 2</label>
                    <select
                        name="reminder2"
                        value={formData.reminder2}
                        onChange={handleChange}
                        className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all appearance-none"
                    >
                        <option value="">None</option>
                        <option value="1440">1 day before</option>
                        <option value="2880">2 days before</option>
                        <option value="10080">1 week before</option>
                    </select>
                </div>
            </div>
        </div>

        <div className="flex items-center justify-end gap-4 pt-2">
            <Link 
                href="/dashboard/events"
                className="px-6 py-3 rounded-xl text-slate-300 hover:text-white hover:bg-white/5 font-medium transition-colors"
            >
                Cancel
            </Link>
            <button
                type="submit"
                disabled={isSubmitting}
                className="flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-cyan-600 hover:from-indigo-500 hover:to-cyan-500 text-white px-8 py-3 rounded-xl font-medium shadow-lg shadow-indigo-500/20 hover:shadow-indigo-500/30 transition-all disabled:opacity-70 disabled:cursor-not-allowed"
            >
                {isSubmitting ? (
                    <Loader2 className="w-5 h-5 animate-spin" />
                ) : (
                    <>
                        <Save className="w-5 h-5" />
                        Create Event
                    </>
                )}
            </button>
        </div>
      </form>
    </div>
  );
}
