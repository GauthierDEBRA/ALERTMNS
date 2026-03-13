<template>
  <div class="reunions-view">
    <!-- Header -->
    <div class="page-topbar">
      <div class="topbar-left">
        <h1 class="page-title">Réunions</h1>
      </div>
      <div class="topbar-right">
        <button class="btn btn-primary" @click="showCreateModal = true">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Nouvelle réunion
        </button>
        <div class="desktop-notif">
          <NotificationPanel />
        </div>
      </div>
    </div>

    <div class="reunions-content">
      <!-- Loading -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Chargement des réunions...</p>
      </div>

      <template v-else>
        <!-- Pending invitations -->
        <div v-if="pendingReunions.length > 0" class="pending-section">
          <h2 class="section-heading">
            <span class="pending-badge">{{ pendingReunions.length }}</span>
            Invitations en attente
          </h2>
          <div class="invitations-list">
            <div v-for="reunion in pendingReunions" :key="reunion.id" class="invitation-card">
              <div class="invitation-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
              <div class="invitation-info">
                <h3 class="invitation-title">{{ reunion.titre }}</h3>
                <p v-if="reunion.description" class="invitation-desc">{{ reunion.description }}</p>
                <div class="invitation-meta">
                  <span class="meta-item">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                      <line x1="16" y1="2" x2="16" y2="6"/>
                      <line x1="8" y1="2" x2="8" y2="6"/>
                      <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                    {{ formatDateFull(reunion.datePrevue) }}
                  </span>
                  <span v-if="reunion.organisateur" class="meta-item">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                    {{ reunion.organisateur.prenom }} {{ reunion.organisateur.nom }}
                  </span>
                </div>
              </div>
              <div class="invitation-actions">
                <button
                  class="btn btn-success btn-sm"
                  @click="respondToReunion(reunion.id, 'accepte')"
                  :disabled="respondingId === reunion.id"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  Accepter
                </button>
                <button
                  class="btn btn-danger btn-sm"
                  @click="respondToReunion(reunion.id, 'refuse')"
                  :disabled="respondingId === reunion.id"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                  Refuser
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- All reunions list -->
        <div class="all-section">
          <h2 class="section-heading">Toutes mes réunions</h2>

          <div v-if="allReunions.length === 0" class="empty-state">
            <div class="empty-state-icon">📅</div>
            <h3>Aucune réunion</h3>
            <p>Vous n'avez aucune réunion planifiée. Créez-en une !</p>
            <button class="btn btn-primary" @click="showCreateModal = true">
              Créer une réunion
            </button>
          </div>

          <div v-else class="reunions-grid">
            <div
              v-for="reunion in sortedReunions"
              :key="reunion.id"
              class="reunion-card"
              :class="getReunionClass(reunion)"
            >
              <div class="reunion-card-header">
                <div class="reunion-status-badge" :class="getStatusClass(reunion)">
                  {{ getStatusLabel(reunion) }}
                </div>
                <span class="reunion-date-chip">{{ formatDateShort(reunion.datePrevue) }}</span>
              </div>

              <div class="reunion-card-body">
                <h3 class="reunion-title">{{ reunion.titre }}</h3>
                <p v-if="reunion.description" class="reunion-desc">{{ reunion.description }}</p>
              </div>

              <div class="reunion-card-footer">
                <div class="reunion-meta">
                  <span class="meta-item">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/>
                      <polyline points="12 6 12 12 16 14"/>
                    </svg>
                    {{ formatTimeOnly(reunion.datePrevue) }}
                  </span>
                  <span v-if="reunion.organisateur" class="meta-item">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                    {{ reunion.organisateur.prenom }} {{ reunion.organisateur.nom }}
                  </span>
                </div>

                <div v-if="getMyStatus(reunion) === 'en_attente'" class="reunion-actions">
                  <button
                    class="action-btn accept-btn"
                    @click="respondToReunion(reunion.id, 'accepte')"
                    :disabled="respondingId === reunion.id"
                  >
                    Accepter
                  </button>
                  <button
                    class="action-btn refuse-btn"
                    @click="respondToReunion(reunion.id, 'refuse')"
                    :disabled="respondingId === reunion.id"
                  >
                    Refuser
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Create Reunion Modal -->
    <Teleport to="body">
      <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Nouvelle réunion</h3>
            <button class="modal-close" @click="showCreateModal = false">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="createError" class="alert alert-error">{{ createError }}</div>

            <div class="form-group">
              <label class="form-label">Titre <span style="color:var(--danger)">*</span></label>
              <input
                v-model="form.titre"
                class="form-input"
                type="text"
                placeholder="Titre de la réunion"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">Description</label>
              <textarea
                v-model="form.description"
                class="form-input"
                rows="3"
                placeholder="Description optionnelle..."
                style="resize: vertical;"
              ></textarea>
            </div>

            <div class="form-group">
              <label class="form-label">Date et heure <span style="color:var(--danger)">*</span></label>
              <input
                v-model="form.datePrevue"
                class="form-input"
                type="datetime-local"
                required
              />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showCreateModal = false">Annuler</button>
            <button
              class="btn btn-primary"
              @click="createReunion"
              :disabled="!form.titre || !form.datePrevue || creating"
            >
              {{ creating ? 'Création...' : 'Créer la réunion' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import NotificationPanel from '../components/NotificationPanel.vue'
import { useAuthStore } from '../stores/auth.js'
import { useDate } from '../composables/useDate.js'
import api from '../api/axios.js'

const authStore = useAuthStore()
const { formatDateFull, formatDateShort } = useDate()

const reunions = ref([])
const loading = ref(false)
const showCreateModal = ref(false)
const creating = ref(false)
const createError = ref('')
const respondingId = ref(null)

const form = ref({
  titre: '',
  description: '',
  datePrevue: ''
})

const MOIS = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc']

function formatTimeOnly(dateInput) {
  if (!dateInput) return ''
  return new Date(dateInput).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
}

function getMyStatus(reunion) {
  if (!reunion.participants) return 'en_attente'
  const me = reunion.participants.find(p => p.userId === authStore.user?.id || p.id === authStore.user?.id)
  return me?.statut || 'en_attente'
}

function getReunionClass(reunion) {
  const status = getMyStatus(reunion)
  return {
    'reunion-accepted': status === 'accepte',
    'reunion-refused': status === 'refuse',
    'reunion-pending': status === 'en_attente'
  }
}

function getStatusClass(reunion) {
  const status = getMyStatus(reunion)
  return {
    'status-accepted': status === 'accepte',
    'status-refused': status === 'refuse',
    'status-pending': status === 'en_attente'
  }
}

function getStatusLabel(reunion) {
  const status = getMyStatus(reunion)
  const map = {
    'accepte': 'Acceptée',
    'refuse': 'Refusée',
    'en_attente': 'En attente'
  }
  return map[status] || 'En attente'
}

const allReunions = computed(() => reunions.value)

const pendingReunions = computed(() =>
  reunions.value.filter(r => getMyStatus(r) === 'en_attente' && !isOrganizer(r))
)

const sortedReunions = computed(() =>
  [...reunions.value].sort((a, b) => new Date(a.datePrevue) - new Date(b.datePrevue))
)

function isOrganizer(reunion) {
  return reunion.organisateur?.id === authStore.user?.id ||
    reunion.createurId === authStore.user?.id
}

async function fetchReunions() {
  loading.value = true
  try {
    const res = await api.get('/reunions')
    reunions.value = res.data || []
  } catch (e) {
    console.error('Error fetching reunions:', e)
  } finally {
    loading.value = false
  }
}

async function createReunion() {
  if (!form.value.titre || !form.value.datePrevue) return
  creating.value = true
  createError.value = ''

  try {
    await api.post('/reunions', {
      titre: form.value.titre,
      description: form.value.description,
      datePrevue: form.value.datePrevue
    })
    showCreateModal.value = false
    form.value = { titre: '', description: '', datePrevue: '' }
    await fetchReunions()
  } catch (e) {
    createError.value = e.response?.data?.message || 'Erreur lors de la création'
  } finally {
    creating.value = false
  }
}

async function respondToReunion(id, statut) {
  respondingId.value = id
  try {
    await api.post(`/reunions/${id}/statut`, { statut })
    await fetchReunions()
  } catch (e) {
    console.error('Error responding to reunion:', e)
  } finally {
    respondingId.value = null
  }
}

onMounted(fetchReunions)
</script>

<style scoped>
.reunions-view {
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

.topbar-left .page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.desktop-notif :deep(.notif-btn) {
  color: var(--text-light);
}

.reunions-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  max-width: 1000px;
  width: 100%;
  margin: 0 auto;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 12px;
  color: var(--text-light);
}

.section-heading {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.pending-badge {
  background: var(--warning);
  color: white;
  font-size: 12px;
  font-weight: 700;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.pending-section {
  margin-bottom: 32px;
}

.invitations-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.invitation-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow);
  border-left: 4px solid var(--warning);
}

.invitation-icon {
  width: 44px;
  height: 44px;
  background: rgba(237, 137, 54, 0.1);
  color: var(--warning);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.invitation-info {
  flex: 1;
  min-width: 0;
}

.invitation-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 4px;
}

.invitation-desc {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.invitation-meta {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-light);
}

.invitation-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* Reunions grid */
.all-section {
  margin-bottom: 32px;
}

.reunions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.reunion-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  border-top: 4px solid var(--border);
  transition: var(--transition);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.reunion-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.reunion-accepted {
  border-top-color: var(--success);
}

.reunion-refused {
  border-top-color: var(--danger);
  opacity: 0.7;
}

.reunion-pending {
  border-top-color: var(--warning);
}

.reunion-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.reunion-status-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-accepted {
  background: rgba(72, 187, 120, 0.12);
  color: var(--success);
}

.status-refused {
  background: rgba(245, 101, 101, 0.12);
  color: var(--danger);
}

.status-pending {
  background: rgba(237, 137, 54, 0.12);
  color: var(--warning);
}

.reunion-date-chip {
  font-size: 12px;
  color: var(--text-light);
  background: var(--content-bg);
  padding: 4px 10px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border);
}

.reunion-card-body {
  flex: 1;
}

.reunion-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 6px;
}

.reunion-desc {
  font-size: 13px;
  color: var(--text-light);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.reunion-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.reunion-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.reunion-actions {
  display: flex;
  gap: 6px;
}

.action-btn {
  padding: 5px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: var(--transition);
}

.accept-btn {
  background: rgba(72, 187, 120, 0.15);
  color: var(--success);
}

.accept-btn:hover:not(:disabled) {
  background: var(--success);
  color: white;
}

.refuse-btn {
  background: rgba(245, 101, 101, 0.15);
  color: var(--danger);
}

.refuse-btn:hover:not(:disabled) {
  background: var(--danger);
  color: white;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .page-topbar {
    padding: 0 16px;
    flex-wrap: wrap;
    height: auto;
    min-height: 60px;
    gap: 8px;
    padding-top: 8px;
    padding-bottom: 8px;
  }

  .reunions-content {
    padding: 16px;
  }

  .invitation-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .invitation-actions {
    width: 100%;
    justify-content: stretch;
  }

  .invitation-actions .btn {
    flex: 1;
    justify-content: center;
  }

  .reunions-grid {
    grid-template-columns: 1fr;
  }

  .desktop-notif {
    display: none;
  }
}
</style>
