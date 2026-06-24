import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { login } from '../../api'
import './Auth.css'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login: authLogin } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true); setError('')
    try {
      const r = await login(email, password)
      authLogin(r.data.token, { nombre: r.data.nombre, email: r.data.email, rol: r.data.rol, id: r.data.id })
      navigate('/dashboard')
    } catch (err: any) {
      setError(err.response?.data?.error || 'Credenciales incorrectas')
    } finally { setLoading(false) }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="auth-logo-icon">E</div>
          <div className="auth-logo-text">Equilibria</div>
          <div className="auth-logo-sub">Gestión académica + Bienestar con IA</div>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Correo institucional</label>
            <input className="form-input" type="email" placeholder="estudiante@upc.edu.pe"
              value={email} onChange={e => setEmail(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Contraseña</label>
            <input className="form-input" type="password" placeholder="••••••"
              value={password} onChange={e => setPassword(e.target.value)} required />
          </div>
          {error && <div className="auth-error">{error}</div>}
          <button className="btn btn-primary btn-full" type="submit" disabled={loading} style={{justifyContent:'center'}}>
            {loading ? 'Ingresando...' : 'Iniciar sesión'}
          </button>
        </form>
        <div className="auth-footer">
          ¿No tienes cuenta? <Link to="/register">Regístrate aquí</Link>
        </div>
      </div>
    </div>
  )
}
