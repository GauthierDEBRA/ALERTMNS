import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useToast } from './useToast.js'

// Réinitialiser le module avant chaque test (singleton partagé)
beforeEach(() => {
  const { toasts, remove } = useToast()
  toasts.value.forEach(t => remove(t.id))
})

describe('useToast', () => {
  it('error() ajoute un toast de type error', () => {
    const { toasts, error } = useToast()
    error('Une erreur')
    expect(toasts.value).toHaveLength(1)
    expect(toasts.value[0].type).toBe('error')
    expect(toasts.value[0].message).toBe('Une erreur')
  })

  it('success() ajoute un toast de type success', () => {
    const { toasts, success } = useToast()
    success('Succès !')
    expect(toasts.value[0].type).toBe('success')
  })

  it('info() ajoute un toast de type info', () => {
    const { toasts, info } = useToast()
    info('Une info')
    expect(toasts.value[0].type).toBe('info')
  })

  it('chaque toast reçoit un id unique', () => {
    const { toasts, error, success } = useToast()
    error('msg1')
    success('msg2')
    const ids = toasts.value.map(t => t.id)
    expect(new Set(ids).size).toBe(ids.length)
  })

  it('remove() supprime le toast par id', () => {
    const { toasts, error, remove } = useToast()
    error('à supprimer')
    const id = toasts.value[0].id
    remove(id)
    expect(toasts.value.find(t => t.id === id)).toBeUndefined()
  })

  it('remove() avec un id inexistant ne lève pas d\'erreur', () => {
    const { remove } = useToast()
    expect(() => remove(99999)).not.toThrow()
  })

  it('le toast se supprime automatiquement après la durée (fake timers)', () => {
    vi.useFakeTimers()
    const { toasts, error } = useToast()
    error('auto-dismiss')
    expect(toasts.value).toHaveLength(1)
    vi.advanceTimersByTime(4001)
    expect(toasts.value).toHaveLength(0)
    vi.useRealTimers()
  })
})
