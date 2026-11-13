import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AuthProvider } from './context/AuthContext'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { PortalDashboardPage } from './pages/PortalDashboardPage'
import { RegisterPage } from './pages/RegisterPage'

/**
 * Enrutador principal de AutoClean Web con separación por rol (panel staff y portal cliente).
 */
function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR', 'MANAGER']}>
                <DashboardPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/portal/dashboard"
            element={
              <ProtectedRoute allowedRoles={['CLIENTE']}>
                <PortalDashboardPage />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
