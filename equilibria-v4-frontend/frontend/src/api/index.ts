import axios from 'axios'

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL ? `${import.meta.env.VITE_API_URL}/api` : '/api' })

api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('eq_token')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

api.interceptors.response.use(r => r, err => {
  if (err.response?.status === 401) {
    localStorage.removeItem('eq_token')
    localStorage.removeItem('eq_user')
    window.location.href = '/login'
  }
  return Promise.reject(err)
})

export default api

export const login = (email: string, password: string) => api.post('/auth/login', { email, password })
export const register = (nombre: string, email: string, password: string) => api.post('/auth/register', { nombre, email, password })
export const getCursos = () => api.get('/courses')
export const createCurso = (data: any) => api.post('/courses', data)
export const updateCurso = (id: number, data: any) => api.put(`/courses/${id}`, data)
export const deleteCurso = (id: number) => api.delete(`/courses/${id}`)
export const getTareas = (params?: any) => {
  const cleanParams: any = {}
  if (params?.estado) cleanParams.estado = params.estado
  if (params?.tipo) cleanParams.tipo = params.tipo
  return api.get('/tasks', { params: cleanParams })
}
export const createTarea = (data: any) => api.post('/tasks', data)
export const updateTarea = (id: number, data: any) => api.put(`/tasks/${id}`, data)
export const deleteTarea = (id: number) => api.delete(`/tasks/${id}`)
export const completarTarea = (id: number) => api.patch(`/tasks/${id}/completar`)
export const priorizarTareas = () => api.post('/tasks/priorizar')
export const getMetas = () => api.get('/metas')
export const createMeta = (data: any) => api.post('/metas', data)
export const updateProgresoMeta = (id: number, progreso: number) => api.patch(`/metas/${id}/progreso`, { progreso })
export const deleteMeta = (id: number) => api.delete(`/metas/${id}`)
export const getAsistencia = () => api.get('/attendance')
export const createAsistencia = (data: any) => api.post('/attendance', data)
export const deleteAsistencia = (id: number) => api.delete(`/attendance/${id}`)
export const getCuestionarioPSS10 = () => api.get('/wellbeing/pss10/cuestionario')
export const submitPSS10 = (respuestas: Record<string, number>) => api.post('/wellbeing/pss10', respuestas)
export const getHistorialEstres = () => api.get('/wellbeing/historial')
export const getFraseMotivacional = () => api.get('/wellbeing/frase')
export const getTecnicasEstudio = () => api.get('/wellbeing/tecnicas')
export const generarHorario = () => api.post('/schedule/generar')
export const reagendarTareas = () => api.post('/schedule/reagendar')
export const getDashboard = () => api.get('/stats/dashboard')
export const getResumenDiario = () => api.get('/stats/resumen-diario')
export const syncData = () => api.get('/stats/sync')
export const getProgresoHistorico = () => api.get('/stats/progreso-historico')
export const exportarPDF = () => api.get('/stats/exportar-pdf', { responseType: 'blob' })
export const compartirProgreso = () => api.post('/stats/compartir-progreso')
export const getMetricasAdmin = () => api.get('/stats/admin/metricas')
export const getPreferencias = () => api.get('/preferences')
export const setModoDescanso = (activo: boolean, hasta?: string) => api.post('/preferences/modo-descanso', { activo, hasta })
export const getModoDescanso = () => api.get('/preferences/modo-descanso')
export const setHorarioDescanso = (horaInicio: string, horaFin: string) => api.post('/preferences/horario-descanso', { horaInicio, horaFin })
export const setSesionEstudio = (minutos: number, descanso: number) => api.post('/preferences/sesion-estudio', { minutos, descanso })
export const setPerfilEstudio = (tipo: string, horasPico: string) => api.post('/preferences/perfil-estudio', { tipo, horasPico })
export const savePreferencia = (clave: string, valor: string) => api.put('/preferences', { clave, valor })
export const subscribePush = (endpoint: string) => api.post('/notifications/subscribe', { endpoint })
export const unsubscribePush = () => api.delete('/notifications/unsubscribe')
export const getArchivos = (tareaId: number) => api.get(`/storage/tasks/${tareaId}/files`)
export const uploadArchivo = (tareaId: number, file: File) => {
  const form = new FormData(); form.append('file', file)
  return api.post(`/storage/tasks/${tareaId}/files`, form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const deleteArchivo = (id: number) => api.delete(`/storage/files/${id}`)
export const sendFeedback = (data: any) => api.post('/feedback', data)
export const setResumenSemanal = (activo: boolean, dia?: string, hora?: string) => api.post('/preferences/resumen-semanal', { activo, dia, hora })
export const getResumenSemanalConfig = () => api.get('/preferences/resumen-semanal')
export const getHorariosClase = () => api.get('/horarios-clase')
export const createHorarioClase = (data: any) => api.post('/horarios-clase', data)
export const deleteHorarioClase = (id: number) => api.delete(`/horarios-clase/${id}`)
export const updateHorarioClase = (id: number, data: any) => api.put(`/horarios-clase/${id}`, data)
