import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        ui: ['Manrope', 'PingFang SC', 'Microsoft YaHei', 'sans-serif'],
        display: ['Syne', 'PingFang SC', 'sans-serif'],
      },
      colors: {
        luminova: {
          ink: '#0c1628',
          blue: '#2563eb',
          cyan: '#06b6d4',
          violet: '#6366f1',
        },
      },
    },
  },
  plugins: [],
} satisfies Config;
