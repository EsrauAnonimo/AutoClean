import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AuthProvider } from './context/AuthContext'
import { AppointmentsPage } from './pages/AppointmentsPage'
import { BookAppointmentPage } from './pages/BookAppointmentPage'
import { CatalogPage } from './pages/CatalogPage'
import { CustomersPage } from './pages/CustomersPage'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { MyVehiclesPage } from './pages/MyVehiclesPage'
import { OAuth2RedirectPage } from './pages/OAuth2RedirectPage'
import { PortalDashboardPage } from './pages/PortalDashboardPage'
import { RegisterPage } from './pages/RegisterPage'
import { ReportsPage } from './pages/ReportsPage'
import { SuppliesPage } from './pages/SuppliesPage'
import { VehiclesPage } from './pages/VehiclesPage'

const STAFF_ROLES = ['ADMIN', 'OPERATOR', 'MANAGER']
const CLIENT_ROLES = ['CLIENTE']

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

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
