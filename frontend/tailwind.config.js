/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: { extend: { colors: { ink: '#182230', navy: '#16324F', teal: '#0B8A84', mist: '#F5F8FA', amber: '#E9A23B' }, fontFamily: { sans: ['Inter', 'ui-sans-serif', 'system-ui'] } } },
  plugins: [],
}
