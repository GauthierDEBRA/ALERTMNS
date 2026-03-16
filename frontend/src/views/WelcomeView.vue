<template>
  <div class="welcome-page">
    <!-- Top bar -->
    <div class="welcome-topbar">
      <div class="topbar-left">
        <h1 class="page-title">Messagerie</h1>
      </div>
      <div class="topbar-right">
        <div class="desktop-notif">
          <NotificationPanel />
        </div>
      </div>
    </div>

    <!-- Welcome Content -->
    <div class="welcome-content">
      <div class="welcome-hero">
        <div class="hero-logo">A</div>
        <h2 class="hero-title">Bienvenue, {{ authStore.user?.prenom }} !</h2>
        <p class="hero-subtitle">
          Sélectionnez un canal ou un message privé dans la barre latérale pour commencer à discuter
        </p>
      </div>

      <!-- Stats cards -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(232,80,26,0.1); color: var(--primary)">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value" :class="{ skeleton: channelsStore.loading }">{{ channelsStore.loading ? '' : channelsStore.channels.length }}</span>
            <span class="stat-label">Conversations disponibles</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(72,187,120,0.1); color: var(--success)">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value" :class="{ skeleton: loadingStats }">{{ loadingStats ? '' : presentCount }}</span>
            <span class="stat-label">Collaborateurs présents</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(66,153,225,0.1); color: var(--info)">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ notifStore.unreadCount }}</span>
            <span class="stat-label">Notifications non lues</span>
          </div>
        </div>

      </div>

      <!-- Quick actions -->
      <div class="quick-actions">
        <h3 class="section-title">Accès rapide</h3>
        <div class="actions-grid">
          <div v-if="channelsStore.channels.length === 0" class="action-card action-card-empty">
            <div class="action-icon" style="color: var(--text-light)">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="4" y1="9" x2="20" y2="9"/>
                <line x1="4" y1="15" x2="20" y2="15"/>
                <line x1="10" y1="3" x2="8" y2="21"/>
                <line x1="16" y1="3" x2="14" y2="21"/>
              </svg>
            </div>
            <span class="action-label">Aucun canal</span>
            <p class="empty-hint">Rejoins ou crée un canal depuis la barre latérale pour commencer à échanger.</p>
          </div>
          <div class="action-card channel-shortcuts-card" v-else>
            <div class="action-icon channel-shortcuts-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="4" y1="9" x2="20" y2="9"/>
                <line x1="4" y1="15" x2="20" y2="15"/>
                <line x1="10" y1="3" x2="8" y2="21"/>
                <line x1="16" y1="3" x2="14" y2="21"/>
              </svg>
            </div>
            <span class="action-label">Canaux récents</span>
            <div class="channel-shortcuts-list">
              <router-link
                v-for="channel in channelsStore.sortedChannels.slice(0, 3)"
                :key="channel.id"
                :to="`/chat/canal/${channel.id}`"
                class="channel-shortcut"
              >
                <span class="shortcut-hash">#</span>
                <span>{{ channel.nom.replace(/^#/, '') }}</span>
              </router-link>
            </div>
          </div>

          <router-link to="/pointage" class="action-card action-pointage">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
            </div>
            <span class="action-label">Pointage</span>
            <span class="action-sub">Gérer mes présences</span>
          </router-link>

          <router-link to="/reunions" class="action-card action-reunions">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
            </div>
            <span class="action-label">Réunions</span>
            <span class="action-sub">Voir mes réunions</span>
          </router-link>

          <router-link to="/profil" class="action-card action-profil">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <span class="action-label">Mon profil</span>
            <span class="action-sub">Gérer mes paramètres</span>
          </router-link>
        </div>
      </div>

      <!-- Activité récente -->
      <div class="recent-section" v-if="recentActivity.length > 0">
        <h3 class="section-title">Activité récente</h3>
        <div class="activity-list">
          <div
            v-for="item in recentActivity"
            :key="item.id"
            class="activity-item"
          >
            <div class="activity-dot" :class="`dot-${item.type}`"></div>
            <div class="activity-body">
              <span class="activity-msg">{{ item.message }}</span>
              <span class="activity-time">{{ item.timeAgo }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth.js'
import { useChannelsStore } from '../stores/channels.js'
import { useNotificationsStore } from '../stores/notifications.js'
import NotificationPanel from '../components/NotificationPanel.vue'
import api from '../api/axios.js'

const authStore = useAuthStore()
const channelsStore = useChannelsStore()
const notifStore = useNotificationsStore()

const presentCount = ref(0)
const loadingStats = ref(true)

function timeAgo(dateInput) {
  if (!dateInput) return ''
  const date = new Date(dateInput)
  const diff = Math.floor((Date.now() - date.getTime()) / 1000)
  if (diff < 60) return "à l'instant"
  if (diff < 3600) return `il y a ${Math.floor(diff / 60)} min`
  if (diff < 86400) return `il y a ${Math.floor(diff / 3600)} h`
  return `il y a ${Math.floor(diff / 86400)} j`
}

function notifTypeKey(type) {
  if (!type) return 'info'
  const t = type.toLowerCase()
  if (t.includes('message')) return 'message'
  if (t.includes('reunion') || t.includes('meeting')) return 'meeting'
  if (t.includes('absence')) return 'absence'
  return 'info'
}

const recentActivity = computed(() => {
  return notifStore.notifications
    .slice(0, 6)
    .map(n => ({
      id: n.id,
      message: n.message || n.contenu || '',
      timeAgo: timeAgo(n.createdAt || n.dateCreation),
      type: notifTypeKey(n.type)
    }))
})

async function fetchPresence() {
  try {
    const res = await api.get('/users/presence')
    presentCount.value = (res.data || []).length
  } catch (e) {
    // Silently fail
  } finally {
    loadingStats.value = false
  }
}

onMounted(() => {
  fetchPresence()
})
</script>

<style scoped>
.welcome-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--content-bg);
  overflow: hidden;
}

.welcome-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.topbar-left .page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.desktop-notif {
  display: block;
}

:deep(.notif-btn) {
  color: var(--text-light);
}

:deep(.notif-btn:hover) {
  background: var(--content-bg);
  color: var(--text);
}

.welcome-content {
  flex: 1;
  overflow-y: auto;
  padding: 32px 32px;
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
}

.welcome-hero {
  text-align: center;
  padding: 24px 20px 28px;
  margin-bottom: 24px;
}

.hero-logo {
  width: 52px;
  height: 52px;
  background: var(--primary);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  color: white;
  margin: 0 auto 16px;
  box-shadow: 0 4px 16px rgba(232, 80, 26, 0.3);
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 8px;
}

.hero-subtitle {
  font-size: 15px;
  color: var(--text-light);
  max-width: 400px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value.skeleton {
  display: inline-block;
  width: 36px;
  height: 24px;
  border-radius: 5px;
  background: linear-gradient(90deg, var(--border) 25%, var(--content-bg) 50%, var(--border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: var(--text-light);
}

.quick-actions {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 16px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.action-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  text-decoration: none;
  transition: var(--transition);
  cursor: pointer;
}

.action-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--primary);
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
}

.action-pointage .action-icon {
  background: rgba(232,80,26,0.1);
  color: var(--primary);
}

.action-reunions .action-icon {
  background: rgba(66,153,225,0.1);
  color: var(--info);
}

.action-profil .action-icon {
  background: rgba(159,122,234,0.1);
  color: #9f7aea;
}

.action-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.action-sub {
  font-size: 12px;
  color: var(--text-light);
}

.channel-shortcuts-icon {
  background: rgba(232, 80, 26, 0.1);
  color: var(--primary);
}

.channel-shortcuts-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 4px;
}

.channel-shortcut {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 8px;
  border-radius: var(--radius-sm);
  text-decoration: none;
  color: var(--text);
  font-size: 13px;
  transition: var(--transition);
}

.channel-shortcut:hover {
  background: var(--content-bg);
  color: var(--primary);
}

.shortcut-hash {
  color: var(--primary);
  font-weight: 700;
}

/* Empty state canal card */
.action-card-empty {
  border: 2px dashed var(--border);
  background: transparent;
  box-shadow: none;
  opacity: 0.8;
}
.action-card-empty:hover {
  opacity: 1;
  border-color: var(--primary);
  transform: none;
  box-shadow: none;
}
.empty-hint {
  font-size: 0.78rem;
  color: var(--text-light);
  text-align: center;
  margin: 4px 0 0;
  line-height: 1.4;
}

/* Recent activity */
.recent-section {
  margin-bottom: 32px;
}

.activity-list {
  background: var(--white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  overflow: hidden;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
  transition: background 0.15s ease;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-item:hover {
  background: var(--content-bg);
}

.activity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.dot-message  { background: var(--primary); }
.dot-meeting  { background: var(--info); }
.dot-absence  { background: var(--warning); }
.dot-info     { background: var(--text-light); }

.activity-body {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  min-width: 0;
}

.activity-msg {
  font-size: 13px;
  color: var(--text);
  flex: 1;
  line-height: 1.45;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-time {
  font-size: 11px;
  color: var(--text-light);
  white-space: nowrap;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .welcome-topbar {
    padding: 0 16px;
  }

  .welcome-content {
    padding: 16px;
  }

  .welcome-hero {
    padding: 16px 12px 20px;
  }

  .hero-title {
    font-size: 20px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .stat-card {
    padding: 14px;
    gap: 12px;
  }

  .stat-icon {
    width: 38px;
    height: 38px;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .action-card {
    padding: 14px;
  }

  .desktop-notif {
    display: none;
  }
}
</style>
