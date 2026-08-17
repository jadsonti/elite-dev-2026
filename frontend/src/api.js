export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export async function api(path, options = {}) {
  const { token, body, headers, ...requestOptions } = options
  const response = await fetch(`${API_URL}${path}`, {
    ...requestOptions,
    headers: {
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  const contentType = response.headers.get('content-type') || ''
  const data = contentType.includes('application/json')
    ? await response.json()
    : await response.blob()

  if (!response.ok) {
    const message = data?.message || data?.error || 'Não foi possível concluir a operação.'
    throw new Error(message)
  }
  return data
}

export function formatMoney(value) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(Number(value || 0))
}

export function formatDate(value) {
  if (!value) return 'Data a confirmar'
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}
