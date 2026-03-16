import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient = null
let connectionPromise = null
const subscriptions = new Map()
const connectionState = ref('disconnected') // 'disconnected' | 'connecting' | 'connected'

function buildWebSocketUrl() {
  // Le token NE DOIT PAS apparaître en query string (logs serveur, historique
  // navigateur, Referer). Il est transmis via les connectHeaders STOMP CONNECT.
  const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
  return `${protocol}//${window.location.host}/ws`
}

function getOrCreateClient(token) {
  if (stompClient && stompClient.connected) {
    return Promise.resolve(stompClient)
  }

  if (connectionPromise) {
    return connectionPromise
  }

  connectionPromise = new Promise((resolve, reject) => {
    connectionState.value = 'connecting'

    const client = new Client({
      webSocketFactory: () => new SockJS(buildWebSocketUrl()),
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      debug: () => {},
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient = client
        connectionState.value = 'connected'
        connectionPromise = null
        resolve(client)
      },
      onDisconnect: () => {
        connectionState.value = 'disconnected'
        connectionPromise = null
        // Re-subscribe after reconnect is handled automatically by stompjs
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
        connectionState.value = 'disconnected'
        connectionPromise = null
        reject(new Error('STOMP error'))
      },
      onWebSocketError: (event) => {
        console.error('WebSocket transport error:', event)
        connectionState.value = 'disconnected'
        connectionPromise = null
        reject(new Error('WebSocket transport error'))
      },
      onWebSocketClose: (event) => {
        connectionState.value = 'disconnected'
        connectionPromise = null
        if (!client.connected) {
          reject(new Error(`WebSocket closed before STOMP connect (${event.code})`))
        }
      }
    })

    client.activate()
  })

  return connectionPromise
}

export function useWebSocket() {
  const localSubscriptions = new Set()

  async function connect(token) {
    try {
      await getOrCreateClient(token)
    } catch (err) {
      console.error('WebSocket connection error:', err)
      throw err
    }
  }

  async function subscribe(canalId, callback) {
    const destination = `/topic/canal/${canalId}`
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).callbacks.add(callback)
      return
    }

    try {
      const client = await getOrCreateClient(localStorage.getItem('token'))
      const sub = client.subscribe(destination, (frame) => {
        try {
          const msg = JSON.parse(frame.body)
          subscriptions.get(destination)?.callbacks.forEach(cb => cb(msg))
        } catch (e) {
          console.error('Error parsing WebSocket message:', e)
        }
      })

      subscriptions.set(destination, {
        subscription: sub,
        callbacks: new Set([callback])
      })
      localSubscriptions.add(destination)
    } catch (err) {
      console.error('Error subscribing to channel:', err)
    }
  }

  async function subscribeToTyping(canalId, callback) {
    const destination = `/topic/canal/${canalId}/typing`
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).callbacks.add(callback)
      return
    }

    try {
      const client = await getOrCreateClient(localStorage.getItem('token'))
      const sub = client.subscribe(destination, (frame) => {
        try {
          const payload = JSON.parse(frame.body)
          subscriptions.get(destination)?.callbacks.forEach(cb => cb(payload))
        } catch (e) {
          console.error('Error parsing typing payload:', e)
        }
      })

      subscriptions.set(destination, {
        subscription: sub,
        callbacks: new Set([callback])
      })
      localSubscriptions.add(destination)
    } catch (err) {
      console.error('Error subscribing to typing events:', err)
    }
  }

  function unsubscribe(canalId) {
    const destination = `/topic/canal/${canalId}`
    const entry = subscriptions.get(destination)
    if (entry) {
      entry.subscription.unsubscribe()
      subscriptions.delete(destination)
      localSubscriptions.delete(destination)
    }
  }

  function unsubscribeTyping(canalId) {
    const destination = `/topic/canal/${canalId}/typing`
    const entry = subscriptions.get(destination)
    if (entry) {
      entry.subscription.unsubscribe()
      subscriptions.delete(destination)
      localSubscriptions.delete(destination)
    }
  }

  async function publish(destination, body) {
    try {
      const client = await getOrCreateClient(localStorage.getItem('token'))
      client.publish({
        destination,
        body: JSON.stringify(body),
        headers: { 'content-type': 'application/json' }
      })
    } catch (err) {
      console.error('Error publishing message:', err)
    }
  }

  async function sendMessage(canalId, contenu, auteurId, auteurNom, auteurPrenom, pieceJointeUrl = null, pieceJointeNom = null) {
    const payload = {
      canalId,
      contenu,
      auteurId,
      auteurNom,
      auteurPrenom,
      ...(pieceJointeUrl && { pieceJointeUrl }),
      ...(pieceJointeNom && { pieceJointeNom })
    }
    await publish(`/app/chat.sendMessage/${canalId}`, payload)
  }

  async function sendTyping(canalId) {
    await publish(`/app/chat.typing/${canalId}`, {})
  }

  async function subscribeToNotifications(_userId, callback) {
    const destination = '/user/queue/notifications'
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).callbacks.add(callback)
      return
    }
    try {
      const client = await getOrCreateClient(localStorage.getItem('token'))
      const sub = client.subscribe(destination, (frame) => {
        try {
          const notif = JSON.parse(frame.body)
          subscriptions.get(destination)?.callbacks.forEach(cb => cb(notif))
        } catch (e) {
          console.error('Error parsing notification:', e)
        }
      })
      subscriptions.set(destination, {
        subscription: sub,
        callbacks: new Set([callback])
      })
      localSubscriptions.add(destination)
    } catch (err) {
      console.error('Error subscribing to notifications:', err)
    }
  }

  async function subscribeToPresence(callback) {
    const destination = '/topic/presence'
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).callbacks.add(callback)
      return
    }
    try {
      const client = await getOrCreateClient(localStorage.getItem('token'))
      const sub = client.subscribe(destination, (frame) => {
        try {
          const data = JSON.parse(frame.body)
          subscriptions.get(destination)?.callbacks.forEach(cb => cb(data))
        } catch (e) {
          console.error('Error parsing presence update:', e)
        }
      })
      subscriptions.set(destination, {
        subscription: sub,
        callbacks: new Set([callback])
      })
      localSubscriptions.add(destination)
    } catch (err) {
      console.error('Error subscribing to presence:', err)
    }
  }

  function disconnect() {
    localSubscriptions.forEach(destination => {
      const entry = subscriptions.get(destination)
      if (entry) {
        entry.subscription.unsubscribe()
        subscriptions.delete(destination)
      }
    })
    localSubscriptions.clear()
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    connect,
    subscribe,
    unsubscribe,
    subscribeToTyping,
    unsubscribeTyping,
    subscribeToNotifications,
    subscribeToPresence,
    publish,
    sendMessage,
    sendTyping,
    disconnect,
    connectionState
  }
}

export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
    connectionPromise = null
    subscriptions.clear()
    connectionState.value = 'disconnected'
  }
}
