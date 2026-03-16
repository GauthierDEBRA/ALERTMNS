import { ref, watchEffect } from 'vue'

function getInitialTheme() {
  const stored = localStorage.getItem('theme')
  if (stored) return stored === 'dark'
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false
}

const isDark = ref(getInitialTheme())

function applyTheme(dark) {
  document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light')
  localStorage.setItem('theme', dark ? 'dark' : 'light')
}

// Apply immediately on import
applyTheme(isDark.value)

watchEffect(() => applyTheme(isDark.value))

export function useDarkMode() {
  function toggleDark() {
    isDark.value = !isDark.value
  }
  return { isDark, toggleDark }
}
