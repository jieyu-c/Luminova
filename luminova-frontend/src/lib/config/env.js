var _a;
var DEFAULT_DEV_GATEWAY_URL = 'http://localhost:8080';
/** Gateway base URL from env; empty in dev means use Vite proxy with relative /api paths. */
export var gatewayUrl = ((_a = import.meta.env.VITE_GATEWAY_URL) !== null && _a !== void 0 ? _a : '').replace(/\/$/, '');
export function resolveApiUrl(path) {
    var normalizedPath = path.startsWith('/') ? path : "/".concat(path);
    return gatewayUrl ? "".concat(gatewayUrl).concat(normalizedPath) : normalizedPath;
}
/** Proxy target for Vite dev server (falls back to local gateway). */
export function getDevGatewayProxyTarget(env) {
    return (env.VITE_GATEWAY_URL || DEFAULT_DEV_GATEWAY_URL).replace(/\/$/, '');
}
