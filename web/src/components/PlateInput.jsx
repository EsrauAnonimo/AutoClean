/**
 * Input de placa colombiana en mayúsculas (formato ABC123) para registro y agendamiento.
 */
export function PlateInput({
  id = 'plate-input',
  label = 'Placa del vehículo',
  value,
  onChange,
  error,
  placeholder = 'ABC123',
  required = false,
  compact = false,
}) {
  const handleInput = (event) => {
    const cleaned = event.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 6)
    onChange(cleaned)
  }

  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={id} className="block text-[13px] font-semibold text-[#1E1B4B]">
          {label}
        </label>
      )}
      <div
        className={`overflow-hidden rounded-xl border-2 bg-[#FEF08A] transition focus-within:ring-4 ${
          error
            ? 'border-[#B91C1C] focus-within:ring-[#EF4444]/20'
            : 'border-[#1E1B4B] focus-within:ring-[#4F46E5]/20'
        }`}
      >
        <div className="bg-[#1E1B4B] py-0.5 text-center text-[9px] font-bold tracking-[0.28em] text-white uppercase">
          Colombia
        </div>
        <input
          id={id}
          type="text"
          maxLength={6}
          value={value}
          onChange={handleInput}
          placeholder={placeholder}
          required={required}
          className={`w-full bg-transparent text-center font-mono font-extrabold tracking-[0.22em] text-[#1E1B4B] uppercase placeholder-[#1E1B4B]/35 outline-none ${
            compact ? 'py-1.5 text-lg' : 'py-2.5 text-2xl'
          }`}
        />
      </div>
      {error && <p className="text-[12px] font-medium text-[#B91C1C]">{error}</p>}
    </div>
  )
}

