import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useEffect, useState } from 'react'
import './AppLayout.css'
import logoEquilibria from '../../assets/Equilibria.png'

const NAV = [
  { to:'/dashboard', icon:'🏠', label:'Inicio' },
  { to:'/tareas', icon:'✅', label:'Mis Tareas' },
  { to:'/calendario', icon:'📅', label:'Calendario' },
  { to:'/cursos', icon:'📚', label:'Mis Cursos' },
  { to:'/metas', icon:'🎯', label:'Metas' },
  { to:'/asistencia', icon:'📋', label:'Asistencia' },
  { to:'/bienestar', icon:'💚', label:'Bienestar PSS-10' },
  { to:'/coach', icon:'🧠', label:'Coach IA' },
  { to:'/estadisticas', icon:'📊', label:'Estadísticas' },
  { to:'/notificaciones', icon:'🔔', label:'Notificaciones' },
  { to:'/perfil', icon:'👤', label:'Perfil' },
]

export default function AppLayout() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()
  const [darkMode, setDarkMode] = useState(() => localStorage.getItem('eq_dark') === 'true')
  const [menuOpen, setMenuOpen] = useState(false)

  useEffect(() => {
    document.body.classList.toggle('dark', darkMode)
    localStorage.setItem('eq_dark', String(darkMode))
  }, [darkMode])

  // Cierra el menú al cambiar de ruta
  const handleNavClick = () => setMenuOpen(false)

  const initials = (user?.nombre || 'U').split(' ').map(n => n[0]).slice(0,2).join('').toUpperCase()

  return (
    <div className="layout">
      {/* Overlay oscuro cuando el menú está abierto en móvil */}
      {menuOpen && <div className="sidebar-overlay" onClick={() => setMenuOpen(false)} />}

      <aside className={`sidebar${menuOpen ? ' sidebar-open' : ''}`}>
        <div className="sidebar-logo">
          <img src={logoEquilibria} alt="Equilibria" style={{width:38,height:38,borderRadius:10,objectFit:'cover',flexShrink:0}} />
          <div style={{flex:1}}>
            <div className="logo-title">Equilibria</div>
            <div className="logo-sub">Bienestar + IA</div>
          </div>
          <button className="dark-toggle" onClick={() => setDarkMode(d => !d)} title="Modo oscuro">
            {darkMode ? '☀️' : '🌙'}
          </button>
        </div>
        <nav className="sidebar-nav">
          {NAV.map(n => (
            <NavLink key={n.to} to={n.to} className={({isActive}) => `nav-item${isActive?' active':''}`} onClick={handleNavClick}>
              <span className="nav-icon">{n.icon}</span>{n.label}
            </NavLink>
          ))}
          {isAdmin && (
            <NavLink to="/admin" className={({isActive}) => `nav-item${isActive?' active':''}`} onClick={handleNavClick}>
              <span className="nav-icon">🛡️</span>Administración
            </NavLink>
          )}
        </nav>
        <div className="sidebar-footer">
          <div className="user-chip">
            <div className="user-avatar">{initials}</div>
            <div className="user-info">
              <div className="user-name">{user?.nombre}</div>
              <div className="user-role">{user?.rol}</div>
            </div>
            <button className="logout-btn" onClick={() => { logout(); navigate('/login') }} title="Salir">⎋</button>
          </div>
        </div>
      </aside>

      <div className="main-wrapper">
        {/* Topbar solo en móvil */}
        <header className="mobile-topbar">
          <button className="hamburger" onClick={() => setMenuOpen(o => !o)}>
            <span /><span /><span />
          </button>
          <div style={{display:'flex',alignItems:'center',gap:8}}>
            <img src={logoEquilibria} alt="Equilibria" style={{width:28,height:28,borderRadius:7,objectFit:'cover'}} />
            <span style={{fontWeight:700,fontSize:15,color:'var(--blue)'}}>Equilibria</span>
          </div>
          <button className="dark-toggle" onClick={() => setDarkMode(d => !d)}>
            {darkMode ? '☀️' : '🌙'}
          </button>
        </header>

        <main className="main-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
