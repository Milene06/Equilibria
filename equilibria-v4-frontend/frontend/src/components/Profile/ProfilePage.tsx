import { useEffect, useState } from 'react'
import { getPreferencias, setModoDescanso, setHorarioDescanso, setSesionEstudio, setPerfilEstudio, setResumenSemanal } from '../../api'
import { useAuth } from '../../context/AuthContext'

export default function ProfilePage() {
  const { user } = useAuth()
  const [prefs, setPrefs] = useState<any>({})
  const [pomodoroMin, setPomodoroMin] = useState(25)
  const [pomodoroDesc, setPomodoroDesc] = useState(5)
  const [timer, setTimer] = useState(0)
  const [timerActive, setTimerActive] = useState(false)
  const [timerType, setTimerType] = useState<'estudio'|'descanso'>('estudio')
  const [descansoInicio, setDescansoInicio] = useState('22:00')
  const [descansoFin, setDescansoFin] = useState('07:00')
  const [perfil, setPerfil] = useState('VESPERTINO')
  const [resumenActivo, setResumenActivo] = useState(true)
  const [resumenDia, setResumenDia] = useState('SUNDAY')
  const [resumenHora, setResumenHora] = useState('20')
  const [saved, setSaved] = useState('')

  useEffect(() => {
    getPreferencias().then(r => {
      const map: any = {}
      r.data.forEach((p: any) => { map[p.clave] = p.valor })
      setPrefs(map)
      if (map.duracion_sesion) setPomodoroMin(+map.duracion_sesion)
      if (map.descanso_sesion) setPomodoroDesc(+map.descanso_sesion)
      if (map.descanso_inicio) setDescansoInicio(map.descanso_inicio)
      if (map.descanso_fin) setDescansoFin(map.descanso_fin)
      if (map.perfil_estudio) setPerfil(map.perfil_estudio)
      if (map.resumen_semanal_activo !== undefined) setResumenActivo(map.resumen_semanal_activo !== 'false')
      if (map.resumen_semanal_dia) setResumenDia(map.resumen_semanal_dia)
      if (map.resumen_semanal_hora) setResumenHora(map.resumen_semanal_hora)
    }).catch(() => {})
  }, [])

  useEffect(() => {
    if (!timerActive) return
    const secs = (timerType === 'estudio' ? pomodoroMin : pomodoroDesc) * 60
    if (timer <= 0) { setTimer(secs); return }
    const id = setInterval(() => setTimer(t => { if (t <= 1) { clearInterval(id); setTimerActive(false); return 0 } return t - 1 }), 1000)
    return () => clearInterval(id)
  }, [timerActive, timerType])

  const startTimer = (type: 'estudio'|'descanso') => {
    setTimerType(type)
    setTimer((type === 'estudio' ? pomodoroMin : pomodoroDesc) * 60)
    setTimerActive(true)
  }

  const fmt = (s: number) => `${String(Math.floor(s/60)).padStart(2,'0')}:${String(s%60).padStart(2,'0')}`

  const save = async (fn: () => Promise<any>, msg: string) => {
    await fn(); setSaved(msg); setTimeout(() => setSaved(''), 3000)
  }

  return (
    <div>
      <div className="page-header"><h1 className="page-title">👤 Perfil y Configuración</h1></div>

      {saved && <div style={{background:'var(--teal-l)',color:'var(--teal2)',padding:'10px 14px',borderRadius:8,fontSize:12,fontWeight:500,marginBottom:14}}>✓ {saved}</div>}

      <div className="grid-2" style={{marginBottom:16}}>
        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>Datos del usuario</div>
          {[['Nombre',user?.nombre],['Correo',user?.email],['Rol',user?.rol]].map(([k,v]) => (
            <div key={k} style={{marginBottom:12}}>
              <div style={{fontSize:10,color:'#999',textTransform:'uppercase',letterSpacing:.4,marginBottom:3}}>{k}</div>
              <div style={{fontSize:14,fontWeight:500}}>{v}</div>
            </div>
          ))}
        </div>

        <div className="card">
          <div style={{fontWeight:600,marginBottom:4}}>🍅 Timer Pomodoro</div>
          <div style={{fontSize:11,color:'var(--muted)',marginBottom:14}}>Configura tus sesiones de estudio</div>
          <div style={{textAlign:'center',marginBottom:16}}>
            <div style={{fontSize:48,fontWeight:700,color:timerType==='estudio'?'var(--blue)':'var(--teal)',fontVariantNumeric:'tabular-nums'}}>{fmt(timer||pomodoroMin*60)}</div>
            <div style={{fontSize:12,color:'#999',marginTop:4}}>{timerType === 'estudio' ? '🧠 Sesión de estudio' : '☕ Descanso'}</div>
          </div>
          <div style={{display:'flex',gap:7,marginBottom:12}}>
            <button className="btn btn-primary btn-sm" onClick={() => startTimer('estudio')} disabled={timerActive}>▶ Estudiar</button>
            <button className="btn btn-teal btn-sm" onClick={() => startTimer('descanso')} disabled={timerActive}>☕ Descanso</button>
            <button className="btn btn-ghost btn-sm" onClick={() => { setTimerActive(false); setTimer(0) }}>⏹ Parar</button>
          </div>
          <div className="form-row" style={{marginBottom:8}}>
            <div className="form-group"><label className="form-label">Estudio (min)</label>
              <input className="form-input" type="number" value={pomodoroMin} onChange={e => setPomodoroMin(+e.target.value)} /></div>
            <div className="form-group"><label className="form-label">Descanso (min)</label>
              <input className="form-input" type="number" value={pomodoroDesc} onChange={e => setPomodoroDesc(+e.target.value)} /></div>
          </div>
          <button className="btn btn-ghost btn-sm" onClick={() => save(() => setSesionEstudio(pomodoroMin, pomodoroDesc), 'Sesión guardada')}>Guardar configuración</button>
        </div>
      </div>

      <div className="grid-2" style={{marginBottom:16}}>
        <div className="card">
          <div style={{fontWeight:600,marginBottom:4}}>🌙 Horario de descanso</div>
          <div style={{fontSize:11,color:'var(--muted)',marginBottom:14}}>Sin notificaciones en este horario</div>
          <div className="form-row" style={{marginBottom:10}}>
            <div className="form-group"><label className="form-label">Inicio descanso</label>
              <input className="form-input" type="time" value={descansoInicio} onChange={e => setDescansoInicio(e.target.value)} /></div>
            <div className="form-group"><label className="form-label">Fin descanso</label>
              <input className="form-input" type="time" value={descansoFin} onChange={e => setDescansoFin(e.target.value)} /></div>
          </div>
          <button className="btn btn-primary btn-sm" onClick={() => save(() => setHorarioDescanso(descansoInicio, descansoFin), 'Horario de descanso guardado')}>Guardar</button>
        </div>

        <div className="card">
          <div style={{fontWeight:600,marginBottom:4}}>📚 Perfil de estudio</div>
          <div style={{fontSize:11,color:'var(--muted)',marginBottom:14}}>El ScheduleGenerator usará este perfil</div>
          <div className="form-group"><label className="form-label">Tipo de estudiante</label>
            <select className="form-input" value={perfil} onChange={e => setPerfil(e.target.value)}>
              <option value="MATUTINO">🌅 Matutino (6AM - 12PM)</option>
              <option value="VESPERTINO">🌤 Vespertino (12PM - 6PM)</option>
              <option value="NOCTURNO">🌙 Nocturno (6PM - 12AM)</option>
            </select>
          </div>
          <button className="btn btn-primary btn-sm" onClick={() => save(() => setPerfilEstudio(perfil, perfil==='MATUTINO'?'06:00-12:00':perfil==='VESPERTINO'?'12:00-18:00':'18:00-24:00'), 'Perfil de estudio guardado')}>Guardar perfil</button>
        </div>
      </div>

      <div className="card">
        <div style={{fontWeight:600,marginBottom:4}}>🔕 Modo descanso</div>
        <div style={{fontSize:11,color:'var(--muted)',marginBottom:14}}>Pausa todas las notificaciones temporalmente</div>
        <div style={{display:'flex',gap:9}}>
          <button className="btn btn-primary btn-sm" onClick={() => save(() => setModoDescanso(true), 'Modo descanso activado — sin notificaciones')}>🔕 Activar modo descanso</button>
          <button className="btn btn-ghost btn-sm" onClick={() => save(() => setModoDescanso(false), 'Modo descanso desactivado')}>🔔 Desactivar</button>
        </div>
        {prefs.modo_descanso === 'true' && (
          <div style={{marginTop:10,padding:'8px 12px',background:'var(--warn-l)',borderRadius:7,fontSize:12,color:'var(--warn)'}}>⚠️ Modo descanso activo — las notificaciones están pausadas</div>
        )}
      </div>

      <div className="card" style={{marginTop:16}}>
        <div style={{fontWeight:600,marginBottom:4}}>📧 Resumen semanal por correo</div>
        <div style={{fontSize:11,color:'var(--muted)',marginBottom:14}}>Recibe cada semana tus tareas, metas y nivel de estrés promedio</div>
        <div style={{display:'flex',alignItems:'center',gap:9,marginBottom:14}}>
          <input type="checkbox" checked={resumenActivo} onChange={e => setResumenActivo(e.target.checked)} />
          <span style={{fontSize:13}}>Recibir resumen semanal por correo</span>
        </div>
        <div className="form-row" style={{marginBottom:12}}>
          <div className="form-group"><label className="form-label">Día de envío</label>
            <select className="form-input" value={resumenDia} onChange={e => setResumenDia(e.target.value)} disabled={!resumenActivo}>
              <option value="MONDAY">Lunes</option>
              <option value="TUESDAY">Martes</option>
              <option value="WEDNESDAY">Miércoles</option>
              <option value="THURSDAY">Jueves</option>
              <option value="FRIDAY">Viernes</option>
              <option value="SATURDAY">Sábado</option>
              <option value="SUNDAY">Domingo</option>
            </select></div>
          <div className="form-group"><label className="form-label">Hora de envío</label>
            <select className="form-input" value={resumenHora} onChange={e => setResumenHora(e.target.value)} disabled={!resumenActivo}>
              {Array.from({length:24}, (_, h) => (
                <option key={h} value={String(h)}>{String(h).padStart(2,'0')}:00</option>
              ))}
            </select></div>
        </div>
        <button className="btn btn-primary btn-sm" onClick={() => save(() => setResumenSemanal(resumenActivo, resumenDia, resumenHora), 'Configuración de resumen semanal guardada')}>Guardar configuración</button>
      </div>
    </div>
  )
}