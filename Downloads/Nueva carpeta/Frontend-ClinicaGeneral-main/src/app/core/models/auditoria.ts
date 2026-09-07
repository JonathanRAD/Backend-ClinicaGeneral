export interface AuditLog {
  id: number;
  fecha: string;
  usuarioId: number;
  usuarioEmail: string;
  usuarioNombre: string;
  rol: string;
  accion: string;
  entidad: string;
  entidadId: number | null;
  descripcion: string;
  datosAnteriores: string | null;
  datosNuevos: string | null;
  ipAddress: string;
}

export interface AuditPageResponse {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface AuditEstadisticas {
  totalEventos: number;
  eventosHoy: number;
  eventosSemana: number;
  eventosPorAccion: Record<string, number>;
  eventosPorEntidad: Record<string, number>;
  usuariosMasActivos: { nombre: string; email: string; total: number }[];
}

export interface AuditFiltros {
  usuarioId?: number;
  rol?: string;
  accion?: string;
  entidad?: string;
  fechaInicio?: string;
  fechaFin?: string;
  busqueda?: string;
}
