import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  {
    path: '/',
    redirect: '/chat'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/forgot-password',
    component: () => import('../views/ForgotPasswordView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/reset-password',
    component: () => import('../views/ResetPasswordView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/chat',
    component: () => import('../components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Welcome',
        component: () => import('../views/WelcomeView.vue')
      },
      {
        path: 'canal/:id',
        name: 'Chat',
        component: () => import('../views/ChatView.vue')
      }
    ]
  },
  {
    path: '/pointage',
    component: () => import('../components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Pointage',
        component: () => import('../views/PointageView.vue')
      }
    ]
  },
  {
    path: '/reunions',
    component: () => import('../components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Reunions',
        component: () => import('../views/ReunionsView.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../components/AppLayout.vue'),
    meta: { requiresAuth: true, roles: ['Admin', 'RH'] },
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('../views/AdminView.vue')
      }
    ]
  },
  {
    path: '/profil',
    component: () => import('../components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Profil',
        component: () => import('../views/ProfilView.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  if (!authStore.isReady) {
    await authStore.initAuth()
  }

  const requiresAuth = to.matched.some(r => r.meta.requiresAuth !== false)
  const isLoginPage = to.path === '/login'

  if (requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }

  if (isLoginPage && authStore.isAuthenticated) {
    next('/chat')
    return
  }

  // Role check
  const requiredRoles = to.meta.roles
  if (requiredRoles && authStore.isAuthenticated) {
    if (!requiredRoles.includes(authStore.user?.role)) {
      next('/chat')
      return
    }
  }

  next()
})

export default router
