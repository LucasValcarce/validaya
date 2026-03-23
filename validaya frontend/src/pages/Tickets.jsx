import { useState, useEffect } from 'react'
import Layout from '../components/Layout'
import QrImage from '../components/QrImage'
import { apiFetch } from '../services/apiService'

const ESTADO_TICKET = {
  ACTIVE:   { label: 'Vigente', cls: 'bg-emerald-100 text-emerald-700' },
  USED:     { label: 'Usado',   cls: 'bg-gray-200 text-gray-700'       },
  EXPIRED:  { label: 'Vencido', cls: 'bg-red-100 text-red-600'         },
  PENDING:  { label: 'Vigente', cls: 'bg-emerald-100 text-emerald-700' },
}

function getEstadoTicket(status) {
  return ESTADO_TICKET[status?.toUpperCase()] || { label: status || '—', cls: 'bg-gray-200 text-gray-600' }
}

function formatFecha(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('es-BO', { day: '2-digit', month: 'short', year: 'numeric' })
}

function formatHora(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleTimeString('es-BO', { hour: '2-digit', minute: '2-digit' })
}

export default function Tickets() {
  const [tickets,  setTickets]  = useState([])
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState('')

  const user   = JSON.parse(localStorage.getItem('auth_user') || '{}')
  const userId = user.userId

  useEffect(() => {
    if (!userId) return
    async function load() {
      setLoading(true)
      setError('')
      try {
        // 1. Obtener todas las applications del usuario
        const apps = await apiFetch(`/applications/user/${userId}`)
        if (!apps || apps.length === 0) { setTickets([]); return }

        // 2. Intentar obtener el ticket de cada application
        const results = await Promise.all(
          apps.map(app =>
            apiFetch(`/tickets/application/${app.id}`)
              .then(ticket => ticket ? { ...ticket, procedureName: app.procedureName, institutionName: app.institutionName } : null)
              .catch(() => null)
          )
        )
        setTickets(results.filter(Boolean))
      } catch (err) {
        console.error('[Tickets] error:', err)
        setError(err.message || 'Error al cargar tickets')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [userId])

  const vigentes   = tickets.filter(t => !['USED','EXPIRED'].includes(t.status?.toUpperCase()))
  const historicos = tickets.filter(t =>  ['USED','EXPIRED'].includes(t.status?.toUpperCase()))

  return (
    <Layout title="🎫 Mis tickets">

      <div className="flex items-start gap-3 px-4 py-3 bg-teal-light border border-teal/25 rounded-xl mb-6">
        <span className="text-lg flex-shrink-0">ℹ️</span>
        <p className="text-xs text-gray-600 leading-relaxed">
          Aquí se muestran los tickets generados al completar un pago. Presenta el{' '}
          <strong className="text-navy">código y el QR</strong> en la institución para recoger tu documento.
        </p>
      </div>

      {loading && (
        <div className="flex items-center justify-center py-20 gap-3">
          <svg className="w-6 h-6 animate-spin text-teal" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          <p className="text-sm text-gray-400">Cargando tickets…</p>
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
          {/* Tickets vigentes */}
          <h2 className="text-sm font-black text-navy mb-3">
            Ticket vigente <span className="text-gray-400 font-semibold">({vigentes.length})</span>
          </h2>

          {vigentes.length > 0 ? (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-8">
              {vigentes.map(t => {
                const estado = getEstadoTicket(t.status)
                return (
                  <div key={t.id} className="bg-white border-2 border-emerald-200 rounded-2xl overflow-hidden relative">
                    <div className="h-1.5 bg-gradient-to-r from-navy via-teal to-navy" />
                    <div className="p-5 flex flex-col gap-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-widest text-gray-400">Ticket de atención</p>
                          <p className="text-lg font-black text-navy mt-0.5">{t.procedureName || '—'}</p>
                          <p className="text-xs text-gray-400 mt-0.5">{t.institutionName || '—'}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-[11px] text-gray-400">N° ticket</p>
                          <p className="text-base font-black text-navy">{t.code}</p>
                          <span className={`inline-block mt-1 px-2 py-0.5 rounded-full text-[10px] font-bold ${estado.cls}`}>
                            {estado.label}
                          </span>
                        </div>
                      </div>

                      <div className="border-t-2 border-dashed border-gray-200" />

                      <div className="grid grid-cols-2 gap-3">
                        <div className="rounded-xl px-3 py-2.5 bg-gray-50">
                          <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wide">Fecha</p>
                          <p className="text-sm font-black text-navy mt-0.5">{formatFecha(t.createdAt)}</p>
                        </div>
                        <div className="rounded-xl px-3 py-2.5 bg-gray-50">
                          <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wide">Hora</p>
                          <p className="text-sm font-black text-navy mt-0.5">{formatHora(t.createdAt)}</p>
                        </div>
                        {t.expiryAt && (
                          <div className="rounded-xl px-3 py-2.5 bg-gray-50 col-span-2">
                            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wide">Vence</p>
                            <p className="text-xs font-semibold text-navy mt-0.5">{formatFecha(t.expiryAt)}</p>
                          </div>
                        )}
                      </div>

                      <div className="border-t-2 border-dashed border-gray-200" />

                      <div className="flex justify-center py-2">
                        <div className="flex flex-col items-center gap-2">
                          {t.qrPayload ? (
                            <div className="w-32 h-32 rounded-xl overflow-hidden border border-gray-200 bg-white">
                              <QrImage data={t.qrPayload} size={128} className="w-full h-full object-contain" />
                            </div>
                          ) : (
                            <div className="w-32 h-32 rounded-xl bg-gray-100 flex items-center justify-center">
                              <span className="text-gray-400 text-xs">Sin QR</span>
                            </div>
                          )}
                          <p className="text-xs font-black text-navy">Presenta este QR en ventanilla</p>
                          <p className="text-[11px] text-gray-400">El personal escaneará el código para confirmar tu turno.</p>
                        </div>
                      </div>
                    </div>
                    <div className="h-1.5 bg-gradient-to-r from-navy via-teal to-navy" />
                  </div>
                )
              })}
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
            Historial <span className="text-gray-400 font-semibold">({historicos.length})</span>
          </h2>

          <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
            {historicos.length > 0 ? (
              <ul className="divide-y divide-gray-100">
                {historicos.map(t => {
                  const estado = getEstadoTicket(t.status)
                  return (
                    <li key={t.id} className="px-5 py-3.5 flex items-center gap-3">
                      <div className="w-9 h-9 rounded-xl bg-teal-light flex items-center justify-center text-lg flex-shrink-0">
                        🎫
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between gap-2">
                          <p className="text-sm font-bold text-navy truncate">{t.procedureName || '—'}</p>
                          <span className="text-[11px] text-gray-400 flex-shrink-0">
                            {formatFecha(t.createdAt)} · {formatHora(t.createdAt)}
                          </span>
                        </div>
                        <p className="text-xs text-gray-400 mt-0.5">
                          {t.institutionName} · Ticket {t.code}
                        </p>
                      </div>
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold flex-shrink-0 ${estado.cls}`}>
                        {estado.label}
                      </span>
                    </li>
                  )
                })}
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
        </>
      )}
    </Layout>
  )
}