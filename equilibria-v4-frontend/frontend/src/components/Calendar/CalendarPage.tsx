import { useEffect, useState } from 'react'
import { getCursos, getTareas, getHorariosClase } from '../../api'

export default function CalendarPage() {
  const [cursos, setCursos] = useState<any[]>([])
  const [tareas, setTareas] = useState<any[]>([])
  const [horarios, setHorarios] = useState<any[]>([])

  useEffect(() => {
    getCursos().then(r => setCursos(r.data)).catch(() => {})
    getTareas().then(r => setTareas(r.data)).catch(() => {})
    getHorariosClase().then(r => setHorarios(r.data)).catch(() => {})
  }, [])

  const DIAS = ['LUNES','MARTES','MIÉRCOLES','JUEVES','VIERNES','SÁBADO']
  const DIAS_SHORT = ['LUN','MAR','MIÉ','JUE','VIE','SÁB']
  const HORAS = [7,8,9,10,11,12,13,14,15,16,17,18,19,20]

  const tareasProximas = [...tareas].filter(t => !t.completada)
    .sort((a,b) => a.fechaEntrega?.localeCompare(b.fechaEntrega)).slice(0,8)

  const getCursoColor = (idCurso: number) => {
    const c = cursos.find(c => c.idCurso === idCurso)
    return c?.color || '#1F4FA8'
  }
  const getCursoNombre = (idCurso: number) => {
    const c = cursos.find(c => c.idCurso === idCurso)
    return c?.nombre || ''
  }

  // Para cada celda (dia, hora) devuelve el horario que cae en ese slot
  const getBloque = (diaIndex: number, hora: number) => {
    const dia = DIAS[diaIndex]
    return horarios.find(h => {
      if (h.dia !== dia) return false
      const ini = parseInt(h.horaInicio?.substring(0,2) || '0')
      const fin = parseInt(h.horaFin?.substring(0,2) || '0')
      return hora >= ini && hora < fin
    })
  }

  // Para saber si es la hora de inicio (para mostrar el label)
  const esPrimeraHora = (diaIndex: number, hora: number) => {
    const dia = DIAS[diaIndex]
    return horarios.some(h => h.dia === dia && parseInt(h.horaInicio?.substring(0,2) || '0') === hora)
  }

  const getAlturaBloque = (h: any) => {
    const ini = parseInt(h.horaInicio?.substring(0,2) || '0')
    const fin = parseInt(h.horaFin?.substring(0,2) || '0')
    return fin - ini // en horas
  }

  return (
    <div>
      <div className="page-header"><h1 className="page-title">📅 Calendario Semanal</h1></div>
      <div className="card" style={{marginBottom:16,overflowX:'auto'}}>
        <div style={{display:'grid',gridTemplateColumns:'50px repeat(6,1fr)',gap:2,fontSize:11,minWidth:600}}>
          <div></div>
          {DIAS_SHORT.map(d => (
            <div key={d} style={{textAlign:'center',fontWeight:700,color:'var(--muted)',padding:'6px 3px',textTransform:'uppercase',fontSize:10}}>{d}</div>
          ))}
          {HORAS.map(h => (
            <>
              <div key={`h-${h}`} style={{color:'#999',textAlign:'right',paddingRight:7,paddingTop:4,fontSize:10}}>{h}:00</div>
              {DIAS.map((d,di) => {
                const bloque = getBloque(di, h)
                const esPrimero = esPrimeraHora(di, h)
                return (
                  <div key={`${h}-${di}`} style={{
                    minHeight:34,
                    borderTop:'1px solid rgba(31,79,168,.06)',
                    padding:2,
                    position:'relative',
                    background: bloque ? `${getCursoColor(bloque.idCurso)}18` : 'transparent'
                  }}>
                    {bloque && esPrimero && (
                      <div style={{
                        position:'absolute',
                        top:2,left:2,right:2,
                        background: getCursoColor(bloque.idCurso),
                        color:'white',
                        borderRadius:4,
                        padding:'2px 4px',
                        fontSize:9,
                        fontWeight:600,
                        lineHeight:1.3,
                        zIndex:1,
                        overflow:'hidden',
                        whiteSpace:'nowrap',
                        textOverflow:'ellipsis'
                      }}>
                        {getCursoNombre(bloque.idCurso)}
                      </div>
                    )}
                  </div>
                )
              })}
            </>
          ))}
        </div>
      </div>

      <div className="grid-2">
        <div className="card">
          <div style={{fontWeight:600,marginBottom:12}}>Próximas entregas</div>
          {tareasProximas.length === 0 && <div style={{fontSize:13,color:'var(--muted)',textAlign:'center',padding:20}}>Sin tareas pendientes 🎉</div>}
          {tareasProximas.map((t: any) => (
            <div key={t.idTarea} style={{display:'flex',justifyContent:'space-between',alignItems:'center',padding:'8px 0',borderBottom:'1px solid rgba(31,79,168,.07)'}}>
              <div style={{flex:1}}>
                <div style={{fontSize:13,fontWeight:500}}>{t.nombre}</div>
                <div style={{fontSize:11,color:'var(--muted)',marginTop:2}}>{t.tipo} · {t.curso?.nombre}</div>
              </div>
              <div style={{fontSize:11,color:'var(--muted)',flexShrink:0,marginLeft:10}}>{t.fechaEntrega}</div>
            </div>
          ))}
        </div>
        <div className="card">
          <div style={{fontWeight:600,marginBottom:12}}>Cursos del ciclo</div>
          {cursos.map((c: any) => (
            <div key={c.idCurso} style={{display:'flex',alignItems:'center',gap:8,padding:'7px 0',borderBottom:'1px solid rgba(31,79,168,.07)'}}>
              <div style={{width:10,height:10,borderRadius:'50%',background:c.color,flexShrink:0}} />
              <div style={{flex:1,fontSize:13,fontWeight:500}}>{c.nombre}</div>
              {c.fechaExamen && <div style={{fontSize:11,color:'#999'}}>Examen: {c.fechaExamen}</div>}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}