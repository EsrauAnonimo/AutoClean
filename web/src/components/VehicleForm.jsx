import { useState } from 'react'
import { Button } from './Button'
import { Input } from './Input'
import { PlateInput } from './PlateInput'

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
 * Formulario controlado para registrar o editar un vehículo del cliente.
 * Mantiene las validaciones de placa, marca, modelo obligatorios y año 1900-2100.
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

  const clearFieldError = (field) => {
    if (fieldErrors[field]) {
      setFieldErrors((prev) => {
        const next = { ...prev }
        delete next[field]
        return next
      })
    }
  }

  const handleChange = (field) => (event) => {
    const value = event.target.value
    setForm((prev) => ({ ...prev, [field]: value }))
    clearFieldError(field)
  }

  const handlePlateChange = (cleanedPlate) => {
    setForm((prev) => ({ ...prev, plate: cleanedPlate }))
    clearFieldError('plate')
  }

  const validate = () => {
    const errors = {}
    const plate = form.plate.trim().toUpperCase()
    const brand = form.brand.trim()
    const model = form.model.trim()
    const rawYear = String(form.year).trim()

    if (!plate) {
      errors.plate = 'La placa es obligatoria.'
    }
    if (!brand) {
      errors.brand = 'La marca es obligatoria.'
    }
    if (!model) {
      errors.model = 'El modelo es obligatorio.'
    }

    if (!rawYear) {
      errors.year = 'El año es obligatorio.'
    } else if (!/^\d+$/.test(rawYear)) {
      errors.year = `Ingresa un año válido entre ${MIN_VEHICLE_YEAR} y ${MAX_VEHICLE_YEAR}.`
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
          className="rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
        >
          {serverError}
        </div>
      )}

      <PlateInput
        id="vehicle-plate"
        label="Placa"
        value={form.plate}
        onChange={handlePlateChange}
        error={mergedErrors.plate}
        compact
        required
      />

      <div className="grid gap-4 sm:grid-cols-3">
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
          placeholder="CX-30"
          value={form.model}
          onChange={handleChange('model')}
          error={mergedErrors.model}
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
      </div>

      <div className="flex flex-wrap items-center justify-end gap-3 border-t border-[#E4E6F2] pt-4">
        {onCancel && (
          <Button type="button" variant="secondary" onClick={onCancel} disabled={submitting}>
            Cancelar
          </Button>
        )}
        <Button type="submit" loading={submitting}>
          {isEditing ? 'Guardar cambios' : 'Guardar vehículo'}
        </Button>
      </div>
    </form>
  )
}
