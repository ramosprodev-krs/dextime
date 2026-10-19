import type { AppointmentStatus } from '../types';

export const parseJwt = (token: string): { sub?: string; exp?: number } => {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(payload)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return {};
  }
};

const normRole = (r: string): string => r.replace(/^ROLE_/, '').toUpperCase();

/** Papéis do usuário: usa o campo `role` do login; senão, tenta as claims do JWT. */
export const getRoles = (token: string, hint?: string): string[] => {
  if (hint) return [normRole(hint)];
  const claims = parseJwt(token) as Record<string, unknown>;
  const raw = claims.role ?? claims.roles ?? claims.authorities ?? claims.scope;
  const list = Array.isArray(raw) ? raw : raw ? [raw] : [];
  return list
    .map((r) => (typeof r === 'string' ? r : (r as { authority?: string })?.authority))
    .filter((r): r is string => typeof r === 'string')
    .map(normRole);
};

const PANEL_PATH: Record<string, string> = { CUSTOMER: '/', MANAGER: '/manager', PROVIDER: '/provider' };

/** Retorna uma mensagem de erro se o usuário não pode usar este painel; null se pode. */
export const checkPanelAccess = (roles: string[], expected: 'CUSTOMER' | 'MANAGER' | 'PROVIDER'): string | null => {
  if (roles.includes(expected)) return null;
  if (roles.length === 0) return 'Não foi possível verificar o tipo da sua conta. Tente novamente mais tarde.';
  const own = roles.map((r) => PANEL_PATH[r]).find(Boolean);
  return own
    ? `Esta conta não tem acesso a este painel. Acesse por ${own === '/' ? 'o app do cliente (endereço raiz)' : own}.`
    : 'Esta conta não tem acesso a este painel.';
};

export const isTokenExpired =(token: string): boolean => {
  const { exp } = parseJwt(token);
  return exp ? exp * 1000 < Date.now() : false;
};

export const formatPrice = (value: number): string =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);

export const formatDateTime = (iso: string): string =>
  new Date(iso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

export const formatTime = (iso: string): string =>
  new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

export const dayKey = (iso: string): string => iso.slice(0, 10);

export const formatDayLabel = (key: string): string =>
  new Date(`${key}T00:00:00`).toLocaleDateString('pt-BR', {
    weekday: 'short',
    day: '2-digit',
    month: 'short',
  });

/** Agrupa datas ISO por dia, ordenadas, ignorando datas passadas. */
export const groupFutureDates = (dates: string[]): Record<string, string[]> => {
  const now = Date.now();
  const groups: Record<string, string[]> = {};
  [...dates]
    .filter((d) => new Date(d).getTime() > now)
    .sort()
    .forEach((d) => {
      (groups[dayKey(d)] ||= []).push(d);
    });
  return groups;
};

export const maskCpf = (v: string): string => {
  const d = v.replace(/\D/g, '').slice(0, 11);
  return d
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1-$2');
};

export const maskCnpj = (v: string): string => {
  const d = v.replace(/\D/g, '').slice(0, 14);
  return d
    .replace(/^(\d{2})(\d)/, '$1.$2')
    .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1/$2')
    .replace(/(\d{4})(\d)/, '$1-$2');
};

/** datetime-local ("2026-10-20T14:30") -> formato da API ("2026-10-20T14:30:00") */
export const toApiDate = (local: string): string => (local.length === 16 ? `${local}:00` : local);

export const maskPhone =(v: string): string => {
  const d = v.replace(/\D/g, '').slice(0, 13);
  if (d.length <= 2) return d ? `+${d}` : '';
  return `+${d.slice(0, 2)} ${d.slice(2)}`;
};

export const STATUS_LABEL: Record<AppointmentStatus, string> = {
  PENDING: 'Aguardando',
  CONFIRMED: 'Confirmado',
  IN_PROGRESS: 'Em andamento',
  COMPLETED: 'Concluído',
  CANCELLED: 'Cancelado',
};

export const STATUS_CLASS: Record<AppointmentStatus, string> = {
  PENDING: 'badge-warning',
  CONFIRMED: 'badge-success',
  IN_PROGRESS: 'badge-info',
  COMPLETED: 'badge-primary',
  CANCELLED: 'badge-danger',
};

export const initials = (name: string): string =>
  name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('');

/** Matiz estável derivado do nome, para dar identidade visual a cada loja. */
export const hueFromString = (s: string): number => {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h *31 + s.charCodeAt(i)) % 360;
  return h;
};