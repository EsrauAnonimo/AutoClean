import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getHomeRouteByRole } from '../utils/authRedirect'
import { showToast } from '../utils/toast'
import { AutoCleanLogo } from './AutoCleanLogo'

const ADMIN_NAV_ITEMS = [
  { to: '/dashboard', label: 'Panel principal', icon: 'grid' },
  { to: '/appointments', label: 'Agenda de Citas', icon: 'calendar' },
  { to: '/customers', label: 'Clientes', icon: 'users' },
  { to: '/vehicles', label: 'Vehículos', icon: 'car' },
  { to: '/catalog', label: 'Catálogo', icon: 'sparkles' },
  { to: '/employees', label: 'Empleados', icon: 'badge' },
  { to: '/supplies', label: 'Insumos', icon: 'box' },
  { to: '/reports', label: 'Reportes', icon: 'chart' },
]

const PORTAL_NAV_ITEMS = [
  { to: '/portal/dashboard', label: 'Inicio', shortLabel: 'Inicio', icon: 'home' },
  { to: '/portal/mis-vehiculos', label: 'Mis Vehículos', shortLabel: 'Vehículos', icon: 'car' },
  { to: '/portal/agendar', label: 'Agendar Cita', shortLabel: 'Agendar', icon: 'plus', featured: true },
  { to: '/portal/historial', label: 'Historial', shortLabel: 'Historial', icon: 'clock' },
]

const ROLE_LABELS = {
  ADMIN: 'Administrador',
  MANAGER: 'Gerente',
  OPERATOR: 'Operador',
  CLIENTE: 'Cliente',
}

function NavIcon({ name, className = 'h-4 w-4 shrink-0' }) {
  const paths = {
    home: 'M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5.5v-6h-5v6H4a1 1 0 0 1-1-1v-9.5Z',
    grid: 'M4 4h7v7H4V4Zm9 0h7v7h-7V4ZM4 13h7v7H4v-7Zm9 0h7v7h-7v-7Z',
    users: 'M16 11a4 4 0 1 0-8 0 4 4 0 0 0 8 0Zm-12 9a8 8 0 1 1 16 0',
    car: 'M5 11l1.5-4.5A2 2 0 0 1 8.4 5h7.2a2 2 0 0 1 1.9 1.5L19 11m-14 0h14v6a1 1 0 0 1-1 1h-1a1 1 0 0 1-1-1v-1H8v1a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1v-6Zm2.5 3h.01M16.5 14h.01',
    calendar: 'M8 2v3m8-3v3M3 9h18M5 5h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z',
    clock: 'M12 6v6l4 2m6-2a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z',
    sparkles: 'M12 3l1.8 4.2L18 9l-4.2 1.8L12 15l-1.8-4.2L6 9l4.2-1.8L12 3Z',
    badge: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 8a7 7 0 0 1 14 0',
    box: 'M20 7 12 3 4 7m16 0v10l-8 4m8-14-8 4m0 10-8-4V7m8 4v10',
    chart: 'M4 20h16M7 16v-5m5 5V7m5 9v-8',
    plus: 'M12 5v14M5 12h14',
    search: 'M21 21l-4.3-4.3M11 18a7 7 0 1 1 0-14 7 7 0 0 1 0 14Z',
    bell: 'M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9Zm-4.27 13a2 2 0 0 1-3.46 0',
    logout: 'M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4m7 14 5-5-5-5m5 5H9',
    menu: 'M4 6h16M4 12h16M4 18h16',
  }

  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={paths[name] || paths.grid} />
    </svg>
  )
}

function getInitials(email) {
  if (!email) return 'AC'
  const clean = email.split('@')[0]
  return clean.slice(0, 2).toUpperCase()
}

/**
 * Plantilla adaptativa AutoClean:
 * - Admin (Desktop 1440px): Sidebar oscuro Índigo 950 fijo con perfil y "Cerrar sesión" abajo + Header claro.
 * - Cliente (Mobile-first): Encabezado gradiente Índigo->Violeta + barra inferior fija (y sidebar en desktop).
 */
export function MainLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [globalQuery, setGlobalQuery] = useState('')
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  const isClient = user?.role === 'CLIENTE'
  const homeRoute = getHomeRouteByRole(user?.role)
  const navItems = isClient ? PORTAL_NAV_ITEMS : ADMIN_NAV_ITEMS
  const roleLabel = ROLE_LABELS[user?.role] || 'Usuario'
  const initials = getInitials(user?.email)

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const handleGlobalSearch = (event) => {
    event.preventDefault()
    const query = globalQuery.trim()
    if (!query) return
    const upper = query.toUpperCase()
    if (/^[A-Z]{3}[0-9]{2,3}[A-Z]?$/.test(upper)) {
      navigate(isClient ? `/portal/agendar?plate=${upper}` : `/appointments`)
      showToast({ type: 'info', title: `Búsqueda rápida: ${upper}`, description: 'Abriendo agenda de citas.' })
      return
    }
    const match = navItems.find((item) => item.label.toLowerCase().includes(query.toLowerCase()))
    if (match) {
      navigate(match.to)
      setGlobalQuery('')
    } else {
      navigate('/appointments')
    }
  }

  const sidebarContent = (
    <div className="flex h-full flex-col justify-between bg-[#1E1B4B] p-4 text-white">
      <div>
        <Link
          to={homeRoute}
          onClick={() => setMobileMenuOpen(false)}
          className="flex items-center rounded-xl px-2 py-2 transition hover:bg-[#312E81]/60"
        >
          <AutoCleanLogo
            size="md"
            subtitle={isClient ? 'Portal del Cliente' : 'Panel Operativo'}
          />
        </Link>

        <nav aria-label="Navegación principal" className="mt-6 space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={() => setMobileMenuOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-[14px] font-medium transition-all ${
                  isActive
                    ? 'bg-[#4F46E5] text-white shadow-sm'
                    : 'text-indigo-200 hover:bg-[#312E81] hover:text-white'
                }`
              }
            >
              <NavIcon name={item.icon} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      {/* Perfil y Cerrar sesión siempre al fondo del sidebar */}
      <div className="mt-6 border-t border-white/10 pt-4">
        <div className="flex items-center gap-3 px-2">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[#8B5CF6] text-xs font-bold text-white">
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <p className="truncate text-[13px] font-semibold text-white">
              {user?.email || 'usuario@autoclean.co'}
            </p>
            <p className="text-[11px] font-medium text-indigo-300">{roleLabel}</p>
          </div>
        </div>

        <button
          type="button"
          onClick={handleLogout}
          className="mt-3 flex h-[40px] w-full items-center justify-center gap-2 rounded-xl bg-white/10 px-3 text-[13px] font-semibold text-indigo-100 transition hover:bg-rose-500/20 hover:text-rose-200"
        >
          <NavIcon name="logout" />
          <span>Cerrar sesión</span>
        </button>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-[#F5F6FB] text-[#1E1B4B]">
      {/* Sidebar fijo en Desktop */}
      <aside className="hidden lg:fixed lg:inset-y-0 lg:left-0 lg:z-30 lg:block lg:w-64">
        {sidebarContent}
      </aside>

      {/* Drawer móvil para Admin */}
      {mobileMenuOpen && !isClient && (
        <div className="fixed inset-0 z-40 flex lg:hidden">
          <div className="w-64 shrink-0">{sidebarContent}</div>
          <button
            type="button"
            aria-label="Cerrar menú"
            onClick={() => setMobileMenuOpen(false)}
            className="flex-1 bg-[#1E1B4B]/50 backdrop-blur-xs"
          />
        </div>
      )}

      {/* Columna principal */}
      <div className="flex min-h-screen flex-col lg:pl-64">
        {isClient ? (
          <header className="bg-gradient-to-br from-[#1E1B4B] via-[#312E81] to-[#4F46E5] px-4 py-4 text-white lg:border-b lg:border-[#E4E6F2] lg:bg-white lg:bg-none lg:px-8 lg:text-[#1E1B4B]">
            <div className="mx-auto flex max-w-6xl items-center justify-between gap-3">
              <Link to={homeRoute} className="lg:hidden">
                <AutoCleanLogo size="sm" subtitle="Portal del Cliente" />
              </Link>

              <div className="hidden lg:block">
                <p className="text-[15px] font-semibold text-[#1E1B4B]">Tu espacio AutoClean</p>
                <p className="text-[12px] text-[#5B5F7A]">Reserva en segundos y sigue tu vehículo en vivo</p>
              </div>

              <div className="flex items-center gap-2.5">
                <button
                  type="button"
                  onClick={handleLogout}
                  className="inline-flex h-9 items-center gap-1.5 rounded-xl bg-white/15 px-3 text-xs font-semibold text-white transition hover:bg-white/25 lg:hidden"
                >
                  <NavIcon name="logout" className="h-3.5 w-3.5" />
                  <span>Salir</span>
                </button>
                <div className="hidden items-center gap-2.5 rounded-xl border border-[#E4E6F2] bg-[#F5F6FB] px-3 py-1.5 lg:flex">
                  <span className="flex h-7 w-7 items-center justify-center rounded-full bg-[#4F46E5] text-[11px] font-bold text-white">
                    {initials}
                  </span>
                  <span className="text-[13px] font-semibold text-[#1E1B4B]">{user?.email}</span>
                  <span className="rounded-full bg-[#EDE9FE] px-2 py-0.5 text-[11px] font-semibold text-[#6D28D9]">
                    {roleLabel}
                  </span>
                </div>
              </div>
            </div>
          </header>
        ) : (
          <header className="sticky top-0 z-20 border-b border-[#E4E6F2] bg-white px-4 py-3 sm:px-6 lg:px-8">
            <div className="mx-auto flex max-w-[1440px] items-center justify-between gap-3">
              <div className="flex flex-1 items-center gap-3">
                <button
                  type="button"
                  onClick={() => setMobileMenuOpen(true)}
                  aria-label="Abrir navegación"
                  className="flex h-10 w-10 items-center justify-center rounded-xl border border-[#E4E6F2] text-[#1E1B4B] hover:bg-[#F5F6FB] lg:hidden"
                >
                  <NavIcon name="menu" className="h-5 w-5" />
                </button>
                <form onSubmit={handleGlobalSearch} className="relative w-full max-w-md">
                  <span className="pointer-events-none absolute left-3.5 top-3 text-[#5B5F7A]">
                    <NavIcon name="search" className="h-4 w-4" />
                  </span>
                  <input
                    type="search"
                    value={globalQuery}
                    onChange={(e) => setGlobalQuery(e.target.value)}
                    placeholder="Buscar placa, módulo o ir a agenda..."
                    className="h-[40px] w-full rounded-xl border border-[#E4E6F2] bg-[#F5F6FB] pl-10 pr-3.5 text-[14px] text-[#1E1B4B] placeholder-[#5B5F7A]/70 transition outline-none focus:border-[#4F46E5] focus:bg-white focus:ring-4 focus:ring-[#4F46E5]/10"
                  />
                </form>
              </div>

              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={() =>
                    showToast({
                      type: 'info',
                      title: 'Operación al día',
                      description: 'Horario activo: 7:00 a. m. – 12:00 p. m. y 2:30 p. m. – 9:00 p. m.',
                    })
                  }
                  aria-label="Notificaciones"
                  className="relative flex h-10 w-10 items-center justify-center rounded-xl border border-[#E4E6F2] bg-white text-[#5B5F7A] transition hover:bg-[#F5F6FB] hover:text-[#1E1B4B]"
                >
                  <NavIcon name="bell" className="h-4 w-4" />
                  <span className="absolute right-2.5 top-2.5 h-2 w-2 rounded-full bg-[#8B5CF6]" />
                </button>
                <div className="flex items-center gap-2.5 rounded-xl border border-[#E4E6F2] bg-white px-2.5 py-1.5">
                  <span className="flex h-7 w-7 items-center justify-center rounded-full bg-[#4F46E5] text-[11px] font-bold text-white">
                    {initials}
                  </span>
                  <span className="hidden text-[13px] font-semibold text-[#1E1B4B] sm:inline">
                    {user?.email?.split('@')[0] || 'Equipo'}
                  </span>
                  <span className="rounded-full bg-[#EDE9FE] px-2.5 py-0.5 text-[11px] font-semibold text-[#6D28D9]">
                    {roleLabel}
                  </span>
                </div>
              </div>
            </div>
          </header>
        )}

        <main
          className={`mx-auto w-full max-w-[1440px] flex-1 px-4 py-6 sm:px-6 lg:px-8 ${
            isClient ? 'pb-24 lg:pb-8' : ''
          }`}
        >
          {children}
        </main>
      </div>

      {/* Barra inferior fija en móvil para Portal Cliente (72px) */}
      {isClient && (
        <nav
          aria-label="Navegación móvil cliente"
          className="fixed inset-x-0 bottom-0 z-30 flex h-[72px] items-center justify-around border-t border-[#E4E6F2] bg-white/95 px-2 backdrop-blur lg:hidden"
        >
          {PORTAL_NAV_ITEMS.map((item) =>
            item.featured ? (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `-mt-6 flex flex-col items-center gap-1 ${
                    isActive ? 'text-[#4F46E5]' : 'text-[#5B5F7A]'
                  }`
                }
              >
                <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-[#4F46E5] to-[#8B5CF6] text-white shadow-float">
                  <NavIcon name="plus" className="h-5 w-5" />
                </span>
                <span className="text-[11px] font-semibold">{item.shortLabel}</span>
              </NavLink>
            ) : (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex flex-col items-center gap-1 px-2 py-1 text-[11px] font-semibold transition-colors ${
                    isActive ? 'text-[#4F46E5]' : 'text-[#5B5F7A]'
                  }`
                }
              >
                <NavIcon name={item.icon} className="h-5 w-5" />
                <span>{item.shortLabel}</span>
              </NavLink>
            ),
          )}
        </nav>
      )}
    </div>
  )
}
