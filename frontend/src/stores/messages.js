import { defineStore } from 'pinia'
import api from '../api/axios.js'

// Normalize backend MessageDto fields to frontend field names
function normalizeMessage(msg) {
  return {
    ...msg,
    id: msg.id ?? msg.idMessage,
    canalId: msg.canalId,
    contenu: msg.contenu,
    dateEnvoi: msg.dateEnvoi,
    auteurId: msg.auteurId ?? msg.userId,
    auteurNom: msg.auteurNom ?? msg.userNom,
    auteurPrenom: msg.auteurPrenom ?? msg.userPrenom,
    pieceJointeUrl: msg.pieceJointeUrl ?? (msg.piecesJointes?.[0]?.url || null),
    pieceJointeNom: msg.pieceJointeNom ?? (msg.piecesJointes?.[0]?.nomFichier || null)
  }
}

export const useMessagesStore = defineStore('messages', {
  state: () => ({
    messages: {},
    unreadCounts: {},
    loading: false
  }),

  getters: {
    getMessages: (state) => (canalId) => state.messages[canalId] || [],
    getUnreadCount: (state) => (canalId) => state.unreadCounts[canalId] || 0,
    totalUnread: (state) => Object.values(state.unreadCounts).reduce((sum, n) => sum + n, 0)
  },

  actions: {
    async fetchMessages(canalId) {
      this.loading = true
      try {
        const response = await api.get(`/messages/${canalId}`)
        const msgs = (response.data || []).map(normalizeMessage)
        this.messages[canalId] = msgs
        return msgs
      } catch (error) {
        console.error('Error fetching messages:', error)
        this.messages[canalId] = []
        return []
      } finally {
        this.loading = false
      }
    },

    addMessage(msg) {
      const normalized = normalizeMessage(msg)
      const canalId = normalized.canalId || normalized.canal?.id
      if (!canalId) return
      if (!this.messages[canalId]) {
        this.messages[canalId] = []
      }
      // Check if this message already exists by real id
      if (normalized.id && this.messages[canalId].some(m => m.id === normalized.id)) return
      // Replace matching optimistic (temp) message from same author with same content
      const tempIdx = this.messages[canalId].findIndex(m =>
        m.id?.toString().startsWith('temp-') &&
        m.auteurId === normalized.auteurId &&
        m.contenu === normalized.contenu
      )
      if (tempIdx !== -1) {
        this.messages[canalId].splice(tempIdx, 1, normalized)
      } else {
        this.messages[canalId].push(normalized)
      }
    },

    incrementUnread(canalId) {
      if (!this.unreadCounts[canalId]) {
        this.unreadCounts[canalId] = 0
      }
      this.unreadCounts[canalId]++
    },

    markAsRead(canalId) {
      this.unreadCounts[canalId] = 0
      api.put(`/messages/${canalId}/lire`).catch(() => {})
    },

    async sendMessage(canalId, contenu, fileUrl = null, fileName = null) {
      try {
        const payload = {
          canalId,
          contenu,
          ...(fileUrl && { pieceJointeUrl: fileUrl }),
          ...(fileName && { pieceJointeNom: fileName })
        }
        const response = await api.post(`/messages/${canalId}`, payload)
        return { success: true, message: response.data }
      } catch (error) {
        const msg = error.response?.data?.message || 'Erreur lors de l\'envoi'
        return { success: false, message: msg }
      }
    },

    async exportConversation(canalId, format) {
      try {
        const response = await api.get(`/messages/${canalId}/export/${format}`, {
          responseType: 'blob'
        })
        const contentType = response.headers['content-type']
        const blob = new Blob([response.data], { type: contentType })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `canal-${canalId}-export.${format}`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        return { success: true }
      } catch (error) {
        return { success: false, message: 'Erreur lors de l\'export' }
      }
    },

    async uploadFile(file) {
      try {
        const formData = new FormData()
        formData.append('file', file)
        const response = await api.post('/files/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        return { success: true, url: response.data.url, name: file.name }
      } catch (error) {
        return { success: false, message: 'Erreur lors de l\'upload du fichier' }
      }
    },

    clearChannel(canalId) {
      delete this.messages[canalId]
      delete this.unreadCounts[canalId]
    }
  }
})
