import { useState } from 'react'
import { Button } from './Button'
import { Input } from './Input'

const MIN_VEHICLE_YEAR = 1900
const MAX_VEHICLE_YEAR = 2100

function buildInitialForm(initialVehicle) {
  if (!initialVehicle) {
    return { plate: '', brand: '', model: '', year: '' }
  }
  return {
    plate: initialVehicle.plate ?? '',
    brand: initialVehicle.brand ?? '',
    model: initialVehicle.model ?? '',
    year: initialVehicle.year != null ? String(initialVehicle.year) : '',
  }
}

/**
 * Formulario controlado reutilizable para registrar o editar un vehículo del cliente.
 * Replica las validaciones de VehicleRequest (placa, marca, modelo obligatorios y año 1900-2100).
 */
export function VehicleForm({
  initialVehicle = null,
  onSubmit,
  onCancel,
  submitting = false,
  serverError = '',
  serverFieldErrors = {},
}) {
  const [form, setForm] = useState(() => buildInitialForm(initialVehicle))
  const [fieldErrors, setFieldErrors] = useState({})

  const isEditing = Boolean(initialVehicle?.id)

  const mergedErrors = {
    ...serverFieldErrors,
    ...fieldErrors,
  }

  const handleChange = (field) => (event) => {
    const value = event.target.value
    setForm((prev) => ({ ...prev, [field]: value }))
    if (fieldErrors[field]) {
      setFieldErrors((prev) => {
        const next = { ...prev }
        delete next[field]
        return next
      })
    }
  }

  const validate = () => {
    const errors = {}
    const plate = form.plate.trim().toUpperCase()
    const brand = form.brand.trim()
    const model = form.model.trim()
    const rawYear = String(form.year).trim()

    if (!plate) {
      errors.plate = 'La placa no puede estar vacía.'
    }
    if (!brand) {
      errors.brand = 'La marca no puede estar vacía.'
    }
    if (!model) {
      errors.model = 'El modelo no puede estar vacío.'
    }

    if (!rawYear) {
      errors.year = 'El año es obligatorio.'
    } else if (!/^\d+$/.test(rawYear)) {
      errors.year = `El año debe ser un número válido entre ${MIN_VEHICLE_YEAR} y ${MAX_VEHICLE_YEAR}.`
    } else {
      const parsedYear = Number.parseInt(rawYear, 10)
      if (parsedYear < MIN_VEHICLE_YEAR || parsedYear > MAX_VEHICLE_YEAR) {
        errors.year = `El año debe estar entre ${MIN_VEHICLE_YEAR} y ${MAX_VEHICLE_YEAR}.`
      }
    }

    return {
      errors,
      payload: {
        plate,
        brand,
        model,
        year: Number.parseInt(rawYear, 10),
      },
    }
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    const { errors, payload } = validate()
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setFieldErrors({})
    await onSubmit(payload)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4" noValidate>
      {serverError && (
        <div
          role="alert"
          className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
        >
          {serverError}
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2">
        <Input
          id="vehicle-plate"
          label="Placa"
          type="text"
          placeholder="ABC123"
          value={form.plate}
          onChange={handleChange('plate')}
          error={mergedErrors.plate}
          className="uppercase"
          required
        />

        <Input
          id="vehicle-year"
          label="Año"
          type="number"
          min={MIN_VEHICLE_YEAR}
          max={MAX_VEHICLE_YEAR}
          placeholder="2023"
          value={form.year}
          onChange={handleChange('year')}
          error={mergedErrors.year}
          required
        />

        <Input
          id="vehicle-brand"
          label="Marca"
          type="text"
          placeholder="Mazda"
          value={form.brand}
          onChange={handleChange('brand')}
          error={mergedErrors.brand}
          required
        />

        <Input
          id="vehicle-model"
          label="Modelo"
          type="text"
          placeholder="CX-5"
          value={form.model}
          onChange={handleChange('model')}
          error={mergedErrors.model}
          required
        />
      </div>

      <div className="flex flex-wrap items-center justify-end gap-3 pt-2">
        {onCancel && (
          <Button type="button" variant="secondary" onClick={onCancel} disabled={submitting}>
            Cancelar
          </Button>
        )}
        <Button type="submit" loading={submitting}>
          {isEditing ? 'Guardar cambios' : 'Registrar vehículo'}
        </Button>
      </div>
    </form>
  )
}
