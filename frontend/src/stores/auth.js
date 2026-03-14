import { defineStore } from 'pinia'
import api from '../api/axios.js'

function normalizeUserPayload(data, fallbackEmail = '') {
  if (!data) return null

  return {
    id: data.id ?? data.userId ?? data.idUser ?? null,
    nom: data.nom ?? '',
    prenom: data.prenom ?? '',
    email: data.email ?? fallbackEmail ?? '',
    role: data.role ?? '',
    idStructure: data.idStructure ?? data.structureId ?? null,
    structureNom: data.structureNom ?? '',
    structureType: data.structureType ?? '',
    msgAbsence: data.msgAbsence ?? '',
    isActive: data.isActive ?? data.actif ?? true,
    isPresent: data.isPresent ?? false,
    avatarUrl: data.avatarUrl ?? '',
    notifyReunions: data.notifyReunions ?? true,
    notifyMessages: data.notifyMessages ?? true,
    notifyAbsences: data.notifyAbsences ?? true
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isAuthenticated: false
  }),

  getters: {
    isAdmin: (state) => state.user?.role === 'Admin',
    isRH: (state) => state.user?.role === 'RH',
    isAdminOrRH: (state) => state.user?.role === 'Admin' || state.user?.role === 'RH',
    fullName: (state) => state.user ? `${state.user.prenom} ${state.user.nom}`.trim() : '',
    initials: (state) => {
      if (!state.user) return '?'
      return `${state.user.prenom?.[0] || ''}${state.user.nom?.[0] || ''}`.toUpperCase()
    }
  },

  actions: {
    async login(email, password) {
      try {
        const response = await api.post('/auth/login', { email, password })
        const data = response.data
        this.token = data.token
        this.user = normalizeUserPayload(data, email)
        this.isAuthenticated = true
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(this.user))
        return { success: true }
      } catch (error) {
        const msg = error.response?.data?.message || 'Identifiants invalides'
        return { success: false, message: msg }
      }
    },

    async logout() {
      try {
        await api.post('/auth/logout', {})
      } catch {
        // Even if the backend session is already gone, we still clear the local session.
      } finally {
        this.user = null
        this.token = null
        this.isAuthenticated = false
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    },

    loadFromStorage() {
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      if (token && userStr) {
        try {
          this.token = token
          this.user = normalizeUserPayload(JSON.parse(userStr))
          this.isAuthenticated = true
        } catch {
          this.logout()
        }
      }
    },

    updateUser(userData) {
      this.user = normalizeUserPayload({ ...this.user, ...userData }, this.user?.email)
      localStorage.setItem('user', JSON.stringify(this.user))
    },

    async refreshCurrentUser() {
      if (!this.token) {
        return { success: false, message: 'Utilisateur non connecté' }
      }

      try {
        const response = await api.get('/users/me')
        this.updateUser(response.data)
        return { success: true, user: this.user }
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || 'Impossible de recharger le profil'
        }
      }
    }
  }
})
