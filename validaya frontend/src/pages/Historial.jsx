import { useState, useEffect } from 'react'
import Layout from '../components/Layout'
import { apiFetch } from '../services/apiService'

const ESTADO_LABEL = {
  COMPLETED: { label: 'Completado', cls: 'bg-emerald-100 text-emerald-700' },
  APPROVED:  { label: 'Completado', cls: 'bg-emerald-100 text-emerald-700' },
  PENDING:   { label: 'En proceso', cls: 'bg-amber-100 text-amber-700'    },
  PAYMENT_PENDING: { label: 'Pago pendiente', cls: 'bg-amber-100 text-amber-700' },
  IN_REVIEW: { label: 'En revisión', cls: 'bg-amber-100 text-amber-700'   },
  SUBMITTED: { label: 'Enviado',    cls: 'bg-amber-100 text-amber-700'    },
  REJECTED:  { label: 'Rechazado',  cls: 'bg-red-100 text-red-600'        },
  CANCELLED: { label: 'Cancelado',  cls: 'bg-gray-200 text-gray-600'      },
}

function getEstado(status) {
  return ESTADO_LABEL[status] || { label: status, cls: 'bg-gray-200 text-gray-600' }
}

export default function Historial() {
  const [items,   setItems]   = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState('')
  const [filtro,  setFiltro]  = useState('')

  const user   = JSON.parse(localStorage.getItem('auth_user') || '{}')
  const userId = user.userId

  useEffect(() => {
    if (!userId) return
    async function load() {
      setLoading(true)
      setError('')
      try {
        const data = await apiFetch(`/applications/user/${userId}`)
        setItems(data || [])
      } catch (err) {
        console.error('[Historial] error:', err)
        setError(err.message || 'Error al cargar el historial')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [userId])

  const filtrados = items.filter(t =>
    t.procedureName?.toLowerCase().includes(filtro.toLowerCase()) ||
    t.institutionName?.toLowerCase().includes(filtro.toLowerCase()) ||
    t.applicationNumber?.toLowerCase().includes(filtro.toLowerCase())
  )

  const completados  = items.filter(t => ['COMPLETED','APPROVED'].includes(t.status)).length
  const montoTotal   = items.reduce((acc, t) => acc + (parseFloat(t.totalAmount) || 0), 0)

  function formatFecha(dateStr) {
    if (!dateStr) return '—'
    return new Date(dateStr).toLocaleDateString('es-BO', {
      day: '2-digit', month: 'short', year: 'numeric'
    })
  }

  return (
    <Layout title="🕐 Historial de trámites">

      {/* Resumen */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-6">
        <div className="bg-white border border-gray-200 rounded-2xl p-4">
          <p className="text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1">Trámites registrados</p>
          <p className="text-2xl font-black text-navy">{loading ? '…' : items.length}</p>
          <p className="text-xs text-gray-400 mt-1">Total histórico</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-2xl p-4">
          <p className="text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1">Completados</p>
          <p className="text-2xl font-black text-emerald-500">{loading ? '…' : completados}</p>
          <p className="text-xs text-gray-400 mt-1">
            {items.length > 0 ? `${((completados / items.length) * 100).toFixed(0)}% éxito` : '—'}
          </p>
        </div>
        <div className="bg-white border border-gray-200 rounded-2xl p-4">
          <p className="text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1">Monto total pagado</p>
          <p className="text-2xl font-black text-navy">{loading ? '…' : `Bs. ${montoTotal.toFixed(0)}`}</p>
          <p className="text-xs text-gray-400 mt-1">Acumulado</p>
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
          placeholder="Buscar por trámite, institución o N° de solicitud…"
          value={filtro}
          onChange={e => setFiltro(e.target.value)}
          className="w-full pl-11 pr-4 py-3 bg-white border-2 border-gray-200 rounded-xl text-sm text-navy
            outline-none focus:border-teal transition-colors placeholder:text-gray-300"
        />
      </div>

      {/* Lista */}
      <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
        <div className="px-5 py-3 border-b border-gray-100 flex items-center justify-between">
          <h2 className="text-sm font-black text-navy">Movimientos recientes</h2>
          <span className="text-[11px] text-gray-400 font-bold">
            {loading ? '…' : `${filtrados.length} resultado${filtrados.length !== 1 ? 's' : ''}`}
          </span>
        </div>

        {loading && (
          <div className="py-12 flex justify-center gap-3">
            <svg className="w-6 h-6 animate-spin text-teal" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            <p className="text-sm text-gray-400">Cargando historial…</p>
          </div>
        )}

        {error && !loading && (
          <div className="px-5 py-4 flex items-start gap-3 bg-red-50">
            <span className="text-red-500">⚠</span>
            <p className="text-sm text-red-600">{error}</p>
          </div>
        )}

        {!loading && !error && filtrados.length > 0 && (
          <ul className="divide-y divide-gray-100">
            {filtrados.map(item => {
              const estado = getEstado(item.status)
              return (
                <li key={item.id} className="px-5 py-3.5 flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-teal-light flex items-center justify-center text-lg flex-shrink-0">
                    🎫
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <p className="text-sm font-bold text-navy truncate">{item.procedureName || '—'}</p>
                      <span className="text-[11px] text-gray-400 flex-shrink-0">
                        {formatFecha(item.createdAt)}
                      </span>
                    </div>
                    <p className="text-xs text-gray-400 mt-0.5">
                      {item.institutionName} · {item.applicationNumber}
                    </p>
                  </div>
                  <div className="flex flex-col items-end gap-1 flex-shrink-0">
                    <span className="text-[11px] font-bold text-navy">
                      {item.totalAmount ? `Bs. ${parseFloat(item.totalAmount).toFixed(0)}` : 'Bs. 0'}
                    </span>
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${estado.cls}`}>
                      {estado.label}
                    </span>
                  </div>
                </li>
              )
            })}
          </ul>
        )}

        {!loading && !error && filtrados.length === 0 && (
          <div className="py-12 text-center">
            <div className="text-4xl mb-2">🕐</div>
            <p className="text-sm font-bold text-navy">
              {filtro ? 'Sin resultados para esa búsqueda' : 'Sin historial aún'}
            </p>
            <p className="text-xs text-gray-400 mt-1">
              {filtro ? 'Intenta con otro término' : 'Cuando completes un trámite aparecerá aquí.'}
            </p>
          </div>
        )}
      </div>

    </Layout>
  )
}