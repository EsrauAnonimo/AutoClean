import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'
import { Button } from '../components/Button'
import { MainLayout } from '../components/MainLayout'
import { VehicleStatusBadge } from '../components/VehicleStatusBadge'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { getAppointments } from '../services/appointmentService'
import { getServiceTypes } from '../services/catalogService'
import { getSupplies } from '../services/supplyService'

function formatCop(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(Number(value) || 0)
}

function getLocalDateKey(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatTime(value) {
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '—'
  return new Intl.DateTimeFormat('es-CO', { timeStyle: 'short' }).format(d)
}

/**
 * Dashboard Ejecutivo Admin con KPIs del día, gráficas semanales reales y próximas citas.
 */
export function DashboardPage() {
  const { user } = useAuth()
  const [selectedDate, setSelectedDate] = useState(() => getLocalDateKey(new Date()))
  const [appointments, setAppointments] = useState([])
  const [services, setServices] = useState([])
  const [supplies, setSupplies] = useState([])
  const [loading, setLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  const loadDashboard = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const [appointmentList, serviceList, supplyList] = await Promise.all([
        getAppointments(),
        getServiceTypes(),
        getSupplies(),
      ])
      setAppointments(Array.isArray(appointmentList) ? appointmentList : [])
      setServices(Array.isArray(serviceList) ? serviceList : [])
      setSupplies(Array.isArray(supplyList) ? supplyList : [])
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar el resumen del día.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadDashboard()
  }, [loadDashboard])

  const priceByServiceId = useMemo(() => {
    const map = new Map()
    for (const srv of services) {
      map.set(srv.id, Number(srv.price) || 0)
    }
    return map
  }, [services])

  const metrics = useMemo(() => {
    const dayItems = appointments.filter((item) => getLocalDateKey(item.dateTime) === selectedDate)
    const prevDateObj = new Date(`${selectedDate}T12:00:00`)
    prevDateObj.setDate(prevDateObj.getDate() - 1)
    const prevKey = getLocalDateKey(prevDateObj)
    const prevItems = appointments.filter((item) => getLocalDateKey(item.dateTime) === prevKey)

    const validDayItems = dayItems.filter((item) => item.status !== 'CANCELLED')
    const validPrevItems = prevItems.filter((item) => item.status !== 'CANCELLED')

    const dayRevenue = validDayItems.reduce(
      (sum, item) => sum + (priceByServiceId.get(item.serviceTypeId) || 0),
      0,
    )
    const prevRevenue = validPrevItems.reduce(
      (sum, item) => sum + (priceByServiceId.get(item.serviceTypeId) || 0),
      0,
    )

    const inProcess = appointments.filter((item) => item.status === 'IN_PROGRESS')
    const washingCount = inProcess.filter((item) => item.vehicleStatus === 'WASHING').length
    const detailingCount = inProcess.filter((item) => item.vehicleStatus === 'DETAILING').length
    const readyCount = appointments.filter(
      (item) => item.vehicleStatus === 'READY' && item.status !== 'CANCELLED',
    ).length

    const serviceCounts = new Map()
    for (const item of appointments.filter((a) => a.status !== 'CANCELLED')) {
      const name = item.serviceTypeName || 'Servicio'
      serviceCounts.set(name, (serviceCounts.get(name) || 0) + 1)
    }
    const sortedServices = [...serviceCounts.entries()].sort((a, b) => b[1] - a[1])
    const topService = sortedServices[0] || ['Sin datos', 0]
    const totalServicesCount = sortedServices.reduce((acc, [, c]) => acc + c, 0) || 1

    const weekly = []
    for (let offset = 6; offset >= 0; offset -= 1) {
      const date = new Date(`${selectedDate}T12:00:00`)
      date.setDate(date.getDate() - offset)
      const key = getLocalDateKey(date)
      const dayLabel = new Intl.DateTimeFormat('es-CO', { weekday: 'short' }).format(date)
      const total = appointments
        .filter((a) => a.status !== 'CANCELLED' && getLocalDateKey(a.dateTime) === key)
        .reduce((sum, a) => sum + (priceByServiceId.get(a.serviceTypeId) || 0), 0)
      weekly.push({ key, label: dayLabel, total })
    }
    const maxWeek = Math.max(...weekly.map((d) => d.total), 1)

    const upcoming = appointments
      .filter((item) => item.status === 'PENDING' || item.status === 'IN_PROGRESS')
      .sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime())
      .slice(0, 5)

    const lowSupplies = supplies.filter((s) => Number(s.stock) <= 5)

    return {
      dayCount: dayItems.length,
      countDiff: dayItems.length - prevItems.length,
      dayRevenue,
      revenueDiff: dayRevenue - prevRevenue,
      inProcessCount: inProcess.length,
      inProcessBreakdown: `${washingCount} lavando · ${detailingCount} detallando · ${readyCount} listos`,
      topServiceName: topService[0],
      topServiceCount: topService[1],
      serviceBreakdown: sortedServices.slice(0, 4).map(([name, count]) => ({
        name,
        count,
        pct: Math.round((count * 100) / totalServicesCount),
      })),
      weekly,
      maxWeek,
      upcoming,
      lowSupplies,
    }
  }, [appointments, priceByServiceId, selectedDate, supplies])

  const displayName = user?.email ? user.email.split('@')[0] : 'Equipo'

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Cabecera saludo + selector de fecha */}
        <section className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[28px] leading-[34px] font-bold tracking-tight text-[#1E1B4B]">
              Buenos días, {displayName}
            </h1>
            <p className="mt-1 text-[15px] text-[#5B5F7A]">
              Resumen operativo del centro de lavado y detailing
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2.5">
            <input
              type="date"
              aria-label="Fecha del resumen"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="h-[44px] rounded-xl border border-[#E4E6F2] bg-white px-3.5 text-sm font-medium text-[#1E1B4B] outline-none focus:border-[#4F46E5] focus:ring-4 focus:ring-[#4F46E5]/15"
            />
            <Link
              to="/appointments"
              className="inline-flex h-[44px] items-center justify-center rounded-xl bg-[#4F46E5] px-4 text-sm font-semibold text-white transition hover:bg-[#4338CA]"
            >
              Abrir Agenda
            </Link>
          </div>
        </section>

        {errorMessage && (
          <div
            role="alert"
            className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#FECACA] bg-[#FEE2E2] px-5 py-4 text-sm text-[#B91C1C]"
          >
            <span>{errorMessage}</span>
            <Button variant="secondary" onClick={loadDashboard}>
              Reintentar
            </Button>
          </div>
        )}

        {/* Alerta de inventario bajo si aplica */}
        {!loading && metrics.lowSupplies.length > 0 && (
          <div className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#F59E0B]/30 bg-[#FEF3C7] px-5 py-3.5 text-sm text-[#B45309]">
            <span>
              <strong>Alerta de inventario:</strong> {metrics.lowSupplies.length} insumo(s) con
              stock bajo ({metrics.lowSupplies.map((s) => s.name).join(', ')}).
            </span>
            <Link to="/supplies" className="font-semibold underline hover:text-[#92400E]">
              Revisar insumos →
            </Link>
          </div>
        )}

        {/* 4 KPIs con variación vs ayer */}
        {loading ? (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {[1, 2, 3, 4].map((n) => (
              <div key={n} className="h-32 animate-pulse rounded-2xl border border-[#E4E6F2] bg-white p-5" />
            ))}
          </div>
        ) : (
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <div className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
              <p className="text-[13px] font-medium text-[#5B5F7A]">Citas del día</p>
              <p className="mt-2 text-[28px] font-bold text-[#1E1B4B] tabular-nums">
                {metrics.dayCount}
              </p>
              <p
                className={`mt-1 text-[13px] font-semibold ${
                  metrics.countDiff >= 0 ? 'text-[#15803D]' : 'text-[#B91C1C]'
                }`}
              >
                {metrics.countDiff >= 0 ? `+${metrics.countDiff}` : metrics.countDiff} vs. ayer
              </p>
            </div>

            <div className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
              <p className="text-[13px] font-medium text-[#5B5F7A]">Ingresos estimados hoy</p>
              <p className="mt-2 text-[28px] font-bold text-[#1E1B4B] tabular-nums">
                {formatCop(metrics.dayRevenue)}
              </p>
              <p
                className={`mt-1 text-[13px] font-semibold tabular-nums ${
                  metrics.revenueDiff >= 0 ? 'text-[#15803D]' : 'text-[#B91C1C]'
                }`}
              >
                {metrics.revenueDiff >= 0
                  ? `+${formatCop(metrics.revenueDiff)}`
                  : formatCop(metrics.revenueDiff)}{' '}
                vs. ayer
              </p>
            </div>

            <div className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
              <p className="text-[13px] font-medium text-[#5B5F7A]">Vehículos en proceso</p>
              <p className="mt-2 text-[28px] font-bold text-[#6D28D9] tabular-nums">
                {metrics.inProcessCount}
              </p>
              <p className="mt-1 text-[13px] text-[#5B5F7A]">{metrics.inProcessBreakdown}</p>
            </div>

            <div className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
              <p className="text-[13px] font-medium text-[#5B5F7A]">Servicio más pedido</p>
              <p className="mt-2 truncate text-[20px] font-bold text-[#1E1B4B]">
                {metrics.topServiceName}
              </p>
              <p className="mt-1 text-[13px] font-semibold text-[#4F46E5] tabular-nums">
                {metrics.topServiceCount} citas registradas
              </p>
            </div>
          </section>
        )}

        {/* Gráficas: Ingresos de la semana + Servicios por tipo */}
        <section className="grid gap-6 lg:grid-cols-12">
          <div className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card lg:col-span-7">
            <div className="flex items-center justify-between">
              <h2 className="text-[18px] font-semibold text-[#1E1B4B]">Ingresos últimos 7 días</h2>
              <span className="text-[13px] font-medium text-[#5B5F7A]">Cifras en COP</span>
            </div>
            <div className="mt-6 flex h-48 items-end gap-3">
              {metrics.weekly.map((day) => {
                const heightPct = Math.max(8, Math.round((day.total * 100) / metrics.maxWeek))
                const isSelected = day.key === selectedDate
                return (
                  <div key={day.key} className="flex flex-1 flex-col items-center gap-2">
                    <span className="text-[11px] font-semibold text-[#5B5F7A] tabular-nums">
                      {day.total > 0 ? `$${Math.round(day.total / 1000)}k` : '$0'}
                    </span>
                    <div className="flex h-32 w-full items-end">
                      <div
                        style={{ height: `${heightPct}%` }}
                        className={`w-full rounded-t-xl transition-all ${
                          isSelected
                            ? 'bg-gradient-to-t from-[#4F46E5] to-[#8B5CF6]'
                            : 'bg-[#4F46E5]/25 hover:bg-[#4F46E5]/50'
                        }`}
                      />
                    </div>
                    <span
                      className={`text-xs uppercase ${
                        isSelected ? 'font-bold text-[#4F46E5]' : 'font-medium text-[#5B5F7A]'
                      }`}
                    >
                      {day.label}
                    </span>
                  </div>
                )
              })}
            </div>
          </div>

          <div className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card lg:col-span-5">
            <h2 className="text-[18px] font-semibold text-[#1E1B4B]">Servicios por tipo</h2>
            {metrics.serviceBreakdown.length === 0 ? (
              <p className="mt-6 text-sm text-[#5B5F7A]">Aún no hay citas registradas.</p>
            ) : (
              <div className="mt-5 space-y-4">
                {metrics.serviceBreakdown.map((srv) => (
                  <div key={srv.name} className="space-y-1.5">
                    <div className="flex items-center justify-between text-[14px]">
                      <span className="font-medium text-[#1E1B4B]">{srv.name}</span>
                      <span className="font-semibold text-[#5B5F7A] tabular-nums">
                        {srv.count} ({srv.pct}%)
                      </span>
                    </div>
                    <div className="h-2.5 overflow-hidden rounded-full bg-[#F5F6FB]">
                      <div
                        style={{ width: `${srv.pct}%` }}
                        className="h-full rounded-full bg-gradient-to-r from-[#4F46E5] to-[#8B5CF6]"
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        {/* Próximas citas */}
        <section className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-[18px] font-semibold text-[#1E1B4B]">Próximas citas activas</h2>
            <Link to="/appointments" className="text-sm font-semibold text-[#4F46E5] hover:text-[#4338CA]">
              Ver tablero completo →
            </Link>
          </div>

          {metrics.upcoming.length === 0 ? (
            <div className="rounded-xl border border-dashed border-[#E4E6F2] p-8 text-center">
              <p className="text-[15px] font-semibold text-[#1E1B4B]">Sin citas pendientes por atender</p>
              <p className="mt-1 text-sm text-[#5B5F7A]">
                Cuando ingresen nuevas reservas aparecerán aquí automáticamente.
              </p>
            </div>
          ) : (
            <div className="space-y-2.5">
              {metrics.upcoming.map((item) => (
                <div
                  key={item.id}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#E4E6F2] p-3.5 transition hover:bg-[#F5F6FB]/70"
                >
                  <div className="flex flex-wrap items-center gap-3">
                    <span className="rounded-lg border border-[#1E1B4B] bg-[#FEF08A] px-2.5 py-0.5 font-mono text-xs font-extrabold text-[#1E1B4B]">
                      {item.vehiclePlate || '---'}
                    </span>
                    <div>
                      <p className="text-[14px] font-semibold text-[#1E1B4B]">
                        {item.customerName} · {item.serviceTypeName}
                      </p>
                      <p className="text-[12px] text-[#5B5F7A]">
                        {formatTime(item.dateTime)} · Especialista: {item.employeeName}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <VehicleStatusBadge status={item.vehicleStatus} />
                    <AppointmentStatusBadge status={item.status} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </MainLayout>
  )
}
