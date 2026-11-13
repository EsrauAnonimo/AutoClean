import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { showToast } from '../utils/toast'
import { Button } from './Button'
import { Input } from './Input'

const PHONE_REGEX = /^[+]?[0-9\s\-()]{7,20}$/

/**
 * Banner discreto para completar nombre y teléfono cuando una cuenta CLIENTE
 * aún no tiene perfil vinculado tras ingresar con Google.
 */
export function CompleteCustomerProfileCard({ onCompleted }) {
  const { user, completeProfile } = useAuth()
  const [expanded, setExpanded] = useState(false)
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
      showToast({
        type: 'success',
        title: 'Perfil activado',
        description: 'Ya puedes registrar vehículos y agendar citas.',
      })
      if (onCompleted) {
        onCompleted(updatedSession)
      }
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar tu perfil.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="rounded-2xl border border-[#E4E6F2] bg-white p-4 shadow-card sm:p-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-[#EDE9FE] text-sm font-bold text-[#4F46E5]">
            ✦
          </span>
          <div>
            <p className="text-[14px] font-semibold text-[#1E1B4B]">Completa tu perfil de cliente</p>
            <p className="text-[13px] text-[#5B5F7A]">
              Agrega nombre y teléfono para {user?.email} y habilita reservas en segundos.
            </p>
          </div>
        </div>
        {!expanded && (
          <Button variant="secondary" onClick={() => setExpanded(true)}>
            Completar perfil
          </Button>
        )}
      </div>

      {expanded && (
        <form onSubmit={handleSubmit} className="mt-4 space-y-3 border-t border-[#E4E6F2] pt-4">
          {errorMessage && (
            <div
              role="alert"
              className="rounded-xl border border-[#FECACA] bg-[#FEE2E2] px-3.5 py-2.5 text-xs font-medium text-[#B91C1C]"
            >
              {errorMessage}
            </div>
          )}

          <div className="grid gap-3 sm:grid-cols-3">
            <Input
              id="profileName"
              label="Nombre completo"
              placeholder="Camila Restrepo"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <Input
              id="profilePhone"
              label="Teléfono"
              placeholder="+57 310 458 9210"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              required
            />
            <div className="flex items-end gap-2">
              <Button type="button" variant="ghost" onClick={() => setExpanded(false)}>
                Cancelar
              </Button>
              <Button type="submit" loading={submitting} className="flex-1">
                Guardar
              </Button>
            </div>
          </div>
        </form>
      )}
    </section>
  )
}
