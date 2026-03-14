import { defineStore } from 'pinia'
import api from '../api/axios.js'

function normalizeNotification(notif = {}) {
  return {
    ...notif,
    id: notif.id ?? notif.idNotif ?? null,
    lu: notif.lu ?? notif.isLu ?? false,
    message: notif.message ?? notif.contenu ?? '',
    contenu: notif.contenu ?? notif.message ?? '',
    type: notif.type ?? 'INFO',
    createdAt: notif.createdAt ?? notif.dateCreation ?? null,
    targetType: notif.targetType ?? null,
    targetId: notif.targetId ?? null,
    targetRoute: notif.targetRoute ?? null
  }
}

export const useNotificationsStore = defineStore('notifications', {
  state: () => ({
    notifications: [],
    unreadCount: 0,
    loading: false
  }),

  getters: {
    sortedNotifications: (state) =>
      [...state.notifications].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  },

  actions: {
    async fetchNotifications() {
      this.loading = true
      try {
        const response = await api.get('/notifications')
        this.notifications = (response.data || []).map(normalizeNotification)
        this.unreadCount = this.notifications.filter(n => !n.lu).length
      } catch (error) {
        console.error('Error fetching notifications:', error)
      } finally {
        this.loading = false
      }
    },

    async markAsRead(id) {
      try {
        await api.put(`/notifications/${id}/lire`)
        const notif = this.notifications.find(n => n.id === id)
        if (notif && !notif.lu) {
          notif.lu = true
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        }
      } catch (error) {
        console.error('Error marking notification as read:', error)
      }
    },

    async markAllAsRead() {
      try {
        await api.put('/notifications/lire-tout')
        this.notifications.forEach(n => { n.lu = true })
        this.unreadCount = 0
      } catch (error) {
        console.error('Error marking all as read:', error)
      }
    },

    addNotification(notif) {
      const normalized = normalizeNotification(notif)
      const alreadyExists = this.notifications.some(existing => existing.id === normalized.id)
      this.notifications = [normalized, ...this.notifications.filter(existing => existing.id !== normalized.id)]
      if (!normalized.lu && !alreadyExists) {
        this.unreadCount++
      }
    }
  }
})
