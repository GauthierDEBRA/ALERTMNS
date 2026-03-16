<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="not-found-code">404</div>
      <h1 class="not-found-title">Page introuvable</h1>
      <p class="not-found-desc">La page que vous cherchez n'existe pas ou a été déplacée.</p>
      <router-link to="/chat" class="btn btn-primary">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 12h18M3 12l6-6M3 12l6 6"/>
        </svg>
        Retour à l'accueil
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: var(--content-bg);
}

.not-found-content {
  text-align: center;
  padding: 40px;
}

.not-found-code {
  font-size: 96px;
  font-weight: 800;
  color: var(--primary);
  line-height: 1;
  margin-bottom: 16px;
  opacity: 0.15;
}

.not-found-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 8px;
}

.not-found-desc {
  color: var(--text-light);
  margin: 0 0 28px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: var(--radius);
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  transition: background 0.15s;
}

.btn-primary {
  background: var(--primary);
  color: #fff;
}

.btn-primary:hover {
  background: var(--primary-dark);
}
</style>
