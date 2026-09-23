export interface IncidenciaPayload {
  tipoIncidencia: 'QUEJA' | 'RECLAMO' | 'DENUNCIA' | 'SUGERENCIA';
  privacidad: 'PUBLICO' | 'CONFIDENCIAL' | 'ANONIMO';
  esBorrador: boolean;
  solicitante?: Solicitante;
  detalleQueja?: DetalleQueja;
  detalleReclamo?: DetalleReclamo;
  detalleDenuncia?: DetalleDenuncia;
  detalleSugerencia?: DetalleSugerencia;
}

export interface Solicitante {
  dpi: string;
  nombresApellidos: string;
  correo: string;
  telefono: string;
  direccion: string;
}

export interface DetalleQueja {
  dependenciaId: number;
  empleadoId?: number;
  fechaIncidencia: string;
  lugar: string;
  descripcion: string;
  testigo?: {
    nombre: string;
    telefono: string;
    correo: string;
  };
}

export interface DetalleReclamo {
  tipoServicioId: number;
  ubicacionGps: string;
  direccion?: string;
  noContador?: string;
  descripcion: string;
}

export interface DetalleDenuncia {
  tipoDenunciaId: number;
  denunciados: string;
  fechaHoraHechos: string;
  direccion: string;
  relato: string;
}

export interface DetalleSugerencia {
  areaId: number;
  descripcionActual: string;
  propuestaMejora: string;
}
