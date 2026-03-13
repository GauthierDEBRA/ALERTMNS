<template>
  <div class="chat-view">
    <!-- Chat header -->
    <div class="chat-header">
      <div class="header-left">
        <span class="channel-hash">#</span>
        <div class="channel-info">
          <h2 class="channel-name">{{ channel?.nom || 'Canal' }}</h2>
          <span class="member-count">{{ memberCount }} membre{{ memberCount !== 1 ? 's' : '' }}</span>
        </div>
      </div>

      <div class="header-right">
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
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              Exporter JSON
            </button>
            <button @click="exportConversation('csv')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              Exporter CSV
            </button>
            <button @click="exportConversation('xml')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              Exporter XML
            </button>
          </div>
        </div>

        <div class="desktop-notif-chat">
          <NotificationPanel />
        </div>
      </div>
    </div>

    <div class="chat-body">
      <!-- Messages area -->
      <div class="messages-area" ref="messagesContainer">
        <div v-if="messagesStore.loading" class="messages-loading">
          <div class="spinner"></div>
          <p>Chargement des messages...</p>
        </div>

        <div v-else-if="messages.length === 0" class="messages-empty">
          <div class="empty-channel-icon">#</div>
          <h3>Bienvenue dans #{{ channel?.nom }}</h3>
          <p>C'est le début de ce canal. Envoyez le premier message !</p>
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
                'message-grouped': !group.showHeader
              }"
            >
              <!-- Avatar / spacer -->
              <div class="message-avatar-col">
                <div
                  v-if="group.showHeader"
                  class="message-avatar"
                  :style="{ background: getAvatarColor(group.msg) }"
                >
                  {{ getMsgInitials(group.msg) }}
                </div>
                <div v-else class="message-avatar-placeholder">
                  <span class="message-time-hover">{{ formatTimeOnly(group.msg.dateEnvoi || group.msg.createdAt) }}</span>
                </div>
              </div>

              <!-- Content -->
              <div class="message-content">
                <div v-if="group.showHeader" class="message-header">
                  <span class="message-author">{{ getMsgFullName(group.msg) }}</span>
                  <span class="message-time">{{ formatDate(group.msg.dateEnvoi || group.msg.createdAt) }}</span>
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

                <!-- Text content -->
                <div v-if="group.msg.contenu" class="message-text">{{ group.msg.contenu }}</div>

                <!-- File attachment -->
                <div v-if="group.msg.pieceJointeUrl" class="message-attachment">
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
              <div class="member-avatar" :style="{ background: getAvatarColorById(member) }">
                {{ `${member.prenom?.[0] || ''}${member.nom?.[0] || ''}`.toUpperCase() }}
              </div>
              <div class="member-info">
                <span class="member-name">{{ member.prenom }} {{ member.nom }}</span>
                <span class="member-role">{{ member.role }}</span>
              </div>
            </div>
          </div>
          <div class="members-footer">
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
            :placeholder="`Envoyer un message dans #${channel?.nom || 'canal'}`"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.exact="addNewLine"
            @input="autoResize"
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
import { useRoute } from 'vue-router'
import { useChannelsStore } from '../stores/channels.js'
import { useMessagesStore } from '../stores/messages.js'
import { useAuthStore } from '../stores/auth.js'
import { useWebSocket } from '../composables/useWebSocket.js'
import { useDate } from '../composables/useDate.js'
import NotificationPanel from '../components/NotificationPanel.vue'
import api from '../api/axios.js'

const route = useRoute()
const channelsStore = useChannelsStore()
const messagesStore = useMessagesStore()
const authStore = useAuthStore()
const { subscribe, unsubscribe, sendMessage: wsSendMessage, connect } = useWebSocket()
const { formatDate, isSameDay, formatDayHeader } = useDate()

const messagesContainer = ref(null)
const textInput = ref(null)
const fileInput = ref(null)
const exportRef = ref(null)

const messageText = ref('')
const pendingFile = ref(null)
const pendingFileUrl = ref(null)
const sending = ref(false)
const showMembers = ref(false)
const showExport = ref(false)
const showAddMember = ref(false)
const members = ref([])
const memberCount = ref(0)
const memberAbsenceMap = ref({})
const allUsers = ref([])
const selectedUserId = ref('')
const addingMember = ref(false)
const addMemberError = ref('')

const canalId = computed(() => Number(route.params.id))
const channel = computed(() => channelsStore.channels.find(c => c.id === canalId.value))
const messages = computed(() => messagesStore.getMessages(canalId.value))
const canSend = computed(() => (messageText.value.trim() || pendingFile.value) && !sending.value)

const availableUsers = computed(() => {
  const memberIds = new Set(members.value.map(m => m.id))
  return allUsers.value.filter(u => !memberIds.has(u.id))
})

const AVATAR_COLORS = [
  '#E8501A', '#4299e1', '#48bb78', '#ed8936', '#9f7aea',
  '#ed64a6', '#38b2ac', '#667eea', '#fc8181', '#68d391'
]

function getAvatarColor(msg) {
  const str = `${msg.auteurPrenom || ''}${msg.auteurNom || ''}${msg.auteurId || ''}`
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length]
}

function getAvatarColorById(user) {
  const str = `${user.prenom || ''}${user.nom || ''}`
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length]
}

function getMsgInitials(msg) {
  return `${msg.auteurPrenom?.[0] || ''}${msg.auteurNom?.[0] || ''}`.toUpperCase() || '?'
}

function getMsgFullName(msg) {
  return `${msg.auteurPrenom || ''} ${msg.auteurNom || ''}`.trim() || 'Utilisateur'
}

function isOwnMessage(msg) {
  return msg.auteurId === authStore.user?.id
}

function formatTimeOnly(dateInput) {
  if (!dateInput) return ''
  return new Date(dateInput).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
}

// Group messages: show header if different user or >5 min gap, track day separators
const groupedMessages = computed(() => {
  const msgs = messages.value
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

async function scrollToBottom(smooth = false) {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: smooth ? 'smooth' : 'auto'
    })
  }
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

function removePendingFile() {
  pendingFile.value = null
  pendingFileUrl.value = null
  if (fileInput.value) fileInput.value.value = ''
}

async function sendMessage() {
  if (!canSend.value) return
  sending.value = true

  let fileUrl = null
  let fileName = null

  // Upload file first if present
  if (pendingFile.value) {
    const uploadResult = await messagesStore.uploadFile(pendingFile.value)
    if (uploadResult.success) {
      fileUrl = uploadResult.url
      fileName = pendingFile.value.name
    }
  }

  const contenu = messageText.value.trim()

  // Optimistically add message
  const optimisticMsg = {
    id: `temp-${Date.now()}`,
    canalId: canalId.value,
    contenu,
    auteurId: authStore.user?.id,
    auteurNom: authStore.user?.nom,
    auteurPrenom: authStore.user?.prenom,
    dateEnvoi: new Date().toISOString(),
    pieceJointeUrl: fileUrl,
    pieceJointeNom: fileName
  }
  messagesStore.addMessage(optimisticMsg)

  // Send via WebSocket
  try {
    await wsSendMessage(
      canalId.value,
      contenu,
      authStore.user?.id,
      authStore.user?.nom,
      authStore.user?.prenom,
      fileUrl,
      fileName
    )
  } catch (e) {
    // Fall back to HTTP
    await messagesStore.sendMessage(canalId.value, contenu, fileUrl, fileName)
  }

  messageText.value = ''
  removePendingFile()
  if (textInput.value) {
    textInput.value.style.height = 'auto'
  }
  sending.value = false
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
  try {
    const res = await api.get('/users')
    allUsers.value = res.data || []
  } catch (e) {
    // Not admin, skip
  }
}

function handleOutsideClick(event) {
  if (exportRef.value && !exportRef.value.contains(event.target)) {
    showExport.value = false
  }
}

async function initChannel(id) {
  channelsStore.setActiveChannel(id)
  await messagesStore.fetchMessages(id)
  messagesStore.markAsRead(id)
  await loadMembers()
  await scrollToBottom()

  // Subscribe to WebSocket
  subscribe(id, (msg) => {
    messagesStore.addMessage({ ...msg, canalId: id })
    scrollToBottom(true)
  })
}

watch(() => route.params.id, async (newId) => {
  if (newId) {
    const oldId = canalId.value
    if (oldId && oldId !== Number(newId)) {
      unsubscribe(oldId)
    }
    await initChannel(Number(newId))
  }
}, { immediate: false })

onMounted(async () => {
  await connect(authStore.token)
  await initChannel(canalId.value)
  await loadAllUsers()
  document.addEventListener('click', handleOutsideClick)
})

onUnmounted(() => {
  unsubscribe(canalId.value)
  channelsStore.setActiveChannel(null)
  document.removeEventListener('click', handleOutsideClick)
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
  padding: 3px 24px;
  transition: background 0.1s;
}

.message:hover {
  background: rgba(0,0,0,0.02);
}

.message-grouped {
  padding-top: 2px;
  padding-bottom: 2px;
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

.message-attachment {
  margin-top: 6px;
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

  .members-panel {
    position: fixed;
    right: 0;
    top: 0;
    height: 100%;
    z-index: 300;
    box-shadow: var(--shadow-lg);
  }

  .desktop-notif-chat {
    display: none;
  }
}
</style>
