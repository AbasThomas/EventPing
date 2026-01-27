import { DashboardNavbar } from '@/components/dashboard/DashboardNavbar';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen relative z-10 pt-20 pb-10">
      <DashboardNavbar />
      <div className="max-w-7xl mx-auto px-4 md:px-6">
        {children}
      </div>
    </div>
  );
}
