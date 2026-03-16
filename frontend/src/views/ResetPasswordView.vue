<template>
  <div class="login-page">
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <div class="login-container">
      <div class="login-logo">
        <div class="logo-icon">A</div>
        <h1 class="app-name">ALERTMNS</h1>
        <p class="app-tagline">Messagerie interne d'entreprise</p>
      </div>

      <div class="login-card">
        <div class="card-header">
          <h2>Nouveau mot de passe</h2>
          <p>Choisissez un nouveau mot de passe pour votre compte</p>
        </div>

        <div v-if="!token" class="alert alert-error">
          Lien invalide. Veuillez refaire une demande de réinitialisation.
        </div>

        <div v-else-if="success" class="alert alert-success">
          Mot de passe modifié avec succès.
          <router-link to="/login" style="color:inherit;text-decoration:underline;">Se connecter</router-link>
        </div>

        <form v-else @submit.prevent="handleSubmit" class="login-form">
          <div v-if="error" class="alert alert-error">{{ error }}</div>

          <div class="form-group">
            <label class="form-label" for="password">Nouveau mot de passe</label>
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
                placeholder="Minimum 8 caractères"
                autocomplete="new-password"
                required
                minlength="8"
              />
              <button type="button" class="password-toggle" @click="showPassword = !showPassword">
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

          <div class="form-group">
            <label class="form-label" for="confirm">Confirmer le mot de passe</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input
                id="confirm"
                v-model="confirm"
                type="password"
                class="form-input with-icon"
                placeholder="Répétez le mot de passe"
                autocomplete="new-password"
                required
              />
            </div>
          </div>

          <button type="submit" class="login-btn" :disabled="loading || !password || !confirm">
            <span v-if="loading"><div class="spinner" style="width:16px;height:16px;border-width:2px;border-top-color:white;"></div></span>
            <span v-else>Enregistrer</span>
          </button>
        </form>

        <div style="text-align:center;margin-top:16px;">
          <router-link to="/login" style="color:var(--color-primary);font-size:0.9rem;">
            Retour à la connexion
          </router-link>
        </div>
      </div>

      <p class="login-footer">© {{ new Date().getFullYear() }} ALERTMNS — Messagerie sécurisée</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api/axios.js'

const route = useRoute()
const token = ref('')
const password = ref('')
const confirm = ref('')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')
const success = ref(false)

onMounted(() => {
  token.value = route.query.token || ''
})

async function handleSubmit() {
  error.value = ''
  if (password.value.length < 8) {
    error.value = 'Le mot de passe doit contenir au moins 8 caractères'
    return
  }
  if (password.value !== confirm.value) {
    error.value = 'Les mots de passe ne correspondent pas'
    return
  }
  loading.value = true
  try {
    await api.post('/auth/reset-password', { token: token.value, password: password.value })
    success.value = true
  } catch (err) {
    error.value = err.response?.data?.message || 'Lien invalide ou expiré'
  } finally {
    loading.value = false
  }
}
</script>
