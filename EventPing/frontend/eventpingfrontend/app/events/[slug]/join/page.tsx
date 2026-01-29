'use client';

import { useEffect, useState, use } from 'react';
import { apiFetch } from '@/lib/api';
import Link from 'next/link';
import { Calendar, Clock, User, Mail, Bell, Loader2, CheckCircle2, ArrowRight, Phone, Type } from 'lucide-react';

interface Event {
    id: number;
    title: string;
    eventDateTime: string;
    slug: string;
    customFields?: CustomField[];
}

interface CustomField {
    id: number;
    fieldName: string;
    fieldType: 'TEXT' | 'EMAIL' | 'PHONE' | 'SELECT' | 'CHECKBOX';
    required: boolean;
    placeholderText: string;
    fieldOptions: string;
    displayOrder: number;
}

export default function JoinEventPage({ params }: { params: Promise<{ slug: string }> }) {
    const [event, setEvent] = useState<Event | null>(null);
    const [loading, setLoading] = useState(true);
    const [email, setEmail] = useState('');
    const [name, setName] = useState('');
    const [customFieldResponses, setCustomFieldResponses] = useState<Record<string, string>>({});
    
    const [reminders, setReminders] = useState({
        r1: true, // 1 hour
        r2: true  // 1 day
    });
    
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState('');

    const resolvedParams = use(params);

    useEffect(() => {
        const fetchEventAndFields = async () => {
            try {
                // Fetch event details
                const eventData = await apiFetch(`/events/${resolvedParams.slug}`);
                
                // Fetch custom fields
                const fieldsData = await apiFetch(`/events/${eventData.id}/custom-fields`);
                
                setEvent({ ...eventData, customFields: fieldsData || [] });
            } catch (err) {
                 console.error('Failed to fetch event');
            } finally {
                setLoading(false);
            }
        };
        fetchEventAndFields();
    }, [resolvedParams.slug]);

    const handleCustomFieldChange = (fieldName: string, value: string) => {
        setCustomFieldResponses(prev => ({ ...prev, [fieldName]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError('');

        try {
            // Validate required custom fields
            if (event?.customFields) {
                for (const field of event.customFields) {
                    if (field.required && !customFieldResponses[field.fieldName]?.trim()) {
                        setError(`Please fill in the required field: ${field.fieldName}`);
                        setIsSubmitting(false);
                        return;
                    }
                }
            }

            // Build reminder offsets
            const offsetMinutes = [];
            if (reminders.r1) offsetMinutes.push(60);
            if (reminders.r2) offsetMinutes.push(1440);

            // Construct query param for list
            const reminderQuery = offsetMinutes.map(m => `reminderOffsetMinutes=${m}`).join('&');

            await apiFetch(`/participants/events/${resolvedParams.slug}/join?${reminderQuery}`, {
                method: 'POST',
                body: JSON.stringify({ 
                    email, 
                    name,
                    customFieldResponses
                }),
            });
            
            setSuccess(true);
        } catch (err: any) {
            setError(err.message || 'Failed to join event. You might already be registered.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const renderCustomField = (field: CustomField) => {
        const value = customFieldResponses[field.fieldName] || '';

        switch (field.fieldType) {
            case 'TEXT':
                return (
                    <input
                        type="text"
                        value={value}
                        onChange={(e) => handleCustomFieldChange(field.fieldName, e.target.value)}
                        required={field.required}
                        placeholder={field.placeholderText}
                        className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                    />
                );
            
            case 'EMAIL':
                return (
                    <input
                        type="email"
                        value={value}
                        onChange={(e) => handleCustomFieldChange(field.fieldName, e.target.value)}
                        required={field.required}
                        placeholder={field.placeholderText}
                        className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                    />
                );
            
            case 'PHONE':
                return (
                    <input
                        type="tel"
                        value={value}
                        onChange={(e) => handleCustomFieldChange(field.fieldName, e.target.value)}
                        required={field.required}
                        placeholder={field.placeholderText}
                        className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                    />
                );
            
            case 'SELECT':
                const options = field.fieldOptions ? field.fieldOptions.split(',').map(o => o.trim()) : [];
                return (
                    <select
                        value={value}
                        onChange={(e) => handleCustomFieldChange(field.fieldName, e.target.value)}
                        required={field.required}
                        className="block w-full px-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                    >
                        <option value="">Select an option...</option>
                        {options.map(option => (
                            <option key={option} value={option}>{option}</option>
                        ))}
                    </select>
                );
            
            case 'CHECKBOX':
                return (
                    <label className="flex items-center gap-3 p-3 rounded-xl bg-slate-900/30 border border-slate-800 cursor-pointer hover:border-slate-700 transition-colors">
                        <input 
                            type="checkbox" 
                            checked={value === 'true'}
                            onChange={(e) => handleCustomFieldChange(field.fieldName, e.target.checked ? 'true' : 'false')}
                            className="w-4 h-4 rounded border-slate-600 text-indigo-500 focus:ring-indigo-500 bg-slate-800"
                        />
                        <span className="text-sm text-slate-300">{field.placeholderText || 'Yes'}</span>
                    </label>
                );
            
            default:
                return null;
        }
    };

    if (loading) {
         return (
           <div className="flex justify-center items-center min-h-screen">
             <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
           </div>
         );
    }

    if (!event) return null;

    if (success) {
        return (
            <div className="min-h-screen flex items-center justify-center p-4">
                <div className="glass-panel p-8 max-w-md w-full rounded-2xl border border-emerald-500/30 text-center animate-in zoom-in-95 duration-300">
                    <div className="w-16 h-16 bg-emerald-500/20 text-emerald-400 rounded-full flex items-center justify-center mx-auto mb-6">
                        <CheckCircle2 className="w-8 h-8" />
                    </div>
                    <h2 className="text-2xl font-bold text-white mb-2">You're In!</h2>
                    <p className="text-slate-300 mb-6">
                        You've successfully registered for <span className="text-white font-medium">{event.title}</span>. We've sent a confirmation email to {email}.
                    </p>
                    <Link 
                        href={`/events/${event.slug}`}
                        className="inline-block px-6 py-3 bg-white/10 hover:bg-white/20 rounded-xl text-white font-medium transition-colors"
                    >
                        Back to Event
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex flex-col items-center justify-center p-4">
             <div className="mb-8 text-center">
                 <Link href={`/events/${event.slug}`} className="inline-flex items-center gap-2 text-slate-400 hover:text-white mb-4 transition-colors">
                     <ArrowRight className="w-4 h-4 rotate-180" /> Back to Event
                 </Link>
                 <h1 className="text-3xl font-bold text-white mb-2">Join Event</h1>
                 <p className="text-slate-400">Enter your details to register and get reminders.</p>
             </div>

             <div className="glass-panel p-8 rounded-2xl border border-white/10 w-full max-w-md">
                 <div className="mb-6 p-4 rounded-xl bg-white/5 border border-white/5 flex items-start gap-4">
                     <div className="p-2 rounded-lg bg-indigo-500/20 text-indigo-400 mt-1">
                         <Calendar className="w-5 h-5" />
                     </div>
                     <div>
                         <h3 className="font-semibold text-white">{event.title}</h3>
                         <div className="text-sm text-slate-400 mt-1">
                            {new Date(event.eventDateTime).toLocaleDateString()} at {new Date(event.eventDateTime).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}
                         </div>
                     </div>
                 </div>

                 {error && (
                    <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm">
                      {error}
                    </div>
                  )}

                 <form onSubmit={handleSubmit} className="space-y-5">
                    <div className="space-y-2">
                        <label className="text-sm font-medium text-slate-300 ml-1">Full Name (Optional)</label>
                         <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                                <User className="h-5 w-5" />
                            </div>
                            <input
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                className="block w-full pl-10 pr-3 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                                placeholder="John Doe"
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-medium text-slate-300 ml-1">Email Address</label>
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                                <Mail className="h-5 w-5" />
                            </div>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                className="block w-full pl-10 pr-3 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                                placeholder="you@example.com"
                            />
                        </div>
                    </div>

                    {/* Custom Fields */}
                    {event.customFields && event.customFields.length > 0 && (
                        <div className="space-y-4 pt-2">
                            <div className="border-t border-slate-800 mb-4"></div>
                            <h3 className="text-sm font-medium text-slate-300 ml-1">Additional Information</h3>
                            {event.customFields
                                .sort((a, b) => a.displayOrder - b.displayOrder)
                                .map(field => (
                                    <div key={field.id} className="space-y-2">
                                        <label className="text-sm font-medium text-slate-300 ml-1">
                                            {field.fieldName}
                                            {field.required && <span className="text-red-400 ml-1">*</span>}
                                        </label>
                                        {renderCustomField(field)}
                                    </div>
                                ))}
                        </div>
                    )}

                    <div className="space-y-3 pt-2">
                         <label className="text-sm font-medium text-slate-300 ml-1 flex items-center gap-2">
                            <Bell className="w-4 h-4 text-cyan-400" />
                            Notification Preferences
                         </label>
                         <div className="space-y-2">
                             <label className="flex items-center gap-3 p-3 rounded-xl bg-slate-900/30 border border-slate-800 cursor-pointer hover:border-slate-700 transition-colors">
                                 <input 
                                    type="checkbox" 
                                    checked={reminders.r1}
                                    onChange={(e) => setReminders(prev => ({ ...prev, r1: e.target.checked }))}
                                    className="w-4 h-4 rounded border-slate-600 text-indigo-500 focus:ring-indigo-500 bg-slate-800"
                                 />
                                 <span className="text-sm text-slate-300">Remind me 1 hour before</span>
                             </label>
                             <label className="flex items-center gap-3 p-3 rounded-xl bg-slate-900/30 border border-slate-800 cursor-pointer hover:border-slate-700 transition-colors">
                                 <input 
                                    type="checkbox" 
                                    checked={reminders.r2}
                                    onChange={(e) => setReminders(prev => ({ ...prev, r2: e.target.checked }))}
                                    className="w-4 h-4 rounded border-slate-600 text-indigo-500 focus:ring-indigo-500 bg-slate-800"
                                 />
                                 <span className="text-sm text-slate-300">Remind me 1 day before</span>
                             </label>
                         </div>
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full flex justify-center items-center py-3 px-4 bg-white text-slate-900 hover:bg-slate-200 font-bold rounded-xl transition-all duration-200 disabled:opacity-70 disabled:cursor-not-allowed mt-4"
                    >
                        {isSubmitting ? (
                            <Loader2 className="h-5 w-5 animate-spin" />
                        ) : (
                            'Confirm Registration'
                        )}
                    </button>
                 </form>
             </div>
        </div>
    );
}
