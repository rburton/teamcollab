module.exports = {
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/resources/static/js/**/*.js'
  ],
  theme: {
    extend: {
      colors: {
        primary: '#2563eb',    // blue-600
        secondary: '#14b8a6',  // teal-500
        accent: '#facc15',     // yellow-400
        neutral: '#f3f4f6',    // gray-100
        text: '#1f2937',       // gray-800
        error: '#ef4444',      // red-500
      },
    },
  },
  plugins: [],
}