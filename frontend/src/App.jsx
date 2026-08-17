import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { api, formatDate, formatMoney } from './api'
import './App.css'

const demoAccounts = [
  { label: 'Organizador', email: 'organizador@demo.com', role: 'ADMIN' },
  { label: 'Cliente', email: 'cliente1@demo.com', role: 'CUSTOMER' },
  { label: 'Portaria', email: 'portaria@demo.com', role: 'GATE' },
]

function readSession() {
  try {
    return JSON.parse(localStorage.getItem('elite-session'))
  } catch {
    return null
  }
}

function App() {
  const [session, setSession] = useState(readSession)
  const [view, setView] = useState('events')
  const [notice, setNotice] = useState(null)
  const sharedToken = useMemo(() => {
    const marker = '/tickets/shared/'
    const index = window.location.pathname.indexOf(marker)
    return index >= 0 ? window.location.pathname.slice(index + marker.length) : null
  }, [])

  function authenticate(nextSession) {
    localStorage.setItem('elite-session', JSON.stringify(nextSession))
    setSession(nextSession)
    setView(nextSession.role === 'ADMIN' ? 'organizer' : nextSession.role === 'GATE' ? 'gate' : 'events')
  }

  function logout() {
    localStorage.removeItem('elite-session')
    setSession(null)
    setView('events')
  }

  const flash = useCallback((message, tone = 'success') => {
    setNotice({ message, tone })
    window.setTimeout(() => setNotice(null), 3800)
  }, [])

  if (sharedToken) return <SharedTicketPage token={sharedToken} />

  return (
    <div className="app-shell">
      <Header session={session} view={view} setView={setView} logout={logout} />
      {notice && <div className={`notice ${notice.tone}`}>{notice.message}</div>}
      <main>
        {view === 'events' && <EventsPage session={session} setView={setView} flash={flash} />}
        {view === 'login' && <LoginPage authenticate={authenticate} flash={flash} />}
        {view === 'tickets' && session?.role === 'CUSTOMER' && <CustomerArea session={session} flash={flash} />}
        {view === 'organizer' && session?.role === 'ADMIN' && <OrganizerArea session={session} flash={flash} />}
        {view === 'gate' && session?.role === 'GATE' && <GateArea session={session} flash={flash} />}
      </main>
      <footer>
        <span>ELITE/26</span>
        <p>Experiências que começam antes da luz apagar.</p>
      </footer>
    </div>
  )
}

function Header({ session, view, setView, logout }) {
  return (
    <header className="topbar">
      <button className="brand" onClick={() => setView('events')}>
        <span className="brand-mark">E</span>
        <span>ELITE<small>eventos</small></span>
      </button>
      <nav>
        <button className={view === 'events' ? 'active' : ''} onClick={() => setView('events')}>Agenda</button>
        {session?.role === 'CUSTOMER' && <button className={view === 'tickets' ? 'active' : ''} onClick={() => setView('tickets')}>Meus ingressos</button>}
        {session?.role === 'ADMIN' && <button className={view === 'organizer' ? 'active' : ''} onClick={() => setView('organizer')}>Produção</button>}
        {session?.role === 'GATE' && <button className={view === 'gate' ? 'active' : ''} onClick={() => setView('gate')}>Portaria</button>}
      </nav>
      {session ? (
        <div className="session-pill">
          <span><b>{session.name}</b><small>{roleLabel(session.role)}</small></span>
          <button onClick={logout}>Sair</button>
        </div>
      ) : <button className="primary compact" onClick={() => setView('login')}>Entrar</button>}
    </header>
  )
}

function LoginPage({ authenticate, flash }) {
  const [form, setForm] = useState({ email: 'cliente1@demo.com', password: '123456' })
  const [busy, setBusy] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    try {
      const data = await api('/auth/login', { method: 'POST', body: form })
      authenticate(data)
      flash(`Bem-vindo, ${data.name}.`)
    } catch (error) {
      flash(error.message, 'error')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="login-layout">
      <div className="login-copy">
        <span className="eyebrow">ACESSO À PLATAFORMA</span>
        <h1>Seu próximo evento começa aqui.</h1>
        <p>Entre como cliente, produção ou portaria. O mesmo palco, três experiências precisas.</p>
        <div className="demo-list">
          {demoAccounts.map((account) => (
            <button key={account.email} onClick={() => setForm({ email: account.email, password: '123456' })}>
              <span>{account.label}</span><small>{account.email}</small>
            </button>
          ))}
        </div>
      </div>
      <form className="panel login-card" onSubmit={submit}>
        <span className="step-number">01 / LOGIN</span>
        <label>E-mail<input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required /></label>
        <label>Senha<input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required /></label>
        <button className="primary" disabled={busy}>{busy ? 'Entrando…' : 'Acessar plataforma'}</button>
        <p className="hint">Todos os usuários demo usam a senha <b>123456</b>.</p>
      </form>
    </section>
  )
}

function EventsPage({ session, setView, flash }) {
  const [events, setEvents] = useState([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [selected, setSelected] = useState(null)

  const load = useCallback(async (search = '') => {
    setLoading(true)
    try {
      setEvents(await api(`/events${search ? `?query=${encodeURIComponent(search)}` : ''}`))
    } catch (error) {
      flash(error.message, 'error')
    } finally {
      setLoading(false)
    }
  }, [flash])

  useEffect(() => {
    const timer = window.setTimeout(() => load(), 0)
    return () => window.clearTimeout(timer)
  }, [load])

  function reserve(event) {
    if (!session) return setView('login')
    if (session.role !== 'CUSTOMER') return flash('Entre com um perfil de cliente para reservar.', 'error')
    setSelected(event)
  }

  return (
    <>
      <section className="hero-section">
        <div>
          <span className="eyebrow">CURADORIA ELITE — TEMPORADA 2026</span>
          <h1>Histórias para viver <em>fora da tela.</em></h1>
          <p>Escolha a sessão, garanta seu lugar e leve o ingresso no bolso.</p>
        </div>
        <div className="hero-index"><strong>{String(events.length).padStart(2, '0')}</strong><span>eventos<br />em cartaz</span></div>
      </section>
      <section className="catalog-section">
        <div className="section-heading">
          <div><span className="eyebrow">AGENDA</span><h2>Em cartaz</h2></div>
          <form className="search" onSubmit={(e) => { e.preventDefault(); load(query) }}>
            <input aria-label="Buscar eventos" placeholder="Busque por título ou local" value={query} onChange={(e) => setQuery(e.target.value)} />
            <button>Buscar</button>
          </form>
        </div>
        {loading ? <Loading /> : events.length === 0 ? <Empty title="Nenhum evento encontrado" text="Tente buscar outro título ou volte em breve." /> : (
          <div className="event-grid">
            {events.map((event, index) => <EventCard key={event.id} event={event} index={index} onReserve={() => reserve(event)} />)}
          </div>
        )}
      </section>
      {selected && <CheckoutModal event={selected} session={session} close={() => setSelected(null)} done={() => { setSelected(null); load(query); flash('Pagamento aprovado. Seus ingressos já estão disponíveis.'); setView('tickets') }} flash={flash} />}
    </>
  )
}

function EventCard({ event, index, onReserve }) {
  return (
    <article className="event-card">
      <div className="poster">
        {event.imageUrl ? <img src={event.imageUrl} alt={`Pôster de ${event.title}`} /> : <div className="poster-fallback">ELITE/{String(index + 1).padStart(2, '0')}</div>}
        <span className="availability">{event.availableQuantity} lugares</span>
      </div>
      <div className="event-body">
        <span className="date-line">{formatDate(event.eventDateTime)}</span>
        <h3>{event.title}</h3>
        <p>{event.location}</p>
        <div className="event-action"><strong>{formatMoney(event.price)}</strong><button onClick={onReserve} disabled={!event.availableQuantity}>Reservar <span>↗</span></button></div>
      </div>
    </article>
  )
}

function CheckoutModal({ event, session, close, done, flash }) {
  const [quantity, setQuantity] = useState(1)
  const [reservation, setReservation] = useState(null)
  const [busy, setBusy] = useState(false)

  async function createReservation() {
    setBusy(true)
    try {
      setReservation(await api('/reservations', { method: 'POST', token: session.token, body: { eventId: event.id, quantity } }))
    } catch (error) {
      flash(error.message, 'error')
    } finally { setBusy(false) }
  }

  async function pay(outcome) {
    setBusy(true)
    try {
      const payment = await api('/payments', { method: 'POST', token: session.token, body: { reservationId: reservation.id, outcome } })
      if (payment.status === 'APPROVED') done()
      else { flash('Pagamento recusado na simulação. Você pode tentar novamente.', 'error'); setReservation({ ...reservation, paymentDeclined: true }) }
    } catch (error) { flash(error.message, 'error') } finally { setBusy(false) }
  }

  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && close()}>
      <section className="modal panel">
        <button className="modal-close" onClick={close}>×</button>
        <span className="eyebrow">CHECKOUT SEGURO</span><h2>{event.title}</h2><p>{formatDate(event.eventDateTime)} · {event.location}</p>
        {!reservation ? <>
          <div className="quantity-row"><span>Quantidade</span><div><button onClick={() => setQuantity(Math.max(1, quantity - 1))}>−</button><b>{quantity}</b><button onClick={() => setQuantity(Math.min(event.availableQuantity, quantity + 1))}>+</button></div></div>
          <div className="total"><span>Total</span><strong>{formatMoney(Number(event.price) * quantity)}</strong></div>
          <button className="primary" disabled={busy} onClick={createReservation}>{busy ? 'Reservando…' : 'Continuar para pagamento'}</button>
        </> : <>
          <div className="payment-sim"><span className="step-number">SIMULAÇÃO</span><p>Escolha o resultado para demonstrar os dois fluxos exigidos.</p>
            <button className="primary" disabled={busy} onClick={() => pay('APPROVED')}>Aprovar pagamento</button>
            <button className="secondary" disabled={busy} onClick={() => pay('DECLINED')}>Simular recusa</button>
          </div>
        </>}
      </section>
    </div>
  )
}

function CustomerArea({ session, flash }) {
  const [tickets, setTickets] = useState([])
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [ticketData, reservationData] = await Promise.all([
        api('/tickets/me', { token: session.token }),
        api('/reservations/me', { token: session.token }),
      ])
      setTickets(ticketData)
      setReservations(reservationData)
    } catch (error) { flash(error.message, 'error') } finally { setLoading(false) }
  }, [flash, session.token])

  useEffect(() => {
    const timer = window.setTimeout(load, 0)
    return () => window.clearTimeout(timer)
  }, [load])

  async function cancel(id) {
    try {
      await api(`/reservations/${id}/cancel`, { method: 'PATCH', token: session.token })
      flash('Reserva cancelada e lugares devolvidos ao evento.')
      load()
    } catch (error) { flash(error.message, 'error') }
  }

  return (
    <section className="workspace">
      <div className="workspace-title"><span className="eyebrow">CARTEIRA DIGITAL</span><h1>Meus ingressos</h1><p>Apresente o QR na entrada ou compartilhe o link com quem vai usar.</p></div>
      {loading ? <Loading /> : tickets.length === 0 ? <Empty title="Sua carteira está vazia" text="Escolha um evento e conclua o pagamento para receber os ingressos." /> : (
        <div className="ticket-grid">{tickets.map((ticket) => <TicketCard key={ticket.id} ticket={ticket} session={session} flash={flash} />)}</div>
      )}
      <div className="subsection"><div className="section-heading"><div><span className="eyebrow">HISTÓRICO</span><h2>Reservas</h2></div></div>
        <div className="list-panel">{reservations.map((item) => <div className="list-row" key={item.id}><div><b>{item.eventTitle}</b><small>{item.quantity} ingresso(s) · {formatMoney(item.totalPrice)}</small></div><Status value={item.status} />{item.status === 'PENDING_PAYMENT' && <button className="text-button" onClick={() => cancel(item.id)}>Cancelar</button>}</div>)}</div>
      </div>
    </section>
  )
}

function TicketCard({ ticket, session, flash }) {
  const [qrUrl, setQrUrl] = useState(null)
  useEffect(() => {
    let url
    api(`/tickets/${ticket.id}/qr`, { token: session.token }).then((blob) => { url = URL.createObjectURL(blob); setQrUrl(url) }).catch(() => {})
    return () => url && URL.revokeObjectURL(url)
  }, [ticket.id, session.token])

  async function copyLink() {
    await navigator.clipboard.writeText(ticket.shareUrl)
    flash('Link do ingresso copiado.')
  }

  return (
    <article className="ticket-card">
      <div className="ticket-main"><span className="eyebrow">INGRESSO #{ticket.sequenceNumber}/{ticket.reservationQuantity}</span><h3>{ticket.eventTitle}</h3><p>{formatDate(ticket.eventDateTime)}</p><p>{ticket.eventLocation}</p><Status value={ticket.status} /></div>
      <div className="ticket-stub">{qrUrl ? <img src={qrUrl} alt="QR Code do ingresso" /> : <div className="qr-loading">Gerando QR</div>}<button onClick={copyLink}>Copiar link</button></div>
    </article>
  )
}

function OrganizerArea({ session, flash }) {
  const emptyForm = { externalSource: 'TMDB', externalId: '', title: '', description: '', imageUrl: '', eventDateTime: '', location: '', capacity: 80, price: 35 }
  const [events, setEvents] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [catalogQuery, setCatalogQuery] = useState('Matrix')
  const [catalog, setCatalog] = useState([])
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    try { setEvents(await api('/events/organizer/me', { token: session.token })) } catch (error) { flash(error.message, 'error') }
  }, [flash, session.token])
  useEffect(() => {
    const timer = window.setTimeout(load, 0)
    return () => window.clearTimeout(timer)
  }, [load])

  async function searchCatalog(event) {
    event.preventDefault(); setBusy(true)
    try { setCatalog((await api(`/catalog/movies?query=${encodeURIComponent(catalogQuery)}`, { token: session.token })).results) } catch (error) { flash(error.message, 'error') } finally { setBusy(false) }
  }

  function selectMovie(movie) {
    setForm({ ...form, externalSource: 'TMDB', externalId: String(movie.id), title: movie.title, description: movie.description || '', imageUrl: movie.imageUrl || '' })
    flash(`${movie.title} adicionado ao formulário.`)
  }

  async function create(event) {
    event.preventDefault(); setBusy(true)
    try {
      await api('/events', { method: 'POST', token: session.token, body: { ...form, capacity: Number(form.capacity), price: Number(form.price), externalId: form.externalId || null } })
      setForm(emptyForm); setCatalog([]); await load(); flash('Evento criado como rascunho.')
    } catch (error) { flash(error.message, 'error') } finally { setBusy(false) }
  }

  async function changeStatus(id, action) {
    try { await api(`/events/${id}/${action}`, { method: 'PATCH', token: session.token }); await load(); flash(action === 'publish' ? 'Evento publicado.' : 'Evento cancelado.') } catch (error) { flash(error.message, 'error') }
  }

  return (
    <section className="workspace organizer-layout">
      <div className="workspace-title"><span className="eyebrow">MESA DE PRODUÇÃO</span><h1>Crie a próxima sessão.</h1><p>Comece pelo catálogo TMDb e defina quando, onde e para quantas pessoas.</p></div>
      <div className="organizer-columns">
        <div className="panel catalog-panel"><span className="step-number">01 / CATÁLOGO</span><form className="search" onSubmit={searchCatalog}><input value={catalogQuery} onChange={(e) => setCatalogQuery(e.target.value)} placeholder="Buscar filme" /><button>{busy ? '…' : 'Buscar'}</button></form><div className="catalog-results">{catalog.map((movie) => <button key={movie.id} onClick={() => selectMovie(movie)}>{movie.imageUrl && <img src={movie.imageUrl} alt="" />}<span><b>{movie.title}</b><small>{movie.releaseDate || 'Sem data'}</small></span></button>)}</div></div>
        <form className="panel event-form" onSubmit={create}><span className="step-number">02 / EVENTO</span>
          <label>Título<input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></label>
          <label>Descrição<textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label>
          <div className="field-pair"><label>Data e hora<input type="datetime-local" value={form.eventDateTime} onChange={(e) => setForm({ ...form, eventDateTime: e.target.value })} required /></label><label>Local<input value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} required /></label></div>
          <div className="field-pair"><label>Capacidade<input type="number" min="1" value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} required /></label><label>Preço<input type="number" min="0" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} required /></label></div>
          <button className="primary" disabled={busy}>Criar rascunho</button>
        </form>
      </div>
      <div className="subsection"><div className="section-heading"><div><span className="eyebrow">SEUS EVENTOS</span><h2>Programação</h2></div></div><div className="list-panel">{events.map((item) => <div className="list-row organizer-row" key={item.id}><div><b>{item.title}</b><small>{formatDate(item.eventDateTime)} · {item.availableQuantity}/{item.capacity} disponíveis</small></div><Status value={item.status} /><div>{item.status === 'DRAFT' && <button className="text-button" onClick={() => changeStatus(item.id, 'publish')}>Publicar</button>}{item.status !== 'CANCELLED' && <button className="text-button danger" onClick={() => changeStatus(item.id, 'cancel')}>Cancelar</button>}</div></div>)}</div></div>
    </section>
  )
}

function GateArea({ session, flash }) {
  const [events, setEvents] = useState([])
  const [eventId, setEventId] = useState('')
  const [token, setToken] = useState('')
  const [result, setResult] = useState(null)
  const [scanning, setScanning] = useState(false)
  const videoRef = useRef(null)
  const streamRef = useRef(null)
  const scanTimer = useRef(null)

  useEffect(() => {
    api('/events').then((data) => { setEvents(data); if (data[0]) setEventId(String(data[0].id)) }).catch((error) => flash(error.message, 'error'))
    return () => {
      if (scanTimer.current) window.clearInterval(scanTimer.current)
      streamRef.current?.getTracks().forEach((track) => track.stop())
    }
  }, [flash])

  async function validate(value = token) {
    if (!value || !eventId) return flash('Informe o evento e o código do ingresso.', 'error')
    try { setResult(await api('/gate/validate', { method: 'POST', token: session.token, body: { token: value.trim(), eventId: Number(eventId) } })) } catch (error) { flash(error.message, 'error') }
  }

  async function startCamera() {
    if (!('BarcodeDetector' in window)) return flash('Leitura nativa de QR não disponível neste navegador. Use a digitação manual.', 'error')
    try {
      streamRef.current = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
      videoRef.current.srcObject = streamRef.current; await videoRef.current.play(); setScanning(true)
      const detector = new window.BarcodeDetector({ formats: ['qr_code'] })
      scanTimer.current = window.setInterval(async () => {
        try { const codes = await detector.detect(videoRef.current); if (codes[0]?.rawValue) { setToken(codes[0].rawValue); stopCamera(); validate(codes[0].rawValue) } } catch { return }
      }, 550)
    } catch { flash('Não foi possível acessar a câmera. Use a alternativa manual.', 'error') }
  }

  function stopCamera() {
    if (scanTimer.current) window.clearInterval(scanTimer.current)
    streamRef.current?.getTracks().forEach((track) => track.stop())
    streamRef.current = null; setScanning(false)
  }

  return (
    <section className="workspace gate-workspace">
      <div className="workspace-title"><span className="eyebrow">CONTROLE DE ACESSO</span><h1>Portaria</h1><p>Leia o QR pela câmera ou digite o código. Cada ingresso libera uma única entrada.</p></div>
      <div className="gate-grid">
        <div className="panel scanner-panel"><span className="step-number">01 / EVENTO</span><label>Evento da entrada<select value={eventId} onChange={(e) => { setEventId(e.target.value); setResult(null) }}>{events.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}</select></label><div className={`camera ${scanning ? 'live' : ''}`}><video ref={videoRef} muted playsInline /><div className="scan-frame"><i></i><span>{scanning ? 'Aponte para o QR' : 'Câmera pronta'}</span></div></div><button className="primary" onClick={scanning ? stopCamera : startCamera}>{scanning ? 'Parar câmera' : 'Ler QR pela câmera'}</button></div>
        <div className="panel manual-panel"><span className="step-number">02 / CÓDIGO MANUAL</span><label>Token do ingresso<textarea value={token} onChange={(e) => setToken(e.target.value)} placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.assinatura" /></label><button className="secondary" onClick={() => validate()}>Validar código</button>{result && <ValidationResult result={result} />}</div>
      </div>
    </section>
  )
}

function ValidationResult({ result }) {
  const labels = { VALID: 'Entrada liberada', INVALID: 'Ingresso inválido', ALREADY_USED: 'Já utilizado', WRONG_EVENT: 'Evento incorreto' }
  return <div className={`validation-result ${result.result.toLowerCase()}`}><span>{result.result === 'VALID' ? '✓' : '!'}</span><div><small>{result.result}</small><h2>{labels[result.result]}</h2><p>{result.message}</p>{result.eventTitle && <b>{result.eventTitle} · ingresso #{result.sequenceNumber}</b>}</div></div>
}

function SharedTicketPage({ token }) {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  useEffect(() => { api(`/tickets/shared/${encodeURIComponent(token)}`).then(setData).catch((e) => setError(e.message)) }, [token])
  return <main className="shared-page"><div className="brand static"><span className="brand-mark">E</span><span>ELITE<small>eventos</small></span></div>{error ? <ValidationResult result={{ result: 'INVALID', message: error }} /> : !data ? <Loading /> : <article className="shared-ticket"><span className="eyebrow">INGRESSO COMPARTILHADO</span><h1>{data.eventTitle}</h1><p>{formatDate(data.eventDateTime)}</p><p>{data.eventLocation}</p><div className="ticket-number">#{data.sequenceNumber}/{data.reservationQuantity}</div><Status value={data.status} /></article>}</main>
}

function Status({ value }) {
  const labels = { PENDING_PAYMENT: 'Aguardando pagamento', CONFIRMED: 'Confirmada', CANCELLED: 'Cancelada', ACTIVE: 'Válido', USED: 'Utilizado', DRAFT: 'Rascunho', PUBLISHED: 'Publicado', APPROVED: 'Aprovado', DECLINED: 'Recusado' }
  return <span className={`status ${value?.toLowerCase()}`}>{labels[value] || value}</span>
}

function Loading() { return <div className="loading"><i></i><span>Preparando experiência…</span></div> }
function Empty({ title, text }) { return <div className="empty"><span>○</span><h3>{title}</h3><p>{text}</p></div> }
function roleLabel(role) { return { ADMIN: 'Produção', CUSTOMER: 'Cliente', GATE: 'Portaria' }[role] || role }

export default App
