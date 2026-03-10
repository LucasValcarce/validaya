import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'

const NAV = [
  {
    section: 'Principal',
    items: [
      { ico: '🏠', label: 'Inicio',       path: '/home'     },
      { ico: '📝', label: 'Mis trámites', path: '/tramites' },
      { ico: '🕐', label: 'Historial',    path: '/historial'},
    ],
  },
  {
    section: 'Documentos',
    items: [
      { ico: '📁', label: 'Repositorio', path: '/docs'    },
      { ico: '🎫', label: 'Mis tickets', path: '/tickets' },
    ],
  },
  {
    section: 'Cuenta',
    items: [
      { ico: '👤', label: 'Perfil',        path: '/perfil' },
      { ico: '⚙️', label: 'Configuración', path: '/config' },
    ],
  },
]

export default function Layout({ title, actions, children }) {
  const navigate        = useNavigate()
  const location        = useLocation()
  const [open, setOpen] = useState(false)

  const handleLogout = () => navigate('/login', { replace: true })

  return (
    <div className="min-h-screen bg-[#F7F9FC] flex">

      {open && (
        <div
          className="fixed inset-0 bg-black/50 z-30 lg:hidden"
          onClick={() => setOpen(false)}
        />
      )}

      <aside
        className={`fixed lg:sticky top-0 h-screen z-40 w-56 bg-navy flex flex-col flex-shrink-0
          transition-transform duration-300
          ${open ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0`}
      >
        <div className="px-5 py-5 border-b border-white/[0.08]">
          <span className="text-xl font-black text-white">
            Valida<span className="text-teal">Ya</span>
          </span>
          <p className="text-[10px] text-white/30 mt-0.5">Bolivia · Plataforma digital</p>
        </div>

        <nav className="flex-1 px-3 py-3 overflow-y-auto">
          {NAV.map(({ section, items }) => (
            <div key={section}>
              <p className="text-[10px] font-bold uppercase tracking-widest text-white/25 px-2 mt-4 mb-1">
                {section}
              </p>
              {items.map(({ ico, label, path }) => {
                const active = location.pathname === path
                return (
                  <button
                    key={path}
                    onClick={() => { navigate(path); setOpen(false) }}
                    className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-[13px] font-semibold mb-0.5 transition-colors text-left
                      ${active
                        ? 'bg-teal/15 text-teal'
                        : 'text-white/55 hover:bg-white/[0.06] hover:text-white'
                      }`}
                  >
                    <span className="w-5 text-center text-base">{ico}</span>
                    {label}
                  </button>
                )
              })}
            </div>
          ))}
        </nav>

        <div className="px-4 py-4 border-t border-white/[0.08] flex items-center gap-3">
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
              <path strokeLinecap="round" strokeLinejoin="round"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        <header className="sticky top-0 z-20 h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 sm:px-6">
          <div className="flex items-center gap-3 min-w-0">
            <button
              className="lg:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors flex-shrink-0"
              onClick={() => setOpen(true)}
              aria-label="Abrir menú"
            >
              <svg className="w-5 h-5 text-navy" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <h1 className="font-black text-navy text-base truncate">{title}</h1>
          </div>

          <div className="flex items-center gap-2 sm:gap-3 flex-shrink-0">
            {actions}
            <button className="relative w-9 h-9 rounded-lg bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors">
              <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round"
                  d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500 border border-white" />
            </button>
            <div className="w-9 h-9 rounded-full bg-teal flex items-center justify-center text-white font-black text-xs">
              JM
            </div>
          </div>
        </header>

        <main className="flex-1 p-4 sm:p-6 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  )
}