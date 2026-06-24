const DEFAULT_DEV_GATEWAY_URL = 'http://localhost:8080';

/** Gateway base URL from env; empty in dev means use Vite proxy with relative /api paths. */
export const gatewayUrl = (import.meta.env.VITE_GATEWAY_URL ?? '').replace(/\/$/, '');

export function resolveApiUrl(path: string): string {
  const normalizedPath = path.charAt(0) === '/' ? path : `/${path}`;
  return gatewayUrl ? `${gatewayUrl}${normalizedPath}` : normalizedPath;
}

export { DEFAULT_DEV_GATEWAY_URL };
