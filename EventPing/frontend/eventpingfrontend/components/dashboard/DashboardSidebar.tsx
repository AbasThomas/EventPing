'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { 
  LayoutDashboard, 
  Calendar, 
  Users, 
  Settings, 
  PlusCircle,
  Bell,
  HelpCircle,
  ChevronRight,
  BarChart3,
  Link2
} from 'lucide-react';
import { useAuth } from '@/lib/auth-context';

export function DashboardSidebar() {
  const pathname = usePathname();
  const { user } = useAuth();

  const menuItems = [
    { name: 'Overview', icon: LayoutDashboard, href: '/dashboard' },
    { name: 'My Events', icon: Calendar, href: '/dashboard/events' },
    { name: 'Registrants', icon: Users, href: '/dashboard/registrants' },
    { name: 'Analytics', icon: BarChart3, href: '/dashboard/analytics' },
    { name: 'Integrations', icon: Link2, href: '/dashboard/integrations' },
    { name: 'Reminders', icon: Bell, href: '/dashboard/reminders' },
  ];

  const secondaryItems = [
    { name: 'Settings', icon: Settings, href: '/dashboard/settings' },
    { name: 'Help Support', icon: HelpCircle, href: '/support' },
  ];

  const isActive = (path: string) => {
    if (path === '/dashboard') return pathname === '/dashboard';
    return pathname?.startsWith(path);
  };

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-slate-950/80 backdrop-blur-xl border-r border-white/5 z-50 hidden lg:flex flex-col">
      <div className="p-6">
        <Link href="/dashboard" className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center">
            <div className="w-2.5 h-2.5 bg-white rounded-full"></div>
          </div>
          <span className="font-bold tracking-tight text-white text-lg">EventPing</span>
        </Link>
      </div>

      <div className="flex-1 px-4 py-4 space-y-8 overflow-y-auto">
        <div>
          <p className="px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">
            Main Menu
          </p>
          <nav className="space-y-1">
            {menuItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center justify-between px-4 py-3 rounded-xl transition-all group ${
                  isActive(item.href)
                    ? 'bg-indigo-500/10 text-indigo-400'
                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                }`}
              >
                <div className="flex items-center gap-3">
                  <item.icon className={`w-5 h-5 ${isActive(item.href) ? 'text-indigo-400' : 'group-hover:text-white transition-colors'}`} />
                  <span className="font-medium">{item.name}</span>
                </div>
                {isActive(item.href) && (
                  <div className="w-1.5 h-1.5 rounded-full bg-indigo-500"></div>
                )}
              </Link>
            ))}
          </nav>
        </div>

        <div>
          <p className="px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">
            Actions
          </p>
          <Link
            href="/dashboard/events/create"
            className="flex items-center gap-3 px-4 py-3 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 transition-all group"
          >
            <PlusCircle className="w-5 h-5 group-hover:text-indigo-400 transition-colors" />
            <span className="font-medium">Create Event</span>
          </Link>
        </div>

        <div>
          <p className="px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">
            System
          </p>
          <nav className="space-y-1">
            {secondaryItems.map((item) => (
              <Link
                key={item.name}
                href={item.href}
                className={`flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${
                  isActive(item.href)
                    ? 'bg-indigo-500/10 text-indigo-400'
                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                }`}
              >
                <item.icon className="w-5 h-5" />
                <span className="font-medium">{item.name}</span>
              </Link>
            ))}
          </nav>
        </div>
      </div>

      <div className="p-4 mt-auto">
        <div className="p-4 rounded-2xl bg-slate-900 border border-white/5">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 font-bold border border-white/10">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="overflow-hidden">
              <p className="text-sm font-semibold text-white truncate">{user?.fullName || 'User'}</p>
              <p className="text-xs text-slate-500 truncate">{user?.email || 'user@example.com'}</p>
            </div>
          </div>
          <div className="flex items-center justify-between text-xs text-slate-400 bg-white/5 p-2 rounded-lg">
            <span>Role: {user?.role || 'User'}</span>
            <ChevronRight className="w-3 h-3" />
          </div>
        </div>
      </div>
    </aside>
  );
}
