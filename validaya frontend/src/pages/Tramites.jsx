import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, useLocation } from 'react-router-dom'
import Layout from '../components/Layout'
import { apiFetch } from '../services/apiService'

const ICONS = {
  'CI': '🪪', 'BIRTH_CERT': '📜', 'MARRIAGE_CERT': '💍', 'CERT_DOM': '🏠',
  'CERT_TRABAJO': '💼', 'EXTRACTO_BANCARIO': '💳', 'FOTO': '📸',
  'PASSPORT': '🛂', 'DRIVER_LICENSE': '🚗', 'TITLE': '🎓', 'SEGURO_SALUD': '🛡️',
}

const INST_ICONS = {
  'SEGIP': '🪪', 'SERECI': '📜', 'Gobierno Municipal': '🏠',
  'Banco Nacional de Bolivia': '🏦', 'Seguros Bolivia': '🛡️',
}

function mapProcedure(proc, userDocNames) {
  return {
    id:                  proc.id,
    institutionId:       proc.institutionId,
    backendCode:         proc.slug,                    // ← slug, no code (ProcedureDto no tiene code)
    ico:                 INST_ICONS[proc.institutionName] || '📄',
    nombre:              proc.name,
    institucion:         proc.institutionName,
    descripcion:         proc.description || '',
    precio:              `Bs. ${(parseFloat(proc.basePrice || 0) + parseFloat(proc.platformFee || 0)).toFixed(0)}`,
    tiempo:              proc.estimatedDays === 0
                           ? 'Inmediato'
                           : `${proc.estimatedDays} día${proc.estimatedDays !== 1 ? 's' : ''} hábil${proc.estimatedDays !== 1 ? 'es' : ''}`,
    outputDocName:       proc.outputDocumentTypeName || null,  // ← qué documento genera
    yaCompletado:        proc.outputDocumentTypeName           // ← si el usuario ya lo tiene
                           ? userDocNames.has(proc.outputDocumentTypeName)
                           : false,
    requiere:            (proc.requirements || [])
      .sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0))
      .map(req => ({
        id:       req.documentTypeId,
        nombre:   req.documentTypeName,
        obtenido: userDocNames.has(req.documentTypeName),
      })),
  }
}

function DetalleModal({ tramite, onClose, onIniciar }) {
  const pendientes = tramite.requiere.filter(r => !r.obtenido)
  const listo = pendientes.length === 0

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-sm p-0 sm:p-4">
      <div className="w-full sm:max-w-lg bg-white rounded-t-2xl sm:rounded-2xl overflow-hidden shadow-2xl max-h-[90vh] flex flex-col">

        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100 flex-shrink-0">
          <div className="flex items-center gap-3">
            <span className="text-2xl">{tramite.ico}</span>
            <div>
              <h3 className="font-black text-navy text-sm leading-tight">{tramite.nombre}</h3>
              <p className="text-xs text-gray-400 mt-0.5">{tramite.institucion}</p>
            </div>
          </div>
          <button onClick={onClose}
            className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-400 hover:bg-gray-200 transition-colors text-lg flex-shrink-0">
            ×
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-5 py-5 flex flex-col gap-4">
          <p className="text-sm text-gray-500 leading-relaxed">{tramite.descripcion}</p>

          <div className="flex flex-wrap gap-2">
            <span className="flex items-center gap-1.5 px-3 py-1.5 bg-teal-light rounded-full text-xs font-bold text-teal">
              💳 {tramite.precio}
            </span>
            <span className="flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 rounded-full text-xs font-bold text-gray-600">
              🕐 {tramite.tiempo}
            </span>
            <span className="flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 rounded-full text-xs font-bold text-gray-600">
              🏛️ {tramite.institucion}
            </span>
          </div>

          <div>
            <h4 className="text-xs font-black uppercase tracking-wide text-gray-400 mb-2">Documentos requeridos</h4>
            <ul className="flex flex-col gap-2">
              {tramite.requiere.map(req => (
                <li key={req.id}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl border
                    ${req.obtenido ? 'bg-emerald-50 border-emerald-200' : 'bg-amber-50 border-amber-200'}`}>
                  <span className={`text-base flex-shrink-0 ${req.obtenido ? 'text-emerald-500' : 'text-amber-400'}`}>
                    {req.obtenido ? '✓' : '○'}
                  </span>
                  <span className={`text-xs font-semibold flex-1 ${req.obtenido ? 'text-emerald-800' : 'text-amber-800'}`}>
                    {req.nombre}
                  </span>
                  {req.obtenido
                    ? <span className="text-[10px] font-bold text-emerald-600 bg-emerald-100 px-2 py-0.5 rounded-full flex-shrink-0">Obtenido</span>
                    : <span className="text-[10px] font-bold text-amber-600 bg-amber-100 px-2 py-0.5 rounded-full flex-shrink-0">Pendiente</span>
                  }
                </li>
              ))}
            </ul>
          </div>

          {!listo && (
            <div className="flex items-start gap-2.5 px-4 py-3 bg-amber-50 border border-amber-300 rounded-xl">
              <span className="flex-shrink-0 text-base">⚠️</span>
              <p className="text-xs text-amber-800 leading-relaxed">
                <strong>Faltan {pendientes.length} documento{pendientes.length > 1 ? 's' : ''}.</strong>{' '}
                La verificación lo detectará durante el proceso.
              </p>
            </div>
          )}
          {listo && (
            <div className="flex items-center gap-2.5 px-4 py-3 bg-emerald-50 border border-emerald-300 rounded-xl">
              <span className="text-base">✅</span>
              <p className="text-xs text-emerald-800 font-semibold">
                Tienes todos los documentos. ¡Puedes iniciar el trámite!
              </p>
            </div>
          )}
        </div>

        <div className="px-5 py-4 border-t border-gray-100 flex gap-3 flex-shrink-0">
          <button onClick={onClose}
            className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-bold text-sm hover:border-gray-300 transition-colors">
            Cancelar
          </button>
          <button onClick={() => onIniciar(tramite)}
            className={`flex-1 py-3 rounded-xl font-bold text-sm transition-all shadow-md
              ${listo ? 'bg-teal text-white hover:bg-teal-hover' : 'bg-navy text-white hover:bg-navy-light'}`}>
            {listo ? 'Iniciar trámite →' : 'Continuar de todas formas →'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function Tramites() {
  const navigate       = useNavigate()
  const location       = useLocation()
  const [searchParams] = useSearchParams()

  const [tramites,    setTramites]    = useState([])
  const [loading,     setLoading]     = useState(true)
  const [error,       setError]       = useState('')
  const [busqueda,    setBusqueda]    = useState('')
  const [detalle,     setDetalle]     = useState(null)
  const [showIntro,   setShowIntro]   = useState(() => !!location.state?.nuevo)

  useEffect(() => {
  async function load() {
    setLoading(true)
    setError('')
    try {
      const [summaries, userDocs] = await Promise.all([
        apiFetch('/procedures'),
        apiFetch('/user-documents'),
      ])

      const procedures = await Promise.all(
        (summaries || []).map(s => apiFetch(`/procedures/${s.id}`))
      )

      // LOG: ver qué trae cada procedure del backend
      procedures.forEach(p => {
        console.log(`[Tramites] ${p?.name} → requirements:`, p?.requirements)
      })

      const userDocNames = new Set(
        (userDocs || [])
          .filter(d => d.status === 'active')
          .map(d => d.documentTypeName)
      )

      setTramites(procedures.map(p => mapProcedure(p, userDocNames)))
    } catch (err) {
      console.error('[Tramites] error al cargar:', err)
      setError(err.message || 'Error al cargar trámites')
    } finally {
      setLoading(false)
    }
  }
  load()
}, [])

  const docParam   = searchParams.get('doc')
  
  const tramitesPendientes = tramites.filter(t =>
    !t.yaCompletado &&
    (t.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
     t.institucion.toLowerCase().includes(busqueda.toLowerCase())) &&
    (!docParam || t.backendCode === docParam || t.nombre.toLowerCase().includes(docParam.toLowerCase()))
  )

  const tramitesCompletados = tramites.filter(t =>
    t.yaCompletado &&
    (t.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
     t.institucion.toLowerCase().includes(busqueda.toLowerCase()))
  )

  const handleIniciar = (tramite) => {
    navigate('/tramite-flow', { state: { tramite } })
  }

  return (
    <Layout title="📝 Trámites disponibles">

      {showIntro && (
        <div className="mb-5 rounded-2xl border border-teal/30 bg-teal-light px-4 py-4 sm:px-5 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <p className="text-sm font-black text-navy">Empecemos tu nuevo trámite</p>
            <p className="text-xs text-gray-600 mt-1">Elige un trámite o busca por nombre o institución.</p>
            <div className="flex flex-wrap gap-2 mt-3">
              {tramites.slice(0, 3).map(t => (
                <button key={t.id} type="button" onClick={() => setDetalle(t)}
                  className="px-3 py-1.5 rounded-full bg-white text-[11px] font-bold text-navy border border-gray-200 hover:border-teal hover:text-teal transition-colors">
                  {t.ico} {t.nombre}
                </button>
              ))}
            </div>
          </div>
          <button type="button" onClick={() => setShowIntro(false)}
            className="self-start sm:self-auto px-3 py-2 rounded-xl bg-navy text-white text-xs font-bold hover:bg-navy-light transition-colors">
            Ver todos
          </button>
        </div>
      )}

      <div className="relative mb-6">
        <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input type="text" placeholder="Buscar trámite o institución…"
          value={busqueda} onChange={e => setBusqueda(e.target.value)}
          className="w-full pl-11 pr-4 py-3 bg-white border-2 border-gray-200 rounded-xl text-sm text-navy
            outline-none focus:border-teal transition-colors placeholder:text-gray-300" />
      </div>

      {loading && (
        <div className="flex items-center justify-center py-20 gap-3">
          <svg className="w-6 h-6 animate-spin text-teal" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
          <p className="text-sm text-gray-400">Cargando trámites…</p>
        </div>
      )}

      {error && !loading && (
        <div className="flex items-start gap-3 px-4 py-4 bg-red-50 border border-red-300 rounded-xl mb-4">
          <span className="text-red-500">⚠</span>
          <p className="text-sm text-red-600">{error}</p>
        </div>
      )}

      {/* ── Trámites disponibles ─────────────────────── */}
      {!loading && !error && (
        <>
          {tramitesPendientes.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {tramitesPendientes.map(tramite => {
                const pendientes = tramite.requiere.filter(r => !r.obtenido).length
                const listo = pendientes === 0

                return (
                  <div key={tramite.id} onClick={() => setDetalle(tramite)}
                    className="bg-white border border-gray-200 rounded-2xl p-5 flex flex-col gap-4
                      hover:border-teal hover:shadow-md transition-all cursor-pointer group">

                    <div className="flex items-start justify-between gap-3">
                      <div className="w-12 h-12 rounded-xl bg-teal-light flex items-center justify-center text-2xl flex-shrink-0">
                        {tramite.ico}
                      </div>
                      <span className={`px-2.5 py-1 rounded-full text-[10px] font-black flex-shrink-0 mt-1
                        ${listo ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}`}>
                        {listo ? '✓ Listo para iniciar' : `${pendientes} doc${pendientes > 1 ? 's' : ''} pendiente${pendientes > 1 ? 's' : ''}`}
                      </span>
                    </div>

                    <div className="flex-1">
                      <p className="text-sm font-black text-navy leading-tight">{tramite.nombre}</p>
                      <p className="text-xs text-gray-400 mt-1">{tramite.institucion}</p>
                      <p className="text-xs text-gray-500 mt-2 leading-relaxed line-clamp-2">{tramite.descripcion}</p>
                    </div>

                    <div className="flex flex-col gap-1.5">
                      {tramite.requiere.slice(0, 3).map(req => (
                        <div key={req.id} className="flex items-center gap-2">
                          <span className={`w-3.5 h-3.5 rounded-full flex-shrink-0 flex items-center justify-center text-[9px] font-black
                            ${req.obtenido ? 'bg-emerald-500 text-white' : 'bg-amber-400 text-white'}`}>
                            {req.obtenido ? '✓' : '!'}
                          </span>
                          <span className="text-[11px] text-gray-500 truncate">{req.nombre}</span>
                        </div>
                      ))}
                      {tramite.requiere.length > 3 && (
                        <p className="text-[10px] text-gray-400 pl-5">+{tramite.requiere.length - 3} más…</p>
                      )}
                    </div>

                    <div className="flex items-center justify-between pt-3 border-t border-gray-100">
                      <div className="flex gap-3">
                        <span className="text-[11px] font-bold text-teal">{tramite.precio}</span>
                        <span className="text-[11px] text-gray-400">{tramite.tiempo}</span>
                      </div>
                      <span className="text-[11px] font-bold text-teal opacity-0 group-hover:opacity-100 transition-opacity">
                        Ver detalle →
                      </span>
                    </div>
                  </div>
                )
              })}
            </div>
          ) : (
            <div className="text-center py-16">
              <div className="text-4xl mb-3">{busqueda ? '🔍' : '🎉'}</div>
              <p className="text-sm font-bold text-navy">
                {busqueda ? 'Sin resultados' : '¡Ya completaste todos los trámites disponibles!'}
              </p>
              <p className="text-xs text-gray-400 mt-1">
                {busqueda ? 'Intenta con otro nombre o institución' : 'Revisa tus documentos en el repositorio'}
              </p>
            </div>
          )}

          {/* ── Trámites ya completados (colapsable) ── */}
          {tramitesCompletados.length > 0 && !busqueda && (
            <details className="mt-8 group">
              <summary className="flex items-center gap-2 cursor-pointer select-none list-none mb-4">
                <span className="text-sm font-black text-gray-400">
                  Ya obtenidos ({tramitesCompletados.length})
                </span>
                <span className="text-xs text-gray-300 group-open:hidden">▶</span>
                <span className="text-xs text-gray-300 hidden group-open:inline">▼</span>
              </summary>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {tramitesCompletados.map(tramite => (
                  <div key={tramite.id}
                    className="bg-gray-50 border border-gray-200 rounded-2xl p-5 flex items-center gap-4 opacity-60">
                    <div className="w-10 h-10 rounded-xl bg-emerald-100 flex items-center justify-center text-xl flex-shrink-0">
                      {tramite.ico}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-bold text-navy truncate">{tramite.nombre}</p>
                      <p className="text-xs text-gray-400">{tramite.institucion}</p>
                    </div>
                    <span className="text-[10px] font-bold text-emerald-600 bg-emerald-100 px-2 py-1 rounded-full flex-shrink-0">
                      ✓ Obtenido
                    </span>
                  </div>
                ))}
              </div>
            </details>
          )}
        </>
      )}

      {detalle && (
        <DetalleModal tramite={detalle} onClose={() => setDetalle(null)} onIniciar={handleIniciar} />
      )}
    </Layout>
  )
}