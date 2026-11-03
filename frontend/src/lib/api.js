import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('hiresense_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('hiresense_token')
      localStorage.removeItem('hiresense_user')
    }
    return Promise.reject(error)
  },
)

export const apiError = (error) => {
  if (!error?.response) return 'The HireSense API is unavailable right now. Check the API URL and try again.'
  const body = error.response.data
  const detail = body?.detail || body?.message || body?.error
  if (typeof detail === 'string') return detail
  if (Array.isArray(detail)) return detail.map((item) => item.msg || item.message || String(item)).join(', ')
  return `Request failed (${error.response.status}). Please try again.`
}

export const pageContent = (payload) => {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.content)) return payload.content
  return []
}

export const authApi = {
  login: (payload) => api.post('/api/auth/login', payload),
  register: (payload) => api.post('/api/auth/register', payload),
}

export const resumeApi = {
  upload: (file) => {
    const body = new FormData()
    body.append('file', file)
    return api.post('/api/resumes/upload', body, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  get: (id) => api.get(`/api/resumes/${id}`),
  list: () => api.get('/api/resumes'),
}

export const jobsApi = {
  list: (params) => api.get('/api/jobs', { params }),
  get: (id) => api.get(`/api/jobs/${id}`),
  recommended: () => api.get('/api/jobs/recommended'),
  create: (payload) => api.post('/api/jobs', payload),
  update: (id, payload) => api.put(`/api/jobs/${id}`, payload),
  delete: (id) => api.delete(`/api/jobs/${id}`),
  candidates: (id) => api.get(`/api/jobs/${id}/candidates`),
  apply: (id, resumeId) => api.post(`/api/jobs/${id}/applications`, { resumeId }),
  applicationsMe: () => api.get('/api/jobs/applications/me'),
}

export const matchApi = {
  calculate: (payload) => api.post('/api/match', payload),
  reportPdf: (id) => api.get(`/api/reports/match/${id}/pdf`, { responseType: 'blob' }),
}

export const employeesApi = {
  list: (params) => api.get('/api/employees', { params }),
  create: (payload) => api.post('/api/employees', payload),
  update: (id, payload) => api.put(`/api/employees/${id}`, payload),
  import: (file) => {
    const body = new FormData()
    body.append('file', file)
    return api.post('/api/employees/import', body, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
}

export const attritionApi = {
  predict: (id) => api.post(`/api/attrition/predict/${id}`),
  dashboard: () => api.get('/api/attrition/dashboard'),
}

export const adminApi = {
  users: (params) => api.get('/api/admin/users', { params }),
  stats: () => api.get('/api/admin/stats'),
  activity: (params) => api.get('/api/admin/activity', { params }),
  setUserActive: (id, value) => api.patch(`/api/admin/users/${id}/active`, null, { params: { value } }),
}

export const profileApi = {
  me: () => api.get('/api/profile/me'),
  update: (payload) => api.put('/api/profile/me', payload),
}

export default api
