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

      <!-- Page content -->
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useChannelsStore } from '../stores/channels.js'
import { useNotificationsStore } from '../stores/notifications.js'
import { useAuthStore } from '../stores/auth.js'
import { useWebSocket } from '../composables/useWebSocket.js'
import Sidebar from './Sidebar.vue'
import NotificationPanel from './NotificationPanel.vue'

const sidebarOpen = ref(false)
const channelsStore = useChannelsStore()
const notificationsStore = useNotificationsStore()
const authStore = useAuthStore()
const { connect, subscribeToNotifications } = useWebSocket()

let notifInterval = null

onMounted(async () => {
  await channelsStore.fetchMyChannels()
  await notificationsStore.fetchNotifications()

  // Connect WebSocket then subscribe to personal notifications
  if (authStore.token) {
    await connect(authStore.token)
    if (authStore.user?.id) {
      subscribeToNotifications(authStore.user.id, (notif) => {
        notificationsStore.addNotification(notif)
      })
    }
  }

  // Poll notifications every 30 seconds as fallback
  notifInterval = setInterval(() => {
    notificationsStore.fetchNotifications()
  }, 30000)
})

onUnmounted(() => {
  if (notifInterval) clearInterval(notifInterval)
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
</style>
