import api from './api'

/**
 * Consulta el reporte de servicios realizados dentro de un rango de fechas (YYYY-MM-DD).
 */
export async function getServicesReport(from, to) {
  const { data } = await api.get('/reports/services', { params: { from, to } })
  return data
}

/**
 * Consulta el reporte comparativo de ingresos vs costos en un rango de fechas (YYYY-MM-DD).
 */
export async function getRevenueReport(from, to) {
  const { data } = await api.get('/reports/revenue', { params: { from, to } })
  return data
}
