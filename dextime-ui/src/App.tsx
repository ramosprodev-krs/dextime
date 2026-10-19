import React, { useCallback, useEffect, useMemo, useState } from 'react';
import './App.css';
import { api, ApiError, setUnauthorizedHandler } from './services/api';
import ManagerApp from './ManagerApp';
import ProviderApp from './ProviderApp';
import EmptyState from './EmptyState';
import type { Appointment, LoginRequest, Procedure, Store, UserDto } from '../types';
import {
  checkPanelAccess,
  getRoles,
  formatDateTime,
  formatDayLabel,
  formatPrice,
  formatTime,
  groupFutureDates,
  hueFromString,
  initials,
  isTokenExpired,
  maskCpf,
  maskPhone,
  parseJwt,
  STATUS_CLASS,
  STATUS_LABEL,
} from './utils';

type AuthView = 'login' | 'register';
type View = 'explore' | 'store' | 'appointments';
type Toast = { type: 'success' | 'error'; text: string } | null;

const TOKEN_KEY = 'dextime:token:v2'; // v2: descarta tokens salvos antes da checagem de papel
const emptyUser: UserDto = {
  username: '', password: '', firstName: '', lastName: '', email: '', phoneNumber: '', cpf: '',
};

/* ------------------------------------------------------------------ */
/* Telas de autenticação                                              */
/* ------------------------------------------------------------------ */

interface AuthScreenProps {
  onLogin: (token: string) => void;
  notify: (t: Toast) => void;
}

const AuthScreen: React.FC<AuthScreenProps> = ({ onLogin, notify }) => {
  const [mode, setMode] = useState<AuthView>('login');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loginForm, setLoginForm] = useState<LoginRequest>({ username: '', password: '' });
  const [form, setForm] = useState<UserDto>(emptyUser);

  const switchMode = (m: AuthView) => {
    setMode(m);
    setError(null);
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await api.login(loginForm);
      const denied = checkPanelAccess(getRoles(res.token, res.role), 'CUSTOMER');
      if (denied) {
        setError(denied);
        return;
      }
      onLogin(res.token);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Erro inesperado.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await api.registerCustomer(form);
      setLoginForm({ username: form.username, password: '' });
      setForm(emptyUser);
      setMode('login');
      notify({ type: 'success', text: 'Conta criada! Faça login para continuar.' });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Erro inesperado.');
    } finally {
      setLoading(false);
    }
  };

  const set = (field: keyof UserDto, value: string) => setForm((f) => ({ ...f, [field]: value }));

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-header">
          <div className="brand-logo">D</div>
          <h2>DexTime</h2>
          <p>{mode === 'login' ? 'Agende seus serviços em poucos cliques' : 'Crie sua conta de cliente'}</p>
        </div>

        {error && <div className="alert alert-danger" role="alert">{error}</div>}

        {mode === 'login' ? (
          <form onSubmit={handleLogin} className="auth-form">
            <div className="form-group">
              <label htmlFor="l-user">Usuário</label>
              <input id="l-user" type="text" autoComplete="username" value={loginForm.username}
                onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })} required />
            </div>
            <div className="form-group">
              <label htmlFor="l-pass">Senha</label>
              <input id="l-pass" type="password" autoComplete="current-password" value={loginForm.password}
                onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} required />
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Entrando…' : 'Entrar'}
            </button>
            <p className="auth-switch">
              Ainda não tem conta?{' '}
              <button type="button" className="link-btn" onClick={() => switchMode('register')}>Cadastre-se</button>
            </p>
          </form>
        ) : (
          <form onSubmit={handleRegister} className="auth-form">
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="r-first">Nome</label>
                <input id="r-first" type="text" value={form.firstName}
                  onChange={(e) => set('firstName', e.target.value)} required />
              </div>
              <div className="form-group">
                <label htmlFor="r-last">Sobrenome</label>
                <input id="r-last" type="text" value={form.lastName}
                  onChange={(e) => set('lastName', e.target.value)} required />
              </div>
            </div>
            <div className="form-group">
              <label htmlFor="r-user">Usuário</label>
              <input id="r-user" type="text" autoComplete="username" value={form.username}
                onChange={(e) => set('username', e.target.value)} required />
            </div>
            <div className="form-group">
              <label htmlFor="r-pass">Senha</label>
              <input id="r-pass" type="password" autoComplete="new-password" value={form.password}
                onChange={(e) => set('password', e.target.value)} required />
            </div>
            <div className="form-group">
              <label htmlFor="r-mail">E-mail</label>
              <input id="r-mail" type="email" value={form.email}
                onChange={(e) => set('email', e.target.value)} required />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="r-phone">Telefone</label>
                <input id="r-phone" type="text" inputMode="tel" placeholder="+55 11999999999" value={form.phoneNumber}
                  onChange={(e) => set('phoneNumber', maskPhone(e.target.value))} required />
              </div>
              <div className="form-group">
                <label htmlFor="r-cpf">CPF</label>
                <input id="r-cpf" type="text" inputMode="numeric" placeholder="000.000.000-00" value={form.cpf}
                  onChange={(e) => set('cpf', maskCpf(e.target.value))} required />
              </div>
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Criando conta…' : 'Criar conta'}
            </button>
            <p className="auth-switch">
              Já tem conta?{' '}
              <button type="button" className="link-btn" onClick={() => switchMode('login')}>Entrar</button>
            </p>
          </form>
        )}
      </div>
    </div>
  );
};

/* ------------------------------------------------------------------ */
/* Explorar lojas                                                     */
/* ------------------------------------------------------------------ */

const ExploreView: React.FC<{ token: string; onSelect: (s: Store) => void }> = ({ token, onSelect }) => {
  const [stores, setStores] = useState<Store[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState('');

  const load = useCallback(() => {
    setStores(null);
    setError(null);
    api.getStores(token)
      .then(setStores)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Erro ao carregar lojas.'));
  }, [token]);

  useEffect(load, [load]);

  const filtered = useMemo(
    () => (stores ?? []).filter((s) => s.name.toLowerCase().includes(query.trim().toLowerCase())),
    [stores, query]
  );

  return (
    <>
      <section className="hero">
        <h1>Encontre e agende o seu próximo serviço</h1>
        <p>Escolha um estabelecimento, selecione o serviço e o melhor horário para você.</p>
        <input type="search" className="search-input" placeholder="Buscar estabelecimento…"
          value={query} onChange={(e) => setQuery(e.target.value)} aria-label="Buscar estabelecimento" />
      </section>

      {error && (
        <div className="state-box">
          <p>{error}</p>
          <button className="btn btn-secondary" onClick={load}>Tentar novamente</button>
        </div>
      )}

      {!error && stores === null && (
        <div className="grid">
          {[0, 1, 2, 3, 4, 5].map((i) => <div key={i} className="skeleton skeleton-card" />)}
        </div>
      )}

      {stores && filtered.length === 0 && (
        <div className="state-box">
          <p>{query ? 'Nenhum estabelecimento encontrado para essa busca.' : 'Ainda não há estabelecimentos disponíveis.'}</p>
        </div>
      )}

      {stores && filtered.length > 0 && (
        <div className="grid">
          {filtered.map((store) => {
            const hue = hueFromString(store.name);
            return (
              <button key={store.id} className="store-card" onClick={() => onSelect(store)}>
                <div className="store-cover" style={{ background: `linear-gradient(135deg, hsl(${hue} 70% 55%), hsl(${(hue + 40) % 360} 70% 40%))` }}>
                  <span>{initials(store.name)}</span>
                </div>
                <div className="store-body">
                  <h3>{store.name}</h3>
                  <span className="store-cta">Ver serviços →</span>
                </div>
              </button>
            );
          })}
        </div>
      )}
    </>
  );
};

/* ------------------------------------------------------------------ */
/* Loja + serviços                                                    */
/* ------------------------------------------------------------------ */

interface StoreViewProps {
  token: string;
  store: Store;
  onBack: () => void;
  onBooked: (a: Appointment) => void;
  notify: (t: Toast) => void;
}

const StoreView: React.FC<StoreViewProps> = ({ token, store, onBack, onBooked, notify }) => {
  const [procedures, setProcedures] = useState<Procedure[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<Procedure | null>(null);

  const load = useCallback(() => {
    setProcedures(null);
    setError(null);
    api.getProcedures(store.id, token)
      .then(setProcedures)
      .catch((e) => {
        // 404 = loja sem serviços; outros erros (ex.: 500) são falhas reais e aparecem como erro
        if (e instanceof ApiError && e.status === 404) setProcedures([]);
        else setError(e instanceof ApiError ? e.message : 'Erro ao carregar serviços.');
      });
  }, [store.id, token]);

  useEffect(load, [load]);

  const handleBooked = (appointment: Appointment, procedureId: string, date: string) => {
    // a API remove a data da lista; refletimos isso localmente
    setProcedures((list) =>
      list ? list.map((p) => (p.id === procedureId ? { ...p, availableDates: p.availableDates.filter((d) => d !== date) } : p)) : list
    );
    setSelected(null);
    onBooked(appointment);
  };

  const hue = hueFromString(store.name);

  return (
    <>
      <button className="link-btn back-link" onClick={onBack}>← Voltar</button>

      <header className="store-header" style={{ background: `linear-gradient(135deg, hsl(${hue} 70% 55%), hsl(${(hue + 40) % 360} 70% 38%))` }}>
        <div className="store-avatar">{initials(store.name)}</div>
        <div>
          <h1>{store.name}</h1>
          <p>Escolha um serviço para ver os horários disponíveis</p>
        </div>
      </header>

      {error && (
        <div className="state-box">
          <p>{error}</p>
          <button className="btn btn-secondary" onClick={load}>Tentar novamente</button>
        </div>
      )}

      {!error && procedures === null && (
        <div className="list">
          {[0, 1, 2].map((i) => <div key={i} className="skeleton skeleton-row" />)}
        </div>
      )}

      {procedures && procedures.length === 0 && (
        <EmptyState
          icon="✂️"
          title="Ainda não há serviços por aqui"
          text="Este estabelecimento ainda não cadastrou serviços. Volte em breve ou explore outros estabelecimentos."
          actionLabel="Ver outros estabelecimentos"
          onAction={onBack}
        />
      )}

      {procedures && procedures.length > 0 && (
        <div className="list">
          {procedures.map((p) => {
            const slots = Object.values(groupFutureDates(p.availableDates)).flat().length;
            return (
              <article key={p.id} className="service-card">
                <div className="service-info">
                  <h3>{p.name}</h3>
                  {p.description && <p>{p.description}</p>}
                  <span className={`slots ${slots === 0 ? 'slots-none' : ''}`}>
                    {slots === 0 ? 'Sem horários disponíveis' : `${slots} horário${slots > 1 ? 's' : ''} disponível${slots > 1 ? 'is' : ''}`}
                  </span>
                </div>
                <div className="service-action">
                  <strong className="price">{formatPrice(p.price)}</strong>
                  <button className="btn btn-primary" disabled={slots === 0} onClick={() => setSelected(p)}>
                    Agendar
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      )}

      {selected && (
        <BookingModal
          token={token}
          store={store}
          procedure={selected}
          onClose={() => setSelected(null)}
          onBooked={handleBooked}
          notify={notify}
        />
      )}
    </>
  );
};

/* ------------------------------------------------------------------ */
/* Modal de agendamento                                               */
/* ------------------------------------------------------------------ */

interface BookingModalProps {
  token: string;
  store: Store;
  procedure: Procedure;
  onClose: () => void;
  onBooked: (a: Appointment, procedureId: string, date: string) => void;
  notify: (t: Toast) => void;
}

const BookingModal: React.FC<BookingModalProps> = ({ token, store, procedure, onClose, onBooked, notify }) => {
  const groups = useMemo(() => groupFutureDates(procedure.availableDates), [procedure.availableDates]);
  const days = Object.keys(groups);
  const [day, setDay] = useState<string>(days[0]);
  const [slot, setSlot] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  const confirm = async () => {
    if (!slot) return;
    setLoading(true);
    try {
      // enviamos a data exatamente como veio da API, pois o backend a remove da lista por igualdade
      const appointment = await api.requestAppointment(
        { storeId: store.id, procedureId: procedure.id, date: slot }, token
      );
      notify({ type: 'success', text: 'Agendamento solicitado! Enviamos a confirmação por e-mail.' });
      onBooked(appointment, procedure.id, slot);
    } catch (err) {
      notify({ type: 'error', text: err instanceof ApiError ? err.message : 'Não foi possível agendar.' });
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" role="dialog" aria-modal="true" aria-label="Agendar serviço" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h3>{procedure.name}</h3>
            <p>{store.name} · {formatPrice(procedure.price)}</p>
          </div>
          <button className="icon-btn" onClick={onClose} aria-label="Fechar">✕</button>
        </div>

        <h4 className="modal-label">Escolha o dia</h4>
        <div className="chips">
          {days.map((d) => (
            <button key={d} className={`chip ${d === day ? 'chip-active' : ''}`}
              onClick={() => { setDay(d); setSlot(null); }}>
              {formatDayLabel(d)}
            </button>
          ))}
        </div>

        <h4 className="modal-label">Escolha o horário</h4>
        <div className="chips">
          {groups[day]?.map((d) => (
            <button key={d} className={`chip ${d === slot ? 'chip-active' : ''}`} onClick={() => setSlot(d)}>
              {formatTime(d)}
            </button>
          ))}
        </div>

        <div className="modal-footer">
          <div className="summary">
            {slot ? <>Você selecionou <strong>{formatDateTime(slot)}</strong></> : 'Selecione um horário para continuar'}
          </div>
          <button className="btn btn-primary" disabled={!slot || loading} onClick={confirm}>
            {loading ? 'Agendando…' : 'Confirmar agendamento'}
          </button>
        </div>
      </div>
    </div>
  );
};

/* ------------------------------------------------------------------ */
/* Meus agendamentos                                                  */
/* ------------------------------------------------------------------ */

interface AppointmentsViewProps {
  token: string;
  sessionAppointments: Appointment[];
  onExplore: () => void;
  notify: (t: Toast) => void;
}

const AppointmentsView: React.FC<AppointmentsViewProps> = ({ token, sessionAppointments, onExplore, notify }) => {
  const [items, setItems] = useState<Appointment[] | null>(null);
  const [fallback, setFallback] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState<string | null>(null);

  const load = useCallback(() => {
    setError(null);
    api.getMyAppointments(token)
      .then((list) => { setItems(list); setFallback(false); })
      .catch((e) => {
        if (e instanceof ApiError && (e.status === 404 || e.status === 405)) {
          // endpoint ainda não existe: mostra apenas o que foi agendado nesta sessão
          setFallback(true);
          setItems(sessionAppointments);
        } else {
          setError(e instanceof ApiError ? e.message : 'Erro ao carregar agendamentos.');
        }
      });
  }, [token, sessionAppointments]);

  useEffect(load, [load]);

  const cancel = async (a: Appointment) => {
    if (!window.confirm(`Cancelar ${a.procedureName} em ${a.storeName}?`)) return;
    setCancelling(a.id);
    try {
      const updated = await api.cancelAppointment(a.id, token);
      setItems((list) => list?.map((i) => (i.id === a.id ? { ...i, ...(updated ?? {}), status: 'CANCELLED' } : i)) ?? list);
      notify({ type: 'success', text: 'Agendamento cancelado.' });
    } catch (err) {
      const unavailable = err instanceof ApiError && (err.status === 404 || err.status === 405);
      notify({ type: 'error', text: unavailable ? 'O cancelamento ainda não está disponível.' : err instanceof Error ? err.message : 'Erro ao cancelar.' });
    } finally {
      setCancelling(null);
    }
  };

  const sorted = useMemo(
    () => [...(items ?? [])].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()),
    [items]
  );

  return (
    <>
      <h1 className="page-title">Meus agendamentos</h1>

      {fallback && (
        <div className="alert alert-info">
          Exibindo apenas os agendamentos feitos nesta sessão. O histórico completo estará disponível em breve.
        </div>
      )}

      {error && (
        <div className="state-box">
          <p>{error}</p>
          <button className="btn btn-secondary" onClick={load}>Tentar novamente</button>
        </div>
      )}

      {!error && items === null && <div className="list"><div className="skeleton skeleton-row" /><div className="skeleton skeleton-row" /></div>}

      {items && sorted.length === 0 && (
        <div className="state-box">
          <p>Você ainda não tem agendamentos.</p>
          <button className="btn btn-primary" onClick={onExplore}>Explorar estabelecimentos</button>
        </div>
      )}

      {sorted.length > 0 && (
        <div className="list">
          {sorted.map((a) => {
            const canCancel = a.status === 'PENDING' || a.status === 'CONFIRMED';
            return (
              <article key={a.id} className="appointment-card">
                <div className="appointment-date">
                  <strong>{new Date(a.date).toLocaleDateString('pt-BR', { day: '2-digit' })}</strong>
                  <span>{new Date(a.date).toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')}</span>
                </div>
                <div className="appointment-info">
                  <h3>{a.procedureName}</h3>
                  <p>{a.storeName} · {formatTime(a.date)}</p>
                  {a.providerUsername && <p className="muted">Profissional: {a.providerUsername}</p>}
                </div>
                <div className="appointment-side">
                  <span className={`badge ${STATUS_CLASS[a.status]}`}>{STATUS_LABEL[a.status]}</span>
                  {canCancel && (
                    <button className="btn btn-ghost-danger btn-sm" disabled={cancelling === a.id} onClick={() => cancel(a)}>
                      {cancelling === a.id ? 'Cancelando…' : 'Cancelar'}
                    </button>
                  )}
                </div>
              </article>
            );
          })}
        </div>
      )}
    </>
  );
};

/* ------------------------------------------------------------------ */
/* App                                                                */
/* ------------------------------------------------------------------ */

const ConsumerApp: React.FC = () => {
  const [token, setToken] = useState<string | null>(() => {
    try {
      const saved = localStorage.getItem(TOKEN_KEY);
      return saved && !isTokenExpired(saved) ? saved : null;
    } catch {
      return null;
    }
  });
  const [view, setView] = useState<View>('explore');
  const [store, setStore] = useState<Store | null>(null);
  const [sessionAppointments, setSessionAppointments] = useState<Appointment[]>([]);
  const [toast, setToast] = useState<Toast>(null);

  const username = token ? parseJwt(token).sub ?? '' : '';

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 4500);
    return () => clearTimeout(t);
  }, [toast]);

  const handleLogin = (t: string) => {
    try { localStorage.setItem(TOKEN_KEY, t); } catch { /* storage indisponível */ }
    setToken(t);
    setView('explore');
  };

  const handleLogout = useCallback(() => {
    try { localStorage.removeItem(TOKEN_KEY); } catch { /* noop */ }
    setToken(null);
    setStore(null);
    setSessionAppointments([]);
    setView('explore');
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(handleLogout);
  }, [handleLogout]);

  const goExplore = () => { setStore(null); setView('explore'); };

  if (!token) {
    return (
      <div className="app-container">
        <AuthScreen onLogin={handleLogin} notify={setToast} />
        <ToastView toast={toast} />
      </div>
    );
  }

  return (
    <div className="app-container">
      <header className="navbar">
        <div className="navbar-inner">
          <button className="navbar-brand" onClick={goExplore}>
            <span className="brand-logo-small">D</span>
            <span>DexTime</span>
          </button>
          <nav className="navbar-links">
            <button className={view !== 'appointments' ? 'active' : ''} onClick={goExplore}>Explorar</button>
            <button className={view === 'appointments' ? 'active' : ''} onClick={() => setView('appointments')}>Meus agendamentos</button>
          </nav>
          <div className="navbar-user">
            <span className="user-chip" title={username}>
              <span className="user-avatar">{username.slice(0, 1).toUpperCase()}</span>
              <span className="user-name">{username}</span>
            </span>
            <button className="btn btn-secondary btn-sm" onClick={handleLogout}>Sair</button>
          </div>
        </div>
      </header>

      <main className="page">
        {view === 'explore' && (
          <ExploreView token={token} onSelect={(s) => { setStore(s); setView('store'); }} />
        )}
        {view === 'store' && store && (
          <StoreView
            token={token}
            store={store}
            onBack={goExplore}
            notify={setToast}
            onBooked={(a) => {
              setSessionAppointments((list) => [...list, a]);
              setView('appointments');
            }}
          />
        )}
        {view === 'appointments' && (
          <AppointmentsView token={token} sessionAppointments={sessionAppointments} onExplore={goExplore} notify={setToast} />
        )}
      </main>

      <ToastView toast={toast} />
    </div>
  );
};

const ToastView: React.FC<{ toast: Toast }> = ({ toast }) =>
  toast ? <div className={`toast toast-${toast.type}`} role="status">{toast.text}</div> : null;

/** /manager => painel do gestor; /provider => painel do profissional; demais rotas => app do cliente */
const App: React.FC = () => {
  const path = window.location.pathname;
  if (path.startsWith('/manager')) return <ManagerApp />;
  if (path.startsWith('/provider')) return <ProviderApp />;
  return <ConsumerApp />;
};

export default App;