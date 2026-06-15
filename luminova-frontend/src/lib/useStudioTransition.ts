import type { MouseEvent, PointerEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  playStudioTransition,
  saveStudioTransition,
  type StudioTransitionSnapshot,
} from './studioTransition';

type TransitionTarget = {
  id: string;
  title: string;
  accent: string;
};

export function useStudioTransition() {
  const navigate = useNavigate();

  const createSnapshot = (
    element: HTMLElement,
    project: TransitionTarget,
  ): StudioTransitionSnapshot | null => {
    const cover = element.querySelector<HTMLElement>('.continue-cover, .ws-thumb') ?? element;
    const origin = cover.classList.contains('continue-cover') ? 'hero' : 'table';

    const rect = cover.getBoundingClientRect();
    return {
      title: project.title,
      accent: project.accent,
      origin,
      rect: {
        left: rect.left,
        top: rect.top,
        width: rect.width,
        height: rect.height,
      },
    };
  };

  const enterStudio = (event: MouseEvent<HTMLAnchorElement>, project: TransitionTarget) => {
    const snapshot = createSnapshot(event.currentTarget, project);
    if (!snapshot) return;

    event.preventDefault();
    const enterUrl = `/canvas?studio=${project.id}&enter=${Date.now()}`;
    saveStudioTransition(snapshot);
    const playedStudioTransition = playStudioTransition(snapshot);
    window.setTimeout(
      () => navigate(enterUrl, { state: { studioTransition: snapshot, playedStudioTransition } }),
      playedStudioTransition ? 520 : 0,
    );
  };

  const captureTransition = (
    event: PointerEvent<HTMLAnchorElement>,
    project: TransitionTarget,
  ) => {
    const snapshot = createSnapshot(event.currentTarget, project);
    if (snapshot) saveStudioTransition(snapshot);
  };

  return { enterStudio, captureTransition };
}
