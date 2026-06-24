import { useEffect, useState } from 'react'
import { getCuestionarioPSS10, submitPSS10, getHistorialEstres, getTecnicasEstudio } from '../../api'

function renderMd(text: string) {
  if (!text) return ''
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br/>')
}

export default function WellnessPage() {
  const [cuestionario, setCuestionario] = useState<any>(null)
  const [respuestas, setRespuestas] = useState<Record<number,number>>({})
  const [resultado, setResultado] = useState<any>(null)
  const [historial, setHistorial] = useState<any[]>([])
  const [vista, setVista] = useState<'menu'|'cuestionario'|'resultado'|'historial'>('menu')
  const [tecnicas, setTecnicas] = useState('')
  const [loadingTec, setLoadingTec] = useState(false)

  useEffect(() => {
    getCuestionarioPSS10().then(r => setCuestionario(r.data)).catch(() => {})
    getHistorialEstres().then(r => setHistorial(r.data)).catch(() => {})
  }, [])

  const submit = async () => {
    if (Object.keys(respuestas).length < 10) { alert('Responde las 10 preguntas'); return }
    const map: Record<string,number> = {}
    Object.entries(respuestas).forEach(([k,v]) => { map[k] = v })
    const r = await submitPSS10(map)
    setResultado(r.data)
    setVista('resultado')
    getHistorialEstres().then(r => setHistorial(r.data)).catch(() => {})
  }

  const handleTecnicas = async () => {
    setLoadingTec(true)
    try { const r = await getTecnicasEstudio(); setTecnicas(r.data.tecnicas) }
    finally { setLoadingTec(false) }
  }

  const nivelColor = (n: string) => n === 'ALTO' ? 'var(--danger)' : n === 'MODERADO' ? 'var(--warn)' : 'var(--teal)'

  return (
    <div>
      <div className="page-header"><h1 className="page-title">💚 Bienestar Emocional</h1></div>

      <div style={{display:'flex',gap:9,marginBottom:20,flexWrap:'wrap'}}>
        <button className="btn btn-primary" onClick={() => { setRespuestas({}); setVista('cuestionario') }}>📋 Nueva evaluación PSS-10</button>
        <button className="btn btn-ghost" onClick={() => setVista('historial')}>📈 Ver historial</button>
        <button className="btn btn-teal" onClick={handleTecnicas} disabled={loadingTec}>
          {loadingTec ? '⏳ Consultando Gemini...' : '🎓 Técnicas de estudio'}
        </button>
      </div>

      {tecnicas && (
        <div className="card" style={{marginBottom:20,borderLeft:'4px solid var(--teal)'}}>
          <div style={{fontWeight:600,marginBottom:8,color:'var(--teal)'}}>🎓 Técnicas de estudio recomendadas por Gemini</div>
          <div style={{fontSize:13,lineHeight:1.75}} dangerouslySetInnerHTML={{__html: renderMd(tecnicas)}} />
        </div>
      )}

      {vista === 'menu' && historial.length > 0 && (
        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>Última evaluación</div>
          <div style={{textAlign:'center',padding:20}}>
            <div style={{fontSize:48,fontWeight:700,color:nivelColor(historial[0].nivel)}}>{historial[0].puntuacion}<span style={{fontSize:18,color:'#999'}}>/40</span></div>
            <div style={{display:'inline-block',padding:'5px 18px',borderRadius:999,fontWeight:700,background:nivelColor(historial[0].nivel)+'20',color:nivelColor(historial[0].nivel),marginTop:8}}>Estrés {historial[0].nivel}</div>
            <div style={{marginTop:12,fontSize:13,lineHeight:1.7,textAlign:'left',background:'var(--blue-l)',borderRadius:10,padding:14}}
              dangerouslySetInnerHTML={{__html: renderMd(historial[0].consejosIa)}} />
          </div>
        </div>
      )}

      {vista === 'cuestionario' && cuestionario && (
        <div className="card">
          <div style={{fontWeight:600,marginBottom:4}}>PSS-10 — Cohen, Kamarck & Mermelstein (1983)</div>
          <div style={{fontSize:12,color:'var(--muted)',marginBottom:16}}>Responde sobre el <strong>último mes</strong>. Ítems invertidos se calculan automáticamente.</div>
          {cuestionario.preguntas.map((q: any) => (
            <div key={q.numero} style={{background:'#f8f9fd',borderRadius:9,padding:14,marginBottom:10}}>
              <div style={{fontSize:13,fontWeight:500,marginBottom:10}}>{q.numero}. {q.pregunta}{q.invertida && <span style={{fontSize:10,color:'#999',marginLeft:5}}>(invertida)</span>}</div>
              <div style={{display:'flex',gap:5,flexWrap:'wrap'}}>
                {cuestionario.opciones.map((o: string, j: number) => (
                  <button key={j} className={`pss-opt${respuestas[q.numero] === j ? ' selected' : ''}`}
                    onClick={() => setRespuestas(prev => ({...prev, [q.numero]: j}))}>{o}</button>
                ))}
              </div>
            </div>
          ))}
          <div style={{fontSize:12,color:'#999',marginBottom:12}}>Respondidas: {Object.keys(respuestas).length}/10</div>
          <button className="btn btn-primary btn-full" onClick={submit}>✦ Calcular + consultar WellnessAdvisor</button>
        </div>
      )}

      {vista === 'resultado' && resultado && (
        <div className="grid-2">
          <div className="card">
            <div style={{textAlign:'center',padding:20}}>
              <div style={{fontSize:52,fontWeight:700,color:nivelColor(resultado.nivel)}}>{resultado.score}<span style={{fontSize:18,color:'#999'}}>/40</span></div>
              <div style={{display:'inline-block',padding:'5px 18px',borderRadius:999,fontWeight:700,background:nivelColor(resultado.nivel)+'20',color:nivelColor(resultado.nivel),marginTop:8}}>Estrés {resultado.nivel}</div>
              <div style={{height:7,background:'#f4f6fb',borderRadius:4,overflow:'hidden',margin:'12px 0'}}>
                <div style={{width:`${resultado.score/40*100}%`,height:'100%',background:nivelColor(resultado.nivel)}} />
              </div>
              <div style={{fontSize:11,color:'#999'}}>BAJO: 0-13 · MODERADO: 14-26 · ALTO: 27-40</div>
            </div>
            <div style={{background:'var(--blue-l)',borderRadius:10,padding:14,fontSize:13,lineHeight:1.75}}>
              <strong style={{color:'var(--blue)'}}>🧠 WellnessAdvisor — Gemini 2.0 Flash</strong>
              <div style={{marginTop:8}} dangerouslySetInnerHTML={{__html: renderMd(resultado.consejos)}} />
            </div>
          </div>
          <div className="card">
            <div style={{fontWeight:600,marginBottom:14}}>Historial reciente</div>
            {historial.slice(0,6).map((h: any, i: number) => (
              <div key={i} style={{display:'flex',alignItems:'center',gap:10,padding:'8px 0',borderBottom:'1px solid rgba(31,79,168,.07)'}}>
                <div style={{width:55,height:7,background:'#f4f6fb',borderRadius:3,overflow:'hidden'}}>
                  <div style={{width:`${h.puntuacion/40*100}%`,height:'100%',background:nivelColor(h.nivel)}} />
                </div>
                <div style={{flex:1,fontSize:12,fontWeight:600,color:nivelColor(h.nivel)}}>{h.nivel} {h.puntuacion}/40</div>
                <div style={{fontSize:10,color:'#999'}}>{new Date(h.fecha).toLocaleDateString('es-PE')}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {vista === 'historial' && (
        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>Todas las evaluaciones PSS-10</div>
          {historial.length === 0 && <div className="empty-state"><div className="empty-state-icon">📊</div><div>Sin evaluaciones. Completa el PSS-10.</div></div>}
          {historial.map((h: any, i: number) => (
            <div key={i} style={{padding:'12px 0',borderBottom:'1px solid rgba(31,79,168,.07)'}}>
              <div style={{display:'flex',alignItems:'center',gap:10,marginBottom:7}}>
                <div style={{fontSize:22,fontWeight:700,color:nivelColor(h.nivel)}}>{h.puntuacion}<span style={{fontSize:13,color:'#999'}}>/40</span></div>
                <div style={{padding:'4px 14px',borderRadius:999,fontWeight:700,background:nivelColor(h.nivel)+'20',color:nivelColor(h.nivel),fontSize:12}}>Estrés {h.nivel}</div>
                <div style={{marginLeft:'auto',fontSize:11,color:'#999'}}>{new Date(h.fecha).toLocaleDateString('es-PE',{day:'2-digit',month:'long',year:'numeric'})}</div>
              </div>
              <div style={{fontSize:12,color:'var(--muted)',lineHeight:1.65}}
                dangerouslySetInnerHTML={{__html: renderMd(h.consejosIa)}} />
            </div>
          ))}
        </div>
      )}
    </div>
  )
}