import { useState, useEffect } from 'react'
import { priorizarTareas, generarHorario, getFraseMotivacional, reagendarTareas, sendFeedback, getHorariosClase, createHorarioClase, updateHorarioClase, deleteHorarioClase } from '../../api'

const COLORES: Record<string,string> = {
  ESTUDIO:'#1F4FA820', DESCANSO:'#2DA39B20', CLASE:'#7c3aed20', TIEMPO_LIBRE:'#f5900020'
}
const TEXTO: Record<string,string> = {
  ESTUDIO:'#1F4FA8', DESCANSO:'#2DA39B', CLASE:'#7c3aed', TIEMPO_LIBRE:'#f59000'
}
const DIAS_ORDEN = ['Lunes','Martes','Miércoles','Jueves','Viernes','Sábado']
const DIAS_BACKEND = ['LUNES','MARTES','MIÉRCOLES','JUEVES','VIERNES','SÁBADO']

function normalizarDiaDisplay(dia: string) {
  const idx = DIAS_BACKEND.indexOf((dia || '').toUpperCase())
  return idx >= 0 ? DIAS_ORDEN[idx] : dia
}

function HorarioEditable({ bloquesIniciales, onChange }: { bloquesIniciales: any[], onChange?: () => void }) {
  const [bloques, setBloques] = useState<any[]>(() =>
    bloquesIniciales.map(b => ({ ...b, _key: b.idHorario ?? `tmp-${Math.random()}` }))
  )
  const [editandoKey, setEditandoKey] = useState<string|null>(null)
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    setBloques(bloquesIniciales.map(b => ({ ...b, _key: b.idHorario ?? `tmp-${Math.random()}` })))
  }, [bloquesIniciales])

  if (!bloques || bloques.length === 0) return (
    <div style={{fontSize:13,color:'var(--muted)',textAlign:'center',padding:20}}>Sin bloques aún. Agrega uno con el botón + en cada día.</div>
  )

  const diasActivos = DIAS_ORDEN.filter(d => bloques.some((b:any) => normalizarDiaDisplay(b.dia) === d))
  const porDia = (dia: string) => bloques.filter((b:any) => normalizarDiaDisplay(b.dia) === dia)
  const maxRows = Math.max(...diasActivos.map(d => porDia(d).length), 1)

  const persistirBloque = async (b: any): Promise<number|null> => {
    setGuardando(true)
    try {
      const payload = {
        dia: DIAS_BACKEND[DIAS_ORDEN.indexOf(normalizarDiaDisplay(b.dia))] || 'LUNES',
        horaInicio: b.horaInicio,
        horaFin: b.horaFin,
        actividad: b.actividad,
        tipo: b.tipo,
        iaGenerado: true
      }
      if (b.idHorario) {
        await updateHorarioClase(b.idHorario, payload)
        return b.idHorario
      } else {
        const res = await createHorarioClase(payload)
        return res.data.idHorario
      }
    } catch (e) {
      console.error('Error guardando bloque', e)
      return null
    } finally {
      setGuardando(false)
    }
  }

  const updateBloque = (key: string, field: string, val: string) => {
    setBloques(prev => prev.map(b => b._key === key ? { ...b, [field]: val } : b))
  }

  const guardarBloque = async (key: string) => {
    const b = bloques.find(x => x._key === key)
    if (!b) return
    const idHorario = await persistirBloque(b)
    if (idHorario) {
      setBloques(prev => prev.map(x => x._key === key ? { ...x, idHorario } : x))
    }
    setEditandoKey(null)
    onChange?.()
  }

  const eliminarBloque = async (key: string) => {
    const b = bloques.find(x => x._key === key)
    if (b?.idHorario) {
      try { await deleteHorarioClase(b.idHorario) } catch (e) { console.error(e) }
    }
    setBloques(prev => prev.filter(x => x._key !== key))
    setEditandoKey(null)
    onChange?.()
  }

  const agregarBloque = async (dia: string) => {
    const key = `tmp-${Math.random()}`
    const nuevo = { dia, horaInicio:'08:00', horaFin:'09:00', actividad:'Nueva actividad', tipo:'ESTUDIO', _key: key }
    setBloques(prev => [...prev, nuevo])
    const idHorario = await persistirBloque(nuevo)
    if (idHorario) {
      setBloques(prev => prev.map(x => x._key === key ? { ...x, idHorario } : x))
    }
    onChange?.()
  }

  return (
    <div style={{overflowX:'auto'}}>
      <div style={{display:'flex',gap:8,marginBottom:10,flexWrap:'wrap',alignItems:'center'}}>
        {['ESTUDIO','CLASE','DESCANSO','TIEMPO_LIBRE'].map(t => (
          <span key={t} style={{fontSize:10,padding:'2px 8px',borderRadius:999,background:COLORES[t],color:TEXTO[t],fontWeight:600}}>{t}</span>
        ))}
        <span style={{fontSize:10,color:'var(--muted)'}}>· Haz clic en una celda para editar{guardando ? ' · Guardando...' : ''}</span>
      </div>
      <table style={{width:'100%',borderCollapse:'collapse',fontSize:12}}>
        <thead>
          <tr>
            {diasActivos.map(d => (
              <th key={d} style={{padding:'8px 10px',background:'var(--blue-l)',color:'var(--blue)',fontWeight:700,textAlign:'center',border:'1px solid #e5e7eb'}}>
                {d}
                <button onClick={() => agregarBloque(d)}
                  style={{marginLeft:6,background:'var(--blue)',color:'#fff',border:'none',borderRadius:4,padding:'1px 6px',cursor:'pointer',fontSize:10}}>+</button>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {Array.from({length: maxRows}, (_,i) => (
            <tr key={i}>
              {diasActivos.map(d => {
                const diaItems = porDia(d)
                const b = diaItems[i]
                const isEditing = b && editandoKey === b._key
                return (
                  <td key={d} style={{padding:4,border:'1px solid #e5e7eb',verticalAlign:'top',background: b ? COLORES[b.tipo]||'#f8f9fd' : '#fff',minWidth:120}}>
                    {b && !isEditing && (
                      <div style={{cursor:'pointer',padding:2}} onClick={() => setEditandoKey(b._key)}>
                        <div style={{fontSize:10,fontWeight:700,color:TEXTO[b.tipo]||'#333'}}>{b.horaInicio?.substring(0,5)} – {b.horaFin?.substring(0,5)}</div>
                        <div style={{fontSize:11,marginTop:2,lineHeight:1.4}}>{b.actividad}</div>
                        <div style={{fontSize:9,color:'#999',marginTop:2}}>{b.tipo}</div>
                      </div>
                    )}
                    {b && isEditing && (
                      <div style={{display:'flex',flexDirection:'column',gap:3}}>
                        <input style={{fontSize:10,padding:'2px 4px',border:'1px solid #ddd',borderRadius:4,width:'100%',boxSizing:'border-box'}}
                          type="time"
                          value={b.horaInicio?.substring(0,5)} onChange={e => updateBloque(b._key,'horaInicio',e.target.value)} />
                        <input style={{fontSize:10,padding:'2px 4px',border:'1px solid #ddd',borderRadius:4,width:'100%',boxSizing:'border-box'}}
                          type="time"
                          value={b.horaFin?.substring(0,5)} onChange={e => updateBloque(b._key,'horaFin',e.target.value)} />
                        <input style={{fontSize:10,padding:'2px 4px',border:'1px solid #ddd',borderRadius:4,width:'100%',boxSizing:'border-box'}}
                          value={b.actividad} onChange={e => updateBloque(b._key,'actividad',e.target.value)} />
                        <select style={{fontSize:10,padding:'2px 4px',border:'1px solid #ddd',borderRadius:4}}
                          value={b.tipo} onChange={e => updateBloque(b._key,'tipo',e.target.value)}>
                          <option value="ESTUDIO">ESTUDIO</option>
                          <option value="CLASE">CLASE</option>
                          <option value="DESCANSO">DESCANSO</option>
                          <option value="TIEMPO_LIBRE">TIEMPO_LIBRE</option>
                        </select>
                        <div style={{display:'flex',gap:3,marginTop:2}}>
                          <button style={{flex:1,fontSize:9,padding:'3px',background:'var(--blue)',color:'#fff',border:'none',borderRadius:3,cursor:'pointer'}}
                            onClick={() => guardarBloque(b._key)}>✓ Guardar</button>
                          <button style={{fontSize:9,padding:'3px 6px',background:'#fee2e2',color:'#dc2626',border:'none',borderRadius:3,cursor:'pointer'}}
                            onClick={() => eliminarBloque(b._key)}>✕</button>
                        </div>
                      </div>
                    )}
                    {!b && <div style={{minHeight:34}} />}
                  </td>
                )
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default function AICoachPage() {
  const [result, setResult] = useState<any>(null)
  const [type, setType] = useState('')
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [bloquesHorario, setBloquesHorario] = useState<any[]>([])

  const cargarHorarioGuardado = async () => {
    try {
      const r = await getHorariosClase()
      const iaBloques = r.data.filter((h: any) => h.iaGenerado)
      if (iaBloques.length > 0) {
        setBloquesHorario(iaBloques)
        setType('horario')
        setResult({ horario: 'persisted' })
      }
    } catch {}
  }

  useEffect(() => { cargarHorarioGuardado() }, [])

  const run = async (fn: () => Promise<any>, t: string) => {
    setLoading(true); setType(t); setMsg(''); setResult(null)
    try {
      const r = await fn()
      setResult(r.data)
      if (t === 'horario') {
        try {
          const parsed = JSON.parse(r.data.horario)
          setBloquesHorario(parsed)
        } catch { setBloquesHorario([]) }
        setTimeout(() => cargarHorarioGuardado(), 500)
      }
    } catch { setMsg('Error al consultar la IA. Verifica la API key.') }
    finally { setLoading(false) }
  }

  const feedback = async (util: boolean) => {
    await sendFeedback({ util, tipo: type })
    setMsg(util ? '¡Gracias por el feedback positivo!' : 'Feedback registrado. Mejoraremos las recomendaciones.')
  }

  return (
    <div>
      <div className="page-header"><h1 className="page-title">🧠 Coach IA</h1></div>

      {msg && <div style={{background:'var(--blue-l)',color:'var(--blue)',padding:'10px 14px',borderRadius:8,fontSize:12,fontWeight:500,marginBottom:14}}>{msg}</div>}

      <div className="grid-3" style={{marginBottom:22}}>
        {[
          { t:'priorizar', icon:'🎯', title:'PriorityAlgorithm', desc:'Analiza tus tareas y asigna un score de urgencia (1-99) según deadline, dificultad y tipo.', label:'Priorizar mis tareas', color:'#1F4FA8', fn: () => priorizarTareas() },
          { t:'horario', icon:'📅', title:'ScheduleGenerator', desc:'Genera un horario semanal equilibrado con bloques de estudio, descanso y tiempo libre.', label:'Generar mi horario', color:'#2DA39B', fn: () => generarHorario() },
          { t:'frase', icon:'💬', title:'WellnessAdvisor', desc:'Genera una frase motivacional personalizada según tu nivel de estrés PSS-10 actual.', label:'Frase motivacional', color:'#7c3aed', fn: () => getFraseMotivacional() },
          { t:'reagendar', icon:'🔄', title:'Reagendar por estrés', desc:'Si tu PSS-10 ≥ 27, la IA sugiere qué tareas reagendar para reducir tu carga.', label:'Sugerir reagendado', color:'#A32D2D', fn: () => reagendarTareas() },
        ].map(c => (
          <div key={c.t} style={{background:`linear-gradient(135deg,${c.color}dd,${c.color})`,borderRadius:12,padding:18,color:'#fff'}}>
            <div style={{fontSize:28,marginBottom:9}}>{c.icon}</div>
            <div style={{fontSize:15,fontWeight:700,marginBottom:5}}>{c.title}</div>
            <div style={{fontSize:11,opacity:.85,lineHeight:1.5,marginBottom:12}}>{c.desc}</div>
            <button style={{width:'100%',padding:9,background:'rgba(255,255,255,.18)',color:'#fff',border:'1.5px solid rgba(255,255,255,.35)',borderRadius:7,fontSize:12,fontWeight:600,cursor:'pointer',fontFamily:'inherit'}}
              onClick={() => run(c.fn, c.t)} disabled={loading}>
              {loading && type === c.t ? '⏳ Consultando IA...' : `✦ ${c.label}`}
            </button>
          </div>
        ))}
      </div>

      {result && type === 'priorizar' && (
        <div className="card" style={{marginBottom:16}}>
          <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:14}}>
            <span style={{fontWeight:600}}>Tareas priorizadas por IA</span>
            <div style={{display:'flex',gap:6}}>
              <button className="btn btn-ghost btn-sm" onClick={() => feedback(true)}>👍 Útil</button>
              <button className="btn btn-ghost btn-sm" onClick={() => feedback(false)}>👎</button>
            </div>
          </div>
          {Array.isArray(result) && result.map((t: any) => (
            <div key={t.idTarea} style={{display:'flex',alignItems:'center',gap:10,padding:'9px 0',borderBottom:'1px solid rgba(31,79,168,.07)'}}>
              <div style={{width:38,height:38,borderRadius:7,background:'var(--blue-l)',display:'flex',alignItems:'center',justifyContent:'center',fontWeight:700,color:'var(--blue)',fontSize:13,flexShrink:0}}>{t.iaScore}</div>
              <div style={{flex:1}}>
                <div style={{fontSize:13,fontWeight:500}}>{t.nombre}</div>
                {t.iaRazon && <div style={{fontSize:11,color:'var(--blue)',fontStyle:'italic'}}>💡 {t.iaRazon}</div>}
              </div>
            </div>
          ))}
        </div>
      )}

      {result && type === 'horario' && (
        <div className="card" style={{marginBottom:16}}>
          <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:14}}>
            <span style={{fontWeight:600}}>Horario semanal — editable y guardado automáticamente</span>
            <div style={{display:'flex',gap:6}}>
              <button className="btn btn-ghost btn-sm" onClick={() => feedback(true)}>👍 Útil</button>
              <button className="btn btn-ghost btn-sm" onClick={() => feedback(false)}>👎</button>
            </div>
          </div>
          <HorarioEditable bloquesIniciales={bloquesHorario} onChange={cargarHorarioGuardado} />
        </div>
      )}

      {result && type === 'frase' && (
        <div style={{background:'linear-gradient(135deg,var(--teal2),var(--teal))',borderRadius:12,padding:'16px 20px',color:'#fff',marginBottom:16}}>
          <div style={{fontSize:11,opacity:.8,marginBottom:5}}>WellnessAdvisor</div>
          <div style={{fontSize:14,fontStyle:'italic',lineHeight:1.65}}>{result.frase}</div>
          <div style={{display:'flex',gap:6,marginTop:10}}>
            <button onClick={() => feedback(true)} style={{background:'rgba(255,255,255,.2)',border:'none',cursor:'pointer',padding:'4px 10px',borderRadius:6,color:'#fff',fontSize:12}}>👍</button>
            <button onClick={() => feedback(false)} style={{background:'rgba(255,255,255,.2)',border:'none',cursor:'pointer',padding:'4px 10px',borderRadius:6,color:'#fff',fontSize:12}}>👎</button>
          </div>
        </div>
      )}

      {result && type === 'reagendar' && (
        <div className="card">
          <div style={{fontWeight:600,marginBottom:10,color:'var(--danger)'}}>🔄 Sugerencias de reagendado</div>
          {result.mensaje && <div style={{fontSize:13,color:'var(--teal)'}}>{result.mensaje}</div>}
          {result.sugerencias && <pre style={{fontSize:12,lineHeight:1.7,whiteSpace:'pre-wrap',background:'#f8f9fd',padding:14,borderRadius:8}}>{result.sugerencias}</pre>}
        </div>
      )}
    </div>
  )
}
