import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BiometricCamera from '../components/BiometricCamera'
import * as authService from '../services/authService'

/*
  FLUJO:
  1. Usuario ingresa CI + contraseña (siempre se muestran ambos campos)
  2. Biometría facial
  3. POST /identify con CI + faceBase64
     - Si passwordSet === false → step 'set-password' (primera vez)
     - Si passwordSet === true  → POST /login con CI + password + faceBase64 → /home
*/

const validateCI = (val) => /^\d{5,10}$/.test(val.trim())

export default function Login() {
  const navigate = useNavigate()

  const [role,     setRole]     = useState('ciudadano') // 'ciudadano' | 'institucion'
  const [step,     setStep]     = useState('credentials') // 'credentials' | 'biometric' | 'set-password'

  const [ci,          setCI]          = useState('')
  const [password,    setPassword]    = useState('')
  const [showPass,    setShowPass]    = useState(false)
  const [newPassword, setNewPassword] = useState('')
  const [confirmPass, setConfirmPass] = useState('')
  const [showNew,     setShowNew]     = useState(false)

  const [errors,   setErrors]   = useState({})
  const [apiError, setApiError] = useState('')
  const [loading,  setLoading]  = useState(false)

  const [tempToken, setTempToken] = useState(null)

  /* ── Validar credenciales → ir a biometría ─────────── */
  const handleSubmit = (e) => {
    e.preventDefault()
    const newErrors = {}
    if (!validateCI(ci)) newErrors.ci = 'La CI debe tener entre 5 y 10 dígitos.'
    setErrors(newErrors)
    if (Object.keys(newErrors).length > 0) return
    setApiError('')
    setStep('biometric')
  }

  /* ── Biometría capturada ───────────────────────────── */
  const handleBiometricSuccess = async (imageDataUrl) => {
  setLoading(true)
  setApiError('')
  const faceBase64 = imageDataUrl.split(',')[1]

  console.log('[handleBiometricSuccess] → llamando /identify primero')

  try {
    const res = await authService.identify(ci.trim(), faceBase64)
    console.log('[handleBiometricSuccess] identify respondió:', res)

    // Caso 1: usuario ya tiene contraseña establecida (faceVerified=true en backend)
    if (res.alreadyVerified) {
      console.log('[handleBiometricSuccess] → usuario ya verificado, llamando /login')
      const loginRes = await authService.login(ci.trim(), password, faceBase64)
      authService.saveSession(loginRes)
      navigate('/home', { replace: true })
      return
    }

    // Caso 2: usuario no existe
    if (!res.exists) {
      setApiError('No se encontró un usuario con esa Cédula de Identidad.')
      setStep('credentials')
      return
    }

    // Caso 3: verificación facial fallida
    if (!res.verified) {
      setApiError(res.message || 'Verificación facial fallida. Intenta de nuevo.')
      setStep('credentials')
      return
    }

    // Caso 4: primera vez — identificado OK, establecer contraseña
    console.log('[handleBiometricSuccess] → primera vez, ir a set-password')
    setTempToken(res.token)
    setStep('set-password')

  } catch (err) {
    console.error('[handleBiometricSuccess] error:', err)
    setApiError(err.message || 'Error al conectar con el servidor.')
    setStep('credentials')
  } finally {
    setLoading(false)
  }
}

  /* ── Establecer contraseña ─────────────────────────── */
  const handleSetPassword = async (e) => {
    e.preventDefault()
    const newErrors = {}
    if (newPassword.length < 8)          newErrors.newPassword = 'Mínimo 8 caracteres.'
    if (newPassword !== confirmPass)      newErrors.confirmPass = 'Las contraseñas no coinciden.'
    setErrors(newErrors)
    if (Object.keys(newErrors).length > 0) return

    setLoading(true)
    setApiError('')
    try {
      const res = await authService.setPassword(tempToken, newPassword)
      authService.saveSession(res)
      navigate('/home', { replace: true })
    } catch (err) {
      setApiError(err.message || 'Error al establecer contraseña.')
    } finally {
      setLoading(false)
    }
  }

  /* ════════════════════════════════════════════════════
     RENDER
  ════════════════════════════════════════════════════ */
  return (
    <div className="min-h-screen bg-navy flex">

      {/* ── LEFT hero ─────────────────────────────── */}
      <div className="hidden lg:flex flex-1 flex-col justify-center px-16 xl:px-24 relative overflow-hidden">
        <div className="absolute -top-32 -left-32 w-96 h-96 rounded-full bg-teal/10 blur-3xl pointer-events-none" />
        <div className="absolute -bottom-24 -right-16 w-80 h-80 rounded-full bg-teal/6 blur-3xl pointer-events-none" />

        <span className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-teal/30 bg-teal/10 text-teal text-xs font-bold tracking-wide w-fit mb-8">
          <span className="w-1.5 h-1.5 rounded-full bg-teal animate-pulse" />
          🇧🇴 Transformación Digital Bolivia
        </span>

        <h1 className="text-5xl xl:text-6xl font-black text-white leading-[1.1] mb-6 tracking-tight">
          Trámites legales<br />
          <span className="text-teal">sin filas</span>,<br />
          sin demoras.
        </h1>

        <p className="text-white/55 text-base leading-relaxed max-w-md mb-10">
          Centraliza, digitaliza y agiliza la revisión documental para instituciones y ciudadanos de Bolivia.
        </p>

        <ul className="flex flex-col gap-4">
          {[
            { ico: '📄', text: 'Repositorio personal de documentos' },
            { ico: '✅', text: 'Verificación automática en tiempo real' },
            { ico: '💳', text: 'Pago digital directo desde la plataforma' },
            { ico: '🎫', text: 'Recoge tu documento sin filas con tu ticket' },
          ].map(({ ico, text }) => (
            <li key={text} className="flex items-center gap-3 text-white/70 text-sm">
              <span className="w-8 h-8 rounded-lg bg-teal/15 flex items-center justify-center text-base flex-shrink-0">{ico}</span>
              {text}
            </li>
          ))}
        </ul>
      </div>

      {/* ── RIGHT card ────────────────────────────── */}
      <div className="w-full lg:w-[440px] bg-white flex items-center justify-center px-6 py-10 sm:px-10">
        <div className="w-full max-w-sm">

          {/* Logo mobile */}
          <div className="lg:hidden mb-8 text-center">
            <span className="text-2xl font-black text-navy">
              Valida<span className="text-teal">Ya</span>
            </span>
          </div>

          {/* ── STEP: credentials ─────────────────── */}
          {(step === 'credentials' || (loading && step === 'credentials')) && (
            <>
              <div className="mb-7">
                <h2 className="text-2xl font-black text-navy">Iniciar sesión</h2>
                <p className="text-sm text-gray-400 mt-1">Accede a tu cuenta o regístrate</p>
              </div>

              {/* Role selector */}
              <div className="grid grid-cols-2 gap-3 mb-7">
                {[
                  { id: 'ciudadano',   label: 'Ciudadano',   ico: '👤' },
                  { id: 'institucion', label: 'Institución', ico: '🏛️' },
                ].map(({ id, label, ico }) => (
                  <button
                    key={id}
                    type="button"
                    onClick={() => setRole(id)}
                    className={`py-3 rounded-xl border-2 text-sm font-bold transition-all
                      ${role === id
                        ? 'border-teal text-teal bg-teal-light'
                        : 'border-gray-200 text-gray-400 hover:border-teal/50'}`}
                  >
                    <span className="block text-xl mb-1">{ico}</span>
                    {label}
                  </button>
                ))}
              </div>

              {/* Error API */}
              {apiError && (
                <div className="flex items-start gap-2 px-3 py-2.5 bg-red-50 border border-red-300 rounded-xl mb-4">
                  <span className="text-red-500 flex-shrink-0">⚠</span>
                  <p className="text-xs text-red-600">{apiError}</p>
                </div>
              )}

              <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">

                {/* CI */}
                <div>
                  <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                    Cédula de Identidad (CI)
                  </label>
                  <input
                    type="text"
                    inputMode="numeric"
                    placeholder="Ej: 12345678"
                    value={ci}
                    maxLength={10}
                    onChange={e => {
                      setCI(e.target.value.replace(/\D/g, ''))
                      setErrors(p => ({ ...p, ci: undefined }))
                    }}
                    className={`w-full px-4 py-3 rounded-xl border-2 text-navy text-sm outline-none transition-colors
                      placeholder:text-gray-300 focus:border-teal
                      ${errors.ci ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
                  />
                  {errors.ci && <p className="text-red-500 text-xs mt-1.5">⚠ {errors.ci}</p>}
                </div>

                {/* Contraseña */}
                <div>
                  <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                    Contraseña
                  </label>
                  <div className="relative">
                    <input
                      type={showPass ? 'text' : 'password'}
                      placeholder="Si es tu primera vez, déjala en blanco"
                      value={password}
                      onChange={e => setPassword(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border-2 border-gray-200 text-navy text-sm outline-none
                        transition-colors pr-11 placeholder:text-gray-300 focus:border-teal"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPass(v => !v)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 p-1"
                    >
                      {showPass ? (
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                        </svg>
                      ) : (
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                          <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                        </svg>
                      )}
                    </button>
                  </div>
                  <p className="text-[10px] text-gray-400 mt-1.5">
                    Si es tu primer ingreso, no necesitas contraseña.
                  </p>
                </div>

                {/* Nota biometría */}
                <div className="flex items-start gap-3 px-3 py-3 bg-teal-light rounded-xl border border-teal/20">
                  <span className="text-xl flex-shrink-0 mt-0.5">📷</span>
                  <div>
                    <p className="text-xs font-bold text-teal">Verificación biométrica requerida</p>
                    <p className="text-xs text-gray-500 mt-0.5 leading-relaxed">
                      Se activará la cámara para confirmar tu identidad facial antes de ingresar.
                    </p>
                  </div>
                </div>

                <button
                  type="submit"
                  className="w-full py-3.5 rounded-xl bg-teal text-white font-bold text-sm
                    hover:bg-teal-hover active:scale-[.98] transition-all shadow-md"
                >
                  Continuar a biometría →
                </button>

                <div className="flex items-center gap-3">
                  <div className="flex-1 h-px bg-gray-200" />
                  <span className="text-xs text-gray-400">o</span>
                  <div className="flex-1 h-px bg-gray-200" />
                </div>

                <button
                  type="button"
                  className="w-full py-3 rounded-xl border-2 border-gray-200 text-navy text-sm font-semibold
                    hover:border-gray-300 transition-colors"
                >
                  Registrarme como ciudadano
                </button>
              </form>
            </>
          )}

          {/* ── STEP: set-password ────────────────── */}
          {step === 'set-password' && (
            <>
              <div className="mb-7">
                <div className="w-12 h-12 rounded-full bg-teal flex items-center justify-center text-white text-xl mb-4">
                  🔐
                </div>
                <h2 className="text-2xl font-black text-navy">Establece tu contraseña</h2>
                <p className="text-sm text-gray-400 mt-1">
                  Identidad verificada. Crea tu contraseña para futuros inicios de sesión.
                </p>
              </div>

              {apiError && (
                <div className="flex items-start gap-2 px-3 py-2.5 bg-red-50 border border-red-300 rounded-xl mb-4">
                  <span className="text-red-500 flex-shrink-0">⚠</span>
                  <p className="text-xs text-red-600">{apiError}</p>
                </div>
              )}

              <form onSubmit={handleSetPassword} noValidate className="flex flex-col gap-5">

                <div>
                  <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                    Nueva contraseña
                  </label>
                  <div className="relative">
                    <input
                      type={showNew ? 'text' : 'password'}
                      placeholder="Mínimo 8 caracteres"
                      value={newPassword}
                      onChange={e => {
                        setNewPassword(e.target.value)
                        setErrors(p => ({ ...p, newPassword: undefined }))
                      }}
                      className={`w-full px-4 py-3 rounded-xl border-2 text-navy text-sm outline-none transition-colors pr-11
                        placeholder:text-gray-300 focus:border-teal
                        ${errors.newPassword ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
                    />
                    <button
                      type="button"
                      onClick={() => setShowNew(v => !v)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 p-1"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    </button>
                  </div>
                  {errors.newPassword && <p className="text-red-500 text-xs mt-1.5">⚠ {errors.newPassword}</p>}
                </div>

                <div>
                  <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                    Confirmar contraseña
                  </label>
                  <input
                    type="password"
                    placeholder="Repite la contraseña"
                    value={confirmPass}
                    onChange={e => {
                      setConfirmPass(e.target.value)
                      setErrors(p => ({ ...p, confirmPass: undefined }))
                    }}
                    className={`w-full px-4 py-3 rounded-xl border-2 text-navy text-sm outline-none transition-colors
                      placeholder:text-gray-300 focus:border-teal
                      ${errors.confirmPass ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
                  />
                  {errors.confirmPass && <p className="text-red-500 text-xs mt-1.5">⚠ {errors.confirmPass}</p>}
                </div>

                {/* Indicador de fortaleza */}
                {newPassword.length > 0 && (
                  <div>
                    <div className="flex gap-1 mb-1">
                      {[1,2,3,4].map(n => (
                        <div
                          key={n}
                          className={`flex-1 h-1 rounded-full transition-colors duration-300
                            ${newPassword.length >= n * 2
                              ? n <= 1 ? 'bg-red-400'
                              : n <= 2 ? 'bg-amber-400'
                              : n <= 3 ? 'bg-teal'
                              : 'bg-emerald-500'
                              : 'bg-gray-200'}`}
                        />
                      ))}
                    </div>
                    <p className="text-[10px] text-gray-400">
                      {newPassword.length < 3 ? 'Muy corta'
                        : newPassword.length < 5 ? 'Débil'
                        : newPassword.length < 7 ? 'Moderada'
                        : 'Segura'}
                    </p>
                  </div>
                )}

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-3.5 rounded-xl bg-teal text-white font-bold text-sm
                    hover:bg-teal-hover active:scale-[.98] transition-all shadow-md
                    disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  {loading ? (
                    <>
                      <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                      </svg>
                      Guardando…
                    </>
                  ) : 'Guardar contraseña y entrar →'}
                </button>

                <button
                  type="button"
                  onClick={() => { setStep('credentials'); setApiError('') }}
                  className="text-xs text-gray-400 hover:text-gray-600 text-center transition-colors"
                >
                  ← Volver
                </button>
              </form>
            </>
          )}

          {/* Loading mientras verifica después de biometría */}
          {loading && step !== 'set-password' && (
            <div className="fixed inset-0 bg-white/80 backdrop-blur-sm z-50 flex flex-col items-center justify-center gap-3">
              <svg className="w-10 h-10 animate-spin text-teal" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
              <p className="text-sm font-semibold text-navy">Verificando identidad…</p>
            </div>
          )}
        </div>
      </div>

      {/* ── Modal biométrico ──────────────────────── */}
      {step === 'biometric' && (
        <BiometricCamera
          onSuccess={handleBiometricSuccess}
          onCancel={() => setStep('credentials')}
        />
      )}
    </div>
  )
}