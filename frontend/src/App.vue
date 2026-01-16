<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue';

onMounted(() => {
  // Initialize theme from localStorage or system preference
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme) {
    document.documentElement.setAttribute('data-theme', savedTheme);
  } else if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
    document.documentElement.setAttribute('data-theme', 'dark');
  }
});
</script>

<style>
:root {
  /* Light Theme Variables */
  --bg-color: #f5f6fa;
  --sidebar-bg: #ffffff;
  --chat-bg: #f5f6fa;
  --input-bg: #ffffff;
  --text-color: #333333;
  --text-secondary: #666666;
  --border-color: #e5e6eb;
  --accent-color: #4b7bec;
  --user-msg-bg: #4b7bec;
  --user-msg-text: #ffffff;
  --ai-msg-bg: transparent;
  --ai-msg-text: #333333;
  --hover-bg: #f0f2f5;
  --shadow-color: rgba(0, 0, 0, 0.05);
  --code-bg: #f6f8fa;
}

[data-theme='dark'] {
  /* Dark Theme Variables (DeepSeek Style) */
  --bg-color: #1e1e1e;
  --sidebar-bg: #101010;
  --chat-bg: #1e1e1e;
  --input-bg: #2d2d2d;
  --text-color: #ececec;
  --text-secondary: #a0a0a0;
  --border-color: #333333;
  --accent-color: #4d88ff;
  --user-msg-bg: #2d2d2d;
  --user-msg-text: #ececec;
  --ai-msg-bg: transparent;
  --ai-msg-text: #ececec;
  --hover-bg: #2d2d2d;
  --shadow-color: rgba(0, 0, 0, 0.3);
  --code-bg: #2d2d2d;
}

body {
  margin: 0;
  background: var(--bg-color);
  color: var(--text-color);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  transition: background-color 0.3s, color 0.3s;
}

/* Scrollbar Styles */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}
</style>
