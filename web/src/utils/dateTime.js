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
