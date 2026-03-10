import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BiometricCamera from '../components/BiometricCamera'

/* ─── Validation helpers ─────────────────────────────── */
const validateCI = (val) => /^\d{5,10}$/.test(val.trim())
const validatePassword = (val) => val.length >= 6

export default function Login() {
  const navigate = useNavigate()

  const [role,     setRole]     = useState('ciudadano') // 'ciudadano' | 'institucion'
  const [ci,       setCI]       = useState('')
  const [password, setPassword] = useState('')
  const [showPass, setShowPass] = useState(false)

  const [errors,   setErrors]   = useState({})
  const [step,     setStep]     = useState('credentials') // 'credentials' | 'biometric'
  const [loading,  setLoading]  = useState(false)

  /* ── Validate & advance to biometric ─────────────────── */
  const handleSubmit = (e) => {
    e.preventDefault()
    const newErrors = {}
    if (!validateCI(ci))       newErrors.ci       = 'La CI debe tener entre 5 y 10 dígitos.'
    if (!validatePassword(password)) newErrors.password = 'La contraseña debe tener al menos 6 caracteres.'
    setErrors(newErrors)
    if (Object.keys(newErrors).length > 0) return
    setStep('biometric')
  }

  /* ── Biometric success ───────────────────────────────── */
  const handleBiometricSuccess = (_imageDataUrl) => {
    // TODO: send _imageDataUrl + ci + password to Spring Boot API
    setLoading(true)
    // Simulate API verification delay
    setTimeout(() => {
      setLoading(false)
      navigate('/home', { replace: true })
    }, 800)
  }

  /* ── Biometric cancel ────────────────────────────────── */
  const handleBiometricCancel = () => {
    setStep('credentials')
  }

  /* ════════════════════════════════════════════════════
     RENDER
  ════════════════════════════════════════════════════ */
  return (
    <div className="min-h-screen bg-navy flex">

      {/* ── LEFT: Hero (hidden on mobile) ─────────────────── */}
      <div className="hidden lg:flex flex-1 flex-col justify-center px-16 xl:px-24 relative overflow-hidden">
        {/* Background glow effects */}
        <div className="absolute -top-32 -left-32 w-96 h-96 rounded-full bg-teal/10 blur-3xl pointer-events-none" />
        <div className="absolute -bottom-24 -right-16 w-80 h-80 rounded-full bg-teal/6 blur-3xl pointer-events-none" />

        {/* Badge */}
        <span className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-teal/30 bg-teal/10 text-teal text-xs font-bold tracking-wide w-fit mb-8">
          <span className="w-1.5 h-1.5 rounded-full bg-teal animate-pulse" />
          🇧🇴 Transformación Digital Bolivia
        </span>

        {/* Headline */}
        <h1 className="text-5xl xl:text-6xl font-black text-white leading-[1.1] mb-6 tracking-tight">
          Trámites legales<br />
          <span className="text-teal">sin filas</span>,<br />
          sin demoras.
        </h1>

        <p className="text-white/55 text-base leading-relaxed max-w-md mb-10">
          Centraliza, digitaliza y agiliza la revisión documental para instituciones y ciudadanos de Bolivia.
        </p>

        {/* Feature list */}
        <ul className="flex flex-col gap-4">
          {[
            { ico: '📄', text: 'Repositorio personal de documentos' },
            { ico: '✅', text: 'Verificación automática en tiempo real' },
            { ico: '💳', text: 'Pago digital directo desde la plataforma' },
            { ico: '🎫', text: 'Recoge tu documento sin filas con tu ticket' },
          ].map(({ ico, text }) => (
            <li key={text} className="flex items-center gap-3 text-white/70 text-sm">
              <span className="w-8 h-8 rounded-lg bg-teal/15 flex items-center justify-center text-base flex-shrink-0">
                {ico}
              </span>
              {text}
            </li>
          ))}
        </ul>
      </div>

      {/* ── RIGHT: Login card ──────────────────────────────── */}
      <div className="w-full lg:w-[440px] bg-white flex items-center justify-center px-6 py-10 sm:px-10">
        <div className="w-full max-w-sm">

          {/* Logo (mobile only) */}
          <div className="lg:hidden mb-8 text-center">
            <span className="text-2xl font-black text-navy">
              Docu<span className="text-teal">Track</span>
            </span>
          </div>

          {/* Card heading */}
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
                className={`py-3 rounded-xl border-2 text-sm font-bold transition-all ${
                  role === id
                    ? 'border-teal text-teal bg-teal-light'
                    : 'border-gray-200 text-gray-400 hover:border-teal/50'
                }`}
              >
                <span className="block text-xl mb-1">{ico}</span>
                {label}
              </button>
            ))}
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">

            {/* CI field */}
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                Cédula de Identidad (CI)
              </label>
              <input
                type="text"
                inputMode="numeric"
                placeholder="Ej: 12345678"
                value={ci}
                onChange={(e) => {
                  setCI(e.target.value.replace(/\D/g, ''))
                  setErrors(prev => ({ ...prev, ci: undefined }))
                }}
                maxLength={10}
                className={`w-full px-4 py-3 rounded-xl border-2 text-navy text-sm outline-none transition-colors
                  placeholder:text-gray-300 focus:border-teal
                  ${errors.ci ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
              />
              {errors.ci && (
                <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1">
                  <span>⚠</span> {errors.ci}
                </p>
              )}
            </div>

            {/* Password field */}
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                Contraseña
              </label>
              <div className="relative">
                <input
                  type={showPass ? 'text' : 'password'}
                  placeholder="Mínimo 6 caracteres"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value)
                    setErrors(prev => ({ ...prev, password: undefined }))
                  }}
                  className={`w-full px-4 py-3 rounded-xl border-2 text-navy text-sm outline-none transition-colors pr-11
                    placeholder:text-gray-300 focus:border-teal
                    ${errors.password ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
                />
                <button
                  type="button"
                  onClick={() => setShowPass(v => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors p-1"
                  aria-label={showPass ? 'Ocultar contraseña' : 'Mostrar contraseña'}
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
              {errors.password && (
                <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1">
                  <span>⚠</span> {errors.password}
                </p>
              )}
            </div>

            {/* Biometric note */}
            <div className="flex items-start gap-3 px-3 py-3 bg-teal-light rounded-xl border border-teal/20">
              <span className="text-xl flex-shrink-0 mt-0.5">📷</span>
              <div>
                <p className="text-xs font-bold text-teal">Verificación biométrica requerida</p>
                <p className="text-xs text-gray-500 mt-0.5 leading-relaxed">
                  Después de ingresar tus datos, se activará la cámara para confirmar tu identidad facial.
                </p>
              </div>
            </div>

            {/* Submit */}
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
                  Verificando…
                </>
              ) : (
                'Continuar a biometría →'
              )}
            </button>

            {/* Divider */}
            <div className="flex items-center gap-3 my-1">
              <div className="flex-1 h-px bg-gray-200" />
              <span className="text-xs text-gray-400">o</span>
              <div className="flex-1 h-px bg-gray-200" />
            </div>

            {/* Register */}
            <button
              type="button"
              className="w-full py-3 rounded-xl border-2 border-gray-200 text-navy text-sm font-semibold
                hover:border-gray-300 transition-colors"
            >
              Registrarme como ciudadano
            </button>
          </form>

          {/* Footer */}
          <p className="text-center text-xs text-gray-400 mt-6">
            ¿Eres institución?{' '}
            <button
              type="button"
              onClick={() => setRole('institucion')}
              className="text-teal font-semibold hover:underline"
            >
              Accede aquí
            </button>
          </p>
        </div>
      </div>

      {/* ── Biometric modal ──────────────────────────────────── */}
      {step === 'biometric' && (
        <BiometricCamera
          onSuccess={handleBiometricSuccess}
          onCancel={handleBiometricCancel}
        />
      )}
    </div>
  )
}