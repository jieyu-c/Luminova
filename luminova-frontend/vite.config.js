import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
var DEFAULT_DEV_GATEWAY_URL = 'http://localhost:8080';
function getDevGatewayProxyTarget(env) {
    return (env.VITE_GATEWAY_URL || DEFAULT_DEV_GATEWAY_URL).replace(/\/$/, '');
}
export default defineConfig(function (_a) {
    var mode = _a.mode;
    var env = loadEnv(mode, process.cwd(), '');
    return {
        plugins: [react()],
        server: {
            port: 5173,
            proxy: {
                '/api': {
                    target: getDevGatewayProxyTarget(env),
                    changeOrigin: true,
                },
            },
        },
    };
});
