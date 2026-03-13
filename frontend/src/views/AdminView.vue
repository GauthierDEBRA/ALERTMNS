<template>
  <div class="admin-view">
    <!-- Header -->
    <div class="page-topbar">
      <div class="topbar-left">
        <h1 class="page-title">Administration</h1>
      </div>
      <div class="topbar-right">
        <div class="desktop-notif">
          <NotificationPanel />
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="admin-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-btn"
        :class="{ 'tab-active': activeTab === tab.id }"
        @click="activeTab = tab.id"
      >
        {{ tab.label }}
        <span v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <div class="admin-content">
      <!-- Users Tab -->
      <div v-if="activeTab === 'users'">
        <div class="content-header">
          <div class="search-bar">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              v-model="userSearch"
              type="text"
              placeholder="Rechercher un utilisateur..."
              class="search-input"
            />
          </div>
          <div class="filter-group">
            <select v-model="roleFilter" class="form-input" style="width: auto;">
              <option value="">Tous les rôles</option>
              <option value="Admin">Admin</option>
              <option value="RH">RH</option>
              <option value="Manager">Manager</option>
              <option value="Employe">Employé</option>
            </select>
            <select v-model="statusFilter" class="form-input" style="width: auto;">
              <option value="">Tous les statuts</option>
              <option value="active">Actifs</option>
              <option value="inactive">Inactifs</option>
            </select>
          </div>
        </div>

        <div v-if="usersLoading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <div v-else class="table-container">
          <table>
            <thead>
              <tr>
                <th>Utilisateur</th>
                <th>Email</th>
                <th>Rôle</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in filteredUsers" :key="user.id">
                <td>
                  <div class="user-cell">
                    <div class="user-avatar-sm" :style="{ background: getAvatarColor(user) }">
                      {{ `${user.prenom?.[0] || ''}${user.nom?.[0] || ''}`.toUpperCase() }}
                    </div>
                    <span class="user-name">{{ user.prenom }} {{ user.nom }}</span>
                  </div>
                </td>
                <td class="email-cell">{{ user.email }}</td>
                <td>
                  <span class="role-badge" :class="getRoleClass(user.role)">{{ user.role }}</span>
                </td>
                <td>
                  <div class="toggle-wrapper">
                    <button
                      class="status-toggle"
                      :class="{ 'toggle-active': user.actif }"
                      @click="toggleUserStatus(user)"
                      :title="user.actif ? 'Désactiver' : 'Activer'"
                    >
                      <div class="toggle-thumb"></div>
                    </button>
                    <span class="toggle-label">{{ user.actif ? 'Actif' : 'Inactif' }}</span>
                  </div>
                </td>
                <td>
                  <button class="icon-btn" @click="openEditUser(user)" title="Modifier">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  </button>
                </td>
              </tr>
              <tr v-if="filteredUsers.length === 0">
                <td colspan="5" style="text-align: center; color: var(--text-light); padding: 30px;">
                  Aucun utilisateur trouvé
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Channels Tab -->
      <div v-if="activeTab === 'canaux'">
        <div class="content-header">
          <div class="search-bar">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              v-model="channelSearch"
              type="text"
              placeholder="Rechercher un canal..."
              class="search-input"
            />
          </div>
          <button class="btn btn-primary btn-sm" @click="showCreateChannel = true">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Nouveau canal
          </button>
        </div>

        <div v-if="canaux.length === 0" class="empty-state">
          <div class="empty-state-icon">💬</div>
          <h3>Aucun canal</h3>
          <p>Créez le premier canal de discussion</p>
        </div>

        <div v-else class="canaux-grid">
          <div v-for="canal in filteredCanaux" :key="canal.id" class="canal-card">
            <div class="canal-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="4" y1="9" x2="20" y2="9"/>
                <line x1="4" y1="15" x2="20" y2="15"/>
                <line x1="10" y1="3" x2="8" y2="21"/>
                <line x1="16" y1="3" x2="14" y2="21"/>
              </svg>
            </div>
            <div class="canal-info">
              <h3 class="canal-name">{{ canal.nom }}</h3>
              <div class="canal-meta">
                <span v-if="canal.estPrive" class="canal-private">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                  Privé
                </span>
                <span v-else class="canal-public">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="2" y1="12" x2="22" y2="12"/>
                    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                  </svg>
                  Public
                </span>
              </div>
            </div>
            <div class="canal-members">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
              <span>{{ canal.membresCount || 0 }} membres</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit User Modal -->
    <Teleport to="body">
      <div v-if="showEditUser && editingUser" class="modal-overlay" @click.self="closeEditUser">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Modifier l'utilisateur</h3>
            <button class="modal-close" @click="closeEditUser">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="editError" class="alert alert-error">{{ editError }}</div>
            <div v-if="editSuccess" class="alert alert-success">{{ editSuccess }}</div>

            <div class="form-group">
              <label class="form-label">Prénom</label>
              <input v-model="editingUser.prenom" class="form-input" type="text" />
            </div>
            <div class="form-group">
              <label class="form-label">Nom</label>
              <input v-model="editingUser.nom" class="form-input" type="text" />
            </div>
            <div class="form-group">
              <label class="form-label">Email</label>
              <input v-model="editingUser.email" class="form-input" type="email" />
            </div>
            <div class="form-group">
              <label class="form-label">Rôle</label>
              <select v-model="editingUser.role" class="form-input">
                <option value="Admin">Admin</option>
                <option value="RH">RH</option>
                <option value="Manager">Manager</option>
                <option value="Employe">Employé</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeEditUser">Annuler</button>
            <button class="btn btn-primary" @click="saveUser" :disabled="saving">
              {{ saving ? 'Sauvegarde...' : 'Sauvegarder' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Create Channel Modal -->
    <Teleport to="body">
      <div v-if="showCreateChannel" class="modal-overlay" @click.self="showCreateChannel = false">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Créer un canal</h3>
            <button class="modal-close" @click="showCreateChannel = false">✕</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">Nom du canal</label>
              <input v-model="newCanalNom" class="form-input" type="text" placeholder="nom-du-canal" />
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input v-model="newCanalPrive" type="checkbox" />
                <span>Canal privé</span>
              </label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showCreateChannel = false">Annuler</button>
            <button class="btn btn-primary" @click="createCanal" :disabled="!newCanalNom || creatingCanal">
              {{ creatingCanal ? 'Création...' : 'Créer' }}
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
import api from '../api/axios.js'
import { useChannelsStore } from '../stores/channels.js'

const channelsStore = useChannelsStore()

const activeTab = ref('users')
const users = ref([])
const canaux = ref([])
const usersLoading = ref(false)
const userSearch = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const channelSearch = ref('')

const showEditUser = ref(false)
const editingUser = ref(null)
const editError = ref('')
const editSuccess = ref('')
const saving = ref(false)

const showCreateChannel = ref(false)
const newCanalNom = ref('')
const newCanalPrive = ref(false)
const creatingCanal = ref(false)

const tabs = computed(() => [
  { id: 'users', label: 'Utilisateurs', count: users.value.length },
  { id: 'canaux', label: 'Canaux', count: canaux.value.length }
])

const filteredUsers = computed(() => {
  let result = users.value
  if (userSearch.value) {
    const q = userSearch.value.toLowerCase()
    result = result.filter(u =>
      `${u.prenom} ${u.nom}`.toLowerCase().includes(q) ||
      u.email?.toLowerCase().includes(q)
    )
  }
  if (roleFilter.value) {
    result = result.filter(u => u.role === roleFilter.value)
  }
  if (statusFilter.value === 'active') {
    result = result.filter(u => u.actif)
  } else if (statusFilter.value === 'inactive') {
    result = result.filter(u => !u.actif)
  }
  return result
})

const filteredCanaux = computed(() => {
  if (!channelSearch.value) return canaux.value
  const q = channelSearch.value.toLowerCase()
  return canaux.value.filter(c => c.nom?.toLowerCase().includes(q))
})

const AVATAR_COLORS = [
  '#E8501A', '#4299e1', '#48bb78', '#ed8936', '#9f7aea',
  '#ed64a6', '#38b2ac', '#667eea', '#fc8181', '#68d391'
]

function getAvatarColor(user) {
  const str = `${user.prenom || ''}${user.nom || ''}`
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length]
}

function getRoleClass(role) {
  const map = { 'Admin': 'role-admin', 'RH': 'role-rh', 'Employe': 'role-employe', 'Manager': 'role-manager' }
  return map[role] || 'role-employe'
}

async function fetchUsers() {
  usersLoading.value = true
  try {
    const res = await api.get('/users')
    users.value = res.data || []
  } catch (e) {
    console.error('Error fetching users:', e)
  } finally {
    usersLoading.value = false
  }
}

async function fetchCanaux() {
  try {
    const data = await channelsStore.fetchAllChannels()
    canaux.value = data
  } catch (e) {
    console.error('Error fetching canaux:', e)
  }
}

async function toggleUserStatus(user) {
  try {
    if (user.actif) {
      await api.put(`/users/${user.id}/desactiver`)
    } else {
      await api.put(`/users/${user.id}/activer`)
    }
    user.actif = !user.actif
  } catch (e) {
    console.error('Error toggling user status:', e)
  }
}

function openEditUser(user) {
  editingUser.value = { ...user }
  editError.value = ''
  editSuccess.value = ''
  showEditUser.value = true
}

function closeEditUser() {
  showEditUser.value = false
  editingUser.value = null
}

async function saveUser() {
  if (!editingUser.value) return
  saving.value = true
  editError.value = ''
  editSuccess.value = ''

  try {
    await api.put(`/users/${editingUser.value.id}`, editingUser.value)
    const idx = users.value.findIndex(u => u.id === editingUser.value.id)
    if (idx !== -1) {
      users.value[idx] = { ...editingUser.value }
    }
    editSuccess.value = 'Utilisateur mis à jour avec succès'
    setTimeout(() => {
      closeEditUser()
    }, 1200)
  } catch (e) {
    editError.value = e.response?.data?.message || 'Erreur lors de la mise à jour'
  } finally {
    saving.value = false
  }
}

async function createCanal() {
  if (!newCanalNom.value) return
  creatingCanal.value = true
  try {
    const result = await channelsStore.createChannel(newCanalNom.value, newCanalPrive.value)
    if (result.success) {
      canaux.value.push(result.channel)
      showCreateChannel.value = false
      newCanalNom.value = ''
      newCanalPrive.value = false
    }
  } catch (e) {
    console.error('Error creating canal:', e)
  } finally {
    creatingCanal.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchUsers(), fetchCanaux()])
})
</script>

<style scoped>
.admin-view {
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

.admin-tabs {
  display: flex;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  padding: 0 24px;
  flex-shrink: 0;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 0;
  margin-right: 28px;
  background: none;
  border: none;
  color: var(--text-light);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: var(--transition);
}

.tab-btn:hover {
  color: var(--text);
}

.tab-active {
  color: var(--primary) !important;
  border-bottom-color: var(--primary);
  font-weight: 600;
}

.tab-count {
  background: var(--content-bg);
  color: var(--text-light);
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 10px;
  font-weight: 600;
}

.tab-active .tab-count {
  background: var(--primary-light);
  color: var(--primary);
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.content-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 8px 14px;
  flex: 1;
  min-width: 200px;
  color: var(--text-light);
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: none;
  font-size: 14px;
  color: var(--text);
}

.search-input::placeholder {
  color: #a0aec0;
}

.filter-group {
  display: flex;
  gap: 10px;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 40px;
}

/* Users table */
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.email-cell {
  color: var(--text-light);
  font-size: 13px;
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

/* Toggle */
.toggle-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-toggle {
  width: 40px;
  height: 22px;
  border-radius: 11px;
  background: #e2e8f0;
  border: none;
  cursor: pointer;
  padding: 2px;
  display: flex;
  align-items: center;
  transition: background 0.2s;
  position: relative;
}

.toggle-active {
  background: var(--success);
}

.toggle-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  transition: transform 0.2s;
}

.toggle-active .toggle-thumb {
  transform: translateX(18px);
}

.toggle-label {
  font-size: 13px;
  color: var(--text-light);
}

.icon-btn {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  transition: var(--transition);
}

.icon-btn:hover {
  background: var(--content-bg);
  color: var(--primary);
}

/* Canaux grid */
.canaux-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}

.canal-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  padding: 18px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 14px;
  transition: var(--transition);
}

.canal-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--primary);
}

.canal-icon {
  width: 40px;
  height: 40px;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.canal-info {
  flex: 1;
  min-width: 0;
}

.canal-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.canal-meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.canal-private,
.canal-public {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.canal-private {
  background: rgba(245, 101, 101, 0.1);
  color: var(--danger);
}

.canal-public {
  background: rgba(72, 187, 120, 0.1);
  color: var(--success);
}

.canal-members {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-light);
  flex-shrink: 0;
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
  .page-topbar {
    padding: 0 16px;
  }

  .admin-tabs {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    padding: 0 16px;
  }

  .tab-btn {
    white-space: nowrap;
    flex-shrink: 0;
    margin-right: 20px;
  }

  .admin-content {
    padding: 16px;
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-bar {
    width: 100%;
  }

  .filter-group {
    width: 100%;
    flex-wrap: wrap;
  }

  .filter-group .form-input {
    flex: 1;
    min-width: 120px;
  }

  /* Hide email column on small screens */
  .email-cell {
    display: none;
  }

  .canaux-grid {
    grid-template-columns: 1fr;
  }

  .desktop-notif {
    display: none;
  }
}
</style>
