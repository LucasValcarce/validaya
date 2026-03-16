const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

export async function identify(identification, faceBase64) {
  const body = { identification, faceBase64 }
  console.log('[identify] → REQUEST:', { url: `${BASE_URL}/auth/identify`, body: { identification, faceBase64: faceBase64.slice(0, 30) + '…' } })

  const res = await fetch(`${BASE_URL}/auth/identify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const json = await res.json()
  console.log('[identify] ← RESPONSE status:', res.status, '| body:', json)

  // 401 con este mensaje específico = usuario ya tiene contraseña → debe usar /login
  if (res.status === 401 && json.error === 'Usuario ya se verifico antes') {
    console.log('[identify] → usuario ya verificado, debe usar /login')
    return { alreadyVerified: true }
  }

  if (!res.ok) throw new Error(json.message || 'Error al identificar usuario')
  return json.data  // IdentifyResponse: { exists, verified, token, ... }
}

export async function setPassword(token, password) {
  const body = { password }
  console.log('[setPassword] → REQUEST:', { url: `${BASE_URL}/auth/set-password`, token: token?.slice(0, 20) + '…', body })

  const res = await fetch(`${BASE_URL}/auth/set-password`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  })
  const json = await res.json()
  console.log('[setPassword] ← RESPONSE status:', res.status, '| body:', json)

  if (!res.ok) throw new Error(json.message || 'Error al establecer contraseña')
  return json.data
}

export async function login(identification, password, faceBase64) {
  const body = { identification, password, faceBase64 }
  console.log('[login] → REQUEST:', { url: `${BASE_URL}/auth/login`, body: { identification, password, faceBase64: faceBase64.slice(0, 30) + '…' } })

  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const json = await res.json()
  console.log('[login] ← RESPONSE status:', res.status, '| body:', json)

  if (!res.ok) throw new Error(json.message || 'Credenciales inválidas')
  return json.data
}

export async function logout(token) {
  console.log('[logout] → REQUEST:', { url: `${BASE_URL}/auth/logout` })
  await fetch(`${BASE_URL}/auth/logout`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
  })
  localStorage.removeItem('auth_token')
  localStorage.removeItem('auth_user')
}

export function saveSession(authResponse) {
  console.log('[saveSession] guardando:', { userId: authResponse.userId, fullName: authResponse.fullName, passwordSet: authResponse.passwordSet })
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