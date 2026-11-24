/**
 * Serializa un objeto Date en formato ISO-8601 preservando la hora local y su offset
 * (ej. "2026-11-13T17:30:00-05:00") en lugar de convertirlo a UTC ("Z").
 */
export function toLocalOffsetIsoString(date) {
  if (!(date instanceof Date) || Number.isNaN(date.getTime())) {
    return ''
  }
  const pad = (value) => String(value).padStart(2, '0')
  const offsetMinutes = -date.getTimezoneOffset()
  const sign = offsetMinutes >= 0 ? '+' : '-'
  const absOffset = Math.abs(offsetMinutes)
  const offsetHours = pad(Math.floor(absOffset / 60))
  const offsetRemainder = pad(absOffset % 60)

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours(),
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}${sign}${offsetHours}:${offsetRemainder}`
}

/**
 * Indica si una fecha (Date o ISO) cae en el horario de atención: 7:00–12:00 o 14:30–21:00.
 * Un valor vacío se considera válido.
 */
export function isWithinOperatingHours(value) {
  if (!value) return true
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return false
  const mins = date.getHours() * 60 + date.getMinutes()
  return (mins >= 420 && mins <= 720) || (mins >= 870 && mins <= 1260)
}
