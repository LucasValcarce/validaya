import { useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'

const TRAMITES = [
  { ico: '🪪', name: 'Renovación de CI',     inst: 'SEGIP',          docs: 3, price: 'Bs. 3', status: 'ready'    },
  { ico: '📜', name: 'Partida de Nacimiento', inst: 'SERECI',         docs: 2, price: 'Bs. 5', status: 'ready'    },
  { ico: '🏦', name: 'Apertura de Cuenta',    inst: 'Banco Nacional', docs: 4, price: 'Bs. 0', status: 'missing1' },
  { ico: '🛡️', name: 'Seguro de Salud',      inst: 'Seguros Bol.',   docs: 5, price: 'Bs. 5', status: 'missing2' },
]

const STATUS_BADGE = {
  ready:    { label: 'Listo ✓',     cls: 'bg-emerald-100 text-emerald-700' },
  missing1: { label: '1 pendiente', cls: 'bg-amber-100 text-amber-700'    },
  missing2: { label: '2 pendientes',cls: 'bg-red-100 text-red-600'        },
}

export default function Home() {
  const navigate = useNavigate()

  return (
    <Layout title="👋 Bienvenido, Juan">

      {/* Alert */}
      <div className="flex items-start sm:items-center gap-3 px-4 py-3 bg-amber-50 border border-amber-400 rounded-xl mb-5">
        <span className="text-lg flex-shrink-0">⚠️</span>
        <p className="text-xs sm:text-sm text-amber-800 flex-1">
          <strong>Tienes documentos pendientes de obtener.</strong> Inicia un trámite para conseguirlos.
        </p>
        <button
          onClick={() => navigate('/tramites')}
          className="flex-shrink-0 px-3 py-1.5 rounded-lg bg-amber-400 text-amber-900 text-xs font-bold hover:bg-amber-500 transition-colors whitespace-nowrap"
        >
          Ver trámites
        </button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4 mb-5">
        {[
          { ico: '📄', label: 'Documentos obtenidos', val: '5/8', sub: '3 por obtener',    color: 'text-navy'        },
          { ico: '🔄', label: 'En proceso',            val: '1',  sub: 'Pago pendiente',   color: 'text-amber-500'   },
          { ico: '✅', label: 'Trámites completados',  val: '3',  sub: 'Este año',          color: 'text-emerald-500' },
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
      <h2 className="text-sm font-black text-navy mb-3">Accesos rápidos</h2>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-5">
        <button
          onClick={() => navigate('/tramites')}
          className="bg-teal rounded-2xl p-4 flex flex-col gap-2 text-left hover:bg-teal-hover transition-colors active:scale-95"
        >
          <span className="text-2xl">📝</span>
          <span className="text-sm font-bold text-white">Nuevo trámite</span>
        </button>
        <button
          onClick={() => navigate('/docs')}
          className="bg-navy rounded-2xl p-4 flex flex-col gap-2 text-left hover:bg-navy-light transition-colors active:scale-95"
        >
          <span className="text-2xl">📁</span>
          <span className="text-sm font-bold text-white">Mis documentos</span>
        </button>
        <button
          onClick={() => navigate('/tickets')}
          className="bg-white border border-gray-200 rounded-2xl p-4 flex flex-col gap-2 text-left hover:border-gray-300 transition-colors active:scale-95"
        >
          <span className="text-2xl">🎫</span>
          <span className="text-sm font-bold text-navy">Mis tickets</span>
        </button>
        <button
          onClick={() => navigate('/historial')}
          className="bg-white border border-gray-200 rounded-2xl p-4 flex flex-col gap-2 text-left hover:border-gray-300 transition-colors active:scale-95"
        >
          <span className="text-2xl">🕐</span>
          <span className="text-sm font-bold text-navy">Historial</span>
        </button>
      </div>

      {/* Trámites */}
      <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <h2 className="text-sm font-black text-navy">🚀 Trámites disponibles</h2>
          <button
            onClick={() => navigate('/tramites')}
            className="px-3 py-1.5 rounded-lg bg-teal text-white text-xs font-bold hover:bg-teal-hover transition-colors"
          >
            Ver todos
          </button>
        </div>
        <ul className="divide-y divide-gray-100">
          {TRAMITES.map(({ ico, name, inst, docs, price, status }) => (
            <li
              key={name}
              onClick={() => navigate('/tramites')}
              className="flex items-center gap-3 px-5 py-3.5 hover:bg-gray-50 transition-colors cursor-pointer"
            >
              <div className="w-10 h-10 rounded-xl bg-teal-light flex items-center justify-center text-lg flex-shrink-0">
                {ico}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-bold text-navy truncate">{name}</p>
                <p className="text-xs text-gray-400 mt-0.5">{inst} · {docs} docs · {price}</p>
              </div>
              <span className={`px-2.5 py-1 rounded-full text-[11px] font-bold flex-shrink-0 ${STATUS_BADGE[status].cls}`}>
                {STATUS_BADGE[status].label}
              </span>
            </li>
          ))}
        </ul>
      </div>

    </Layout>
  )
}