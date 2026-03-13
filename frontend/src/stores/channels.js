import { defineStore } from 'pinia'
import api from '../api/axios.js'

export const useChannelsStore = defineStore('channels', {
  state: () => ({
    channels: [],
    activeChannelId: null,
    loading: false
  }),

  getters: {
    activeChannel: (state) => state.channels.find(c => c.id === state.activeChannelId),
    sortedChannels: (state) => [...state.channels].sort((a, b) => a.nom?.localeCompare(b.nom))
  },

  actions: {
    async fetchMyChannels() {
      this.loading = true
      try {
        const response = await api.get('/canaux/mes-canaux')
        this.channels = response.data
      } catch (error) {
        console.error('Error fetching channels:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchAllChannels() {
      this.loading = true
      try {
        const response = await api.get('/canaux')
        return response.data
      } catch (error) {
        console.error('Error fetching all channels:', error)
        return []
      } finally {
        this.loading = false
      }
    },

    async createChannel(nom, estPrive = false) {
      try {
        const response = await api.post('/canaux', { nom, estPrive })
        this.channels.push(response.data)
        return { success: true, channel: response.data }
      } catch (error) {
        const msg = error.response?.data?.message || 'Erreur lors de la création'
        return { success: false, message: msg }
      }
    },

    async getChannelMembers(canalId) {
      try {
        const response = await api.get(`/canaux/${canalId}/membres`)
        return response.data
      } catch (error) {
        console.error('Error fetching members:', error)
        return []
      }
    },

    async addMember(canalId, idUser) {
      try {
        await api.post(`/canaux/${canalId}/membres`, { idUser })
        return { success: true }
      } catch (error) {
        const msg = error.response?.data?.message || 'Erreur lors de l\'ajout'
        return { success: false, message: msg }
      }
    },

    setActiveChannel(id) {
      this.activeChannelId = id
    },

    removeChannel(id) {
      this.channels = this.channels.filter(c => c.id !== id)
    }
  }
})
