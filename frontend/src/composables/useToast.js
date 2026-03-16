import { ref } from 'vue'

let nextId = 0
const toasts = ref([])

function addToast(type, message, duration = 4000) {
  const id = ++nextId
  toasts.value.push({ id, type, message })
  setTimeout(() => remove(id), duration)
}

function remove(id) {
  const idx = toasts.value.findIndex(t => t.id === id)
  if (idx !== -1) toasts.value.splice(idx, 1)
}

export function useToast() {
  return {
    toasts,
    error:   (msg) => addToast('error',   msg),
    success: (msg) => addToast('success', msg),
    info:    (msg) => addToast('info',    msg),
    remove
  }
}
