<template>
  <div class="notif-panel" ref="panelRef">
    <button ref="buttonRef" class="notif-btn" @click="togglePanel">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      <span v-if="notifStore.unreadCount > 0" class="notif-badge">
        {{ notifStore.unreadCount > 99 ? '99+' : notifStore.unreadCount }}
      </span>
    </button>

    <Teleport to="body">
      <Transition name="dropdown">
        <div
          v-if="isOpen"
          ref="dropdownRef"
          class="notif-dropdown"
          :style="dropdownStyle"
        >
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
              :class="[{ 'is-unread': !notif.lu }, `notif-${getNotifTypeClass(notif.type)}`]"
              @click="handleNotifClick(notif)"
            >
              <div class="notif-dot" v-if="!notif.lu"></div>
              <div class="notif-content">
                <div class="notif-topline">
                  <span class="notif-type-badge" :class="`badge-${getNotifTypeClass(notif.type)}`">
                    {{ getNotifTypeLabel(notif.type) }}
                  </span>
                  <span class="notif-time">{{ formatDate(notif.createdAt || notif.dateCreation) }}</span>
                </div>
                <p class="notif-message">{{ notif.message || notif.contenu }}</p>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationsStore } from '../stores/notifications.js'
import { useDate } from '../composables/useDate.js'

const notifStore = useNotificationsStore()
const { formatDate } = useDate()
const router = useRouter()

const isOpen = ref(false)
const panelRef = ref(null)
const buttonRef = ref(null)
const dropdownRef = ref(null)
const dropdownStyle = ref({})

async function togglePanel() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    await nextTick()
    updateDropdownPosition()
  }
}

async function markAllAsRead() {
  await notifStore.markAllAsRead()
}

async function handleNotifClick(notif) {
  if (!notif.lu) {
    await notifStore.markAsRead(notif.id)
  }

  if (notif.targetRoute) {
    router.push(notif.targetRoute)
    isOpen.value = false
    return
  }

  const type = String(notif.type || '').toUpperCase()

  if (type === 'REUNION') {
    router.push('/reunions')
    isOpen.value = false
    return
  }

  if (type === 'MESSAGE') {
    router.push('/chat')
    isOpen.value = false
  }
}

function updateDropdownPosition() {
  if (!buttonRef.value) return

  const rect = buttonRef.value.getBoundingClientRect()
  const viewportPadding = 12
  const dropdownWidth = Math.min(340, window.innerWidth - (viewportPadding * 2))
  const dropdownHeight = dropdownRef.value?.offsetHeight || Math.min(420, window.innerHeight - 32)
  const maxLeft = window.innerWidth - dropdownWidth - viewportPadding
  const left = Math.max(viewportPadding, Math.min(rect.right - dropdownWidth, maxLeft))
  const fitsBelow = rect.bottom + 8 + dropdownHeight <= window.innerHeight - viewportPadding
  const top = fitsBelow
    ? rect.bottom + 8
    : Math.max(viewportPadding, rect.top - dropdownHeight - 8)

  dropdownStyle.value = {
    position: 'fixed',
    top: `${top}px`,
    left: `${left}px`,
    width: `${dropdownWidth}px`
  }
}

function getNotifTypeClass(type) {
  switch (String(type || '').toUpperCase()) {
    case 'REUNION':
      return 'meeting'
    case 'MESSAGE':
      return 'message'
    case 'ABSENCE':
      return 'absence'
    default:
      return 'info'
  }
}

function getNotifTypeLabel(type) {
  switch (String(type || '').toUpperCase()) {
    case 'REUNION':
      return 'Réunion'
    case 'MESSAGE':
      return 'Message'
    case 'ABSENCE':
      return 'Absence'
    default:
      return 'Info'
  }
}

function handleOutsideClick(event) {
  const clickedTrigger = panelRef.value?.contains(event.target)
  const clickedDropdown = dropdownRef.value?.contains(event.target)

  if (!clickedTrigger && !clickedDropdown) {
    isOpen.value = false
  }
}

function handleViewportChange() {
  if (isOpen.value) {
    updateDropdownPosition()
  }
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  window.addEventListener('resize', handleViewportChange)
  window.addEventListener('scroll', handleViewportChange, true)
})

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
  window.removeEventListener('resize', handleViewportChange)
  window.removeEventListener('scroll', handleViewportChange, true)
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
  background: var(--white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
  z-index: 950;
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
  max-height: min(360px, calc(100vh - 96px));
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

.notif-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.notif-type-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.badge-meeting {
  background: rgba(237, 98, 34, 0.12);
  color: var(--primary);
}

.badge-message {
  background: rgba(66, 153, 225, 0.12);
  color: #2b6cb0;
}

.badge-absence {
  background: rgba(245, 101, 101, 0.12);
  color: #c53030;
}

.badge-info {
  background: rgba(74, 85, 104, 0.10);
  color: var(--text-light);
}

.notif-message {
  font-size: 13px;
  color: var(--text);
  line-height: 1.4;
  margin-bottom: 0;
}

.notif-time {
  font-size: 11px;
  color: var(--text-light);
  white-space: nowrap;
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

@media (max-width: 768px) {
  .notif-dropdown {
    max-width: calc(100vw - 24px);
  }
}
</style>
