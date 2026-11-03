import { createContext, useContext, useMemo, useState } from 'react'
import { authApi, apiError } from '../lib/api'

const AuthContext = createContext(null)
const demoUser = { id: 'sample-001', name: 'Avery Morgan', email: 'avery@hiresense.ai', role: 'CANDIDATE', title: 'Product Designer', location: 'Toronto, Canada' }

const roleName = (value) => {
  const role = String(value || '').toUpperCase().replace(/^ROLE_/, '')
  return ['ADMIN', 'HR', 'CANDIDATE'].includes(role) ? role : 'CANDIDATE'
}

export const normalizeUser = (raw = {}, fallback = {}) => {
  const roles = Array.isArray(raw.roles) ? raw.roles : raw.role ? [raw.role] : fallback.roles || [fallback.role]
  const role = roleName(roles?.[0])
  return {
    id: raw.id ?? fallback.id,
    name: raw.fullName || raw.name || fallback.name || 'HireSense user',
    email: raw.email || fallback.email || '',
    role,
    roles: roles?.map(roleName) || [role],
    active: raw.active ?? fallback.active ?? true,
    title: raw.jobRole || raw.title || fallback.title || role,
    location: raw.location || fallback.location || '',
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('hiresense_token'))
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('hiresense_user')) } catch { return null }
  })
  const [busy, setBusy] = useState(false)

  const persist = (nextToken, nextUser) => {
    localStorage.setItem('hiresense_token', nextToken)
    localStorage.setItem('hiresense_user', JSON.stringify(nextUser))
    setToken(nextToken)
    setUser(nextUser)
  }

  const login = async (credentials) => {
    setBusy(true)
    try {
      const { data } = await authApi.login(credentials)
      persist(data.token, normalizeUser(data.user, { email: credentials.email }))
      return { ok: true }
    } catch (error) {
      return { ok: false, error: apiError(error) }
    } finally { setBusy(false) }
  }

  const register = async (payload) => {
    setBusy(true)
    try {
      const { data } = await authApi.register({ fullName: payload.fullName, email: payload.email, password: payload.password })
      persist(data.token, normalizeUser(data.user, { name: payload.fullName, email: payload.email, role: 'CANDIDATE' }))
      return { ok: true }
    } catch (error) {
      return { ok: false, error: apiError(error) }
    } finally { setBusy(false) }
  }

  const enterDemo = (role = 'CANDIDATE') => {
    const next = { ...demoUser, role, roles: [role], name: role === 'HR' ? 'Jordan Lee' : role === 'ADMIN' ? 'Sam Rivera' : demoUser.name, title: role === 'HR' ? 'Talent Partner' : role === 'ADMIN' ? 'Platform Admin' : demoUser.title }
    persist('sample-workspace-token', next)
  }

  const logout = () => {
    localStorage.removeItem('hiresense_token')
    localStorage.removeItem('hiresense_user')
    localStorage.removeItem('hiresense_latest_resume_id')
    setToken(null)
    setUser(null)
  }

  const value = useMemo(() => ({ token, user, busy, login, register, enterDemo, logout, isAuthenticated: Boolean(token), isDemo: token === 'sample-workspace-token' }), [token, user, busy])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
