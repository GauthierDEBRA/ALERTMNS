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
        <img
          v-if="authStore.user?.avatarUrl"
          :src="authStore.user.avatarUrl"
          :alt="authStore.fullName"
          class="user-avatar-image"
        />
        <template v-else>
          {{ authStore.initials }}
        </template>
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

    <div class="sidebar-scroll">
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

      <div class="sidebar-search-section" ref="globalSearchRef">
        <div class="sidebar-search-box">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="globalSearchQuery"
            type="text"
            class="sidebar-search-input"
            placeholder="Recherche globale"
            @focus="handleSearchFocus"
          />
          <button v-if="globalSearchQuery" class="sidebar-search-clear" @click="clearGlobalSearch">✕</button>
        </div>

        <div v-if="showGlobalSearchResults" class="sidebar-search-results">
          <div v-if="searchingMessages" class="sidebar-search-state">Recherche...</div>
          <div v-else-if="globalSearchError" class="sidebar-search-state sidebar-search-error">{{ globalSearchError }}</div>
          <div v-else-if="globalSearchQuery.trim().length < 2" class="sidebar-search-state">
            Tape au moins 2 caractères
          </div>
          <template v-else>
            <button
              v-for="result in globalSearchResults"
              :key="`${result.canalId}-${result.idMessage}`"
              class="sidebar-search-result"
              @click="openSearchResult(result)"
            >
              <div class="sidebar-search-result-avatar" :style="{ background: getAvatarColor(result) }">
                <img
                  v-if="result.auteurAvatarUrl"
                  :src="result.auteurAvatarUrl"
                  :alt="`${result.auteurPrenom} ${result.auteurNom}`"
                  class="present-avatar-image"
                />
                <template v-else>
                  {{ getUserInitials({ prenom: result.auteurPrenom, nom: result.auteurNom }) }}
                </template>
              </div>
              <div class="sidebar-search-result-content">
                <div class="sidebar-search-result-top">
                  <span class="sidebar-search-conversation">{{ result.conversationName }}</span>
                  <span class="sidebar-search-date">{{ formatSearchDate(result.dateEnvoi) }}</span>
                </div>
                <div class="sidebar-search-author">{{ result.auteurPrenom }} {{ result.auteurNom }}</div>
                <div class="sidebar-search-excerpt">{{ result.extrait }}</div>
              </div>
            </button>
            <div v-if="globalSearchResults.length === 0" class="sidebar-search-state">
              Aucun message trouvé
            </div>
          </template>
        </div>
      </div>

      <!-- Channels Section -->
      <div class="sidebar-section">
        <div class="section-header">
          <span class="section-title">MES CANAUX</span>
          <button
            v-if="authStore.isAdmin"
            class="section-action"
            @click="showCreateModal = true"
            title="Créer un canal"
          >
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
            <span class="channel-name">{{ channel.nom.replace(/^#/, '') }}</span>
            <span
              v-if="messagesStore.getUnreadCount(channel.id) > 0"
              class="channel-badge"
            >
              {{ messagesStore.getUnreadCount(channel.id) > 99 ? '99+' : messagesStore.getUnreadCount(channel.id) }}
            </span>
          </router-link>
          <div v-if="channelsStore.groupChannels.length === 0" class="no-channels">
            Aucun canal disponible
          </div>
        </div>
      </div>

      <div class="sidebar-section">
        <div class="section-header">
          <span class="section-title">MESSAGES PRIVÉS</span>
          <button
            class="section-action"
            @click="openDirectModal"
            title="Démarrer une conversation privée"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
          </button>
        </div>
        <div class="channels-list direct-list">
          <router-link
            v-for="channel in channelsStore.sortedDirectChannels"
            :key="channel.id"
            :to="`/chat/canal/${channel.id}`"
            class="channel-item direct-item"
            :class="{ 'channel-active': channelsStore.activeChannelId === channel.id }"
            @click="channelsStore.setActiveChannel(channel.id)"
          >
            <div class="direct-avatar" :style="{ background: getDirectAvatarColor(channel) }">
              <img
                v-if="channel.directUserAvatarUrl"
                :src="channel.directUserAvatarUrl"
                :alt="channel.displayName"
                class="present-avatar-image"
              />
              <template v-else>
                {{ getDirectInitials(channel) }}
              </template>
            </div>
            <span class="channel-name">{{ channel.displayName }}</span>
            <span
              v-if="messagesStore.getUnreadCount(channel.id) > 0"
              class="channel-badge"
            >
              {{ messagesStore.getUnreadCount(channel.id) > 99 ? '99+' : messagesStore.getUnreadCount(channel.id) }}
            </span>
          </router-link>
          <div v-if="channelsStore.directChannels.length === 0" class="no-channels">
            Aucune conversation privée
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
          <div v-for="user in presentUsers" :key="user.idUser" class="present-user">
            <div class="present-avatar" :style="{ background: getAvatarColor(user) }">
              <img
                v-if="user.avatarUrl"
                :src="user.avatarUrl"
                :alt="`${user.prenom} ${user.nom}`"
                class="present-avatar-image"
              />
              <template v-else>
                {{ getUserInitials(user) }}
              </template>
            </div>
            <span class="present-name">{{ user.prenom }} {{ user.nom }}</span>
          </div>
          <div v-if="presentUsers.length === 0" class="no-present">
            Personne pour l'instant
          </div>
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

    <Teleport to="body">
      <div v-if="showDirectModal" class="modal-overlay" @click.self="showDirectModal = false">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Nouveau message privé</h3>
            <button class="modal-close" @click="showDirectModal = false">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="directError" class="alert alert-error">{{ directError }}</div>
            <div class="form-group">
              <label class="form-label">Choisir un collaborateur</label>
              <select v-model="selectedDirectUserId" class="form-input">
                <option value="">-- Sélectionner --</option>
                <option
                  v-for="user in availableDirectUsers"
                  :key="user.id"
                  :value="user.id"
                >
                  {{ user.prenom }} {{ user.nom }} ({{ user.email }})
                </option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDirectModal = false">Annuler</button>
            <button
              class="btn btn-primary"
              @click="startDirectConversation"
              :disabled="!selectedDirectUserId || startingDirect"
            >
              {{ startingDirect ? 'Ouverture...' : 'Ouvrir la conversation' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </aside>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useChannelsStore } from '../stores/channels.js'
import { useMessagesStore } from '../stores/messages.js'
import { disconnectWebSocket, useWebSocket } from '../composables/useWebSocket.js'
import { getAvatarColor, getAvatarInitials } from '../utils/avatar.js'
import NotificationPanel from './NotificationPanel.vue'
import api from '../api/axios.js'

const emit = defineEmits(['close'])

const router = useRouter()
const authStore = useAuthStore()
const channelsStore = useChannelsStore()
const messagesStore = useMessagesStore()
const { subscribeToPresence } = useWebSocket()

const showCreateModal = ref(false)
const showDirectModal = ref(false)
const showPresent = ref(true)
const showGlobalSearchResults = ref(false)
const newChannelName = ref('')
const newChannelPrive = ref(false)
const creating = ref(false)
const createError = ref('')
const directError = ref('')
const startingDirect = ref(false)
const selectedDirectUserId = ref('')
const allActiveUsers = ref([])
const presentUsers = ref([])
const isPresent = ref(false)
const pointageLoading = ref(false)
const globalSearchQuery = ref('')
const globalSearchResults = ref([])
const searchingMessages = ref(false)
const globalSearchError = ref('')
const globalSearchRef = ref(null)

const totalUnread = computed(() => messagesStore.totalUnread)
const availableDirectUsers = computed(() =>
  allActiveUsers.value
    .filter(user => user.id !== authStore.user?.id)
    .sort((a, b) => `${a.prenom} ${a.nom}`.localeCompare(`${b.prenom} ${b.nom}`, 'fr'))
)

const avatarColor = computed(() => getAvatarColor(authStore.user || {}))

function getUserInitials(user) {
  return getAvatarInitials(user, '?')
}

function getDirectInitials(channel) {
  return getAvatarInitials(channel, '@')
}

function getDirectAvatarColor(channel) {
  return getAvatarColor(channel)
}

function formatSearchDate(dateInput) {
  if (!dateInput) return ''
  return new Date(dateInput).toLocaleTimeString('fr-FR', {
    hour: '2-digit',
    minute: '2-digit'
  })
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

async function fetchActiveUsers() {
  try {
    const res = await api.get('/users/actifs')
    allActiveUsers.value = (res.data || []).map(user => ({
      ...user,
      id: user.id ?? user.idUser
    }))
  } catch (e) {
    allActiveUsers.value = []
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

async function openDirectModal() {
  directError.value = ''
  selectedDirectUserId.value = ''
  await fetchActiveUsers()
  showDirectModal.value = true
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

async function startDirectConversation() {
  if (!selectedDirectUserId.value) return
  startingDirect.value = true
  directError.value = ''
  const result = await channelsStore.startDirectConversation(selectedDirectUserId.value)
  startingDirect.value = false

  if (result.success) {
    showDirectModal.value = false
    selectedDirectUserId.value = ''
    router.push(`/chat/canal/${result.channel.id}`)
  } else {
    directError.value = result.message
  }
}

function clearGlobalSearch() {
  globalSearchQuery.value = ''
  globalSearchResults.value = []
  globalSearchError.value = ''
  showGlobalSearchResults.value = false
}

function handleSearchFocus() {
  showGlobalSearchResults.value = true
}

function handleSearchOutsideClick(event) {
  if (globalSearchRef.value && !globalSearchRef.value.contains(event.target)) {
    showGlobalSearchResults.value = false
  }
}

async function performGlobalSearch(query) {
  const trimmed = query.trim()
  globalSearchError.value = ''

  if (trimmed.length < 2) {
    globalSearchResults.value = []
    searchingMessages.value = false
    return
  }

  searchingMessages.value = true
  const result = await messagesStore.searchMessages(trimmed)
  searchingMessages.value = false

  if (!result.success) {
    globalSearchError.value = result.message
    globalSearchResults.value = []
    return
  }

  globalSearchResults.value = result.results
}

function openSearchResult(result) {
  showGlobalSearchResults.value = false
  router.push({
    path: `/chat/canal/${result.canalId}`,
    query: {
      search: globalSearchQuery.value.trim(),
      message: String(result.id ?? result.idMessage)
    }
  })
  emit('close')
}

async function handleLogout() {
  disconnectWebSocket()
  await authStore.logout()
  router.push('/login')
}

let presenceInterval = null
let globalSearchTimer = null

watch(globalSearchQuery, (value) => {
  showGlobalSearchResults.value = true
  if (globalSearchTimer) clearTimeout(globalSearchTimer)
  globalSearchTimer = setTimeout(() => {
    performGlobalSearch(value)
  }, 250)
})

onMounted(() => {
  fetchPresent()
  fetchActiveUsers()
  presenceInterval = setInterval(fetchPresent, 15000)
  document.addEventListener('click', handleSearchOutsideClick)
  // Also update presence instantly via WebSocket when someone clocks in/out
  subscribeToPresence((users) => {
    presentUsers.value = users
    isPresent.value = users.some(u => u.idUser === authStore.user?.id)
  })
})

onUnmounted(() => {
  if (presenceInterval) clearInterval(presenceInterval)
  if (globalSearchTimer) clearTimeout(globalSearchTimer)
  document.removeEventListener('click', handleSearchOutsideClick)
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

.sidebar-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  overscroll-behavior: contain;
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

.user-avatar-image,
.present-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  display: block;
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

.sidebar-search-section {
  position: relative;
  padding: 12px 14px;
  border-bottom: 1px solid var(--border-dark);
  z-index: 5;
}

.sidebar-search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 12px;
  border-radius: var(--radius);
  border: 1px solid var(--border-dark);
  background: rgba(255, 255, 255, 0.04);
  color: var(--sidebar-text);
}

.sidebar-search-box:focus-within {
  border-color: rgba(232, 80, 26, 0.5);
  box-shadow: 0 0 0 3px rgba(232, 80, 26, 0.12);
}

.sidebar-search-input {
  flex: 1;
  min-width: 0;
  background: transparent;
  border: none;
  color: white;
  font-size: 13px;
}

.sidebar-search-input::placeholder {
  color: var(--sidebar-text);
}

.sidebar-search-clear {
  background: none;
  border: none;
  color: var(--sidebar-text);
  font-size: 12px;
  padding: 0;
}

.sidebar-search-clear:hover {
  color: white;
}

.sidebar-search-results {
  position: absolute;
  left: 14px;
  right: 14px;
  top: calc(100% - 4px);
  max-height: 360px;
  overflow-y: auto;
  background: #232732;
  border: 1px solid var(--border-dark);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 8px;
}

.sidebar-search-state {
  padding: 12px 10px;
  font-size: 12px;
  color: var(--sidebar-text);
}

.sidebar-search-error {
  color: #fc8181;
}

.sidebar-search-result {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border-radius: var(--radius);
  background: transparent;
  color: inherit;
  text-align: left;
}

.sidebar-search-result:hover {
  background: var(--sidebar-hover);
}

.sidebar-search-result-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
}

.sidebar-search-result-content {
  min-width: 0;
  flex: 1;
}

.sidebar-search-result-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 2px;
}

.sidebar-search-conversation {
  font-size: 12px;
  font-weight: 700;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-search-date {
  font-size: 11px;
  color: var(--sidebar-text);
  flex-shrink: 0;
}

.sidebar-search-author {
  font-size: 11px;
  color: #f6ad55;
  margin-bottom: 2px;
}

.sidebar-search-excerpt {
  font-size: 12px;
  color: var(--sidebar-text);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

.direct-list {
  max-height: 180px;
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

.direct-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
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
  flex-shrink: 0;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-top: 1px solid var(--border-dark);
  background: var(--sidebar-bg);
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
    margin-top: 0;
  }
}
</style>
