import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import Layout from '../components/Layout'

const TRAMITES = [
  {
    id: 'ci',
    ico: '🪪',
    nombre: 'Renovación de Cédula de Identidad',
    institucion: 'SEGIP',
    descripcion: 'Renovación o primera emisión de la Cédula de Identidad boliviana.',
    precio: 'Bs. 3',
    tiempo: '3-5 días hábiles',
    requiere: [
      { id: 'ci-anterior',  nombre: 'CI anterior o partida de nacimiento',  obtenido: true  },
      { id: 'foto',         nombre: 'Fotografía reciente (tomada en SEGIP)', obtenido: true  },
      { id: 'pago-arancel', nombre: 'Comprobante de pago de arancel',        obtenido: false },
    ],
  },
  {
    id: 'partida-nacimiento',
    ico: '📜',
    nombre: 'Partida de Nacimiento',
    institucion: 'SERECI',
    descripcion: 'Certificado oficial de nacimiento emitido por el Servicio de Registro Cívico.',
    precio: 'Bs. 5',
    tiempo: '1-2 días hábiles',
    requiere: [
      { id: 'ci',          nombre: 'Cédula de Identidad vigente',              obtenido: true },
      { id: 'datos-padre', nombre: 'Datos del padre/madre (nombre completo)',  obtenido: true },
    ],
  },
  {
    id: 'cert-domicilio',
    ico: '🏠',
    nombre: 'Certificado de Domicilio',
    institucion: 'Gobierno Municipal',
    descripcion: 'Documento que certifica el domicilio actual del ciudadano.',
    precio: 'Bs. 0',
    tiempo: '1 día hábil',
    requiere: [
      { id: 'ci',       nombre: 'Cédula de Identidad vigente',              obtenido: true  },
      { id: 'factura',  nombre: 'Factura de servicio básico (luz/agua/gas)', obtenido: false },
      { id: 'alquiler', nombre: 'Contrato de alquiler (si aplica)',          obtenido: true  },
    ],
  },
  {
    id: 'apertura-cuenta',
    ico: '🏦',
    nombre: 'Apertura de Cuenta Bancaria',
    institucion: 'Banco Nacional',
    descripcion: 'Apertura de cuenta de ahorros o corriente en entidad bancaria.',
    precio: 'Bs. 0',
    tiempo: '1 día hábil',
    requiere: [
      { id: 'ci',             nombre: 'Cédula de Identidad vigente',          obtenido: true  },
      { id: 'cert-domicilio', nombre: 'Certificado de Domicilio',             obtenido: true  },
      { id: 'extracto',       nombre: 'Extracto bancario previo (si aplica)', obtenido: false },
      { id: 'cert-trabajo',   nombre: 'Certificado de trabajo o ingresos',    obtenido: false },
    ],
  },
  {
    id: 'extracto-bancario',
    ico: '💳',
    nombre: 'Extracto Bancario',
    institucion: 'Entidad Bancaria',
    descripcion: 'Documento oficial del historial de movimientos bancarios.',
    precio: 'Bs. 0',
    tiempo: 'Inmediato',
    requiere: [
      { id: 'ci',         nombre: 'Cédula de Identidad vigente', obtenido: true  },
      { id: 'num-cuenta', nombre: 'Número de cuenta bancaria',   obtenido: false },
    ],
  },
  {
    id: 'cert-matrimonio',
    ico: '💍',
    nombre: 'Certificado de Matrimonio',
    institucion: 'SERECI',
    descripcion: 'Certificado oficial de matrimonio emitido por el Registro Cívico.',
    precio: 'Bs. 5',
    tiempo: '1-2 días hábiles',
    requiere: [
      { id: 'ci-ambos',  nombre: 'CI de ambos cónyuges',              obtenido: true  },
      { id: 'testigos',  nombre: 'Datos de 2 testigos (CI incluido)', obtenido: false },
    ],
  },
  {
    id: 'seguro-salud',
    ico: '🛡️',
    nombre: 'Afiliación Seguro de Salud',
    institucion: 'Seguros Bolivia',
    descripcion: 'Registro y afiliación al sistema de seguro de salud estatal.',
    precio: 'Bs. 5',
    tiempo: '2-3 días hábiles',
    requiere: [
      { id: 'ci',           nombre: 'Cédula de Identidad vigente', obtenido: true  },
      { id: 'cert-trabajo', nombre: 'Certificado de trabajo',      obtenido: false },
      { id: 'partida',      nombre: 'Partida de nacimiento',       obtenido: true  },
      { id: 'foto',         nombre: 'Fotografía reciente 4×4',     obtenido: true  },
      { id: 'cert-dom',     nombre: 'Certificado de domicilio',    obtenido: true  },
    ],
  },
]

function DetalleModal({ tramite, onClose, onIniciar }) {
  const pendientes = tramite.requiere.filter(r => !r.obtenido)
  const listo      = pendientes.length === 0

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
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-400 hover:bg-gray-200 transition-colors text-lg flex-shrink-0"
          >
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
            <h4 className="text-xs font-black uppercase tracking-wide text-gray-400 mb-2">
              Documentos requeridos
            </h4>
            <ul className="flex flex-col gap-2">
              {tramite.requiere.map(req => (
                <li
                  key={req.id}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl border
                    ${req.obtenido ? 'bg-emerald-50 border-emerald-200' : 'bg-amber-50 border-amber-200'}`}
                >
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
                La verificación lo detectará y te informará durante el proceso.
              </p>
            </div>
          )}

          {listo && (
            <div className="flex items-center gap-2.5 px-4 py-3 bg-emerald-50 border border-emerald-300 rounded-xl">
              <span className="text-base">✅</span>
              <p className="text-xs text-emerald-800 font-semibold">
                Tienes todos los documentos requeridos. ¡Puedes iniciar el trámite!
              </p>
            </div>
          )}
        </div>

        <div className="px-5 py-4 border-t border-gray-100 flex gap-3 flex-shrink-0">
          <button
            onClick={onClose}
            className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-bold text-sm hover:border-gray-300 transition-colors"
          >
            Cancelar
          </button>
          <button
            onClick={() => onIniciar(tramite)}
            className={`flex-1 py-3 rounded-xl font-bold text-sm transition-all shadow-md
              ${listo ? 'bg-teal text-white hover:bg-teal-hover' : 'bg-navy text-white hover:bg-navy-light'}`}
          >
            {listo ? 'Iniciar trámite →' : 'Continuar de todas formas →'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function Tramites() {
  const navigate           = useNavigate()
  const [searchParams]     = useSearchParams()
  const [busqueda, setBusqueda] = useState('')
  const [detalle,  setDetalle]  = useState(null)

  const docParam       = searchParams.get('doc')
  const busquedaActiva = busqueda || (docParam ? TRAMITES.find(t => t.id === docParam)?.nombre || '' : '')

  const filtrados = TRAMITES.filter(t =>
    t.nombre.toLowerCase().includes(busquedaActiva.toLowerCase()) ||
    t.institucion.toLowerCase().includes(busquedaActiva.toLowerCase())
  )

  // Navega al flow pasando el tramite por state
  const handleIniciar = (tramite) => {
    navigate('/tramite-flow', { state: { tramite } })
  }

  return (
    <Layout title="📝 Trámites disponibles">

      <div className="relative mb-6">
        <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input
          type="text"
          placeholder="Buscar trámite o institución…"
          value={busqueda}
          onChange={e => setBusqueda(e.target.value)}
          className="w-full pl-11 pr-4 py-3 bg-white border-2 border-gray-200 rounded-xl text-sm text-navy
            outline-none focus:border-teal transition-colors placeholder:text-gray-300"
        />
      </div>

      {filtrados.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtrados.map(tramite => {
            const pendientes = tramite.requiere.filter(r => !r.obtenido).length
            const listo      = pendientes === 0

            return (
              <div
                key={tramite.id}
                onClick={() => setDetalle(tramite)}
                className="bg-white border border-gray-200 rounded-2xl p-5 flex flex-col gap-4
                  hover:border-teal hover:shadow-md transition-all cursor-pointer group"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="w-12 h-12 rounded-xl bg-teal-light flex items-center justify-center text-2xl flex-shrink-0">
                    {tramite.ico}
                  </div>
                  <span className={`px-2.5 py-1 rounded-full text-[10px] font-black flex-shrink-0 mt-1
                    ${listo ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}`}>
                    {listo ? '✓ Listo' : `${pendientes} pendiente${pendientes > 1 ? 's' : ''}`}
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
          <div className="text-4xl mb-3">🔍</div>
          <p className="text-sm font-bold text-navy">Sin resultados</p>
          <p className="text-xs text-gray-400 mt-1">Intenta con otro nombre o institución</p>
        </div>
      )}

      {detalle && (
        <DetalleModal
          tramite={detalle}
          onClose={() => setDetalle(null)}
          onIniciar={handleIniciar}
        />
      )}
    </Layout>
  )
}