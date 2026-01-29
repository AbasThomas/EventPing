'use client';

import { useState } from 'react';
import { 
  Mail, 
  MessageSquare, 
  Send, 
  MessageCircle, 
  Slack, 
  Smartphone,
  Lock,
  Check
} from 'lucide-react';

interface Integration {
  id: string;
  name: string;
  icon: React.ComponentType<{ className?: string }>;
  color: string;
  bg: string;
  borderColor: string;
}

const AVAILABLE_INTEGRATIONS: Integration[] = [
  {
    id: 'EMAIL',
    name: 'Email',
    icon: Mail,
    color: 'text-blue-400',
    bg: 'bg-blue-400/10',
    borderColor: 'border-blue-400/30'
  },
  {
    id: 'WHATSAPP',
    name: 'WhatsApp',
    icon: MessageSquare,
    color: 'text-green-400',
    bg: 'bg-green-400/10',
    borderColor: 'border-green-400/30'
  },
  {
    id: 'TELEGRAM',
    name: 'Telegram',
    icon: Send,
    color: 'text-cyan-400',
    bg: 'bg-cyan-400/10',
    borderColor: 'border-cyan-400/30'
  },
  {
    id: 'DISCORD',
    name: 'Discord',
    icon: MessageCircle,
    color: 'text-indigo-400',
    bg: 'bg-indigo-400/10',
    borderColor: 'border-indigo-400/30'
  },
  {
    id: 'SLACK',
    name: 'Slack',
    icon: Slack,
    color: 'text-purple-400',
    bg: 'bg-purple-400/10',
    borderColor: 'border-purple-400/30'
  },
  {
    id: 'SMS',
    name: 'SMS',
    icon: Smartphone,
    color: 'text-orange-400',
    bg: 'bg-orange-400/10',
    borderColor: 'border-orange-400/30'
  }
];

interface IntegrationSelectorProps {
  userPlanChannels: string; // e.g., "EMAIL,WHATSAPP"
  selectedIntegrations: string[];
  onChange: (integrations: string[]) => void;
}

export function IntegrationSelector({ 
  userPlanChannels, 
  selectedIntegrations, 
  onChange 
}: IntegrationSelectorProps) {
  const allowedChannels = userPlanChannels ? userPlanChannels.split(',').map(c => c.trim()) : ['EMAIL'];

  const toggleIntegration = (integrationId: string) => {
    if (!allowedChannels.includes(integrationId)) {
      return; // Don't allow selecting locked integrations
    }

    if (selectedIntegrations.includes(integrationId)) {
      onChange(selectedIntegrations.filter(id => id !== integrationId));
    } else {
      onChange([...selectedIntegrations, integrationId]);
    }
  };

  return (
    <div className="space-y-4">
      <div>
        <h3 className="text-lg font-semibold text-white mb-1">Reminder Integrations</h3>
        <p className="text-sm text-slate-400">
          Select platforms where participants will receive event reminders
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {AVAILABLE_INTEGRATIONS.map((integration) => {
          const isAllowed = allowedChannels.includes(integration.id);
          const isSelected = selectedIntegrations.includes(integration.id);
          const Icon = integration.icon;

          return (
            <button
              key={integration.id}
              type="button"
              onClick={() => toggleIntegration(integration.id)}
              disabled={!isAllowed}
              className={`
                relative p-4 rounded-xl border-2 transition-all text-left
                ${isSelected && isAllowed
                  ? `${integration.borderColor} ${integration.bg}` 
                  : 'border-slate-800 bg-slate-900/30'
                }
                ${isAllowed 
                  ? 'cursor-pointer hover:border-slate-700' 
                  : 'cursor-not-allowed opacity-50'}
              `}
            >
              <div className="flex items-start justify-between gap-3">
                <div className={`p-2 rounded-lg ${integration.bg} shrink-0`}>
                  <Icon className={`w-5 h-5 ${integration.color}`} />
                </div>
                
                <div className="flex-1 min-w-0">
                  <h4 className="text-white font-medium text-sm">{integration.name}</h4>
                </div>

                {!isAllowed && (
                  <Lock className="w-4 h-4 text-slate-600 shrink-0" />
                )}
                
                {isSelected && isAllowed && (
                  <div className="shrink-0">
                    <div className={`w-5 h-5 rounded-full ${integration.bg} ${integration.borderColor} border flex items-center justify-center`}>
                      <Check className={`w-3 h-3 ${integration.color}`} />
                    </div>
                  </div>
                )}
              </div>

              {!isAllowed && (
                <div className="mt-2 text-xs text-slate-500">
                  Upgrade to unlock
                </div>
              )}
            </button>
          );
        })}
      </div>

      {selectedIntegrations.length === 0 && (
        <p className="text-sm text-amber-400/80 bg-amber-500/10 border border-amber-500/20 rounded-lg p-3">
          Please select at least one integration platform
        </p>
      )}
    </div>
  );
}
