import { getToken } from './authService'

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

export async function apiFetch(path, options = {}) {
  const token = getToken()
  console.log(`[apiFetch] ${path} | token:`, token ? token.slice(0, 20) + '…' : 'NINGUNO')

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  })
  const json = await res.json()
  console.log(`[apiFetch] ${path} → status ${res.status} | data:`, json)

  if (!res.ok) throw new Error(json.error || json.message || `Error ${res.status}`)
  return json.data
}