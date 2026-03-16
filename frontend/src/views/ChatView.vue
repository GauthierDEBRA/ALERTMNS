<template>
  <div class="chat-view">
    <!-- Chat header -->
    <div class="chat-header">
      <div class="header-left">
        <template v-if="isDirectChannel">
          <div class="direct-header-avatar" :style="{ background: directAvatarColor }">
            <img
              v-if="channel?.directUserAvatarUrl"
              :src="channel.directUserAvatarUrl"
              :alt="channelDisplayName"
              class="direct-header-avatar-image"
            />
            <template v-else>
              {{ directInitials }}
            </template>
          </div>
        </template>
        <span v-else class="channel-hash">#</span>
        <div class="channel-info">
          <h2 class="channel-name">{{ channelDisplayName }}</h2>
          <span class="member-count" :class="{ 'member-count-typing': Boolean(typingLabel) }">
            {{ typingLabel || channelSubtitle }}
          </span>
        </div>
      </div>

      <div class="header-right">
        <!-- Search toggle button -->
        <button class="header-btn" :class="{ 'header-btn-active': showSearch }" @click="toggleSearch" title="Rechercher (Ctrl+F)">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
        </button>

        <!-- Members button -->
        <button class="header-btn" @click="showMembers = !showMembers" title="Membres">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </button>

        <!-- Export dropdown -->
        <div class="export-wrapper" ref="exportRef">
          <button class="header-btn" @click="showExport = !showExport" title="Exporter">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
          </button>
          <div v-if="showExport" class="export-dropdown">
            <button @click="exportConversation('json')">
              📋 Exporter JSON
            </button>
            <button @click="exportConversation('csv')">
              📊 Exporter CSV
            </button>
            <button @click="exportConversation('xml')">
              🗂 Exporter XML
            </button>
            <button @click="exportConversation('xlsx')">
              📗 Exporter Excel
            </button>
            <button @click="exportConversation('pdf')">
              📄 Exporter PDF
            </button>
          </div>
        </div>

        <div class="desktop-notif-chat">
          <NotificationPanel />
        </div>
      </div>
    </div>

    <Transition name="search-slide">
      <div v-if="showSearch" class="chat-toolbar">
        <div class="chat-search">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            ref="searchInputRef"
            v-model="searchQuery"
            type="text"
            class="chat-search-input"
            placeholder="Rechercher dans cette conversation…"
            @keydown.escape="toggleSearch"
          />
          <button v-if="searchQuery" class="chat-search-clear" @click="clearSearch">Effacer</button>
        </div>
        <span v-if="searchQuery.trim()" class="search-count">
          {{ filteredMessages.length }} résultat{{ filteredMessages.length > 1 ? 's' : '' }}
        </span>
      </div>
    </Transition>

    <div class="chat-body">
      <!-- Messages area -->
      <div class="messages-area" ref="messagesContainer">
        <div v-if="messagesStore.loading" class="messages-loading">
          <div class="spinner"></div>
          <p>Chargement des messages...</p>
        </div>

        <template v-else>
          <!-- Sentinelle pour le scroll infini vers le haut -->
          <div ref="topSentinel" class="top-sentinel"></div>

          <!-- Indicateur de chargement des anciens messages -->
          <div v-if="messagesStore.loadingMore[canalId]" class="messages-loading-more">
            <div class="spinner spinner-sm"></div>
            <span>Chargement des messages précédents...</span>
          </div>

          <!-- Indicateur "début de la conversation" -->
          <div v-else-if="!messagesStore.hasMore[canalId] && messages.length > 0" class="conversation-start">
            <div class="conversation-start-icon">{{ isDirectChannel ? '@' : '#' }}</div>
            <p>Début de la conversation</p>
          </div>
        </template>

        <div v-if="!messagesStore.loading && messagesStore.hasLoadError(canalId)" class="messages-empty" style="color:var(--color-danger,#ef4444);">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom:8px;opacity:0.6">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <p>Impossible de charger les messages.</p>
          <button class="btn btn-sm" style="margin-top:8px;" @click="messagesStore.fetchMessages(canalId)">Réessayer</button>
        </div>
        <div v-else-if="!messagesStore.loading && messages.length === 0" class="messages-empty">
          <div class="empty-channel-icon">{{ isDirectChannel ? '@' : '#' }}</div>
          <h3>{{ isDirectChannel ? `Conversation avec ${channelDisplayName}` : `Bienvenue dans #${channelDisplayName}` }}</h3>
          <p>{{ isDirectChannel ? 'Dites bonjour pour lancer la conversation privée.' : "C'est le début de ce canal. Envoyez le premier message !" }}</p>
        </div>

        <div v-else-if="filteredMessages.length === 0" class="messages-empty">
          <div class="empty-channel-icon">?</div>
          <h3>Aucun résultat</h3>
          <p>Essaie avec un autre mot-clé dans cette conversation.</p>
        </div>

        <template v-else>
          <div
            v-for="(group, gIndex) in groupedMessages"
            :key="gIndex"
          >
            <!-- Day separator -->
            <div v-if="group.showDaySep" class="day-separator">
              <div class="day-line"></div>
              <span class="day-label">{{ group.dayLabel }}</span>
              <div class="day-line"></div>
            </div>

            <!-- Unread separator -->
            <div v-if="group.showUnreadSep" class="unread-separator">
              <div class="unread-line"></div>
              <span class="unread-label">Nouveaux messages</span>
              <div class="unread-line"></div>
            </div>

            <!-- Message -->
            <div
              class="message"
              :class="{
                'message-own': isOwnMessage(group.msg),
                'message-grouped': !group.showHeader,
                'message-group-start': group.showHeader,
                'message-highlighted': highlightedMessageId === group.msg.id
              }"
              :data-message-id="group.msg.id"
            >
              <!-- Avatar / spacer -->
              <div class="message-avatar-col">
                <div
                  v-if="group.showHeader"
                  class="message-avatar"
                  :style="{ background: getAvatarColor(group.msg) }"
                >
                  <img
                    v-if="group.msg.auteurAvatarUrl"
                    :src="group.msg.auteurAvatarUrl"
                    :alt="getMsgFullName(group.msg)"
                    class="message-avatar-image"
                  />
                  <template v-else>
                    {{ getMsgInitials(group.msg) }}
                  </template>
                </div>
                <div v-else class="message-avatar-placeholder">
                  <span class="message-time-hover">{{ formatTimeOnly(group.msg.dateEnvoi || group.msg.createdAt) }}</span>
                </div>
              </div>

              <!-- Content -->
              <div class="message-content">
                <div v-if="group.showHeader" class="message-header">
                  <span class="message-author">{{ getMsgFullName(group.msg) }}</span>
                  <div class="message-meta">
                    <span class="message-time">{{ formatDate(group.msg.dateEnvoi || group.msg.createdAt) }}</span>
                    <span v-if="group.msg.dateModification && !group.msg.isDeleted" class="message-edited-badge">modifié</span>
                  </div>
                </div>

                <!-- Absence banner -->
                <div v-if="group.msg.msgAbsence" class="absence-banner">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  {{ group.msg.msgAbsence }}
                </div>

                <div v-if="editingMessageId === group.msg.id" class="message-edit-box">
                  <textarea
                    v-model="editingText"
                    class="message-edit-input"
                    rows="3"
                    placeholder="Modifier le message"
                    @keydown.escape="cancelEditing"
                    @keydown.enter.ctrl.exact.prevent="saveEdit(group.msg)"
                  ></textarea>
                  <div class="message-edit-actions">
                    <button class="btn btn-secondary btn-sm" @click="cancelEditing">Annuler</button>
                    <button
                      class="btn btn-primary btn-sm"
                      @click="saveEdit(group.msg)"
                      :disabled="editingBusy || !editingText.trim()"
                    >
                      {{ editingBusy ? 'Enregistrement...' : 'Enregistrer' }}
                    </button>
                  </div>
                </div>

                <template v-else>
                  <div v-if="group.msg.contenu" class="message-text" :class="{ 'message-text-deleted': group.msg.isDeleted }">
                    {{ group.msg.contenu }}
                  </div>

                  <div v-if="canManageMessage(group.msg)" class="message-inline-actions">
                    <!-- Desktop: text buttons (hidden on mobile) -->
                    <span class="desktop-msg-actions">
                      <button class="message-action-btn" @click="startEditing(group.msg)">Modifier</button>
                      <template v-if="confirmDeleteMessageId === group.msg.id">
                        <span class="confirm-delete-text">Supprimer ?</span>
                        <button class="message-action-btn danger" @click="doRemoveMessage(group.msg)">Oui</button>
                        <button class="message-action-btn" @click="confirmDeleteMessageId = null">Non</button>
                      </template>
                      <button v-else class="message-action-btn danger" @click="confirmDeleteMessageId = group.msg.id">Supprimer</button>
                    </span>
                    <!-- Mobile: ⋮ button + dropdown (hidden on desktop) -->
                    <div class="mobile-msg-menu-wrapper">
                      <button class="message-action-btn mobile-menu-btn" @click.stop="toggleMobileMenu(group.msg.id)" title="Actions">⋮</button>
                      <div v-if="mobileMenuMessageId === group.msg.id" class="mobile-msg-menu">
                        <button @click="startEditing(group.msg); mobileMenuMessageId = null">Modifier</button>
                        <button class="danger" @click="doRemoveMessage(group.msg); mobileMenuMessageId = null">Supprimer</button>
                      </div>
                    </div>
                  </div>
                </template>

                <!-- File attachment -->
                <div v-if="group.msg.pieceJointeUrl && !group.msg.isDeleted" class="message-attachment">
                  <a
                    :href="group.msg.pieceJointeUrl"
                    target="_blank"
                    rel="noopener"
                    class="attachment-link"
                  >
                    <div class="attachment-icon">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
                      </svg>
                    </div>
                    <span>{{ group.msg.pieceJointeNom || 'Pièce jointe' }}</span>
                  </a>
                </div>

                <div v-if="!group.msg.isDeleted" class="message-reactions">
                  <button
                    v-for="reaction in group.msg.reactions || []"
                    :key="`${group.msg.id}-${reaction.emoji}`"
                    class="reaction-chip"
                    :class="{ 'reaction-chip-active': hasReacted(reaction) }"
                    :title="getReactionTooltip(reaction)"
                    @click="toggleReaction(group.msg, reaction.emoji)"
                    :disabled="reactionBusyMessageId === group.msg.id"
                  >
                    <span class="reaction-chip-emoji">{{ reaction.emoji }}</span>
                    <span class="reaction-chip-count">{{ reaction.count }}</span>
                  </button>

                  <div class="reaction-picker-wrapper">
                    <button
                      class="reaction-add-btn"
                      :class="{ 'reaction-add-btn-active': reactionPickerMessageId === group.msg.id }"
                      @click="toggleReactionPicker(group.msg.id, $event)"
                      :disabled="reactionBusyMessageId === group.msg.id"
                    >
                      +
                    </button>

                    <div v-if="reactionPickerMessageId === group.msg.id" class="reaction-picker" :class="{ 'reaction-picker-above': reactionPickerAbove }">
                      <button
                        v-for="emoji in AVAILABLE_REACTIONS"
                        :key="`${group.msg.id}-${emoji}`"
                        class="reaction-picker-btn"
                        @click="toggleReaction(group.msg, emoji)"
                      >
                        {{ emoji }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- Members panel -->
      <Transition name="slide-right">
        <div v-if="showMembers" class="members-panel">
          <div class="members-header">
            <h3>Membres ({{ members.length }})</h3>
            <button class="close-panel-btn" @click="showMembers = false">✕</button>
          </div>
          <div class="members-list">
            <div v-for="member in members" :key="member.id" class="member-item">
              <div class="member-avatar" :style="{ background: getAvatarColor(member) }">
                <img
                  v-if="member.avatarUrl"
                  :src="member.avatarUrl"
                  :alt="`${member.prenom} ${member.nom}`"
                  class="member-avatar-image"
                />
                <template v-else>
                  {{ `${member.prenom?.[0] || ''}${member.nom?.[0] || ''}`.toUpperCase() }}
                </template>
              </div>
              <div class="member-info">
                <span class="member-name">{{ member.prenom }} {{ member.nom }}</span>
                <span class="member-role">{{ member.role }}</span>
              </div>
            </div>
          </div>
          <div v-if="!isDirectChannel && authStore.isAdmin" class="members-footer">
            <button class="btn btn-secondary btn-sm" @click="showAddMember = true">
              + Ajouter un membre
            </button>
          </div>
        </div>
      </Transition>
    </div>

    <!-- Message Input -->
    <div class="message-input-area">
      <div class="input-container">
        <div v-if="sendError" class="send-error">
          {{ sendError }}
        </div>

        <div v-if="uploadingFile" class="upload-status">
          <div class="upload-status-row">
            <span>Envoi du fichier...</span>
            <span>{{ uploadProgress }}%</span>
          </div>
          <div class="upload-progress-track">
            <div class="upload-progress-bar" :style="{ width: `${uploadProgress}%` }"></div>
          </div>
        </div>

        <!-- File preview -->
        <div v-if="pendingFile" class="file-preview">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
          </svg>
          <span>{{ pendingFile.name }}</span>
          <button @click="removePendingFile" class="remove-file-btn">✕</button>
        </div>

        <div class="input-row">
          <!-- File attach button -->
          <label class="attach-btn" title="Joindre un fichier">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
            </svg>
            <input
              ref="fileInput"
              type="file"
              style="display: none"
              @change="handleFileSelect"
            />
          </label>

          <!-- Text input -->
          <textarea
            ref="textInput"
            v-model="messageText"
            class="message-textarea"
            :placeholder="messagePlaceholder"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.exact="addNewLine"
            @input="handleComposerInput"
            rows="1"
          ></textarea>

          <!-- Send button -->
          <button
            class="send-btn"
            :class="{ 'send-active': canSend }"
            @click="sendMessage"
            :disabled="!canSend || sending"
            title="Envoyer (Entrée)"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Add Member Modal -->
    <Teleport to="body">
      <div v-if="showAddMember" class="modal-overlay" @click.self="showAddMember = false">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">Ajouter un membre</h3>
            <button class="modal-close" @click="showAddMember = false">✕</button>
          </div>
          <div class="modal-body">
            <div v-if="addMemberError" class="alert alert-error">{{ addMemberError }}</div>
            <div class="form-group">
              <label class="form-label">Sélectionner un utilisateur</label>
              <select v-model="selectedUserId" class="form-input">
                <option value="">-- Choisir un utilisateur --</option>
                <option
                  v-for="user in availableUsers"
                  :key="user.id"
                  :value="user.id"
                >
                  {{ user.prenom }} {{ user.nom }} ({{ user.email }})
                </option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showAddMember = false">Annuler</button>
            <button
              class="btn btn-primary"
              @click="addMember"
              :disabled="!selectedUserId || addingMember"
            >
              {{ addingMember ? 'Ajout...' : 'Ajouter' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useChannelsStore } from '../stores/channels.js'
import { useMessagesStore } from '../stores/messages.js'
import { useAuthStore } from '../stores/auth.js'
import { useWebSocket } from '../composables/useWebSocket.js'
import { useDate } from '../composables/useDate.js'
import NotificationPanel from '../components/NotificationPanel.vue'
import { getAvatarColor, getAvatarInitials } from '../utils/avatar.js'
import api from '../api/axios.js'

const route = useRoute()
const router = useRouter()
const channelsStore = useChannelsStore()
const messagesStore = useMessagesStore()
const authStore = useAuthStore()
const { subscribe, unsubscribe, subscribeToTyping, unsubscribeTyping, sendTyping, connect } = useWebSocket()
const { formatDate, isSameDay, formatDayHeader } = useDate()

const messagesContainer = ref(null)
const topSentinel = ref(null)
const textInput = ref(null)
const fileInput = ref(null)
const exportRef = ref(null)
const searchInputRef = ref(null)

const messageText = ref('')
const pendingFile = ref(null)
const pendingFileUrl = ref(null)
const sending = ref(false)
const editingBusy = ref(false)
const showMembers = ref(false)
const showExport = ref(false)
const showAddMember = ref(false)
const showSearch = ref(false)
const members = ref([])
const memberCount = ref(0)
const memberAbsenceMap = ref({})
const allUsers = ref([])
const selectedUserId = ref('')
const addingMember = ref(false)
const addMemberError = ref('')
const sendError = ref('')
const uploadingFile = ref(false)
const uploadProgress = ref(0)
const searchQuery = ref('')
const editingMessageId = ref(null)
const editingText = ref('')
const reactionPickerMessageId = ref(null)
const reactionPickerAbove = ref(false)
const reactionBusyMessageId = ref(null)
const highlightedMessageId = ref(null)
const confirmDeleteMessageId = ref(null)
const mobileMenuMessageId = ref(null)
const typingUsers = ref({})
const typingTimers = new Map()
const lastTypingSentAt = ref(0)

const canalId = computed(() => {
  const parsed = Number(route.params.id)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})
const channel = computed(() => channelsStore.channels.find(c => c.id === canalId.value))
const isDirectChannel = computed(() => channel.value?.typeCanal === 'direct')
const channelDisplayName = computed(() => channel.value?.displayName || 'canal')
const channelSubtitle = computed(() => {
  if (isDirectChannel.value) {
    return channel.value?.directUserEmail || 'Conversation privée'
  }
  return `${memberCount.value} membre${memberCount.value !== 1 ? 's' : ''}`
})
const typingLabel = computed(() => {
  const activeUsers = Object.values(typingUsers.value)
  if (!activeUsers.length) return ''

  const names = activeUsers.map(user => `${user.prenom || ''} ${user.nom || ''}`.trim() || user.email || 'Quelqu’un')
  if (names.length === 1) {
    return `${names[0]} est en train d'écrire...`
  }
  if (names.length === 2) {
    return `${names[0]} et ${names[1]} écrivent...`
  }
  return `${names.length} personnes écrivent...`
})
const messagePlaceholder = computed(() =>
  isDirectChannel.value
    ? `Envoyer un message à ${channelDisplayName.value}`
    : `Envoyer un message dans #${channelDisplayName.value}`
)
const messages = computed(() => messagesStore.getMessages(canalId.value))
const filteredMessages = computed(() => {
  const search = searchQuery.value.trim().toLowerCase()
  if (!search) {
    return messages.value
  }

  return messages.value.filter((msg) => {
    const content = msg.isDeleted ? '' : (msg.contenu || '')
    const haystack = `${content} ${msg.auteurPrenom || ''} ${msg.auteurNom || ''}`.toLowerCase()
    return haystack.includes(search)
  })
})
const canSend = computed(() => Boolean(canalId.value && (messageText.value.trim() || pendingFile.value) && !sending.value))
const directInitials = computed(() => getAvatarInitials(channel.value || {}, '@'))
const directAvatarColor = computed(() => getAvatarColor(channel.value || {}))

const availableUsers = computed(() => {
  const memberIds = new Set(members.value.map(member => member.userId || member.id))
  return allUsers.value.filter(u => !memberIds.has(u.id))
})

const AVAILABLE_REACTIONS = ['👍', '❤️', '😂', '🎉', '✅', '👀']

function getMsgInitials(msg) {
  return getAvatarInitials(msg, '?')
}

function getMsgFullName(msg) {
  return `${msg.auteurPrenom || ''} ${msg.auteurNom || ''}`.trim() || 'Utilisateur'
}

function isOwnMessage(msg) {
  return msg.auteurId === authStore.user?.id
}

function canManageMessage(msg) {
  return isOwnMessage(msg) && !msg.isDeleted && editingMessageId.value !== msg.id
}

function hasReacted(reaction) {
  return Boolean(reaction?.userIds?.includes(authStore.user?.id))
}

function formatTimeOnly(dateInput) {
  if (!dateInput) return ''
  return new Date(dateInput).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
}

// Group messages: show header if different user or >5 min gap, track day separators
const groupedMessages = computed(() => {
  const msgs = filteredMessages.value
  if (!msgs.length) return []

  const result = []
  let lastAuthorId = null
  let lastDate = null
  let lastDay = null
  let unreadShown = false
  const unreadFrom = messagesStore.getUnreadCount(canalId.value)

  msgs.forEach((msg, index) => {
    const msgDate = new Date(msg.dateEnvoi || msg.createdAt)
    const msgDay = new Date(msgDate.getFullYear(), msgDate.getMonth(), msgDate.getDate())

    const showDaySep = !lastDay || !isSameDay(msgDay, lastDay)
    const timeDiff = lastDate ? (msgDate - lastDate) / 60000 : Infinity
    const showHeader = showDaySep || msg.auteurId !== lastAuthorId || timeDiff > 5
    const showUnreadSep = !unreadShown && unreadFrom > 0 && index === msgs.length - unreadFrom

    if (showUnreadSep) unreadShown = true

    // Only show absence banner on the first message from this author in the group
    const senderId = msg.auteurId || msg.userId
    const absenceMsg = showHeader ? (memberAbsenceMap.value[senderId] || null) : null

    result.push({
      msg: absenceMsg ? { ...msg, msgAbsence: absenceMsg } : msg,
      showDaySep,
      showHeader,
      showUnreadSep,
      dayLabel: showDaySep ? formatDayHeader(msgDay) : ''
    })

    lastAuthorId = msg.auteurId
    lastDate = msgDate
    lastDay = msgDay
  })

  return result
})

let topObserver = null

function setupTopObserver() {
  if (topObserver) topObserver.disconnect()
  if (!topSentinel.value) return

  topObserver = new IntersectionObserver(async ([entry]) => {
    if (!entry.isIntersecting) return
    if (!messagesStore.hasMore[canalId.value] || messagesStore.loadingMore[canalId.value]) return

    // Préserver la position de scroll : on mémorise la hauteur AVANT le prepend
    const container = messagesContainer.value
    const prevScrollHeight = container?.scrollHeight ?? 0

    const loaded = await messagesStore.fetchOlderMessages(canalId.value)

    if (loaded && container) {
      await nextTick()
      // Ajuster scrollTop pour que la vue ne saute pas
      container.scrollTop += container.scrollHeight - prevScrollHeight
    }
  }, {
    root: messagesContainer.value,
    threshold: 0.1
  })

  topObserver.observe(topSentinel.value)
}

async function scrollToBottom(smooth = false) {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: smooth ? 'smooth' : 'auto'
    })
  }
}

async function scrollToMessage(messageId) {
  await nextTick()
  if (!messagesContainer.value || !messageId) return
  const target = messagesContainer.value.querySelector(`[data-message-id="${messageId}"]`)
  if (!target) return
  target.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

function autoResize() {
  if (textInput.value) {
    textInput.value.style.height = 'auto'
    textInput.value.style.height = Math.min(textInput.value.scrollHeight, 120) + 'px'
  }
}

function addNewLine() {
  messageText.value += '\n'
  nextTick(() => autoResize())
}

function handleFileSelect(event) {
  const file = event.target.files[0]
  if (file) {
    pendingFile.value = file
  }
}

function handleComposerInput() {
  autoResize()
  notifyTyping()
}

function removePendingFile() {
  pendingFile.value = null
  pendingFileUrl.value = null
  if (fileInput.value) fileInput.value.value = ''
}

function clearSearch() {
  searchQuery.value = ''
}

function toggleSearch() {
  showSearch.value = !showSearch.value
  if (!showSearch.value) {
    searchQuery.value = ''
  } else {
    nextTick(() => searchInputRef.value?.focus())
  }
}

function toggleReactionPicker(messageId, event) {
  if (reactionPickerMessageId.value === messageId) {
    reactionPickerMessageId.value = null
    reactionPickerAbove.value = false
    return
  }
  const btn = event?.currentTarget
  if (btn) {
    const rect = btn.getBoundingClientRect()
    reactionPickerAbove.value = rect.bottom > window.innerHeight - 170
  }
  reactionPickerMessageId.value = messageId
}

function getReactionTooltip(reaction) {
  if (!reaction?.userIds?.length) return reaction?.emoji || ''
  const names = reaction.userIds.map(uid => {
    if (uid === authStore.user?.id) return 'Vous'
    const member = members.value.find(m => (m.id || m.userId) === uid)
    return member ? `${member.prenom} ${member.nom}`.trim() : null
  }).filter(Boolean)
  return names.length ? names.join(', ') : `${reaction.count} réaction${reaction.count > 1 ? 's' : ''}`
}

function toggleMobileMenu(messageId) {
  mobileMenuMessageId.value = mobileMenuMessageId.value === messageId ? null : messageId
}

function startEditing(msg) {
  editingMessageId.value = msg.id
  editingText.value = msg.contenu || ''
  sendError.value = ''
}

function cancelEditing() {
  editingMessageId.value = null
  editingText.value = ''
}

function clearTypingUsers() {
  typingUsers.value = {}
  for (const timeout of typingTimers.values()) {
    clearTimeout(timeout)
  }
  typingTimers.clear()
}

function handleTypingEvent(payload = {}) {
  const userId = payload.userId
  if (!userId || userId === authStore.user?.id) {
    return
  }

  typingUsers.value = {
    ...typingUsers.value,
    [userId]: {
      userId,
      prenom: payload.prenom || '',
      nom: payload.nom || '',
      email: payload.email || ''
    }
  }

  if (typingTimers.has(userId)) {
    clearTimeout(typingTimers.get(userId))
  }

  const timeout = window.setTimeout(() => {
    const nextUsers = { ...typingUsers.value }
    delete nextUsers[userId]
    typingUsers.value = nextUsers
    typingTimers.delete(userId)
  }, 2500)

  typingTimers.set(userId, timeout)
}

function notifyTyping() {
  if (!canalId.value) return
  // Envoyer l'événement dès qu'on tape, même le 1er caractère
  const now = Date.now()
  if (now - lastTypingSentAt.value < 1500) return
  lastTypingSentAt.value = now
  sendTyping(canalId.value)
}

async function toggleReaction(msg, emoji) {
  reactionBusyMessageId.value = msg.id
  sendError.value = ''
  const result = await messagesStore.toggleReaction(msg.id, emoji)
  reactionBusyMessageId.value = null

  if (!result.success) {
    sendError.value = result.message
    return
  }

  messagesStore.replaceMessage(canalId.value, result.message)
  reactionPickerMessageId.value = null
}

async function saveEdit(msg) {
  if (!editingText.value.trim()) return
  editingBusy.value = true
  sendError.value = ''
  const result = await messagesStore.updateMessage(msg.id, editingText.value.trim())
  editingBusy.value = false

  if (!result.success) {
    sendError.value = result.message
    return
  }

  messagesStore.replaceMessage(canalId.value, result.message)
  cancelEditing()
}

async function doRemoveMessage(msg) {
  confirmDeleteMessageId.value = null
  sendError.value = ''
  const result = await messagesStore.deleteMessage(msg.id)
  if (!result.success) {
    sendError.value = result.message
    return
  }
  messagesStore.replaceMessage(canalId.value, result.message)
}

async function sendMessage() {
  if (!canSend.value) return
  sending.value = true
  sendError.value = ''
  uploadingFile.value = false
  uploadProgress.value = 0

  let fileUrl = null
  let fileName = null

  // Upload file first if present
  if (pendingFile.value) {
    uploadingFile.value = true
    const uploadResult = await messagesStore.uploadFile(pendingFile.value, (progress) => {
      uploadProgress.value = progress
    })
    uploadingFile.value = false
    if (!uploadResult.success) {
      sendError.value = uploadResult.message
      sending.value = false
      return
    }
    fileUrl = uploadResult.url
    fileName = pendingFile.value.name
  }

  const contenu = messageText.value.trim()
  const optimisticId = `temp-${Date.now()}`

  // Optimistically add message
  const optimisticMsg = {
    id: optimisticId,
    canalId: canalId.value,
    contenu,
    auteurId: authStore.user?.id,
    auteurNom: authStore.user?.nom,
    auteurPrenom: authStore.user?.prenom,
    auteurAvatarUrl: authStore.user?.avatarUrl || null,
    dateEnvoi: new Date().toISOString(),
    pieceJointeUrl: fileUrl,
    pieceJointeNom: fileName
  }
  messagesStore.addMessage(optimisticMsg)

  try {
    const result = await messagesStore.sendMessage(canalId.value, contenu, fileUrl, fileName)
    if (!result.success) {
      messagesStore.removeMessage(canalId.value, optimisticId)
      sendError.value = result.message
      return
    }

    messagesStore.addMessage(result.message)
  } catch (e) {
    messagesStore.removeMessage(canalId.value, optimisticId)
    sendError.value = 'Erreur lors de l\'envoi du message'
    return
  } finally {
    sending.value = false
  }

  messageText.value = ''
  removePendingFile()
  uploadProgress.value = 0
  if (textInput.value) {
    textInput.value.style.height = 'auto'
  }
  await scrollToBottom(true)
}

async function exportConversation(format) {
  showExport.value = false
  await messagesStore.exportConversation(canalId.value, format)
}

async function addMember() {
  if (!selectedUserId.value) return
  addingMember.value = true
  addMemberError.value = ''
  const result = await channelsStore.addMember(canalId.value, selectedUserId.value)
  addingMember.value = false
  if (result.success) {
    showAddMember.value = false
    selectedUserId.value = ''
    await loadMembers()
  } else {
    addMemberError.value = result.message
  }
}

async function loadMembers() {
  if (!canalId.value) {
    members.value = []
    memberCount.value = 0
    memberAbsenceMap.value = {}
    return
  }

  const data = await channelsStore.getChannelMembers(canalId.value)
  members.value = data
  memberCount.value = data.length
  // Build a map of userId → msgAbsence for the absence banner
  const map = {}
  for (const m of data) {
    const uid = m.id || m.userId
    if (uid && m.msgAbsence) map[uid] = m.msgAbsence
  }
  memberAbsenceMap.value = map
}

async function loadAllUsers() {
  if (!authStore.isAdmin || isDirectChannel.value) {
    allUsers.value = []
    return
  }

  try {
    const res = await api.get('/users')
    allUsers.value = (res.data || []).map(user => ({
      ...user,
      id: user.id ?? user.idUser
    }))
  } catch (e) {
    // Not admin, skip
  }
}

function handleOutsideClick(event) {
  if (exportRef.value && !exportRef.value.contains(event.target)) {
    showExport.value = false
  }
  if (!event.target.closest('.reaction-picker-wrapper')) {
    reactionPickerMessageId.value = null
    reactionPickerAbove.value = false
  }
  if (!event.target.closest('.mobile-msg-menu-wrapper')) {
    mobileMenuMessageId.value = null
  }
  if (!event.target.closest('.message-inline-actions')) {
    confirmDeleteMessageId.value = null
  }
}

function handleGlobalKeydown(event) {
  // Ctrl/Cmd+F → toggle search
  if ((event.ctrlKey || event.metaKey) && event.key === 'f' && !event.target.matches('input, textarea')) {
    event.preventDefault()
    toggleSearch()
  }
  // Escape → close search or mobile menus
  if (event.key === 'Escape') {
    if (showSearch.value) {
      showSearch.value = false
      searchQuery.value = ''
    }
    mobileMenuMessageId.value = null
    confirmDeleteMessageId.value = null
  }
}

function syncQueryState() {
  const search = typeof route.query.search === 'string' ? route.query.search : ''
  searchQuery.value = search

  const messageId = Number(route.query.message)
  highlightedMessageId.value = Number.isInteger(messageId) && messageId > 0 ? messageId : null
}

async function initChannel(id) {
  if (!id) return
  cancelEditing()
  clearTypingUsers()
  uploadProgress.value = 0
  uploadingFile.value = false
  reactionPickerMessageId.value = null
  confirmDeleteMessageId.value = null
  mobileMenuMessageId.value = null
  syncQueryState()
  channelsStore.setActiveChannel(id)
  const loadedMessages = await messagesStore.fetchMessages(id)
  const lastMessageId = loadedMessages.at(-1)?.id
  messagesStore.markAsRead(id, lastMessageId)
  await loadMembers()
  await loadAllUsers()
  if (highlightedMessageId.value) {
    await scrollToMessage(highlightedMessageId.value)
  } else {
    await scrollToBottom()
  }

  // Démarrer l'observer de scroll infini une fois que le DOM est prêt
  await nextTick()
  setupTopObserver()

  // Subscribe to WebSocket
  subscribe(id, (msg) => {
    const alreadyExists = messagesStore.getMessages(id).some(message => message.id === (msg.id ?? msg.idMessage))
    messagesStore.addMessage({ ...msg, canalId: id })
    if (!alreadyExists) {
      scrollToBottom(true)
    }
  })

  subscribeToTyping(id, handleTypingEvent)
}

watch(() => route.params.id, async (newId, oldId) => {
  if (newId) {
    const nextId = Number(newId)
    const previousId = Number(oldId)
    if (Number.isInteger(previousId) && previousId > 0 && previousId !== nextId) {
      unsubscribe(previousId)
      unsubscribeTyping(previousId)
    }
    await initChannel(Number.isInteger(nextId) && nextId > 0 ? nextId : null)
  }
}, { immediate: false })

watch(
  () => route.query,
  async () => {
    syncQueryState()
    if (highlightedMessageId.value && canalId.value) {
      await scrollToMessage(highlightedMessageId.value)
    }
  },
  { deep: true }
)

watch(
  () => channelsStore.channels,
  (channels) => {
    const firstConversation = channelsStore.sortedChannels[0] || channelsStore.sortedDirectChannels[0]
    if (!canalId.value && firstConversation) {
      router.replace(`/chat/canal/${firstConversation.id}`)
      return
    }

    if (canalId.value && channels.length > 0 && !channel.value && firstConversation) {
      router.replace(`/chat/canal/${firstConversation.id}`)
    }
  },
  { immediate: true, deep: true }
)

onMounted(async () => {
  await connect(authStore.token)
  await initChannel(canalId.value)
  document.addEventListener('click', handleOutsideClick)
  document.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  if (topObserver) topObserver.disconnect()
  unsubscribe(canalId.value)
  unsubscribeTyping(canalId.value)
  clearTypingUsers()
  channelsStore.setActiveChannel(null)
  document.removeEventListener('click', handleOutsideClick)
  document.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--content-bg);
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.channel-hash {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
  flex-shrink: 0;
}

.direct-header-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
}

.direct-header-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.channel-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.channel-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.member-count {
  font-size: 12px;
  color: var(--text-light);
}

.member-count-typing {
  color: var(--primary);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.header-btn {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
}

.header-btn:hover {
  background: var(--content-bg);
  color: var(--text);
}

.header-btn-active {
  background: var(--primary-light);
  color: var(--primary);
}

.export-wrapper {
  position: relative;
}

.export-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 4px);
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-md);
  min-width: 160px;
  z-index: 100;
  overflow: hidden;
}

.export-dropdown button {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  background: none;
  border: none;
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
  text-align: left;
  transition: var(--transition);
}

.export-dropdown button:hover {
  background: var(--content-bg);
  color: var(--primary);
}

.desktop-notif-chat :deep(.notif-btn) {
  color: var(--text-light);
}

.desktop-notif-chat :deep(.notif-btn:hover) {
  background: var(--content-bg);
}

/* Search slide transition */
.search-slide-enter-active,
.search-slide-leave-active {
  transition: max-height 0.2s ease, opacity 0.2s ease, padding 0.2s ease;
  overflow: hidden;
}

.search-slide-enter-from,
.search-slide-leave-to {
  max-height: 0 !important;
  opacity: 0;
  padding-top: 0 !important;
  padding-bottom: 0 !important;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px;
  background: var(--white);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
  max-height: 80px;
}

.chat-search {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  min-height: 40px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--content-bg);
  color: var(--text-light);
}

.chat-search:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.chat-search-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text);
  font-size: 13px;
}

.chat-search-input::placeholder {
  color: #a0aec0;
}

.chat-search-clear {
  border: none;
  background: none;
  color: var(--primary);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  padding: 4px 0;
}

.chat-search-clear:hover {
  color: var(--primary-hover);
}

.search-count {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-light);
}

.chat-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
}

.messages-loading,
.messages-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  color: var(--text-light);
}

.empty-channel-icon {
  font-size: 48px;
  font-weight: 800;
  color: var(--primary);
  opacity: 0.5;
  line-height: 1;
}

.messages-empty h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
}

/* Scroll infini */
.top-sentinel {
  height: 1px;
  width: 100%;
}

.messages-loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 0 4px;
  color: var(--text-light);
  font-size: 13px;
}

.spinner-sm {
  width: 16px;
  height: 16px;
  border-width: 2px;
}

.conversation-start {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 24px 0 8px;
  color: var(--text-light);
  font-size: 13px;
}

.conversation-start-icon {
  font-size: 32px;
  font-weight: 800;
  color: var(--primary);
  opacity: 0.35;
  line-height: 1;
}

.send-error {
  margin: 0 0 8px;
  color: #c53030;
  font-size: 13px;
  font-weight: 600;
}

.upload-status {
  margin: 0 0 8px;
  padding: 10px 12px;
  background: rgba(232, 80, 26, 0.06);
  border: 1px solid rgba(232, 80, 26, 0.12);
  border-radius: var(--radius);
}

.upload-status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  color: var(--text);
  font-size: 13px;
  font-weight: 600;
}

.upload-progress-track {
  width: 100%;
  height: 6px;
  background: rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

.upload-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), #f97316);
  border-radius: inherit;
  transition: width 0.2s ease;
}

.day-separator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  flex-shrink: 0;
}

.day-line {
  flex: 1;
  height: 1px;
  background: var(--border);
}

.day-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-light);
  white-space: nowrap;
  background: var(--content-bg);
  padding: 2px 10px;
  border-radius: 10px;
  border: 1px solid var(--border);
}

.unread-separator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 24px;
  flex-shrink: 0;
}

.unread-line {
  flex: 1;
  height: 1px;
  background: var(--danger);
  opacity: 0.5;
}

.unread-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--danger);
  white-space: nowrap;
  background: rgba(245, 101, 101, 0.08);
  padding: 2px 10px;
  border-radius: 10px;
}

.message {
  display: flex;
  gap: 12px;
  padding: 2px 24px;
  transition: background 0.1s;
}

.message:hover {
  background: rgba(0,0,0,0.02);
}

.message-highlighted {
  background: rgba(232, 80, 26, 0.08);
  box-shadow: inset 3px 0 0 var(--primary);
}

/* First message from a new author in a new group — extra top gap */
.message-group-start {
  padding-top: 10px;
}

/* Follow-up messages in the same group — tight */
.message-grouped {
  padding-top: 1px;
  padding-bottom: 1px;
}

.message-avatar-col {
  width: 36px;
  flex-shrink: 0;
  padding-top: 2px;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.message-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  display: block;
}

.message-avatar-placeholder {
  width: 36px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-time-hover {
  font-size: 10px;
  color: var(--text-light);
  opacity: 0;
  transition: opacity 0.1s;
}

.message:hover .message-time-hover {
  opacity: 1;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 3px;
}

.message-author {
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
}

.message-time {
  font-size: 11px;
  color: var(--text-light);
}

.message-meta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.message-edited-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(232, 80, 26, 0.12);
  color: var(--primary);
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.absence-banner {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(237, 137, 54, 0.1);
  border: 1px solid rgba(237, 137, 54, 0.3);
  color: #c05621;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  margin-bottom: 4px;
}

.message-text {
  font-size: 14px;
  color: var(--text);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-text-deleted {
  color: var(--text-light);
  font-style: italic;
}

.message-inline-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease;
}

.message:hover .message-inline-actions {
  opacity: 1;
  pointer-events: auto;
}

.message-action-btn {
  border: none;
  background: none;
  color: var(--text-light);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
}

.message-action-btn:hover {
  color: var(--primary);
}

.message-action-btn.danger:hover {
  color: var(--danger);
}

.confirm-delete-text {
  font-size: 12px;
  font-weight: 600;
  color: var(--danger);
}

/* Desktop actions shown inline; mobile ⋮ menu hidden on desktop */
.desktop-msg-actions {
  display: contents;
}

.mobile-msg-menu-wrapper {
  display: none;
  position: relative;
}

.mobile-menu-btn {
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
}

.mobile-msg-menu {
  position: absolute;
  bottom: calc(100% + 4px);
  right: 0;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-md);
  z-index: 50;
  min-width: 140px;
  overflow: hidden;
}

.mobile-msg-menu button {
  display: block;
  width: 100%;
  padding: 10px 14px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: var(--text);
  cursor: pointer;
  transition: var(--transition);
}

.mobile-msg-menu button:hover {
  background: var(--content-bg);
}

.mobile-msg-menu button.danger {
  color: var(--danger);
}

.message-edit-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 6px;
  padding: 12px;
  background: var(--content-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}

.message-edit-input {
  width: 100%;
  resize: vertical;
  min-height: 82px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--text);
  background: var(--white);
  outline: none;
  font-family: var(--font);
}

.message-edit-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.message-edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.message-attachment {
  margin-top: 6px;
}

.message-reactions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.reaction-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  padding: 0 10px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border);
  background: var(--white);
  color: var(--text);
  font-size: 13px;
  transition: var(--transition);
}

.reaction-chip:hover {
  border-color: var(--primary);
  background: var(--primary-light);
}

.reaction-chip-active {
  border-color: var(--primary);
  background: rgba(232, 80, 26, 0.12);
  color: var(--primary);
}

.reaction-chip:disabled,
.reaction-add-btn:disabled,
.reaction-picker-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.reaction-chip-emoji {
  line-height: 1;
}

.reaction-chip-count {
  font-size: 12px;
  font-weight: 700;
}

.reaction-picker-wrapper {
  position: relative;
}

.reaction-add-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px dashed var(--border);
  background: var(--content-bg);
  color: var(--text-light);
  font-size: 16px;
  font-weight: 700;
  transition: var(--transition);
}

.reaction-add-btn:hover,
.reaction-add-btn-active {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.reaction-picker {
  position: absolute;
  left: 0;
  top: calc(100% + 8px);
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px;
  border-radius: var(--radius);
  background: var(--white);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
  z-index: 30;
}

.reaction-picker.reaction-picker-above {
  top: auto;
  bottom: calc(100% + 8px);
}

.reaction-picker-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: transparent;
  font-size: 18px;
  transition: var(--transition);
}

.reaction-picker-btn:hover {
  background: var(--content-bg);
  transform: translateY(-1px);
}

.attachment-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--content-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--primary);
  text-decoration: none;
  font-size: 13px;
  transition: var(--transition);
}

.attachment-link:hover {
  background: var(--primary-light);
  border-color: var(--primary);
}

.attachment-icon {
  color: var(--primary);
}

/* Members Panel */
.members-panel {
  width: 260px;
  min-width: 260px;
  background: var(--white);
  border-left: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.members-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--border);
}

.members-header h3 {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
}

.close-panel-btn {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  border-radius: 4px;
}

.close-panel-btn:hover {
  background: var(--content-bg);
}

.members-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
}

.member-avatar {
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

.member-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  display: block;
}

.member-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.member-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.member-role {
  font-size: 11px;
  color: var(--text-light);
}

.members-footer {
  padding: 12px;
  border-top: 1px solid var(--border);
}

/* Message Input */
.message-input-area {
  padding: 12px 16px 14px;
  background: var(--white);
  border-top: 1px solid var(--border);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.input-container {
  background: var(--content-bg);
  border: 1.5px solid var(--border);
  border-radius: var(--radius-lg);
  transition: var(--transition);
  overflow: hidden;
}

.input-container:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px 0;
  font-size: 13px;
  color: var(--primary);
}

.remove-file-btn {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  font-size: 14px;
  padding: 2px;
  line-height: 1;
}

.remove-file-btn:hover {
  color: var(--danger);
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 8px 12px;
}

.attach-btn {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
  flex-shrink: 0;
  margin-bottom: 2px;
}

.attach-btn:hover {
  background: var(--border);
  color: var(--primary);
}

.message-textarea {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  resize: none;
  font-size: 14px;
  color: var(--text);
  line-height: 1.6;
  padding: 4px 0;
  max-height: 120px;
  overflow-y: auto;
  font-family: var(--font);
}

.message-textarea::placeholder {
  color: #a0aec0;
}

.send-btn {
  background: var(--border);
  border: none;
  color: var(--text-light);
  width: 34px;
  height: 34px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition);
  flex-shrink: 0;
}

.send-btn.send-active {
  background: var(--primary);
  color: white;
}

.send-btn.send-active:hover {
  background: var(--primary-hover);
}

.send-btn:disabled {
  cursor: not-allowed;
}

/* Slide transition for members panel */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.25s ease, opacity 0.25s ease;
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

@media (max-width: 768px) {
  .chat-header {
    padding: 0 12px;
  }

  .channel-hash {
    font-size: 18px;
  }

  .channel-name {
    font-size: 15px;
  }

  .chat-toolbar {
    padding: 8px 12px;
  }

  .message {
    padding: 3px 12px;
  }

  .day-separator,
  .unread-separator {
    padding: 8px 12px;
  }

  .message-input-area {
    padding: 8px 12px 10px;
  }

  .message-inline-actions {
    opacity: 1;
    pointer-events: auto;
  }

  /* On mobile: hide text action buttons, show ⋮ menu instead */
  .desktop-msg-actions {
    display: none;
  }

  .mobile-msg-menu-wrapper {
    display: block;
  }

  .reaction-picker {
    left: auto;
    right: 0;
  }

  .members-panel {
    position: fixed;
    right: 0;
    top: 0;
    height: 100%;
    width: min(260px, 82vw);
    min-width: unset;
    z-index: 300;
    box-shadow: var(--shadow-lg);
  }

  .desktop-notif-chat {
    display: none;
  }

  /* Tap targets minimum 44px */
  .attach-btn,
  .send-btn {
    min-width: 44px;
    min-height: 44px;
  }

  .message-action-btn,
  .mobile-menu-btn {
    min-height: 36px;
    padding: 6px 10px;
  }
}
</style>
