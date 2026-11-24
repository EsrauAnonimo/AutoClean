/**
 * Formatea un valor numérico como moneda colombiana sin decimales (ej. "$ 45.000").
 */
export function formatCop(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(Number(value) || 0)
}

/**
 * Formatea una fecha ISO con fecha media y hora corta; devuelve "—" si está vacía.
 */
export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}
