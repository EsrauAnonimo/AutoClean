import axios from 'axios'

export const TOKEN_STORAGE_KEY = 'autoclean_token'
export const USER_STORAGE_KEY = 'autoclean_user'

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

export default api
