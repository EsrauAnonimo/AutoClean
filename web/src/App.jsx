import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/layout/ProtectedRoute'
import { ToastViewport } from './components/layout/ToastViewport'
import { AuthProvider } from './context/AuthContext'
import { AppointmentHistoryPage } from './pages/client/AppointmentHistoryPage'
import { AppointmentsPage } from './pages/admin/AppointmentsPage'
import { BookAppointmentPage } from './pages/client/BookAppointmentPage'
import { CatalogPage } from './pages/admin/CatalogPage'
import { CustomersPage } from './pages/admin/CustomersPage'
import { DashboardPage } from './pages/admin/DashboardPage'
import { LoginPage } from './pages/auth/LoginPage'
import { MyVehiclesPage } from './pages/client/MyVehiclesPage'
import { OAuth2RedirectPage } from './pages/auth/OAuth2RedirectPage'
import { PortalDashboardPage } from './pages/client/PortalDashboardPage'
import { RegisterPage } from './pages/auth/RegisterPage'
import { ReportsPage } from './pages/admin/ReportsPage'
import { SuppliesPage } from './pages/admin/SuppliesPage'
import { VehiclesPage } from './pages/admin/VehiclesPage'

const STAFF_ROLES = ['ADMIN', 'OPERATOR', 'MANAGER']
const CLIENT_ROLES = ['CLIENTE']

/**
 * Enrutador principal de AutoClean Web con separación por rol (panel staff y portal cliente).
 */
function App() {
  return (
    <AuthProvider>
      <ToastViewport />
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/oauth2/redirect" element={<OAuth2RedirectPage />} />

          {/* Rutas del panel administrativo/operativo */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute allowedRoles={STAFF_ROLES}>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/customers"
            element={
              <ProtectedRoute allowedRoles={STAFF_ROLES}>
                <CustomersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/vehicles"
            element={
              <ProtectedRoute allowedRoles={STAFF_ROLES}>
                <VehiclesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/appointments"
            element={
              <ProtectedRoute allowedRoles={STAFF_ROLES}>
                <AppointmentsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/catalog"
            element={
              <ProtectedRoute allowedRoles={STAFF_ROLES}>
                <CatalogPage key="services" initialTab="services" />
              </ProtectedRoute>
            }
          />
          <Route
            path="/employees"
            element={
              <ProtectedRoute allowedRoles={STAFF_ROLES}>
                <CatalogPage key="employees" initialTab="employees" />
              </ProtectedRoute>
            }
          />
          <Route
            path="/supplies"
            element={
              <ProtectedRoute allowedRoles={STAFF_ROLES}>
                <SuppliesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reports"
            element={
              <ProtectedRoute allowedRoles={STAFF_ROLES}>
                <ReportsPage />
              </ProtectedRoute>
            }
          />

          {/* Rutas del Portal de Clientes */}
          <Route
            path="/portal/dashboard"
            element={
              <ProtectedRoute allowedRoles={CLIENT_ROLES}>
                <PortalDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/portal/mis-vehiculos"
            element={
              <ProtectedRoute allowedRoles={CLIENT_ROLES}>
                <MyVehiclesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/portal/agendar"
            element={
              <ProtectedRoute allowedRoles={CLIENT_ROLES}>
                <BookAppointmentPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/portal/historial"
            element={
              <ProtectedRoute allowedRoles={CLIENT_ROLES}>
                <AppointmentHistoryPage />
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
