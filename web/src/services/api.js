import axios from 'axios'

export const TOKEN_STORAGE_KEY = 'autoclean_token'
export const USER_STORAGE_KEY = 'autoclean_user'
export const AUTH_UNAUTHORIZED_EVENT = 'auth:unauthorized'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

/**
 * Cliente HTTP compartido configurado contra el backend REST de AutoClean.
 */
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Adjunta automáticamente el token JWT en cada petición saliente cuando existe sesión activa
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// Limpia la sesión almacenada cuando una ruta protegida responde 401 Unauthorized
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const requestUrl = error.config?.url || ''
    const isAuthEndpoint = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register')

    if (status === 401 && !isAuthEndpoint) {
      localStorage.removeItem(TOKEN_STORAGE_KEY)
      localStorage.removeItem(USER_STORAGE_KEY)
      window.dispatchEvent(new CustomEvent(AUTH_UNAUTHORIZED_EVENT))
    }

    return Promise.reject(error)
  },
)

export default api

/**
 * Extrae un mensaje legible desde una respuesta de error de Axios/Spring Boot.
 */
export function getApiErrorMessage(error, fallback = 'Ocurrió un error al comunicarse con el servidor.') {
  if (!error.response) {
    return 'No fue posible conectar con el servidor. Verifica que el backend esté activo.'
  }

  const data = error.response.data
  if (data?.errors && typeof data.errors === 'object') {
    const fieldMessages = Object.values(data.errors).filter(Boolean)
    if (fieldMessages.length > 0) {
      return fieldMessages.join(' · ')
    }
  }

  if (data?.message) {
    return data.message
  }

  if (error.response.status === 401 || error.response.status === 403) {
    return 'No tienes permisos o tu sesión expiró. Inicia sesión nuevamente.'
  }

  return fallback
}
