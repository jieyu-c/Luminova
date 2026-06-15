export type StudioTransitionSnapshot = {
  title: string;
  accent: string;
  origin: 'hero' | 'table' | 'fallback';
  rect: {
    left: number;
    top: number;
    width: number;
    height: number;
  };
};

const STORAGE_KEY = 'luminova:studio-transition';
const TRANSITION_CLASS = 'studio-transition';

function createTransitionMarkup(snapshot: StudioTransitionSnapshot) {
  const overlay = document.createElement('div');
  overlay.className = `${TRANSITION_CLASS} ${TRANSITION_CLASS}--${snapshot.origin}`;
  overlay.setAttribute('aria-hidden', 'true');

  overlay.innerHTML = `
    <div class="studio-transition__wash"></div>
    <div class="studio-transition__grid"></div>
    <div class="studio-transition__beam"></div>
    <div class="studio-transition__portal">
      <div class="studio-transition__portal-map"></div>
      <div class="studio-transition__portal-node studio-transition__portal-node--a"></div>
      <div class="studio-transition__portal-node studio-transition__portal-node--b"></div>
      <div class="studio-transition__portal-node studio-transition__portal-node--c"></div>
      <span></span>
    </div>
    <div class="studio-transition__aperture"></div>
    <div class="studio-transition__label">进入创作台</div>
  `;

  overlay.style.setProperty('--from-left', `${snapshot.rect.left}px`);
  overlay.style.setProperty('--from-top', `${snapshot.rect.top}px`);
  overlay.style.setProperty('--from-width', `${snapshot.rect.width}px`);
  overlay.style.setProperty('--from-height', `${snapshot.rect.height}px`);
  overlay.style.setProperty('--studio-accent', snapshot.accent);
  overlay.querySelector('span')!.textContent = snapshot.title;

  return overlay;
}

export function saveStudioTransition(snapshot: StudioTransitionSnapshot) {
  try {
    window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot));
  } catch {
    // The route-level fallback still animates when session storage is unavailable.
  }
}

export function takeStudioTransition() {
  try {
    const rawSnapshot = window.sessionStorage.getItem(STORAGE_KEY);
    if (!rawSnapshot) return null;

    window.sessionStorage.removeItem(STORAGE_KEY);

    return JSON.parse(rawSnapshot) as StudioTransitionSnapshot;
  } catch {
    return null;
  }
}

export function playStudioTransition(snapshot: StudioTransitionSnapshot) {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return false;

  document.querySelector(`.${TRANSITION_CLASS}`)?.remove();

  const overlay = createTransitionMarkup(snapshot);
  document.body.append(overlay);
  window.setTimeout(() => overlay.remove(), 1120);

  return true;
}
