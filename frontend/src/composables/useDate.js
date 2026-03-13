/**
 * Date formatting utilities in French
 */
export function useDate() {
  const MOIS = [
    'janvier', 'février', 'mars', 'avril', 'mai', 'juin',
    'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'
  ]

  function formatDate(dateInput) {
    if (!dateInput) return ''
    const date = new Date(dateInput)
    const now = new Date()

    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

    const timeStr = date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })

    if (msgDate.getTime() === today.getTime()) {
      return `Aujourd'hui à ${timeStr}`
    } else if (msgDate.getTime() === yesterday.getTime()) {
      return `Hier à ${timeStr}`
    } else {
      const day = date.getDate()
      const month = MOIS[date.getMonth()]
      return `${day} ${month} à ${timeStr}`
    }
  }

  function formatDateShort(dateInput) {
    if (!dateInput) return ''
    const date = new Date(dateInput)
    const now = new Date()

    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

    const timeStr = date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })

    if (msgDate.getTime() === today.getTime()) {
      return timeStr
    } else if (msgDate.getTime() === yesterday.getTime()) {
      return `Hier`
    } else {
      const day = date.getDate()
      const month = MOIS[date.getMonth()]
      return `${day} ${month}`
    }
  }

  function formatDateFull(dateInput) {
    if (!dateInput) return ''
    const date = new Date(dateInput)
    const day = date.getDate()
    const month = MOIS[date.getMonth()]
    const year = date.getFullYear()
    const timeStr = date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
    return `${day} ${month} ${year} à ${timeStr}`
  }

  function isSameDay(date1, date2) {
    const d1 = new Date(date1)
    const d2 = new Date(date2)
    return d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate()
  }

  function formatDayHeader(dateInput) {
    if (!dateInput) return ''
    const date = new Date(dateInput)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

    if (msgDate.getTime() === today.getTime()) return "Aujourd'hui"
    if (msgDate.getTime() === yesterday.getTime()) return 'Hier'
    const day = date.getDate()
    const month = MOIS[date.getMonth()]
    const year = date.getFullYear()
    if (date.getFullYear() === now.getFullYear()) return `${day} ${month}`
    return `${day} ${month} ${year}`
  }

  return {
    formatDate,
    formatDateShort,
    formatDateFull,
    formatDayHeader,
    isSameDay
  }
}
