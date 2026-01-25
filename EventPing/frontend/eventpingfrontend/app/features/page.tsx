'use client';

import { PageLayout } from '@/components/layout/PageLayout';
import { Zap, Shield, Users, Bell, BarChart, Globe } from 'lucide-react';

const features = [
  {
    icon: <Zap className="h-6 w-6 text-indigo-400" />,
    title: 'Instant Setup',
    description: 'Create events in seconds with our intuitive interface. No complex configurations needed.',
  },
  {
    icon: <Bell className="h-6 w-6 text-indigo-400" />,
    title: 'Smart Reminders',
    description: 'Automated email and SMS notifications ensure your participants never miss an update.',
  },
  {
    icon: <Users className="h-6 w-6 text-indigo-400" />,
    title: 'Guest Management',
    description: 'Track RSVPs, manage waitlists, and communicate with your attendees effortlessly.',
  },
  {
    icon: <Shield className="h-6 w-6 text-indigo-400" />,
    title: 'Secure & Private',
    description: 'Enterprise-grade security protects your data and your participants information.',
  },
  {
    icon: <BarChart className="h-6 w-6 text-indigo-400" />,
    title: 'Analytics',
    description: 'Gain insights into attendance rates, engagement, and more with detailed reports.',
  },
  {
    icon: <Globe className="h-6 w-6 text-indigo-400" />,
    title: 'Public Pages',
    description: 'Beautiful, SEO-optimized landing pages for your public events automatically generated.',
  },
];

export default function FeaturesPage() {
  return (
    <PageLayout>
      <div className="max-w-7xl mx-auto px-4 py-16 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-white mb-4">Powerful Features for Modern Events</h1>
          <p className="text-xl text-slate-400 max-w-2xl mx-auto">
            Everything you need to organize, manage, and track your events in one place.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature, index) => (
            <div
              key={index}
              className="bg-slate-900/50 backdrop-blur-sm border border-white/10 rounded-2xl p-8 hover:bg-slate-800/50 transition-colors"
            >
              <div className="w-12 h-12 bg-indigo-500/10 rounded-lg flex items-center justify-center mb-6">
                {feature.icon}
              </div>
              <h3 className="text-xl font-semibold text-white mb-3">{feature.title}</h3>
              <p className="text-slate-400 leading-relaxed">{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </PageLayout>
  );
}
