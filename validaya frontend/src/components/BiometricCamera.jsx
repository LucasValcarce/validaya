import { useRef, useState, useEffect, useCallback } from 'react'

/**
 * BiometricCamera
 * ─────────────────────────────────────────────────────────────
 * Props:
 *   onSuccess(imageDataUrl) – called when face capture succeeds
 *   onCancel()              – called when user closes the modal
 */
export default function BiometricCamera({ onSuccess, onCancel }) {
  const videoRef   = useRef(null)
  const canvasRef  = useRef(null)
  const streamRef  = useRef(null)

  const [phase, setPhase] = useState('init')
  // phases: 'init' | 'loading' | 'ready' | 'scanning' | 'success' | 'error'

  const [countdown, setCountdown] = useState(null)
  const [errorMsg,  setErrorMsg]  = useState('')

  /* ── Start camera ───────────────────────────────────── */
  const startCamera = useCallback(async () => {
    setPhase('loading')
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } },
        audio: false,
      })
      streamRef.current = stream
      if (videoRef.current) {
        videoRef.current.srcObject = stream
        videoRef.current.onloadedmetadata = () => {
          videoRef.current.play()
          setPhase('ready')
        }
      }
    } catch (err) {
      setErrorMsg(
        err.name === 'NotAllowedError'
          ? 'Permiso de cámara denegado. Habilita el acceso en la configuración de tu navegador.'
          : 'No se pudo acceder a la cámara. Verifica que esté conectada.'
      )
      setPhase('error')
    }
  }, [])

  /* ── Stop camera ────────────────────────────────────── */
  const stopCamera = useCallback(() => {
    streamRef.current?.getTracks().forEach(t => t.stop())
    streamRef.current = null
  }, [])

  useEffect(() => {
    startCamera()
    return () => stopCamera()
  }, [startCamera, stopCamera])

  /* ── Capture & simulate scan ────────────────────────── */
  const handleCapture = useCallback(() => {
    if (phase !== 'ready') return
    setPhase('scanning')

    // Draw current video frame to hidden canvas
    const canvas = canvasRef.current
    const video  = videoRef.current
    canvas.width  = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0)

    // Simulate countdown 3 → 2 → 1 → success
    let count = 3
    setCountdown(count)
    const timer = setInterval(() => {
      count -= 1
      if (count > 0) {
        setCountdown(count)
      } else {
        clearInterval(timer)
        setCountdown(null)
        setPhase('success')
        stopCamera()
        // Return captured image (can be sent to backend later)
        const imageDataUrl = canvas.toDataURL('image/jpeg', 0.8)
        setTimeout(() => onSuccess(imageDataUrl), 900)
      }
    }, 800)
  }, [phase, stopCamera, onSuccess])

  /* ── Cancel ─────────────────────────────────────────── */
  const handleCancel = useCallback(() => {
    stopCamera()
    onCancel()
  }, [stopCamera, onCancel])

  /* ── Retry ──────────────────────────────────────────── */
  const handleRetry = useCallback(() => {
    setErrorMsg('')
    startCamera()
  }, [startCamera])

  /* ════════════════════════════════════════════════════
     RENDER
  ════════════════════════════════════════════════════ */
  return (
    /* Backdrop */
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
      <div className="relative w-full max-w-md bg-white rounded-2xl overflow-hidden shadow-2xl">

        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <div>
            <h3 className="font-bold text-navy text-base">Verificación Biométrica</h3>
            <p className="text-xs text-gray-400 mt-0.5">Mira al centro de la cámara</p>
          </div>
          <button
            onClick={handleCancel}
            className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-400 hover:bg-gray-200 transition-colors text-lg leading-none"
            aria-label="Cerrar"
          >
            ×
          </button>
        </div>

        {/* Camera area */}
        <div className="relative bg-navy aspect-[4/3] flex items-center justify-center overflow-hidden">

          {/* Video */}
          <video
            ref={videoRef}
            className={`w-full h-full object-cover transition-opacity duration-300 ${
              phase === 'ready' || phase === 'scanning' ? 'opacity-100' : 'opacity-0'
            }`}
            playsInline
            muted
          />

          {/* Hidden canvas for capture */}
          <canvas ref={canvasRef} className="hidden" />

          {/* Overlay: scanning frame */}
          {(phase === 'ready' || phase === 'scanning') && (
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
              {/* Corner brackets */}
              <div
                className={`relative w-52 h-52 transition-all duration-500 ${
                  phase === 'scanning' ? 'scale-105' : ''
                }`}
              >
                {/* Top-left */}
                <span className="absolute top-0 left-0 w-10 h-10 border-t-[3px] border-l-[3px] border-teal rounded-tl-lg" />
                {/* Top-right */}
                <span className="absolute top-0 right-0 w-10 h-10 border-t-[3px] border-r-[3px] border-teal rounded-tr-lg" />
                {/* Bottom-left */}
                <span className="absolute bottom-0 left-0 w-10 h-10 border-b-[3px] border-l-[3px] border-teal rounded-bl-lg" />
                {/* Bottom-right */}
                <span className="absolute bottom-0 right-0 w-10 h-10 border-b-[3px] border-r-[3px] border-teal rounded-br-lg" />

                {/* Scanning bar animation */}
                {phase === 'scanning' && (
                  <span className="absolute left-2 right-2 h-0.5 bg-teal/70 animate-scan shadow-[0_0_8px_rgba(0,184,150,0.8)]" />
                )}
              </div>

              {/* Countdown bubble */}
              {countdown !== null && (
                <div className="absolute top-4 right-4 w-10 h-10 rounded-full bg-teal flex items-center justify-center text-white font-bold text-lg shadow-lg">
                  {countdown}
                </div>
              )}
            </div>
          )}

          {/* Overlay: loading */}
          {phase === 'loading' && (
            <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 text-white">
              <svg className="w-10 h-10 animate-spin text-teal" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
              <p className="text-sm text-gray-300">Iniciando cámara…</p>
            </div>
          )}

          {/* Overlay: success */}
          {phase === 'success' && (
            <div className="absolute inset-0 bg-navy/80 flex flex-col items-center justify-center gap-3">
              <div className="w-20 h-20 rounded-full bg-teal flex items-center justify-center shadow-lg animate-pop">
                <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" strokeWidth="3" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <p className="text-white font-bold text-base">¡Identidad verificada!</p>
            </div>
          )}

          {/* Overlay: error */}
          {phase === 'error' && (
            <div className="absolute inset-0 bg-navy/90 flex flex-col items-center justify-center gap-3 p-6 text-center">
              <div className="w-16 h-16 rounded-full bg-red-500/20 flex items-center justify-center">
                <svg className="w-8 h-8 text-red-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M15 10l-6 6m0-6l6 6" />
                  <circle cx="12" cy="12" r="10" />
                </svg>
              </div>
              <p className="text-white font-semibold text-sm">{errorMsg}</p>
            </div>
          )}
        </div>

        {/* Footer actions */}
        <div className="px-5 py-4">
          {phase === 'error' ? (
            <div className="flex gap-3">
              <button
                onClick={handleCancel}
                className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-semibold text-sm hover:border-gray-300 transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={handleRetry}
                className="flex-1 py-3 rounded-xl bg-teal text-white font-semibold text-sm hover:bg-teal-hover transition-colors"
              >
                Reintentar
              </button>
            </div>
          ) : phase === 'success' ? (
            <div className="py-2 text-center text-sm text-teal font-semibold">
              Redirigiendo…
            </div>
          ) : (
            <div className="flex gap-3">
              <button
                onClick={handleCancel}
                className="flex-1 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-semibold text-sm hover:border-gray-300 transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={handleCapture}
                disabled={phase !== 'ready'}
                className={`flex-1 py-3 rounded-xl font-semibold text-sm transition-all flex items-center justify-center gap-2 ${
                  phase === 'ready'
                    ? 'bg-teal text-white hover:bg-teal-hover shadow-md'
                    : 'bg-gray-100 text-gray-400 cursor-not-allowed'
                }`}
              >
                {phase === 'scanning' ? (
                  <>
                    <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                    </svg>
                    Escaneando…
                  </>
                ) : (
                  <>
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M3 9V5a2 2 0 012-2h4M3 15v4a2 2 0 002 2h4m10-14h4a2 2 0 012 2v4m-6 10h4a2 2 0 002-2v-4" />
                    </svg>
                    Capturar rostro
                  </>
                )}
              </button>
            </div>
          )}

          {phase === 'ready' && (
            <p className="text-center text-xs text-gray-400 mt-3">
              Posiciona tu rostro dentro del marco y presiona <strong>Capturar</strong>
            </p>
          )}
        </div>
      </div>
    </div>
  )
}