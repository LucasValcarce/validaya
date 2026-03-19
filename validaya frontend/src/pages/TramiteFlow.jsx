import { useState, useEffect, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import Layout from '../components/Layout'
import QrImage from '../components/QrImage'

/* ════════════════════════════════════════════════════
   STEP 1 — VERIFICACIÓN
════════════════════════════════════════════════════ */
function StepVerificacion({ tramite, onAprobado, onRechazado }) {
  const [estado,         setEstado]         = useState('verificando')
  const [docsVerificados, setDocsVerificados] = useState([])
  const [docFallido,     setDocFallido]     = useState(null)

  useEffect(() => {
    const docs = tramite.requiere
    let i = 0
    const interval = setInterval(() => {
      if (i < docs.length) {
        setDocsVerificados(prev => [...prev, docs[i]])
        i++
      } else {
        clearInterval(interval)
        const fallido = docs.find(d => !d.obtenido)
        if (fallido) { setDocFallido(fallido); setEstado('rechazado') }
        else setEstado('aprobado')
      }
    }, 500)
    return () => clearInterval(interval)
  }, [tramite])

  return (
    <div className="max-w-lg mx-auto flex flex-col gap-5">

      <div className={`flex items-center gap-4 px-5 py-4 rounded-2xl border-2 transition-all duration-500
        ${estado === 'verificando' ? 'border-gray-200 bg-gray-50'
          : estado === 'aprobado'  ? 'border-emerald-300 bg-emerald-50'
          : 'border-red-300 bg-red-50'}`}>
        <div className={`w-12 h-12 rounded-full flex items-center justify-center text-xl flex-shrink-0
          ${estado === 'verificando' ? 'bg-gray-200'
            : estado === 'aprobado'  ? 'bg-emerald-500' : 'bg-red-500'}`}>
          {estado === 'verificando'
            ? <svg className="w-6 h-6 text-gray-400 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
            : estado === 'aprobado'
              ? <span className="text-white font-black">✓</span>
              : <span className="text-white font-black">✕</span>}
        </div>
        <div>
          <p className="font-black text-navy text-sm">
            {estado === 'verificando' ? 'Verificando documentos…'
              : estado === 'aprobado' ? '¡Verificación exitosa!' : 'Verificación fallida'}
          </p>
          <p className="text-xs text-gray-400 mt-0.5">
            {estado === 'verificando' ? 'Consultando con la institución'
              : estado === 'aprobado' ? 'Todos los documentos están en orden'
              : `Problema con: ${docFallido?.nombre}`}
          </p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
        <div className="px-5 py-3 border-b border-gray-100">
          <h3 className="text-xs font-black uppercase tracking-wide text-gray-400">Documentos requeridos</h3>
        </div>
        <ul className="divide-y divide-gray-100">
          {tramite.requiere.map((req, idx) => {
            const verificado = estado !== 'verificando'
              ? req
              : docsVerificados.find(d => d?.id === req.id)
            const enProceso = docsVerificados.length === idx && estado === 'verificando'
            return (
              <li key={req.id} className="flex items-center gap-3 px-5 py-3.5">
                <div className={`w-6 h-6 rounded-full flex-shrink-0 flex items-center justify-center text-xs font-black transition-all duration-300
                  ${enProceso ? 'bg-gray-200 animate-pulse'
                    : !verificado ? 'bg-gray-100 text-gray-300'
                    : req.obtenido ? 'bg-emerald-500 text-white' : 'bg-red-500 text-white'}`}>
                  {enProceso ? '…' : !verificado ? '○' : req.obtenido ? '✓' : '✕'}
                </div>
                <span className={`text-xs font-semibold flex-1 transition-colors duration-300
                  ${!verificado ? 'text-gray-400' : req.obtenido ? 'text-navy' : 'text-red-600'}`}>
                  {req.nombre}
                </span>
                {verificado && (
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full flex-shrink-0
                    ${req.obtenido ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-600'}`}>
                    {req.obtenido ? 'OK' : 'Faltante'}
                  </span>
                )}
              </li>
            )
          })}
        </ul>
      </div>

      {estado === 'rechazado' && (
        <div className="flex items-start gap-3 px-4 py-4 bg-red-50 border border-red-300 rounded-2xl">
          <span className="text-xl flex-shrink-0">❌</span>
          <div>
            <p className="text-sm font-black text-red-700">No se puede continuar</p>
            <p className="text-xs text-red-600 mt-1 leading-relaxed">
              El documento <strong>"{docFallido?.nombre}"</strong> no está disponible.
              Debes obtenerlo primero a través del trámite correspondiente.
            </p>
          </div>
        </div>
      )}

      <div className="flex gap-3">
        {estado === 'rechazado' && (
          <>
            <button onClick={onRechazado}
              className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-bold text-sm hover:border-gray-300 transition-colors">
              Volver a trámites
            </button>
            <button onClick={onRechazado}
              className="flex-1 py-3 rounded-xl bg-navy text-white font-bold text-sm hover:bg-navy-light transition-colors">
              Ver mis documentos
            </button>
          </>
        )}
        {estado === 'aprobado' && (
          <button onClick={onAprobado}
            className="w-full py-3 rounded-xl bg-teal text-white font-bold text-sm hover:bg-teal-hover transition-colors shadow-md">
            Continuar al pago →
          </button>
        )}
      </div>
    </div>
  )
}

/* ════════════════════════════════════════════════════
   STEP 2 — PAGO QR
════════════════════════════════════════════════════ */
function StepPago({ tramite, onPagado }) {
  const applicationIdRef = useRef(null)
  const pollingRef       = useRef(null)
  const [intento,   setIntento]   = useState(0)
  const [fase,      setFase]      = useState('iniciando')
  const [segundos,  setSegundos]  = useState(300)
  const [errorMsg,  setErrorMsg]  = useState('')
  const [qrPayload, setQrPayload] = useState(null)  // payload para generar el QR
  const [paymentId, setPaymentId] = useState(null)

  const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
  const token = () => localStorage.getItem('auth_token')

  useEffect(() => {
    async function iniciarPago() {
      try {
        const user = JSON.parse(localStorage.getItem('auth_user') || '{}')
        const userId = user.userId
        if (!userId) throw new Error('No se encontró el usuario en sesión')

        const branchRes = await fetch(`${BASE}/branches/institution/${tramite.institutionId}`,
          { headers: { Authorization: `Bearer ${token()}` } })
        const branchJson = await branchRes.json()
        const branchId = branchJson.data?.[0]?.id
        if (!branchId) throw new Error('No se encontró sucursal disponible para este trámite')

        const appRes = await fetch(`${BASE}/applications?userId=${userId}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token()}` },
          body: JSON.stringify({ procedureId: tramite.id, institutionId: tramite.institutionId, branchId }),
        })
        const appJson = await appRes.json()
        console.log('[StepPago] /applications:', appRes.status, appJson)
        if (!appRes.ok) throw new Error(appJson.error || appJson.message || 'Error al crear solicitud')

        const applicationId = appJson.data?.id
        if (!applicationId) throw new Error('No se recibió ID de solicitud')
        applicationIdRef.current = applicationId   // ← guardamos para el ticket

        const payRes = await fetch(`${BASE}/payments/create`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token()}` },
          body: JSON.stringify({ applicationId, paymentMethod: 'qr' }),
        })
        const payJson = await payRes.json()
        console.log('[StepPago] /payments/create:', payRes.status, payJson)
        if (!payRes.ok) throw new Error(payJson.error || payJson.message || 'Error al generar pago')

        const payment = payJson.data
        setPaymentId(payment.id)

        // qr_base64 es el payload del QR (string) — lo usamos directamente para generar la imagen
        if (payment.qr_base64) {
          setQrPayload(payment.qr_base64)
        } else {
          console.warn('[StepPago] qr_base64 es null')
        }

        setFase('esperando')
      } catch (err) {
        console.error('[StepPago] error:', err)
        setErrorMsg(err.message || 'Error al iniciar el pago')
        setFase('error')
      }
    }
    iniciarPago()
  }, [tramite.id, intento])

  // Countdown
  useEffect(() => {
    if (fase !== 'esperando') return
    const t = setInterval(() => {
      setSegundos(s => { if (s <= 1) { clearInterval(t); return 0 } return s - 1 })
    }, 1000)
    return () => clearInterval(t)
  }, [fase])

  // Polling — empieza a los 7s para que el usuario vea el QR
  useEffect(() => {
    if (fase !== 'esperando' || !paymentId) return
    const delay = setTimeout(() => {
      const t = setInterval(async () => {
        try {
          const res  = await fetch(`${BASE}/payments/${paymentId}/status`,
            { headers: { Authorization: `Bearer ${token()}` } })
          const json = await res.json()
          const status = json.data?.status
          console.log('[StepPago] polling status:', status)
          if (['PAID','paid','COMPLETED','completed','tester'].includes(status)) {
            clearInterval(t)
            setFase('verificando')
            setTimeout(() => {
              setFase('completado')
              setTimeout(() => onPagado(applicationIdRef.current), 1200)
            }, 1500)
          }
        } catch (err) { console.error('[StepPago] polling error:', err) }
      }, 3000)
      pollingRef.current = t
    }, 7000)
    return () => { clearTimeout(delay); if (pollingRef.current) clearInterval(pollingRef.current) }
  }, [fase, paymentId, onPagado])

  const expirado = segundos === 0
  const minutos  = Math.floor(segundos / 60)
  const segs     = segundos % 60

  const handleRegenerarQR = () => {
    setSegundos(300); setQrPayload(null); setPaymentId(null)
    setFase('iniciando'); setIntento(n => n + 1)
  }

  return (
    <div className="max-w-lg mx-auto flex flex-col gap-5">

      <div className="bg-white border border-gray-200 rounded-2xl p-5 flex items-center gap-4">
        <div className="w-12 h-12 rounded-xl bg-teal-light flex items-center justify-center text-2xl flex-shrink-0">
          {tramite.ico}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-black text-navy truncate">{tramite.nombre}</p>
          <p className="text-xs text-gray-400 mt-0.5">{tramite.institucion}</p>
        </div>
        <div className="text-right flex-shrink-0">
          <p className="text-2xl font-black text-navy">{tramite.precio}</p>
          <p className="text-[10px] text-gray-400">Total a pagar</p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
        <div className="px-5 py-3 border-b border-gray-100 flex items-center justify-between">
          <h3 className="text-xs font-black uppercase tracking-wide text-gray-400">Código QR de pago</h3>
          {fase === 'esperando' && !expirado && (
            <span className={`text-xs font-bold px-2 py-1 rounded-full
              ${segundos < 60 ? 'bg-red-100 text-red-600' : 'bg-amber-100 text-amber-700'}`}>
              ⏱ {minutos}:{segs.toString().padStart(2, '0')}
            </span>
          )}
        </div>

        <div className="p-6 flex flex-col items-center gap-4">
          {fase === 'iniciando' && (
            <div className="w-48 h-48 rounded-2xl bg-gray-50 border-2 border-gray-200 flex flex-col items-center justify-center gap-3">
              <svg className="w-10 h-10 text-teal animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              <p className="text-xs font-bold text-gray-400">Generando QR…</p>
            </div>
          )}

          {fase === 'esperando' && qrPayload && !expirado && (
            <div className="w-48 h-48 rounded-2xl border-2 border-gray-200 overflow-hidden">
              <QrImage data={qrPayload} size={192} className="w-full h-full object-contain" />
            </div>
          )}

          {fase === 'esperando' && !qrPayload && !expirado && (
            <div className="w-48 h-48 rounded-2xl bg-gray-50 border-2 border-gray-200 flex flex-col items-center justify-center gap-2">
              <span className="text-4xl">💳</span>
              <p className="text-xs font-bold text-gray-500 text-center px-4">QR no disponible</p>
            </div>
          )}

          {fase === 'verificando' && (
            <div className="w-48 h-48 rounded-2xl bg-teal-light border-2 border-teal flex flex-col items-center justify-center gap-3">
              <svg className="w-10 h-10 text-teal animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              <p className="text-xs font-bold text-teal">Verificando pago…</p>
            </div>
          )}

          {fase === 'completado' && (
            <div className="w-48 h-48 rounded-2xl bg-emerald-50 border-2 border-emerald-300 flex flex-col items-center justify-center gap-2 animate-pop">
              <span className="text-5xl">✅</span>
              <p className="text-sm font-black text-emerald-700">¡Pago recibido!</p>
            </div>
          )}

          {fase === 'esperando' && expirado && (
            <div className="w-48 h-48 rounded-2xl bg-gray-100 border-2 border-gray-300 flex flex-col items-center justify-center gap-2">
              <span className="text-5xl opacity-30">⏰</span>
              <p className="text-xs font-bold text-gray-500">QR expirado</p>
            </div>
          )}

          {fase === 'error' && (
            <div className="w-48 h-48 rounded-2xl bg-red-50 border-2 border-red-300 flex flex-col items-center justify-center gap-2 px-4 text-center">
              <span className="text-4xl">❌</span>
              <p className="text-xs font-bold text-red-600">{errorMsg}</p>
            </div>
          )}

          {fase === 'esperando' && !expirado && (
            <div className="text-center">
              <p className="text-xs font-bold text-navy">Escanea con tu billetera virtual</p>
              <p className="text-[10px] text-gray-400 mt-1">Tigo Money · BancoSol · Stereum</p>
            </div>
          )}
        </div>
      </div>

      {fase === 'esperando' && !expirado && (
        <div className="flex items-start gap-3 px-4 py-3 bg-teal-light border border-teal/20 rounded-xl">
          <span className="flex-shrink-0 text-base">ℹ️</span>
          <p className="text-xs text-gray-600 leading-relaxed">
            El QR se verifica automáticamente. Una vez realizado el pago esta pantalla
            avanzará sola. <strong className="text-navy">No cierres esta ventana.</strong>
          </p>
        </div>
      )}

      {fase === 'esperando' && expirado && (
        <div className="flex items-start gap-3 px-4 py-3 bg-red-50 border border-red-300 rounded-xl">
          <span className="flex-shrink-0 text-base">⏰</span>
          <p className="text-xs text-red-700 leading-relaxed">El QR expiró. Genera uno nuevo para continuar.</p>
        </div>
      )}

      {fase === 'esperando' && expirado && (
        <button onClick={handleRegenerarQR}
          className="w-full py-3 rounded-xl bg-teal text-white font-bold text-sm hover:bg-teal-hover transition-colors shadow-md">
          Regenerar QR
        </button>
      )}

      {fase === 'error' && (
        <button onClick={handleRegenerarQR}
          className="w-full py-3 rounded-xl bg-navy text-white font-bold text-sm hover:bg-navy-light transition-colors">
          Reintentar
        </button>
      )}
    </div>
  )
}

/* ════════════════════════════════════════════════════
   STEP 3 — TICKET
════════════════════════════════════════════════════ */
function StepTicket({ tramite, applicationId }) {
  const navigate             = useNavigate()
  const [ticket,   setTicket]   = useState(null)
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState('')

  const BASE  = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
  const token = () => localStorage.getItem('auth_token')

  const fechaHoy = new Date().toLocaleDateString('es-BO', {
    day: '2-digit', month: 'long', year: 'numeric'
  })

  useEffect(() => {
    async function fetchTicket() {
      if (!applicationId) {
        setError('No se encontró el ID de la solicitud')
        setLoading(false)
        return
      }
      try {
        // Genera el ticket
        await fetch(`${BASE}/tickets/application/${applicationId}`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token()}` },
        })

        // Lo obtiene
        const res  = await fetch(`${BASE}/tickets/application/${applicationId}`,
          { headers: { Authorization: `Bearer ${token()}` } })
        const json = await res.json()
        console.log('[StepTicket] ticket:', json)
        if (!res.ok) throw new Error(json.error || 'Error al obtener ticket')
        setTicket(json.data)
      } catch (err) {
        console.error('[StepTicket] error:', err)
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }
    fetchTicket()
  }, [applicationId])

  const ticketCode = ticket?.code || '—'
  const qrPayload  = ticket?.qrPayload || null

  if (loading) return (
    <div className="max-w-lg mx-auto flex flex-col items-center justify-center py-20 gap-3">
      <svg className="w-10 h-10 animate-spin text-teal" fill="none" viewBox="0 0 24 24">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
      <p className="text-sm text-gray-400">Generando tu ticket…</p>
    </div>
  )

  if (error) return (
    <div className="max-w-lg mx-auto flex flex-col gap-4">
      <div className="flex items-start gap-3 px-4 py-4 bg-red-50 border border-red-300 rounded-2xl">
        <span className="text-xl">❌</span>
        <div>
          <p className="text-sm font-black text-red-700">Error al generar el ticket</p>
          <p className="text-xs text-red-600 mt-1">{error}</p>
        </div>
      </div>
      <button onClick={() => navigate('/home')}
        className="w-full py-3 rounded-xl bg-navy text-white font-bold text-sm hover:bg-navy-light transition-colors">
        Ir al inicio
      </button>
    </div>
  )

  return (
    <div className="max-w-lg mx-auto flex flex-col gap-5">

      <div className="flex flex-col items-center text-center gap-3 py-4">
        <div className="w-16 h-16 rounded-full bg-teal flex items-center justify-center text-3xl shadow-lg animate-pop">
          🎫
        </div>
        <div>
          <h2 className="font-black text-navy text-lg">¡Trámite completado!</h2>
          <p className="text-sm text-gray-400 mt-1">Presenta este ticket en la institución para recoger tu documento.</p>
        </div>
      </div>

      <div className="bg-white border-2 border-gray-200 rounded-2xl overflow-hidden relative">
        <div className="h-2 bg-gradient-to-r from-navy via-teal to-navy" />
        <div className="absolute left-0 top-1/2 -translate-x-1/2 w-5 h-5 rounded-full bg-[#F7F9FC] border-2 border-gray-200" />
        <div className="absolute right-0 top-1/2 translate-x-1/2 w-5 h-5 rounded-full bg-[#F7F9FC] border-2 border-gray-200" />

        <div className="p-5 flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <div>
              <span className="text-[10px] font-bold uppercase tracking-widest text-gray-400">ValidaYa Bolivia</span>
              <p className="text-lg font-black text-navy mt-0.5">{tramite.nombre}</p>
            </div>
            <span className="text-3xl">{tramite.ico}</span>
          </div>

          <div className="border-t-2 border-dashed border-gray-200" />

          <div className="grid grid-cols-2 gap-3">
            {[
              { label: 'N° Ticket',    val: ticketCode,         highlight: true  },
              { label: 'Institución',  val: tramite.institucion, highlight: false },
              { label: 'Fecha',        val: fechaHoy,            highlight: false },
              { label: 'Monto pagado', val: tramite.precio,      highlight: false },
            ].map(({ label, val, highlight }) => (
              <div key={label} className={`rounded-xl px-3 py-2.5 ${highlight ? 'bg-teal-light' : 'bg-gray-50'}`}>
                <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wide">{label}</p>
                <p className={`text-sm font-black mt-0.5 ${highlight ? 'text-teal' : 'text-navy'}`}>{val}</p>
              </div>
            ))}
          </div>

          <div className="border-t-2 border-dashed border-gray-200" />

          <div className="flex items-center gap-4">
            <div className="w-20 h-20 flex-shrink-0 rounded-xl overflow-hidden border border-gray-200 bg-white">
              {qrPayload
                ? <QrImage data={qrPayload} size={80} className="w-full h-full object-contain" />
                : <div className="w-full h-full bg-gray-100 flex items-center justify-center">
                    <svg className="w-5 h-5 animate-spin text-gray-400" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                    </svg>
                  </div>
              }
            </div>
            <div>
              <p className="text-xs font-black text-navy">Presenta este QR en ventanilla</p>
              <p className="text-[11px] text-gray-400 mt-1 leading-relaxed">
                El personal escaneará el código para verificar tu trámite.
              </p>
            </div>
          </div>
        </div>

        <div className="h-2 bg-gradient-to-r from-navy via-teal to-navy" />
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl overflow-hidden">
        <div className="px-5 py-3 border-b border-gray-100">
          <h3 className="text-xs font-black uppercase tracking-wide text-gray-400">
            📍 Dónde recoger tu documento
          </h3>
        </div>
        <div className="p-5 flex flex-col gap-3">
          {[
            { ico: '🏛️', texto: `Dirígete a las oficinas de ${tramite.institucion} más cercana.` },
            { ico: '🎫', texto: `Presenta el ticket ${ticketCode} en la ventanilla de atención.` },
            { ico: '🪪', texto: 'Lleva tu Cédula de Identidad original como respaldo.' },
            { ico: '⏰', texto: `Tiempo estimado de procesamiento: ${tramite.tiempo}.` },
          ].map(({ ico, texto }) => (
            <div key={ico} className="flex items-start gap-3">
              <span className="text-lg flex-shrink-0">{ico}</span>
              <p className="text-xs text-gray-600 leading-relaxed">{texto}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="flex gap-3 pb-4">
        <button onClick={() => navigate('/home')}
          className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-bold text-sm hover:border-gray-300 transition-colors">
          Ir al inicio
        </button>
        <button onClick={() => navigate('/tramites')}
          className="flex-1 py-3 rounded-xl bg-teal text-white font-bold text-sm hover:bg-teal-hover transition-colors shadow-md">
          Otro trámite
        </button>
      </div>
    </div>
  )
}

/* ════════════════════════════════════════════════════
   STEPPER
════════════════════════════════════════════════════ */
const PASOS = [
  { id: 1, label: 'Verificación' },
  { id: 2, label: 'Pago'         },
  { id: 3, label: 'Ticket'       },
]

function Stepper({ paso }) {
  return (
    <div className="flex items-center justify-center gap-0 mb-8">
      {PASOS.map((p, idx) => (
        <div key={p.id} className="flex items-center">
          <div className="flex flex-col items-center gap-1">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-black transition-all duration-300
              ${paso > p.id  ? 'bg-teal text-white'
                : paso === p.id ? 'bg-navy text-white ring-4 ring-navy/20'
                : 'bg-gray-200 text-gray-400'}`}>
              {paso > p.id ? '✓' : p.id}
            </div>
            <span className={`text-[10px] font-bold transition-colors duration-300
              ${paso >= p.id ? 'text-navy' : 'text-gray-400'}`}>
              {p.label}
            </span>
          </div>
          {idx < PASOS.length - 1 && (
            <div className={`w-16 sm:w-24 h-0.5 mb-4 mx-1 transition-all duration-500
              ${paso > p.id ? 'bg-teal' : 'bg-gray-200'}`} />
          )}
        </div>
      ))}
    </div>
  )
}

/* ════════════════════════════════════════════════════
   TRAMITE FLOW — página principal
════════════════════════════════════════════════════ */
export default function TramiteFlow() {
  const navigate  = useNavigate()
  const location  = useLocation()
  const [paso,          setPaso]          = useState(1)
  const [applicationId, setApplicationId] = useState(null)

  const tramite = location.state?.tramite
  if (!tramite) { navigate('/tramites', { replace: true }); return null }

  const titulos = {
    1: '🔍 Verificando documentos',
    2: '💳 Pago del trámite',
    3: '🎫 Ticket de retiro',
  }

  return (
    <Layout title={titulos[paso]}>
      <Stepper paso={paso} />
      {paso === 1 && (
        <StepVerificacion
          tramite={tramite}
          onAprobado={() => setPaso(2)}
          onRechazado={() => navigate('/tramites')}
        />
      )}
      {paso === 2 && (
        <StepPago
          tramite={tramite}
          onPagado={(appId) => { setApplicationId(appId); setPaso(3) }}
        />
      )}
      {paso === 3 && (
        <StepTicket tramite={tramite} applicationId={applicationId} />
      )}
    </Layout>
  )
}