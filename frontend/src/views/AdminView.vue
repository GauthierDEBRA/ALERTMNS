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
      <div v-if="isRH" class="alert alert-info page-alert">
        Accès RH : consultation des utilisateurs, des pointages et du journal d'audit. Les actions de gestion restent réservées à l'admin.
      </div>

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
              <option value="Responsable">Responsable</option>
              <option value="Collaborateur">Collaborateur</option>
            </select>
            <select v-model="statusFilter" class="form-input" style="width: auto;">
              <option value="">Tous les statuts</option>
              <option value="active">Actifs</option>
              <option value="inactive">Inactifs</option>
            </select>
          </div>
          <button v-if="canCreateUsers" class="btn btn-primary btn-sm" @click="openCreateUser">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Nouvel utilisateur
          </button>
        </div>

        <div v-if="usersLoading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <div v-else class="table-container">
          <table>
            <thead>
              <tr>
                <th>Utilisateur</th>
                <th class="email-column">Email</th>
                <th>Rôle</th>
                <th>Statut</th>
                <th v-if="canManageUsers">Actions</th>
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
                <td class="email-cell email-column">{{ user.email }}</td>
                <td>
                  <span class="role-badge" :class="getRoleClass(user.role)">{{ user.role }}</span>
                </td>
                <td>
                  <div v-if="canManageUsers" class="toggle-wrapper">
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
                  <span v-else class="status-pill" :class="user.actif ? 'status-active' : 'status-inactive'">
                    {{ user.actif ? 'Actif' : 'Inactif' }}
                  </span>
                </td>
                <td v-if="canManageUsers">
                  <div class="row-actions">
                    <button class="icon-btn" @click="openEditUser(user)" title="Modifier">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                      </svg>
                    </button>
                    <button class="icon-btn" @click="openResetPassword(user)" title="Réinitialiser le mot de passe">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="11" width="18" height="10" rx="2"/>
                        <path d="M7 11V8a5 5 0 0 1 10 0v3"/>
                      </svg>
                    </button>
                    <template v-if="confirmDeleteUserId === user.id">
                      <span class="confirm-delete-text">Supprimer ?</span>
                      <button class="icon-btn icon-danger" @click="deleteUser(user)" :disabled="deletingUserId === user.id" title="Confirmer">✓</button>
                      <button class="icon-btn" @click="confirmDeleteUserId = null" title="Annuler">✕</button>
                    </template>
                    <button
                      v-else
                      class="icon-btn icon-danger"
                      @click="confirmDeleteUserId = user.id"
                      :disabled="deletingUserId === user.id || user.id === authStore.user?.id"
                      title="Supprimer"
                    >
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6l-1 14H6L5 6"/>
                        <path d="M10 11v6"/>
                        <path d="M14 11v6"/>
                        <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredUsers.length === 0">
                <td :colspan="canManageUsers ? 5 : 4" style="text-align: center; color: var(--text-light); padding: 30px;">
                  Aucun utilisateur trouvé
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Pointages Tab -->
      <div v-if="activeTab === 'pointages' && canManagePointages">
        <div class="content-header">
          <div class="filter-group">
            <select v-model="pointageUserFilter" class="form-input" style="width: auto;">
              <option value="">Tous les utilisateurs</option>
              <option v-for="user in users" :key="user.id" :value="user.id">
                {{ user.prenom }} {{ user.nom }}
              </option>
            </select>
            <input v-model="pointageStart" class="form-input" style="width: auto;" type="date" />
            <input v-model="pointageEnd" class="form-input" style="width: auto;" type="date" />
          </div>
          <div class="filter-group">
            <button class="btn btn-secondary btn-sm" @click="fetchPointages" :disabled="pointagesLoading">
              {{ pointagesLoading ? 'Chargement...' : 'Actualiser' }}
            </button>
            <div class="export-dropdown-wrapper">
              <button
                class="btn btn-primary btn-sm export-dropdown-btn"
                @click.stop="showExportDropdown = !showExportDropdown"
                :disabled="pointageExporting || pointageXlsxExporting || pointagePdfExporting"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="7 10 12 15 17 10"/>
                  <line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
                Exporter
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </button>
              <div v-if="showExportDropdown" class="export-dropdown">
                <button class="export-dropdown-item" @click="exportPointagesCsv(); showExportDropdown = false" :disabled="pointageExporting">
                  {{ pointageExporting ? 'Export...' : 'CSV (.csv)' }}
                </button>
                <button class="export-dropdown-item" @click="exportPointagesXlsx(); showExportDropdown = false" :disabled="pointageXlsxExporting">
                  {{ pointageXlsxExporting ? 'Export...' : 'Excel (.xlsx)' }}
                </button>
                <button class="export-dropdown-item" @click="exportPointagesPdf(); showExportDropdown = false" :disabled="pointagePdfExporting">
                  {{ pointagePdfExporting ? 'Export...' : 'PDF (.pdf)' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="pointageError" class="alert alert-error page-alert">{{ pointageError }}</div>
        <div v-if="pointageSuccess" class="alert alert-success page-alert">{{ pointageSuccess }}</div>

        <div v-if="pointagesLoading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <div v-else class="table-container">
          <table>
            <thead>
              <tr>
                <th>Utilisateur</th>
                <th>Arrivée</th>
                <th>Départ</th>
                <th>Durée</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in pointages" :key="row.pointageId">
                <td>
                  <div class="user-cell">
                    <div class="user-avatar-sm" :style="{ background: getAvatarColor(row) }">
                      {{ `${row.prenom?.[0] || ''}${row.nom?.[0] || ''}`.toUpperCase() }}
                    </div>
                    <div style="display:flex; flex-direction:column; min-width:0;">
                      <span class="user-name">{{ row.prenom }} {{ row.nom }}</span>
                      <span class="table-subtext">{{ row.email }}</span>
                    </div>
                  </div>
                </td>
                <td>{{ formatPointageDate(row.dateDebut) }}</td>
                <td>{{ row.dateFin ? formatPointageDate(row.dateFin) : 'En cours' }}</td>
                <td>{{ row.dureeFormatted }}</td>
                <td>
                  <span class="status-pill" :class="row.enCours ? 'status-active' : 'status-inactive'">
                    {{ row.enCours ? 'Ouvert' : 'Clôturé' }}
                  </span>
                </td>
              </tr>
              <tr v-if="pointages.length === 0">
                <td colspan="5" style="text-align: center; color: var(--text-light); padding: 30px;">
                  Aucun pointage trouvé pour ces filtres
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Audit Tab -->
      <div v-if="activeTab === 'audit' && canViewAudit">
        <div class="content-header">
          <div class="search-bar audit-search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              v-model="auditQuery"
              type="text"
              placeholder="Rechercher dans l'audit : acteur, action, cible, détail..."
              class="search-input"
            />
          </div>
          <div class="filter-group">
            <select v-model="auditActorFilter" class="form-input" style="width: auto;">
              <option value="">Tous les acteurs</option>
              <option v-for="user in users" :key="user.id" :value="user.id">
                {{ user.prenom }} {{ user.nom }}
              </option>
            </select>
            <select v-model="auditActionFilter" class="form-input" style="width: auto;">
              <option value="">Toutes les actions</option>
              <option v-for="action in auditActionOptions" :key="action" :value="action">{{ action }}</option>
            </select>
            <select v-model="auditTargetTypeFilter" class="form-input" style="width: auto;">
              <option value="">Toutes les cibles</option>
              <option v-for="targetType in auditTargetTypeOptions" :key="targetType" :value="targetType">
                {{ targetType }}
              </option>
            </select>
            <input v-model="auditStart" class="form-input" style="width: auto;" type="date" />
            <input v-model="auditEnd" class="form-input" style="width: auto;" type="date" />
          </div>
          <div class="filter-group">
            <button class="btn btn-secondary btn-sm" @click="fetchAuditLogs" :disabled="auditLogsLoading">
              {{ auditLogsLoading ? 'Chargement...' : 'Actualiser' }}
            </button>
          </div>
        </div>

        <div v-if="auditError" class="alert alert-error page-alert">{{ auditError }}</div>

        <div v-if="auditLogsLoading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <div v-else class="table-container">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Acteur</th>
                <th>Action</th>
                <th>Cible</th>
                <th>Détails</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="entry in auditLogs" :key="entry.id">
                <td>{{ formatDateTime(entry.createdAt) }}</td>
                <td>
                  <div style="display:flex; flex-direction:column; min-width:0;">
                    <span class="user-name">{{ entry.actorPrenom }} {{ entry.actorNom }}</span>
                    <span class="table-subtext">{{ entry.actorRole || 'Système' }}</span>
                  </div>
                </td>
                <td><span class="status-pill status-active">{{ entry.action }}</span></td>
                <td>{{ entry.targetType }}<span v-if="entry.targetId"> #{{ entry.targetId }}</span></td>
                <td class="audit-details">{{ entry.details || '—' }}</td>
              </tr>
              <tr v-if="auditLogs.length === 0">
                <td colspan="5" style="text-align: center; color: var(--text-light); padding: 30px;">
                  Aucune entrée d'audit pour ces filtres
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Channels Tab -->
      <div v-if="activeTab === 'canaux' && canManageChannels">
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
          <div
            v-for="canal in filteredCanaux"
            :key="canal.id"
            class="canal-card canal-card-clickable"
            @click="openChannelMembers(canal)"
          >
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
            <div class="canal-actions">
              <div class="canal-members">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
                <span>{{ canal.membresCount || 0 }} membre{{ (canal.membresCount || 0) > 1 ? 's' : '' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Structures Tab -->
      <div v-if="activeTab === 'structures' && canManageStructures">
        <div class="content-header">
          <div class="search-bar">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              v-model="structureSearch"
              type="text"
              placeholder="Rechercher une structure..."
              class="search-input"
            />
          </div>
          <button class="btn btn-primary btn-sm" @click="openCreateStructure">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Nouvelle structure
          </button>
        </div>

        <div v-if="filteredStructures.length === 0" class="empty-state">
          <div class="empty-state-icon">🏢</div>
          <h3>Aucune structure</h3>
          <p>Créez une structure pour mieux organiser les utilisateurs.</p>
        </div>

        <div v-else class="structures-grid">
          <div v-for="structure in filteredStructures" :key="structure.id" class="structure-card">
            <div class="structure-card-header">
              <div>
                <h3 class="structure-name">{{ structure.nom }}</h3>
                <p v-if="structure.typeStructure" class="structure-type">{{ structure.typeStructure }}</p>
              </div>
              <div class="structure-actions">
                <button class="icon-btn" @click="openEditStructure(structure)" title="Modifier">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
                <template v-if="confirmDeleteStructureId === structure.id">
                  <span class="confirm-delete-text">Supprimer ?</span>
                  <button class="icon-btn icon-danger" @click="deleteStructure(structure)" :disabled="deletingStructureId === structure.id" title="Confirmer">✓</button>
                  <button class="icon-btn" @click="confirmDeleteStructureId = null" title="Annuler">✕</button>
                </template>
                <button v-else class="icon-btn icon-danger" @click="confirmDeleteStructureId = structure.id" :disabled="deletingStructureId === structure.id" title="Supprimer">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6l-1 14H6L5 6"/>
                    <path d="M10 11v6"/>
                    <path d="M14 11v6"/>
                    <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create User Modal -->
    <Teleport to="body">
      <div v-if="showCreateUser" class="modal-overlay" @click.self="closeCreateUser">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Créer un utilisateur</h3>
            <button class="modal-close" @click="closeCreateUser">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="createUserError" class="alert alert-error">{{ createUserError }}</div>
            <div v-if="createUserSuccess" class="alert alert-success">{{ createUserSuccess }}</div>

            <div class="form-group">
              <label class="form-label">Prénom</label>
              <input v-model="createUserForm.prenom" class="form-input" type="text" />
            </div>
            <div class="form-group">
              <label class="form-label">Nom</label>
              <input v-model="createUserForm.nom" class="form-input" type="text" />
            </div>
            <div class="form-group">
              <label class="form-label">Email</label>
              <input v-model="createUserForm.email" class="form-input" type="email" />
            </div>
            <div class="form-group">
              <label class="form-label">Mot de passe</label>
              <input v-model="createUserForm.password" class="form-input" type="password" />
            </div>
            <div class="form-group">
              <label class="form-label">Rôle</label>
              <select v-model="createUserForm.role" class="form-input">
                <option value="Collaborateur">Collaborateur</option>
                <option value="Responsable">Responsable</option>
                <option value="RH">RH</option>
                <option value="Admin">Admin</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Structure</label>
              <select v-model="createUserForm.structureId" class="form-input">
                <option :value="null">Aucune</option>
                <option v-for="structure in structures" :key="structure.idStructure" :value="structure.idStructure">
                  {{ structure.nom }}
                </option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeCreateUser">Annuler</button>
            <button class="btn btn-primary" @click="createUser" :disabled="creatingUser">
              {{ creatingUser ? 'Création...' : 'Créer le compte' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

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
                <option value="Responsable">Responsable</option>
                <option value="Collaborateur">Collaborateur</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Structure</label>
              <select v-model="editingUser.structureId" class="form-input">
                <option :value="null">Aucune</option>
                <option v-for="structure in structures" :key="structure.idStructure" :value="structure.idStructure">
                  {{ structure.nom }}
                </option>
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

    <!-- Reset Password Modal -->
    <Teleport to="body">
      <div v-if="showResetPassword" class="modal-overlay" @click.self="closeResetPassword">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Réinitialiser le mot de passe</h3>
            <button class="modal-close" @click="closeResetPassword">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="resetPasswordError" class="alert alert-error">{{ resetPasswordError }}</div>
            <div v-if="resetPasswordSuccess" class="alert alert-success">{{ resetPasswordSuccess }}</div>

            <p class="modal-helper">
              Nouveau mot de passe pour <strong>{{ resetPasswordTarget?.prenom }} {{ resetPasswordTarget?.nom }}</strong>
            </p>

            <div class="form-group">
              <label class="form-label">Nouveau mot de passe</label>
              <input v-model="resetPasswordForm.password" class="form-input" type="password" />
            </div>
            <div class="form-group">
              <label class="form-label">Confirmation</label>
              <input v-model="resetPasswordForm.confirm" class="form-input" type="password" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeResetPassword">Annuler</button>
            <button class="btn btn-primary" @click="resetPassword" :disabled="resettingPassword">
              {{ resettingPassword ? 'Réinitialisation...' : 'Réinitialiser' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Structure Modal -->
    <Teleport to="body">
      <div v-if="showStructureModal" class="modal-overlay" @click.self="closeStructureModal">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">{{ editingStructureId ? 'Modifier la structure' : 'Créer une structure' }}</h3>
            <button class="modal-close" @click="closeStructureModal">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="structureError" class="alert alert-error">{{ structureError }}</div>
            <div v-if="structureSuccess" class="alert alert-success">{{ structureSuccess }}</div>

            <div class="form-group">
              <label class="form-label">Nom</label>
              <input v-model="structureForm.nom" class="form-input" type="text" />
            </div>
            <div class="form-group">
              <label class="form-label">Type</label>
              <input v-model="structureForm.typeStructure" class="form-input" type="text" placeholder="Agence, Service, Direction..." />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeStructureModal">Annuler</button>
            <button class="btn btn-primary" @click="saveStructure" :disabled="savingStructure">
              {{ savingStructure ? 'Enregistrement...' : 'Enregistrer' }}
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

    <!-- Channel Members Modal -->
    <Teleport to="body">
      <div v-if="showChannelMembersModal" class="modal-overlay" @click.self="closeChannelMembers">
        <div class="modal">
          <div class="modal-header">
            <div>
              <h3 class="modal-title">Gérer les membres</h3>
              <p class="modal-helper modal-helper-tight">{{ selectedChannel?.nom }}</p>
            </div>
            <div class="modal-header-actions">
              <template v-if="confirmDeleteChannelId === selectedChannel?.id">
                <span class="confirm-delete-text">Supprimer le canal ?</span>
                <button class="btn btn-danger btn-sm" @click="deleteSelectedChannel" :disabled="deletingChannelId === selectedChannel?.id">Oui</button>
                <button class="btn btn-secondary btn-sm" @click="confirmDeleteChannelId = null">Non</button>
              </template>
              <button
                v-else
                class="btn btn-danger btn-sm"
                @click="confirmDeleteChannelId = selectedChannel?.id"
                :disabled="deletingChannelId === selectedChannel?.id"
              >
                {{ deletingChannelId === selectedChannel?.id ? 'Suppression...' : 'Supprimer le canal' }}
              </button>
              <button class="modal-close" @click="closeChannelMembers">✕</button>
            </div>
          </div>
          <div class="modal-body">
            <div v-if="channelMembersError" class="alert alert-error">{{ channelMembersError }}</div>
            <div v-if="channelMembersSuccess" class="alert alert-success">{{ channelMembersSuccess }}</div>

            <p class="modal-helper">
              {{ channelMembers.length }} membre{{ channelMembers.length > 1 ? 's' : '' }} dans ce canal.
            </p>

            <div class="form-group">
              <label class="form-label">Ajouter un membre</label>
              <div class="channel-member-add">
                <select v-model="channelMemberForm.userId" class="form-input">
                  <option :value="null">Sélectionner un utilisateur actif</option>
                  <option v-for="user in availableChannelUsers" :key="user.id" :value="user.id">
                    {{ user.prenom }} {{ user.nom }} · {{ user.email }}
                  </option>
                </select>
                <button class="btn btn-primary" @click="addChannelMember" :disabled="addingChannelMember || !channelMemberForm.userId">
                  {{ addingChannelMember ? 'Ajout...' : 'Ajouter' }}
                </button>
              </div>
            </div>

            <div v-if="channelMembersLoading" class="loading-state">
              <div class="spinner"></div>
            </div>

            <div v-else-if="channelMembers.length === 0" class="empty-state channel-members-empty">
              <div class="empty-state-icon">👥</div>
              <h3>Aucun membre</h3>
              <p>Ajoute un premier membre à ce canal.</p>
            </div>

            <div v-else class="channel-members-list">
              <div v-for="member in channelMembers" :key="member.userId" class="channel-member-row">
                <div class="channel-member-user">
                  <div class="user-avatar-sm" :style="{ background: getAvatarColor(member) }">
                    {{ `${member.prenom?.[0] || ''}${member.nom?.[0] || ''}`.toUpperCase() }}
                  </div>
                  <div class="channel-member-meta">
                    <span class="channel-member-name">
                      {{ member.prenom }} {{ member.nom }}
                      <span v-if="member.userId === authStore.user?.id" class="channel-member-self">(vous)</span>
                    </span>
                    <span class="channel-member-email">{{ member.email }}</span>
                  </div>
                </div>

                <div class="channel-member-actions">
                  <select
                    class="form-input channel-role-select"
                    :value="member.role"
                    @change="updateChannelMemberRole(member, $event.target.value)"
                    :disabled="updatingChannelRoleId === member.userId || member.userId === authStore.user?.id"
                  >
                    <option value="membre">Membre</option>
                    <option value="admin">Admin canal</option>
                  </select>
                  <template v-if="confirmRemoveChannelMemberId === member.userId">
                    <span class="confirm-delete-text">Retirer ?</span>
                    <button class="icon-btn icon-danger" @click="removeChannelMember(member)" :disabled="removingChannelMemberId === member.userId" title="Confirmer">✓</button>
                    <button class="icon-btn" @click="confirmRemoveChannelMemberId = null" title="Annuler">✕</button>
                  </template>
                  <button
                    v-else
                    class="icon-btn icon-danger"
                    @click="member.userId !== authStore.user?.id && (confirmRemoveChannelMemberId = member.userId)"
                    :disabled="removingChannelMemberId === member.userId || member.userId === authStore.user?.id"
                    :title="member.userId === authStore.user?.id ? 'Retrait de votre compte désactivé ici' : 'Retirer du canal'"
                  >
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6l-1 14H6L5 6"/>
                      <path d="M10 11v6"/>
                      <path d="M14 11v6"/>
                      <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeChannelMembers">Fermer</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import NotificationPanel from '../components/NotificationPanel.vue'
import api from '../api/axios.js'
import { useChannelsStore } from '../stores/channels.js'
import { useAuthStore } from '../stores/auth.js'
import { getAvatarColor } from '../utils/avatar.js'

const authStore = useAuthStore()
const channelsStore = useChannelsStore()

const activeTab = ref('users')
const users = ref([])
const canaux = ref([])
const structures = ref([])
const usersLoading = ref(false)
const pointagesLoading = ref(false)
const userSearch = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const channelSearch = ref('')
const structureSearch = ref('')
const pointages = ref([])
const pointageUserFilter = ref('')
const pointageStart = ref('')
const pointageEnd = ref('')
const pointageError = ref('')
const pointageSuccess = ref('')
const pointageExporting = ref(false)
const pointageXlsxExporting = ref(false)
const pointagePdfExporting = ref(false)
const auditLogsLoading = ref(false)
const auditLogs = ref([])
const auditActorFilter = ref('')
const auditActionFilter = ref('')
const auditTargetTypeFilter = ref('')
const auditQuery = ref('')
const auditStart = ref('')
const auditEnd = ref('')
const auditError = ref('')

const showCreateUser = ref(false)
const createUserForm = ref({
  nom: '',
  prenom: '',
  email: '',
  password: '',
  role: 'Collaborateur',
  structureId: null
})
const creatingUser = ref(false)
const createUserError = ref('')
const createUserSuccess = ref('')

const showEditUser = ref(false)
const editingUser = ref(null)
const editError = ref('')
const editSuccess = ref('')
const saving = ref(false)
const deletingUserId = ref(null)
const confirmDeleteUserId = ref(null)

const showResetPassword = ref(false)
const resetPasswordTarget = ref(null)
const resetPasswordForm = ref({ password: '', confirm: '' })
const resetPasswordError = ref('')
const resetPasswordSuccess = ref('')
const resettingPassword = ref(false)

const showCreateChannel = ref(false)
const newCanalNom = ref('')
const newCanalPrive = ref(false)
const creatingCanal = ref(false)
const showChannelMembersModal = ref(false)
const selectedChannel = ref(null)
const channelMembers = ref([])
const channelMembersLoading = ref(false)
const channelMembersError = ref('')
const channelMembersSuccess = ref('')
const channelMemberForm = ref({ userId: null })
const addingChannelMember = ref(false)
const removingChannelMemberId = ref(null)
const confirmRemoveChannelMemberId = ref(null)
const updatingChannelRoleId = ref(null)
const deletingChannelId = ref(null)
const confirmDeleteChannelId = ref(null)

const showStructureModal = ref(false)
const editingStructureId = ref(null)
const structureForm = ref({ nom: '', typeStructure: '' })
const structureError = ref('')
const structureSuccess = ref('')
const savingStructure = ref(false)
const deletingStructureId = ref(null)
const confirmDeleteStructureId = ref(null)
const showExportDropdown = ref(false)

const isAdmin = computed(() => authStore.user?.role === 'Admin')
const isRH = computed(() => authStore.user?.role === 'RH')
const canManageUsers = computed(() => isAdmin.value)
const canCreateUsers = computed(() => isAdmin.value)
const canManageChannels = computed(() => isAdmin.value)
const canManageStructures = computed(() => isAdmin.value)
const canManagePointages = computed(() => isAdmin.value || isRH.value)
const canViewAudit = computed(() => isAdmin.value || isRH.value)

const tabs = computed(() => [
  { id: 'users', label: 'Utilisateurs', count: users.value.length },
  ...(canManagePointages.value ? [{ id: 'pointages', label: 'Pointages' }] : []),
  ...(canViewAudit.value ? [{ id: 'audit', label: 'Audit' }] : []),
  ...(canManageChannels.value ? [{ id: 'canaux', label: 'Canaux', count: canaux.value.length }] : []),
  ...(canManageStructures.value ? [{ id: 'structures', label: 'Structures', count: structures.value.length }] : [])
])

const auditActionOptions = [
  'POINTAGE_REPORT_VIEW',
  'POINTAGE_EXPORT_CSV',
  'POINTAGE_EXPORT_XLSX',
  'POINTAGE_EXPORT_PDF',
  'USER_CREATE',
  'USER_UPDATE',
  'USER_PASSWORD_RESET',
  'USER_ACTIVATE',
  'USER_DEACTIVATE',
  'USER_DELETE',
  'STRUCTURE_CREATE',
  'STRUCTURE_UPDATE',
  'STRUCTURE_DELETE',
  'CHANNEL_CREATE',
  'CHANNEL_UPDATE',
  'CHANNEL_DELETE',
  'CHANNEL_MEMBER_ADD',
  'CHANNEL_MEMBER_ROLE_UPDATE',
  'CHANNEL_MEMBER_REMOVE',
  'REUNION_CREATE',
  'REUNION_UPDATE',
  'REUNION_DELETE',
  'REUNION_INVITE',
  'REUNION_REMOVE_PARTICIPANT',
  'REUNION_MANUAL_REMINDER'
]

const auditTargetTypeOptions = [
  'pointage',
  'user',
  'structure',
  'canal',
  'reunion'
]

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

const filteredStructures = computed(() => {
  if (!structureSearch.value) return structures.value
  const q = structureSearch.value.toLowerCase()
  return structures.value.filter((structure) =>
    `${structure.nom || ''} ${structure.typeStructure || ''}`.toLowerCase().includes(q)
  )
})

const availableChannelUsers = computed(() => {
  const memberIds = new Set(channelMembers.value.map((member) => member.userId))
  return [...users.value]
    .filter((user) => user.actif && !memberIds.has(user.id))
    .sort((a, b) => `${a.prenom} ${a.nom}`.localeCompare(`${b.prenom} ${b.nom}`, 'fr'))
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

function formatPointageDate(dateInput) {
  if (!dateInput) return ''
  return new Date(dateInput).toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatDateTime(dateInput) {
  if (!dateInput) return ''
  return new Date(dateInput).toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function normalizeUser(user = {}) {
  return {
    ...user,
    id: user.id ?? user.idUser,
    actif: user.actif ?? user.isActive ?? false,
    structureId: user.structureId ?? null
  }
}

function normalizeStructure(structure = {}) {
  return {
    ...structure,
    id: structure.id ?? structure.idStructure
  }
}

function normalizeChannelMember(member = {}) {
  return {
    ...member,
    id: member.id ?? member.userId ?? member.idUser,
    userId: member.userId ?? member.idUser ?? member.id,
    role: member.role ?? 'membre'
  }
}

async function fetchUsers() {
  usersLoading.value = true
  try {
    const res = await api.get('/users')
    users.value = (res.data || []).map(normalizeUser)
  } catch (e) {
    console.error('Error fetching users:', e)
  } finally {
    usersLoading.value = false
  }
}

async function fetchPointages() {
  if (!canManagePointages.value) {
    pointages.value = []
    return
  }

  pointagesLoading.value = true
  pointageError.value = ''

  try {
    const res = await api.get('/pointage/admin/report', {
      params: {
        ...(pointageUserFilter.value ? { userId: pointageUserFilter.value } : {}),
        ...(pointageStart.value ? { start: pointageStart.value } : {}),
        ...(pointageEnd.value ? { end: pointageEnd.value } : {})
      }
    })
    pointages.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    pointageError.value = e.response?.data?.message || 'Impossible de charger les pointages.'
    pointages.value = []
  } finally {
    pointagesLoading.value = false
  }
}

async function exportPointagesCsv() {
  if (!canManagePointages.value) return

  pointageExporting.value = true
  pointageError.value = ''
  pointageSuccess.value = ''

  try {
    const response = await api.get('/pointage/admin/export/csv', {
      params: {
        ...(pointageUserFilter.value ? { userId: pointageUserFilter.value } : {}),
        ...(pointageStart.value ? { start: pointageStart.value } : {}),
        ...(pointageEnd.value ? { end: pointageEnd.value } : {})
      },
      responseType: 'blob'
    })

    const blob = new Blob([response.data], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'pointages-export.csv'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    pointageSuccess.value = 'Export CSV généré.'
  } catch (e) {
    pointageError.value = e.response?.data?.message || "Impossible d'exporter les pointages."
  } finally {
    pointageExporting.value = false
  }
}

async function exportPointagesXlsx() {
  if (!canManagePointages.value) return

  pointageXlsxExporting.value = true
  pointageError.value = ''
  pointageSuccess.value = ''

  try {
    const response = await api.get('/pointage/admin/export/xlsx', {
      params: {
        ...(pointageUserFilter.value ? { userId: pointageUserFilter.value } : {}),
        ...(pointageStart.value ? { start: pointageStart.value } : {}),
        ...(pointageEnd.value ? { end: pointageEnd.value } : {})
      },
      responseType: 'blob'
    })

    const blob = new Blob(
      [response.data],
      { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }
    )
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'pointages-export.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    pointageSuccess.value = 'Export XLSX généré.'
  } catch (e) {
    pointageError.value = e.response?.data?.message || "Impossible d'exporter les pointages en XLSX."
  } finally {
    pointageXlsxExporting.value = false
  }
}

async function exportPointagesPdf() {
  if (!canManagePointages.value) return

  pointagePdfExporting.value = true
  pointageError.value = ''
  pointageSuccess.value = ''

  try {
    const response = await api.get('/pointage/admin/export/pdf', {
      params: {
        ...(pointageUserFilter.value ? { userId: pointageUserFilter.value } : {}),
        ...(pointageStart.value ? { start: pointageStart.value } : {}),
        ...(pointageEnd.value ? { end: pointageEnd.value } : {})
      },
      responseType: 'blob'
    })

    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'pointages-export.pdf'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    pointageSuccess.value = 'Export PDF généré.'
  } catch (e) {
    pointageError.value = e.response?.data?.message || "Impossible d'exporter les pointages en PDF."
  } finally {
    pointagePdfExporting.value = false
  }
}

async function fetchAuditLogs() {
  if (!canViewAudit.value) {
    auditLogs.value = []
    return
  }

  auditLogsLoading.value = true
  auditError.value = ''

  try {
    const res = await api.get('/audit-logs', {
      params: {
        ...(auditActorFilter.value ? { actorId: auditActorFilter.value } : {}),
        ...(auditActionFilter.value ? { action: auditActionFilter.value } : {}),
        ...(auditTargetTypeFilter.value ? { targetType: auditTargetTypeFilter.value } : {}),
        ...(auditQuery.value.trim() ? { query: auditQuery.value.trim() } : {}),
        ...(auditStart.value ? { start: auditStart.value } : {}),
        ...(auditEnd.value ? { end: auditEnd.value } : {})
      }
    })
    auditLogs.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    auditError.value = e.response?.data?.message || "Impossible de charger le journal d'audit."
    auditLogs.value = []
  } finally {
    auditLogsLoading.value = false
  }
}

async function fetchCanaux() {
  if (!canManageChannels.value) {
    canaux.value = []
    return
  }

  try {
    const data = await channelsStore.fetchAllChannels()
    canaux.value = data
  } catch (e) {
    console.error('Error fetching canaux:', e)
  }
}

async function fetchStructures() {
  if (!isAdmin.value) {
    structures.value = []
    return
  }

  try {
    const res = await api.get('/structures')
    structures.value = (res.data || []).map(normalizeStructure)
  } catch (e) {
    console.error('Error fetching structures:', e)
  }
}

async function toggleUserStatus(user) {
  if (!canManageUsers.value) return
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

function resetCreateUserForm() {
  createUserForm.value = {
    nom: '',
    prenom: '',
    email: '',
    password: '',
    role: 'Collaborateur',
    structureId: null
  }
}

function openCreateUser() {
  resetCreateUserForm()
  createUserError.value = ''
  createUserSuccess.value = ''
  showCreateUser.value = true
}

function closeCreateUser() {
  showCreateUser.value = false
  createUserError.value = ''
  createUserSuccess.value = ''
  resetCreateUserForm()
}

function openEditUser(user) {
  if (!canManageUsers.value) return
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
    const res = await api.put(`/users/${editingUser.value.id}`, {
      nom: editingUser.value.nom,
      prenom: editingUser.value.prenom,
      email: editingUser.value.email,
      role: editingUser.value.role,
      structureId: editingUser.value.structureId
    })
    const idx = users.value.findIndex(u => u.id === editingUser.value.id)
    if (idx !== -1) {
      users.value[idx] = normalizeUser(res.data)
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

function openResetPassword(user) {
  resetPasswordTarget.value = user
  resetPasswordForm.value = { password: '', confirm: '' }
  resetPasswordError.value = ''
  resetPasswordSuccess.value = ''
  showResetPassword.value = true
}

function closeResetPassword() {
  showResetPassword.value = false
  resetPasswordTarget.value = null
  resetPasswordForm.value = { password: '', confirm: '' }
  resetPasswordError.value = ''
  resetPasswordSuccess.value = ''
}

async function resetPassword() {
  if (!resetPasswordTarget.value?.id) return
  resettingPassword.value = true
  resetPasswordError.value = ''
  resetPasswordSuccess.value = ''

  try {
    if (!resetPasswordForm.value.password || resetPasswordForm.value.password.length < 6) {
      throw new Error('Le mot de passe doit contenir au moins 6 caractères')
    }
    if (resetPasswordForm.value.password !== resetPasswordForm.value.confirm) {
      throw new Error('La confirmation du mot de passe ne correspond pas')
    }

    await api.put(`/users/${resetPasswordTarget.value.id}/password`, {
      password: resetPasswordForm.value.password
    })
    resetPasswordSuccess.value = 'Mot de passe réinitialisé avec succès'
    setTimeout(() => closeResetPassword(), 1000)
  } catch (e) {
    resetPasswordError.value = e.response?.data?.message || e.message || 'Erreur lors de la réinitialisation'
  } finally {
    resettingPassword.value = false
  }
}

async function deleteUser(user) {
  if (!user?.id || user.id === authStore.user?.id) return

  confirmDeleteUserId.value = null
  deletingUserId.value = user.id
  try {
    await api.delete(`/users/${user.id}`)
    users.value = users.value.filter((entry) => entry.id !== user.id)
  } catch (e) {
    console.error('Error deleting user:', e)
    window.alert(e.response?.data?.message || 'Erreur lors de la suppression')
  } finally {
    deletingUserId.value = null
  }
}

async function createUser() {
  creatingUser.value = true
  createUserError.value = ''
  createUserSuccess.value = ''

  try {
    const payload = {
      nom: createUserForm.value.nom,
      prenom: createUserForm.value.prenom,
      email: createUserForm.value.email,
      password: createUserForm.value.password,
      role: createUserForm.value.role,
      structureId: createUserForm.value.structureId
    }

    const res = await api.post('/users', payload)
    users.value.unshift(normalizeUser(res.data))
    createUserSuccess.value = 'Utilisateur créé avec succès'
    setTimeout(() => {
      closeCreateUser()
    }, 1000)
  } catch (e) {
    createUserError.value = e.response?.data?.message || 'Erreur lors de la création'
  } finally {
    creatingUser.value = false
  }
}

function resetStructureForm() {
  structureForm.value = { nom: '', typeStructure: '' }
}

function openCreateStructure() {
  editingStructureId.value = null
  structureError.value = ''
  structureSuccess.value = ''
  resetStructureForm()
  showStructureModal.value = true
}

function openEditStructure(structure) {
  editingStructureId.value = structure.id
  structureError.value = ''
  structureSuccess.value = ''
  structureForm.value = {
    nom: structure.nom || '',
    typeStructure: structure.typeStructure || ''
  }
  showStructureModal.value = true
}

function closeStructureModal() {
  showStructureModal.value = false
  editingStructureId.value = null
  structureError.value = ''
  structureSuccess.value = ''
  resetStructureForm()
}

async function saveStructure() {
  savingStructure.value = true
  structureError.value = ''
  structureSuccess.value = ''

  try {
    const payload = {
      nom: structureForm.value.nom,
      typeStructure: structureForm.value.typeStructure
    }

    const res = editingStructureId.value
      ? await api.put(`/structures/${editingStructureId.value}`, payload)
      : await api.post('/structures', payload)

    const structure = normalizeStructure(res.data)
    if (editingStructureId.value) {
      structures.value = structures.value.map((entry) => entry.id === structure.id ? structure : entry)
      users.value = users.value.map((user) =>
        user.structureId === structure.id
          ? { ...user, structureNom: structure.nom, structureType: structure.typeStructure }
          : user
      )
      structureSuccess.value = 'Structure mise à jour'
    } else {
      structures.value.unshift(structure)
      structureSuccess.value = 'Structure créée'
    }

    structures.value = [...structures.value].sort((a, b) => (a.nom || '').localeCompare(b.nom || '', 'fr'))
    setTimeout(() => closeStructureModal(), 1000)
  } catch (e) {
    structureError.value = e.response?.data?.message || 'Erreur lors de l’enregistrement'
  } finally {
    savingStructure.value = false
  }
}

async function deleteStructure(structure) {
  if (!structure?.id) return

  confirmDeleteStructureId.value = null
  deletingStructureId.value = structure.id
  try {
    await api.delete(`/structures/${structure.id}`)
    structures.value = structures.value.filter((entry) => entry.id !== structure.id)
    users.value = users.value.map((user) =>
      user.structureId === structure.id
        ? { ...user, structureId: null, structureNom: null, structureType: null }
        : user
    )
  } catch (e) {
    console.error('Error deleting structure:', e)
    window.alert(e.response?.data?.message || 'Erreur lors de la suppression')
  } finally {
    deletingStructureId.value = null
  }
}

async function createCanal() {
  if (!canManageChannels.value) return
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

function updateChannelCount(canalId, count) {
  canaux.value = canaux.value.map((canal) =>
    canal.id === canalId ? { ...canal, membresCount: count } : canal
  )

  if (selectedChannel.value?.id === canalId) {
    selectedChannel.value = { ...selectedChannel.value, membresCount: count }
  }
}

async function loadChannelMembers(canalId = selectedChannel.value?.id) {
  if (!canalId) return

  channelMembersLoading.value = true
  channelMembersError.value = ''
  channelMembersSuccess.value = ''

  try {
    const members = await channelsStore.getChannelMembers(canalId)
    channelMembers.value = members
      .map(normalizeChannelMember)
      .sort((a, b) => {
        const roleOrder = a.role === 'admin' ? -1 : b.role === 'admin' ? 1 : 0
        if (roleOrder !== 0) return roleOrder
        return `${a.prenom} ${a.nom}`.localeCompare(`${b.prenom} ${b.nom}`, 'fr')
      })
    updateChannelCount(canalId, channelMembers.value.length)
  } catch (e) {
    channelMembersError.value = 'Impossible de charger les membres du canal'
  } finally {
    channelMembersLoading.value = false
  }
}

async function openChannelMembers(canal) {
  selectedChannel.value = { ...canal }
  channelMembers.value = []
  channelMemberForm.value = { userId: null }
  channelMembersError.value = ''
  channelMembersSuccess.value = ''
  showChannelMembersModal.value = true
  await loadChannelMembers(canal.id)
}

function closeChannelMembers() {
  showChannelMembersModal.value = false
  selectedChannel.value = null
  channelMembers.value = []
  channelMemberForm.value = { userId: null }
  channelMembersError.value = ''
  channelMembersSuccess.value = ''
}

async function addChannelMember() {
  if (!selectedChannel.value?.id || !channelMemberForm.value.userId) return

  addingChannelMember.value = true
  channelMembersError.value = ''
  channelMembersSuccess.value = ''

  try {
    const result = await channelsStore.addMember(selectedChannel.value.id, channelMemberForm.value.userId)
    if (!result.success) {
      throw new Error(result.message)
    }
    await loadChannelMembers(selectedChannel.value.id)
    channelMembersSuccess.value = 'Membre ajouté au canal'
    channelMemberForm.value = { userId: null }
  } catch (e) {
    channelMembersError.value = e.message || 'Erreur lors de l’ajout du membre'
  } finally {
    addingChannelMember.value = false
  }
}

async function updateChannelMemberRole(member, role) {
  if (!selectedChannel.value?.id || !member?.userId || member.userId === authStore.user?.id) return

  updatingChannelRoleId.value = member.userId
  channelMembersError.value = ''
  channelMembersSuccess.value = ''

  try {
    const result = await channelsStore.updateMemberRole(selectedChannel.value.id, member.userId, role)
    if (!result.success) {
      throw new Error(result.message)
    }
    await loadChannelMembers(selectedChannel.value.id)
    channelMembersSuccess.value = 'Rôle du membre mis à jour'
  } catch (e) {
    channelMembersError.value = e.message || 'Erreur lors du changement de rôle'
  } finally {
    updatingChannelRoleId.value = null
  }
}

async function removeChannelMember(member) {
  if (!selectedChannel.value?.id || !member?.userId || member.userId === authStore.user?.id) return

  confirmRemoveChannelMemberId.value = null
  removingChannelMemberId.value = member.userId
  channelMembersError.value = ''
  channelMembersSuccess.value = ''

  try {
    const result = await channelsStore.removeMember(selectedChannel.value.id, member.userId)
    if (!result.success) {
      throw new Error(result.message)
    }
    await loadChannelMembers(selectedChannel.value.id)
    channelMembersSuccess.value = 'Membre retiré du canal'
  } catch (e) {
    channelMembersError.value = e.message || 'Erreur lors du retrait du membre'
  } finally {
    removingChannelMemberId.value = null
  }
}

async function deleteSelectedChannel() {
  if (!selectedChannel.value?.id) return

  confirmDeleteChannelId.value = null
  deletingChannelId.value = selectedChannel.value.id
  channelMembersError.value = ''
  channelMembersSuccess.value = ''

  try {
    await api.delete(`/canaux/${selectedChannel.value.id}`)
    canaux.value = canaux.value.filter((canal) => canal.id !== selectedChannel.value.id)
    channelsStore.removeChannel(selectedChannel.value.id)
    closeChannelMembers()
  } catch (e) {
    channelMembersError.value = e.response?.data?.message || 'Erreur lors de la suppression du canal'
  } finally {
    deletingChannelId.value = null
  }
}

function handleAdminDocumentClick() {
  showExportDropdown.value = false
}

onMounted(async () => {
  if (!tabs.value.some(tab => tab.id === activeTab.value)) {
    activeTab.value = 'users'
  }

  const tasks = [fetchUsers()]
  if (canManagePointages.value) tasks.push(fetchPointages())
  if (canViewAudit.value) tasks.push(fetchAuditLogs())
  if (canManageChannels.value) tasks.push(fetchCanaux())
  if (isAdmin.value) tasks.push(fetchStructures())
  await Promise.all(tasks)

  document.addEventListener('click', handleAdminDocumentClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleAdminDocumentClick)
})

watch([pointageUserFilter, pointageStart, pointageEnd], () => {
  if (canManagePointages.value) {
    fetchPointages()
  }
})

watch([auditActorFilter, auditActionFilter, auditTargetTypeFilter, auditQuery, auditStart, auditEnd], () => {
  if (canViewAudit.value) {
    fetchAuditLogs()
  }
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

.audit-details {
  color: var(--text-light);
  max-width: 380px;
  white-space: normal;
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

.page-alert {
  margin-bottom: 20px;
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

.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: rgba(72, 187, 120, 0.12);
  color: var(--success);
}

.status-inactive {
  background: rgba(245, 101, 101, 0.12);
  color: var(--danger);
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 6px;
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

.icon-danger:hover {
  color: var(--danger);
}

.modal-helper {
  margin-bottom: 16px;
  color: var(--text-light);
  font-size: 14px;
}

.modal-helper-tight {
  margin: 4px 0 0;
}

.modal-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
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

.canal-card-clickable {
  cursor: pointer;
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

.canal-actions {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  flex-direction: column;
  flex-shrink: 0;
}

.export-dropdown-wrapper {
  position: relative;
}

.export-dropdown-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.export-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-md);
  z-index: 50;
  min-width: 150px;
  overflow: hidden;
}

.export-dropdown-item {
  display: block;
  width: 100%;
  padding: 9px 16px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  color: var(--text);
  transition: var(--transition);
}

.export-dropdown-item:hover:not(:disabled) {
  background: var(--content-bg);
  color: var(--primary);
}

.export-dropdown-item:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.confirm-delete-text {
  font-size: 12px;
  font-weight: 600;
  color: var(--danger);
  white-space: nowrap;
}

.channel-member-add {
  display: flex;
  align-items: center;
  gap: 10px;
}

.channel-member-add .form-input {
  flex: 1;
}

.channel-members-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.channel-member-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--white);
}

.channel-member-user {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.channel-member-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.channel-member-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.channel-member-self {
  font-size: 12px;
  color: var(--text-light);
  font-weight: 500;
}

.channel-member-email {
  font-size: 12px;
  color: var(--text-light);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.channel-member-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.channel-role-select {
  min-width: 130px;
}

.status-role-admin {
  background: rgba(232, 80, 26, 0.1);
  color: var(--primary);
}

.status-role-member {
  background: rgba(72, 187, 120, 0.12);
  color: var(--success);
}

.channel-members-empty {
  padding: 20px 0 10px;
}

.structures-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}

.structure-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  padding: 18px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
}

.structure-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.structure-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
}

.structure-type {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-light);
}

.structure-actions {
  display: flex;
  align-items: center;
  gap: 6px;
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
  .email-column {
    display: none;
  }

  .canaux-grid {
    grid-template-columns: 1fr;
  }

  .structures-grid {
    grid-template-columns: 1fr;
  }

  .row-actions {
    flex-wrap: wrap;
  }

  .channel-member-add {
    flex-direction: column;
    align-items: stretch;
  }

  .channel-member-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .channel-member-actions {
    width: 100%;
    justify-content: space-between;
  }

  .canal-actions {
    align-items: flex-start;
  }

  .modal-header-actions {
    gap: 6px;
  }

  .desktop-notif {
    display: none;
  }

  .table-container {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
}
</style>
