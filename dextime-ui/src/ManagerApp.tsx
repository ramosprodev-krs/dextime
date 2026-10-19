import React, { useCallback, useEffect, useMemo, useState } from 'react';
import './manager.css';
import { api, ApiError, setUnauthorizedHandler } from './services/api';
import EmptyState from './EmptyState';
import type { Appointment, Procedure, Provider, Store, UserDto } from '../types';
import {
  checkPanelAccess,
  getRoles,
  formatDateTime,
  formatPrice,
  hueFromString,
  initials,
  isTokenExpired,
  maskCnpj,
  maskCpf,
  maskPhone,
  parseJwt,
  STATUS_CLASS,
  STATUS_LABEL,
  toApiDate,
} from './utils';

const TOKEN_KEY = 'dextime:manager-token:v2';
type Toast = { type: 'success' | 'error'; text: string } | null;

const errMsg = (e: unknown, fallback = 'Algo deu errado.') => (e instanceof ApiError ? e.message : fallback);
const emptyUser: UserDto = { username: '', password: '', firstName: '', lastName: '', email: '', phoneNumber: '', cpf: '' };

/* ------------------------------------------------------------------ */
/* Login                                                              */
/* ------------------------------------------------------------------ */

const UserFields: React.FC<{ form: UserDto; set: (f: keyof UserDto, v: string) => void }> = ({ form, set }) => (
  <>
    <div className="form-row">
      <div className="form-group"><label>Nome</label><input value={form.firstName} onChange={(e) => set('firstName', e.target.value)} required /></div>
      <div className="form-group"><label>Sobrenome</label><input value={form.lastName} onChange={(e) => set('lastName', e.target.value)} required /></div>
    </div>
    <div className="form-row">
      <div className="form-group"><label>Usuário</label><input value={form.username} onChange={(e) => set('username', e.target.value)} required /></div>
      <div className="form-group"><label>Senha</label><input type="password" autoComplete="new-password" value={form.password} onChange={(e) => set('password', e.target.value)} required /></div>
    </div>
    <div className="form-group"><label>E-mail</label><input type="email" value={form.email} onChange={(e) => set('email', e.target.value)} required /></div>
    <div className="form-row">
      <div className="form-group"><label>Telefone</label><input placeholder="+55 11999999999" value={form.phoneNumber} onChange={(e) => set('phoneNumber', maskPhone(e.target.value))} required /></div>
      <div className="form-group"><label>CPF</label><input placeholder="000.000.000-00" value={form.cpf} onChange={(e) => set('cpf', maskCpf(e.target.value))} required /></div>
    </div>
  </>
);

const ManagerLogin: React.FC<{ onLogin: (t: string) => void; notify: (t: Toast) => void }> = ({ onLogin, notify }) => {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [form, setForm] = useState({ username: '', password: '' });
  const [reg, setReg] = useState<UserDto>(emptyUser);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const setR = (f: keyof UserDto, v: string) => setReg((s) => ({ ...s, [f]: v }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await api.login(form);
      const denied = checkPanelAccess(getRoles(res.token, res.role), 'MANAGER');
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

  const register = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await api.registerManager(reg);
      setForm({ username: reg.username, password: '' });
      setReg(emptyUser);
      setMode('login');
      notify({ type: 'success', text: 'Conta criada! Faça login para continuar.' });
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
          <h2>DexTime Business</h2>
          <p>{mode === 'login' ? 'Painel para donos de estabelecimentos' : 'Cadastre o seu estabelecimento'}</p>
        </div>
        {error && <div className="alert alert-danger" role="alert">{error}</div>}
        {mode === 'login' ? (
          <form onSubmit={submit} className="auth-form">
            <div className="form-group">
              <label htmlFor="m-user">Usuário</label>
              <input id="m-user" value={form.username} autoComplete="username"
                onChange={(e) => setForm({ ...form, username: e.target.value })} required />
            </div>
            <div className="form-group">
              <label htmlFor="m-pass">Senha</label>
              <input id="m-pass" type="password" value={form.password} autoComplete="current-password"
                onChange={(e) => setForm({ ...form, password: e.target.value })} required />
            </div>
            <button className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Entrando…' : 'Entrar'}</button>
            <p className="auth-switch">
              Ainda não tem conta?{' '}
              <button type="button" className="link-btn" onClick={() => { setMode('register'); setError(null); }}>Cadastre-se</button>
            </p>
            <p className="auth-switch">É cliente? <a className="link-btn" href="/">Ir para o app do cliente</a></p>
          </form>
        ) : (
          <form onSubmit={register} className="auth-form">
            <UserFields form={reg} set={setR} />
            <button className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Criando conta…' : 'Criar conta'}</button>
            <p className="auth-switch">
              Já tem conta?{' '}
              <button type="button" className="link-btn" onClick={() => { setMode('login'); setError(null); }}>Entrar</button>
            </p>
          </form>
        )}
      </div>
    </div>
  );
};

/* ------------------------------------------------------------------ */
/* Seletor de datas (chips)                                           */
/* ------------------------------------------------------------------ */

const DatesPicker: React.FC<{ dates: string[]; onChange: (d: string[]) => void }> = ({ dates, onChange }) => {
  const [value, setValue] = useState('');

  const add = () => {
    if (!value) return;
    const iso = toApiDate(value);
    if (!dates.includes(iso)) onChange([...dates, iso].sort());
    setValue('');
  };

  return (
    <div className="dates-picker">
      <div className="dates-input">
        <input type="datetime-local" value={value} onChange={(e) => setValue(e.target.value)}
          min={new Date().toISOString().slice(0, 16)} aria-label="Novo horário" />
        <button type="button" className="btn btn-secondary btn-sm" onClick={add} disabled={!value}>Adicionar</button>
      </div>
      {dates.length > 0 && (
        <div className="chips">
          {dates.map((d) => (
            <span key={d} className="chip chip-removable">
              {formatDateTime(d)}
              <button type="button" aria-label="Remover horário" onClick={() => onChange(dates.filter((x) => x !== d))}>✕</button>
            </span>
          ))}
        </div>
      )}
    </div>
  );
};

/* ------------------------------------------------------------------ */
/* Serviços de uma loja                                               */
/* ------------------------------------------------------------------ */

const ServicesTab: React.FC<{ token: string; store: Store; notify: (t: Toast) => void }> = ({ token, store, notify }) => {
  const [procedures, setProcedures] = useState<Procedure[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [dates, setDates] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);

  const [addingTo, setAddingTo] = useState<string | null>(null);
  const [extraDates, setExtraDates] = useState<string[]>([]);

  const load = useCallback(() => {
    setProcedures(null);
    setError(null);
    api.getProcedures(store.id, token)
      .then(setProcedures)
      .catch((e) => {
        // 404 = loja sem serviços; outros erros (ex.: 500) são falhas reais e aparecem como erro
        if (e instanceof ApiError && e.status === 404) setProcedures([]);
        else setError(errMsg(e));
      });
  }, [store.id, token]);

  useEffect(load, [load]);

  const create = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const created = await api.createProcedure(
        { name, description, price: Number(price), storeId: store.id, availableDates: dates }, token
      );
      setProcedures((list) => [...(list ?? []), created]);
      setName(''); setDescription(''); setPrice(''); setDates([]);
      notify({ type: 'success', text: 'Serviço criado!' });
    } catch (err) {
      notify({ type: 'error', text: errMsg(err) });
    } finally {
      setSaving(false);
    }
  };

  const saveDates = async (p: Procedure) => {
    if (extraDates.length === 0) return;
    try {
      const updated = await api.addProcedureDates(p.id, extraDates, token);
      setProcedures((list) => list?.map((i) => (i.id === p.id ? updated : i)) ?? list);
      setAddingTo(null);
      setExtraDates([]);
      notify({ type: 'success', text: 'Horários adicionados.' });
    } catch (err) {
      notify({ type: 'error', text: errMsg(err) });
    }
  };

  return (
    <div className="two-col">
      <section className="panel">
        <h3>Novo serviço</h3>
        <form className="auth-form" onSubmit={create}>
          <div className="form-group">
            <label htmlFor="p-name">Nome</label>
            <input id="p-name" value={name} onChange={(e) => setName(e.target.value)} required />
          </div>
          <div className="form-group">
            <label htmlFor="p-desc">Descrição</label>
            <textarea id="p-desc" rows={3} value={description} onChange={(e) => setDescription(e.target.value)} required />
          </div>
          <div className="form-group">
            <label htmlFor="p-price">Preço (R$)</label>
            <input id="p-price" type="number" min="0" step="0.01" value={price} onChange={(e) => setPrice(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Horários disponíveis</label>
            <DatesPicker dates={dates} onChange={setDates} />
          </div>
          <button className="btn btn-primary" disabled={saving}>{saving ? 'Salvando…' : 'Criar serviço'}</button>
        </form>
      </section>

      <section>
        <h3 className="section-title">Serviços cadastrados</h3>
        {error && <div className="state-box"><p>{error}</p><button className="btn btn-secondary" onClick={load}>Tentar novamente</button></div>}
        {!error && procedures === null && <div className="skeleton skeleton-row" />}
        {procedures && procedures.length === 0 && (
          <EmptyState
            icon="✂️"
            title="Nenhum serviço cadastrado ainda"
            text="Crie o primeiro serviço no formulário ao lado, com preço e horários, e ele aparecerá aqui para os seus clientes."
          />
        )}
        <div className="list">
          {procedures?.map((p) => (
            <article key={p.id} className="panel">
              <div className="panel-row">
                <div>
                  <h4>{p.name}</h4>
                  <p className="muted">{p.description}</p>
                </div>
                <strong>{formatPrice(p.price)}</strong>
              </div>
              <p className="muted small">{p.availableDates.length} horário(s) disponível(is)</p>
              {addingTo === p.id ? (
                <div className="inline-add">
                  <DatesPicker dates={extraDates} onChange={setExtraDates} />
                  <div className="inline-actions">
                    <button className="btn btn-primary btn-sm" disabled={extraDates.length === 0} onClick={() => saveDates(p)}>Salvar</button>
                    <button className="btn btn-secondary btn-sm" onClick={() => { setAddingTo(null); setExtraDates([]); }}>Cancelar</button>
                  </div>
                </div>
              ) : (
                <button className="btn btn-secondary btn-sm" onClick={() => { setAddingTo(p.id); setExtraDates([]); }}>+ Adicionar horários</button>
              )}
            </article>
          ))}
        </div>
      </section>
    </div>
  );
};

/* ------------------------------------------------------------------ */
/* Agendamentos de uma loja                                           */
/* ------------------------------------------------------------------ */

const AppointmentsTab: React.FC<{ token: string; store: Store }> = ({ token, store }) => {
  const [items, setItems] = useState<Appointment[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    setItems(null);
    setError(null);
    api.getStoreAppointments(store.id, token).then(setItems).catch((e) => setError(errMsg(e)));
  }, [store.id, token]);

  useEffect(load, [load]);

  const sorted = useMemo(
    () => [...(items ?? [])].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()),
    [items]
  );

  if (error) return <div className="state-box"><p>{error}</p><button className="btn btn-secondary" onClick={load}>Tentar novamente</button></div>;
  if (items === null) return <div className="skeleton skeleton-row" />;
  if (sorted.length === 0) return <div className="state-box"><p>Nenhum agendamento para este estabelecimento.</p></div>;

  return (
    <div className="table-wrap">
      <table className="table">
        <thead><tr><th>Status</th><th>Cliente</th><th>Serviço</th><th>Data</th><th>Profissional</th></tr></thead>
        <tbody>
          {sorted.map((a) => (
            <tr key={a.id}>
              <td><span className={`badge ${STATUS_CLASS[a.status]}`}>{STATUS_LABEL[a.status]}</span></td>
              <td>{a.customerUsername}</td>
              <td>{a.procedureName}</td>
              <td>{formatDateTime(a.date)}</td>
              <td>{a.providerUsername ?? '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

/* ------------------------------------------------------------------ */
/* Estabelecimentos                                                   */
/* ------------------------------------------------------------------ */

const StoresSection: React.FC<{ token: string; username: string; notify: (t: Toast) => void }> = ({ token, username, notify }) => {
  const [stores, setStores] = useState<Store[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<Store | null>(null);
  const [tab, setTab] = useState<'services' | 'providers' | 'appointments'>('services');
  const [name, setName] = useState('');
  const [cnpj, setCnpj] = useState('');
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    setStores(null);
    setError(null);
    api.getStores(token).then(setStores).catch((e) => setError(errMsg(e)));
  }, [token]);

  useEffect(load, [load]);

  const mine = useMemo(() => (stores ?? []).filter((s) => s.managerUsername === username), [stores, username]);

  const create = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const created = await api.createStore({ name, cnpj }, token);
      setStores((list) => [...(list ?? []), created]);
      setName(''); setCnpj('');
      notify({ type: 'success', text: 'Estabelecimento criado!' });
    } catch (err) {
      notify({ type: 'error', text: errMsg(err) });
    } finally {
      setSaving(false);
    }
  };

  if (selected) {
    const hue = hueFromString(selected.name);
    return (
      <>
        <button className="link-btn back-link" onClick={() => setSelected(null)}>← Meus estabelecimentos</button>
        <header className="store-header" style={{ background: `linear-gradient(135deg, hsl(${hue} 70% 55%), hsl(${(hue + 40) % 360} 70% 38%))` }}>
          <div className="store-avatar">{initials(selected.name)}</div>
          <div><h1>{selected.name}</h1><p>CNPJ {selected.cnpj}</p></div>
        </header>
        <div className="tabs">
          <button className={tab === 'services' ? 'active' : ''} onClick={() => setTab('services')}>Serviços e horários</button>
          <button className={tab === 'providers' ? 'active' : ''} onClick={() => setTab('providers')}>Prestadores</button>
          <button className={tab === 'appointments' ? 'active' : ''} onClick={() => setTab('appointments')}>Agendamentos</button>
        </div>
        {tab === 'services' && <ServicesTab token={token} store={selected} notify={notify} />}
        {tab === 'providers' && <ProvidersTab token={token} store={selected} notify={notify} />}
        {tab === 'appointments' && <AppointmentsTab token={token} store={selected} />}
      </>
    );
  }

  return (
    <>
      <h1 className="page-title">Meus estabelecimentos</h1>
      <div className="two-col">
        <section className="panel">
          <h3>Novo estabelecimento</h3>
          <form className="auth-form" onSubmit={create}>
            <div className="form-group">
              <label htmlFor="s-name">Nome</label>
              <input id="s-name" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="form-group">
              <label htmlFor="s-cnpj">CNPJ</label>
              <input id="s-cnpj" inputMode="numeric" placeholder="00.000.000/0000-00" value={cnpj}
                onChange={(e) => setCnpj(maskCnpj(e.target.value))} required />
            </div>
            <button className="btn btn-primary" disabled={saving}>{saving ? 'Criando…' : 'Criar estabelecimento'}</button>
          </form>
        </section>

        <section>
          {error && <div className="state-box"><p>{error}</p><button className="btn btn-secondary" onClick={load}>Tentar novamente</button></div>}
          {!error && stores === null && <div className="skeleton skeleton-row" />}
          {stores && mine.length === 0 && (
            <EmptyState
              icon="🏪"
              title="Você ainda não tem estabelecimentos"
              text="Cadastre o seu primeiro estabelecimento no formulário ao lado para começar a receber agendamentos."
            />
          )}
          <div className="list">
            {mine.map((s) => {
              const hue = hueFromString(s.name);
              return (
                <button key={s.id} className="store-row" onClick={() => { setSelected(s); setTab('services'); }}>
                  <span className="store-row-avatar" style={{ background: `linear-gradient(135deg, hsl(${hue} 70% 55%), hsl(${(hue + 40) % 360} 70% 40%))` }}>{initials(s.name)}</span>
                  <span className="store-row-info"><strong>{s.name}</strong><span className="muted small">CNPJ {s.cnpj}</span></span>
                  <span className="store-cta">Gerenciar →</span>
                </button>
              );
            })}
          </div>
        </section>
      </div>
    </>
  );
};

/* ------------------------------------------------------------------ */
/* Novo manager                                                       */
/* ------------------------------------------------------------------ */

const ProvidersTab: React.FC<{ token: string; store: Store; notify: (t: Toast) => void }> = ({ token, store, notify }) => {
  const [form, setForm] = useState<UserDto>(emptyUser);
  const [saving, setSaving] = useState(false);
  const [providers, setProviders] = useState<Provider[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const set = (f: keyof UserDto, v: string) => setForm((s) => ({ ...s, [f]: v }));

  const load = useCallback(() => {
    setProviders(null);
    setError(null);
    api.getStoreProviders(store.id, token)
      .then(setProviders)
      .catch((e) => setError(errMsg(e)));
  }, [store.id, token]);

  useEffect(load, [load]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.createProvider(store.id, form, token);
      notify({ type: 'success', text: `Prestador "${form.username}" cadastrado em ${store.name}.` });
      setForm(emptyUser);
      load();
    } catch (err) {
      notify({ type: 'error', text: errMsg(err) });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="two-col">
      <section className="panel">
        <h3>Novo prestador</h3>
        <p className="muted small">Cadastre os profissionais que atendem em {store.name}. Eles entram em /provider com o usuário e a senha definidos aqui.</p>
        <form className="auth-form" onSubmit={submit}>
          <UserFields form={form} set={set} />
          <button className="btn btn-primary" disabled={saving}>{saving ? 'Cadastrando…' : 'Cadastrar prestador'}</button>
        </form>
      </section>

      <section>
        <h3 className="section-title">Prestadores da loja</h3>
        {error && <div className="state-box"><p>{error}</p><button className="btn btn-secondary" onClick={load}>Tentar novamente</button></div>}
        {!error && providers === null && <div className="skeleton skeleton-row" />}
        {providers && providers.length === 0 && (
          <EmptyState
            icon="👥"
            title="Nenhum prestador cadastrado"
            text="Cadastre o primeiro profissional no formulário ao lado para ele poder assumir os agendamentos desta loja."
          />
        )}
        {providers && providers.length > 0 && (
          <div className="list">
            {providers.map((p) => {
              const fullName = `${p.firstName} ${p.lastName}`.trim();
              const hue = hueFromString(p.username);
              return (
                <article key={p.id} className="provider-row">
                  <span className="store-row-avatar" style={{ background: `linear-gradient(135deg, hsl(${hue} 70% 55%), hsl(${(hue + 40) % 360} 70% 40%))` }}>{initials(fullName || p.username)}</span>
                  <span className="store-row-info">
                    <strong>{fullName || p.username}</strong>
                    <span className="muted small">@{p.username} · {p.email}</span>
                    <span className="muted small">{p.phoneNumber}</span>
                  </span>
                </article>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
};

/* ------------------------------------------------------------------ */
/* Shell do painel                                                    */
/* ------------------------------------------------------------------ */

const ManagerApp: React.FC = () => {
  const [token, setToken] = useState<string | null>(() => {
    try {
      const saved = localStorage.getItem(TOKEN_KEY);
      return saved && !isTokenExpired(saved) ? saved : null;
    } catch {
      return null;
    }
  });
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
  }, []);

  useEffect(() => { setUnauthorizedHandler(logout); }, [logout]);

  const login = (t: string) => {
    try { localStorage.setItem(TOKEN_KEY, t); } catch { /* noop */ }
    setToken(t);
  };

  if (!token) {
    return (
      <div className="app-container">
        <ManagerLogin onLogin={login} notify={setToast} />
        {toast && <div className={`toast toast-${toast.type}`} role="status">{toast.text}</div>}
      </div>
    );
  }

  return (
    <div className="app-container">
      <header className="navbar">
        <div className="navbar-inner">
          <span className="navbar-brand"><span className="brand-logo-small">D</span><span>DexTime Business</span></span>
          <nav className="navbar-links">
            <button className="active">Estabelecimentos</button>
          </nav>
          <div className="navbar-user">
            <span className="user-chip"><span className="user-avatar">{username.slice(0, 1).toUpperCase()}</span><span className="user-name">{username}</span></span>
            <button className="btn btn-secondary btn-sm" onClick={logout}>Sair</button>
          </div>
        </div>
      </header>
      <main className="page">
        <StoresSection token={token} username={username} notify={setToast} />
      </main>
      {toast && <div className={`toast toast-${toast.type}`} role="status">{toast.text}</div>}
    </div>
  );
};

export default ManagerApp;