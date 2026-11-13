import { useCallback, useEffect, useMemo, useState } from 'react'
import { Button } from '../components/Button'
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
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Reportes Gerenciales</h2>
          <p className="mt-1 text-sm text-slate-500">
            Consulta los servicios atendidos y la comparación de ingresos vs costos por rango de fechas.
          </p>
        </div>

        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
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
            className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
          >
            {errorMessage}
          </div>
        )}

        <div className="grid gap-6 lg:grid-cols-2">
          <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div className="border-b border-slate-200 px-6 py-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h3 className="text-base font-semibold text-slate-900">
                  {servicesReport?.title || 'Servicios por periodo'}
                </h3>
                <span className="rounded-full bg-sky-50 px-2.5 py-0.5 text-xs font-semibold text-sky-700">
                  {servicesReport?.rows?.length ?? 0} registros
                </span>
              </div>
              {servicesReport?.generatedAt && (
                <p className="mt-1 text-xs text-slate-500">
                  Generado: {formatDateTime(servicesReport.generatedAt)}
                </p>
              )}
            </div>

            {loading ? (
              <div className="p-8 text-center text-sm text-slate-500">Cargando servicios por periodo...</div>
            ) : !servicesReport?.rows?.length ? (
              <div className="p-8 text-center text-sm text-slate-500">
                {servicesReport?.title || 'Sin datos en el periodo'}
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
                  <thead className="bg-slate-50 text-xs font-semibold tracking-wider text-slate-600 uppercase">
                    <tr>
                      <th className="px-4 py-3">Cliente</th>
                      <th className="px-4 py-3">Servicio</th>
                      <th className="px-4 py-3">Estado</th>
                      <th className="px-4 py-3">Fecha</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 bg-white">
                    {servicesReport.rows.map((row, index) => (
                      <tr key={row[0] || index} className="hover:bg-slate-50/80">
                        <td className="px-4 py-3 font-medium text-slate-900">{row[1] || '—'}</td>
                        <td className="px-4 py-3 text-slate-700">{row[2] || '—'}</td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <span className="inline-flex rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-700">
                            {row[3] || '—'}
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                          {formatDateTime(row[4])}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div className="border-b border-slate-200 px-6 py-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h3 className="text-base font-semibold text-slate-900">
                  {revenueReport?.title || 'Ingresos vs Costos'}
                </h3>
                <span className="rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">
                  {revenueReport?.rows?.length ?? 0} estimaciones
                </span>
              </div>
              {revenueReport?.generatedAt && (
                <p className="mt-1 text-xs text-slate-500">
                  Generado: {formatDateTime(revenueReport.generatedAt)}
                </p>
              )}
            </div>

            {loading ? (
              <div className="p-8 text-center text-sm text-slate-500">Cargando ingresos vs costos...</div>
            ) : !revenueReport?.rows?.length ? (
              <div className="p-8 text-center text-sm text-slate-500">
                {revenueReport?.title || 'Sin datos en el periodo'}
              </div>
            ) : (
              <>
                <div className="grid grid-cols-3 gap-2 border-b border-slate-200 bg-slate-50/70 px-6 py-3 text-xs">
                  <div>
                    <span className="block text-slate-500">Ingresos</span>
                    <span className="font-semibold text-slate-900">{formatMoney(revenueTotals.price)}</span>
                  </div>
                  <div>
                    <span className="block text-slate-500">Costos</span>
                    <span className="font-semibold text-slate-900">{formatMoney(revenueTotals.cost)}</span>
                  </div>
                  <div>
                    <span className="block text-slate-500">Diferencia</span>
                    <span
                      className={`font-semibold ${
                        revenueTotals.difference >= 0 ? 'text-emerald-700' : 'text-rose-700'
                      }`}
                    >
                      {formatMoney(revenueTotals.difference)}
                    </span>
                  </div>
                </div>

                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
                    <thead className="bg-slate-50 text-xs font-semibold tracking-wider text-slate-600 uppercase">
                      <tr>
                        <th className="px-4 py-3">Servicio</th>
                        <th className="px-4 py-3">Ingreso</th>
                        <th className="px-4 py-3">Costo</th>
                        <th className="px-4 py-3">Diferencia</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 bg-white">
                      {revenueReport.rows.map((row, index) => {
                        const diff = Number(row[3])
                        return (
                          <tr key={`${row[0]}-${index}`} className="hover:bg-slate-50/80">
                            <td className="px-4 py-3 font-medium text-slate-900">{row[0] || '—'}</td>
                            <td className="whitespace-nowrap px-4 py-3 text-slate-700">
                              {formatMoney(row[1])}
                            </td>
                            <td className="whitespace-nowrap px-4 py-3 text-slate-700">
                              {formatMoney(row[2])}
                            </td>
                            <td
                              className={`whitespace-nowrap px-4 py-3 font-semibold ${
                                !Number.isNaN(diff) && diff < 0 ? 'text-rose-700' : 'text-emerald-700'
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
      </div>
    </MainLayout>
  )
}
