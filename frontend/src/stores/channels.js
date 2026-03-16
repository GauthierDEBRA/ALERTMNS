import { defineStore } from 'pinia'
import api from '../api/axios.js'

function buildDisplayName(channel) {
  if ((channel.typeCanal ?? 'canal') === 'direct') {
    const fullName = [channel.directUserPrenom, channel.directUserNom].filter(Boolean).join(' ').trim()
    return fullName || channel.directUserEmail || 'Conversation privée'
  }

  return (channel.nom || 'canal').replace(/^#/, '')
}

function normalizeChannel(channel) {
  const normalized = {
    ...channel,
    id: channel.id ?? channel.idCanal,
    idCanal: channel.idCanal ?? channel.id,
    estPrive: channel.estPrive ?? false,
    typeCanal: channel.typeCanal ?? 'canal',
    membresCount: channel.membresCount ?? channel.membersCount ?? 0,
    lastMessageAt: channel.lastMessageAt ?? null,
    directUserId: channel.directUserId ?? null,
    directUserNom: channel.directUserNom ?? '',
    directUserPrenom: channel.directUserPrenom ?? '',
    directUserEmail: channel.directUserEmail ?? '',
    directUserAvatarUrl: channel.directUserAvatarUrl ?? ''
  }

  normalized.displayName = buildDisplayName(normalized)
  return normalized
}

function normalizeMember(member) {
  return {
    ...member,
    id: member.id ?? member.userId ?? member.idUser,
    userId: member.userId ?? member.idUser ?? member.id,
    avatarUrl: member.avatarUrl ?? ''
  }
}

export const useChannelsStore = defineStore('channels', {
  state: () => ({
    channels: [],
    activeChannelId: null,
    loading: false
  }),

  getters: {
    activeChannel: (state) => state.channels.find(c => c.id === state.activeChannelId),
    groupChannels: (state) => state.channels.filter(channel => channel.typeCanal !== 'direct'),
    directChannels: (state) => state.channels.filter(channel => channel.typeCanal === 'direct'),
    sortedChannels() {
      return [...this.groupChannels].sort((a, b) => a.displayName.localeCompare(b.displayName, 'fr'))
    },
    sortedDirectChannels() {
      return [...this.directChannels].sort((a, b) => {
        const aTime = a.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0
        const bTime = b.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0
        if (aTime !== bTime) {
          return bTime - aTime
        }
        return a.displayName.localeCompare(b.displayName, 'fr')
      })
    }
  },

  actions: {
    upsertChannel(rawChannel) {
      const channel = normalizeChannel(rawChannel)
      const index = this.channels.findIndex(existing => existing.id === channel.id)
      if (index === -1) {
        this.channels.push(channel)
      } else {
        this.channels.splice(index, 1, channel)
      }
      return channel
    },

    async fetchMyChannels() {
      this.loading = true
      try {
        const response = await api.get('/canaux/mes-canaux')
        this.channels = (response.data || []).map(normalizeChannel)
        return true
      } catch (error) {
        console.error('Error fetching channels:', error)
        return false
      } finally {
        this.loading = false
      }
    },

    async fetchAllChannels() {
      this.loading = true
      try {
        const response = await api.get('/canaux')
        return (response.data || []).map(normalizeChannel)
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
        const channel = this.upsertChannel(response.data)
        return { success: true, channel }
      } catch (error) {
        const msg = error.response?.data?.message || 'Erreur lors de la creation'
        return { success: false, message: msg }
      }
    },

    async startDirectConversation(userId) {
      try {
        const response = await api.post(`/canaux/direct/${userId}`)
        const channel = this.upsertChannel(response.data)
        return { success: true, channel }
      } catch (error) {
        const msg = error.response?.data?.message || 'Erreur lors de l\'ouverture de la conversation'
        return { success: false, message: msg }
      }
    },

    async getChannelMembers(canalId) {
      try {
        const response = await api.get(`/canaux/${canalId}/membres`)
        return (response.data || []).map(normalizeMember)
      } catch (error) {
        console.error('Error fetching members:', error)
        return []
      }
    },

    async addMember(canalId, userId) {
      try {
        await api.post(`/canaux/${canalId}/membres`, { userId })
        return { success: true }
      } catch (error) {
        const msg = error.response?.data?.message || 'Erreur lors de l\'ajout'
        return { success: false, message: msg }
      }
    },

    async removeMember(canalId, userId) {
      try {
        await api.delete(`/canaux/${canalId}/membres/${userId}`)
        return { success: true }
      } catch (error) {
        const msg = error.response?.data?.message || 'Erreur lors du retrait'
        return { success: false, message: msg }
      }
    },

    async updateMemberRole(canalId, userId, role) {
      try {
        await api.put(`/canaux/${canalId}/membres/${userId}`, { role })
        return { success: true }
      } catch (error) {
        const msg = error.response?.data?.message || 'Erreur lors du changement de role'
        return { success: false, message: msg }
      }
    },

    setActiveChannel(id) {
      this.activeChannelId = id
    },

    touchChannelActivity(id, dateTime = null) {
      const index = this.channels.findIndex(channel => channel.id === id)
      if (index === -1) return

      const updatedChannel = {
        ...this.channels[index],
        lastMessageAt: dateTime || new Date().toISOString()
      }
      this.channels.splice(index, 1, updatedChannel)
    },

    removeChannel(id) {
      this.channels = this.channels.filter(c => c.id !== id)
    }
  }
})
