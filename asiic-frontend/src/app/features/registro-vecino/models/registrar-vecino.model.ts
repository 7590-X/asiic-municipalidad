export interface RegistrarVecinoRequest {
  cui: string;
  nit?: string;
  pasaporte?: string;
  nombres: string;
  apellidos: string;
  genero: 'M' | 'F';
  telefono: string;
  correo: string;
  direccion: string;
  estado_civil_id: string;
  profesion_id: string;
  locacion_id: number;
}

export interface ApiResponse<T = unknown> {
  code: number;
  action: string;
  datetime: string;
  message: string;
  payload: T | null;
}
