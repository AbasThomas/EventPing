'use client';

import { useState, useEffect, use } from 'react';
import { apiFetch } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { useRouter } from 'next/navigation';

interface Event {
  id: number;
  title: string;
  description: string;
  eventDateTime: string;
  status: string;
  slug: string;
  creatorEmail: string;
  participantCount: number;
}

export default function EventPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params);
  const [event, setEvent] = useState<Event | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [joinEmail, setJoinEmail] = useState('');
  const [isJoining, setIsJoining] = useState(false);
  const [joinMessage, setJoinMessage] = useState('');
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

    try {
      await apiFetch(`/participants/events/${slug}/join`, {
        method: 'POST',
        body: JSON.stringify({ email: joinEmail }),
      });
      setJoinMessage('Successfully joined the event!');
      // Refresh event to update count
      const data = await apiFetch(`/events/${slug}`);
      setEvent(data);
    } catch (err: any) {
      setJoinMessage(err.message || 'Failed to join event');
    } finally {
      setIsJoining(false);
    }
  };

  if (isLoading) return <div className="p-24 text-center">Loading event...</div>;
  if (!event) return <div className="p-24 text-center">Event not found</div>;

  const isCreator = user?.email === event.creatorEmail;

  return (
    <div className="min-h-screen bg-background p-8 md:p-24">
      <div className="max-w-4xl mx-auto bg-gray-900 rounded-2xl shadow-xl overflow-hidden border border-gray-800">
        <div className="p-8 md:p-12">
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-3xl md:text-4xl font-bold text-white mb-4">{event.title}</h1>
              <div className="flex items-center space-x-4 text-gray-400 mb-6">
                <span className="flex items-center">
                  <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  {new Date(event.eventDateTime).toLocaleString()}
                </span>
                <span className="flex items-center">
                  <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                  {event.participantCount} Participants
                </span>
              </div>
            </div>
            {isCreator && (
              <span className="bg-indigo-500/10 text-indigo-400 px-3 py-1 rounded-full text-sm font-medium border border-indigo-500/20">
                You are the host
              </span>
            )}
          </div>

          <div className="prose prose-invert max-w-none mb-12">
            <p className="text-gray-300 whitespace-pre-wrap">{event.description}</p>
          </div>

          <div className="border-t border-gray-800 pt-8">
            <h3 className="text-xl font-semibold text-white mb-6">Join this Event</h3>
            <form onSubmit={handleJoin} className="max-w-md">
              <div className="flex gap-4">
                <input
                  type="email"
                  required
                  placeholder="Enter your email"
                  className="flex-1 rounded-md border-0 bg-gray-800 py-2.5 text-white shadow-sm ring-1 ring-inset ring-gray-700 placeholder:text-gray-500 focus:ring-2 focus:ring-inset focus:ring-indigo-500 sm:text-sm sm:leading-6 px-3"
                  value={joinEmail}
                  onChange={(e) => setJoinEmail(e.target.value)}
                  disabled={!!user} // If logged in, email is fixed
                />
                <button
                  type="submit"
                  disabled={isJoining}
                  className="rounded-md bg-indigo-600 px-6 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isJoining ? 'Joining...' : 'Join Now'}
                </button>
              </div>
              {joinMessage && (
                <p className={`mt-4 text-sm ${joinMessage.includes('Success') ? 'text-green-400' : 'text-red-400'}`}>
                  {joinMessage}
                </p>
              )}
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
