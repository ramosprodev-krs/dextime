export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  /** Papel do usuário (MANAGER, CUSTOMER, PROVIDER). Se a API não enviar, lemos do JWT. */
  role?: string;
}

export interface UserDto {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  cpf: string;
}

export interface Store {
  id: string;
  name: string;
  cnpj: string;
  managerUsername: string;
  createdAt: string;
  updatedAt: string;
}

export interface Procedure {
  id: string;
  name: string;
  description: string;
  price: number;
  availableDates: string[];
  storeId: string;
  storeName: string;
  createdByUsername: string;
  createdAt: string;
  updatedAt: string;
}

export type AppointmentStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface Appointment {
  id: string;
  status: AppointmentStatus;
  date: string;
  customerUsername: string;
  providerUsername: string | null;
  storeName: string;
  procedureName: string;
  createdAt: string;
  updatedAt: string;
}

export interface AppointmentRequest {
  storeId: string;
  procedureId: string;
  date: string;
}

export interface StoreRequest {
  name: string;
  cnpj: string;
}

export interface ProcedureRequest {
  name: string;
  description: string;
  price: number;
  storeId: string;
  availableDates: string[];
}

/** Prestador (provider) como o gestor o enxerga. */
export interface Provider {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}