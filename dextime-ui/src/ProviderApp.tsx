import React, { useCallback, useEffect, useMemo, useState } from 'react';
import './manager.css';
import { api, ApiError, setUnauthorizedHandler } from './services/api';
import EmptyState from './EmptyState';
import type { Appointment, Store } from '../types';
import {
  checkPanelAccess,
  formatTime,
  getRoles,
  hueFromString,
  initials,
  isTokenExpired,
  parseJwt,
  STATUS_CLASS,
  STATUS_LABEL,
} from './utils';

const TOKEN_KEY = 'dextime:provider-token:v2';
type Toast = { type: 'success' | 'error'; text: string } | null;
type Tab = 'available' | 'mine';
type View = 'appointments' | 'stores';

const errMsg = (e: unknown, fallback = 'Algo deu errado.') => (e instanceof ApiError ? e.message : fallback);

/* ------------------------------------------------------------------ */
/* Login                                                              */
/* ------------------------------------------------------------------ */

const ProviderLogin: React.FC<{ onLogin: (t: string) => void }> = ({ onLogin }) => {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await api.login(form);
      const denied = checkPanelAccess(getRoles(res.token, res.role), 'PROVIDER');
      if (denied) {
        setError(denied);
        return;
      }
      onLogin(res.token);
    } catch (err) {
      setError(errMsg(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-header">
          <div className="brand-logo">D</div>
          <h2>DexTime Profissional</h2>
          <p>Acesse com o usuário criado pelo seu estabelecimento</p>
        </div>
        {error && <div className="alert alert-danger" role="alert">{error}</div>}
        <form onSubmit={submit} className="auth-form">
          <div className="form-group">
            <label htmlFor="p-user">Usuário</label>
            <input id="p-user" value={form.username} autoComplete="username"
              onChange={(e) => setForm({ ...form, username: e.target.value })} required />
          </div>
          <div className="form-group">
            <label htmlFor="p-pass">Senha</label>
            <input id="p-pass" type="password" value={form.password} autoComplete="current-password"
              onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          </div>
          <button className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Entrando…' : 'Entrar'}</button>
        </form>
      </div>
    </div>
  );
};

/* ------------------------------------------------------------------ */
/* Card de atendimento                                                */
/* ------------------------------------------------------------------ */

interface RowProps {
  a: Appointment;
  busy: boolean;
  onAssign?: () => void;
  onComplete?: () => void;
}

const Row: React.FC<RowProps> = ({ a, busy, onAssign, onComplete }) => {
  const d = new Date(a.date);
  return (
    <article className="appointment-card">
      <div className="appointment-date">
        <strong>{d.toLocaleDateString('pt-BR', { day: '2-digit' })}</strong>
        <span>{d.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')}</span>
      </div>
      <div className="appointment-info">
        <h3>{a.procedureName}</h3>
        <p>{formatTime(a.date)} · Cliente: {a.customerUsername}</p>
      </div>
      <div className="appointment-side">
        <span className={`badge ${STATUS_CLASS[a.status]}`}>{STATUS_LABEL[a.status]}</span>
        {onAssign && <button className="btn btn-primary btn-sm" disabled={busy} onClick={onAssign}>{busy ? 'Assumindo…' : 'Assumir'}</button>}
        {onComplete && <button className="btn btn-primary btn-sm" disabled={busy} onClick={onComplete}>{busy ? 'Concluindo…' : 'Concluir'}</button>}
      </div>
    </article>
  );
};

/* ------------------------------------------------------------------ */
/* Painel                                                             */
/* ------------------------------------------------------------------ */

const ProviderApp: React.FC = () => {
  const [token, setToken] = useState<string | null>(() => {
    try {
      const saved = localStorage.getItem(TOKEN_KEY);
      return saved && !isTokenExpired(saved) ? saved : null;
    } catch {
      return null;
    }
  });
  const [view, setView] = useState<View>('appointments');
  const [stores, setStores] = useState<Store[] | null>(null);
  const [storeId, setStoreId] = useState<string | null>(null);
  const [items, setItems] = useState<Appointment[] | null>(null);
  const [tab, setTab] = useState<Tab>('available');
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<Toast>(null);

  const username = token ? parseJwt(token).sub ?? '' : '';

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 4500);
    return () => clearTimeout(t);
  }, [toast]);

  const logout = useCallback(() => {
    try { localStorage.removeItem(TOKEN_KEY); } catch { /* noop */ }
    setToken(null);
    setStores(null);
    setStoreId(null);
    setItems(null);
    setView('appointments');
  }, []);

  useEffect(() => { setUnauthorizedHandler(logout); }, [logout]);

  const login = (t: string) => {
    try { localStorage.setItem(TOKEN_KEY, t); } catch { /* noop */ }
    setToken(t);
  };

  // lojas em que o provider trabalha
  const loadStores = useCallback(() => {
    if (!token) return;
    setStores(null);
    setError(null);
    api.getProviderStores(token)
      .then((list) => {
        setStores(list);
        setStoreId((cur) => cur ?? list[0]?.id ?? null);
      })
      .catch((e) => setError(errMsg(e)));
  }, [token]);

  useEffect(loadStores, [loadStores]);

  // agendamentos da loja selecionada
  const loadAppointments = useCallback(() => {
    if (!token || !storeId) return;
    setItems(null);
    api.getStoreAppointments(storeId, token)
      .then(setItems)
      .catch((e) => { setItems([]); setToast({ type: 'error', text: errMsg(e) }); });
  }, [token, storeId]);

  useEffect(loadAppointments, [loadAppointments]);

  const replace = (updated: Appointment) =>
    setItems((list) => list?.map((i) => (i.id === updated.id ? updated : i)) ?? list);

  const assign = async (a: Appointment) => {
    if (!token) return;
    setBusy(a.id);
    try {
      replace(await api.assignAppointment(a.id, token));
      setToast({ type: 'success', text: 'Atendimento assumido! O cliente foi avisado por e-mail.' });
      setTab('mine');
    } catch (err) {
      setToast({ type: 'error', text: errMsg(err) });
      loadAppointments(); // outro profissional pode ter assumido antes
    } finally {
      setBusy(null);
    }
  };

  const complete = async (a: Appointment) => {
    if (!token) return;
    setBusy(a.id);
    try {
      replace(await api.completeAppointment(a.id, token));
      setToast({ type: 'success', text: 'Atendimento concluído.' });
    } catch (err) {
      setToast({ type: 'error', text: errMsg(err) });
    } finally {
      setBusy(null);
    }
  };

  const available = useMemo(
    () => (items ?? []).filter((a) => a.status === 'PENDING' && !a.providerUsername)
      .sort((x, y) => new Date(x.date).getTime() - new Date(y.date).getTime()),
    [items]
  );
  const mine = useMemo(
    () => (items ?? []).filter((a) => a.providerUsername === username)
      .sort((x, y) => new Date(x.date).getTime() - new Date(y.date).getTime()),
    [items, username]
  );

  if (!token) return <div className="app-container"><ProviderLogin onLogin={login} /></div>;

  const currentStore = stores?.find((s) => s.id === storeId);
  const list = tab === 'available' ? available : mine;

  return (
    <div className="app-container">
      <header className="navbar">
        <div className="navbar-inner">
          <span className="navbar-brand"><span className="brand-logo-small">D</span><span>DexTime Profissional</span></span>
          <nav className="navbar-links">
            <button className={view === 'appointments' ? 'active' : ''} onClick={() => setView('appointments')}>Atendimentos</button>
            <button className={view === 'stores' ? 'active' : ''} onClick={() => setView('stores')}>Minhas lojas</button>
          </nav>
          <div className="navbar-user">
            <span className="user-chip"><span className="user-avatar">{username.slice(0, 1).toUpperCase()}</span><span className="user-name">{username}</span></span>
            <button className="btn btn-secondary btn-sm" onClick={logout}>Sair</button>
          </div>
        </div>
      </header>

      <main className="page">
        {error && (
          <EmptyState icon="⚠️" title="Não foi possível carregar seus estabelecimentos" text={error}
            actionLabel="Tentar novamente" onAction={loadStores} />
        )}

        {!error && stores === null && <div className="list"><div className="skeleton skeleton-row" /><div className="skeleton skeleton-row" /></div>}

        {!error && stores && stores.length === 0 && (
          <EmptyState icon="🏪" title="Você ainda não está vinculado a um estabelecimento"
            text="Peça ao gestor para cadastrar o seu usuário em um estabelecimento." />
        )}

        {/* ---------- Minhas lojas ---------- */}
        {!error && stores && stores.length > 0 && view === 'stores' && (
          <>
            <h1 className="page-title">Minhas lojas</h1>
            <p className="muted" style={{ marginBottom: 20 }}>
              Você presta serviços em {stores.length} {stores.length === 1 ? 'estabelecimento' : 'estabelecimentos'}.
            </p>
            <div className="list">
              {stores.map((s) => {
                const hue = hueFromString(s.name);
                return (
                  <button key={s.id} className="store-row"
                    onClick={() => { setStoreId(s.id); setTab('available'); setView('appointments'); }}>
                    <span className="store-row-avatar" style={{ background: `linear-gradient(135deg, hsl(${hue} 70% 55%), hsl(${(hue + 40) % 360} 70% 40%))` }}>{initials(s.name)}</span>
                    <span className="store-row-info">
                      <strong>{s.name}</strong>
                      <span className="muted small">CNPJ {s.cnpj}{s.managerUsername ? ` · Gestor: ${s.managerUsername}` : ''}</span>
                    </span>
                    <span className="store-cta">Ver atendimentos →</span>
                  </button>
                );
              })}
            </div>
          </>
        )}

        {/* ---------- Atendimentos ---------- */}
        {!error && stores && stores.length > 0 && view === 'appointments' && (
          <>
            <h1 className="page-title">{currentStore ? currentStore.name : 'Atendimentos'}</h1>

            {stores.length > 1 && (
              <div className="chips" style={{ marginBottom: 20 }}>
                {stores.map((s) => (
                  <button key={s.id} className={`chip ${s.id === storeId ? 'chip-active' : ''}`} onClick={() => setStoreId(s.id)}>{s.name}</button>
                ))}
              </div>
            )}

            <div className="tabs">
              <button className={tab === 'available' ? 'active' : ''} onClick={() => setTab('available')}>Disponíveis ({available.length})</button>
              <button className={tab === 'mine' ? 'active' : ''} onClick={() => setTab('mine')}>Meus atendimentos ({mine.length})</button>
            </div>

            {items === null && <div className="list"><div className="skeleton skeleton-row" /><div className="skeleton skeleton-row" /></div>}

            {items && list.length === 0 && (
              tab === 'available'
                ? <EmptyState icon="📭" title="Nenhum agendamento aguardando" text="Quando um cliente solicitar um horário, ele aparece aqui para você assumir." actionLabel="Atualizar" onAction={loadAppointments} />
                : <EmptyState icon="🗓️" title="Você ainda não tem atendimentos" text="Assuma um agendamento da aba Disponíveis para ele aparecer aqui." />
            )}

            {items && list.length > 0 && (
              <div className="list">
                {list.map((a) => (
                  <Row key={a.id} a={a} busy={busy === a.id}
                    onAssign={tab === 'available' ? () => assign(a) : undefined}
                    onComplete={tab === 'mine' && a.status === 'CONFIRMED' ? () => complete(a) : undefined} />
                ))}
              </div>
            )}
          </>
        )}
      </main>

      {toast && <div className={`toast toast-${toast.type}`} role="status">{toast.text}</div>}
    </div>
  );
};

export default ProviderApp;