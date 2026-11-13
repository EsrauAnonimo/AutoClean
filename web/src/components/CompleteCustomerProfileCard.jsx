import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { Button } from './Button'
import { Input } from './Input'

const PHONE_REGEX = /^[+]?[0-9\s\-()]{7,20}$/

/**
 * Tarjeta interactiva para completar nombre y teléfono cuando una cuenta CLIENTE
 * (por ejemplo, ingresada con Google) aún no tiene perfil vinculado en clientes.
 */
export function CompleteCustomerProfileCard({ onCompleted }) {
  const { user, completeProfile } = useAuth()
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    const cleanName = name.trim()
    const cleanPhone = phone.trim()

    if (!cleanName || !PHONE_REGEX.test(cleanPhone)) {
      setErrorMessage('Ingresa tu nombre completo y un teléfono válido (7 a 20 caracteres).')
      return
    }

    setSubmitting(true)
    try {
      const updatedSession = await completeProfile({ name: cleanName, phone: cleanPhone })
      if (onCompleted) {
        onCompleted(updatedSession)
      }
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar tu perfil de cliente.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="rounded-2xl border border-amber-200 bg-amber-50/70 p-6 shadow-sm">
      <div className="mb-4">
        <span className="inline-flex rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-semibold text-amber-800">
          Paso único de activación
        </span>
        <h3 className="mt-2 text-lg font-bold text-slate-900">
          Completa tu perfil para registrar vehículos y agendar citas
        </h3>
        <p className="mt-1 text-sm text-slate-600">
          Vincula tus datos de contacto a <span className="font-semibold text-slate-900">{user?.email}</span> para
          habilitar todos los servicios del portal.
        </p>
      </div>

      {errorMessage && (
        <div
          role="alert"
          className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
        >
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-3">
        <Input
          id="profileName"
          label="Nombre completo"
          placeholder="Esteban Ramírez"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <Input
          id="profilePhone"
          label="Teléfono de contacto"
          placeholder="+57 300 123 4567"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          required
        />
        <div className="flex items-end">
          <Button type="submit" loading={submitting} className="w-full">
            Activar mi perfil
          </Button>
        </div>
      </form>
    </section>
  )
}

