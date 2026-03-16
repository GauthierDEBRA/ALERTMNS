import { describe, it, expect } from 'vitest'
import { getAvatarColor, getAvatarInitials, AVATAR_COLORS } from './avatar.js'

describe('getAvatarInitials', () => {
  it('retourne les initiales prenom+nom en majuscules', () => {
    expect(getAvatarInitials({ prenom: 'Marie', nom: 'Dupont' })).toBe('MD')
  })

  it('fonctionne avec auteurPrenom / auteurNom', () => {
    expect(getAvatarInitials({ auteurPrenom: 'Alice', auteurNom: 'Martin' })).toBe('AM')
  })

  it('retourne la première lettre de l\'email si pas de nom', () => {
    expect(getAvatarInitials({ email: 'bob@alertmns.fr' })).toBe('B')
  })

  it('retourne le fallback si aucune info disponible', () => {
    expect(getAvatarInitials({}, '?')).toBe('?')
    expect(getAvatarInitials({})).toBe('?')
  })

  it('retourne le fallback pour un objet vide avec fallback @', () => {
    expect(getAvatarInitials({}, '@')).toBe('@')
  })

  it('utilise directUserPrenom / directUserNom', () => {
    expect(getAvatarInitials({ directUserPrenom: 'Chris', directUserNom: 'Lee' })).toBe('CL')
  })
})

describe('getAvatarColor', () => {
  it('retourne une couleur valide de la palette', () => {
    const color = getAvatarColor({ prenom: 'Marie', nom: 'Dupont', id: 1 })
    expect(AVATAR_COLORS).toContain(color)
  })

  it('est déterministe — même entrée → même couleur', () => {
    const entity = { prenom: 'Alice', nom: 'Martin', email: 'alice@test.fr', id: 42 }
    expect(getAvatarColor(entity)).toBe(getAvatarColor(entity))
  })

  it('retourne une couleur même pour un objet vide', () => {
    const color = getAvatarColor({})
    expect(AVATAR_COLORS).toContain(color)
  })

  it('deux utilisateurs différents peuvent avoir des couleurs différentes', () => {
    const c1 = getAvatarColor({ prenom: 'Alice', nom: 'A', id: 1 })
    const c2 = getAvatarColor({ prenom: 'Zzz', nom: 'Z', id: 999 })
    // On vérifie juste que les deux sont des couleurs valides
    expect(AVATAR_COLORS).toContain(c1)
    expect(AVATAR_COLORS).toContain(c2)
  })
})
