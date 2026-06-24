import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { register } from '../../api'
import './Auth.css'

export default function RegisterPage() {
  const [form, setForm] = useState({ nombre: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login: authLogin } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true); setError('')
    try {
      const r = await register(form.nombre, form.email, form.password)
      authLogin(r.data.token, { nombre: r.data.nombre, email: r.data.email, rol: r.data.rol })
      navigate('/dashboard')
    } catch (err: any) {
      setError(err.response?.data?.error || 'Error al registrarse')
    } finally { setLoading(false) }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="auth-logo-icon">E</div>
          <div className="auth-logo-text">Equilibria</div>
          <div className="auth-logo-sub">Crea tu cuenta gratuita</div>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Nombre completo</label>
            <input className="form-input" placeholder="Milene Ocampo"
              value={form.nombre} onChange={e => setForm({...form, nombre: e.target.value})} required />
          </div>
          <div className="form-group">
            <label className="form-label">Correo institucional</label>
            <input className="form-input" type="email" placeholder="estudiante@upc.edu.pe"
              value={form.email} onChange={e => setForm({...form, email: e.target.value})} required />
          </div>
          <div className="form-group">
            <label className="form-label">Contraseña (mín. 6 caracteres)</label>
            <input className="form-input" type="password" placeholder="••••••" minLength={6}
              value={form.password} onChange={e => setForm({...form, password: e.target.value})} required />
          </div>
          {error && <div className="auth-error">{error}</div>}
          <button className="btn btn-primary btn-full" type="submit" disabled={loading} style={{justifyContent:'center'}}>
            {loading ? 'Creando cuenta...' : 'Crear cuenta'}
          </button>
        </form>
        <div className="auth-footer">
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
        </div>
      </div>
    </div>
  )
}
