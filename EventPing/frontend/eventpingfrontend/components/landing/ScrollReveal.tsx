'use client';

import { useEffect } from 'react';

export function ScrollReveal() {
  useEffect(() => {
    const observerOptions = {
      root: null,
      rootMargin: '0px',
      threshold: 0.05,
    };

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('reveal-visible');
          // Optionally stop observing once visible
          observer.unobserve(entry.target);
        }
      });
    }, observerOptions);

    const initReveal = () => {
      const elements = document.querySelectorAll('.reveal');
      elements.forEach((el) => observer.observe(el));
    };

    // Run on mount
    initReveal();

    // Re-run if content changes (optional, but good for SPA)
    const mutationObserver = new MutationObserver(() => {
        initReveal();
    });

    mutationObserver.observe(document.body, { 
        childList: true, 
        subtree: true 
    });

    return () => {
      observer.disconnect();
      mutationObserver.disconnect();
    };
  }, []);

  return null; // This component doesn't render anything
}
