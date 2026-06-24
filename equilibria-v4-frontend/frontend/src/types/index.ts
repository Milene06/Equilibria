export interface Usuario { idUsuario: number; nombre: string; email: string; rol: string }
export interface Curso { idCurso: number; nombre: string; codigo?: string; creditos?: number; color: string; fechaExamen?: string }
export interface Tarea { idTarea: number; nombre: string; fechaEntrega: string; prioridad: string; tipo?: string; dificultad?: string; tiempoEstimado?: number; completada: boolean; nota?: string; iaScore?: number; iaRazon?: string; curso?: Curso }
export interface Meta { idMeta: number; descripcion: string; horasObjetivo?: number; fechaInicio: string; fechaFin: string; progreso: number; completada: boolean }
