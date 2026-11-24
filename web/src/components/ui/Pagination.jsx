import { Button } from './Button'

/**
 * Barra de paginación "Anterior / Siguiente"; no se muestra con una sola página.
 */
export function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null

  return (
    <div className="flex items-center justify-between border-t border-[#E4E6F2] px-5 py-3 text-xs text-[#5B5F7A]">
      <span className="tabular-nums">
        Página {page} de {totalPages}
      </span>
      <div className="flex items-center gap-2">
        <Button
          variant="secondary"
          className="h-9 px-3 text-xs"
          disabled={page <= 1}
          onClick={() => onChange(page - 1)}
        >
          Anterior
        </Button>
        <Button
          variant="secondary"
          className="h-9 px-3 text-xs"
          disabled={page >= totalPages}
          onClick={() => onChange(page + 1)}
        >
          Siguiente
        </Button>
      </div>
    </div>
  )
}
