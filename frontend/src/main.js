import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router/index.js'
import './assets/global.css'
import './composables/useDarkMode.js' // initialise data-theme dès le démarrage
import { useAuthStore } from './stores/auth.js'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Restaure la session depuis le cookie de refresh avant le premier rendu
const authStore = useAuthStore(pinia)
authStore.initAuth().finally(() => {
  app.mount('#app')
})
