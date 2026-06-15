import { useEffect } from 'react';

export function usePointerGlow(selector: string) {
  useEffect(() => {
    const elements = document.querySelectorAll<HTMLElement>(selector);
    const handlers = new Map<HTMLElement, (event: PointerEvent) => void>();

    elements.forEach((el) => {
      const handler = (event: PointerEvent) => {
        const rect = el.getBoundingClientRect();
        el.style.setProperty('--mx', `${event.clientX - rect.left}px`);
        el.style.setProperty('--my', `${event.clientY - rect.top}px`);
      };
      handlers.set(el, handler);
      el.addEventListener('pointermove', handler);
    });

    return () => {
      handlers.forEach((handler, el) => {
        el.removeEventListener('pointermove', handler);
      });
    };
  }, [selector]);
}
