import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'

const DOCS_ACTIVE = [
  { id: 1, ico: '🪪', name: 'Cédula de Identidad',  meta: 'Vence: 2028',           badge: 'SEGIP verificado',  verified: true,  date: '12 Mar 2024', size: '1.2 MB' },
  { id: 2, ico: '🏠', name: 'Cert. de Domicilio',   meta: 'Actualizado: Ene 2026', badge: null,                verified: false, date: '04 Ene 2026', size: '840 KB' },
  { id: 3, ico: '📜', name: 'Cert. de Nacimiento',  meta: 'Emitido: SERECI',       badge: 'SERECI verificado', verified: true,  date: '01 Jun 2020', size: '2.1 MB' },
  { id: 4, ico: '📸', name: 'Fotografía reciente',  meta: 'Subido: Feb 2026',      badge: null,                verified: false, date: '18 Feb 2026', size: '3.4 MB' },
  { id: 5, ico: '💼', name: 'Cert. de Trabajo',     meta: 'Empresa XYZ · 2025',   badge: null,                verified: false, date: '10 Dic 2025', size: '620 KB' },
]

const DOCS_MISSING = [
  { id: 10, ico: '🏦', name: 'Extracto Bancario',    hint: 'Obtenerlo via trámite bancario',       tramiteId: 'extracto-bancario'   },
  { id: 11, ico: '💍', name: 'Cert. de Matrimonio',  hint: 'Obtenerlo via SERECI',                 tramiteId: 'cert-matrimonio'     },
  { id: 12, ico: '🎓', name: 'Título Universitario', hint: 'Obtenerlo via institución educativa',  tramiteId: 'titulo-universitario'},
]

function DocCard({ doc, onPreview }) {
  return (
    <div
      className="bg-white border border-gray-200 rounded-2xl p-4 flex flex-col gap-3
        hover:border-teal hover:shadow-md transition-all cursor-pointer group relative"
      onClick={() => onPreview(doc)}
    >
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
        {doc.badge && (
          <span className="inline-block mt-1.5 px-2 py-0.5 rounded-full bg-teal-light text-teal text-[10px] font-bold">
            {doc.badge}
          </span>
        )}
      </div>
      <div className="flex items-center justify-between pt-2 border-t border-gray-100">
        <span className="text-[10px] text-gray-400">{doc.date} · {doc.size}</span>
        <span className="text-[10px] font-bold text-teal opacity-0 group-hover:opacity-100 transition-opacity">
          Ver →
        </span>
      </div>
    </div>
  )
}

function MissingCard({ doc, onObtener }) {
  return (
    <div
      className="border-2 border-dashed border-gray-200 bg-gray-50/50 rounded-2xl p-4 flex flex-col gap-3
        cursor-pointer transition-all hover:border-teal hover:bg-teal-light/30 group"
      onClick={() => onObtener(doc)}
    >
      <span className="text-3xl opacity-40">{doc.ico}</span>
      <div className="flex-1">
        <p className="text-sm font-bold text-gray-500">{doc.name}</p>
        <p className="text-xs text-gray-400 mt-1">{doc.hint}</p>
      </div>
      <div className="flex items-center justify-between pt-2 border-t border-gray-200/60">
        <span className="text-[10px] text-gray-400">No obtenido</span>
        <span className="text-[10px] font-bold text-teal opacity-0 group-hover:opacity-100 transition-opacity">
          Obtener →
        </span>
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
              <p className="text-xs text-gray-400 mt-0.5">{doc.date} · {doc.size}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-400 hover:bg-gray-200 transition-colors text-lg"
          >
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
              { label: 'Fecha de obtención', val: doc.date },
              { label: 'Tamaño',             val: doc.size },
              {
                label: 'Estado',
                val:   doc.verified ? '✓ Verificado' : 'Pendiente',
                color: doc.verified ? 'text-emerald-600' : 'text-amber-600',
              },
              { label: 'Uso en trámites', val: 'Activo' },
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
            <button
              onClick={onClose}
              className="flex-1 py-3 rounded-xl bg-teal text-white font-bold text-sm hover:bg-teal-hover transition-colors"
            >
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
  const [previewTarget, setPreviewTarget] = useState(null)

  const handleObtener = (doc) => {
    // Navega a tramites con el tramiteId como query param para pre-seleccionarlo
    navigate(`/tramites?doc=${doc.tramiteId}`)
  }

  return (
    <Layout title="📁 Repositorio de documentos">

      {/* Info banner */}
      <div className="flex items-start gap-3 px-4 py-3 bg-teal-light border border-teal/25 rounded-xl mb-6">
        <span className="text-lg flex-shrink-0">ℹ️</span>
        <p className="text-xs text-gray-600 leading-relaxed">
          Los documentos se obtienen a través de los <strong className="text-navy">trámites con instituciones</strong>.
          Una vez verificados, quedan disponibles aquí automáticamente.
        </p>
      </div>

      {/* Active docs */}
      <div className="flex items-center justify-between mb-2">
        <h2 className="text-sm font-black text-navy">
          Obtenidos <span className="text-gray-400 font-semibold">({DOCS_ACTIVE.length})</span>
        </h2>
        <span className="text-xs text-teal font-bold">
          {DOCS_ACTIVE.length}/{DOCS_ACTIVE.length + DOCS_MISSING.length} completados
        </span>
      </div>

      {/* Progress bar */}
      <div className="w-full h-1.5 bg-gray-200 rounded-full mb-5 overflow-hidden">
        <div
          className="h-full bg-teal rounded-full transition-all duration-500"
          style={{ width: `${(DOCS_ACTIVE.length / (DOCS_ACTIVE.length + DOCS_MISSING.length)) * 100}%` }}
        />
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 mb-8">
        {DOCS_ACTIVE.map(doc => (
          <DocCard key={doc.id} doc={doc} onPreview={setPreviewTarget} />
        ))}
      </div>

      {/* Missing docs */}
      {DOCS_MISSING.length > 0 && (
        <>
          <h2 className="text-sm font-black text-navy mb-3">
            Por obtener <span className="text-gray-400 font-semibold">({DOCS_MISSING.length})</span>
          </h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3">
            {DOCS_MISSING.map(doc => (
              <MissingCard key={doc.id} doc={doc} onObtener={handleObtener} />
            ))}
          </div>
        </>
      )}

      {previewTarget && (
        <PreviewModal doc={previewTarget} onClose={() => setPreviewTarget(null)} />
      )}
    </Layout>
  )
}