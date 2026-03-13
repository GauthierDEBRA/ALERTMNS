<template>
  <div class="login-page">
    <!-- Background decoration -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <div class="login-container">
      <!-- Logo -->
      <div class="login-logo">
        <div class="logo-icon">A</div>
        <h1 class="app-name">ALERTMNS</h1>
        <p class="app-tagline">Messagerie interne d'entreprise</p>
      </div>

      <!-- Card -->
      <div class="login-card">
        <div class="card-header">
          <h2>Connexion</h2>
          <p>Entrez vos identifiants pour accéder à votre espace</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div v-if="error" class="alert alert-error">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {{ error }}
          </div>

          <div class="form-group">
            <label class="form-label" for="email">Adresse email</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
              <input
                id="email"
                v-model="email"
                type="email"
                class="form-input with-icon"
                placeholder="votre@email.com"
                autocomplete="email"
                required
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label" for="password">Mot de passe</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                class="form-input with-icon with-toggle"
                placeholder="Votre mot de passe"
                autocomplete="current-password"
                required
              />
              <button
                type="button"
                class="password-toggle"
                @click="showPassword = !showPassword"
              >
                <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
          </div>

          <button
            type="submit"
            class="login-btn"
            :disabled="loading || !email || !password"
          >
            <span v-if="loading">
              <div class="spinner" style="width:16px;height:16px;border-width:2px;border-top-color:white;"></div>
            </span>
            <span v-else>Se connecter</span>
          </button>
        </form>
      </div>

      <!-- Footer -->
      <p class="login-footer">© {{ new Date().getFullYear() }} ALERTMNS — Messagerie sécurisée</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

async function handleLogin() {
  if (!email.value || !password.value) return
  loading.value = true
  error.value = ''

  const result = await authStore.login(email.value, password.value)
  loading.value = false

  if (result.success) {
    router.push('/chat')
  } else {
    error.value = result.message
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1d23 0%, #2d3340 50%, #1e2128 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.bg-circle-1 {
  width: 500px;
  height: 500px;
  background: var(--primary);
  top: -150px;
  right: -100px;
}

.bg-circle-2 {
  width: 300px;
  height: 300px;
  background: #4299e1;
  bottom: -100px;
  left: -80px;
}

.bg-circle-3 {
  width: 200px;
  height: 200px;
  background: var(--primary);
  bottom: 100px;
  right: 100px;
}

.login-container {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.login-logo {
  text-align: center;
}

.logo-icon {
  width: 72px;
  height: 72px;
  background: var(--primary);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 800;
  color: white;
  margin: 0 auto 16px;
  box-shadow: 0 8px 32px rgba(232, 80, 26, 0.4);
}

.app-name {
  font-size: 28px;
  font-weight: 800;
  color: white;
  letter-spacing: 2px;
  margin-bottom: 6px;
}

.app-tagline {
  font-size: 14px;
  color: rgba(255,255,255,0.5);
}

.login-card {
  width: 100%;
  background: rgba(255,255,255,0.98);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  overflow: hidden;
}

.card-header {
  padding: 28px 32px 0;
  margin-bottom: 24px;
}

.card-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 6px;
}

.card-header p {
  font-size: 13px;
  color: var(--text-light);
}

.login-form {
  padding: 0 32px 28px;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #a0aec0;
  pointer-events: none;
}

.form-input.with-icon {
  padding-left: 42px;
}

.form-input.with-toggle {
  padding-right: 44px;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  border-radius: 4px;
}

.password-toggle:hover {
  color: var(--text);
  background: var(--content-bg);
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: var(--radius);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  box-shadow: 0 4px 12px rgba(232, 80, 26, 0.3);
}

.login-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  box-shadow: 0 6px 16px rgba(232, 80, 26, 0.4);
  transform: translateY(-1px);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.login-footer {
  font-size: 12px;
  color: rgba(255,255,255,0.3);
  text-align: center;
}

.alert {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
