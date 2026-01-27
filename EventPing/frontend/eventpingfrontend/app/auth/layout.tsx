export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center p-6 md:p-24 relative z-10">
      <div className="w-full max-w-md">
        {children}
      </div>
    </div>
  );
}
