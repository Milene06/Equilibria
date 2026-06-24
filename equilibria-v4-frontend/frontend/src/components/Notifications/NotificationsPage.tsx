import { useState } from 'react'
import { setModoDescanso, subscribePush } from '../../api'

export default function NotificationsPage() {
  const [pushEnabled, setPushEnabled] = useState(false)
  const [saved, setSaved] = useState('')

  const togglePush = async () => {
    if (!('Notification' in window)) { alert('Tu navegador no soporta notificaciones push'); return }
    const perm = await Notification.requestPermission()
    if (perm === 'granted') {
      new Notification('Equilibria', { body: '¡Notificaciones push activadas!' })
      await subscribePush(window.location.origin)
      setPushEnabled(true)
      setSaved('Notificaciones push activadas ✓')
    }
  }

  return (
    <div>
      <div className="page-header"><h1 className="page-title">🔔 Notificaciones</h1></div>
      {saved && <div style={{background:'var(--teal-l)',color:'var(--teal2)',padding:'10px 14px',borderRadius:8,fontSize:12,marginBottom:14}}>✓ {saved}</div>}
      <div className="card" style={{marginBottom:16}}>
        <div style={{fontWeight:600,marginBottom:14}}>Configurar recordatorios</div>
        {[
          ['Recordatorios de deadlines','24h antes de la entrega'],
          ['Recordatorios de exámenes','1 semana antes'],
          ['Resumen semanal por correo','Lunes 8:00 AM'],
          ['Alertas de estrés alto','PSS-10 ≥ 27'],
        ].map(([lbl, sub]) => (
          <div key={lbl} style={{display:'flex',justifyContent:'space-between',alignItems:'center',padding:11,background:'var(--surface)',borderRadius:7,marginBottom:7}}>
            <div><div style={{fontSize:12,fontWeight:500}}>{lbl}</div><div style={{fontSize:10,color:'#999'}}>{sub}</div></div>
            <button className="toggle on" onClick={e => (e.target as HTMLElement).classList.toggle('on')}></button>
          </div>
        ))}
        <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',padding:11,background:'var(--surface)',borderRadius:7,marginBottom:7}}>
          <div><div style={{fontSize:12,fontWeight:500}}>Notificaciones push del navegador</div><div style={{fontSize:10,color:'#999'}}>Requiere permiso del navegador</div></div>
          <button className={`toggle${pushEnabled?' on':''}`} onClick={togglePush}></button>
        </div>
      </div>
      <div className="card">
        <div style={{fontWeight:600,marginBottom:12}}>Modo descanso</div>
        <div style={{display:'flex',gap:9}}>
          <button className="btn btn-primary btn-sm" onClick={() => setModoDescanso(true).then(() => setSaved('Modo descanso activado'))}>🔕 Activar descanso</button>
          <button className="btn btn-ghost btn-sm" onClick={() => setModoDescanso(false).then(() => setSaved('Modo descanso desactivado'))}>🔔 Reactivar</button>
        </div>
      </div>
    </div>
  )
}