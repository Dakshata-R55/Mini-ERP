const API_BASE = '/api/auth'

async function readError(response) {
  try {
    const body = await response.json()
    return body.message || 'Request failed'
  } catch {
    return 'Request failed'
  }
}

export async function login(loginId, password) {
  const response = await fetch(`${API_BASE}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ loginId, password }),
  })

  if (!response.ok) {
    throw new Error(await readError(response))
  }

  return response.json()
}

export async function signup({ loginId, email, password }) {
  const response = await fetch(`${API_BASE}/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ loginId, email, password }),
  })

  if (!response.ok) {
    throw new Error(await readError(response))
  }

  return response.json()
}