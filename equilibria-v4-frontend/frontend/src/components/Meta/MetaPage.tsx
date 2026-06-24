import { useEffect, useState } from 'react'
import { getMetas, createMeta, updateProgresoMeta, deleteMeta } from '../../api'

export default function MetaPage() {
  const [metas, setMetas] = useState<any[]>([])
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ descripcion:'', horasObjetivo:10, fechaInicio:'', fechaFin:'' })

  const load = () => getMetas().then(r => setMetas(r.data))
  useEffect(() => { load() }, [])

  const save = async () => { await createMeta(form); setShowModal(false); load() }

  return (
    <div>
      <div className="page-header"><h1 className="page-title">🎯 Metas de Estudio</h1><button className="btn btn-primary" onClick={() => setShowModal(true)}>+ Nueva meta</button></div>
      {metas.length === 0 && <div className="empty-state"><div className="empty-state-icon">🎯</div><div>Agrega tu primera meta de estudio</div></div>}
      {metas.map(m => (
        <div key={m.idMeta} className="card" style={{marginBottom:12}}>
          <div style={{display:'flex',justifyContent:'space-between',alignItems:'flex-start',gap:12}}>
            <div style={{flex:1}}>
              <div style={{fontSize:14,fontWeight:600,marginBottom:4,textDecoration:m.completada?'line-through':'none',color:m.completada?'#999':'inherit'}}>{m.descripcion}</div>
              <div style={{display:'flex',gap:6,flexWrap:'wrap',marginBottom:8,fontSize:11,color:'var(--muted)'}}>
                <span>🎯 {m.horasObjetivo}h objetivo</span>
                {m.fechaFin && <span>📅 Hasta {m.fechaFin}</span>}
                {m.completada && <span className="badge badge-teal">✓ Completada</span>}
              </div>
              <div style={{display:'flex',alignItems:'center',gap:9}}>
                <div style={{flex:1,height:8,background:'#f4f6fb',borderRadius:4,overflow:'hidden'}}>
                  <div style={{width:`${m.progreso}%`,height:'100%',background:m.progreso>=100?'var(--teal)':m.progreso>=50?'var(--blue)':'var(--warn)',transition:'width .4s'}} />
                </div>
                <span style={{fontSize:11,fontWeight:600,color:'var(--blue)',minWidth:36}}>{m.progreso}%</span>
              </div>
            </div>
            <div style={{display:'flex',gap:5,flexShrink:0}}>
              <button className="btn btn-ghost btn-sm" onClick={() => updateProgresoMeta(m.idMeta, Math.max(0, m.progreso-10)).then(load)}>−</button>
              <button className="btn btn-primary btn-sm" onClick={() => updateProgresoMeta(m.idMeta, Math.min(100, m.progreso+10)).then(load)}>+10%</button>
              <button className="btn btn-danger btn-sm" onClick={() => deleteMeta(m.idMeta).then(load)}>×</button>
            </div>
          </div>
        </div>
      ))}
      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal">
            <div className="modal-header"><span className="modal-title">Nueva meta</span><button className="modal-close" onClick={() => setShowModal(false)}>×</button></div>
            <div className="form-group"><label className="form-label">Descripción *</label><input className="form-input" value={form.descripcion} onChange={e => setForm({...form,descripcion:e.target.value})} placeholder="Estudiar 2h de Cálculo diario" /></div>
            <div className="form-group"><label className="form-label">Horas objetivo</label><input className="form-input" type="number" value={form.horasObjetivo} onChange={e => setForm({...form,horasObjetivo:+e.target.value})} /></div>
            <div className="form-row">
              <div className="form-group"><label className="form-label">Fecha inicio</label><input className="form-input" type="date" value={form.fechaInicio} onChange={e => setForm({...form,fechaInicio:e.target.value})} /></div>
              <div className="form-group"><label className="form-label">Fecha fin</label><input className="form-input" type="date" value={form.fechaFin} onChange={e => setForm({...form,fechaFin:e.target.value})} /></div>
            </div>
            <div className="modal-footer"><button className="btn btn-ghost" onClick={() => setShowModal(false)}>Cancelar</button><button className="btn btn-primary" onClick={save}>Guardar</button></div>
          </div>
        </div>
      )}
    </div>
  )
}
