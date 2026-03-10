import { useState, useRef } from 'react'
import Layout from '../components/Layout'

const DOCS_ACTIVE = [
  { id: 1, ico: '🪪', name: 'Cédula de Identidad',  meta: 'Vence: 2028',           badge: 'SEGIP verificado',  verified: true,  date: '12 Mar 2024', size: '1.2 MB' },
  { id: 2, ico: '🏠', name: 'Cert. de Domicilio',   meta: 'Actualizado: Ene 2026', badge: null,                verified: false, date: '04 Ene 2026', size: '840 KB' },
  { id: 3, ico: '📜', name: 'Cert. de Nacimiento',  meta: 'Emitido: SERECI',       badge: 'SERECI verificado', verified: true,  date: '01 Jun 2020', size: '2.1 MB' },
  { id: 4, ico: '📸', name: 'Fotografía reciente',  meta: 'Subido: Feb 2026',      badge: null,                verified: false, date: '18 Feb 2026', size: '3.4 MB' },
  { id: 5, ico: '💼', name: 'Cert. de Trabajo',     meta: 'Empresa XYZ · 2025',   badge: null,                verified: false, date: '10 Dic 2025', size: '620 KB' },
]

const DOCS_MISSING = [
  { id: 10, ico: '🏦', name: 'Extracto Bancario',    hint: 'Requerido para: Apertura de Cuenta', urgent: true  },
  { id: 11, ico: '💍', name: 'Cert. de Matrimonio',  hint: 'Opcional según estado civil',        urgent: false },
  { id: 12, ico: '🎓', name: 'Título Universitario', hint: 'Opcional según trámite',             urgent: false },
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

function MissingCard({ doc, onUpload }) {
  return (
    <div
      className={`border-2 border-dashed rounded-2xl p-4 flex flex-col gap-3 cursor-pointer
        transition-all hover:border-teal hover:bg-teal-light/30 group
        ${doc.urgent ? 'border-amber-300 bg-amber-50/50' : 'border-gray-200 bg-gray-50/50'}`}
      onClick={() => onUpload(doc)}
    >
      <span className="text-3xl opacity-40">{doc.ico}</span>
      <div className="flex-1">
        <p className="text-sm font-bold text-gray-500">{doc.name}</p>
        <p className="text-xs text-gray-400 mt-1">{doc.hint}</p>
      </div>
      <div className="flex items-center justify-between pt-2 border-t border-gray-200/60">
        {doc.urgent
          ? <span className="text-[10px] font-bold text-amber-600 bg-amber-100 px-2 py-0.5 rounded-full">Requerido</span>
          : <span className="text-[10px] text-gray-400">Opcional</span>
        }
        <span className="text-[10px] font-bold text-teal opacity-0 group-hover:opacity-100 transition-opacity">
          + Subir
        </span>
      </div>
    </div>
  )
}

function UploadModal({ doc, onClose, onUploaded }) {
  const inputRef              = useRef(null)
  const [file, setFile]       = useState(null)
  const [dragging, setDragging] = useState(false)
  const [uploading, setUploading] = useState(false)

  const handleFile = (f) => {
    if (!f) return
    const allowed = ['application/pdf', 'image/jpeg', 'image/png', 'image/webp']
    if (!allowed.includes(f.type)) { alert('Formato no permitido. Usa PDF, JPG, PNG o WEBP.'); return }
    if (f.size > 10 * 1024 * 1024) { alert('El archivo supera los 10 MB.'); return }
    setFile(f)
  }

  const handleDrop = (e) => {
    e.preventDefault()
    setDragging(false)
    handleFile(e.dataTransfer.files[0])
  }

  const handleSubmit = () => {
    if (!file) return
    setUploading(true)
    // TODO: POST al API de Spring Boot
    setTimeout(() => {
      setUploading(false)
      onUploaded(doc, file)
    }, 1400)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-sm p-0 sm:p-4">
      <div className="w-full sm:max-w-md bg-white rounded-t-2xl sm:rounded-2xl overflow-hidden shadow-2xl">

        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <div>
            <h3 className="font-black text-navy text-sm">Subir documento</h3>
            <p className="text-xs text-gray-400 mt-0.5">{doc.name}</p>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-400 hover:bg-gray-200 transition-colors text-lg"
          >
            ×
          </button>
        </div>

        <div className="px-5 py-5 flex flex-col gap-4">
          <div
            onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
            onDragLeave={() => setDragging(false)}
            onDrop={handleDrop}
            onClick={() => inputRef.current?.click()}
            className={`border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all
              ${dragging
                ? 'border-teal bg-teal-light scale-[1.01]'
                : file
                  ? 'border-emerald-400 bg-emerald-50'
                  : 'border-gray-200 hover:border-teal hover:bg-teal-light/40'
              }`}
          >
            <input
              ref={inputRef}
              type="file"
              accept=".pdf,.jpg,.jpeg,.png,.webp"
              className="hidden"
              onChange={(e) => handleFile(e.target.files[0])}
            />
            {file ? (
              <>
                <div className="text-3xl mb-2">{file.type === 'application/pdf' ? '📄' : '🖼️'}</div>
                <p className="text-sm font-bold text-emerald-700 truncate px-4">{file.name}</p>
                <p className="text-xs text-gray-400 mt-1">{(file.size / 1024).toFixed(0)} KB</p>
                <button
                  type="button"
                  onClick={(e) => { e.stopPropagation(); setFile(null) }}
                  className="mt-2 text-xs text-red-400 hover:text-red-600 font-semibold"
                >
                  Cambiar archivo
                </button>
              </>
            ) : (
              <>
                <div className="text-4xl mb-3">📂</div>
                <p className="text-sm font-bold text-gray-500">Arrastra tu archivo aquí</p>
                <p className="text-xs text-gray-400 mt-1">o haz clic para buscar</p>
                <p className="text-[10px] text-gray-300 mt-3">PDF, JPG, PNG, WEBP · Máx. 10 MB</p>
              </>
            )}
          </div>

          <div className="flex gap-2.5 px-3 py-2.5 bg-teal-light rounded-xl border border-teal/20">
            <span className="flex-shrink-0 text-base">🔒</span>
            <p className="text-xs text-gray-500 leading-relaxed">
              Los documentos son <strong className="text-navy">inmutables</strong> una vez subidos y se usan automáticamente en tus trámites.
            </p>
          </div>

          <div className="flex gap-3">
            <button
              onClick={onClose}
              className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-bold text-sm hover:border-gray-300 transition-colors"
            >
              Cancelar
            </button>
            <button
              onClick={handleSubmit}
              disabled={!file || uploading}
              className={`flex-1 py-3 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2
                ${file && !uploading
                  ? 'bg-teal text-white hover:bg-teal-hover shadow-md'
                  : 'bg-gray-100 text-gray-400 cursor-not-allowed'
                }`}
            >
              {uploading ? (
                <>
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  Subiendo…
                </>
              ) : 'Subir documento'}
            </button>
          </div>
        </div>
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
            <p className="text-[10px]">Se mostrará al conectar la API</p>
          </div>

          <div className="grid grid-cols-2 gap-2">
            {[
              { label: 'Fecha de subida', val: doc.date },
              { label: 'Tamaño',          val: doc.size },
              { label: 'Estado',
                val: doc.verified ? '✓ Verificado' : 'Pendiente',
                color: doc.verified ? 'text-emerald-600' : 'text-amber-600'
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
            <button className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-bold text-sm hover:border-gray-300 transition-colors flex items-center justify-center gap-1.5">
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
  const [activeDocs,  setActiveDocs]  = useState(DOCS_ACTIVE)
  const [missingDocs, setMissingDocs] = useState(DOCS_MISSING)
  const [uploadTarget,  setUploadTarget]  = useState(null)
  const [previewTarget, setPreviewTarget] = useState(null)

  const handleUploaded = (doc, file) => {
    setActiveDocs(prev => [
      ...prev,
      {
        id:       doc.id,
        ico:      doc.ico,
        name:     doc.name,
        meta:     `Subido hoy · ${(file.size / 1024).toFixed(0)} KB`,
        badge:    null,
        verified: false,
        date:     new Date().toLocaleDateString('es-BO', { day: '2-digit', month: 'short', year: 'numeric' }),
        size:     `${(file.size / 1024).toFixed(0)} KB`,
      },
    ])
    setMissingDocs(prev => prev.filter(d => d.id !== doc.id))
    setUploadTarget(null)
  }

  return (
    <Layout
      title="📁 Repositorio de documentos"
      actions={
        <button
          onClick={() => setUploadTarget({ id: 99, ico: '📄', name: 'Nuevo documento', hint: '' })}
          className="px-3 py-1.5 rounded-lg bg-teal text-white text-xs font-bold hover:bg-teal-hover transition-colors whitespace-nowrap"
        >
          + Subir
        </button>
      }
    >
      <div className="flex items-start gap-3 px-4 py-3 bg-teal-light border border-teal/25 rounded-xl mb-6">
        <span className="text-lg flex-shrink-0">🔒</span>
        <p className="text-xs text-gray-600 leading-relaxed">
          Los documentos son <strong className="text-navy">inmutables</strong> y se usan automáticamente cuando inicias un trámite.
        </p>
      </div>

      <div className="flex items-center justify-between mb-2">
        <h2 className="text-sm font-black text-navy">
          Activos <span className="text-gray-400 font-semibold">({activeDocs.length})</span>
        </h2>
        <span className="text-xs text-teal font-bold">
          {activeDocs.length}/{activeDocs.length + missingDocs.length} completados
        </span>
      </div>

      <div className="w-full h-1.5 bg-gray-200 rounded-full mb-5 overflow-hidden">
        <div
          className="h-full bg-teal rounded-full transition-all duration-500"
          style={{ width: `${(activeDocs.length / (activeDocs.length + missingDocs.length)) * 100}%` }}
        />
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 mb-8">
        {activeDocs.map(doc => (
          <DocCard key={doc.id} doc={doc} onPreview={setPreviewTarget} />
        ))}
      </div>

      {missingDocs.length > 0 && (
        <>
          <h2 className="text-sm font-black text-navy mb-3">
            Faltantes <span className="text-gray-400 font-semibold">({missingDocs.length})</span>
          </h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3">
            {missingDocs.map(doc => (
              <MissingCard key={doc.id} doc={doc} onUpload={setUploadTarget} />
            ))}
          </div>
        </>
      )}

      {missingDocs.length === 0 && (
        <div className="text-center py-10">
          <div className="text-4xl mb-3">🎉</div>
          <p className="text-sm font-bold text-navy">¡Repositorio completo!</p>
          <p className="text-xs text-gray-400 mt-1">Tienes todos los documentos disponibles.</p>
        </div>
      )}

      {uploadTarget && (
        <UploadModal doc={uploadTarget} onClose={() => setUploadTarget(null)} onUploaded={handleUploaded} />
      )}
      {previewTarget && (
        <PreviewModal doc={previewTarget} onClose={() => setPreviewTarget(null)} />
      )}
    </Layout>
  )
}