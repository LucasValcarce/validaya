import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'
import { apiFetch } from '../services/apiService'

const DOC_ICONS_BY_NAME = {
  'Cédula de Identidad':           '🪪',
  'Certificado de Nacimiento':     '📜',
  'Certificado de Matrimonio':     '💍',
  'Certificado de Defunción':      '⚰️',
  'Pasaporte':                     '🛂',
  'Licencia de Conducir':          '🚗',
  'Título Universitario':          '🎓',
  'Certificado de Domicilio':      '🏠',
  'Certificado de Trabajo':        '💼',
  'Extracto Bancario':             '💳',
  'Fotografía Reciente':           '📸',
  'Certificado de Seguro de Salud':'🛡️',
}

function mapUserDoc(doc) {
  return {
    id:        doc.id,
    ico:       DOC_ICONS_BY_NAME[doc.documentTypeName] || '📄',
    name:      doc.documentTypeName,
    meta:      doc.expiryDate
                 ? `Vence: ${new Date(doc.expiryDate).toLocaleDateString('es-BO', { year: 'numeric', month: 'short', day: '2-digit' })}`
                 : doc.issueDate
                   ? `Emitido: ${new Date(doc.issueDate).toLocaleDateString('es-BO', { year: 'numeric', month: 'short', day: '2-digit' })}`
                   : '',
    verified:  doc.verificationStatus === 'verified',
    date:      doc.issueDate
                 ? new Date(doc.issueDate).toLocaleDateString('es-BO', { day: '2-digit', month: 'short', year: 'numeric' })
                 : '—',
    status:    doc.status,
    docNumber: doc.documentNumber,
  }
}

function DocCard({ doc, onPreview }) {
  return (
    <div onClick={() => onPreview(doc)}
      className="bg-white border border-gray-200 rounded-2xl p-4 flex flex-col gap-3
        hover:border-teal hover:shadow-md transition-all cursor-pointer group relative">
      {doc.verified && (
        <span className="absolute top-3 right-3 flex items-center gap-1 px-2 py-0.5
          rounded-full bg-emerald-100 text-emerald-700 text-[10px] font-bold">
          ✓ Verificado
        </span>
      )}
      <span className="text-3xl">{doc.ico}</span>
      <div className="flex-1">
        <p className="text-sm font-bold text-navy leading-tight pr-16">{doc.name}</p>
        <p className="text-xs text-gray-400 mt-1">{doc.meta}</p>
      </div>
      <div className="flex items-center justify-between pt-2 border-t border-gray-100">
        <span className="text-[10px] text-gray-400">{doc.date}</span>
        <span className="text-[10px] font-bold text-teal opacity-0 group-hover:opacity-100 transition-opacity">Ver →</span>
      </div>
    </div>
  )
}

function MissingCard({ doc, onObtener }) {
  return (
    <div onClick={() => onObtener()}
      className="border-2 border-dashed border-gray-200 bg-gray-50/50 rounded-2xl p-4 flex flex-col gap-3
        cursor-pointer transition-all hover:border-teal hover:bg-teal-light/30 group">
      <span className="text-3xl opacity-40">{doc.ico}</span>
      <div className="flex-1">
        <p className="text-sm font-bold text-gray-500">{doc.name}</p>
        <p className="text-xs text-gray-400 mt-1">{doc.hint}</p>
      </div>
      <div className="flex items-center justify-between pt-2 border-t border-gray-200/60">
        <span className="text-[10px] text-gray-400">No obtenido</span>
        <span className="text-[10px] font-bold text-teal opacity-0 group-hover:opacity-100 transition-opacity">Obtener →</span>
      </div>
    </div>
  )
}

function PreviewModal({ doc, onClose }) {
  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-sm p-0 sm:p-4">
      <div className="w-full sm:max-w-md bg-white rounded-t-2xl sm:rounded-2xl overflow-hidden shadow-2xl">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <div className="flex items-center gap-3">
            <span className="text-2xl">{doc.ico}</span>
            <div>
              <h3 className="font-black text-navy text-sm">{doc.name}</h3>
              <p className="text-xs text-gray-400 mt-0.5">{doc.date}</p>
            </div>
          </div>
          <button onClick={onClose}
            className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-400 hover:bg-gray-200 transition-colors text-lg">
            ×
          </button>
        </div>
        <div className="px-5 py-5 flex flex-col gap-3">
          <div className="w-full aspect-[3/4] max-h-64 bg-gray-50 border border-gray-200 rounded-xl
            flex flex-col items-center justify-center gap-2 text-gray-300">
            <span className="text-5xl">{doc.ico}</span>
            <p className="text-xs font-semibold">Vista previa del documento</p>
            <p className="text-[10px]">Disponible al conectar la API</p>
          </div>
          <div className="grid grid-cols-2 gap-2">
            {[
              { label: 'N° Documento',       val: doc.docNumber || '—' },
              { label: 'Fecha de obtención', val: doc.date },
              { label: 'Estado',             val: doc.verified ? '✓ Verificado' : 'Pendiente verificación', color: doc.verified ? 'text-emerald-600' : 'text-amber-600' },
              { label: 'Uso en trámites',    val: 'Activo' },
            ].map(({ label, val, color }) => (
              <div key={label} className="bg-gray-50 rounded-xl px-3 py-2.5">
                <p className="text-[10px] text-gray-400 font-semibold uppercase tracking-wide">{label}</p>
                <p className={`text-xs font-bold mt-0.5 ${color || 'text-navy'}`}>{val}</p>
              </div>
            ))}
          </div>
          <div className="flex gap-3 mt-1">
            <button className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-bold text-sm
              hover:border-gray-300 transition-colors flex items-center justify-center gap-1.5">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              Descargar
            </button>
            <button onClick={onClose}
              className="flex-1 py-3 rounded-xl bg-teal text-white font-bold text-sm hover:bg-teal-hover transition-colors">
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function Docs() {
  const navigate = useNavigate()
  const [docs,          setDocs]          = useState([])
  const [missingDocs,   setMissingDocs]   = useState([])
  const [loading,       setLoading]       = useState(true)
  const [error,         setError]         = useState('')
  const [previewTarget, setPreviewTarget] = useState(null)

  useEffect(() => {
    async function load() {
      setLoading(true)
      setError('')
      try {
        // 1. Carga docs del usuario y summaries de procedimientos en paralelo
        const [docsData, procsData] = await Promise.all([
          apiFetch('/user-documents'),
          apiFetch('/procedures'),
        ])

        const mappedDocs = (docsData || []).map(mapUserDoc)
        setDocs(mappedDocs)

        // 2. Fetch detalle de cada procedimiento para obtener outputDocumentTypeName
        const summaries = procsData || []
        const details   = await Promise.all(
          summaries.map(p => apiFetch(`/procedures/${p.id}`).catch(() => null))
        )

        const activeDocs  = mappedDocs.filter(d => d.status === 'active')
        const userNames   = new Set(activeDocs.map(d => d.name))

        // 3. Filtra procedimientos cuyo outputDocumentTypeName el usuario no tiene
        const seen    = new Set()
        const missing = []
        details.forEach(proc => {
          if (!proc) return
          const outName = proc.outputDocumentTypeName
          if (!outName || userNames.has(outName) || seen.has(outName)) return
          seen.add(outName)
          missing.push({
            name: outName,
            ico:  DOC_ICONS_BY_NAME[outName] || '📄',
            hint: `Obtenerlo via ${proc.institutionName}`,
          })
        })
        setMissingDocs(missing)

      } catch (err) {
        console.error('[Docs] error:', err)
        setError(err.message || 'Error al cargar documentos')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const activeDocs = docs.filter(d => d.status === 'active')

  return (
    <Layout title="📁 Repositorio de documentos">

      <div className="flex items-start gap-3 px-4 py-3 bg-teal-light border border-teal/25 rounded-xl mb-6">
        <span className="text-lg flex-shrink-0">ℹ️</span>
        <p className="text-xs text-gray-600 leading-relaxed">
          Los documentos se obtienen a través de <strong className="text-navy">trámites con instituciones</strong>.
          Una vez verificados, quedan disponibles aquí automáticamente.
        </p>
      </div>

      {loading && (
        <div className="flex items-center justify-center py-20 gap-3">
          <svg className="w-6 h-6 animate-spin text-teal" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
          <p className="text-sm text-gray-400">Cargando documentos…</p>
        </div>
      )}

      {error && !loading && (
        <div className="flex items-start gap-3 px-4 py-4 bg-red-50 border border-red-300 rounded-xl mb-4">
          <span className="text-red-500">⚠</span>
          <p className="text-sm text-red-600">{error}</p>
        </div>
      )}

      {!loading && !error && (
        <>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-sm font-black text-navy">
              Obtenidos <span className="text-gray-400 font-semibold">({activeDocs.length})</span>
            </h2>
            <span className="text-xs text-teal font-bold">
              {activeDocs.length}/{activeDocs.length + missingDocs.length} completados
            </span>
          </div>

          <div className="w-full h-1.5 bg-gray-200 rounded-full mb-5 overflow-hidden">
            <div className="h-full bg-teal rounded-full transition-all duration-500"
              style={{ width: `${activeDocs.length + missingDocs.length > 0
                ? (activeDocs.length / (activeDocs.length + missingDocs.length)) * 100
                : 0}%` }}
            />
          </div>

          {activeDocs.length > 0 ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 mb-8">
              {activeDocs.map(doc => (
                <DocCard key={doc.id} doc={doc} onPreview={setPreviewTarget} />
              ))}
            </div>
          ) : (
            <div className="text-center py-10 mb-8">
              <div className="text-4xl mb-2">📭</div>
              <p className="text-sm font-bold text-navy">Sin documentos aún</p>
              <p className="text-xs text-gray-400 mt-1">Inicia un trámite para obtener tu primer documento</p>
            </div>
          )}

          {missingDocs.length > 0 && (
            <>
              <h2 className="text-sm font-black text-navy mb-3">
                Por obtener <span className="text-gray-400 font-semibold">({missingDocs.length})</span>
              </h2>
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3">
                {missingDocs.map(doc => (
                  <MissingCard key={doc.name} doc={doc} onObtener={() => navigate('/tramites')} />
                ))}
              </div>
            </>
          )}
        </>
      )}

      {previewTarget && (
        <PreviewModal doc={previewTarget} onClose={() => setPreviewTarget(null)} />
      )}
    </Layout>
  )
}