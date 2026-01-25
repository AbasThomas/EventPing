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
      'Up to 5 active events',
      '50 participants per event',
      'Basic email reminders',
      'Public event pages',
      'RSVP tracking',
    ],
    notIncluded: [
      'SMS reminders',
      'Custom branding',
      'Priority support',
      'Analytics dashboard',
    ],
    cta: 'Get Started',
    href: '/auth/register',
    popular: false,
  },
  {
    name: 'Pro',
    price: '$12',
    period: '/month',
    description: 'For power users who organize frequent or larger events.',
    features: [
      'Unlimited active events',
      '500 participants per event',
      'Email & SMS reminders',
      'Custom branding',
      'RSVP tracking',
      'Advanced analytics',
      'Priority support',
    ],
    notIncluded: [
      'White-label domain',
    ],
    cta: 'Start Pro Trial',
    href: '/auth/register?plan=pro',
    popular: true,
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    period: '',
    description: 'Tailored solutions for large organizations and event planners.',
    features: [
      'Unlimited everything',
      'White-label domain',
      'Dedicated account manager',
      'SLA guarantee',
      'Custom integration API',
      'SSO Authentication',
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

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
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
