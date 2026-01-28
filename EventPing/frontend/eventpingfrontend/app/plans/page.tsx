'use client';

import { PageLayout } from '@/components/layout/PageLayout';
import { Check, X } from 'lucide-react';
import Link from 'next/link';

const plans = [
  {
    name: 'Free',
    price: '$0',
    period: '/month',
    description: 'Perfect for small personal events and casual gatherings.',
    features: [
      '100 Monthly Credits',
      '3 events per day',
      '20 participants per event',
      'Email reminders',
      'Basic RSVP tracking',
    ],
    notIncluded: [
      'Telegram & WhatsApp',
      'Custom intervals',
      'Analytics & Teams',
    ],
    cta: 'Get Started',
    href: '/auth/register',
    popular: false,
  },
  {
    name: 'Basic',
    price: '$5',
    period: '/month',
    description: 'Entry-level tools for growing event needs.',
    features: [
      '1,000 Monthly Credits',
      '10 events per day',
      '100 participants per event',
      'Email & Telegram',
      'Custom intervals',
    ],
    notIncluded: [
      'WhatsApp & Discord',
      'Advanced Analytics',
      'Team Members',
    ],
    cta: 'Start Basic',
    href: '/auth/register?plan=basic',
    popular: false,
  },
  {
    name: 'Pro',
    price: '$15',
    period: '/month',
    description: 'Our most popular plan for power users.',
    features: [
      '5,000 Monthly Credits',
      '50 events per day',
      '500 participants per event',
      'WhatsApp reminders',
      'Advanced RSVP & Analytics',
    ],
    notIncluded: [
      'Discord support',
      'Custom branding',
      'Team Members',
    ],
    cta: 'Go Pro',
    href: '/auth/register?plan=pro',
    popular: true,
  },
  {
    name: 'Business',
    price: '$45',
    period: '/month',
    description: 'Built for teams and large-scale operations.',
    features: [
      '25,000 Monthly Credits',
      'Unlimited events',
      '2,000 participants per event',
      'All notification channels',
      'Up to 5 Team Members',
      'Custom branding',
    ],
    notIncluded: [
      'Dedicated manager',
    ],
    cta: 'Start Business',
    href: '/auth/register?plan=business',
    popular: false,
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    period: '',
    description: 'Tailored solutions for large organizations.',
    features: [
      'Unlimited Credits',
      'Unlimited everything',
      'White-label domain',
      'API & Webhook access',
      'Dedicated account manager',
      'SLA guarantee',
    ],
    notIncluded: [],
    cta: 'Contact Sales',
    href: '/support',
    popular: false,
  },
];

export default function PlansPage() {
  return (
    <PageLayout>
      <div className="max-w-7xl mx-auto px-4 py-16 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-white mb-4">Simple, Transparent Pricing</h1>
          <p className="text-xl text-slate-400 max-w-2xl mx-auto">
            Choose the perfect plan for your event planning needs. No hidden fees, cancel anytime.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6">
          {plans.map((plan) => (
            <div
              key={plan.name}
              className={`relative rounded-2xl p-8 border ${
                plan.popular
                  ? 'bg-indigo-900/20 border-indigo-500/50 shadow-[0_0_30px_-5px_rgba(99,102,241,0.3)]'
                  : 'bg-slate-900/50 border-white/10'
              } backdrop-blur-sm flex flex-col`}
            >
              {plan.popular && (
                <div className="absolute top-0 right-0 -translate-y-1/2 translate-x-1/4">
                  <span className="bg-indigo-500 text-white text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider shadow-lg">
                    Most Popular
                  </span>
                </div>
              )}

              <div className="mb-8">
                <h3 className="text-xl font-semibold text-white mb-2">{plan.name}</h3>
                <div className="flex items-baseline mb-4">
                  <span className="text-4xl font-bold text-white">{plan.price}</span>
                  <span className="text-slate-400 ml-1">{plan.period}</span>
                </div>
                <p className="text-slate-400 text-sm">{plan.description}</p>
              </div>

              <div className="flex-1 space-y-4 mb-8">
                {plan.features.map((feature) => (
                  <div key={feature} className="flex items-start">
                    <Check className="h-5 w-5 text-indigo-400 shrink-0 mr-3" />
                    <span className="text-slate-300 text-sm">{feature}</span>
                  </div>
                ))}
                {plan.notIncluded.map((feature) => (
                  <div key={feature} className="flex items-start opacity-50">
                    <X className="h-5 w-5 text-slate-600 shrink-0 mr-3" />
                    <span className="text-slate-500 text-sm">{feature}</span>
                  </div>
                ))}
              </div>

              <Link
                href={plan.href}
                className={`w-full py-3 px-4 rounded-lg text-center font-medium transition-all ${
                  plan.popular
                    ? 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg hover:shadow-indigo-500/25'
                    : 'bg-white/10 hover:bg-white/20 text-white'
                }`}
              >
                {plan.cta}
              </Link>
            </div>
          ))}
        </div>
      </div>
    </PageLayout>
  );
}
