/** Gateway base URL from env; empty in dev means use Vite proxy with relative /api paths. */
export declare const gatewayUrl: any;
export declare function resolveApiUrl(path: string): string;
/** Proxy target for Vite dev server (falls back to local gateway). */
export declare function getDevGatewayProxyTarget(env: Record<string, string>): string;
