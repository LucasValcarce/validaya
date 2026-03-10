/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        teal: {
          DEFAULT: '#00B896',
          hover:   '#009e80',
          light:   '#E6F7F4',
        },
        navy: {
          DEFAULT: '#0F1F3D',
          light:   '#1A2F52',
        },
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'sans-serif'],
      },
    },
  },
  plugins: [],
}