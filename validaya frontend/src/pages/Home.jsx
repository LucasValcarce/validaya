import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

/* ─── Sidebar navigation items ───────────────────────── */
const NAV = [
  {
    section: 'Principal',
    items: [
      { ico: '🏠', label: 'Inicio',       id: 'home',     active: true  },
      { ico: '📝', label: 'Mis trámites', id: 'tramites', active: false },
      { ico: '🕐', label: 'Historial',    id: 'historial',active: false },
    ],
  },
  {
    section: 'Documentos',
    items: [
      { ico: '📁', label: 'Repositorio', id: 'docs',    active: false },
      { ico: '🎫', label: 'Mis tickets', id: 'tickets', active: false },
    ],
  },
  {
    section: 'Cuenta',
    items: [
      { ico: '👤', label: 'Perfil',          id: 'perfil', active: false },
      { ico: '⚙️', label: 'Configuración',   id: 'config', active: false },
    ],
  },
]

const TRAMITES = [
  { ico: '🪪', name: 'Renovación de CI',    inst: 'SEGIP',         docs: 3, price: 'Bs. 3',  status: 'ready'    },
  { ico: '📜', name: 'Partida de Nacimiento',inst: 'SERECI',       docs: 2, price: 'Bs. 5',  status: 'ready'    },
  { ico: '🏦', name: 'Apertura de Cuenta',   inst: 'Banco Nacional',docs: 4, price: 'Bs. 0', status: 'missing1' },
  { ico: '🛡️', name: 'Seguro de Salud',     inst: 'Seguros Bol.',  docs: 5, price: 'Bs. 5',  status: 'missing2' },
]

const STATUS_BADGE = {
  ready:    { label: 'Listo ✓',    cls: 'bg-emerald-100 text-emerald-700' },
  missing1: { label: '1 faltante', cls: 'bg-amber-100 text-amber-700'    },
  missing2: { label: '2 faltantes',cls: 'bg-red-100 text-red-600'        },
}

export default function Home() {
  const navigate = useNavigate()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const handleLogout = () => navigate('/login', { replace: true })

  return (
    <div className="min-h-screen bg-[#F7F9FC] flex">

      {/* ── Mobile sidebar backdrop ──────────────────────── */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-30 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ── SIDEBAR ──────────────────────────────────────── */}
      <aside
        className={`fixed lg:sticky top-0 h-screen z-40 w-56 bg-navy flex flex-col flex-shrink-0
          transition-transform duration-300
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0`}
      >
        {/* Logo */}
        <div className="px-5 py-5 border-b border-white/8">
          <span className="text-xl font-black text-white">
            Docu<span className="text-teal">Track</span>
          </span>
          <p className="text-[10px] text-white/30 mt-0.5">Bolivia · Plataforma digital</p>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-3 overflow-y-auto">
          {NAV.map(({ section, items }) => (
            <div key={section}>
              <p className="text-[10px] font-bold uppercase tracking-widest text-white/25 px-2 mt-4 mb-1">
                {section}
              </p>
              {items.map(({ ico, label, id, active }) => (
                <button
                  key={id}
                  className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-[13px] font-semibold mb-0.5 transition-colors text-left
                    ${active
                      ? 'bg-teal/15 text-teal'
                      : 'text-white/55 hover:bg-white/6 hover:text-white'
                    }`}
                >
                  <span className="w-5 text-center text-base">{ico}</span>
                  {label}
                </button>
              ))}
            </div>
          ))}
        </nav>

        {/* User footer */}
        <div className="px-4 py-4 border-t border-white/8 flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-teal flex items-center justify-center text-white font-black text-xs flex-shrink-0">
            JM
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-white text-xs font-bold truncate">Juan Mamani</p>
            <p className="text-white/35 text-[10px]">CI: 12345678</p>
          </div>
          <button
            onClick={handleLogout}
            className="text-white/30 hover:text-white/70 transition-colors flex-shrink-0"
            title="Cerrar sesión"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </button>
        </div>
      </aside>

      {/* ── MAIN CONTENT ────────────────────────────────── */}
      <div className="flex-1 flex flex-col min-w-0">

        {/* Topbar */}
        <header className="sticky top-0 z-20 h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 sm:px-6">
          {/* Hamburger (mobile) */}
          <button
            className="lg:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors mr-2"
            onClick={() => setSidebarOpen(true)}
            aria-label="Abrir menú"
          >
            <svg className="w-5 h-5 text-navy" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>

          <h1 className="font-black text-navy text-base">👋 Bienvenido, Juan</h1>

          <div className="flex items-center gap-2 sm:gap-3">
            {/* Notification */}
            <button className="relative w-9 h-9 rounded-lg bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors">
              <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500 border border-white" />
            </button>
            {/* Avatar */}
            <div className="w-9 h-9 rounded-full bg-teal flex items-center justify-center text-white font-black text-xs">
              JM
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 p-4 sm:p-6 overflow-y-auto">

          {/* Alert banner */}
          <div className="flex items-start sm:items-center gap-3 px-4 py-3 bg-amber-50 border border-amber-400 rounded-xl mb-5">
            <span className="text-lg flex-shrink-0">⚠️</span>
            <p className="text-xs sm:text-sm text-amber-800 flex-1">
              <strong>Tienes 1 documento faltante.</strong> Sube tu Extracto Bancario para desbloquear 1 trámite.
            </p>
            <button className="flex-shrink-0 px-3 py-1.5 rounded-lg bg-amber-400 text-amber-900 text-xs font-bold hover:bg-amber-500 transition-colors whitespace-nowrap">
              Subir
            </button>
          </div>

          {/* Stat cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4 mb-5">
            {[
              { ico: '📄', label: 'Documentos activos', val: '5/8', sub: '3 faltantes',      color: 'text-navy'           },
              { ico: '🔄', label: 'En proceso',         val: '1',   sub: 'Pago pendiente',   color: 'text-amber-500'      },
              { ico: '✅', label: 'Trámites completados',val: '3',  sub: 'Este año',          color: 'text-emerald-500'    },
            ].map(({ ico, label, val, sub, color }) => (
              <div key={label} className="bg-white border border-gray-200 rounded-2xl p-4 sm:p-5">
                <div className="text-xl mb-2">{ico}</div>
                <div className="text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1">{label}</div>
                <div className={`text-3xl font-black ${color}`}>{val}</div>
                <div className="text-xs text-gray-400 mt-1">{sub}</div>
              </div>
            ))}
          </div>

          {/* Quick actions */}
          <div className="mb-5">
            <h2 className="text-sm font-black text-navy mb-3">Accesos rápidos</h2>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <button className="bg-teal rounded-2xl p-4 flex flex-col gap-2 text-left hover:bg-teal-hover transition-colors active:scale-95">
                <span className="text-2xl">📝</span>
                <span className="text-sm font-bold text-white">Nuevo trámite</span>
              </button>
              <button className="bg-navy rounded-2xl p-4 flex flex-col gap-2 text-left hover:bg-navy-light transition-colors active:scale-95">
                <span className="text-2xl">📁</span>
                <span className="text-sm font-bold text-white">Mis documentos</span>
              </button>
              <button className="bg-white border border-gray-200 rounded-2xl p-4 flex flex-col gap-2 text-left hover:border-gray-300 transition-colors active:scale-95">
                <span className="text-2xl">🎫</span>
                <span className="text-sm font-bold text-navy">Mis tickets</span>
              </button>
              <button className="bg-white border border-gray-200 rounded-2xl p-4 flex flex-col gap-2 text-left hover:border-gray-300 transition-colors active:scale-95">
                <span className="text-2xl">🕐</span>
                <span className="text-sm font-bold text-navy">Historial</span>
              </button>
            </div>
          </div>

          {/* Trámites table */}
          <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
              <h2 className="text-sm font-black text-navy">🚀 Trámites disponibles</h2>
              <button className="px-3 py-1.5 rounded-lg bg-teal text-white text-xs font-bold hover:bg-teal-hover transition-colors">
                Ver todos
              </button>
            </div>

            <ul className="divide-y divide-gray-100">
              {TRAMITES.map(({ ico, name, inst, docs, price, status }) => (
                <li
                  key={name}
                  className="flex items-center gap-3 px-5 py-3.5 hover:bg-gray-50 transition-colors cursor-pointer"
                >
                  <div className="w-10 h-10 rounded-xl bg-teal-light flex items-center justify-center text-lg flex-shrink-0">
                    {ico}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-bold text-navy truncate">{name}</p>
                    <p className="text-xs text-gray-400 mt-0.5">
                      {inst} · {docs} docs · {price}
                    </p>
                  </div>
                  <span className={`px-2.5 py-1 rounded-full text-[11px] font-bold flex-shrink-0 ${STATUS_BADGE[status].cls}`}>
                    {STATUS_BADGE[status].label}
                  </span>
                </li>
              ))}
            </ul>
          </div>

        </main>
      </div>
    </div>
  )
}