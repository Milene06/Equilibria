import { createContext, useContext, useState, useEffect, ReactNode } from 'react'

interface User { nombre: string; email: string; rol: string; id?: number }
interface AuthCtx { user: User | null; token: string | null; login: (token: string, user: User) => void; logout: () => void; isAdmin: boolean }

const Ctx = createContext<AuthCtx>({} as AuthCtx)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    const s = localStorage.getItem('eq_user')
    return s ? JSON.parse(s) : null
  })
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('eq_token'))

  const login = (t: string, u: User) => {
    setToken(t); setUser(u)
    localStorage.setItem('eq_token', t)
    localStorage.setItem('eq_user', JSON.stringify(u))
  }
  const logout = () => {
    setToken(null); setUser(null)
    localStorage.removeItem('eq_token')
    localStorage.removeItem('eq_user')
  }

  return (
    <Ctx.Provider value={{ user, token, login, logout, isAdmin: user?.rol === 'admin' }}>
      {children}
    </Ctx.Provider>
  )
}

export const useAuth = () => useContext(Ctx)
