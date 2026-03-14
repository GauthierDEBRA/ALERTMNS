<template>
  <div class="profil-view">
    <div class="page-topbar">
      <div class="topbar-left">
        <h1 class="page-title">Mon profil</h1>
      </div>
      <div class="topbar-right">
        <div class="desktop-notif">
          <NotificationPanel />
        </div>
      </div>
    </div>

    <div class="profil-content">
      <div class="profile-card">
        <div class="profile-header">
          <div class="profile-avatar-panel">
            <div class="profile-avatar-large" :style="!hasAvatar ? { background: avatarColor } : {}">
              <img
                v-if="hasAvatar"
                :src="authStore.user?.avatarUrl"
                :alt="authStore.fullName"
                class="profile-avatar-image"
              />
              <template v-else>
                {{ authStore.initials }}
              </template>
            </div>

            <div class="avatar-actions">
              <input
                ref="avatarInputRef"
                type="file"
                class="sr-only"
                accept=".png,.jpg,.jpeg,.webp,.gif,image/png,image/jpeg,image/webp,image/gif"
                @change="handleAvatarSelected"
              />
              <button class="btn btn-secondary btn-compact" @click="triggerAvatarPicker" :disabled="uploadingAvatar">
                {{ uploadingAvatar ? 'Upload...' : (hasAvatar ? 'Changer la photo' : 'Ajouter une photo') }}
              </button>
              <button
                v-if="hasAvatar"
                class="btn btn-ghost btn-compact"
                @click="removeAvatar"
                :disabled="removingAvatar || uploadingAvatar"
              >
                {{ removingAvatar ? 'Suppression...' : 'Supprimer' }}
              </button>
            </div>

            <span class="avatar-hint">PNG, JPG, WEBP, GIF. 5 Mo max.</span>
          </div>

          <div class="profile-header-main">
            <div class="profile-title-row">
              <div>
                <h2 class="profile-name">{{ authStore.fullName }}</h2>
                <p class="profile-email">{{ authStore.user?.email }}</p>
              </div>
              <span class="profile-role-badge" :class="getRoleClass(authStore.user?.role)">
                {{ authStore.user?.role }}
              </span>
            </div>

            <div class="profile-tags">
              <span class="profile-chip">
                {{ authStore.user?.structureNom || 'Aucune structure' }}
              </span>
              <span class="profile-chip" :class="authStore.user?.isActive ? 'chip-success' : 'chip-muted'">
                {{ authStore.user?.isActive ? 'Compte actif' : 'Compte inactif' }}
              </span>
              <span class="profile-chip" :class="presenceChipClass">
                {{ presenceLabel }}
              </span>
            </div>

            <div v-if="avatarSuccess" class="alert alert-success compact-alert">{{ avatarSuccess }}</div>
            <div v-if="avatarError" class="alert alert-error compact-alert">{{ avatarError }}</div>
          </div>
        </div>

        <div class="profile-highlights">
          <div class="highlight-item">
            <span class="highlight-label">Aujourd'hui</span>
            <strong class="highlight-value">{{ pointageStats?.dailyFormatted || '0h00' }}</strong>
          </div>
          <div class="highlight-item">
            <span class="highlight-label">Cette semaine</span>
            <strong class="highlight-value">{{ pointageStats?.weeklyFormatted || '0h00' }}</strong>
          </div>
          <div class="highlight-item">
            <span class="highlight-label">Ce mois-ci</span>
            <strong class="highlight-value">{{ pointageStats?.monthlyFormatted || '0h00' }}</strong>
          </div>
          <div class="highlight-item">
            <span class="highlight-label">Arrivée du jour</span>
            <strong class="highlight-value">{{ arrivedAtLabel }}</strong>
          </div>
        </div>
      </div>

      <div class="profil-grid">
        <div class="card profil-section">
          <h3 class="section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            Informations personnelles
          </h3>
          <p class="section-desc">Mettez à jour votre identité d'affichage. L'email reste votre identifiant de connexion.</p>

          <div v-if="profileSuccess" class="alert alert-success">{{ profileSuccess }}</div>
          <div v-if="profileError" class="alert alert-error">{{ profileError }}</div>

          <div class="form-row two-columns">
            <div class="form-group">
              <label class="form-label">Prénom</label>
              <input v-model="profileForm.prenom" type="text" class="form-input" placeholder="Votre prénom" />
            </div>
            <div class="form-group">
              <label class="form-label">Nom</label>
              <input v-model="profileForm.nom" type="text" class="form-input" placeholder="Votre nom" />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Email de connexion</label>
            <input :value="authStore.user?.email || ''" type="email" class="form-input input-readonly" readonly />
          </div>

          <div class="form-actions">
            <button class="btn btn-primary" @click="saveProfileInfo" :disabled="savingProfile || !canSaveProfile">
              {{ savingProfile ? 'Sauvegarde...' : 'Enregistrer mes informations' }}
            </button>
          </div>
        </div>

        <div class="card profil-section">
          <h3 class="section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            Message d'absence
          </h3>
          <p class="section-desc">Ce message sera affiché dans vos messages quand vous êtes absent(e).</p>

          <div v-if="absenceSuccess" class="alert alert-success">{{ absenceSuccess }}</div>
          <div v-if="absenceError" class="alert alert-error">{{ absenceError }}</div>

          <div class="form-group">
            <textarea
              v-model="absenceMessage"
              class="form-input"
              rows="4"
              placeholder="Ex: En déplacement jusqu'au 15 mars. Je réponds dès mon retour."
              style="resize: vertical;"
            ></textarea>
          </div>

          <div class="form-actions">
            <button class="btn btn-primary" @click="saveAbsenceMessage" :disabled="savingAbsence">
              {{ savingAbsence ? 'Sauvegarde...' : 'Enregistrer le message' }}
            </button>
            <button v-if="absenceMessage" class="btn btn-secondary" @click="clearAbsenceMessage">
              Effacer
            </button>
          </div>
        </div>

        <div class="card profil-section">
          <h3 class="section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            Préférences de notifications
          </h3>
          <p class="section-desc">Choisissez les notifications internes que vous souhaitez continuer à recevoir.</p>

          <div v-if="preferencesSuccess" class="alert alert-success">{{ preferencesSuccess }}</div>
          <div v-if="preferencesError" class="alert alert-error">{{ preferencesError }}</div>

          <div class="preferences-list">
            <label class="preference-row">
              <div>
                <strong>Invitations et réponses aux réunions</strong>
                <span>Invitation, acceptation et refus des participants.</span>
              </div>
              <input v-model="preferencesForm.notifyReunions" type="checkbox" class="switch-input" />
            </label>

            <label class="preference-row">
              <div>
                <strong>Nouveaux messages</strong>
                <span>Prêt pour les futures notifications de messagerie.</span>
              </div>
              <input v-model="preferencesForm.notifyMessages" type="checkbox" class="switch-input" />
            </label>

            <label class="preference-row">
              <div>
                <strong>Alertes d'absence</strong>
                <span>Prêt pour les futures notifications liées aux absences.</span>
              </div>
              <input v-model="preferencesForm.notifyAbsences" type="checkbox" class="switch-input" />
            </label>
          </div>

          <div class="form-actions">
            <button class="btn btn-primary" @click="saveNotificationPreferences" :disabled="savingPreferences || !canSavePreferences">
              {{ savingPreferences ? 'Sauvegarde...' : 'Enregistrer mes préférences' }}
            </button>
          </div>
        </div>

        <div class="card profil-section">
          <h3 class="section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 20V10"/>
              <path d="M18 20V4"/>
              <path d="M6 20v-4"/>
            </svg>
            Résumé d'activité
          </h3>
          <p class="section-desc">Un aperçu rapide de votre présence et de votre rythme récent.</p>

          <div v-if="statsError" class="alert alert-error">{{ statsError }}</div>

          <div class="activity-status" :class="presenceChipClass">
            <div>
              <span class="activity-status-label">Statut actuel</span>
              <strong>{{ presenceLabel }}</strong>
            </div>
            <span class="activity-status-time">{{ arrivedAtLabel }}</span>
          </div>

          <router-link to="/pointage" class="activity-pointage-link">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Voir le détail du pointage
          </router-link>
        </div>

        <div class="card profil-section">
          <h3 class="section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            Informations du compte
          </h3>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">Identifiant</span>
              <span class="info-value">#{{ authStore.user?.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Rôle</span>
              <span class="info-value">
                <span class="role-badge-solid" :class="getRoleClass(authStore.user?.role)">
                  {{ authStore.user?.role }}
                </span>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">Structure</span>
              <span class="info-value">{{ authStore.user?.structureNom || 'Aucune structure' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Type de structure</span>
              <span class="info-value">{{ authStore.user?.structureType || 'Non renseigné' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Statut du compte</span>
              <span class="info-value">{{ authStore.user?.isActive ? 'Actif' : 'Inactif' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Dernière activité</span>
              <span class="info-value">{{ lastActivityLabel }}</span>
            </div>
          </div>
        </div>

        <div class="card profil-section profil-section-wide">
          <div class="section-header-row">
            <div>
              <h3 class="section-title">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
                Historique de pointage
              </h3>
              <p class="section-desc">Filtrez vos pointages récents pour retrouver plus vite une journée ou un créneau.</p>
            </div>
            <button class="btn btn-secondary btn-compact" @click="loadActivityData" :disabled="loadingActivity">
              {{ loadingActivity ? 'Actualisation...' : 'Actualiser' }}
            </button>
          </div>

          <div class="history-toolbar">
            <input
              v-model="historySearch"
              type="text"
              class="form-input"
              placeholder="Rechercher par date, heure ou statut..."
            />
            <select v-model="historyFilter" class="form-input">
              <option value="all">Tous les pointages</option>
              <option value="today">Aujourd'hui</option>
              <option value="open">En cours</option>
              <option value="closed">Clôturés</option>
            </select>
          </div>

          <div class="history-summary">
            {{ filteredHistory.length }} résultat<span v-if="filteredHistory.length > 1">s</span>
            <template v-if="displayedHistory.length < filteredHistory.length">
              · {{ displayedHistory.length }} affiché<span v-if="displayedHistory.length > 1">s</span>
            </template>
          </div>

          <div v-if="historyError" class="alert alert-error">{{ historyError }}</div>

          <div v-if="displayedHistory.length" class="history-list">
            <div v-for="entry in displayedHistory" :key="entry.id" class="history-row">
              <div>
                <div class="history-date">{{ formatDateTime(entry.startedAt) }}</div>
                <div class="history-meta">
                  <span>Arrivée {{ formatTime(entry.startedAt) }}</span>
                  <span>Départ {{ entry.endedAt ? formatTime(entry.endedAt) : 'En cours' }}</span>
                  <span>{{ entry.endedAt ? 'Clôturé' : 'Ouvert' }}</span>
                </div>
              </div>
              <div class="history-duration" :class="{ ongoing: !entry.endedAt }">
                {{ formatDuration(entry.startedAt, entry.endedAt) }}
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            Aucun pointage ne correspond aux filtres actuels.
          </div>
        </div>

        <div class="card profil-section profil-section-wide">
          <h3 class="section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            Changer le mot de passe
          </h3>

          <div v-if="passwordSuccess" class="alert alert-success">{{ passwordSuccess }}</div>
          <div v-if="passwordError" class="alert alert-error">{{ passwordError }}</div>

          <form class="password-grid" @submit.prevent="changePassword">
            <div class="form-group">
              <label class="form-label">Mot de passe actuel</label>
              <div class="password-wrapper">
                <input
                  v-model="passwordForm.current"
                  :type="showPasswords.current ? 'text' : 'password'"
                  class="form-input"
                  placeholder="Mot de passe actuel"
                />
                <button type="button" class="pass-eye" @click="showPasswords.current = !showPasswords.current">
                  <svg v-if="!showPasswords.current" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Nouveau mot de passe</label>
              <div class="password-wrapper">
                <input
                  v-model="passwordForm.new"
                  :type="showPasswords.new ? 'text' : 'password'"
                  class="form-input"
                  placeholder="Nouveau mot de passe"
                />
                <button type="button" class="pass-eye" @click="showPasswords.new = !showPasswords.new">
                  <svg v-if="!showPasswords.new" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                </button>
              </div>

              <div v-if="passwordForm.new" class="password-strength">
                <div class="strength-bars">
                  <div
                    v-for="i in 4"
                    :key="i"
                    class="strength-bar"
                    :class="{ filled: i <= passwordStrength.score }"
                    :style="{ background: i <= passwordStrength.score ? passwordStrength.color : '' }"
                  ></div>
                </div>
                <span class="strength-label" :style="{ color: passwordStrength.color }">
                  {{ passwordStrength.label }}
                </span>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Confirmer le nouveau mot de passe</label>
              <div class="password-wrapper">
                <input
                  v-model="passwordForm.confirm"
                  :type="showPasswords.confirm ? 'text' : 'password'"
                  class="form-input"
                  :class="{ 'input-error': passwordForm.confirm && passwordForm.new !== passwordForm.confirm }"
                  placeholder="Confirmer le mot de passe"
                />
                <button type="button" class="pass-eye" @click="showPasswords.confirm = !showPasswords.confirm">
                  <svg v-if="!showPasswords.confirm" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                </button>
              </div>
              <span v-if="passwordForm.confirm && passwordForm.new !== passwordForm.confirm" class="input-error-msg">
                Les mots de passe ne correspondent pas
              </span>
            </div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary" :disabled="!canChangePassword || changingPassword">
                {{ changingPassword ? 'Modification...' : 'Changer le mot de passe' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import NotificationPanel from '../components/NotificationPanel.vue'
import { useAuthStore } from '../stores/auth.js'
import { getAvatarColor } from '../utils/avatar.js'
import api from '../api/axios.js'

const authStore = useAuthStore()

const avatarInputRef = ref(null)
const uploadingAvatar = ref(false)
const removingAvatar = ref(false)
const avatarSuccess = ref('')
const avatarError = ref('')

const profileForm = ref({ prenom: '', nom: '' })
const savingProfile = ref(false)
const profileSuccess = ref('')
const profileError = ref('')

const preferencesForm = ref({
  notifyReunions: true,
  notifyMessages: true,
  notifyAbsences: true
})
const savingPreferences = ref(false)
const preferencesSuccess = ref('')
const preferencesError = ref('')

const absenceMessage = ref('')
const savingAbsence = ref(false)
const absenceSuccess = ref('')
const absenceError = ref('')

const passwordForm = ref({ current: '', new: '', confirm: '' })
const showPasswords = ref({ current: false, new: false, confirm: false })
const changingPassword = ref(false)
const passwordSuccess = ref('')
const passwordError = ref('')

const pointageStats = ref(null)
const pointageHistory = ref([])
const loadingActivity = ref(false)
const statsError = ref('')
const historyError = ref('')
const historySearch = ref('')
const historyFilter = ref('all')
const ALLOWED_AVATAR_TYPES = new Set(['image/png', 'image/jpeg', 'image/webp', 'image/gif'])
const ALLOWED_AVATAR_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'webp', 'gif'])

const avatarColor = computed(() => getAvatarColor(authStore.user || {}))

const hasAvatar = computed(() => Boolean(authStore.user?.avatarUrl))

const passwordStrength = computed(() => {
  const pass = passwordForm.value.new
  if (!pass) return { score: 0, label: '', color: '' }
  let score = 0
  if (pass.length >= 8) score++
  if (/[A-Z]/.test(pass)) score++
  if (/[0-9]/.test(pass)) score++
  if (/[^A-Za-z0-9]/.test(pass)) score++

  const labels = ['Très faible', 'Faible', 'Moyen', 'Fort', 'Très fort']
  const colors = ['#f56565', '#ed8936', '#ecc94b', '#48bb78', '#38a169']

  return { score, label: labels[score], color: colors[score] }
})

const canChangePassword = computed(() =>
  passwordForm.value.current &&
  passwordForm.value.new &&
  passwordForm.value.confirm &&
  passwordForm.value.new === passwordForm.value.confirm &&
  passwordForm.value.new.length >= 6
)

const canSaveProfile = computed(() => {
  const currentPrenom = authStore.user?.prenom || ''
  const currentNom = authStore.user?.nom || ''
  return Boolean(profileForm.value.prenom.trim() && profileForm.value.nom.trim()) && (
    profileForm.value.prenom.trim() !== currentPrenom ||
    profileForm.value.nom.trim() !== currentNom
  )
})

const canSavePreferences = computed(() => {
  return preferencesForm.value.notifyReunions !== Boolean(authStore.user?.notifyReunions) ||
    preferencesForm.value.notifyMessages !== Boolean(authStore.user?.notifyMessages) ||
    preferencesForm.value.notifyAbsences !== Boolean(authStore.user?.notifyAbsences)
})

const recentHistory = computed(() => {
  return [...pointageHistory.value]
    .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt))
})

const filteredHistory = computed(() => {
  const search = historySearch.value.trim().toLowerCase()

  return recentHistory.value.filter((entry) => {
    const isOpen = !entry.endedAt
    const startedDate = new Date(entry.startedAt)
    const isToday = startedDate.toDateString() === new Date().toDateString()

    if (historyFilter.value === 'open' && !isOpen) return false
    if (historyFilter.value === 'closed' && isOpen) return false
    if (historyFilter.value === 'today' && !isToday) return false

    if (!search) return true

    const haystack = [
      formatDateTime(entry.startedAt),
      formatTime(entry.startedAt),
      entry.endedAt ? formatTime(entry.endedAt) : 'en cours',
      entry.endedAt ? 'clôturé' : 'ouvert',
      formatDuration(entry.startedAt, entry.endedAt)
    ].join(' ').toLowerCase()

    return haystack.includes(search)
  })
})

const displayedHistory = computed(() => filteredHistory.value.slice(0, 12))
const presenceLabel = computed(() => pointageStats.value?.isCurrentlyPresent ? 'Présent(e)' : 'Absent(e)')
const presenceChipClass = computed(() => pointageStats.value?.isCurrentlyPresent ? 'chip-success' : 'chip-muted')
const arrivedAtLabel = computed(() => {
  if (!pointageStats.value?.arrivedAt) return 'Pas encore pointé aujourd\'hui'
  return `Depuis ${formatTime(pointageStats.value.arrivedAt)}`
})
const lastActivityLabel = computed(() => {
  if (!recentHistory.value.length) return 'Aucune activité récente'
  return formatDateTime(recentHistory.value[0].startedAt)
})

function getRoleClass(role) {
  const map = {
    Admin: 'role-admin',
    RH: 'role-rh',
    Collaborateur: 'role-employe',
    Responsable: 'role-manager'
  }
  return map[role] || 'role-employe'
}

function syncProfileForms() {
  profileForm.value = {
    prenom: authStore.user?.prenom || '',
    nom: authStore.user?.nom || ''
  }
  preferencesForm.value = {
    notifyReunions: Boolean(authStore.user?.notifyReunions),
    notifyMessages: Boolean(authStore.user?.notifyMessages),
    notifyAbsences: Boolean(authStore.user?.notifyAbsences)
  }
  absenceMessage.value = authStore.user?.msgAbsence || ''
}

function resetAvatarFeedback() {
  avatarSuccess.value = ''
  avatarError.value = ''
}

function normalizeHistoryEntry(entry) {
  return {
    id: entry.idPointage ?? entry.id ?? `${entry.dateDebut}-${entry.dateFin}`,
    startedAt: entry.dateDebut,
    endedAt: entry.dateFin
  }
}

function formatDateTime(value) {
  if (!value) return 'Non renseigné'
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value))
}

function formatTime(value) {
  if (!value) return '--:--'
  return new Intl.DateTimeFormat('fr-FR', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

function formatDuration(start, end) {
  if (!start) return '0 min'
  const startDate = new Date(start)
  const endDate = end ? new Date(end) : new Date()
  const diffMinutes = Math.max(0, Math.round((endDate - startDate) / 60000))
  const hours = Math.floor(diffMinutes / 60)
  const minutes = diffMinutes % 60
  if (!hours) return `${minutes} min${end ? '' : ' en cours'}`
  return `${hours}h${String(minutes).padStart(2, '0')}${end ? '' : ' en cours'}`
}

function triggerAvatarPicker() {
  resetAvatarFeedback()
  avatarInputRef.value?.click()
}

async function handleAvatarSelected(event) {
  const file = event.target?.files?.[0]
  if (!file || !authStore.user?.id) {
    return
  }

  resetAvatarFeedback()

  if (file.size > 5 * 1024 * 1024) {
    avatarError.value = 'L\'image est trop volumineuse. Maximum 5 Mo.'
    event.target.value = ''
    return
  }

  const extension = String(file.name || '').split('.').pop()?.toLowerCase() || ''
  const type = String(file.type || '').toLowerCase()
  const looksAllowed = ALLOWED_AVATAR_TYPES.has(type) || ALLOWED_AVATAR_EXTENSIONS.has(extension)

  if (!looksAllowed) {
    avatarError.value = 'Formats acceptés : PNG, JPG, WEBP ou GIF.'
    event.target.value = ''
    return
  }

  uploadingAvatar.value = true

  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await api.put(`/users/${authStore.user.id}/avatar`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    authStore.updateUser(response.data)
    syncProfileForms()
    avatarSuccess.value = 'Photo de profil mise à jour !'
    setTimeout(() => { avatarSuccess.value = '' }, 3000)
  } catch (error) {
    if (error.response?.status === 413) {
      avatarError.value = 'L\'image est trop volumineuse. Maximum 5 Mo.'
    } else if (error.response?.status === 429) {
      avatarError.value = error.response?.data?.message || 'Trop de tentatives d\'upload. Réessaie dans quelques minutes.'
    } else {
      avatarError.value = error.response?.data?.message || 'Erreur lors de l\'upload de la photo'
    }
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

async function removeAvatar() {
  if (!authStore.user?.id) return

  resetAvatarFeedback()
  removingAvatar.value = true

  try {
    const response = await api.delete(`/users/${authStore.user.id}/avatar`)
    authStore.updateUser(response.data)
    syncProfileForms()
    avatarSuccess.value = 'Photo de profil supprimée.'
    setTimeout(() => { avatarSuccess.value = '' }, 3000)
  } catch (error) {
    avatarError.value = error.response?.data?.message || 'Erreur lors de la suppression de la photo'
  } finally {
    removingAvatar.value = false
  }
}

async function loadActivityData() {
  loadingActivity.value = true
  statsError.value = ''
  historyError.value = ''

  const [statsResponse, historyResponse] = await Promise.allSettled([
    api.get('/pointage/stats'),
    api.get('/pointage/historique')
  ])

  if (statsResponse.status === 'fulfilled') {
    pointageStats.value = statsResponse.value.data
    authStore.updateUser({ isPresent: Boolean(statsResponse.value.data?.isCurrentlyPresent) })
  } else {
    statsError.value = statsResponse.reason?.response?.data?.message || 'Impossible de charger les statistiques de pointage'
  }

  if (historyResponse.status === 'fulfilled') {
    pointageHistory.value = (historyResponse.value.data || []).map(normalizeHistoryEntry)
  } else {
    historyError.value = historyResponse.reason?.response?.data?.message || 'Impossible de charger l\'historique de pointage'
  }

  loadingActivity.value = false
}

async function loadProfileData() {
  const userRefresh = await authStore.refreshCurrentUser()
  if (!userRefresh.success && !authStore.user) {
    profileError.value = userRefresh.message
  }
  syncProfileForms()
  await loadActivityData()
}

async function saveProfileInfo() {
  if (!canSaveProfile.value || !authStore.user?.id) return

  savingProfile.value = true
  profileSuccess.value = ''
  profileError.value = ''

  try {
    const response = await api.put(`/users/${authStore.user.id}/profil`, {
      prenom: profileForm.value.prenom,
      nom: profileForm.value.nom
    })
    authStore.updateUser(response.data)
    syncProfileForms()
    profileSuccess.value = 'Informations personnelles mises à jour !'
    setTimeout(() => { profileSuccess.value = '' }, 3000)
  } catch (error) {
    profileError.value = error.response?.data?.message || 'Erreur lors de la mise à jour du profil'
  } finally {
    savingProfile.value = false
  }
}

async function saveNotificationPreferences() {
  if (!canSavePreferences.value || !authStore.user?.id) return

  savingPreferences.value = true
  preferencesSuccess.value = ''
  preferencesError.value = ''

  try {
    const response = await api.put(`/users/${authStore.user.id}/preferences`, preferencesForm.value)
    authStore.updateUser(response.data)
    syncProfileForms()
    preferencesSuccess.value = 'Préférences de notifications enregistrées !'
    setTimeout(() => { preferencesSuccess.value = '' }, 3000)
  } catch (error) {
    preferencesError.value = error.response?.data?.message || 'Erreur lors de la sauvegarde des préférences'
  } finally {
    savingPreferences.value = false
  }
}

async function saveAbsenceMessage() {
  savingAbsence.value = true
  absenceSuccess.value = ''
  absenceError.value = ''

  try {
    const response = await api.put(`/users/${authStore.user?.id}/absence`, { message: absenceMessage.value })
    authStore.updateUser(response.data)
    syncProfileForms()
    absenceSuccess.value = 'Message d\'absence mis à jour !'
    setTimeout(() => { absenceSuccess.value = '' }, 3000)
  } catch (error) {
    absenceError.value = error.response?.data?.message || 'Erreur lors de la mise à jour'
  } finally {
    savingAbsence.value = false
  }
}

async function clearAbsenceMessage() {
  absenceMessage.value = ''
  await saveAbsenceMessage()
}

async function changePassword() {
  if (!canChangePassword.value) return
  changingPassword.value = true
  passwordSuccess.value = ''
  passwordError.value = ''

  try {
    await api.put(`/users/${authStore.user?.id}/password`, {
      currentPassword: passwordForm.value.current,
      password: passwordForm.value.new
    })
    passwordSuccess.value = 'Mot de passe modifié avec succès !'
    passwordForm.value = { current: '', new: '', confirm: '' }
    setTimeout(() => { passwordSuccess.value = '' }, 4000)
  } catch (error) {
    passwordError.value = error.response?.data?.message || 'Erreur lors du changement de mot de passe'
  } finally {
    changingPassword.value = false
  }
}

onMounted(async () => {
  syncProfileForms()
  await loadProfileData()
})
</script>

<style scoped>
.profil-view {
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

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.desktop-notif :deep(.notif-btn) {
  color: var(--text-light);
}

.profil-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  max-width: 1120px;
  width: 100%;
  margin: 0 auto;
}

.profile-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  overflow: hidden;
  margin-bottom: 24px;
}

.profile-header {
  background: linear-gradient(135deg, var(--dark) 0%, #283244 60%, #1d2230 100%);
  padding: 32px;
  display: flex;
  align-items: flex-start;
  gap: 28px;
}

.profile-avatar-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
}

.profile-avatar-large {
  width: 108px;
  height: 108px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 800;
  color: white;
  flex-shrink: 0;
  border: 4px solid rgba(255, 255, 255, 0.16);
  overflow: hidden;
}

.profile-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.avatar-hint {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.68);
  text-align: center;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.profile-header-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profile-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.profile-name {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin-bottom: 6px;
}

.profile-email {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.72);
}

.profile-role-badge,
.role-badge-solid {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 700;
  width: fit-content;
}

.role-admin {
  background: rgba(232, 80, 26, 0.14);
  color: #fc8c6a;
}

.role-rh {
  background: rgba(66, 153, 225, 0.14);
  color: #90cdf4;
}

.role-employe {
  background: rgba(72, 187, 120, 0.14);
  color: #82d89f;
}

.role-manager {
  background: rgba(159, 122, 234, 0.14);
  color: #d6bcfa;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.profile-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 12px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.08);
  color: white;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.profile-chip.chip-success,
.activity-status.chip-success {
  background: rgba(72, 187, 120, 0.18);
  color: #68d391;
  border-color: rgba(72, 187, 120, 0.3);
}

.profile-chip.chip-muted {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
}

.activity-status.chip-muted {
  background: #f8fafc;
  color: var(--text);
  border-color: var(--border);
}

.profile-highlights {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  padding: 18px 24px 24px;
  background: linear-gradient(180deg, rgba(245, 247, 250, 0.95), rgba(255, 255, 255, 0.95));
}

.highlight-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px;
  border-radius: 16px;
  background: white;
  border: 1px solid var(--border);
}

.highlight-label,
.mini-stat-label,
.activity-status-label,
.info-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-light);
}

.highlight-value {
  font-size: 22px;
  line-height: 1.1;
  color: var(--text);
}

.compact-alert {
  margin-top: 4px;
}

.profil-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  align-items: start;
}

.profil-section {
  padding: 24px;
}

.profil-section-wide {
  grid-column: 1 / -1;
}

.section-header-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 8px;
}

.section-desc {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 16px;
}

.form-row.two-columns,
.history-toolbar {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 4px;
}

.input-readonly {
  background: #f8fafc;
  color: var(--text-light);
  cursor: not-allowed;
}

.preferences-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preference-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff, #fbfcfd);
}

.preference-row strong {
  display: block;
  font-size: 14px;
  color: var(--text);
  margin-bottom: 4px;
}

.preference-row span {
  font-size: 12px;
  color: var(--text-light);
}

.switch-input {
  appearance: none;
  -webkit-appearance: none;
  width: 42px;
  height: 24px;
  border-radius: 12px;
  background: #cbd5e0;
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s ease;
  outline: none;
}

.switch-input::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  transition: transform 0.2s ease;
}

.switch-input:checked {
  background: var(--primary);
}

.switch-input:checked::after {
  transform: translateX(18px);
}

.switch-input:focus-visible {
  box-shadow: 0 0 0 3px rgba(232, 80, 26, 0.25);
}

.activity-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: #f8fafc;
  margin-bottom: 16px;
}

.activity-status strong {
  display: block;
  font-size: 18px;
  margin-top: 4px;
}

.activity-status-time {
  font-size: 13px;
  font-weight: 600;
}

.activity-pointage-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 14px;
  font-size: 13px;
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
  transition: var(--transition);
}

.activity-pointage-link:hover {
  text-decoration: underline;
  opacity: 0.85;
}

.mini-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.mini-stat-card {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff, #fbfcfd);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mini-stat-card strong {
  font-size: 22px;
  color: var(--text);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid var(--border);
}

.info-value {
  font-size: 14px;
  color: var(--text);
  font-weight: 600;
}

.history-toolbar {
  margin-bottom: 12px;
}

.history-summary {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 12px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: linear-gradient(180deg, #ffffff, #fbfcfd);
}

.history-date {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 6px;
}

.history-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: var(--text-light);
}

.history-duration {
  flex-shrink: 0;
  padding: 8px 12px;
  border-radius: var(--radius-full);
  background: rgba(232, 80, 26, 0.08);
  color: var(--primary);
  font-weight: 700;
}

.history-duration.ongoing {
  background: rgba(72, 187, 120, 0.12);
  color: #2f855a;
}

.empty-state {
  padding: 20px;
  border: 1px dashed var(--border);
  border-radius: 16px;
  color: var(--text-light);
  text-align: center;
  background: #fbfcfd;
}

.password-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.password-wrapper {
  position: relative;
}

.pass-eye {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
}

.pass-eye:hover {
  color: var(--text);
}

.form-input.input-error {
  border-color: var(--danger);
}

.input-error-msg {
  font-size: 12px;
  color: var(--danger);
  margin-top: 4px;
  display: block;
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.strength-bars {
  display: flex;
  gap: 4px;
}

.strength-bar {
  height: 4px;
  width: 36px;
  border-radius: 2px;
  background: var(--border);
  transition: background 0.2s;
}

.strength-label {
  font-size: 12px;
  font-weight: 600;
}

.password-grid .form-actions {
  grid-column: 1 / -1;
}

.btn-compact {
  padding: 10px 14px;
  font-size: 13px;
}

.btn-ghost {
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.06);
  color: white;
}

.btn-ghost:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.12);
}

@media (max-width: 980px) {
  .profile-highlights,
  .profil-grid,
  .password-grid,
  .mini-stats-grid,
  .info-grid,
  .form-row.two-columns,
  .history-toolbar {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-topbar {
    padding: 0 16px;
  }

  .profil-content {
    padding: 16px;
  }

  .profile-header {
    padding: 24px 16px;
    flex-direction: column;
    align-items: stretch;
  }

  .profile-avatar-panel {
    align-items: flex-start;
  }

  .profile-title-row,
  .section-header-row,
  .history-row,
  .activity-status,
  .preference-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-name {
    font-size: 22px;
  }

  .profil-section {
    padding: 18px;
  }

  .desktop-notif {
    display: none;
  }
}
</style>
