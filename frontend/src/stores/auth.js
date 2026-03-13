import { defineStore } from 'pinia'
import api from '../api/axios.js'

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
    fullName: (state) => state.user ? `${state.user.prenom} ${state.user.nom}` : '',
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
        this.user = {
          id: data.userId,
          nom: data.nom,
          prenom: data.prenom,
          email: email,
          role: data.role,
          idStructure: data.structureId,
          msgAbsence: data.msgAbsence
        }
        this.isAuthenticated = true
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(this.user))
        return { success: true }
      } catch (error) {
        const msg = error.response?.data?.message || 'Identifiants invalides'
        return { success: false, message: msg }
      }
    },

    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    loadFromStorage() {
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      if (token && userStr) {
        try {
          this.token = token
          this.user = JSON.parse(userStr)
          this.isAuthenticated = true
        } catch {
          this.logout()
        }
      }
    },

    updateUser(userData) {
      this.user = { ...this.user, ...userData }
      localStorage.setItem('user', JSON.stringify(this.user))
    }
  }
})
