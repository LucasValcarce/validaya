import { useNavigate } from 'react-router-dom'

export default function Landing() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-navy text-white flex flex-col">

      {/* Header simple */}
      <header className="w-full max-w-6xl mx-auto px-4 sm:px-8 py-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="text-xl font-black">
            Valida<span className="text-teal">Ya</span>
          </span>
          <span className="hidden sm:inline-block text-[11px] text-white/60 border border-white/10 rounded-full px-2 py-0.5">
            MVP · Demo
          </span>
        </div>
        <button
          onClick={() => navigate('/login')}
          className="text-xs font-bold px-4 py-2 rounded-xl bg-white text-navy hover:bg-gray-100 transition-colors"
        >
          Ir a la app
        </button>
      </header>

      {/* Hero */}
      <main className="flex-1 w-full flex items-center">
        <div className="w-full max-w-6xl mx-auto px-4 sm:px-8 py-10 sm:py-16 grid gap-10 lg:grid-cols-2 lg:items-center">

          {/* Columna texto */}
          <div>
            <span className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-teal/40 bg-teal/15 text-teal text-[11px] font-bold tracking-wide">
              <span className="w-1.5 h-1.5 rounded-full bg-teal animate-pulse" />
              MVP · Validación de concepto
            </span>

            <h1 className="mt-5 text-3xl sm:text-4xl lg:text-5xl font-black leading-tight tracking-tight">
              Trámites públicos,
              <br />
              <span className="text-teal">sin filas</span> y sin papeles.
            </h1>

            <p className="mt-4 text-sm sm:text-base text-white/70 max-w-lg leading-relaxed">
              ValidaYa centraliza documentos, verifica requisitos en tiempo real y genera tickets
              digitales para que ciudadanos e instituciones reduzcan tiempos, errores y costos
              operativos.
            </p>

            <div className="mt-6 flex flex-col sm:flex-row gap-3">
              <button
                onClick={() => navigate('/login')}
                className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-teal text-navy text-sm font-bold hover:bg-teal-hover transition-colors shadow-md"
              >
                Probar el MVP ahora
                <span>→</span>
              </button>
              {/* <button
                onClick={() => navigate('/home')}
                className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl border border-white/25 text-sm font-semibold text-white/80 hover:bg-white/5 transition-colors"
              >
                Ver flujo de trámites
              </button> */}
            </div>

            <div className="mt-6 grid grid-cols-3 gap-3 max-w-xs text-[11px] text-white/60">
              <div>
                <p className="font-black text-sm text-white">70%</p>
                <p>menos tiempo en ventanilla</p>
              </div>
              <div>
                <p className="font-black text-sm text-white">100%</p>
                <p>validación digital de requisitos</p>
              </div>
              <div>
                <p className="font-black text-sm text-white">24/7</p>
                <p>operación desde cualquier lugar</p>
              </div>
            </div>
          </div>

          {/* Columna mock UI */}
          <div className="relative">
            <div className="absolute -top-10 -right-8 w-56 h-56 bg-teal/20 blur-3xl rounded-full pointer-events-none" />
            <div className="absolute -bottom-10 -left-8 w-48 h-48 bg-teal/10 blur-3xl rounded-full pointer-events-none" />

            <div className="relative mx-auto max-w-sm bg-[#F7F9FC] rounded-3xl shadow-2xl border border-white/10 overflow-hidden">
              <div className="bg-navy px-4 py-3 flex items-center justify-between">
                <span className="text-xs font-bold text-white/80">Demo de la app</span>
                <span className="text-[11px] text-teal/80">Ciudadano</span>
              </div>

              <div className="p-4 flex flex-col gap-3">
                <div className="bg-white rounded-2xl border border-gray-200 p-3 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-teal-light flex items-center justify-center text-lg">
                    🪪
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-xs font-bold text-navy truncate">Renovación de CI</p>
                    <p className="text-[10px] text-gray-400">SEGIP · 3 requisitos</p>
                  </div>
                  <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 text-[9px] font-bold">
                    Listo
                  </span>
                </div>

                <div className="bg-white rounded-2xl border border-gray-200 p-3 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-teal-light flex items-center justify-center text-lg">
                    📜
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-xs font-bold text-navy truncate">Certificado de nacimiento</p>
                    <p className="text-[10px] text-gray-400">SERECI · 2 requisitos</p>
                  </div>
                  <span className="px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 text-[9px] font-bold">
                    1 pendiente
                  </span>
                </div>

                <div className="mt-1 bg-white rounded-2xl border border-dashed border-gray-300 p-3 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-xl bg-navy text-white flex items-center justify-center text-base">
                      🎫
                    </div>
                    <div>
                      <p className="text-xs font-bold text-navy">Ticket digital</p>
                      <p className="text-[10px] text-gray-400">Sin filas, solo presentas el QR</p>
                    </div>
                  </div>
                  {/* <button
                    type="button"
                    className="px-3 py-1.5 rounded-xl bg-teal text-navy text-[10px] font-bold"
                    onClick={() => navigate('/home')}
                  >
                    Ver ejemplo
                  </button> */}
                </div>
              </div>
            </div>
          </div>

        </div>
      </main>

      {/* Footer corto */}
      <footer className="w-full max-w-6xl mx-auto px-4 sm:px-8 py-4 flex flex-col sm:flex-row gap-2 sm:items-center sm:justify-between text-[11px] text-white/50">
        <span>ValidaYa · MVP para demostración</span>
        <span>Construido para agilizar trámites en Bolivia</span>
      </footer>
    </div>
  )
}


