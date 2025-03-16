module.exports = {
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/resources/static/js/**/*.js'
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Gabarito', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      colors: {
        primary: '#4f46e5',    // indigo-600
        secondary: '#0ea5e9',  // sky-500
        accent: '#f59e0b',     // amber-500
        neutral: '#f9fafb',    // gray-50
        surface: '#ffffff',    // white
        text: '#1f2937',       // gray-800
        error: '#ef4444',      // red-500
        success: '#10b981',    // emerald-500
        info: '#3b82f6',       // blue-500
        warning: '#f59e0b',    // amber-500
      },
    },
  },
  plugins: [],
}
