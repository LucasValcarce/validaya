import { useState } from 'react'
import Layout from '../components/Layout'

const HISTORIAL_TRAMITES = [
  {
    id: 'tk-4821',
    fecha: '10 Mar 2026',
    nombre: 'Renovación de Cédula de Identidad',
    institucion: 'SEGIP',
    estado: 'completado',
    monto: 'Bs. 3',
  },
  {
    id: 'tk-4710',
    fecha: '02 Feb 2026',
    nombre: 'Partida de Nacimiento',
    institucion: 'SERECI',
    estado: 'completado',
    monto: 'Bs. 5',
  },
  {
    id: 'tk-4633',
    fecha: '20 Ene 2026',
    nombre: 'Certificado de Domicilio',
    institucion: 'Gobierno Municipal',
    estado: 'en_proceso',
    monto: 'Bs. 0',
  },
  {
    id: 'tk-4502',
    fecha: '15 Dic 2025',
    nombre: 'Apertura de Cuenta Bancaria',
    institucion: 'Banco Nacional',
    estado: 'rechazado',
    monto: 'Bs. 0',
  },
]

const ESTADO_LABEL = {
  completado: { label: 'Completado', cls: 'bg-emerald-100 text-emerald-700' },
  en_proceso: { label: 'En proceso', cls: 'bg-amber-100 text-amber-700' },
  rechazado:  { label: 'Rechazado',  cls: 'bg-red-100 text-red-600' },
}

export default function Historial() {
  const [filtro, setFiltro] = useState('')

  const filtrados = HISTORIAL_TRAMITES.filter(t =>
    t.nombre.toLowerCase().includes(filtro.toLowerCase()) ||
    t.institucion.toLowerCase().includes(filtro.toLowerCase()) ||
    t.id.toLowerCase().includes(filtro.toLowerCase())
  )

  const completados = HISTORIAL_TRAMITES.filter(t => t.estado === 'completado').length

  return (
    <Layout title="🕐 Historial de trámites">

      {/* Resumen */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-6">
        <div className="bg-white border border-gray-200 rounded-2xl p-4">
          <p className="text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1">
            Trámites registrados
          </p>
          <p className="text-2xl font-black text-navy">{HISTORIAL_TRAMITES.length}</p>
          <p className="text-xs text-gray-400 mt-1">Últimos 12 meses</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-2xl p-4">
          <p className="text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1">
            Completados
          </p>
          <p className="text-2xl font-black text-emerald-500">{completados}</p>
          <p className="text-xs text-gray-400 mt-1">
            {(completados / HISTORIAL_TRAMITES.length * 100).toFixed(0)}% éxito
          </p>
        </div>
        <div className="bg-white border border-gray-200 rounded-2xl p-4">
          <p className="text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1">
            Monto pagado aprox.
          </p>
          <p className="text-2xl font-black text-navy">Bs. 13</p>
          <p className="text-xs text-gray-400 mt-1">Simulado para demo</p>
        </div>
      </div>

      {/* Filtro */}
      <div className="relative mb-4">
        <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input
          type="text"
          placeholder="Buscar por trámite, institución o N° de ticket…"
          value={filtro}
          onChange={e => setFiltro(e.target.value)}
          className="w-full pl-11 pr-4 py-3 bg-white border-2 border-gray-200 rounded-xl text-sm text-navy
            outline-none focus:border-teal transition-colors placeholder:text-gray-300"
        />
      </div>

      {/* Lista */}
      <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
        <div className="px-5 py-3 border-b border-gray-100 flex items-center justify-between">
          <h2 className="text-sm font-black text-navy">
            Movimientos recientes
          </h2>
          <span className="text-[11px] text-gray-400 font-bold">
            {filtrados.length} resultado{filtrados.length !== 1 && 's'}
          </span>
        </div>

        {filtrados.length > 0 ? (
          <ul className="divide-y divide-gray-100">
            {filtrados.map(item => (
              <li key={item.id} className="px-5 py-3.5 flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-teal-light flex items-center justify-center text-lg flex-shrink-0">
                  🎫
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between gap-2">
                    <p className="text-sm font-bold text-navy truncate">
                      {item.nombre}
                    </p>
                    <span className="text-[11px] text-gray-400 flex-shrink-0">
                      {item.fecha}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {item.institucion} · Ticket {item.id}
                  </p>
                </div>
                <div className="flex flex-col items-end gap-1 flex-shrink-0">
                  <span className="text-[11px] font-bold text-navy">
                    {item.monto}
                  </span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${ESTADO_LABEL[item.estado].cls}`}>
                    {ESTADO_LABEL[item.estado].label}
                  </span>
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <div className="py-12 text-center">
            <div className="text-4xl mb-2">🕐</div>
            <p className="text-sm font-bold text-navy">Sin movimientos</p>
            <p className="text-xs text-gray-400 mt-1">
              Aún no tienes historial de trámites. Cuando completes un trámite aparecerá aquí.
            </p>
          </div>
        )}
      </div>

    </Layout>
  )}