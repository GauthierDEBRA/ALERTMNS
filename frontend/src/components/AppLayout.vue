<template>
  <div class="app-layout">
    <!-- Mobile overlay -->
    <div
      v-if="sidebarOpen"
      class="sidebar-overlay"
      @click="sidebarOpen = false"
    />

    <!-- Sidebar -->
    <Sidebar
      :class="{ 'sidebar-mobile-open': sidebarOpen }"
      @close="sidebarOpen = false"
    />

    <!-- Main content -->
    <div class="main-content">
      <!-- Mobile header -->
      <div class="mobile-header">
        <button class="menu-btn" @click="sidebarOpen = !sidebarOpen">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="3" y1="6" x2="21" y2="6"/>
            <line x1="3" y1="12" x2="21" y2="12"/>
            <line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </button>
        <span class="mobile-title">ALERTMNS</span>
        <NotificationPanel />
      </div>

      <!-- WebSocket status banner -->
      <transition name="ws-banner">
        <div v-if="wsDisconnected" class="ws-banner">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 6s4-4 11-4 11 4 11 4"/>
            <path d="M5 10s2.5-2.5 7-2.5 7 2.5 7 2.5"/>
            <line x1="2" y1="2" x2="22" y2="22"/>
          </svg>
          Connexion perdue — reconnexion en cours…
        </div>
      </transition>

      <!-- Page content -->
      <router-view v-slot="{ Component, route }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useChannelsStore } from '../stores/channels.js'
import { useNotificationsStore } from '../stores/notifications.js'
import { useAuthStore } from '../stores/auth.js'
import { useWebSocket } from '../composables/useWebSocket.js'
import { useToast } from '../composables/useToast.js'
import Sidebar from './Sidebar.vue'
import NotificationPanel from './NotificationPanel.vue'

const sidebarOpen = ref(false)
const channelsStore = useChannelsStore()
const notificationsStore = useNotificationsStore()
const authStore = useAuthStore()
const { connect, subscribeToNotifications, connectionState } = useWebSocket()
const { error: toastError } = useToast()

// Only show banner after initial connection attempt (not on first load)
const hasConnectedOnce = ref(false)
const wsDisconnected = computed(() => hasConnectedOnce.value && connectionState.value === 'disconnected')

let notifInterval = null

function startPolling() {
  if (notifInterval) return
  notifInterval = setInterval(() => notificationsStore.fetchNotifications(), 30000)
}

function stopPolling() {
  if (notifInterval) {
    clearInterval(notifInterval)
    notifInterval = null
  }
}

// Stop polling when WS is connected, restart it as fallback when disconnected
watch(connectionState, (state) => {
  if (state === 'connected') {
    hasConnectedOnce.value = true
    stopPolling()
  } else if (state === 'disconnected' && hasConnectedOnce.value) {
    startPolling()
  }
})

onMounted(async () => {
  const channelsOk = await channelsStore.fetchMyChannels()
  if (!channelsOk) toastError('Impossible de charger vos canaux')
  await notificationsStore.fetchNotifications()

  if (authStore.token) {
    try {
      await connect(authStore.token)
      if (authStore.user?.id) {
        subscribeToNotifications(authStore.user.id, (notif) => {
          notificationsStore.addNotification(notif)
        })
      }
    } catch {
      // First WS connection failed — fall back to polling
      startPolling()
    }
  } else {
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--content-bg);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 100;
}

.mobile-header {
  display: none;
  align-items: center;
  padding: 12px 16px;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  gap: 12px;
}

.menu-btn {
  background: none;
  border: none;
  color: var(--text);
  padding: 6px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-btn:hover {
  background: var(--content-bg);
}

.mobile-title {
  flex: 1;
  font-weight: 700;
  font-size: 16px;
  color: var(--primary);
}

@media (max-width: 768px) {
  .sidebar-overlay {
    display: block;
  }

  .mobile-header {
    display: flex;
  }
}

/* WebSocket status banner */
.ws-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f59e0b;
  color: #1a1a1a;
  font-size: 13px;
  font-weight: 500;
  flex-shrink: 0;
}

.ws-banner-enter-active,
.ws-banner-leave-active {
  transition: max-height 0.2s ease, opacity 0.2s ease;
  overflow: hidden;
  max-height: 40px;
}
.ws-banner-enter-from,
.ws-banner-leave-to {
  max-height: 0;
  opacity: 0;
}

/* Page transitions */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
