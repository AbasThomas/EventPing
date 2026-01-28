import { DashboardNavbar } from '@/components/dashboard/DashboardNavbar';
import { DashboardSidebar } from '@/components/dashboard/DashboardSidebar';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-slate-950/20">
      <DashboardSidebar />
      <div className="lg:pl-64 flex flex-col min-h-screen">
        <DashboardNavbar />
        <main className="flex-1 pt-24 pb-12">
          <div className="max-w-7xl mx-auto px-4 md:px-8">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
