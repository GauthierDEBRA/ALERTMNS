<template>
  <div class="reunions-view">
    <!-- Header -->
    <div class="page-topbar">
      <div class="topbar-left">
        <h1 class="page-title">Réunions</h1>
      </div>
      <div class="topbar-right">
        <button class="btn btn-primary" @click="openCreateModal">
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
        <div v-if="responseSuccess" class="alert alert-success page-alert">{{ responseSuccess }}</div>
        <div v-if="responseError" class="alert alert-error page-alert">{{ responseError }}</div>

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
                  <span v-if="reunion.lieu" class="meta-item">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z"/>
                      <circle cx="12" cy="10" r="3"/>
                    </svg>
                    {{ reunion.lieu }}
                  </span>
                </div>
                <a
                  v-if="reunion.lienVisio"
                  :href="reunion.lienVisio"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="meeting-link"
                >
                  Ouvrir le lien visio
                </a>
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
          <div class="section-heading-row">
            <h2 class="section-heading">Toutes mes réunions</h2>
            <div class="meeting-filters">
              <button
                v-for="filter in reunionFilters"
                :key="filter.id"
                class="filter-btn"
                :class="{ 'filter-btn-active': activeFilter === filter.id }"
                @click="activeFilter = filter.id"
              >
                {{ filter.label }}
              </button>
            </div>
          </div>

          <div v-if="filteredReunions.length === 0" class="empty-state">
            <div class="empty-state-icon">📅</div>
            <h3>Aucune réunion</h3>
            <p>Vous n'avez aucune réunion planifiée. Créez-en une !</p>
            <button class="btn btn-primary" @click="openCreateModal">
              Créer une réunion
            </button>
          </div>

          <div v-else class="reunions-grid">
            <div
              v-for="reunion in filteredReunions"
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
                <div v-if="reunion.lieu || reunion.lienVisio" class="reunion-details">
                  <p v-if="reunion.lieu" class="detail-line">
                    <strong>Lieu :</strong> {{ reunion.lieu }}
                  </p>
                  <a
                    v-if="reunion.lienVisio"
                    :href="reunion.lienVisio"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="meeting-link"
                  >
                    Rejoindre la visio
                  </a>
                </div>
              </div>

              <div class="reunion-card-footer">
                <div class="reunion-meta">
                  <span class="meta-item">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/>
                      <polyline points="12 6 12 12 16 14"/>
                    </svg>
                    {{ formatMeetingSlot(reunion) }}
                  </span>
                  <span v-if="reunion.organisateur" class="meta-item">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                    {{ reunion.organisateur.prenom }} {{ reunion.organisateur.nom }}
                  </span>
                </div>

                <div class="reunion-footer-right">
                  <div v-if="getMyStatus(reunion) === 'en_attente'" class="reunion-actions">
                    <button
                      class="action-btn accept-btn"
                      @click="respondToReunion(reunion.id, 'accepte')"
                      :disabled="respondingId === reunion.id"
                    >Accepter</button>
                    <button
                      class="action-btn refuse-btn"
                      @click="respondToReunion(reunion.id, 'refuse')"
                      :disabled="respondingId === reunion.id"
                    >Refuser</button>
                  </div>

                  <!-- Non-organizer: simple participants button -->
                  <button
                    v-if="!isOrganizer(reunion)"
                    class="action-btn secondary-btn"
                    @click="openParticipantsModal(reunion)"
                  >Voir les participants</button>

                  <!-- Organizer: ⋮ dropdown menu -->
                  <div v-if="isOrganizer(reunion)" class="reunion-menu-wrapper">
                    <button
                      class="action-btn menu-btn"
                      @click="toggleMenu(reunion.id)"
                      :disabled="deletingId === reunion.id"
                      title="Actions"
                    >⋮</button>
                    <div v-if="openMenuId === reunion.id" class="reunion-menu-dropdown">
                      <button class="menu-item" @click.stop="openEditModal(reunion); openMenuId = null">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                        Modifier
                      </button>
                      <button class="menu-item" @click.stop="openParticipantsModal(reunion); openMenuId = null">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                          <circle cx="9" cy="7" r="4"/>
                          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                        </svg>
                        Gérer les participants
                      </button>
                      <button class="menu-item" @click.stop="sendReminder(reunion); openMenuId = null">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M15 17h5l-1.4-1.4A2 2 0 0 1 18 14.2V11a6 6 0 1 0-12 0v3.2a2 2 0 0 1-.6 1.4L4 17h5"/>
                          <path d="M10 21a2 2 0 0 0 4 0"/>
                        </svg>
                        Envoyer un rappel
                      </button>
                      <div class="menu-divider"></div>
                      <button class="menu-item menu-item-danger" @click.stop="deleteReunion(reunion); openMenuId = null">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                        </svg>
                        {{ deletingId === reunion.id ? 'Suppression...' : 'Supprimer' }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Create Reunion Modal -->
    <Teleport to="body">
      <div v-if="showCreateModal" class="modal-overlay" @click.self="closeCreateModal">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">{{ isEditingReunion ? 'Modifier la réunion' : 'Nouvelle réunion' }}</h3>
            <button class="modal-close" @click="closeCreateModal">✕</button>
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

            <div class="form-group">
              <label class="form-label">Durée</label>
              <select v-model.number="form.dureeMinutes" class="form-input">
                <option v-for="duration in durationOptions" :key="duration" :value="duration">
                  {{ formatDuration(duration) }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Lieu</label>
              <input
                v-model="form.lieu"
                class="form-input"
                type="text"
                placeholder="Salle de réunion, bureau, adresse..."
              />
            </div>

            <div class="form-group">
              <label class="form-label">Lien visio</label>
              <input
                v-model="form.lienVisio"
                class="form-input"
                type="url"
                placeholder="https://meet..."
              />
            </div>

            <p class="modal-helper">
              Des rappels automatiques sont envoyés 24h, 1h puis 30 min avant la réunion aux participants concernés.
            </p>

            <div v-if="!isEditingReunion" class="form-group">
              <label class="form-label">Participants à inviter</label>

              <input
                v-model="participantSearch"
                class="form-input"
                type="text"
                placeholder="Rechercher un utilisateur..."
              />

              <div class="invitees-toolbar">
                <span class="invitees-count">
                  {{ selectedInvitees.length }} sélectionné{{ selectedInvitees.length > 1 ? 's' : '' }}
                </span>
                <button
                  v-if="selectedInvitees.length > 0"
                  type="button"
                  class="link-btn"
                  @click="selectedInvitees = []"
                >
                  Tout retirer
                </button>
              </div>

              <div v-if="usersLoading" class="invitees-state">Chargement des utilisateurs...</div>
              <div v-else-if="inviteableUsers.length === 0" class="invitees-state">
                {{ participantSearch ? 'Aucun utilisateur ne correspond à votre recherche.' : 'Aucun autre utilisateur actif à inviter.' }}
              </div>
              <div v-else class="invitees-list">
                <label v-for="user in inviteableUsers" :key="user.idUser" class="invitee-option">
                  <input
                    v-model="selectedInvitees"
                    type="checkbox"
                    :value="user.idUser"
                  />
                  <div class="invitee-option-body">
                    <span class="invitee-name">{{ user.prenom }} {{ user.nom }}</span>
                    <span class="invitee-meta">{{ user.role }}<template v-if="user.structureNom"> • {{ user.structureNom }}</template></span>
                  </div>
                </label>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeCreateModal">Annuler</button>
            <button
              class="btn btn-primary"
              @click="createReunion"
              :disabled="!form.titre || !form.datePrevue || creating"
            >
              {{ creating ? (isEditingReunion ? 'Enregistrement...' : 'Création...') : (isEditingReunion ? 'Enregistrer' : 'Créer la réunion') }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showParticipantsModal" class="modal-overlay" @click.self="closeParticipantsModal">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Participants - {{ selectedReunion?.titre || 'Réunion' }}</h3>
            <button class="modal-close" @click="closeParticipantsModal">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="participantsSuccess" class="alert alert-success">{{ participantsSuccess }}</div>
            <div v-if="participantsError" class="alert alert-error">{{ participantsError }}</div>

            <div v-if="participantsLoading" class="invitees-state">Chargement des participants...</div>
            <div v-else-if="participants.length === 0" class="invitees-state">Aucun participant trouvé pour cette réunion.</div>
            <div v-else class="participants-list">
              <div v-for="participant in participants" :key="participant.userId" class="participant-row">
                <div class="participant-main">
                  <div class="participant-header-row">
                    <span class="participant-name">{{ participant.prenom }} {{ participant.nom }}</span>
                    <span class="reunion-status-badge" :class="getParticipantStatusClass(participant.statut)">
                      {{ getParticipantStatusLabel(participant.statut) }}
                    </span>
                  </div>
                  <div class="participant-meta">
                    {{ participant.role || 'Utilisateur' }}
                    <template v-if="participant.structureNom"> • {{ participant.structureNom }}</template>
                    <template v-if="participant.email"> • {{ participant.email }}</template>
                  </div>
                </div>

                <button
                  v-if="canManageSelectedReunion && !participant.isOrganizer"
                  class="action-btn refuse-btn"
                  @click="removeParticipantFromSelectedReunion(participant.userId)"
                  :disabled="removingParticipantId === participant.userId || addingParticipant"
                >
                  {{ removingParticipantId === participant.userId ? 'Retrait...' : 'Retirer' }}
                </button>
              </div>
            </div>

            <div v-if="canManageSelectedReunion" class="form-group participant-add-group">
              <label class="form-label">Ajouter un participant</label>
              <select v-model="selectedParticipantId" class="form-input">
                <option value="">Sélectionner un utilisateur</option>
                <option v-for="user in availableUsersForSelectedReunion" :key="user.idUser" :value="user.idUser">
                  {{ user.prenom }} {{ user.nom }}<template v-if="user.structureNom"> • {{ user.structureNom }}</template>
                </option>
              </select>

              <div v-if="availableUsersForSelectedReunion.length === 0" class="invitees-state compact-state">
                Tous les utilisateurs actifs sont déjà invités.
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeParticipantsModal">Fermer</button>
            <button
              v-if="canManageSelectedReunion"
              class="btn btn-secondary"
              @click="sendReminder(selectedReunion)"
              :disabled="reminderSendingId === selectedReunion?.id"
            >
              {{ reminderSendingId === selectedReunion?.id ? 'Envoi...' : 'Envoyer un rappel' }}
            </button>
            <button
              v-if="canManageSelectedReunion"
              class="btn btn-primary"
              @click="addParticipantToSelectedReunion"
              :disabled="!selectedParticipantId || addingParticipant || availableUsersForSelectedReunion.length === 0"
            >
              {{ addingParticipant ? 'Invitation...' : 'Inviter' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import NotificationPanel from '../components/NotificationPanel.vue'
import { useAuthStore } from '../stores/auth.js'
import { useDate } from '../composables/useDate.js'
import api from '../api/axios.js'

const authStore = useAuthStore()
const { formatDateFull, formatDateShort } = useDate()

const reunions = ref([])
const activeUsers = ref([])
const loading = ref(false)
const usersLoading = ref(false)
const showCreateModal = ref(false)
const editingReunionId = ref(null)
const creating = ref(false)
const createError = ref('')
const respondingId = ref(null)
const responseSuccess = ref('')
const responseError = ref('')
const deletingId = ref(null)
const reminderSendingId = ref(null)
const showParticipantsModal = ref(false)
const participantsLoading = ref(false)
const participantsError = ref('')
const participantsSuccess = ref('')
const addingParticipant = ref(false)
const removingParticipantId = ref(null)
const selectedReunion = ref(null)
const participants = ref([])
const selectedParticipantId = ref('')
const participantSearch = ref('')
const selectedInvitees = ref([])
const activeFilter = ref('all')
const openMenuId = ref(null)

function toggleMenu(id) {
  openMenuId.value = openMenuId.value === id ? null : id
}

const form = ref({
  titre: '',
  description: '',
  datePrevue: '',
  dureeMinutes: 60,
  lieu: '',
  lienVisio: ''
})

const MOIS = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc']
const durationOptions = [15, 30, 45, 60, 90, 120, 180, 240]
const reunionFilters = [
  { id: 'all', label: 'Toutes' },
  { id: 'upcoming', label: 'À venir' },
  { id: 'past', label: 'Passées' },
  { id: 'organized', label: 'Organisées par moi' }
]

const isEditingReunion = computed(() => editingReunionId.value !== null)

function formatTimeOnly(dateInput) {
  if (!dateInput) return ''
  return new Date(dateInput).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
}

function formatDuration(durationMinutes) {
  const minutes = Number(durationMinutes) || 60
  const hours = Math.floor(minutes / 60)
  const remaining = minutes % 60
  if (hours === 0) return `${remaining} min`
  if (remaining === 0) return `${hours}h`
  return `${hours}h${String(remaining).padStart(2, '0')}`
}

function formatMeetingSlot(reunion) {
  if (!reunion?.datePrevue) return ''
  return `${formatTimeOnly(reunion.datePrevue)} • ${formatDuration(reunion.dureeMinutes)}`
}

function formatForDateTimeLocal(dateInput) {
  if (!dateInput) return ''
  const date = new Date(dateInput)
  if (Number.isNaN(date.getTime())) {
    return String(dateInput).slice(0, 16)
  }

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

function isSameId(left, right) {
  const leftId = Number(left)
  const rightId = Number(right)
  return Number.isInteger(leftId) && leftId === rightId
}

function normalizeStatus(status, organizer = false) {
  if (organizer) return 'organisateur'
  if (!status) return 'en_attente'

  switch (String(status).trim().toLowerCase()) {
    case 'organisateur':
      return 'organisateur'
    case 'invite':
    case 'invité':
    case 'en attente':
    case 'en_attente':
      return 'en_attente'
    case 'accepte':
    case 'accepté':
    case 'acceptée':
    case 'acceptee':
      return 'accepte'
    case 'refuse':
    case 'refusé':
    case 'refusée':
    case 'refusee':
      return 'refuse'
    default:
      return String(status).trim().toLowerCase()
  }
}

function normalizeUser(user) {
  if (!user) return null
  return {
    ...user,
    id: user.id ?? user.idUser ?? null,
    nom: user.nom ?? '',
    prenom: user.prenom ?? ''
  }
}

function extractParticipantStatus(reunion) {
  if (!Array.isArray(reunion.participants)) return null

  const me = reunion.participants.find((participant) =>
    isSameId(
      participant.userId ?? participant.idUser ?? participant.id?.idUser ?? participant.utilisateur?.idUser ?? participant.utilisateur?.id,
      authStore.user?.id
    )
  )

  return me?.statut ?? null
}

function normalizeReunion(reunion) {
  const organisateur = normalizeUser(reunion.organisateur ?? reunion.createur)
  const createurId = reunion.createurId ?? organisateur?.id ?? reunion.createur?.idUser ?? reunion.createur?.id ?? null
  const organizer = isSameId(organisateur?.id, authStore.user?.id) || isSameId(createurId, authStore.user?.id)

  return {
    ...reunion,
    id: reunion.id ?? reunion.idReunion ?? null,
    organisateur,
    createurId,
    dureeMinutes: Number(reunion.dureeMinutes) || 60,
    myStatus: normalizeStatus(reunion.myStatus ?? extractParticipantStatus(reunion), organizer)
  }
}

function getMyStatus(reunion) {
  const organizer = isSameId(reunion?.organisateur?.id, authStore.user?.id) ||
    isSameId(reunion?.createurId, authStore.user?.id)
  return normalizeStatus(reunion?.myStatus, organizer)
}

function getReunionClass(reunion) {
  const status = getMyStatus(reunion)
  return {
    'reunion-organizer': status === 'organisateur',
    'reunion-accepted': status === 'accepte',
    'reunion-refused': status === 'refuse',
    'reunion-pending': status === 'en_attente'
  }
}

function getStatusClass(reunion) {
  const status = getMyStatus(reunion)
  return {
    'status-organizer': status === 'organisateur',
    'status-accepted': status === 'accepte',
    'status-refused': status === 'refuse',
    'status-pending': status === 'en_attente'
  }
}

function getParticipantStatusClass(status) {
  const normalizedStatus = normalizeStatus(status)
  return {
    'status-organizer': normalizedStatus === 'organisateur',
    'status-accepted': normalizedStatus === 'accepte',
    'status-refused': normalizedStatus === 'refuse',
    'status-pending': normalizedStatus === 'en_attente'
  }
}

function getStatusLabel(reunion) {
  const status = getMyStatus(reunion)
  const map = {
    'organisateur': 'Organisateur',
    'accepte': 'Acceptée',
    'refuse': 'Refusée',
    'en_attente': 'En attente'
  }
  return map[status] || 'En attente'
}

function getParticipantStatusLabel(status) {
  const normalizedStatus = normalizeStatus(status)
  const map = {
    'organisateur': 'Organisateur',
    'accepte': 'Accepté',
    'refuse': 'Refusé',
    'en_attente': 'En attente'
  }
  return map[normalizedStatus] || 'En attente'
}

const allReunions = computed(() => reunions.value)

const inviteableUsers = computed(() => {
  const search = participantSearch.value.trim().toLowerCase()

  return activeUsers.value
    .filter(user => !isSameId(user.idUser, authStore.user?.id))
    .filter((user) => {
      if (!search) return true
      const haystack = `${user.prenom || ''} ${user.nom || ''} ${user.email || ''} ${user.role || ''} ${user.structureNom || ''}`.toLowerCase()
      return haystack.includes(search)
    })
    .sort((left, right) => `${left.prenom} ${left.nom}`.localeCompare(`${right.prenom} ${right.nom}`, 'fr'))
})

const canManageSelectedReunion = computed(() =>
  !!selectedReunion.value && isOrganizer(selectedReunion.value)
)

const availableUsersForSelectedReunion = computed(() => {
  const participantIds = new Set(participants.value.map((participant) => Number(participant.userId)))

  return activeUsers.value
    .filter((user) => !participantIds.has(Number(user.idUser)))
    .sort((left, right) => `${left.prenom} ${left.nom}`.localeCompare(`${right.prenom} ${right.nom}`, 'fr'))
})

const pendingReunions = computed(() =>
  reunions.value.filter(r => getMyStatus(r) === 'en_attente' && !isOrganizer(r))
)

const sortedReunions = computed(() =>
  [...reunions.value].sort((a, b) => new Date(a.datePrevue) - new Date(b.datePrevue))
)

const filteredReunions = computed(() => {
  const now = new Date()

  return sortedReunions.value.filter((reunion) => {
    const meetingDate = new Date(reunion.datePrevue)

    switch (activeFilter.value) {
      case 'upcoming':
        return meetingDate >= now
      case 'past':
        return meetingDate < now
      case 'organized':
        return isOrganizer(reunion)
      default:
        return true
    }
  })
})

function isOrganizer(reunion) {
  return normalizeStatus(reunion?.myStatus) === 'organisateur' ||
    isSameId(reunion?.organisateur?.id, authStore.user?.id) ||
    isSameId(reunion?.createurId, authStore.user?.id)
}

async function fetchReunions() {
  loading.value = true
  responseError.value = ''
  try {
    const res = await api.get('/reunions/mes-reunions')
    reunions.value = (res.data || []).map(normalizeReunion).filter(reunion => reunion.id)
  } catch (e) {
    console.error('Error fetching reunions:', e)
    responseError.value = e.response?.data?.message || 'Impossible de charger les réunions.'
  } finally {
    loading.value = false
  }
}

async function fetchActiveUsers() {
  usersLoading.value = true
  try {
    const res = await api.get('/users/actifs')
    activeUsers.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('Error fetching active users:', e)
    createError.value = e.response?.data?.message || "Impossible de charger la liste des utilisateurs."
  } finally {
    usersLoading.value = false
  }
}

function normalizeParticipant(participant) {
  if (!participant) return null

  return {
    userId: participant.userId ?? participant.idUser ?? participant.id?.idUser ?? participant.utilisateur?.idUser ?? null,
    nom: participant.nom ?? participant.utilisateur?.nom ?? '',
    prenom: participant.prenom ?? participant.utilisateur?.prenom ?? '',
    email: participant.email ?? participant.utilisateur?.email ?? '',
    role: participant.role ?? participant.utilisateur?.role ?? '',
    structureNom: participant.structureNom ?? participant.utilisateur?.structureNom ?? participant.utilisateur?.structure?.nom ?? '',
    statut: normalizeStatus(participant.statut, participant.isOrganizer),
    isOrganizer: participant.isOrganizer ?? normalizeStatus(participant.statut) === 'organisateur'
  }
}

async function openCreateModal() {
  editingReunionId.value = null
  showCreateModal.value = true
  createError.value = ''
  participantSearch.value = ''
  selectedInvitees.value = []
  responseSuccess.value = ''
  await fetchActiveUsers()
}

function resetCreateForm() {
  form.value = { titre: '', description: '', datePrevue: '', dureeMinutes: 60, lieu: '', lienVisio: '' }
  participantSearch.value = ''
  selectedInvitees.value = []
}

function closeCreateModal() {
  showCreateModal.value = false
  editingReunionId.value = null
  createError.value = ''
  resetCreateForm()
}

function openEditModal(reunion) {
  editingReunionId.value = reunion.id
  createError.value = ''
  responseSuccess.value = ''
  responseError.value = ''
  form.value = {
    titre: reunion.titre || '',
    description: reunion.description || '',
    datePrevue: formatForDateTimeLocal(reunion.datePrevue),
    dureeMinutes: reunion.dureeMinutes || 60,
    lieu: reunion.lieu || '',
    lienVisio: reunion.lienVisio || ''
  }
  showCreateModal.value = true
}

async function loadParticipants(reunionId) {
  if (!reunionId) return

  participantsLoading.value = true
  participantsError.value = ''

  try {
    const res = await api.get(`/reunions/${reunionId}/participants`)
    participants.value = (res.data || []).map(normalizeParticipant).filter((participant) => participant?.userId)
  } catch (e) {
    console.error('Error fetching participants:', e)
    participantsError.value = e.response?.data?.message || 'Impossible de charger les participants.'
    participants.value = []
  } finally {
    participantsLoading.value = false
  }
}

async function openParticipantsModal(reunion) {
  selectedReunion.value = reunion
  selectedParticipantId.value = ''
  participantsError.value = ''
  participantsSuccess.value = ''
  showParticipantsModal.value = true
  await loadParticipants(reunion.id)
}

function closeParticipantsModal() {
  showParticipantsModal.value = false
  participantsError.value = ''
  participantsSuccess.value = ''
  selectedParticipantId.value = ''
  participants.value = []
  selectedReunion.value = null
  addingParticipant.value = false
  removingParticipantId.value = null
}

async function addParticipantToSelectedReunion() {
  if (!selectedReunion.value?.id || !selectedParticipantId.value) return

  addingParticipant.value = true
  participantsError.value = ''
  participantsSuccess.value = ''

  try {
    await api.post(`/reunions/${selectedReunion.value.id}/inviter`, {
      userId: Number(selectedParticipantId.value)
    })

    const addedUser = activeUsers.value.find((user) => isSameId(user.idUser, selectedParticipantId.value))
    selectedParticipantId.value = ''
    await loadParticipants(selectedReunion.value.id)
    await fetchReunions()

    participantsSuccess.value = addedUser
      ? `${addedUser.prenom} ${addedUser.nom} a bien été invité(e).`
      : 'Participant invité.'
  } catch (e) {
    console.error('Error inviting participant:', e)
    participantsError.value = e.response?.data?.message || "Impossible d'inviter ce participant."
  } finally {
    addingParticipant.value = false
  }
}

async function removeParticipantFromSelectedReunion(userId) {
  if (!selectedReunion.value?.id || !userId) return

  removingParticipantId.value = userId
  participantsError.value = ''
  participantsSuccess.value = ''

  try {
    const removedUser = participants.value.find((participant) => isSameId(participant.userId, userId))
    await api.delete(`/reunions/${selectedReunion.value.id}/participants/${userId}`)
    await loadParticipants(selectedReunion.value.id)
    await fetchReunions()

    participantsSuccess.value = removedUser
      ? `${removedUser.prenom} ${removedUser.nom} a été retiré(e) de la réunion.`
      : 'Participant retiré.'
  } catch (e) {
    console.error('Error removing participant:', e)
    participantsError.value = e.response?.data?.message || 'Impossible de retirer ce participant.'
  } finally {
    removingParticipantId.value = null
  }
}

async function createReunion() {
  if (!form.value.titre || !form.value.datePrevue) return
  creating.value = true
  createError.value = ''
  responseSuccess.value = ''
  responseError.value = ''

  try {
    const payload = {
      titre: form.value.titre,
      description: form.value.description,
      datePrevue: form.value.datePrevue,
      dureeMinutes: Number(form.value.dureeMinutes) || 60,
      lieu: form.value.lieu,
      lienVisio: form.value.lienVisio
    }

    if (!isEditingReunion.value) {
      payload.participantIds = [...new Set(selectedInvitees.value
        .map((id) => Number(id))
        .filter((id) => Number.isInteger(id) && id > 0))]
    }

    if (isEditingReunion.value) {
      await api.put(`/reunions/${editingReunionId.value}`, payload)
      responseSuccess.value = 'Réunion mise à jour.'
    } else {
      await api.post('/reunions', payload)
      const inviteCount = Array.isArray(payload.participantIds) ? payload.participantIds.length : 0
      responseSuccess.value = inviteCount > 0
        ? `Réunion créée et ${inviteCount} invitation${inviteCount > 1 ? 's' : ''} envoyée${inviteCount > 1 ? 's' : ''}.`
        : 'Réunion créée.'
    }

    await fetchReunions()
    closeCreateModal()
  } catch (e) {
    createError.value = e.response?.data?.message || (isEditingReunion.value ? 'Erreur lors de la modification' : 'Erreur lors de la création')
  } finally {
    creating.value = false
  }
}

async function respondToReunion(id, statut) {
  if (!id) {
    responseError.value = "L'identifiant de la réunion est introuvable."
    return
  }

  respondingId.value = id
  responseSuccess.value = ''
  responseError.value = ''
  try {
    await api.post(`/reunions/${id}/statut`, { statut })
    await fetchReunions()
    responseSuccess.value = statut === 'accepte' ? 'Réunion acceptée.' : 'Réunion refusée.'
  } catch (e) {
    console.error('Error responding to reunion:', e)
    responseError.value = e.response?.data?.message || "Impossible d'enregistrer votre réponse."
  } finally {
    respondingId.value = null
  }
}

async function deleteReunion(reunion) {
  if (!reunion?.id) return

  const confirmed = window.confirm(`Supprimer la réunion "${reunion.titre}" ?`)
  if (!confirmed) return

  deletingId.value = reunion.id
  responseSuccess.value = ''
  responseError.value = ''

  try {
    await api.delete(`/reunions/${reunion.id}`)
    if (selectedReunion.value?.id === reunion.id) {
      closeParticipantsModal()
    }
    await fetchReunions()
    responseSuccess.value = 'Réunion supprimée.'
  } catch (e) {
    console.error('Error deleting reunion:', e)
    responseError.value = e.response?.data?.message || 'Impossible de supprimer la réunion.'
  } finally {
    deletingId.value = null
  }
}

async function sendReminder(reunion) {
  if (!reunion?.id) return

  reminderSendingId.value = reunion.id
  responseSuccess.value = ''
  responseError.value = ''
  participantsSuccess.value = ''
  participantsError.value = ''

  try {
    await api.post(`/reunions/${reunion.id}/rappel`)
    const successMessage = 'Rappel envoyé aux participants concernés.'
    responseSuccess.value = successMessage
    if (selectedReunion.value?.id === reunion.id) {
      participantsSuccess.value = successMessage
    }
  } catch (e) {
    const message = e.response?.data?.message || 'Impossible d\'envoyer le rappel.'
    responseError.value = message
    if (selectedReunion.value?.id === reunion.id) {
      participantsError.value = message
    }
  } finally {
    reminderSendingId.value = null
  }
}

function handleDocumentClick(e) {
  if (!e.target.closest('.reunion-menu-wrapper')) {
    openMenuId.value = null
  }
}

onMounted(async () => {
  await fetchReunions()
  await fetchActiveUsers()
  document.addEventListener('click', handleDocumentClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick)
})
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

.section-heading-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.section-heading-row .section-heading {
  margin-bottom: 0;
}

.meeting-filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-btn {
  border: 1px solid var(--border);
  background: var(--white);
  color: var(--text-light);
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
}

.filter-btn:hover {
  color: var(--text);
  border-color: rgba(232, 80, 26, 0.35);
}

.filter-btn-active {
  background: rgba(232, 80, 26, 0.10);
  color: var(--primary);
  border-color: rgba(232, 80, 26, 0.35);
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

.meeting-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: fit-content;
  font-size: 12px;
  font-weight: 600;
  color: var(--primary);
  text-decoration: none;
}

.meeting-link:hover {
  text-decoration: underline;
}

/* Reunions grid */
.all-section {
  margin-bottom: 32px;
}

.reunions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 380px));
  gap: 16px;
  justify-content: start;
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

.reunion-organizer {
  border-top-color: var(--primary);
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
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-full);
}

.status-organizer {
  background: rgba(237, 98, 34, 0.12);
  color: var(--primary);
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

.reunion-details {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-line {
  font-size: 13px;
  color: var(--text-light);
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

.reunion-footer-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

/* ⋮ Menu */
.reunion-menu-wrapper {
  position: relative;
}

.menu-btn {
  font-size: 20px;
  line-height: 1;
  letter-spacing: 1px;
  padding: 2px 10px;
  background: rgba(74, 85, 104, 0.08);
  color: var(--text-light);
  border-radius: var(--radius-sm);
}

.menu-btn:hover:not(:disabled) {
  background: rgba(74, 85, 104, 0.16);
  color: var(--text);
}

.reunion-menu-dropdown {
  position: absolute;
  right: 0;
  bottom: calc(100% + 6px);
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-md);
  min-width: 210px;
  z-index: 50;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  background: none;
  border: none;
  color: var(--text);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  transition: var(--transition);
}

.menu-item:hover {
  background: var(--content-bg);
}

.menu-item-danger {
  color: var(--danger);
}

.menu-item-danger:hover {
  background: rgba(245, 101, 101, 0.07);
}

.menu-divider {
  height: 1px;
  background: var(--border);
  margin: 4px 0;
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

.secondary-btn {
  background: rgba(74, 85, 104, 0.08);
  color: var(--text);
}

.secondary-btn:hover:not(:disabled) {
  background: rgba(74, 85, 104, 0.16);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-alert {
  margin-bottom: 16px;
}

.invitees-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  margin-bottom: 10px;
}

.invitees-count {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-light);
}

.link-btn {
  border: none;
  background: transparent;
  color: var(--primary);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
}

.invitees-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 220px;
  overflow-y: auto;
  margin-top: 10px;
  padding: 4px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--content-bg);
}

.invitee-option {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: var(--white);
  cursor: pointer;
}

.invitee-option:hover {
  background: rgba(237, 98, 34, 0.05);
}

.invitee-option input {
  margin-top: 2px;
}

.invitee-option-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.invitee-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.invitee-meta,
.invitees-state {
  font-size: 12px;
  color: var(--text-light);
}

.invitees-state {
  margin-top: 10px;
  padding: 12px;
  border: 1px dashed var(--border);
  border-radius: var(--radius);
  background: var(--content-bg);
}

.compact-state {
  margin-bottom: 0;
}

.participants-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.participant-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--white);
}

.participant-main {
  min-width: 0;
  flex: 1;
}

.participant-header-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.participant-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
}

.participant-meta {
  font-size: 12px;
  color: var(--text-light);
  word-break: break-word;
}

.participant-add-group {
  margin-top: 18px;
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

  .section-heading-row {
    align-items: flex-start;
  }

  .invitees-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .participant-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .reunion-secondary-actions {
    width: 100%;
    justify-content: stretch;
  }

  .reunion-secondary-actions .action-btn {
    width: 100%;
  }
}
</style>
