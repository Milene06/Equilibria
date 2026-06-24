import { useEffect, useState } from 'react'
import { getAsistencia, createAsistencia, deleteAsistencia, getCursos } from '../../api'

export default function AttendancePage() {
  const [data, setData] = useState<any>({ registros:[], resumen:[] })
  const [cursos, setCursos] = useState<any[]>([])
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ idCurso:'', fecha:'', estado:'ASISTIO' })

  const load = async () => {
  const [ra, rc] = await Promise.allSettled([getAsistencia(), getCursos()])
  if (ra.status === 'fulfilled') setData(ra.value.data)
  if (rc.status === 'fulfilled') setCursos(rc.value.data)
}
  useEffect(() => { load() }, [])

  const save = async () => { await createAsistencia({ ...form, idCurso: +form.idCurso }); setShowModal(false); load() }

  return (
    <div>
      <div className="page-header"><h1 className="page-title">📋 Asistencia</h1><button className="btn btn-primary" onClick={() => setShowModal(true)}>+ Registrar asistencia</button></div>
      <div className="grid-2" style={{marginBottom:16}}>
        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>Resumen por curso</div>
          {data.resumen.length === 0 && <div style={{fontSize:13,color:'var(--muted)',textAlign:'center',padding:20}}>Sin registros</div>}
          {data.resumen.map((r: any) => (
            <div key={r.idCurso} style={{marginBottom:14}}>
              <div style={{display:'flex',justifyContent:'space-between',marginBottom:4}}>
                <span style={{fontSize:12,fontWeight:500}}>{r.curso}</span>
                <span style={{fontSize:11,color:'#999'}}>{r.asistio}/{r.total} ({r.porcentaje}%)</span>
              </div>
              <div style={{height:6,background:'#f4f6fb',borderRadius:3,overflow:'hidden'}}>
                <div style={{width:`${r.porcentaje}%`,height:'100%',background:r.porcentaje<75?'var(--danger)':r.porcentaje<90?'var(--warn)':'var(--teal)'}} />
              </div>
              {r.alerta && <div style={{fontSize:10,color:'var(--danger)',marginTop:2}}>⚠️ Riesgo de desaprobación por inasistencia</div>}
            </div>
          ))}
        </div>
        <div className="card">
          <div style={{fontWeight:600,marginBottom:14}}>Registros recientes</div>
          {data.registros.slice(0,8).map((a: any) => (
            <div key={a.idAsistencia} style={{display:'flex',alignItems:'center',gap:10,padding:'8px 0',borderBottom:'1px solid rgba(31,79,168,.07)'}}>
              <span className={`badge ${a.estado==='ASISTIO'?'badge-teal':'badge-danger'}`}>{a.estado==='ASISTIO'?'✓':'✗'}</span>
              <div style={{flex:1,fontSize:13}}>{a.curso?.nombre}</div>
              <div style={{fontSize:11,color:'#999'}}>{a.fecha}</div>
              <button className="btn btn-ghost btn-sm" style={{padding:'3px 7px'}} onClick={() => deleteAsistencia(a.idAsistencia).then(load)}>×</button>
            </div>
          ))}
        </div>
      </div>
      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal">
            <div className="modal-header"><span className="modal-title">Registrar asistencia</span><button className="modal-close" onClick={() => setShowModal(false)}>×</button></div>
            <div className="form-group"><label className="form-label">Curso *</label>
              <select className="form-input" value={form.idCurso} onChange={e => setForm({...form,idCurso:e.target.value})}>
                <option value="">Selecciona un curso</option>
                {cursos.map((c: any) => <option key={c.idCurso} value={c.idCurso}>{c.nombre}</option>)}
              </select>
            </div>
            <div className="form-row">
              <div className="form-group"><label className="form-label">Fecha</label><input className="form-input" type="date" value={form.fecha} onChange={e => setForm({...form,fecha:e.target.value})} /></div>
              <div className="form-group"><label className="form-label">Estado</label>
                <select className="form-input" value={form.estado} onChange={e => setForm({...form,estado:e.target.value})}>
                  <option value="ASISTIO">✓ Asistió</option><option value="FALTA">✗ Falta</option><option value="TARDANZA">⚠ Tardanza</option>
                </select>
              </div>
            </div>
            <div className="modal-footer"><button className="btn btn-ghost" onClick={() => setShowModal(false)}>Cancelar</button><button className="btn btn-primary" onClick={save}>Guardar</button></div>
          </div>
        </div>
      )}
    </div>
  )
}
