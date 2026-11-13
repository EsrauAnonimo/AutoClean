import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Button } from '../components/Button'
import { CostingWorkspace } from '../components/CostingWorkspace'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { getApiErrorMessage } from '../services/api'
import { getRevenueReport, getServicesReport } from '../services/reportService'

function getDefaultRange() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return {
    from: `${year}-${month}-01`,
    to: `${year}-${month}-${day}`,
  }
}

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function formatMoney(value) {
  const numeric = Number(value)
  if (Number.isNaN(numeric)) return value || '—'
  return `$${numeric.toLocaleString('es-CO')}`
}

/**
 * Vista gerencial de reportes por periodo: servicios realizados e ingresos vs costos.
 */
export function ReportsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const activeTab = searchParams.get('vista') === 'costeo' ? 'costeo' : 'reports'

  const initialRange = useMemo(() => getDefaultRange(), [])
  const [from, setFrom] = useState(initialRange.from)
  const [to, setTo] = useState(initialRange.to)

  const [servicesReport, setServicesReport] = useState(null)
  const [revenueReport, setRevenueReport] = useState(null)

  const [loading, setLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  const fetchReports = useCallback(async (fromDate, toDate) => {
    setErrorMessage('')
    if (!fromDate || !toDate) {
      setErrorMessage('Selecciona las fechas de inicio y fin para consultar los reportes.')
      return
    }
    if (toDate < fromDate) {
      setErrorMessage('La fecha final no puede ser anterior a la fecha inicial.')
      return
    }

    setLoading(true)
    try {
      const [servicesData, revenueData] = await Promise.all([
        getServicesReport(fromDate, toDate),
        getRevenueReport(fromDate, toDate),
      ])
      setServicesReport(servicesData)
      setRevenueReport(revenueData)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible consultar los reportes.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let active = true
    Promise.all([
      getServicesReport(initialRange.from, initialRange.to),
      getRevenueReport(initialRange.from, initialRange.to),
    ])
      .then(([servicesData, revenueData]) => {
        if (!active) return
        setServicesReport(servicesData)
        setRevenueReport(revenueData)
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(getApiErrorMessage(error, 'No fue posible consultar los reportes.'))
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [initialRange.from, initialRange.to])

  const revenueTotals = useMemo(() => {
    const rows = revenueReport?.rows || []
    return rows.reduce(
      (acc, row) => ({
        price: acc.price + (Number(row[1]) || 0),
        cost: acc.cost + (Number(row[2]) || 0),
        difference: acc.difference + (Number(row[3]) || 0),
      }),
      { price: 0, cost: 0, difference: 0 },
    )
  }, [revenueReport])

  const handleSubmit = (event) => {
    event.preventDefault()
    fetchReports(from, to)
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[28px] font-bold tracking-tight text-[#1E1B4B]">
              {activeTab === 'costeo' ? 'Motor de costeo' : 'Reportes'}
            </h1>
            <p className="mt-1 text-sm text-[#5B5F7A]">
              {activeTab === 'costeo'
                ? 'Simula costos directos por servicio o cita y controla el margen de rentabilidad.'
                : 'Servicios atendidos y comparativa de ingresos vs costos por rango de fechas.'}
            </p>
          </div>

          <div
            role="tablist"
            aria-label="Cambiar entre reportes y costeo"
            className="inline-flex rounded-xl border border-[#E4E6F2] bg-white p-1 shadow-xs"
          >
            <button
              type="button"
              role="tab"
              aria-selected={activeTab === 'reports'}
              onClick={() => setSearchParams({})}
              className={`rounded-lg px-4 py-2 text-xs font-semibold transition ${
                activeTab === 'reports'
                  ? 'bg-[#4F46E5] text-white'
                  : 'text-[#5B5F7A] hover:text-[#1E1B4B]'
              }`}
            >
              Reportes
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={activeTab === 'costeo'}
              onClick={() => setSearchParams({ vista: 'costeo' })}
              className={`rounded-lg px-4 py-2 text-xs font-semibold transition ${
                activeTab === 'costeo'
                  ? 'bg-[#4F46E5] text-white'
                  : 'text-[#5B5F7A] hover:text-[#1E1B4B]'
              }`}
            >
              Costeo
            </button>
          </div>
        </div>

        {activeTab === 'costeo' ? (
          <CostingWorkspace onEstimateCreated={() => fetchReports(from, to)} />
        ) : (
          <>
            <section className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card">
          <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-3">
            <Input
              id="reportFrom"
              label="Desde"
              type="date"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              required
            />
            <Input
              id="reportTo"
              label="Hasta"
              type="date"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              required
            />
            <div className="flex items-end">
              <Button type="submit" loading={loading} className="w-full">
                Consultar reportes
              </Button>
            </div>
          </form>
        </section>

        {errorMessage && (
          <div
            role="alert"
            className="rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
          >
            {errorMessage}
          </div>
        )}

        <div className="grid gap-6 lg:grid-cols-2">
          <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card">
            <div className="border-b border-[#E4E6F2] px-6 py-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h2 className="text-[18px] font-semibold text-[#1E1B4B]">
                  {servicesReport?.title || 'Servicios por periodo'}
                </h2>
                <span className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-semibold tabular-nums text-[#4F46E5]">
                  {servicesReport?.rows?.length ?? 0} registros
                </span>
              </div>
              {servicesReport?.generatedAt && (
                <p className="mt-1 text-xs text-[#5B5F7A]">
                  Generado: {formatDateTime(servicesReport.generatedAt)}
                </p>
              )}
            </div>

            {loading ? (
              <div className="p-8 text-center text-sm text-[#5B5F7A]">Cargando servicios por periodo...</div>
            ) : !servicesReport?.rows?.length ? (
              <div className="p-8 text-center text-sm text-[#5B5F7A]">
                {servicesReport?.title || 'Sin datos en el periodo'}
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-[#E4E6F2] text-left text-sm">
                  <thead className="bg-[#F5F6FB] text-xs font-semibold tracking-wider text-[#5B5F7A] uppercase">
                    <tr>
                      <th className="px-4 py-3">Cliente</th>
                      <th className="px-4 py-3">Servicio</th>
                      <th className="px-4 py-3">Estado</th>
                      <th className="px-4 py-3">Fecha</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#E4E6F2] bg-white">
                    {servicesReport.rows.map((row, index) => (
                      <tr key={row[0] || index} className="hover:bg-[#F5F6FB]/60">
                        <td className="px-4 py-3 font-medium text-[#1E1B4B]">{row[1] || '—'}</td>
                        <td className="px-4 py-3 text-[#5B5F7A]">{row[2] || '—'}</td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <span className="inline-flex rounded-full bg-[#F5F6FB] px-2.5 py-0.5 text-xs font-semibold text-[#1E1B4B]">
                            {row[3] || '—'}
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-[#5B5F7A]">
                          {formatDateTime(row[4])}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card">
            <div className="border-b border-[#E4E6F2] px-6 py-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h2 className="text-[18px] font-semibold text-[#1E1B4B]">
                  {revenueReport?.title || 'Ingresos vs Costos'}
                </h2>
                <span className="rounded-full bg-[#DCFCE7] px-2.5 py-0.5 text-xs font-semibold tabular-nums text-[#15803D]">
                  {revenueReport?.rows?.length ?? 0} estimaciones
                </span>
              </div>
              {revenueReport?.generatedAt && (
                <p className="mt-1 text-xs text-[#5B5F7A]">
                  Generado: {formatDateTime(revenueReport.generatedAt)}
                </p>
              )}
            </div>

            {loading ? (
              <div className="p-8 text-center text-sm text-[#5B5F7A]">Cargando ingresos vs costos...</div>
            ) : !revenueReport?.rows?.length ? (
              <div className="p-8 text-center text-sm text-[#5B5F7A]">
                {revenueReport?.title || 'Sin datos en el periodo'}
              </div>
            ) : (
              <>
                <div className="grid grid-cols-3 gap-2 border-b border-[#E4E6F2] bg-[#F5F6FB]/70 px-6 py-3 text-xs">
                  <div>
                    <span className="block text-[#5B5F7A]">Ingresos</span>
                    <span className="font-bold tabular-nums text-[#1E1B4B]">
                      {formatMoney(revenueTotals.price)}
                    </span>
                  </div>
                  <div>
                    <span className="block text-[#5B5F7A]">Costos</span>
                    <span className="font-bold tabular-nums text-[#1E1B4B]">
                      {formatMoney(revenueTotals.cost)}
                    </span>
                  </div>
                  <div>
                    <span className="block text-[#5B5F7A]">Diferencia</span>
                    <span
                      className={`font-bold tabular-nums ${
                        revenueTotals.difference >= 0 ? 'text-[#15803D]' : 'text-[#B91C1C]'
                      }`}
                    >
                      {formatMoney(revenueTotals.difference)}
                    </span>
                  </div>
                </div>

                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-[#E4E6F2] text-left text-sm">
                    <thead className="bg-[#F5F6FB] text-xs font-semibold tracking-wider text-[#5B5F7A] uppercase">
                      <tr>
                        <th className="px-4 py-3">Servicio</th>
                        <th className="px-4 py-3">Ingreso</th>
                        <th className="px-4 py-3">Costo</th>
                        <th className="px-4 py-3">Diferencia</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#E4E6F2] bg-white">
                      {revenueReport.rows.map((row, index) => {
                        const diff = Number(row[3])
                        return (
                          <tr key={`${row[0]}-${index}`} className="hover:bg-[#F5F6FB]/60">
                            <td className="px-4 py-3 font-medium text-[#1E1B4B]">{row[0] || '—'}</td>
                            <td className="whitespace-nowrap px-4 py-3 tabular-nums text-[#5B5F7A]">
                              {formatMoney(row[1])}
                            </td>
                            <td className="whitespace-nowrap px-4 py-3 tabular-nums text-[#5B5F7A]">
                              {formatMoney(row[2])}
                            </td>
                            <td
                              className={`whitespace-nowrap px-4 py-3 font-semibold tabular-nums ${
                                !Number.isNaN(diff) && diff < 0 ? 'text-[#B91C1C]' : 'text-[#15803D]'
                              }`}
                            >
                              {formatMoney(row[3])}
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </section>
        </div>
          </>
        )}
      </div>
    </MainLayout>
  )
}
