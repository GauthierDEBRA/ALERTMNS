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
          <h2>Mot de passe oublié</h2>
          <p>Entrez votre email pour recevoir un lien de réinitialisation</p>
        </div>

        <div v-if="sent" class="alert alert-success">
          Si cet email est connu, un lien vous a été envoyé. Vérifiez votre boîte mail.
        </div>

        <form v-else @submit.prevent="handleSubmit" class="login-form">
          <div v-if="error" class="alert alert-error">{{ error }}</div>

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

          <button type="submit" class="login-btn" :disabled="loading || !email">
            <span v-if="loading"><div class="spinner" style="width:16px;height:16px;border-width:2px;border-top-color:white;"></div></span>
            <span v-else>Envoyer le lien</span>
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
import { ref } from 'vue'
import api from '../api/axios.js'

const email = ref('')
const loading = ref(false)
const error = ref('')
const sent = ref(false)

async function handleSubmit() {
  loading.value = true
  error.value = ''
  try {
    await api.post('/auth/forgot-password', { email: email.value })
    sent.value = true
  } catch {
    error.value = 'Une erreur est survenue. Réessayez plus tard.'
  } finally {
    loading.value = false
  }
}
</script>
