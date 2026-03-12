import Layout from '../components/Layout'

const TICKETS = [
  {
    id: 'TK-4821',
    fecha: '10 Mar 2026',
    tramite: 'Renovación de Cédula de Identidad',
    institucion: 'SEGIP',
    hora: '09:30',
    estado: 'vigente', // vigente | usado | vencido
    lugar: 'Oficina Central La Paz',
  },
  {
    id: 'TK-4710',
    fecha: '02 Feb 2026',
    tramite: 'Partida de Nacimiento',
    institucion: 'SERECI',
    hora: '15:00',
    estado: 'usado',
    lugar: 'SERECI Miraflores',
  },
  {
    id: 'TK-4633',
    fecha: '20 Ene 2026',
    tramite: 'Certificado de Domicilio',
    institucion: 'Gobierno Municipal',
    hora: '11:15',
    estado: 'vencido',
    lugar: 'Subalcaldía Cotahuma',
  },
]

const ESTADO_TICKET = {
  vigente: { label: 'Vigente', cls: 'bg-emerald-100 text-emerald-700' },
  usado:   { label: 'Usado',   cls: 'bg-gray-200 text-gray-700' },
  vencido: { label: 'Vencido', cls: 'bg-red-100 text-red-600' },
}

export default function Tickets() {
  const vigente = TICKETS.filter(t => t.estado === 'vigente')
  const historico = TICKETS.filter(t => t.estado !== 'vigente')

  return (
    <Layout title="🎫 Mis tickets">

      {/* Info */}
      <div className="flex items-start gap-3 px-4 py-3 bg-teal-light border border-teal/25 rounded-xl mb-6">
        <span className="text-lg flex-shrink-0">ℹ️</span>
        <p className="text-xs text-gray-600 leading-relaxed">
          Aquí se muestran los tickets generados al completar un pago. Presenta el{' '}
          <strong className="text-navy">código y el QR</strong> en la institución para recoger tu documento.
        </p>
      </div>

      {/* Ticket vigente */}
      <h2 className="text-sm font-black text-navy mb-3">
        Ticket vigente <span className="text-gray-400 font-semibold">({vigente.length})</span>
      </h2>

      {vigente.length > 0 ? (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-8">
          {vigente.map(t => (
            <div
              key={t.id}
              className="bg-white border-2 border-emerald-200 rounded-2xl overflow-hidden relative"
            >
              <div className="h-1.5 bg-gradient-to-r from-navy via-teal to-navy" />

              <div className="p-5 flex flex-col gap-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-[10px] font-bold uppercase tracking-widest text-gray-400">
                      Ticket de atención
                    </p>
                    <p className="text-lg font-black text-navy mt-0.5">{t.tramite}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{t.institucion}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-[11px] text-gray-400">N° ticket</p>
                    <p className="text-base font-black text-navy">{t.id}</p>
                    <span className={`inline-block mt-1 px-2 py-0.5 rounded-full text-[10px] font-bold ${ESTADO_TICKET[t.estado].cls}`}>
                      {ESTADO_TICKET[t.estado].label}
                    </span>
                  </div>
                </div>

                <div className="border-t-2 border-dashed border-gray-200" />

                <div className="grid grid-cols-2 gap-3">
                  <div className="rounded-xl px-3 py-2.5 bg-gray-50">
                    <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wide">Fecha</p>
                    <p className="text-sm font-black text-navy mt-0.5">{t.fecha}</p>
                  </div>
                  <div className="rounded-xl px-3 py-2.5 bg-gray-50">
                    <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wide">Hora</p>
                    <p className="text-sm font-black text-navy mt-0.5">{t.hora}</p>
                  </div>
                  <div className="rounded-xl px-3 py-2.5 bg-gray-50 col-span-2">
                    <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wide">Lugar</p>
                    <p className="text-xs font-semibold text-navy mt-0.5">{t.lugar}</p>
                  </div>
                </div>

                <div className="border-t-2 border-dashed border-gray-200" />

                <div className="flex items-center gap-4">
                  <div className="w-20 h-20 flex-shrink-0 bg-navy rounded-xl p-2">
                    <div className="w-full h-full grid grid-cols-5 gap-px">
                      {Array.from({ length: 25 }).map((_, i) => (
                        <div
                          key={i}
                          className={`rounded-[1px] ${
                            [0,1,2,3,4,5,9,10,14,15,19,20,21,22,23,24,7,17].includes(i)
                              ? 'bg-white' : 'bg-navy'
                          }`}
                        />
                      ))}
                    </div>
                  </div>
                  <div>
                    <p className="text-xs font-black text-navy">Presenta este QR en ventanilla</p>
                    <p className="text-[11px] text-gray-400 mt-1 leading-relaxed">
                      El personal de la institución escaneará el código para confirmar tu turno.
                    </p>
                  </div>
                </div>
              </div>

              <div className="h-1.5 bg-gradient-to-r from-navy via-teal to-navy" />
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-white border border-gray-200 rounded-2xl p-8 mb-8 text-center">
          <div className="text-4xl mb-2">🎫</div>
          <p className="text-sm font-bold text-navy">No tienes tickets activos</p>
          <p className="text-xs text-gray-400 mt-1">
            Cuando completes el pago de un trámite, generaremos automáticamente un ticket aquí.
          </p>
        </div>
      )}

      {/* Historial de tickets */}
      <h2 className="text-sm font-black text-navy mb-3">
        Historial <span className="text-gray-400 font-semibold">({historico.length})</span>
      </h2>

      <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
        {historico.length > 0 ? (
          <ul className="divide-y divide-gray-100">
            {historico.map(t => (
              <li key={t.id} className="px-5 py-3.5 flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-teal-light flex items-center justify-center text-lg flex-shrink-0">
                  🎫
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between gap-2">
                    <p className="text-sm font-bold text-navy truncate">
                      {t.tramite}
                    </p>
                    <span className="text-[11px] text-gray-400 flex-shrink-0">
                      {t.fecha} · {t.hora}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {t.institucion} · {t.lugar} · Ticket {t.id}
                  </p>
                </div>
                <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold flex-shrink-0 ${ESTADO_TICKET[t.estado].cls}`}>
                  {ESTADO_TICKET[t.estado].label}
                </span>
              </li>
            ))}
          </ul>
        ) : (
          <div className="py-10 text-center">
            <p className="text-sm font-bold text-navy">Sin historial</p>
            <p className="text-xs text-gray-400 mt-1">
              A medida que uses tus tickets, los verás listados aquí.
            </p>
          </div>
        )}
      </div>

    </Layout>
  )
}

