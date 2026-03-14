export const AVATAR_COLORS = [
  '#E8501A', '#4299e1', '#48bb78', '#ed8936', '#9f7aea',
  '#ed64a6', '#38b2ac', '#667eea', '#fc8181', '#68d391'
]

function pickFirst(...values) {
  for (const value of values) {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      return String(value)
    }
  }
  return ''
}

function getAvatarParts(entity = {}) {
  return {
    prenom: pickFirst(entity.prenom, entity.auteurPrenom, entity.directUserPrenom),
    nom: pickFirst(entity.nom, entity.auteurNom, entity.directUserNom),
    email: pickFirst(entity.email, entity.auteurEmail, entity.directUserEmail),
    id: pickFirst(entity.id, entity.userId, entity.idUser, entity.auteurId, entity.directUserId)
  }
}

export function getAvatarColor(entity = {}) {
  const { prenom, nom, email, id } = getAvatarParts(entity)
  const seed = `${prenom}${nom}${email}${id}`

  let hash = 0
  for (let i = 0; i < seed.length; i++) {
    hash = seed.charCodeAt(i) + ((hash << 5) - hash)
  }

  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length]
}

export function getAvatarInitials(entity = {}, fallback = '?') {
  const { prenom, nom, email } = getAvatarParts(entity)
  const initials = `${prenom[0] || ''}${nom[0] || ''}`.toUpperCase()

  if (initials) {
    return initials
  }

  if (email) {
    return email[0].toUpperCase()
  }

  return fallback
}
