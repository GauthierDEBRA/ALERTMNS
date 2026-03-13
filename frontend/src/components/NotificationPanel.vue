<template>
  <div class="notif-panel" ref="panelRef">
    <button class="notif-btn" @click="togglePanel">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      <span v-if="notifStore.unreadCount > 0" class="notif-badge">
        {{ notifStore.unreadCount > 99 ? '99+' : notifStore.unreadCount }}
      </span>
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" class="notif-dropdown">
        <div class="notif-header">
          <span class="notif-title">Notifications</span>
          <button
            v-if="notifStore.unreadCount > 0"
            class="mark-all-btn"
            @click="markAllAsRead"
          >
            Tout lire
          </button>
        </div>

        <div class="notif-list">
          <div v-if="notifStore.loading" class="notif-loading">
            <div class="spinner"></div>
          </div>
          <div v-else-if="notifStore.notifications.length === 0" class="notif-empty">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <p>Aucune notification</p>
          </div>
          <div
            v-for="notif in notifStore.sortedNotifications"
            :key="notif.id"
            class="notif-item"
            :class="{ 'is-unread': !notif.lu }"
            @click="handleNotifClick(notif)"
          >
            <div class="notif-dot" v-if="!notif.lu"></div>
            <div class="notif-content">
              <p class="notif-message">{{ notif.message || notif.contenu }}</p>
              <span class="notif-time">{{ formatDate(notif.createdAt || notif.dateCreation) }}</span>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useNotificationsStore } from '../stores/notifications.js'
import { useDate } from '../composables/useDate.js'

const notifStore = useNotificationsStore()
const { formatDate } = useDate()

const isOpen = ref(false)
const panelRef = ref(null)

function togglePanel() {
  isOpen.value = !isOpen.value
}

async function markAllAsRead() {
  await notifStore.markAllAsRead()
}

async function handleNotifClick(notif) {
  if (!notif.lu) {
    await notifStore.markAsRead(notif.id)
  }
}

function handleOutsideClick(event) {
  if (panelRef.value && !panelRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<style scoped>
.notif-panel {
  position: relative;
}

.notif-btn {
  position: relative;
  background: none;
  border: none;
  color: var(--sidebar-text);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
}

.notif-btn:hover {
  background: var(--sidebar-hover);
  color: white;
}

.notif-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: var(--primary);
  color: white;
  font-size: 9px;
  font-weight: 700;
  padding: 0 4px;
  border-radius: 10px;
  min-width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--sidebar-bg);
  line-height: 1;
}

.notif-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 340px;
  background: var(--white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
  z-index: 500;
  overflow: hidden;
}

.notif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
}

.notif-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
}

.mark-all-btn {
  background: none;
  border: none;
  color: var(--primary);
  font-size: 13px;
  cursor: pointer;
  font-weight: 500;
}

.mark-all-btn:hover {
  text-decoration: underline;
}

.notif-list {
  max-height: 360px;
  overflow-y: auto;
}

.notif-loading {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.notif-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 30px 20px;
  color: var(--text-light);
  font-size: 13px;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: var(--transition);
  border-bottom: 1px solid var(--border);
}

.notif-item:last-child {
  border-bottom: none;
}

.notif-item:hover {
  background: var(--content-bg);
}

.notif-item.is-unread {
  background: rgba(232, 80, 26, 0.04);
}

.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  margin-top: 5px;
  flex-shrink: 0;
}

.notif-content {
  flex: 1;
  min-width: 0;
}

.notif-message {
  font-size: 13px;
  color: var(--text);
  line-height: 1.4;
  margin-bottom: 4px;
}

.notif-time {
  font-size: 11px;
  color: var(--text-light);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
