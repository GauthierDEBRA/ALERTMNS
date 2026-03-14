<template>
  <div class="pointage-view">
    <!-- Header -->
    <div class="page-topbar">
      <div class="topbar-left">
        <h1 class="page-title">Pointage</h1>
        <span class="page-date">{{ todayFormatted }}</span>
      </div>
      <div class="topbar-right">
        <div class="desktop-notif">
          <NotificationPanel />
        </div>
      </div>
    </div>

    <div class="pointage-content">
      <!-- Big check-in/out button -->
      <div class="checkin-section">
        <div class="checkin-card" :class="{ 'is-present': isPresent }">
          <div class="checkin-status">
            <div class="status-indicator" :class="{ 'online': isPresent }"></div>
            <span>{{ isPresent ? 'Vous êtes présent(e)' : 'Vous êtes absent(e)' }}</span>
          </div>

          <div class="checkin-time" v-if="isPresent">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Arrivé(e) à {{ arrivalTime }}
          </div>

          <button
            class="checkin-btn"
            :class="{ 'btn-depart': isPresent }"
            @click="togglePointage"
            :disabled="loading"
          >
            <svg v-if="!loading" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle v-if="!isPresent" cx="12" cy="12" r="10"/>
              <polyline v-if="!isPresent" points="12 6 12 12 16 14"/>
              <path v-if="isPresent" d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline v-if="isPresent" points="16 17 21 12 16 7"/>
              <line v-if="isPresent" x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            <div v-else class="spinner" style="width:22px;height:22px;border-top-color:white;"></div>
            <span>{{ loading ? 'Traitement...' : (isPresent ? 'Signaler mon départ' : 'Signaler mon arrivée') }}</span>
          </button>

          <p v-if="pointageError" class="checkin-error">
            {{ pointageError }}
          </p>
        </div>
      </div>

      <!-- Stats -->
      <div class="stats-section">
        <h2 class="section-heading">Mes statistiques</h2>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-label">Aujourd'hui</span>
              <div class="stat-icon today-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
              </div>
            </div>
            <div class="stat-value">{{ stats.dailyFormatted || '0h 00min' }}</div>
            <div class="stat-bar">
              <div class="stat-bar-fill" :style="{ width: Math.max(0, Math.min((stats.dailyMinutes || 0) / 480 * 100, 100)) + '%' }"></div>
            </div>
            <div class="stat-target">Objectif: 8h</div>
          </div>

          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-label">Cette semaine</span>
              <div class="stat-icon week-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
            </div>
            <div class="stat-value">{{ stats.weeklyFormatted || '0h 00min' }}</div>
            <div class="stat-bar">
              <div class="stat-bar-fill week-bar" :style="{ width: Math.max(0, Math.min((stats.weeklyMinutes || 0) / 2400 * 100, 100)) + '%' }"></div>
            </div>
            <div class="stat-target">Objectif: 40h</div>
          </div>

          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-label">Ce mois</span>
              <div class="stat-icon month-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
              </div>
            </div>
            <div class="stat-value">{{ stats.monthlyFormatted || '0h 00min' }}</div>
            <div class="stat-bar">
              <div class="stat-bar-fill month-bar" :style="{ width: Math.max(0, Math.min((stats.monthlyMinutes || 0) / 10380 * 100, 100)) + '%' }"></div>
            </div>
            <div class="stat-target">Objectif: ~173h</div>
          </div>
        </div>
      </div>

      <!-- Present employees table -->
      <div class="present-section">
        <div class="section-header-row">
          <h2 class="section-heading">Employés présents ({{ presentUsers.length }})</h2>
          <button class="btn btn-secondary btn-sm" @click="fetchPresent">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 4 23 10 17 10"/>
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
            </svg>
            Actualiser
          </button>
        </div>

        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Employé</th>
                <th>Rôle</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in presentUsers" :key="user.idUser">
                <td>
                  <div class="user-cell">
                    <div class="user-cell-avatar" :style="{ background: getAvatarColor(user) }">
                      {{ `${user.prenom?.[0] || ''}${user.nom?.[0] || ''}`.toUpperCase() }}
                    </div>
                    <div class="user-cell-info">
                      <span class="user-cell-name">{{ user.prenom }} {{ user.nom }}</span>
                      <span class="user-cell-email">{{ user.email }}</span>
                    </div>
                  </div>
                </td>
                <td>
                  <span class="role-badge" :class="getRoleClass(user.role)">{{ user.role }}</span>
                </td>
                <td>
                  <div class="status-cell">
                    <span class="status-dot online"></span>
                    <span class="status-text-cell">Présent</span>
                  </div>
                </td>
              </tr>
              <tr v-if="presentUsers.length === 0">
                <td colspan="3" style="text-align: center; color: var(--text-light); padding: 30px;">
                  Aucun employé présent pour l'instant
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import NotificationPanel from '../components/NotificationPanel.vue'
import api from '../api/axios.js'
import { useAuthStore } from '../stores/auth.js'
import { getAvatarColor } from '../utils/avatar.js'

const authStore = useAuthStore()

const isPresent = ref(false)
const loading = ref(false)
const arrivalTime = ref('')
const pointageError = ref('')
const stats = ref({
  dailyMinutes: 0,
  weeklyMinutes: 0,
  monthlyMinutes: 0,
  dailyFormatted: '0h 00min',
  weeklyFormatted: '0h 00min',
  monthlyFormatted: '0h 00min'
})
const presentUsers = ref([])

const MOIS_LONG = ['janvier', 'février', 'mars', 'avril', 'mai', 'juin', 'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre']
const JOURS = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi']

const todayFormatted = computed(() => {
  const now = new Date()
  return `${JOURS[now.getDay()]} ${now.getDate()} ${MOIS_LONG[now.getMonth()]} ${now.getFullYear()}`
})

function getRoleClass(role) {
  const map = {
    'Admin': 'role-admin',
    'RH': 'role-rh',
    'Collaborateur': 'role-employe',
    'Responsable': 'role-manager'
  }
  return map[role] || 'role-employe'
}

function formatArrivalTime(dateTime) {
  if (!dateTime || typeof dateTime !== 'string') return ''

  const timePart = dateTime.split('T')[1]
  return timePart ? timePart.slice(0, 5) : ''
}

function syncPresenceFromStats(statsData) {
  isPresent.value = Boolean(statsData?.isCurrentlyPresent)
  arrivalTime.value = isPresent.value ? formatArrivalTime(statsData?.arrivedAt) : ''
}

async function fetchStats() {
  try {
    const res = await api.get('/pointage/stats')
    const data = res.data || {}
    // Guard against negative values that can come from the API when the session
    // is still open (endedAt missing). Clamp to zero so the UI stays sensible.
    if ((data.dailyMinutes || 0) < 0) { data.dailyMinutes = 0; data.dailyFormatted = '0h 00min' }
    if ((data.weeklyMinutes || 0) < 0) { data.weeklyMinutes = 0; data.weeklyFormatted = '0h 00min' }
    if ((data.monthlyMinutes || 0) < 0) { data.monthlyMinutes = 0; data.monthlyFormatted = '0h 00min' }
    stats.value = data
    syncPresenceFromStats(data)
  } catch (e) {
    console.error('Error fetching stats:', e)
  }
}

async function fetchPresent() {
  try {
    const res = await api.get('/pointage/presents')
    presentUsers.value = res.data || []
    isPresent.value = presentUsers.value.some(u => u.idUser === authStore.user?.id)
  } catch (e) {
    console.error('Error fetching present users:', e)
  }
}

async function togglePointage() {
  loading.value = true
  pointageError.value = ''
  try {
    if (isPresent.value) {
      await api.post('/pointage/partir')
    } else {
      await api.post('/pointage/arriver')
    }
    await Promise.all([fetchPresent(), fetchStats()])
  } catch (e) {
    console.error('Pointage error:', e)
    pointageError.value = e.response?.data?.message || 'Impossible de mettre a jour le pointage'
  } finally {
    loading.value = false
  }
}

let refreshInterval = null

onMounted(async () => {
  await Promise.all([fetchPresent(), fetchStats()])
  refreshInterval = setInterval(() => Promise.all([fetchPresent(), fetchStats()]), 60000)
})

onUnmounted(() => {
  if (refreshInterval) clearInterval(refreshInterval)
})
</script>

<style scoped>
.pointage-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: var(--content-bg);
}

.page-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.page-date {
  font-size: 14px;
  color: var(--text-light);
}

.desktop-notif :deep(.notif-btn) {
  color: var(--text-light);
}

.pointage-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
}

/* Check-in card */
.checkin-section {
  margin-bottom: 32px;
}

.checkin-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow);
  border: 2px solid var(--border);
  text-align: center;
  transition: var(--transition);
}

.checkin-error {
  margin-top: 12px;
  color: #c53030;
  font-size: 14px;
  font-weight: 600;
}

.checkin-card.is-present {
  border-color: rgba(72, 187, 120, 0.4);
  background: rgba(72, 187, 120, 0.03);
}

.checkin-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-light);
  margin-bottom: 16px;
}

.is-present .checkin-status {
  color: var(--success);
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #a0aec0;
}

.status-indicator.online {
  background: var(--success);
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.25);
}

.checkin-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 24px;
}

.checkin-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 40px;
  border-radius: var(--radius-lg);
  font-size: 16px;
  font-weight: 700;
  background: var(--primary);
  color: white;
  border: none;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: 0 4px 16px rgba(232, 80, 26, 0.35);
  min-width: 280px;
  margin-bottom: 12px;
}

.checkin-btn.btn-depart {
  background: var(--success);
  box-shadow: 0 4px 16px rgba(72, 187, 120, 0.35);
}

.checkin-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(232, 80, 26, 0.45);
}

.checkin-btn.btn-depart:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(72, 187, 120, 0.45);
}

.checkin-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.checkin-hint {
  font-size: 13px;
  color: var(--text-light);
}

/* Stats */
.section-heading {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 16px;
}

.section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header-row .section-heading {
  margin-bottom: 0;
}

.stats-section {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.stat-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-light);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
}

.today-icon {
  background: rgba(232, 80, 26, 0.1);
  color: var(--primary);
}

.week-icon {
  background: rgba(66, 153, 225, 0.1);
  color: var(--info);
}

.month-icon {
  background: rgba(159, 122, 234, 0.1);
  color: #9f7aea;
}

.stat-card .stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text);
  margin-bottom: 12px;
  line-height: 1;
}

.stat-bar {
  height: 6px;
  background: var(--content-bg);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.stat-bar-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.week-bar {
  background: var(--info);
}

.month-bar {
  background: #9f7aea;
}

.stat-target {
  font-size: 11px;
  color: var(--text-light);
}

/* Present section */
.present-section {
  margin-bottom: 32px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-cell-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.user-cell-info {
  display: flex;
  flex-direction: column;
}

.user-cell-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.user-cell-email {
  font-size: 12px;
  color: var(--text-light);
}

.role-badge {
  display: inline-flex;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
}

.role-admin {
  background: rgba(232, 80, 26, 0.1);
  color: var(--primary);
}

.role-rh {
  background: rgba(66, 153, 225, 0.1);
  color: var(--info);
}

.role-employe {
  background: rgba(72, 187, 120, 0.1);
  color: var(--success);
}

.role-manager {
  background: rgba(159, 122, 234, 0.1);
  color: #9f7aea;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-text-cell {
  font-size: 13px;
  color: var(--success);
  font-weight: 500;
}

@media (max-width: 768px) {
  .pointage-content {
    padding: 16px;
  }

  .page-topbar {
    padding: 0 16px;
  }

  .page-date {
    display: none;
  }

  .checkin-card {
    padding: 24px 16px;
  }

  .checkin-btn {
    width: 100%;
    min-width: unset;
    padding: 14px 20px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .section-header-row {
    flex-wrap: wrap;
    gap: 10px;
  }

  .desktop-notif {
    display: none;
  }
}
</style>
