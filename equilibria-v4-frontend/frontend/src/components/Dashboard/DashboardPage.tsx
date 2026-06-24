import { useEffect, useState } from 'react'
import { getDashboard, getResumenDiario, getFraseMotivacional } from '../../api'
import { useAuth } from '../../context/AuthContext'

export default function DashboardPage() {
  const { user } = useAuth()
  const [stats, setStats] = useState<any>(null)
  const [resumen, setResumen] = useState<any>(null)
  const [frase, setFrase] = useState<string>('')

  useEffect(() => {
    getDashboard().then(r => setStats(r.data)).catch(() => {})
    getResumenDiario().then(r => setResumen(r.data)).catch(() => {})
    getFraseMotivacional().then(r => setFrase(r.data.frase)).catch(() => {})
  }, [])

  const nivelColor = (nivel: string) => nivel === 'ALTO' ? 'var(--danger)' : nivel === 'MODERADO' ? 'var(--warn)' : 'var(--teal)'

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">👋 Hola, {user?.nombre?.split(' ')[0]}</h1>
      </div>

      {frase && (
        <div style={{background:'linear-gradient(135deg,var(--teal2),var(--teal))',borderRadius:12,padding:'16px 20px',color:'#fff',marginBottom:20}}>
          <div style={{fontSize:11,opacity:.8,marginBottom:5}}>✨ WellnessAdvisor — Gemini 2.0 Flash</div>
          <div style={{fontSize:13,lineHeight:1.65,fontStyle:'italic'}}>{frase}</div>
        </div>
      )}

      <div className="grid-4" style={{marginBottom:20}}>
        <div className="kpi"><div className="kpi-label">Cursos activos</div><div className="kpi-value" style={{color:'var(--blue)'}}>{stats?.totalCursos ?? '—'}</div></div>
        <div className="kpi"><div className="kpi-label">Tareas pendientes</div><div className="kpi-value" style={{color:'var(--warn)'}}>{stats?.pendientes ?? '—'}</div></div>
        <div className="kpi"><div className="kpi-label">Completadas</div><div className="kpi-value" style={{color:'var(--teal)'}}>{stats?.completadas ?? '—'}</div></div>
        <div className="kpi"><div className="kpi-label">Tasa finalización</div><div className="kpi-value" style={{color:'var(--blue)'}}>{stats?.tasaFinalizacion ?? 0}%</div></div>
      </div>

      {stats?.ultimoEstres && (
        <div style={{background:'var(--blue-l)',borderRadius:10,padding:'12px 16px',marginBottom:20,display:'flex',alignItems:'center',gap:12}}>
          <div style={{fontSize:24,fontWeight:700,color:nivelColor(stats.ultimoEstres.nivel)}}>{stats.ultimoEstres.score}/40</div>
          <div>
            <div style={{fontSize:12,fontWeight:600,color:nivelColor(stats.ultimoEstres.nivel)}}>Estrés {stats.ultimoEstres.nivel} (último PSS-10)</div>
            {stats.ultimoEstres.nivel === 'ALTO' && <div style={{fontSize:11,color:'var(--danger)'}}>⚠️ Revisa los consejos en la sección Bienestar</div>}
          </div>
        </div>
      )}

      <div className="grid-2">
        <div className="card">
          <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:14}}>
            <span style={{fontWeight:600}}>Vencen hoy ({resumen?.totalHoy ?? 0})</span>
          </div>
          {resumen?.vencenHoy?.length === 0 && <div className="empty-state" style={{padding:20}}><div>✅ Sin tareas para hoy</div></div>}
          {resumen?.vencenHoy?.map((t: any) => (
            <div key={t.idTarea} style={{padding:'10px 0',borderBottom:'1px solid rgba(31,79,168,.07)'}}>
              <div style={{fontSize:13,fontWeight:500}}>{t.nombre}</div>
              <div style={{fontSize:11,color:'var(--muted)',marginTop:3}}>
                <span className={`badge badge-${t.prioridad === 'alta' ? 'danger' : t.prioridad === 'media' ? 'warn' : 'teal'}`}>{t.prioridad}</span>
                {' '}{t.tipo}
              </div>
            </div>
          ))}
        </div>

        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>Próximos 7 días ({resumen?.totalSemana ?? 0} tareas)</div>
          {resumen?.vencenSemana?.slice(0,5).map((t: any) => (
            <div key={t.idTarea} style={{padding:'8px 0',borderBottom:'1px solid rgba(31,79,168,.07)',display:'flex',justifyContent:'space-between',alignItems:'center'}}>
              <div style={{fontSize:13}}>{t.nombre}</div>
              <div style={{fontSize:11,color:'var(--muted)'}}>{t.fechaEntrega}</div>
            </div>
          ))}
          {!resumen?.vencenSemana?.length && <div style={{fontSize:13,color:'var(--muted)',textAlign:'center',padding:20}}>Sin tareas esta semana 🎉</div>}
        </div>
      </div>
    </div>
  )
}
