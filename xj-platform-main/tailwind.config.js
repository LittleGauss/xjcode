// tailwind.config.js
module.exports = {
  purge: ["./src/**/*.{vue,js,ts,jsx,tsx}"], // v2 用 'purge' 而非 'content'
  darkMode: false, // 或 'media'/'class'
  theme: {
    extend: {},
  },
  variants: {
    extend: {},
  },
  plugins: [],
};
