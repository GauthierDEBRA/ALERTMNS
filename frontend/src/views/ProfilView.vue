<template>
  <div class="profil-view">
    <!-- Header -->
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
      <!-- Profile card -->
      <div class="profile-card">
        <div class="profile-header">
          <div class="profile-avatar-large" :style="{ background: avatarColor }">
            {{ authStore.initials }}
          </div>
          <div class="profile-header-info">
            <h2 class="profile-name">{{ authStore.fullName }}</h2>
            <span class="profile-role-badge" :class="getRoleClass(authStore.user?.role)">
              {{ authStore.user?.role }}
            </span>
            <p class="profile-email">{{ authStore.user?.email }}</p>
          </div>
        </div>
      </div>

      <div class="profil-grid">
        <!-- Absence message -->
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
              rows="3"
              placeholder="Ex: En déplacement jusqu'au 15 mars. Je réponds dès mon retour."
              style="resize: vertical;"
            ></textarea>
          </div>
          <div class="form-group">
            <button
              class="btn btn-primary"
              @click="saveAbsenceMessage"
              :disabled="savingAbsence"
            >
              {{ savingAbsence ? 'Sauvegarde...' : 'Enregistrer le message' }}
            </button>
            <button
              v-if="absenceMessage"
              class="btn btn-secondary"
              @click="clearAbsenceMessage"
              style="margin-left: 8px;"
            >
              Effacer
            </button>
          </div>
        </div>

        <!-- Change password -->
        <div class="card profil-section">
          <h3 class="section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            Changer le mot de passe
          </h3>

          <div v-if="passwordSuccess" class="alert alert-success">{{ passwordSuccess }}</div>
          <div v-if="passwordError" class="alert alert-error">{{ passwordError }}</div>

          <form @submit.prevent="changePassword">
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

              <!-- Password strength -->
              <div v-if="passwordForm.new" class="password-strength">
                <div class="strength-bars">
                  <div
                    v-for="i in 4"
                    :key="i"
                    class="strength-bar"
                    :class="{ 'filled': i <= passwordStrength.score }"
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

            <button
              type="submit"
              class="btn btn-primary"
              :disabled="!canChangePassword || changingPassword"
            >
              {{ changingPassword ? 'Modification...' : 'Changer le mot de passe' }}
            </button>
          </form>
        </div>

        <!-- Account info -->
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
              <span class="info-label">Prénom</span>
              <span class="info-value">{{ authStore.user?.prenom }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Nom</span>
              <span class="info-value">{{ authStore.user?.nom }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Email</span>
              <span class="info-value">{{ authStore.user?.email }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Rôle</span>
              <span class="info-value">
                <span class="role-badge" :class="getRoleClass(authStore.user?.role)">
                  {{ authStore.user?.role }}
                </span>
              </span>
            </div>
            <div v-if="authStore.user?.idStructure" class="info-item">
              <span class="info-label">Structure</span>
              <span class="info-value">#{{ authStore.user?.idStructure }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import NotificationPanel from '../components/NotificationPanel.vue'
import { useAuthStore } from '../stores/auth.js'
import api from '../api/axios.js'

const authStore = useAuthStore()

const absenceMessage = ref('')
const savingAbsence = ref(false)
const absenceSuccess = ref('')
const absenceError = ref('')

const passwordForm = ref({ current: '', new: '', confirm: '' })
const showPasswords = ref({ current: false, new: false, confirm: false })
const changingPassword = ref(false)
const passwordSuccess = ref('')
const passwordError = ref('')

const AVATAR_COLORS = [
  '#E8501A', '#4299e1', '#48bb78', '#ed8936', '#9f7aea',
  '#ed64a6', '#38b2ac', '#667eea', '#fc8181', '#68d391'
]

const avatarColor = computed(() => {
  const str = `${authStore.user?.prenom || ''}${authStore.user?.nom || ''}`
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length]
})

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

function getRoleClass(role) {
  const map = { 'Admin': 'role-admin', 'RH': 'role-rh', 'Employe': 'role-employe', 'Manager': 'role-manager' }
  return map[role] || 'role-employe'
}

async function saveAbsenceMessage() {
  savingAbsence.value = true
  absenceSuccess.value = ''
  absenceError.value = ''

  try {
    await api.put('/users/absence', { msgAbsence: absenceMessage.value })
    absenceSuccess.value = 'Message d\'absence mis à jour !'
    setTimeout(() => { absenceSuccess.value = '' }, 3000)
  } catch (e) {
    absenceError.value = e.response?.data?.message || 'Erreur lors de la mise à jour'
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
    await api.put(`/users/${authStore.user?.id}`, {
      ...authStore.user,
      motDePasse: passwordForm.value.new,
      ancienMotDePasse: passwordForm.value.current
    })
    passwordSuccess.value = 'Mot de passe modifié avec succès !'
    passwordForm.value = { current: '', new: '', confirm: '' }
    setTimeout(() => { passwordSuccess.value = '' }, 4000)
  } catch (e) {
    passwordError.value = e.response?.data?.message || 'Erreur lors du changement de mot de passe'
  } finally {
    changingPassword.value = false
  }
}
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
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
}

/* Profile card */
.profile-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  overflow: hidden;
  margin-bottom: 24px;
}

.profile-header {
  background: linear-gradient(135deg, var(--dark) 0%, #2d3340 100%);
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.profile-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
  color: white;
  flex-shrink: 0;
  border: 3px solid rgba(255,255,255,0.2);
}

.profile-header-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-name {
  font-size: 24px;
  font-weight: 700;
  color: white;
}

.profile-email {
  font-size: 14px;
  color: rgba(255,255,255,0.6);
}

.profile-role-badge {
  display: inline-flex;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 700;
  width: fit-content;
}

.role-admin {
  background: rgba(232, 80, 26, 0.2);
  color: #fc8c6a;
}

.role-rh {
  background: rgba(66, 153, 225, 0.2);
  color: #90cdf4;
}

.role-employe {
  background: rgba(72, 187, 120, 0.2);
  color: #9ae6b4;
}

.role-manager {
  background: rgba(159, 122, 234, 0.2);
  color: #d6bcfa;
}

/* Grid */
.profil-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 20px;
}

.profil-section {
  padding: 24px;
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

/* Password */
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

/* Password strength */
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

/* Account info */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-light);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  color: var(--text);
  font-weight: 500;
}

@media (max-width: 768px) {
  .page-topbar {
    padding: 0 16px;
  }

  .profil-content {
    padding: 16px;
  }

  .profil-grid {
    grid-template-columns: 1fr;
  }

  .profile-header {
    padding: 24px 16px;
    flex-direction: column;
    text-align: center;
    align-items: center;
  }

  .profile-name {
    font-size: 20px;
  }

  .profil-section {
    padding: 16px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .desktop-notif {
    display: none;
  }
}
</style>
