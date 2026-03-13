<template>
  <aside class="sidebar">
    <!-- Logo & App Name -->
    <div class="sidebar-header">
      <div class="logo">
        <div class="logo-circle">A</div>
        <div class="logo-text">
          <span class="logo-name">ALERTMNS</span>
          <span class="logo-sub">Messagerie interne</span>
        </div>
      </div>
      <button class="close-btn" @click="$emit('close')">✕</button>
    </div>

    <!-- User info -->
    <div class="user-info">
      <div class="user-avatar" :style="{ background: avatarColor }">
        {{ authStore.initials }}
      </div>
      <div class="user-details">
        <span class="user-name">{{ authStore.fullName }}</span>
        <div class="user-status">
          <span class="status-dot online"></span>
          <span class="status-text">En ligne</span>
        </div>
      </div>
      <NotificationPanel />
    </div>

    <!-- Navigation -->
    <nav class="sidebar-nav">
      <router-link to="/chat" class="nav-item" active-class="nav-active" exact>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span>Messagerie</span>
        <span v-if="totalUnread > 0" class="badge badge-primary">{{ totalUnread > 99 ? '99+' : totalUnread }}</span>
      </router-link>

      <router-link to="/pointage" class="nav-item" active-class="nav-active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        <span>Pointage</span>
      </router-link>

      <router-link to="/reunions" class="nav-item" active-class="nav-active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8" y1="2" x2="8" y2="6"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
        <span>Réunions</span>
      </router-link>

      <router-link to="/profil" class="nav-item" active-class="nav-active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          <circle cx="12" cy="7" r="4"/>
        </svg>
        <span>Profil</span>
      </router-link>

      <router-link v-if="authStore.isAdminOrRH" to="/admin" class="nav-item" active-class="nav-active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3"/>
          <path d="M19.07 4.93a10 10 0 0 1 0 14.14"/>
          <path d="M4.93 4.93a10 10 0 0 0 0 14.14"/>
        </svg>
        <span>Administration</span>
      </router-link>
    </nav>

    <!-- Channels Section -->
    <div class="sidebar-section">
      <div class="section-header">
        <span class="section-title">MES CANAUX</span>
        <button class="section-action" @click="showCreateModal = true" title="Créer un canal">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
        </button>
      </div>
      <div v-if="channelsStore.loading" class="section-loading">
        <div class="spinner" style="width:14px;height:14px;border-width:2px;"></div>
      </div>
      <div v-else class="channels-list">
        <router-link
          v-for="channel in channelsStore.sortedChannels"
          :key="channel.id"
          :to="`/chat/canal/${channel.id}`"
          class="channel-item"
          :class="{ 'channel-active': channelsStore.activeChannelId === channel.id }"
          @click="channelsStore.setActiveChannel(channel.id)"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="channel-icon">
            <line x1="4" y1="9" x2="20" y2="9"/>
            <line x1="4" y1="15" x2="20" y2="15"/>
            <line x1="10" y1="3" x2="8" y2="21"/>
            <line x1="16" y1="3" x2="14" y2="21"/>
          </svg>
          <span class="channel-name">{{ channel.nom }}</span>
          <span
            v-if="messagesStore.getUnreadCount(channel.id) > 0"
            class="channel-badge"
          >
            {{ messagesStore.getUnreadCount(channel.id) > 99 ? '99+' : messagesStore.getUnreadCount(channel.id) }}
          </span>
        </router-link>
        <div v-if="channelsStore.channels.length === 0" class="no-channels">
          Aucun canal disponible
        </div>
      </div>
    </div>

    <!-- Present Users Section -->
    <div class="sidebar-section">
      <div class="section-header">
        <span class="section-title">PRÉSENTS ({{ presentUsers.length }})</span>
        <button class="section-btn-toggle" @click="showPresent = !showPresent">
          {{ showPresent ? '−' : '+' }}
        </button>
      </div>
      <div v-if="showPresent" class="present-list">
        <div v-for="user in presentUsers" :key="user.id" class="present-user">
          <div class="present-avatar" :style="{ background: getAvatarColor(user) }">
            {{ getUserInitials(user) }}
          </div>
          <span class="present-name">{{ user.prenom }} {{ user.nom }}</span>
        </div>
        <div v-if="presentUsers.length === 0" class="no-present">
          Personne pour l'instant
        </div>
      </div>
    </div>

    <!-- Bottom actions -->
    <div class="sidebar-bottom">
      <button
        class="pointage-btn"
        :class="{ 'is-present': isPresent }"
        @click="togglePointage"
        :disabled="pointageLoading"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        <span>{{ pointageLoading ? 'Chargement...' : (isPresent ? 'Partir' : 'Arriver') }}</span>
      </button>

      <button class="logout-btn" @click="handleLogout">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        <span>Déconnexion</span>
      </button>
    </div>

    <!-- Create Channel Modal -->
    <Teleport to="body">
      <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Créer un canal</h3>
            <button class="modal-close" @click="showCreateModal = false">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="createError" class="alert alert-error">{{ createError }}</div>
            <div class="form-group">
              <label class="form-label">Nom du canal</label>
              <input
                v-model="newChannelName"
                class="form-input"
                type="text"
                placeholder="ex: général, dev-team, marketing..."
                @keyup.enter="createChannel"
              />
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input v-model="newChannelPrive" type="checkbox" />
                <span>Canal privé</span>
              </label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showCreateModal = false">Annuler</button>
            <button class="btn btn-primary" @click="createChannel" :disabled="!newChannelName.trim() || creating">
              {{ creating ? 'Création...' : 'Créer' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useChannelsStore } from '../stores/channels.js'
import { useMessagesStore } from '../stores/messages.js'
import { disconnectWebSocket, useWebSocket } from '../composables/useWebSocket.js'
import NotificationPanel from './NotificationPanel.vue'
import api from '../api/axios.js'

const emit = defineEmits(['close'])

const router = useRouter()
const authStore = useAuthStore()
const channelsStore = useChannelsStore()
const messagesStore = useMessagesStore()
const { subscribeToPresence } = useWebSocket()

const showCreateModal = ref(false)
const showPresent = ref(true)
const newChannelName = ref('')
const newChannelPrive = ref(false)
const creating = ref(false)
const createError = ref('')
const presentUsers = ref([])
const isPresent = ref(false)
const pointageLoading = ref(false)

const totalUnread = computed(() => messagesStore.totalUnread)

const AVATAR_COLORS = [
  '#E8501A', '#4299e1', '#48bb78', '#ed8936', '#9f7aea',
  '#ed64a6', '#38b2ac', '#667eea', '#fc8181', '#68d391'
]

function getAvatarColor(user) {
  const str = `${user.prenom}${user.nom}`
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length]
}

const avatarColor = computed(() => getAvatarColor(authStore.user || {}))

function getUserInitials(user) {
  return `${user.prenom?.[0] || ''}${user.nom?.[0] || ''}`.toUpperCase()
}

async function fetchPresent() {
  try {
    const res = await api.get('/users/presence')
    presentUsers.value = res.data || []
    isPresent.value = presentUsers.value.some(u => u.idUser === authStore.user?.id)
  } catch (e) {
    // Silently fail
  }
}

async function togglePointage() {
  pointageLoading.value = true
  try {
    if (isPresent.value) {
      await api.post('/pointage/partir')
    } else {
      await api.post('/pointage/arriver')
    }
    await fetchPresent()
  } catch (e) {
    console.error('Pointage error:', e)
  } finally {
    pointageLoading.value = false
  }
}

async function createChannel() {
  if (!newChannelName.value.trim()) return
  creating.value = true
  createError.value = ''
  const result = await channelsStore.createChannel(newChannelName.value.trim(), newChannelPrive.value)
  creating.value = false
  if (result.success) {
    showCreateModal.value = false
    newChannelName.value = ''
    newChannelPrive.value = false
    router.push(`/chat/canal/${result.channel.id}`)
  } else {
    createError.value = result.message
  }
}

function handleLogout() {
  disconnectWebSocket()
  authStore.logout()
  router.push('/login')
}

let presenceInterval = null

onMounted(() => {
  fetchPresent()
  presenceInterval = setInterval(fetchPresent, 15000)
  // Also update presence instantly via WebSocket when someone clocks in/out
  subscribeToPresence((users) => {
    presentUsers.value = users
    isPresent.value = users.some(u => u.idUser === authStore.user?.id)
  })
})

onUnmounted(() => {
  if (presenceInterval) clearInterval(presenceInterval)
})
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  min-width: var(--sidebar-width);
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  border-right: 1px solid var(--border-dark);
  transition: transform 0.3s ease;
  z-index: 200;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--border-dark);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-circle {
  width: 36px;
  height: 36px;
  background: var(--primary);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 800;
  color: white;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-name {
  font-size: 15px;
  font-weight: 700;
  color: white;
  letter-spacing: 0.5px;
}

.logo-sub {
  font-size: 10px;
  color: var(--sidebar-text);
}

.close-btn {
  display: none;
  background: none;
  border: none;
  color: var(--sidebar-text);
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  gap: 10px;
  border-bottom: 1px solid var(--border-dark);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 2px;
}

.status-text {
  font-size: 11px;
  color: var(--sidebar-text);
}

.sidebar-nav {
  padding: 8px 0;
  border-bottom: 1px solid var(--border-dark);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 16px;
  color: var(--sidebar-text);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition);
  border-radius: 0;
}

.nav-item:hover {
  background: var(--sidebar-hover);
  color: white;
}

.nav-active {
  background: var(--primary-light) !important;
  color: var(--primary) !important;
}

.nav-item .badge {
  margin-left: auto;
}

.sidebar-section {
  padding: 8px 0;
  border-bottom: 1px solid var(--border-dark);
  flex-shrink: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 16px;
  margin-bottom: 4px;
}

.section-title {
  font-size: 11px;
  font-weight: 700;
  color: var(--sidebar-text);
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.section-action {
  background: none;
  border: none;
  color: var(--sidebar-text);
  cursor: pointer;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: var(--transition);
}

.section-action:hover {
  background: var(--sidebar-hover);
  color: white;
}

.section-btn-toggle {
  background: none;
  border: none;
  color: var(--sidebar-text);
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  padding: 0 4px;
}

.section-loading {
  padding: 8px 16px;
  display: flex;
  justify-content: center;
}

.channels-list {
  overflow-y: auto;
  max-height: 200px;
}

.channel-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  color: var(--sidebar-text);
  text-decoration: none;
  font-size: 14px;
  transition: var(--transition);
  cursor: pointer;
}

.channel-item:hover {
  background: var(--sidebar-hover);
  color: white;
}

.channel-active {
  background: var(--primary-light) !important;
  color: var(--primary) !important;
}

.channel-icon {
  flex-shrink: 0;
  opacity: 0.7;
}

.channel-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.channel-badge {
  background: var(--primary);
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
  flex-shrink: 0;
}

.no-channels {
  padding: 8px 16px;
  font-size: 12px;
  color: var(--sidebar-text);
  font-style: italic;
}

.present-list {
  overflow-y: auto;
  max-height: 150px;
  padding: 0 0 4px;
}

.present-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 16px;
}

.present-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.present-name {
  font-size: 13px;
  color: var(--sidebar-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-present {
  padding: 6px 16px;
  font-size: 12px;
  color: var(--sidebar-text);
  font-style: italic;
}

.sidebar-bottom {
  margin-top: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-top: 1px solid var(--border-dark);
}

.pointage-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 9px 16px;
  border-radius: var(--radius);
  font-size: 13px;
  font-weight: 600;
  transition: var(--transition);
  background: var(--primary);
  color: white;
  border: none;
  cursor: pointer;
}

.pointage-btn.is-present {
  background: rgba(72, 187, 120, 0.2);
  color: var(--success);
  border: 1px solid rgba(72, 187, 120, 0.3);
}

.pointage-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.pointage-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 8px 16px;
  border-radius: var(--radius);
  font-size: 13px;
  font-weight: 500;
  color: var(--sidebar-text);
  background: none;
  border: 1px solid var(--border-dark);
  cursor: pointer;
  transition: var(--transition);
}

.logout-btn:hover {
  background: rgba(245, 101, 101, 0.15);
  color: var(--danger);
  border-color: rgba(245, 101, 101, 0.3);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text);
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    transform: translateX(-100%);
    height: 100vh;
    z-index: 200;
    overflow-y: auto;
    overflow-x: hidden;
  }

  .sidebar.sidebar-mobile-open {
    transform: translateX(0);
  }

  .close-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    border-radius: var(--radius-sm);
  }

  .close-btn:hover {
    background: var(--sidebar-hover);
    color: white;
  }

  .channels-list {
    max-height: 180px;
  }

  .present-list {
    max-height: 120px;
  }

  .sidebar-bottom {
    position: sticky;
    bottom: 0;
    background: var(--sidebar-bg);
    margin-top: 8px;
  }
}
</style>
