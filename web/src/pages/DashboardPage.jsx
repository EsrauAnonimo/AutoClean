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

const PERIOD_OPTIONS = [
  { key: 'today', label: 'Hoy' },
  { key: 'week', label: 'Semana' },
  { key: 'month', label: 'Mes' },
]

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

function capitalizeName(user) {
  const raw = user?.name?.trim() || (user?.email ? user.email.split('@')[0] : '')
  if (!raw) return 'Equipo'
  const clean = raw.replace(/[._-]+/g, ' ').replace(/\d+/g, '').trim()
  const firstWord = clean.split(/\s+/)[0] || 'Equipo'
  return firstWord.charAt(0).toUpperCase() + firstWord.slice(1).toLowerCase()
}

function getPeriodBounds(periodKey, now) {
  const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999)

  if (periodKey === 'week') {
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 6, 0, 0, 0, 0)
    const prevEnd = new Date(start.getTime() - 1)
    const prevStart = new Date(prevEnd.getFullYear(), prevEnd.getMonth(), prevEnd.getDate() - 6, 0, 0, 0, 0)
    return { start, end, prevStart, prevEnd, comparisonLabel: 'vs. semana ant.' }
  }

  if (periodKey === 'month') {
    const start = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0)
    const prevStart = new Date(now.getFullYear(), now.getMonth() - 1, 1, 0, 0, 0, 0)
    const prevEnd = new Date(start.getTime() - 1)
    return { start, end, prevStart, prevEnd, comparisonLabel: 'vs. mes ant.' }
  }

  const start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0)
  const prevStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1, 0, 0, 0, 0)
  const prevEnd = new Date(start.getTime() - 1)
  return { start, end, prevStart, prevEnd, comparisonLabel: 'vs. ayer' }
}

/**
 * Dashboard Ejecutivo Admin con KPIs con íconos suaves, control segmentado (Hoy/Semana/Mes)
 * y estados vacíos claros para gráficas sin registros.
 */
export function DashboardPage() {
  const { user } = useAuth()
  const [referenceDate] = useState(() => new Date())
  const [periodMode, setPeriodMode] = useState('today')
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
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar el resumen operativo.'))
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
    const { start, end, prevStart, prevEnd, comparisonLabel } = getPeriodBounds(
      periodMode,
      referenceDate,
    )
    const todayKey = getLocalDateKey(referenceDate)

    const inRange = (isoDate, rangeStart, rangeEnd) => {
      const d = new Date(isoDate)
      return !Number.isNaN(d.getTime()) && d >= rangeStart && d <= rangeEnd
    }

    const periodItems = appointments.filter((item) => inRange(item.dateTime, start, end))
    const prevItems = appointments.filter((item) => inRange(item.dateTime, prevStart, prevEnd))

    const validPeriodItems = periodItems.filter((item) => item.status !== 'CANCELLED')
    const validPrevItems = prevItems.filter((item) => item.status !== 'CANCELLED')

    const periodRevenue = validPeriodItems.reduce(
      (sum, item) => sum + (priceByServiceId.get(item.serviceTypeId) || 0),
      0,
    )
    const prevRevenue = validPrevItems.reduce(
      (sum, item) => sum + (priceByServiceId.get(item.serviceTypeId) || 0),
      0,
    )

    const inProcess = appointments.filter((item) => item.status === 'IN_PROGRESS')
    const readyCount = appointments.filter(
      (item) => item.vehicleStatus === 'READY' && item.status !== 'CANCELLED',
    ).length

    const serviceCounts = new Map()
    for (const item of validPeriodItems) {
      const name = item.serviceTypeName || item.serviceName || 'Servicio'
      serviceCounts.set(name, (serviceCounts.get(name) || 0) + 1)
    }
    const sortedServices = [...serviceCounts.entries()].sort((a, b) => b[1] - a[1])
    const topService = sortedServices[0] || ['Sin registros', 0]
    const totalServicesCount = sortedServices.reduce((acc, [, c]) => acc + c, 0) || 1

    const weekly = []
    for (let offset = 6; offset >= 0; offset -= 1) {
      const date = new Date(referenceDate)
      date.setDate(date.getDate() - offset)
      const key = getLocalDateKey(date)
      const dayLabel = new Intl.DateTimeFormat('es-CO', { weekday: 'short' }).format(date)
      const total = appointments
        .filter((a) => a.status !== 'CANCELLED' && getLocalDateKey(a.dateTime) === key)
        .reduce((sum, a) => sum + (priceByServiceId.get(a.serviceTypeId) || 0), 0)
      weekly.push({ key, label: dayLabel, total })
    }
    const hasWeeklyRevenue = weekly.some((d) => d.total > 0)
    const maxWeek = Math.max(...weekly.map((d) => d.total), 1)

    const upcoming = appointments
      .filter((item) => item.status === 'PENDING' || item.status === 'IN_PROGRESS')
      .sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime())
      .slice(0, 5)

    const lowSupplies = supplies.filter((s) => Number(s.stock) <= 5)

    return {
      todayKey,
      comparisonLabel,
      periodCount: periodItems.length,
      countDiff: periodItems.length - prevItems.length,
      periodRevenue,
      revenueDiff: periodRevenue - prevRevenue,
      inProcessCount: inProcess.length,
      readyCount,
      topServiceName: topService[0],
      topServiceCount: topService[1],
      serviceBreakdown: sortedServices.slice(0, 4).map(([name, count]) => ({
        name,
        count,
        pct: Math.round((count * 100) / totalServicesCount),
      })),
      weekly,
      hasWeeklyRevenue,
      maxWeek,
      upcoming,
      lowSupplies,
    }
  }, [appointments, periodMode, priceByServiceId, referenceDate, supplies])

  const displayName = capitalizeName(user)

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Cabecera saludo + control segmentado + botón principal + Nueva cita */}
        <section className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[28px] leading-[34px] font-semibold tracking-tight text-[#1E1B4B]">
              Buenos días, {displayName}
            </h1>
            <p className="mt-1 text-[15px] font-normal text-[#5B5F7A]">
              Resumen operativo del centro de lavado y detailing
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <div
              role="group"
              aria-label="Selector de periodo"
              className="inline-flex h-[44px] items-center rounded-xl border border-[#E4E6F2] bg-white p-1"
            >
              {PERIOD_OPTIONS.map((option) => {
                const active = periodMode === option.key
                return (
                  <button
                    key={option.key}
                    type="button"
                    onClick={() => setPeriodMode(option.key)}
                    className={`h-full rounded-lg px-3.5 text-xs font-semibold transition ${
                      active
                        ? 'bg-[#4F46E5] text-white'
                        : 'text-[#5B5F7A] hover:bg-[#F5F6FB] hover:text-[#1E1B4B]'
                    }`}
                  >
                    {option.label}
                  </button>
                )
              })}
            </div>

            <Link
              to="/appointments?new=1"
              className="inline-flex h-[44px] items-center justify-center gap-2 rounded-xl bg-[#4F46E5] px-4 text-sm font-semibold text-white transition hover:bg-[#4338CA]"
            >
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              + Nueva cita
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

        {!loading && metrics.lowSupplies.length > 0 && (
          <div className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#F59E0B]/30 bg-[#FEF3C7] px-5 py-3.5 text-sm text-[#B45309]">
            <span>
              <strong className="font-semibold">Alerta de inventario:</strong>{' '}
              {metrics.lowSupplies.length} insumo(s) con stock bajo (
              {metrics.lowSupplies.map((s) => s.name).join(', ')}).
            </span>
            <Link to="/supplies" className="font-semibold underline hover:text-[#92400E]">
              Revisar insumos →
            </Link>
          </div>
        )}

        {/* 4 tarjetas KPI: cuadro 40px con fondo suave (violeta, verde, ámbar, azul), cifra 32px/600 y badge variación */}
        {loading ? (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {[1, 2, 3, 4].map((n) => (
              <div key={n} className="h-36 animate-pulse rounded-2xl border border-[#E4E6F2] bg-white p-5" />
            ))}
          </div>
        ) : (
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {/* KPI 1: Violeta */}
            <article className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
              <div className="flex items-center justify-between gap-3">
                <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#EDE9FE] text-[#6D28D9]">
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                    <path d="M8 2v3m8-3v3M3 9h18M5 5h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z" />
                  </svg>
                </span>
                <span
                  className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold tabular-nums ${
                    metrics.countDiff >= 0
                      ? 'bg-[#DCFCE7] text-[#15803D]'
                      : 'bg-[#FEE2E2] text-[#B91C1C]'
                  }`}
                >
                  {metrics.countDiff >= 0 ? `+${metrics.countDiff}` : metrics.countDiff}{' '}
                  {metrics.comparisonLabel}
                </span>
              </div>
              <p className="mt-4 text-[13px] font-normal text-[#5B5F7A]">Citas del periodo</p>
              <p className="mt-1 text-[32px] leading-none font-semibold text-[#1E1B4B] tabular-nums">
                {metrics.periodCount}
              </p>
            </article>

            {/* KPI 2: Verde */}
            <article className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
              <div className="flex items-center justify-between gap-3">
                <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#DCFCE7] text-[#15803D]">
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                    <path d="M12 2v20m5-16H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
                  </svg>
                </span>
                <span
                  className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold tabular-nums ${
                    metrics.revenueDiff >= 0
                      ? 'bg-[#DCFCE7] text-[#15803D]'
                      : 'bg-[#FEE2E2] text-[#B91C1C]'
                  }`}
                >
                  {metrics.revenueDiff >= 0 ? '+' : ''}
                  {formatCop(metrics.revenueDiff)}
                </span>
              </div>
              <p className="mt-4 text-[13px] font-normal text-[#5B5F7A]">Ingresos estimados</p>
              <p className="mt-1 text-[32px] leading-none font-semibold text-[#1E1B4B] tabular-nums">
                {formatCop(metrics.periodRevenue)}
              </p>
            </article>

            {/* KPI 3: Ámbar */}
            <article className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
              <div className="flex items-center justify-between gap-3">
                <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#FEF3C7] text-[#B45309]">
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                    <path d="M5 11l1.5-4.5A2 2 0 0 1 8.4 5h7.2a2 2 0 0 1 1.9 1.5L19 11m-14 0h14v6a1 1 0 0 1-1 1h-1a1 1 0 0 1-1-1v-1H8v1a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1v-6Z" />
                  </svg>
                </span>
                <span
                  className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold tabular-nums ${
                    metrics.readyCount >= 0
                      ? 'bg-[#DCFCE7] text-[#15803D]'
                      : 'bg-[#FEE2E2] text-[#B91C1C]'
                  }`}
                >
                  {metrics.readyCount} listos
                </span>
              </div>
              <p className="mt-4 text-[13px] font-normal text-[#5B5F7A]">Vehículos en proceso</p>
              <p className="mt-1 text-[32px] leading-none font-semibold text-[#1E1B4B] tabular-nums">
                {metrics.inProcessCount}
              </p>
            </article>

            {/* KPI 4: Azul */}
            <article className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
              <div className="flex items-center justify-between gap-3">
                <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#DBEAFE] text-[#1D4ED8]">
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                    <path d="M12 3l1.8 4.2L18 9l-4.2 1.8L12 15l-1.8-4.2L6 9l4.2-1.8L12 3Z" />
                  </svg>
                </span>
                <span
                  className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold tabular-nums ${
                    metrics.topServiceCount > 0
                      ? 'bg-[#DCFCE7] text-[#15803D]'
                      : 'bg-[#FEE2E2] text-[#B91C1C]'
                  }`}
                >
                  {metrics.topServiceCount} citas
                </span>
              </div>
              <p className="mt-4 text-[13px] font-normal text-[#5B5F7A]">Servicio más pedido</p>
              <p
                title={metrics.topServiceName}
                className="mt-1 truncate text-[32px] leading-none font-semibold text-[#1E1B4B]"
              >
                {metrics.topServiceCount > 0 ? metrics.topServiceCount : '0'}
              </p>
              <p className="mt-1.5 truncate text-xs font-normal text-[#5B5F7A]">
                {metrics.topServiceName}
              </p>
            </article>
          </section>
        )}

        {/* Gráficas con estados vacíos claros */}
        <section className="grid gap-6 lg:grid-cols-12">
          <div className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card lg:col-span-7">
            <div className="flex items-center justify-between">
              <h2 className="text-[18px] font-semibold text-[#1E1B4B]">Ingresos últimos 7 días</h2>
              <span className="text-[13px] font-normal text-[#5B5F7A]">COP</span>
            </div>

            {!metrics.hasWeeklyRevenue ? (
              <div className="mt-6 flex h-48 flex-col items-center justify-center rounded-xl border border-dashed border-[#E4E6F2] bg-[#F5F6FB]/50 px-6 text-center">
                <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-white text-[#5B5F7A] shadow-xs">
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                    <path d="M4 20h16M7 16v-5m5 5V7m5 9v-8" />
                  </svg>
                </span>
                <p className="mt-3 text-sm font-semibold text-[#1E1B4B]">
                  Aún no hay ingresos esta semana
                </p>
                <p className="mt-1 text-xs font-normal text-[#5B5F7A]">
                  Los ingresos diarios aparecerán aquí cuando se registren servicios.
                </p>
              </div>
            ) : (
              <div className="mt-6 flex h-48 items-end gap-3">
                {metrics.weekly.map((day) => {
                  const heightPct = day.total > 0 ? Math.max(12, Math.round((day.total * 100) / metrics.maxWeek)) : 6
                  const isToday = day.key === metrics.todayKey
                  return (
                    <div key={day.key} className="flex flex-1 flex-col items-center gap-2">
                      <span className="text-[11px] font-semibold text-[#5B5F7A] tabular-nums">
                        {day.total > 0 ? `$${Math.round(day.total / 1000)}k` : ''}
                      </span>
                      <div className="flex h-32 w-full items-end">
                        <div
                          style={{ height: `${heightPct}%` }}
                          className={`w-full rounded-t-xl transition-all ${
                            isToday
                              ? 'bg-[#4F46E5]'
                              : day.total > 0
                                ? 'bg-[#4F46E5]/30 hover:bg-[#4F46E5]/55'
                                : 'bg-[#E4E6F2]/60'
                          }`}
                        />
                      </div>
                      <span
                        className={`text-xs uppercase ${
                          isToday ? 'font-semibold text-[#4F46E5]' : 'font-normal text-[#5B5F7A]'
                        }`}
                      >
                        {day.label}
                      </span>
                    </div>
                  )
                })}
              </div>
            )}
          </div>

          <div className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card lg:col-span-5">
            <h2 className="text-[18px] font-semibold text-[#1E1B4B]">Servicios por tipo</h2>
            {metrics.serviceBreakdown.length === 0 ? (
              <div className="mt-6 flex h-48 flex-col items-center justify-center rounded-xl border border-dashed border-[#E4E6F2] bg-[#F5F6FB]/50 px-6 text-center">
                <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-white text-[#5B5F7A] shadow-xs">
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                    <path d="M12 3l1.8 4.2L18 9l-4.2 1.8L12 15l-1.8-4.2L6 9l4.2-1.8L12 3Z" />
                  </svg>
                </span>
                <p className="mt-3 text-sm font-semibold text-[#1E1B4B]">
                  Aún no hay servicios en este periodo
                </p>
                <Link
                  to="/appointments?new=1"
                  className="mt-4 inline-flex h-[44px] items-center justify-center rounded-xl bg-[#4F46E5] px-4 text-xs font-semibold text-white transition hover:bg-[#4338CA]"
                >
                  Crear primera cita
                </Link>
              </div>
            ) : (
              <div className="mt-5 space-y-4">
                {metrics.serviceBreakdown.map((srv) => (
                  <div key={srv.name} className="space-y-1.5">
                    <div className="flex items-center justify-between text-[14px]">
                      <span className="font-semibold text-[#1E1B4B]">{srv.name}</span>
                      <span className="font-semibold text-[#5B5F7A] tabular-nums">
                        {srv.count} ({srv.pct}%)
                      </span>
                    </div>
                    <div className="h-2.5 overflow-hidden rounded-full bg-[#F5F6FB]">
                      <div
                        style={{ width: `${srv.pct}%` }}
                        className="h-full rounded-full bg-[#4F46E5]"
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
              <p className="mt-1 text-sm font-normal text-[#5B5F7A]">
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
                    <span className="rounded-lg border border-[#1E1B4B] bg-[#FEF08A] px-2.5 py-0.5 font-mono text-xs font-semibold text-[#1E1B4B]">
                      {item.vehiclePlate || '---'}
                    </span>
                    <div>
                      <p className="text-[14px] font-semibold text-[#1E1B4B]">
                        {item.customerName} · {item.serviceTypeName || item.serviceName}
                      </p>
                      <p className="text-[12px] font-normal text-[#5B5F7A]">
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
