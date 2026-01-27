export default function PublicEventLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen relative z-10 flex flex-col">
       {children}
    </div>
  );
}
