import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMessagesStore } from './messages.js'

// Mock axios pour éviter les appels réseau réels
vi.mock('../api/axios.js', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

// Mock du store channels (appel dans addMessage)
vi.mock('./channels.js', () => ({
  useChannelsStore: () => ({
    touchChannelActivity: vi.fn()
  })
}))

beforeEach(() => {
  setActivePinia(createPinia())
})

describe('useMessagesStore — addMessage', () => {
  it('ajoute un message à la liste du canal', () => {
    const store = useMessagesStore()
    store.addMessage({ id: 1, canalId: 10, contenu: 'Bonjour', auteurId: 1, dateEnvoi: new Date().toISOString() })
    expect(store.messages[10]).toHaveLength(1)
    expect(store.messages[10][0].contenu).toBe('Bonjour')
  })

  it('ne duplique pas un message avec le même id', () => {
    const store = useMessagesStore()
    const msg = { id: 1, canalId: 10, contenu: 'Hello', auteurId: 1, dateEnvoi: new Date().toISOString() }
    store.addMessage(msg)
    store.addMessage(msg)
    expect(store.messages[10]).toHaveLength(1)
  })

  it('remplace le message existant si le même id est envoyé avec un contenu modifié', () => {
    const store = useMessagesStore()
    store.addMessage({ id: 1, canalId: 10, contenu: 'Original', auteurId: 1, dateEnvoi: new Date().toISOString() })
    store.addMessage({ id: 1, canalId: 10, contenu: 'Modifié', auteurId: 1, dateEnvoi: new Date().toISOString() })
    expect(store.messages[10]).toHaveLength(1)
    expect(store.messages[10][0].contenu).toBe('Modifié')
  })

  it('remplace un message temporaire par le message réel (déduplication temp-*)', () => {
    const store = useMessagesStore()
    const tempMsg = {
      id: 'temp-123',
      canalId: 10,
      contenu: 'Envoi en cours',
      auteurId: 5,
      dateEnvoi: new Date().toISOString()
    }
    store.addMessage(tempMsg)
    expect(store.messages[10]).toHaveLength(1)

    // Le serveur confirme avec un vrai ID
    store.addMessage({ id: 99, canalId: 10, contenu: 'Envoi en cours', auteurId: 5, dateEnvoi: new Date().toISOString() })
    expect(store.messages[10]).toHaveLength(1)
    expect(store.messages[10][0].id).toBe(99)
  })

  it('ignore les messages sans canalId', () => {
    const store = useMessagesStore()
    store.addMessage({ id: 1, contenu: 'Sans canal', auteurId: 1 })
    expect(Object.keys(store.messages)).toHaveLength(0)
  })

  it('gère plusieurs canaux indépendamment', () => {
    const store = useMessagesStore()
    store.addMessage({ id: 1, canalId: 10, contenu: 'Canal 10', auteurId: 1, dateEnvoi: new Date().toISOString() })
    store.addMessage({ id: 2, canalId: 20, contenu: 'Canal 20', auteurId: 1, dateEnvoi: new Date().toISOString() })
    expect(store.messages[10]).toHaveLength(1)
    expect(store.messages[20]).toHaveLength(1)
  })
})

describe('useMessagesStore — removeMessage', () => {
  it('supprime un message par id', () => {
    const store = useMessagesStore()
    store.addMessage({ id: 5, canalId: 10, contenu: 'A supprimer', auteurId: 1, dateEnvoi: new Date().toISOString() })
    store.removeMessage(10, 5)
    expect(store.messages[10]).toHaveLength(0)
  })

  it('ne plante pas si le canal n\'existe pas', () => {
    const store = useMessagesStore()
    expect(() => store.removeMessage(999, 1)).not.toThrow()
  })
})

describe('useMessagesStore — unread', () => {
  it('incrementUnread incrémente le compteur du canal', () => {
    const store = useMessagesStore()
    store.incrementUnread(10)
    store.incrementUnread(10)
    expect(store.getUnreadCount(10)).toBe(2)
  })

  it('markAsRead remet le compteur à zéro', () => {
    const store = useMessagesStore()
    store.incrementUnread(10)
    store.incrementUnread(10)
    store.markAsRead(10)
    expect(store.getUnreadCount(10)).toBe(0)
  })

  it('totalUnread somme tous les canaux', () => {
    const store = useMessagesStore()
    store.incrementUnread(10)
    store.incrementUnread(20)
    store.incrementUnread(20)
    expect(store.totalUnread).toBe(3)
  })
})

describe('useMessagesStore — clearChannel', () => {
  it('supprime tous les messages et métadonnées du canal', () => {
    const store = useMessagesStore()
    store.addMessage({ id: 1, canalId: 10, contenu: 'test', auteurId: 1, dateEnvoi: new Date().toISOString() })
    store.incrementUnread(10)
    store.clearChannel(10)
    expect(store.messages[10]).toBeUndefined()
    expect(store.unreadCounts[10]).toBeUndefined()
    expect(store.hasMore[10]).toBeUndefined()
  })
})
