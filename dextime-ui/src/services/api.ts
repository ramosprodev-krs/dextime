import type {
  Appointment,
  AppointmentRequest,
  LoginRequest,
  LoginResponse,
  Procedure,
  ProcedureRequest,
  Provider,
  Store,
  StoreRequest,
  UserDto,
} from '../../types';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';


export const ENDPOINTS = {
  proceduresByStore: (storeId: string) => `/procedures/store/${encodeURIComponent(storeId)}`,
  myAppointments: '/appointments/my',
  cancelAppointment: (id: string) => `/appointments/cancel/${id}`,
};

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

let onUnauthorized: (() => void) | null = null;
export const setUnauthorizedHandler = (fn: () => void) => {
  onUnauthorized = fn;
};

async function request<T>(
  path: string,
  options: { method?: string; body?: unknown; token?: string | null } = {}
): Promise<T> {
  const { method = 'GET', body, token } = options;

  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers: {
        ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor. Tente novamente.', 0);
  }

  if (response.status === 401 && token) onUnauthorized?.();

  if (!response.ok) {
    let message = 'Algo deu errado. Tente novamente.';
    try {
      const data = await response.json();
      message = data.message || data.error || message;
    } catch {
      /* corpo vazio */
    }
    if (response.status === 404 && message === 'Algo deu errado. Tente novamente.') message = 'Não encontramos o que você procura.';
    if (response.status === 409 && message === 'Algo deu errado. Tente novamente.') message = 'Essa ação não está mais disponível. Atualizamos a lista para você.';
    if (response.status >= 500) message = 'O servidor está com problemas no momento. Tente novamente em instantes.';
    if (response.status === 401) message = token ? 'Sua sessão expirou. Entre novamente.' : 'Usuário ou senha inválidos.';
    if (response.status === 403) message = 'Você não tem permissão para essa ação.';
    throw new ApiError(message, response.status);
  }

  if (response.status === 204) return undefined as T;
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export const api = {
  login: (data: LoginRequest) =>
    request<LoginResponse>('/auth/login', { method: 'POST', body: data }),

  registerCustomer: (data: UserDto) =>
    request<unknown>('/customers', { method: 'POST', body: data }),

  getStores: (token: string) => request<Store[]>('/stores', { token }),

  getProcedures: (storeId: string, token: string) =>
    request<Procedure[]>(ENDPOINTS.proceduresByStore(storeId), { token }),

  requestAppointment: (data: AppointmentRequest, token: string) =>
    request<Appointment>('/appointments/request', { method: 'POST', body: data, token }),

  getMyAppointments: (token: string) =>
    request<Appointment[]>(ENDPOINTS.myAppointments, { token }),

  cancelAppointment: (id: string, token: string) =>
    request<Appointment>(ENDPOINTS.cancelAppointment(id), { method: 'POST', token }),

  /* ---------- Painel do provider (exigem JWT de PROVIDER) ---------- */

  getProviderStores: (token: string) => request<Store[]>('/providers/me/stores', { token }),

  assignAppointment: (id: string, token: string) =>
    request<Appointment>(`/appointments/assign/${id}`, { method: 'POST', token }),

  completeAppointment: (id: string, token: string) =>
    request<Appointment>(`/appointments/complete/${id}`, { method: 'POST', token }),

  /* ---------- Painel do manager (exigem JWT de MANAGER) ---------- */

  registerManager: (data: UserDto) =>
    request<unknown>('/managers', { method: 'POST', body: data }),

  createProvider: (storeId: string, data: UserDto, token: string) =>
    request<unknown>(`/providers?storeId=${encodeURIComponent(storeId)}`, { method: 'POST', body: data, token }),

  createStore: (data: StoreRequest, token: string) =>
    request<Store>('/stores', { method: 'POST', body: data, token }),

  createProcedure: (data: ProcedureRequest, token: string) =>
    request<Procedure>('/procedures', { method: 'POST', body: data, token }),

  addProcedureDates: (procedureId: string, dates: string[], token: string) =>
    request<Procedure>(`/procedures/${procedureId}/dates`, { method: 'PATCH', body: { dates }, token }),

  getStoreProviders: (storeId: string, token: string) =>
    request<Provider[]>(`/providers/store/${encodeURIComponent(storeId)}`, { token }),

  getStoreAppointments: (storeId: string, token: string) =>
    request<Appointment[]>(`/appointments/${storeId}/appointments`, { token }),
};