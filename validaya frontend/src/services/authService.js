const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api/v1'

/**
 * Paso 1 — Primer inicio de sesión
 * POST /auth/identify
 * Body: { identification, faceBase64 }
 * Retorna: { exists, verified, token, userId, fullName, userType, message }
 */
export async function identify(identification, faceBase64) {
  const res = await fetch(`${BASE_URL}/auth/identify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ identification, faceBase64 }),
  })
  const json = await res.json()
  if (!res.ok) throw new Error(json.message || 'Error al identificar usuario')
  return json.data  // IdentifyResponse
}

/**
 * Paso 2 — Primer inicio de sesión: establecer contraseña
 * PUT /auth/set-password
 * Header: Authorization: Bearer <token de identify>
 * Body: { password }
 * Retorna: AuthResponse con token final
 */
export async function setPassword(token, password) {
  const res = await fetch(`${BASE_URL}/auth/set-password`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ password }),
  })
  const json = await res.json()
  if (!res.ok) throw new Error(json.message || 'Error al establecer contraseña')
  return json.data  // AuthResponse
}

/**
 * Login normal (usuario con contraseña ya establecida)
 * POST /auth/login
 * Body: { identification, password, faceBase64 }
 * Retorna: AuthResponse con token de sesión
 */
export async function login(identification, password, faceBase64) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ identification, password, faceBase64 }),
  })
  const json = await res.json()
  if (!res.ok) throw new Error(json.message || 'Credenciales inválidas')
  return json.data  // AuthResponse
}

/**
 * Logout
 * POST /auth/logout
 * Header: Authorization: Bearer <token>
 */
export async function logout(token) {
  await fetch(`${BASE_URL}/auth/logout`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
  })
  localStorage.removeItem('auth_token')
  localStorage.removeItem('auth_user')
}

/** Helpers de sesión */
export function saveSession(authResponse) {
  localStorage.setItem('auth_token', authResponse.token)
  localStorage.setItem('auth_user', JSON.stringify({
    userId:   authResponse.userId,
    fullName: authResponse.fullName,
    email:    authResponse.email,
    userType: authResponse.userType,
  }))
}

export function getToken()   { return localStorage.getItem('auth_token') }
export function getUser()    { return JSON.parse(localStorage.getItem('auth_user') || 'null') }
export function isLoggedIn() { return !!getToken() }