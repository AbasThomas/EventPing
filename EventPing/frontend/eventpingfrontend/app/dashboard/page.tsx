'use client';

import { useState, useEffect } from 'react';
import { apiFetch } from '@/lib/api';
import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';

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
  const [events, setEvents] = useState<Event[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const data = await apiFetch('/events');
        setEvents(data);
      } catch (error) {
        console.error('Failed to fetch events', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchEvents();
  }, []);

  if (isLoading) return <div className="p-24 text-center">Loading events...</div>;

  return (
    <div className="min-h-screen bg-background p-8 md:p-24">
      <div className="max-w-6xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-foreground">Your Events</h1>
          <Link
            href="/events/create"
            className="rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
          >
            Create Event
          </Link>
        </div>

        {events.length === 0 ? (
          <div className="text-center py-12 border-2 border-dashed border-gray-700 rounded-lg">
            <h3 className="mt-2 text-sm font-semibold text-foreground">No events</h3>
            <p className="mt-1 text-sm text-gray-500">Get started by creating a new event.</p>
            <div className="mt-6">
              <Link
                href="/events/create"
                className="inline-flex items-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
              >
                Create Event
              </Link>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {events.map((event) => (
              <div
                key={event.id}
                className="relative flex flex-col overflow-hidden rounded-lg border border-gray-700 bg-gray-800 p-6 hover:shadow-lg transition-shadow"
              >
                <div className="flex items-center justify-between">
                  <span
                    className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ring-1 ring-inset ${
                      event.status === 'SCHEDULED'
                        ? 'bg-green-400/10 text-green-400 ring-green-400/20'
                        : 'bg-gray-400/10 text-gray-400 ring-gray-400/20'
                    }`}
                  >
                    {event.status}
                  </span>
                  <span className="text-xs text-gray-400">
                    {new Date(event.eventDateTime).toLocaleDateString()}
                  </span>
                </div>
                <h3 className="mt-4 text-xl font-bold text-white">
                  <Link href={`/events/${event.slug}`}>
                    <span className="absolute inset-0" />
                    {event.title}
                  </Link>
                </h3>
                <p className="mt-2 text-sm text-gray-300 line-clamp-2">{event.description}</p>
                <div className="mt-4 flex items-center justify-between text-sm text-gray-400">
                  <span>{event.participantCount} participants</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
