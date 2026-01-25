'use client';

import { PageLayout } from '@/components/layout/PageLayout';
import { Mail, MessageCircle, HelpCircle, FileQuestion } from 'lucide-react';

export default function SupportPage() {
  return (
    <PageLayout>
      <div className="max-w-7xl mx-auto px-4 py-16 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-white mb-4">How can we help you?</h1>
          <p className="text-xl text-slate-400 max-w-2xl mx-auto">
            Our team is here to assist you with any questions or issues you may have.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-16">
          <div className="bg-slate-900/50 backdrop-blur-sm border border-white/10 rounded-2xl p-8 text-center hover:-translate-y-1 transition-transform duration-300">
            <div className="w-16 h-16 bg-indigo-500/10 rounded-full flex items-center justify-center mx-auto mb-6">
              <Mail className="h-8 w-8 text-indigo-400" />
            </div>
            <h3 className="text-xl font-semibold text-white mb-2">Email Support</h3>
            <p className="text-slate-400 mb-6">Get a response within 24 hours.</p>
            <a href="mailto:support@eventping.com" className="text-indigo-400 hover:text-indigo-300 font-medium">support@eventping.com</a>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm border border-white/10 rounded-2xl p-8 text-center hover:-translate-y-1 transition-transform duration-300">
            <div className="w-16 h-16 bg-indigo-500/10 rounded-full flex items-center justify-center mx-auto mb-6">
              <MessageCircle className="h-8 w-8 text-indigo-400" />
            </div>
            <h3 className="text-xl font-semibold text-white mb-2">Live Chat</h3>
            <p className="text-slate-400 mb-6">Available Mon-Fri, 9am - 5pm EST.</p>
            <button className="bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-2 rounded-full font-medium transition-colors">
              Start Chat
            </button>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm border border-white/10 rounded-2xl p-8 text-center hover:-translate-y-1 transition-transform duration-300">
            <div className="w-16 h-16 bg-indigo-500/10 rounded-full flex items-center justify-center mx-auto mb-6">
              <HelpCircle className="h-8 w-8 text-indigo-400" />
            </div>
            <h3 className="text-xl font-semibold text-white mb-2">Help Center</h3>
            <p className="text-slate-400 mb-6">Browse tutorials and FAQs.</p>
            <a href="/docs" className="text-indigo-400 hover:text-indigo-300 font-medium">Visit Help Center</a>
          </div>
        </div>

        <div className="max-w-3xl mx-auto">
          <h2 className="text-2xl font-bold text-white mb-8">Frequently Asked Questions</h2>
          <div className="space-y-6">
            {[
              { q: 'How do I create an event?', a: 'Sign up for an account, go to your dashboard, and click the "Create Event" button. Follow the simple steps to set up your event details.' },
              { q: 'Is EventPing free to use?', a: 'Yes, we offer a generous free plan for small events. For larger events and advanced features, check out our Pro plans.' },
              { q: 'Can I customize the event page?', a: 'Absolutely! You can add your own description, location details, and more. Pro users get additional branding options.' },
              { q: 'How do reminders work?', a: 'We automatically send email reminders to your participants before the event starts. You can configure the timing in your event settings.' },
            ].map((faq, index) => (
              <div key={index} className="bg-slate-900/30 border border-white/5 rounded-xl p-6">
                <h3 className="text-lg font-semibold text-white mb-2 flex items-start">
                  <FileQuestion className="h-5 w-5 text-indigo-400 mr-3 shrink-0 mt-0.5" />
                  {faq.q}
                </h3>
                <p className="text-slate-400 ml-8">{faq.a}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </PageLayout>
  );
}
