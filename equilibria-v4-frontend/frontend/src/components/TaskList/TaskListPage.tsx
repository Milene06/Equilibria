import { useEffect, useState } from 'react'
import { getTareas, createTarea, updateTarea, deleteTarea, completarTarea, priorizarTareas, getCursos, uploadArchivo, getArchivos } from '../../api'

export default function TaskListPage() {
  const [tareas, setTareas] = useState<any[]>([])
  const [cursos, setCursos] = useState<any[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editId, setEditId] = useState<number|null>(null)
  const [filterEstado, setFilterEstado] = useState('')
  const [filterTipo, setFilterTipo] = useState('')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(false)
  const [archivosMap, setArchivosMap] = useState<Record<number,any[]>>({})
  const [form, setForm] = useState({ nombre:'', fechaEntrega:'', prioridad:'media', tipo:'TAREA', dificultad:'media', tiempoEstimado:60, nota:'', idCurso:'' })

  const load = async () => {
    const [rt, rc] = await Promise.all([getTareas({ estado: filterEstado, tipo: filterTipo }), getCursos()])
    setTareas(rt.data); setCursos(rc.data)
  }
  
  useEffect(() => {load()}, [filterEstado, filterTipo])

  const filtered = tareas.filter(t => !search || t.nombre.toLowerCase().includes(search.toLowerCase()))

  const openCreate = () => { setEditId(null); setForm({ nombre:'', fechaEntrega:'', prioridad:'media', tipo:'TAREA', dificultad:'media', tiempoEstimado:60, nota:'', idCurso:'' }); setShowModal(true) }
  const openEdit = (t: any) => { setEditId(t.idTarea); setForm({ nombre:t.nombre, fechaEntrega:t.fechaEntrega, prioridad:t.prioridad, tipo:t.tipo||'TAREA', dificultad:t.dificultad||'media', tiempoEstimado:t.tiempoEstimado||60, nota:t.nota||'', idCurso:t.curso?.idCurso||'' }); setShowModal(true) }

  const save = async () => {
    const data = { ...form, idCurso: form.idCurso ? Number(form.idCurso) : undefined }
    if (editId) await updateTarea(editId, data)
    else await createTarea(data)
    setShowModal(false); load()
  }

  const handlePriorizar = async () => {
    setLoading(true)
    try { await priorizarTareas(); load() }
    finally { setLoading(false) }
  }

  const handleFile = async (tareaId: number, file: File) => {
    await uploadArchivo(tareaId, file)
    const r = await getArchivos(tareaId)
    setArchivosMap(prev => ({ ...prev, [tareaId]: r.data }))
  }

  const loadArchivos = async (tareaId: number) => {
    const r = await getArchivos(tareaId)
    setArchivosMap(prev => ({ ...prev, [tareaId]: r.data }))
  }

  const prioColor = (p: string) => p === 'alta' ? 'badge-danger' : p === 'media' ? 'badge-warn' : 'badge-teal'

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Mis Tareas</h1>
        <div style={{display:'flex',gap:8}}>
          <button className="btn btn-teal btn-sm" onClick={handlePriorizar} disabled={loading}>
            {loading ? '⏳' : '🧠'} Priorizar con IA
          </button>
          <button className="btn btn-primary" onClick={openCreate}>+ Nueva tarea</button>
        </div>
      </div>

      <div style={{display:'flex',gap:9,marginBottom:16,flexWrap:'wrap'}}>
        <input className="form-input" style={{maxWidth:260}} placeholder="🔍 Buscar tareas..." value={search} onChange={e => setSearch(e.target.value)} />
        <select className="form-input" style={{width:'auto'}} value={filterEstado} onChange={e => setFilterEstado(e.target.value)}>
          <option value="">Todas</option><option value="pendientes">Pendientes</option><option value="completadas">Completadas</option>
        </select>
        <select className="form-input" style={{width:'auto'}} value={filterTipo} onChange={e => setFilterTipo(e.target.value)}>
          <option value="">Todos los tipos</option>
          {['TAREA','EXAMEN','PROYECTO','LECTURA','OTRO'].map(t => <option key={t}>{t}</option>)}
        </select>
      </div>

      <div className="card">
        {filtered.length === 0 && <div className="empty-state"><div className="empty-state-icon">📋</div><div>Sin tareas. ¡Crea una!</div></div>}
        {filtered.map(t => (
          <div key={t.idTarea} style={{padding:'12px 0',borderBottom:'1px solid rgba(31,79,168,.07)',display:'flex',alignItems:'flex-start',gap:10}}>
            <input type="checkbox" checked={t.completada} style={{marginTop:3,accentColor:'var(--blue)',cursor:'pointer'}}
              onChange={() => completarTarea(t.idTarea).then(load)} />
            <div style={{flex:1}}>
              <div style={{fontSize:13,fontWeight:500,textDecoration:t.completada?'line-through':'none',color:t.completada?'#999':'inherit'}}>{t.nombre}</div>
              <div style={{display:'flex',gap:5,marginTop:5,flexWrap:'wrap',alignItems:'center'}}>
                <span className={`badge ${prioColor(t.prioridad)}`}>{t.prioridad}</span>
                <span className="badge badge-blue">{t.tipo}</span>
                <span style={{fontSize:11,color:'var(--muted)'}}>📅 {t.fechaEntrega}</span>
                {t.curso && <span style={{fontSize:11,color:'var(--blue)'}}>📚 {t.curso.nombre}</span>}
                {t.iaScore != null && <span style={{background:'#f0ebff',color:'#7c3aed',padding:'2px 7px',borderRadius:4,fontSize:10,fontWeight:600}}>🧠 Score: {t.iaScore}</span>}
                {t.tiempoEstimado && <span style={{fontSize:10,color:'#999'}}>⏱ {t.tiempoEstimado}min</span>}
              </div>
              {t.iaRazon && <div style={{fontSize:11,color:'var(--blue)',marginTop:3,fontStyle:'italic'}}>💡 {t.iaRazon}</div>}
              {t.nota && <div style={{fontSize:11,color:'#999',marginTop:2}}>📝 {t.nota}</div>}
              <div style={{marginTop:6,display:'flex',alignItems:'center',gap:6}}>
                <label style={{fontSize:11,color:'var(--blue)',cursor:'pointer',display:'flex',alignItems:'center',gap:3}}>
                  📎 Adjuntar
                  <input type="file" style={{display:'none'}} onChange={e => e.target.files && handleFile(t.idTarea, e.target.files[0])} />
                </label>
                <button style={{background:'none',border:'none',fontSize:11,color:'var(--muted)',cursor:'pointer'}}
                  onClick={() => loadArchivos(t.idTarea)}>Ver archivos</button>
                {archivosMap[t.idTarea]?.map((a: any) => (
                  <span key={a.idArchivo} style={{fontSize:10,background:'var(--blue-l)',color:'var(--blue)',padding:'2px 6px',borderRadius:4}}>{a.nombreArchivo}</span>
                ))}
              </div>
            </div>
            <div style={{display:'flex',gap:4,flexShrink:0}}>
              <button className="btn btn-ghost btn-sm" onClick={() => openEdit(t)}>✏️</button>
              <button className="btn btn-danger btn-sm" onClick={() => deleteTarea(t.idTarea).then(load)}>×</button>
            </div>
          </div>
        ))}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal">
            <div className="modal-header">
              <span className="modal-title">{editId ? 'Editar tarea' : 'Nueva tarea'}</span>
              <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
            </div>
            <div className="form-group"><label className="form-label">Nombre *</label>
              <input className="form-input" value={form.nombre} onChange={e => setForm({...form,nombre:e.target.value})} placeholder="Informe de proyecto" /></div>
            <div className="form-row">
              <div className="form-group"><label className="form-label">Fecha entrega *</label>
                <input className="form-input" type="date" value={form.fechaEntrega} onChange={e => setForm({...form,fechaEntrega:e.target.value})} /></div>
              <div className="form-group"><label className="form-label">Prioridad</label>
                <select className="form-input" value={form.prioridad} onChange={e => setForm({...form,prioridad:e.target.value})}>
                  <option value="alta">Alta</option><option value="media">Media</option><option value="baja">Baja</option>
                </select></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label className="form-label">Tipo</label>
                <select className="form-input" value={form.tipo} onChange={e => setForm({...form,tipo:e.target.value})}>
                  {['TAREA','EXAMEN','PROYECTO','LECTURA','OTRO'].map(t => <option key={t}>{t}</option>)}
                </select></div>
              <div className="form-group"><label className="form-label">Dificultad</label>
                <select className="form-input" value={form.dificultad} onChange={e => setForm({...form,dificultad:e.target.value})}>
                  <option value="facil">Fácil</option><option value="media">Media</option><option value="dificil">Difícil</option>
                </select></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label className="form-label">Tiempo estimado (min)</label>
                <input className="form-input" type="number" value={form.tiempoEstimado} onChange={e => setForm({...form,tiempoEstimado:+e.target.value})} /></div>
              <div className="form-group"><label className="form-label">Curso</label>
                <select className="form-input" value={form.idCurso} onChange={e => setForm({...form,idCurso:e.target.value})}>
                  <option value="">Sin curso</option>
                  {cursos.map((c: any) => <option key={c.idCurso} value={c.idCurso}>{c.nombre}</option>)}
                </select></div>
            </div>
            <div className="form-group"><label className="form-label">Nota</label>
              <input className="form-input" value={form.nota} onChange={e => setForm({...form,nota:e.target.value})} placeholder="Descripción opcional" /></div>
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={() => setShowModal(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={save}>Guardar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
