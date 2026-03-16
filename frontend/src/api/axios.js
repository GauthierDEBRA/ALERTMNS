import axios from 'axios'
import router from '../router/index.js'
import { getToken, setToken, clearToken } from '../utils/tokenStore.js'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

let refreshPromise = null

function storeSessionFromResponse(data) {
  if (!data?.token) return null

  setToken(data.token)
  localStorage.setItem('user', JSON.stringify({
    id: data.id ?? data.userId ?? data.idUser ?? null,
    nom: data.nom ?? '',
    prenom: data.prenom ?? '',
    email: data.email ?? '',
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
  }))

  return data.token
}

function clearSessionAndRedirect() {
  if (router.currentRoute.value.path !== '/login') {
    sessionStorage.setItem('authMessage', 'Votre session a expiré. Merci de vous reconnecter.')
  }
  clearToken()
  localStorage.removeItem('user')
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

async function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = axios.post('/api/auth/refresh', {}, {
      withCredentials: true,
      headers: { 'Content-Type': 'application/json' }
    })
      .then((response) => storeSessionFromResponse(response.data))
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

// Request interceptor - attach Bearer token
api.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor - handle 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config || {}
    const requestUrl = String(originalRequest.url || '')
    const status = error.response?.status

    if (status === 401) {
      const isAuthEndpoint = requestUrl.includes('/auth/login')
        || requestUrl.includes('/auth/refresh')
        || requestUrl.includes('/auth/logout')

      if (!isAuthEndpoint && !originalRequest._retry) {
        originalRequest._retry = true
        try {
          const refreshedToken = await refreshAccessToken()
          if (refreshedToken) {
            originalRequest.headers = originalRequest.headers || {}
            originalRequest.headers.Authorization = `Bearer ${refreshedToken}`
            return api(originalRequest)
          }
        } catch (refreshError) {
          clearSessionAndRedirect()
          return Promise.reject(refreshError)
        }
      }

      clearSessionAndRedirect()
    } else if (status === 403) {
      // Rôle révoqué ou session invalide côté serveur
      const isAuthEndpoint = requestUrl.includes('/auth/')
      if (!isAuthEndpoint) {
        clearSessionAndRedirect()
      }
    }
    return Promise.reject(error)
  }
)

export default api
