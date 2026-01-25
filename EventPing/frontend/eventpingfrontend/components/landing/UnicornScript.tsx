'use client';

import Script from 'next/script';

export function UnicornScript() {
  return (
    <Script
      src="https://cdn.jsdelivr.net/gh/hiunicornstudio/unicornstudio.js@v1.4.29/dist/unicornStudio.umd.js"
      onLoad={() => {
        // @ts-ignore
        if (!window.UnicornStudio) {
          // @ts-ignore
          window.UnicornStudio = { isInitialized: false };
        }
        // @ts-ignore
        if (!window.UnicornStudio.isInitialized) {
          // @ts-ignore
          UnicornStudio.init();
          // @ts-ignore
          window.UnicornStudio.isInitialized = true;
        }
      }}
    />
  );
}
