import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'
import { apiFetch } from '../services/apiService'

const STATUS_BADGE = {
  ready:    { label: 'Listo ✓',      cls: 'bg-emerald-100 text-emerald-700' },
  missing1: { label: '1 pendiente',  cls: 'bg-amber-100 text-amber-700'    },
  missing2: { label: '2 pendientes', cls: 'bg-red-100 text-red-600'        },
  missing3: { label: 'Pendientes',   cls: 'bg-red-100 text-red-600'        },
}

const PROC_ICONS = {
  'CI': '🪪', 'Cédula': '🪪', 'Nacimiento': '📜', 'Partida': '📜',
  'Cuenta': '🏦', 'Bancaria': '🏦', 'Seguro': '🛡️', 'Salud': '🛡️',
  'Domicilio': '🏠', 'Trabajo': '💼', 'Extracto': '💳', 'default': '📄',
}

function getProcIcon(name = '') {
  const key = Object.keys(PROC_ICONS).find(k => name.includes(k))
  return key ? PROC_ICONS[key] : PROC_ICONS.default
}

export default function Home() {
  const navigate = useNavigate()

  const user   = JSON.parse(localStorage.getItem('auth_user') || '{}')
  const nombre = (user.fullName || 'Usuario').split(' ')[0]
  const userId = user.userId

  const [userDocs,       setUserDocs]       = useState([])
  const [applications,   setApplications]   = useState([])
  const [tramitesDetail, setTramitesDetail] = useState([])
  const [loading,        setLoading]        = useState(true)

  useEffect(() => {
  if (!userId) return
  async function loadData() {
    try {
      const [docsData, appsData, procsData] = await Promise.all([
        apiFetch('/user-documents'),
        apiFetch(`/applications/user/${userId}`),
        apiFetch('/procedures'),
      ])

      const docs = docsData || []
      setUserDocs(docs)
      setApplications(appsData || [])

      // Mismo filtro que Tramites: excluir los que el usuario ya tiene
      const docNames = new Set(docs.map(d => d.documentTypeName))
      const pendientes = (procsData || []).filter(
        p => !docNames.has(p.outputDocumentTypeName)
      )

      // Fetch detalles solo de los primeros 4 pendientes
      const details = await Promise.all(
        pendientes.slice(0, 4).map(p =>
          apiFetch(`/procedures/${p.id}`).catch(() => null)
        )
      )
      setTramitesDetail(details.filter(Boolean))
    } catch (err) {
      console.error('[Home] error cargando datos:', err)
    } finally {
      setLoading(false)
    }
  }
  loadData()
}, [userId])

  const docsObtenidos = userDocs.filter(d => d.status?.toLowerCase() === 'active').length
  const enProceso     = applications.filter(a =>
    ['PENDING','PAYMENT_PENDING','IN_REVIEW','SUBMITTED'].includes(a.status)
  ).length
  const completados   = applications.filter(a =>
    ['COMPLETED','APPROVED'].includes(a.status)
  ).length

  const userDocNames     = new Set(userDocs.map(d => d.documentTypeName))
  const tienesPendientes = tramitesDetail.some(p =>
    (p.requirements || []).some(r => r.isMandatory && !userDocNames.has(r.documentTypeName))
  )

  const tramitesDisplay = tramitesDetail.map(proc => {
    const reqs    = proc.requirements || []
    const missing = reqs.filter(r => !userDocNames.has(r.documentTypeName)).length
    let status = 'ready'
    if (missing === 1) status = 'missing1'
    else if (missing === 2) status = 'missing2'
    else if (missing > 2)  status = 'missing3'
    return {
      ico:    getProcIcon(proc.name),
      name:   proc.name,
      inst:   proc.institutionName,
      docs:   reqs.length,
      price:  `Bs. ${proc.totalPrice ?? proc.basePrice ?? 0}`,
      status,
    }
  })

  return (
    <Layout title={`👋 Bienvenido, ${nombre}`}>

      {tienesPendientes && (
        <div className="flex items-start sm:items-center gap-3 px-4 py-3 bg-amber-50 border border-amber-400 rounded-xl mb-5">
          <span className="text-lg flex-shrink-0">⚠️</span>
          <p className="text-xs sm:text-sm text-amber-800 flex-1">
            <strong>Tienes documentos pendientes de obtener.</strong> Inicia un trámite para conseguirlos.
          </p>
          <button onClick={() => navigate('/tramites')}
            className="flex-shrink-0 px-3 py-1.5 rounded-lg bg-amber-400 text-amber-900 text-xs font-bold hover:bg-amber-500 transition-colors whitespace-nowrap">
            Ver trámites
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4 mb-5">
        {[
          { ico: '📄', label: 'Documentos obtenidos', val: loading ? '…' : `${docsObtenidos}`,  sub: loading ? '' : `${userDocs.length - docsObtenidos} por obtener`, color: 'text-navy'        },
          { ico: '🔄', label: 'En proceso',            val: loading ? '…' : `${enProceso}`,      sub: 'Trámites activos',  color: 'text-amber-500'   },
          { ico: '✅', label: 'Trámites completados',  val: loading ? '…' : `${completados}`,    sub: 'Total histórico',   color: 'text-emerald-500' },
        ].map(({ ico, label, val, sub, color }) => (
          <div key={label} className="bg-white border border-gray-200 rounded-2xl p-4 sm:p-5">
            <div className="text-xl mb-2">{ico}</div>
            <div className="text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1">{label}</div>
            <div className={`text-3xl font-black ${color}`}>{val}</div>
            <div className="text-xs text-gray-400 mt-1">{sub}</div>
          </div>
        ))}
      </div>

      <h2 className="text-sm font-black text-navy mb-3">Accesos rápidos</h2>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-5">
        <button onClick={() => navigate('/tramites', { state: { nuevo: true } })}
          className="bg-teal rounded-2xl p-4 flex flex-col gap-2 text-left hover:bg-teal-hover transition-colors active:scale-95">
          <span className="text-2xl">📝</span>
          <span className="text-sm font-bold text-white">Nuevo trámite</span>
        </button>
        <button onClick={() => navigate('/docs')}
          className="bg-navy rounded-2xl p-4 flex flex-col gap-2 text-left hover:bg-navy-light transition-colors active:scale-95">
          <span className="text-2xl">📁</span>
          <span className="text-sm font-bold text-white">Mis documentos</span>
        </button>
        <button onClick={() => navigate('/tickets')}
          className="bg-white border border-gray-200 rounded-2xl p-4 flex flex-col gap-2 text-left hover:border-gray-300 transition-colors active:scale-95">
          <span className="text-2xl">🎫</span>
          <span className="text-sm font-bold text-navy">Mis tickets</span>
        </button>
        <button onClick={() => navigate('/historial')}
          className="bg-white border border-gray-200 rounded-2xl p-4 flex flex-col gap-2 text-left hover:border-gray-300 transition-colors active:scale-95">
          <span className="text-2xl">🕐</span>
          <span className="text-sm font-bold text-navy">Historial</span>
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <h2 className="text-sm font-black text-navy">🚀 Trámites</h2>
          <button onClick={() => navigate('/tramites')}
            className="px-3 py-1.5 rounded-lg bg-teal text-white text-xs font-bold hover:bg-teal-hover transition-colors">
            Ver todos
          </button>
        </div>

        {loading ? (
          <div className="py-10 flex justify-center">
            <svg className="w-6 h-6 animate-spin text-teal" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
          </div>
        ) : tramitesDisplay.length > 0 ? (
          <ul className="divide-y divide-gray-100">
            {tramitesDisplay.map(({ ico, name, inst, docs, price, status }) => (
              <li key={name} onClick={() => navigate('/tramites')}
                className="flex items-center gap-3 px-5 py-3.5 hover:bg-gray-50 transition-colors cursor-pointer">
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
        ) : (
          <div className="py-10 text-center">
            <p className="text-sm font-bold text-navy">Sin trámites disponibles</p>
          </div>
        )}
      </div>

    </Layout>
  )
}