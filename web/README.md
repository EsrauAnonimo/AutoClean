# AutoClean — Frontend web

Panel admin y portal de clientes en React + Vite + Tailwind.

```bash
cp .env.example .env   # define VITE_API_URL
npm install
npm run dev            # http://localhost:5173
npm run build
npm run lint
```

Estructura de `src/`:

- `pages/` — una vista por ruta.
- `components/` — componentes reutilizables (UI kit, layout, formularios).
- `context/` — `AuthContext` (sesión JWT).
- `services/` — llamadas HTTP al backend (`api.js` configura Axios).
- `utils/` — helpers de formato, fechas, validación y toasts.
