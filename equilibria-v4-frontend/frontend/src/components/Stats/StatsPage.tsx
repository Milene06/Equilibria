import { useEffect, useState } from 'react'
import { getDashboard, getProgresoHistorico, exportarPDF, compartirProgreso, getMetricasAdmin } from '../../api'
import { useAuth } from '../../context/AuthContext'

export default function StatsPage() {
  const { isAdmin } = useAuth()
  const [stats, setStats] = useState<any>(null)
  const [progreso, setProgreso] = useState<any>(null)
  const [shareUrl, setShareUrl] = useState('')
  const [adminMetrics, setAdminMetrics] = useState<any>(null)

  useEffect(() => {
    getDashboard().then(r => setStats(r.data)).catch(() => {})
    getProgresoHistorico().then(r => setProgreso(r.data)).catch(() => {})
    if (isAdmin) getMetricasAdmin().then(r => setAdminMetrics(r.data)).catch(() => {})
  }, [isAdmin])

  const handleExportPDF = async () => {
    const r = await exportarPDF()
    const url = URL.createObjectURL(new Blob([r.data], { type: 'application/pdf' }))
    const a = document.createElement('a'); a.href = url; a.download = 'plan-equilibria.pdf'; a.click()
  }

  const handleCompartir = async () => {
    const r = await compartirProgreso()
    setShareUrl(window.location.origin + r.data.url)
    navigator.clipboard.writeText(window.location.origin + r.data.url).catch(() => {})
  }

  const semanas = progreso ? Object.entries(progreso.progresoSemanal || {}).slice(-8) : []
  const maxVal = semanas.length ? Math.max(...semanas.map(([,v]) => Number(v)), 1) : 1

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📊 Estadísticas</h1>
        <div style={{display:'flex',gap:8}}>
          <button className="btn btn-ghost btn-sm" onClick={handleExportPDF}>📄 Exportar PDF</button>
          <button className="btn btn-teal btn-sm" onClick={handleCompartir}>🔗 Compartir con tutor</button>
        </div>
      </div>

      {shareUrl && (
        <div style={{background:'var(--teal-l)',border:'1.5px solid var(--teal)',borderRadius:9,padding:'12px 16px',marginBottom:16,fontSize:12}}>
          <strong style={{color:'var(--teal2)'}}>✓ Enlace copiado (válido 24h):</strong>
          <div style={{color:'var(--teal2)',marginTop:4,wordBreak:'break-all'}}>{shareUrl}</div>
        </div>
      )}

      <div className="grid-4" style={{marginBottom:20}}>
        <div className="kpi"><div className="kpi-label">Total tareas</div><div className="kpi-value" style={{color:'var(--blue)'}}>{stats?.totalTareas ?? 0}</div></div>
        <div className="kpi"><div className="kpi-label">Completadas</div><div className="kpi-value" style={{color:'var(--teal)'}}>{stats?.completadas ?? 0}</div></div>
        <div className="kpi"><div className="kpi-label">Tasa finalización</div><div className="kpi-value" style={{color:'var(--blue)'}}>{stats?.tasaFinalizacion ?? 0}%</div></div>
        <div className="kpi"><div className="kpi-label">Cursos activos</div><div className="kpi-value" style={{color:'var(--warn)'}}>{stats?.totalCursos ?? 0}</div></div>
      </div>

      <div className="grid-2" style={{marginBottom:16}}>
        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>Progreso histórico semanal</div>
          {semanas.length === 0 && <div className="empty-state" style={{padding:20}}><div>Sin datos de progreso aún</div></div>}
          <div style={{display:'flex',alignItems:'flex-end',gap:8,height:120,padding:'8px 0'}}>
            {semanas.map(([sem, val]) => (
              <div key={sem} style={{flex:1,display:'flex',flexDirection:'column',alignItems:'center',gap:3}}>
                <div style={{fontSize:10,fontWeight:700,color:'var(--blue)'}}>{String(val)}</div>
                <div style={{width:'100%',background:'var(--blue)',borderRadius:'3px 3px 0 0',height:`${Number(val)/maxVal*100}px`,minHeight:4}} />
                <div style={{fontSize:9,color:'var(--muted)',textAlign:'center'}}>{sem.split('-W')[1] ? `S${sem.split('-W')[1]}` : sem}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>Últimas evaluaciones PSS-10</div>
          {!stats?.ultimoEstres && <div style={{fontSize:13,color:'var(--muted)',textAlign:'center',padding:20}}>Sin evaluaciones</div>}
          {stats?.ultimoEstres && (
            <div style={{textAlign:'center'}}>
              <div style={{fontSize:36,fontWeight:700,color:stats.ultimoEstres.nivel==='ALTO'?'var(--danger)':stats.ultimoEstres.nivel==='MODERADO'?'var(--warn)':'var(--teal)'}}>
                {stats.ultimoEstres.score}/40
              </div>
              <div style={{marginTop:8,padding:'5px 16px',borderRadius:999,fontWeight:700,display:'inline-block',
                background:(stats.ultimoEstres.nivel==='ALTO'?'var(--danger-l)':stats.ultimoEstres.nivel==='MODERADO'?'var(--warn-l)':'var(--teal-l)'),
                color:(stats.ultimoEstres.nivel==='ALTO'?'var(--danger)':stats.ultimoEstres.nivel==='MODERADO'?'var(--warn)':'var(--teal)')}}>
                Estrés {stats.ultimoEstres.nivel}
              </div>
            </div>
          )}
        </div>
      </div>

      {isAdmin && adminMetrics && (
        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>🛡️ Métricas de plataforma — Solo Admin</div>
          <div className="grid-4">
            <div className="kpi"><div className="kpi-label">Usuarios</div><div className="kpi-value" style={{color:'var(--blue)'}}>{adminMetrics.totalUsuarios}</div></div>
            <div className="kpi"><div className="kpi-label">Tareas totales</div><div className="kpi-value" style={{color:'var(--teal)'}}>{adminMetrics.totalTareas}</div></div>
            <div className="kpi"><div className="kpi-label">Evaluaciones PSS-10</div><div className="kpi-value" style={{color:'var(--warn)'}}>{adminMetrics.totalPss10}</div></div>
            <div className="kpi"><div className="kpi-label">Metas creadas</div><div className="kpi-value" style={{color:'#7c3aed'}}>{adminMetrics.totalMetas}</div></div>
          </div>
        </div>
      )}
    </div>
  )
}