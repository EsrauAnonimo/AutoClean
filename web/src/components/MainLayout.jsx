import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getAppointments } from '../services/appointmentService'
import { getSupplies } from '../services/supplyService'
import { getHomeRouteByRole } from '../utils/authRedirect'
import { showToast } from '../utils/toast'
import { AutoCleanLogo } from './AutoCleanLogo'

const ADMIN_NAV_GROUPS = [
  {
    title: 'OPERACIÓN',
    items: [
      { to: '/dashboard', label: 'Panel principal', icon: 'grid' },
      { to: '/appointments', label: 'Agenda de citas', icon: 'calendar' },
    ],
  },
  {
    title: 'GESTIÓN',
    items: [
      { to: '/customers', label: 'Clientes', icon: 'users' },
      { to: '/vehicles', label: 'Vehículos', icon: 'car' },
      { to: '/catalog', label: 'Catálogo', icon: 'sparkles' },
      { to: '/employees', label: 'Empleados', icon: 'badge' },
      { to: '/supplies', label: 'Insumos', icon: 'box' },
    ],
  },
  {
    title: 'ANÁLISIS',
    items: [
      { to: '/reports', label: 'Reportes', icon: 'chart' },
      { to: '/reports?vista=costeo', label: 'Costeo', icon: 'calculator' },
    ],
  },
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

function NavIcon({ name, className = 'h-5 w-5 shrink-0' }) {
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
    calculator: 'M6 2h12a2 2 0 0 1 2 2v16a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm2 4h8v3H8V6Zm0 7h.01M12 13h.01M16 13h.01M8 17h.01M12 17h.01M16 17h.01',
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
      strokeWidth="1.75"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={paths[name] || paths.grid} />
    </svg>
  )
}

function getCapitalizedUserName(user) {
  const source = user?.name?.trim() || (user?.email ? user.email.split('@')[0] : '')
  if (!source) return ''
  return source
    .replace(/[._-]+/g, ' ')
    .replace(/\d+/g, '')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ')
}

function getInitials(user) {
  const name = getCapitalizedUserName(user)
  if (!name) return 'AC'
  const parts = name.split(' ').filter(Boolean)
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase()
  }
  return parts[0].slice(0, 2).toUpperCase()
}

/**
 * Plantilla adaptativa AutoClean:
 * - Admin: Sidebar oscuro agrupado (OPERACIÓN, GESTIÓN, ANÁLISIS) con perfil completo abajo + header limpio (buscador, campana, avatar pequeño).
 * - Cliente: Móvil sin sidebar con barra inferior (Agendar elevado en violeta); escritorio con sidebar angosto y header sin correo.
 */
export function MainLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [globalQuery, setGlobalQuery] = useState('')
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [alertsOpen, setAlertsOpen] = useState(false)
  const [alertsData, setAlertsData] = useState({ lowSupplies: [], readyVehicles: [], pendingToday: [] })
  const alertsRef = useRef(null)

  const isClient = user?.role === 'CLIENTE'

  const loadOperationalAlerts = useCallback(async () => {
    if (isClient || !user?.role) return
    try {
      const [supplyList, appointmentList] = await Promise.all([
        getSupplies().catch(() => []),
        getAppointments().catch(() => []),
      ])
      const now = new Date()
      const todayKey = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(
        now.getDate(),
      ).padStart(2, '0')}`

      const lowSupplies = (Array.isArray(supplyList) ? supplyList : []).filter(
        (item) => Number(item.stock) <= 5,
      )
      const safeAppointments = Array.isArray(appointmentList) ? appointmentList : []
      const readyVehicles = safeAppointments.filter(
        (item) => item.vehicleStatus === 'READY' && item.status !== 'CANCELLED' && item.status !== 'FINISHED',
      )
      const pendingToday = safeAppointments.filter((item) => {
        if (item.status !== 'PENDING' || !item.dateTime) return false
        const d = new Date(item.dateTime)
        if (Number.isNaN(d.getTime())) return false
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
          d.getDate(),
        ).padStart(2, '0')}`
        return key === todayKey
      })

      setAlertsData({ lowSupplies, readyVehicles, pendingToday })
    } catch {
      // Silencioso en el layout para no interrumpir navegación
    }
  }, [isClient, user?.role])

  useEffect(() => {
    loadOperationalAlerts()
  }, [loadOperationalAlerts, location.pathname])

  useEffect(() => {
    if (!alertsOpen) return undefined
    const handleOutsideClick = (event) => {
      if (alertsRef.current && !alertsRef.current.contains(event.target)) {
        setAlertsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleOutsideClick)
    return () => document.removeEventListener('mousedown', handleOutsideClick)
  }, [alertsOpen])

  const totalAlerts = useMemo(
    () =>
      alertsData.lowSupplies.length +
      alertsData.readyVehicles.length +
      alertsData.pendingToday.length,
    [alertsData],
  )
  const homeRoute = getHomeRouteByRole(user?.role)
  const roleLabel = ROLE_LABELS[user?.role] || 'Usuario'
  const displayName = getCapitalizedUserName(user)
  const firstName = displayName ? displayName.split(' ')[0] : ''
  const initials = getInitials(user)
  const allAdminItems = ADMIN_NAV_GROUPS.flatMap((group) => group.items)

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
    const items = isClient ? PORTAL_NAV_ITEMS : allAdminItems
    const match = items.find((item) => item.label.toLowerCase().includes(query.toLowerCase()))
    if (match) {
      navigate(match.to)
      setGlobalQuery('')
    } else {
      navigate('/appointments')
    }
  }

  const sidebarContent = (
    <div className="flex h-full flex-col justify-between overflow-y-auto bg-[#1E1B4B] p-4 text-white">
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

        {isClient ? (
          <nav aria-label="Navegación cliente" className="mt-6 space-y-1">
            {PORTAL_NAV_ITEMS.map((item) => (
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
        ) : (
          <nav aria-label="Navegación principal" className="mt-6 space-y-5">
            {ADMIN_NAV_GROUPS.map((group) => (
              <div key={group.title}>
                <p className="px-3.5 text-[11px] font-semibold tracking-wider text-indigo-300/75 uppercase">
                  {group.title}
                </p>
                <div className="mt-1.5 space-y-1">
                  {group.items.map((item) => {
                    const isCostingItem = item.to.includes('vista=costeo')
                    const isReportsRoot = item.to === '/reports'
                    const activeCosting =
                      location.pathname === '/reports' && location.search.includes('vista=costeo')

                    return (
                      <NavLink
                        key={item.to}
                        to={item.to}
                        onClick={() => setMobileMenuOpen(false)}
                        className={({ isActive }) => {
                          const resolvedActive = isCostingItem
                            ? activeCosting
                            : isReportsRoot
                              ? isActive && !activeCosting
                              : isActive
                          return `flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-[14px] font-medium transition-all ${
                            resolvedActive
                              ? 'bg-[#4F46E5] text-white shadow-sm'
                              : 'text-indigo-200 hover:bg-[#312E81] hover:text-white'
                          }`
                        }}
                      >
                        <NavIcon name={item.icon} />
                        <span>{item.label}</span>
                      </NavLink>
                    )
                  })}
                </div>
              </div>
            ))}
          </nav>
        )}
      </div>

      {/* Perfil de usuario completo únicamente en el footer del sidebar */}
      <div className="mt-6 border-t border-white/10 pt-4">
        <div className="flex items-center gap-3 px-2">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[#8B5CF6] text-xs font-semibold text-white">
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <p className="truncate text-[13px] font-semibold text-white">
              {displayName || 'Equipo AutoClean'}
            </p>
            <p className="text-[11px] font-normal text-indigo-300">{roleLabel}</p>
          </div>
        </div>

        <button
          type="button"
          onClick={handleLogout}
          className="mt-3 flex h-[40px] w-full items-center justify-center gap-2 rounded-xl bg-white/10 px-3 text-[13px] font-semibold text-indigo-100 transition hover:bg-rose-500/20 hover:text-rose-200"
        >
          <NavIcon name="logout" className="h-4 w-4 shrink-0" />
          <span>Cerrar sesión</span>
        </button>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-[#F5F6FB] text-[#1E1B4B]">
      {/* Sidebar fijo en Desktop: angosto (w-56) en cliente, completo (w-64) en admin */}
      <aside
        className={`hidden lg:fixed lg:inset-y-0 lg:left-0 lg:z-30 lg:block ${
          isClient ? 'lg:w-56' : 'lg:w-64'
        }`}
      >
        {sidebarContent}
      </aside>

      {/* Drawer móvil únicamente para Admin */}
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
      <div className={`flex min-h-screen flex-col ${isClient ? 'lg:pl-56' : 'lg:pl-64'}`}>
        {isClient ? (
          <header className="bg-gradient-to-r from-[#1E1B4B] via-[#312E81] to-[#6D28D9] px-4 py-4 text-white sm:px-6 lg:px-8">
            <div className="mx-auto flex max-w-6xl items-center justify-between gap-3">
              <div>
                <p className="text-[12px] font-normal text-indigo-200 lg:hidden">AutoClean</p>
                <p className="text-[20px] font-semibold tracking-tight text-white">
                  {firstName ? `Hola, ${firstName}` : 'Hola'}
                </p>
              </div>

              <div className="flex items-center gap-2.5">
                <button
                  type="button"
                  onClick={handleLogout}
                  className="inline-flex h-9 items-center gap-1.5 rounded-xl bg-white/15 px-3 text-xs font-semibold text-white transition hover:bg-white/25 lg:hidden"
                >
                  <NavIcon name="logout" className="h-4 w-4" />
                  <span>Salir</span>
                </button>
                <span
                  title={displayName || 'Cliente'}
                  className="flex h-9 w-9 items-center justify-center rounded-full border border-white/25 bg-white/15 text-xs font-semibold text-white"
                >
                  {initials}
                </span>
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
                  <NavIcon name="menu" />
                </button>
                <form onSubmit={handleGlobalSearch} className="relative w-full max-w-md">
                  <span className="pointer-events-none absolute left-3.5 top-2.5 text-[#5B5F7A]">
                    <NavIcon name="search" className="h-5 w-5" />
                  </span>
                  <input
                    type="search"
                    value={globalQuery}
                    onChange={(e) => setGlobalQuery(e.target.value)}
                    placeholder="Buscar placa (ABC123) o módulo..."
                    className="h-10 w-full rounded-xl border border-[#E4E6F2] bg-[#F5F6FB] pl-11 pr-4 text-sm text-[#1E1B4B] placeholder-[#5B5F7A]/70 outline-none focus:border-[#4F46E5] focus:bg-white focus:ring-2 focus:ring-[#4F46E5]/15"
                  />
                </form>
              </div>

              {/* Header Admin limpio: solo campana de alertas reales y avatar pequeño */}
              <div className="flex items-center gap-2.5">
                <div ref={alertsRef} className="relative">
                  <button
                    type="button"
                    onClick={() => {
                      const nextOpen = !alertsOpen
                      setAlertsOpen(nextOpen)
                      if (nextOpen) {
                        loadOperationalAlerts()
                      }
                    }}
                    aria-label="Notificaciones operativas"
                    aria-expanded={alertsOpen}
                    className="relative flex h-10 w-10 items-center justify-center rounded-xl border border-[#E4E6F2] bg-white text-[#5B5F7A] transition hover:bg-[#F5F6FB] hover:text-[#1E1B4B]"
                  >
                    <NavIcon name="bell" />
                    {totalAlerts > 0 && (
                      <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-[#4F46E5] px-1 text-[10px] font-bold text-white">
                        {totalAlerts > 9 ? '9+' : totalAlerts}
                      </span>
                    )}
                  </button>

                  {alertsOpen && (
                    <div className="absolute right-0 z-40 mt-2 w-80 overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-float">
                      <div className="flex items-center justify-between border-b border-[#E4E6F2] px-4 py-3">
                        <p className="text-xs font-semibold tracking-wider text-[#1E1B4B] uppercase">
                          Alertas operativas
                        </p>
                        <span className="rounded-full bg-[#F5F6FB] px-2 py-0.5 text-[11px] font-semibold text-[#5B5F7A]">
                          {totalAlerts} activas
                        </span>
                      </div>

                      <div className="max-h-80 divide-y divide-[#E4E6F2] overflow-y-auto">
                        {totalAlerts === 0 ? (
                          <div className="px-4 py-6 text-center text-xs text-[#5B5F7A]">
                            Todo al día: sin insumos bajos ni entregas pendientes.
                          </div>
                        ) : (
                          <>
                            {alertsData.lowSupplies.map((supply) => (
                              <button
                                key={`supply-${supply.id}`}
                                type="button"
                                onClick={() => {
                                  setAlertsOpen(false)
                                  navigate('/supplies')
                                }}
                                className="flex w-full items-start justify-between gap-2 px-4 py-3 text-left transition hover:bg-[#F5F6FB]"
                              >
                                <div>
                                  <p className="text-xs font-semibold text-[#1E1B4B]">
                                    Stock bajo: {supply.name}
                                  </p>
                                  <p className="mt-0.5 text-[11px] text-[#5B5F7A]">
                                    Quedan {supply.stock} {supply.unit} en inventario
                                  </p>
                                </div>
                                <span className="shrink-0 rounded-full bg-[#FEE2E2] px-2 py-0.5 text-[10px] font-semibold text-[#B91C1C]">
                                  Insumos
                                </span>
                              </button>
                            ))}

                            {alertsData.readyVehicles.map((item) => (
                              <button
                                key={`ready-${item.id}`}
                                type="button"
                                onClick={() => {
                                  setAlertsOpen(false)
                                  navigate('/appointments')
                                }}
                                className="flex w-full items-start justify-between gap-2 px-4 py-3 text-left transition hover:bg-[#F5F6FB]"
                              >
                                <div>
                                  <p className="text-xs font-semibold text-[#1E1B4B]">
                                    Listo para entrega: {item.vehiclePlate}
                                  </p>
                                  <p className="mt-0.5 text-[11px] text-[#5B5F7A]">
                                    {item.customerName} · {item.serviceTypeName}
                                  </p>
                                </div>
                                <span className="shrink-0 rounded-full bg-[#DCFCE7] px-2 py-0.5 text-[10px] font-semibold text-[#15803D]">
                                  Listo
                                </span>
                              </button>
                            ))}

                            {alertsData.pendingToday.map((item) => (
                              <button
                                key={`pending-${item.id}`}
                                type="button"
                                onClick={() => {
                                  setAlertsOpen(false)
                                  navigate('/appointments')
                                }}
                                className="flex w-full items-start justify-between gap-2 px-4 py-3 text-left transition hover:bg-[#F5F6FB]"
                              >
                                <div>
                                  <p className="text-xs font-semibold text-[#1E1B4B]">
                                    Cita pendiente hoy: {item.vehiclePlate}
                                  </p>
                                  <p className="mt-0.5 text-[11px] text-[#5B5F7A]">
                                    {item.serviceTypeName} · {item.employeeName}
                                  </p>
                                </div>
                                <span className="shrink-0 rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-semibold text-amber-700">
                                  Hoy
                                </span>
                              </button>
                            ))}
                          </>
                        )}
                      </div>

                      <div className="border-t border-[#E4E6F2] bg-[#F5F6FB] px-4 py-2 text-[11px] text-[#5B5F7A]">
                        Horario: 7:00 a. m. – 12:00 p. m. y 2:30 p. m. – 9:00 p. m.
                      </div>
                    </div>
                  )}
                </div>
                <span
                  title={displayName || roleLabel}
                  className="flex h-9 w-9 items-center justify-center rounded-full bg-[#4F46E5] text-xs font-semibold text-white"
                >
                  {initials}
                </span>
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

      {/* Barra inferior fija en móvil para Portal Cliente (sin sidebar móvil, Agendar elevado en violeta) */}
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
                    isActive ? 'text-[#6D28D9]' : 'text-[#5B5F7A]'
                  }`
                }
              >
                <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#8B5CF6] text-white shadow-float transition hover:bg-[#7C3AED]">
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
