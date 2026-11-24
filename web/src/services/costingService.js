import api from './api'

/**
 * Consulta todas las estimaciones de costo registradas (de más reciente a más antigua).
 */
export async function getCostEstimates() {
  const { data } = await api.get('/costing')
  return data
}

/**
 * Calcula y guarda una estimación de costo para un tipo de servicio y cita opcional.
 */
export async function createCostEstimate(payload) {
  const { data } = await api.post('/costing/estimate', payload)
  return data
}
