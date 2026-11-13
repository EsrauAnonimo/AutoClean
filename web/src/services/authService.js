import api from './api'

/**
 * Servicio de autenticación conectado a /api/auth/*.
 */
const authService = {
  /**
   * Inicia sesión con correo y contraseña y retorna token JWT + datos básicos del usuario.
   *
   * @param {{ email: string, password: string }} credentials
   * @returns {Promise<{ token: string, type: string, email: string, role: string, customerId: string | null }>}
   */
  async login(credentials) {
    const response = await api.post('/auth/login', credentials)
    return response.data
  },

  /**
   * Registra un usuario con rol CLIENTE.
   *
   * @param {{ name?: string, phone?: string, email: string, password: string }} payload
   * @returns {Promise<{ token: string, type: string, email: string, role: string, customerId: string | null }>}
   */
  async register(payload) {
    const response = await api.post('/auth/register', payload)
    return response.data
  },

  /**
   * Completa o vincula el perfil de cliente de la cuenta autenticada (ej. login con Google).
   *
   * @param {{ name: string, phone: string }} payload
   * @returns {Promise<{ token: string, type: string, email: string, role: string, customerId: string | null }>}
   */
  async completeProfile(payload) {
    const response = await api.post('/auth/complete-profile', payload)
    return response.data
  },
}

export default authService
