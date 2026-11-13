/**
 * Campo de formulario reutilizable con etiqueta y mensaje de error opcional.
 */
export function Input({
  id,
  label,
  error,
  className = '',
  ...props
}) {
  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={id} className="block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      <input
        id={id}
        className={`block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 placeholder-slate-400 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none ${className}`}
        {...props}
      />
      {error && <p className="text-xs font-medium text-rose-600">{error}</p>}
    </div>
  )
}
