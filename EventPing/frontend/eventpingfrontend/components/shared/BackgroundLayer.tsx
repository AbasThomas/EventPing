'use client';

import { UnicornScript } from '../landing/UnicornScript';

export function BackgroundLayer() {
  return (
    <>
      <div className="fixed inset-0 z-0 pointer-events-none">
        <div className="absolute top-0 w-full h-screen" style={{ maskImage: 'linear-gradient(to bottom, transparent, black 0%, black 80%, transparent)' }}>
            <div className="absolute w-full top-0 saturate-100 h-[800px]" style={{ maskImage: 'linear-gradient(transparent, black 0%, black 80%, transparent)' }}>
                {/* Aura Background */}
                 <div data-us-project="bcBYZIStYXwiogchBNHO" className="absolute top-0 left-0 -z-10 w-full h-full opacity-50"></div>
                <div className="fixed top-0 left-0 right-0 h-[800px] hero-glow pointer-events-none z-0"></div>
                <div className="fixed top-[-200px] left-1/2 -translate-x-1/2 w-[600px] h-[400px] bg-indigo-600/10 blur-[120px] rounded-full pointer-events-none z-0"></div>
            </div>
        </div>
      </div>
      <UnicornScript />
    </>
  );
}
