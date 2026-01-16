<template>
  <div class="chat-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: isSidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo" v-if="!isSidebarCollapsed">DeepChat</div>
        <button class="icon-btn" @click="toggleSidebar">
          <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
        </button>
      </div>
      
      <div class="new-chat-btn" @click="newConversation">
        <span v-if="!isSidebarCollapsed">New Chat</span>
        <span v-else>+</span>
      </div>

      <div class="conversation-list">
        <div 
          v-for="c in conversations" 
          :key="c.id" 
          class="conversation-item"
          :class="{ active: activeId === c.id }"
          @click="switchConversation(c.id)"
        >
          <span class="title">{{ c.title }}</span>
        </div>
      </div>
      
      <div class="sidebar-footer">
        <button class="icon-btn theme-toggle" @click="toggleTheme" title="Toggle Theme">
          <span v-if="isDark">🌞</span>
          <span v-else>🌙</span>
        </button>
      </div>
    </aside>

    <!-- Main Chat Area -->
    <main class="chat-main">
      <header class="chat-header">
         <div class="header-title">{{ activeConversationTitle }}</div>
      </header>

      <div class="messages-container" ref="scrollRef">
        <div v-if="messages.length === 0" class="empty-state">
          <h2>How can I help you today?</h2>
        </div>
        
        <div v-for="(m, index) in messages" :key="index" :class="['message-row', m.role]">
          <div class="avatar">
            <div v-if="m.role === 'user'" class="avatar-icon user">U</div>
            <div v-else class="avatar-icon ai">AI</div>
          </div>
          <div class="message-content">
            <div v-if="m.role === 'user'" class="user-bubble">{{ m.content }}</div>
            <div v-else class="ai-text">
              <div class="markdown-body" v-html="renderMarkdown(m.content)"></div>
              <span v-if="m.streaming" class="cursor"></span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-wrapper">
        <div class="input-box">
          <textarea 
            v-model="input" 
            placeholder="Message DeepChat..." 
            @keydown.enter.exact.prevent="onSend"
            @input="adjustTextareaHeight"
            rows="1"
            ref="textareaRef"
          ></textarea>
          <button class="send-btn" :disabled="!input.trim() || sending" @click="onSend">
            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from 'vue';
import { listConversations, createConversation, listMessages } from '../api/chat';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark.css'; // Dark theme for code blocks

// Markdown Setup
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value;
      } catch (__) {}
    }
    return ''; // use external default escaping
  }
});

const renderMarkdown = (content) => {
  return md.render(content || '');
};

// State
const conversations = ref([]);
const messages = ref([]);
const activeId = ref(null);
const input = ref('');
const sending = ref(false);
const scrollRef = ref();
const textareaRef = ref();
const isSidebarCollapsed = ref(false);
const isDark = ref(false);

const activeConversationTitle = computed(() => {
  const c = conversations.value.find(c => c.id === activeId.value);
  return c ? c.title : '';
});

// Theme Logic
const toggleTheme = () => {
  isDark.value = !isDark.value;
  const theme = isDark.value ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('theme', theme);
};

// Layout Logic
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
};

const adjustTextareaHeight = () => {
  const el = textareaRef.value;
  if (el) {
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 200) + 'px';
  }
};

// Chat Logic
const loadConversations = async () => {
  try {
    const res = await listConversations();
    conversations.value = res.data;
    if (!activeId.value && conversations.value.length) {
      activeId.value = conversations.value[0].id;
      await loadMessages();
    }
  } catch (error) {
    console.error('Failed to load conversations', error);
  }
};

const loadMessages = async () => {
  if (!activeId.value) return;
  try {
    const res = await listMessages(activeId.value);
    messages.value = res.data;
    await nextTick();
    scrollToBottom();
  } catch (error) {
    console.error('Failed to load messages', error);
  }
};

const newConversation = async () => {
  const title = `New Chat ${conversations.value.length + 1}`;
  try {
    const res = await createConversation({ title });
    conversations.value.unshift(res.data);
    activeId.value = res.data.id;
    messages.value = [];
  } catch (e) {
    console.error(e);
  }
};

const switchConversation = async (id) => {
  activeId.value = id;
  await loadMessages();
};

const scrollToBottom = () => {
  if (scrollRef.value) {
    scrollRef.value.scrollTop = scrollRef.value.scrollHeight;
  }
};

const onSend = async () => {
  if (!input.value.trim() || !activeId.value || sending.value) return;
  
  const content = input.value;
  input.value = '';
  if (textareaRef.value) textareaRef.value.style.height = 'auto';
  
  // Add User Message
  messages.value.push({ role: 'user', content });
  
  // Add Placeholder AI Message
  const aiMsgIndex = messages.value.length;
  messages.value.push({ role: 'assistant', content: '', streaming: true });
  
  await nextTick();
  scrollToBottom();
  sending.value = true;

  try {
    // Use Fetch API for Streaming
    const response = await fetch(`/api/chat/${activeId.value}/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ content })
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      
      const chunk = decoder.decode(value, { stream: true });
      messages.value[aiMsgIndex].content += chunk;
      scrollToBottom();
    }
  } catch (error) {
    console.error('Stream error:', error);
    messages.value[aiMsgIndex].content += "\n[Error generating response]";
  } finally {
    messages.value[aiMsgIndex].streaming = false;
    sending.value = false;
  }
};

onMounted(async () => {
  // Check theme
  const savedTheme = localStorage.getItem('theme');
  isDark.value = savedTheme === 'dark' || (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches);
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light');

  await loadConversations();
});
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-color);
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 260px;
  background-color: var(--sidebar-bg);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: 60px;
}
.sidebar-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo {
  font-weight: bold;
  font-size: 1.2rem;
}
.icon-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.icon-btn:hover {
  background-color: var(--hover-bg);
  color: var(--text-color);
}
.new-chat-btn {
  margin: 0 12px 12px;
  padding: 10px;
  background-color: var(--hover-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.new-chat-btn:hover {
  background-color: var(--border-color);
}
.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px;
}
.conversation-item {
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text-secondary);
  transition: background 0.2s;
}
.conversation-item:hover {
  background-color: var(--hover-bg);
  color: var(--text-color);
}
.conversation-item.active {
  background-color: var(--hover-bg);
  color: var(--text-color);
  font-weight: 500;
}
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
}

/* Main Chat */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  background-color: var(--chat-bg);
}
.chat-header {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  /* border-bottom: 1px solid var(--border-color); */
}
.header-title {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-secondary);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.message-row {
  width: 100%;
  max-width: 800px;
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}
.message-row.user {
  flex-direction: row-reverse;
}
.avatar-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}
.avatar-icon.user {
  background-color: var(--user-msg-bg);
  color: #fff;
}
.avatar-icon.ai {
  background-color: var(--accent-color);
  color: #fff;
}
.message-content {
  flex: 1;
  max-width: 80%;
}
.message-row.user .message-content {
  text-align: right;
}
.user-bubble {
  background-color: var(--user-msg-bg);
  color: var(--user-msg-text);
  padding: 10px 16px;
  border-radius: 12px;
  display: inline-block;
  white-space: pre-wrap;
  text-align: left;
}
.ai-text {
  color: var(--ai-msg-text);
  line-height: 1.6;
}
.cursor {
  display: inline-block;
  width: 6px;
  height: 18px;
  background-color: var(--text-color);
  margin-left: 2px;
  animation: blink 1s infinite;
  vertical-align: middle;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.empty-state {
  margin-top: 20vh;
  text-align: center;
  color: var(--text-secondary);
}

/* Input Area */
.input-wrapper {
  padding: 24px;
  display: flex;
  justify-content: center;
  background: linear-gradient(to top, var(--bg-color) 80%, transparent);
}
.input-box {
  width: 100%;
  max-width: 800px;
  background-color: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 12px 16px;
  display: flex;
  align-items: flex-end;
  box-shadow: 0 4px 12px var(--shadow-color);
  transition: border-color 0.2s;
}
.input-box:focus-within {
  border-color: var(--accent-color);
}
textarea {
  flex: 1;
  background: transparent;
  border: none;
  resize: none;
  outline: none;
  color: var(--text-color);
  font-family: inherit;
  font-size: 1rem;
  line-height: 1.5;
  max-height: 200px;
  padding: 4px 0;
}
.send-btn {
  background-color: var(--accent-color);
  color: #fff;
  border: none;
  border-radius: 8px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-left: 12px;
  transition: opacity 0.2s;
}
.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Markdown Styles Override */
:deep(.markdown-body) {
  font-family: inherit;
  font-size: 1rem;
  line-height: 1.6;
  color: var(--text-color);
}
:deep(.markdown-body pre) {
  background-color: var(--code-bg);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}
:deep(.markdown-body code) {
  background-color: var(--code-bg);
  padding: 2px 4px;
  border-radius: 4px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}
:deep(.markdown-body p) {
  margin-bottom: 1em;
}
:deep(.markdown-body ul), :deep(.markdown-body ol) {
  margin-bottom: 1em;
  padding-left: 2em;
}
</style>
