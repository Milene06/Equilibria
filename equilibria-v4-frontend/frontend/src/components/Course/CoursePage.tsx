import { useEffect, useState } from 'react'
import { getCursos, createCurso, updateCurso, deleteCurso, getHorariosClase, createHorarioClase, deleteHorarioClase } from '../../api'

const COLORS = ['#1F4FA8','#2DA39B','#BA7517','#A32D2D','#7c3aed','#0891b2','#be185d','#059669']
const DIAS_OPTIONS = ['LUNES','MARTES','MIÉRCOLES','JUEVES','VIERNES','SÁBADO']

export default function CoursePage() {
  const [cursos, setCursos] = useState<any[]>([])
  const [horarios, setHorarios] = useState<any[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editId, setEditId] = useState<number|null>(null)
  const [form, setForm] = useState({ nombre:'', codigo:'', creditos:0, color:'#1F4FA8', fechaExamen:'' })
  const [bloques, setBloques] = useState<{dia:string, horaInicio:string, horaFin:string}[]>([])

  const load = () => {
    getCursos().then(r => setCursos(r.data)).catch(() => {})
    getHorariosClase().then(r => setHorarios(r.data)).catch(() => {})
  }
  useEffect(() => { load() }, [])

  const openCreate = () => {
    setEditId(null)
    setForm({ nombre:'', codigo:'', creditos:0, color:'#1F4FA8', fechaExamen:'' })
    setBloques([])
    setShowModal(true)
  }
  const openEdit = (c: any) => {
    setEditId(c.idCurso)
    setForm({ nombre:c.nombre, codigo:c.codigo||'', creditos:c.creditos||0, color:c.color||'#1F4FA8', fechaExamen:c.fechaExamen||'' })
    // cargar bloques existentes de este curso
    const existentes = horarios.filter((h:any) => h.idCurso === c.idCurso)
    setBloques(existentes.map((h:any) => ({ dia: h.dia, horaInicio: h.horaInicio?.substring(0,5), horaFin: h.horaFin?.substring(0,5) })))
    setShowModal(true)
  }

  const addBloque = () => setBloques([...bloques, { dia:'LUNES', horaInicio:'08:00', horaFin:'10:00' }])
  const removeBloque = (i: number) => setBloques(bloques.filter((_,idx) => idx !== i))
  const updateBloque = (i: number, field: string, val: string) => {
    const copy = [...bloques]
    copy[i] = { ...copy[i], [field]: val }
    setBloques(copy)
  }

  const save = async () => {
    let cursoId = editId
    if (editId) {
      await updateCurso(editId, form)
      // borrar horarios viejos de este curso y recrear
      const viejos = horarios.filter((h:any) => h.idCurso === editId)
      await Promise.all(viejos.map((h:any) => deleteHorarioClase(h.idHorario)))
    } else {
      const res = await createCurso(form)
      cursoId = res.data.idCurso
    }
    // crear nuevos bloques
    await Promise.all(bloques.map(b => createHorarioClase({ idCurso: cursoId, ...b })))
    setShowModal(false)
    load()
  }

  const getHorariosCurso = (idCurso: number) => horarios.filter((h:any) => h.idCurso === idCurso)

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📚 Mis Cursos</h1>
        <button className="btn btn-primary" onClick={openCreate}>+ Nuevo curso</button>
      </div>
      {cursos.length === 0 && <div className="empty-state"><div className="empty-state-icon">📚</div><div>Agrega tu primer curso</div></div>}
      <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(240px,1fr))',gap:14}}>
        {cursos.map(c => (
          <div key={c.idCurso} className="card" style={{borderLeft:`4px solid ${c.color}`}}>
            <div style={{fontSize:14,fontWeight:700,marginBottom:4}}>{c.nombre}</div>
            <div style={{fontSize:12,color:'var(--muted)'}}>{c.codigo && `${c.codigo} · `}{c.creditos ? `${c.creditos} créditos` : ''}</div>
            {getHorariosCurso(c.idCurso).length > 0 && (
              <div style={{marginTop:6}}>
                {getHorariosCurso(c.idCurso).map((h:any,i:number) => (
                  <div key={i} style={{fontSize:11,color:'var(--muted)',marginTop:2}}>
                    🕐 {h.dia.substring(0,3)} {h.horaInicio?.substring(0,5)}–{h.horaFin?.substring(0,5)}
                  </div>
                ))}
              </div>
            )}
            {c.fechaExamen && <div style={{fontSize:11,color:'#999',marginTop:4}}>📅 Examen: {c.fechaExamen}</div>}
            <div style={{display:'flex',gap:7,marginTop:12}}>
              <button className="btn btn-ghost btn-sm" onClick={() => openEdit(c)}>✏️ Editar</button>
              <button className="btn btn-danger btn-sm" onClick={() => deleteCurso(c.idCurso).then(load)}>Eliminar</button>
            </div>
          </div>
        ))}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal" style={{maxHeight:'90vh',overflowY:'auto'}}>
            <div className="modal-header">
              <span className="modal-title">{editId ? 'Editar curso' : 'Nuevo curso'}</span>
              <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
            </div>

            <div className="form-group"><label className="form-label">Nombre *</label>
              <input className="form-input" value={form.nombre} onChange={e => setForm({...form,nombre:e.target.value})} placeholder="Cálculo Diferencial" />
            </div>
            <div className="form-row">
              <div className="form-group"><label className="form-label">Código</label>
                <input className="form-input" value={form.codigo} onChange={e => setForm({...form,codigo:e.target.value})} placeholder="MA01" />
              </div>
              <div className="form-group"><label className="form-label">Créditos</label>
                <input className="form-input" type="number" value={form.creditos} onChange={e => setForm({...form,creditos:+e.target.value})} />
              </div>
            </div>
            <div className="form-group"><label className="form-label">Fecha de examen</label>
              <input className="form-input" type="date" value={form.fechaExamen} onChange={e => setForm({...form,fechaExamen:e.target.value})} />
            </div>
            <div className="form-group"><label className="form-label">Color</label>
              <div style={{display:'flex',gap:7,flexWrap:'wrap',marginTop:5}}>
                {COLORS.map(c => <div key={c} onClick={() => setForm({...form,color:c})} style={{width:26,height:26,borderRadius:'50%',background:c,cursor:'pointer',border:`3px solid ${form.color===c?'#1a1a2e':'transparent'}`}} />)}
              </div>
            </div>

            {/* HORARIOS DE CLASE */}
            <div className="form-group">
              <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:8}}>
                <label className="form-label" style={{margin:0}}>Horario de clases</label>
                <button className="btn btn-ghost btn-sm" onClick={addBloque}>+ Agregar día</button>
              </div>
              {bloques.length === 0 && <div style={{fontSize:12,color:'var(--muted)'}}>Sin horario registrado</div>}
              {bloques.map((b,i) => (
                <div key={i} style={{display:'flex',gap:6,alignItems:'center',marginBottom:6,flexWrap:'wrap'}}>
                  <select className="form-input" style={{flex:'1 1 120px'}} value={b.dia} onChange={e => updateBloque(i,'dia',e.target.value)}>
                    {DIAS_OPTIONS.map(d => <option key={d} value={d}>{d}</option>)}
                  </select>
                  <input className="form-input" type="time" style={{flex:'1 1 90px'}} value={b.horaInicio} onChange={e => updateBloque(i,'horaInicio',e.target.value)} />
                  <span style={{fontSize:12,color:'var(--muted)'}}>a</span>
                  <input className="form-input" type="time" style={{flex:'1 1 90px'}} value={b.horaFin} onChange={e => updateBloque(i,'horaFin',e.target.value)} />
                  <button className="btn btn-danger btn-sm" onClick={() => removeBloque(i)}>✕</button>
                </div>
              ))}
            </div>

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