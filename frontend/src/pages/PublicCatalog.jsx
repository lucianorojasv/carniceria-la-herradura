import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, money } from '../services/api';
import StorefrontHeader, { whatsappUrl } from '../components/storefront/StorefrontHeader';
import StoreLocationSection from '../components/storefront/StoreLocationSection';

const CART_KEY = 'laherradura-cart-v2';

function readCart() {
  try {
    const value = JSON.parse(localStorage.getItem(CART_KEY) || '[]');
    return Array.isArray(value) ? value : [];
  } catch {
    return [];
  }
}

function nextDateFromStatus(status) {
  return status?.nextBusinessDate || new Date(Date.now() + 86400000).toISOString().slice(0, 10);
}

function paymentOptions(business) {
  const options = [{ value: 'CASH', label: 'Efectivo', icon: '💵' }];
  if (business?.yapeEnabled) options.push({ value: 'YAPE', label: 'Yape', icon: '🟣' });
  if (business?.plinEnabled) options.push({ value: 'PLIN', label: 'Plin', icon: '🟢' });
  if (business?.transferEnabled) options.push({ value: 'TRANSFER', label: 'Transferencia', icon: '🏦' });
  return options;
}

function PaymentDetails({ business, method, total }) {
  if (!business || method === 'CASH') return <div className="payment-instructions cash"><span>💵</span><div><b>Pago en efectivo</b><p>Coordina el pago al recoger o recibir el pedido.</p></div></div>;
  if (method === 'YAPE') return <div className="payment-instructions"><div><span>🟣</span><b>Pago con Yape</b><p>Número: <strong>{business.yapeNumber || 'Por confirmar'}</strong></p><p>Titular: {business.yapeHolder || 'Por confirmar'}</p><p>Monto: <strong>{money(total)}</strong></p></div>{business.yapeQrUrl && <img src={business.yapeQrUrl} alt="Código QR de Yape" />}</div>;
  if (method === 'PLIN') return <div className="payment-instructions"><div><span>🟢</span><b>Pago con Plin</b><p>Número: <strong>{business.plinNumber || 'Por confirmar'}</strong></p><p>Titular: {business.plinHolder || 'Por confirmar'}</p><p>Monto: <strong>{money(total)}</strong></p></div>{business.plinQrUrl && <img src={business.plinQrUrl} alt="Código QR de Plin" />}</div>;
  return <div className="payment-instructions bank"><span>🏦</span><div><b>Transferencia bancaria</b><p>Banco: {business.bankName || 'Por confirmar'}</p><p>{business.bankAccountType || 'Cuenta'}: <strong>{business.bankAccountNumber || 'Por confirmar'}</strong></p>{business.bankCci && <p>CCI: <strong>{business.bankCci}</strong></p>}<p>Titular: {business.bankHolder || 'Por confirmar'}</p><p>Monto: <strong>{money(total)}</strong></p></div></div>;
}

function CartDrawer({ open, cart, onClose, onChange, onRemove, subtotal, onCheckout }) {
  return <>
    {open && <button className="cart-overlay" type="button" aria-label="Cerrar carrito" onClick={onClose} />}
    <aside className={open ? 'cart-drawer open' : 'cart-drawer'} aria-hidden={!open}>
      <div className="cart-drawer-head"><div><small>Tu pedido</small><h2>Carrito de compras</h2></div><button type="button" onClick={onClose}>×</button></div>
      <div className="cart-items">
        {cart.length === 0 && <div className="empty-cart"><span>🛒</span><b>Tu carrito está vacío</b><p>Agrega tus cortes favoritos para empezar.</p></div>}
        {cart.map(item => <article className="cart-item" key={item.id}>
          <div className="cart-item-image">{item.imageUrl ? <img src={item.imageUrl} alt={item.name} /> : '🥩'}</div>
          <div className="cart-item-info"><b>{item.name}</b><small>{money(item.pricePerUnit)} {item.unit === 'KG' ? '/ kg' : ''}</small>
            <div className="quantity-control">
              <button type="button" onClick={() => onChange(item.id, -Number(item.minimumQuantity || 1))}>−</button>
              <span>{Number(item.quantity).toFixed(item.unit === 'KG' ? 2 : 0)} {item.unit === 'KG' ? 'kg' : ''}</span>
              <button type="button" onClick={() => onChange(item.id, Number(item.minimumQuantity || 1))}>+</button>
            </div>
          </div>
          <div className="cart-item-total"><b>{money(Number(item.quantity) * Number(item.pricePerUnit))}</b><button type="button" onClick={() => onRemove(item.id)}>Quitar</button></div>
        </article>)}
      </div>
      <div className="cart-drawer-footer">
        <div><span>Subtotal</span><strong>{money(subtotal)}</strong></div>
        <small>El delivery se calcula según la zona seleccionada.</small>
        <button className="checkout-button" type="button" disabled={cart.length === 0} onClick={onCheckout}>Finalizar pedido</button>
        <button className="continue-button" type="button" onClick={onClose}>Continuar comprando</button>
      </div>
    </aside>
  </>;
}

function CheckoutModal({ data, cart, subtotal, onClose, onSuccess }) {
  const status = data.businessStatus;
  const business = data.business;
  const tomorrow = nextDateFromStatus(status);
  const reservationsEnabled = business?.allowNextDayReservations !== false;
  const slots = String(business.reservationSlots || '08:00-10:00;10:00-12:00;12:00-14:00;14:00-16:00;16:00-18:00')
    .split(/[;\n]+/).map(value => value.trim()).filter(Boolean);
  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    fulfillmentType: 'PICKUP', deliveryZoneId: '', deliveryAddress: '', deliveryReference: '',
    scheduleMode: status?.acceptsSameDay ? 'TODAY' : (reservationsEnabled ? 'RESERVATION' : 'TODAY'), scheduledDate: tomorrow,
    scheduledSlot: slots[0] || '08:00-10:00', customerName: '', customerPhone: '',
    paymentMethod: 'CASH', notes: ''
  });

  const zone = data.deliveryZones.find(item => String(item.id) === String(form.deliveryZoneId));
  const deliveryFee = form.fulfillmentType === 'DELIVERY' ? Number(zone?.fee || 0) : 0;
  const total = subtotal + deliveryFee;
  const orderingUnavailable = !status?.acceptsSameDay && !reservationsEnabled;

  function validateStep() {
    setError('');
    if (step === 1 && form.fulfillmentType === 'DELIVERY' && !form.deliveryZoneId) {
      setError('Selecciona una zona de delivery'); return false;
    }
    if (step === 1 && form.fulfillmentType === 'DELIVERY' && !form.deliveryAddress.trim()) {
      setError('Escribe la dirección de entrega'); return false;
    }
    if (step === 1 && form.scheduleMode === 'RESERVATION' && !form.scheduledSlot) {
      setError('Selecciona una franja horaria'); return false;
    }
    if (step === 2 && !form.customerName.trim()) { setError('Escribe tu nombre'); return false; }
    if (step === 2 && form.customerPhone.replace(/\D/g, '').length < 9) { setError('Escribe un celular válido'); return false; }
    return true;
  }

  function next() {
    if (validateStep()) setStep(value => Math.min(3, value + 1));
  }

  async function submit() {
    if (!validateStep()) return;
    setSaving(true);
    setError('');
    try {
      let scheduledFor = null;
      if (form.scheduleMode === 'RESERVATION') {
        const start = form.scheduledSlot.split('-')[0].trim();
        scheduledFor = `${form.scheduledDate}T${start}:00-05:00`;
      }
      const result = await api('/public/orders', {
        method: 'POST',
        body: JSON.stringify({
          customerName: form.customerName.trim(),
          customerPhone: form.customerPhone.replace(/\D/g, ''),
          fulfillmentType: form.fulfillmentType,
          deliveryZoneId: form.fulfillmentType === 'DELIVERY' ? Number(form.deliveryZoneId) : null,
          deliveryAddress: form.fulfillmentType === 'DELIVERY' ? form.deliveryAddress.trim() : null,
          deliveryReference: form.fulfillmentType === 'DELIVERY' ? form.deliveryReference.trim() : null,
          paymentMethod: form.paymentMethod,
          source: 'WEB', notes: form.notes.trim() || null, scheduledFor,
          items: cart.map(item => ({ productId: item.id, quantity: Number(item.quantity) }))
        })
      });
      onSuccess(result, form);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  }

  if (orderingUnavailable) return <div className="storefront-modal-backdrop">
    <section className="checkout-modal unavailable-ordering" role="dialog" aria-modal="true">
      <div className="checkout-head"><div><small>PEDIDOS PAUSADOS</small><h2>En este momento no podemos registrar pedidos</h2></div><button type="button" onClick={onClose}>×</button></div>
      <p>Estamos fuera del horario para pedidos de hoy y las reservas programadas están desactivadas.</p>
      <button type="button" className="checkout-button" onClick={onClose}>Volver al catálogo</button>
    </section>
  </div>;

  return <div className="storefront-modal-backdrop">
    <section className="checkout-modal" role="dialog" aria-modal="true" aria-labelledby="checkout-title">
      <div className="checkout-head"><div><small>Paso {step} de 3</small><h2 id="checkout-title">Completa tu pedido</h2></div><button type="button" onClick={onClose}>×</button></div>
      <div className="checkout-progress"><span className={step >= 1 ? 'active' : ''} /><span className={step >= 2 ? 'active' : ''} /><span className={step >= 3 ? 'active' : ''} /></div>
      {error && <div className="public-error compact">{error}</div>}

      {step === 1 && <div className="checkout-step">
        <h3>Entrega y horario</h3>
        <div className="choice-grid two">
          <label className={form.fulfillmentType === 'PICKUP' ? 'choice-card selected' : 'choice-card'}><input type="radio" name="fulfillment" value="PICKUP" checked={form.fulfillmentType === 'PICKUP'} onChange={event => setForm({ ...form, fulfillmentType: event.target.value })} /><span>🏪</span><b>Recojo en tienda</b><small>Sin costo de entrega</small></label>
          <label className={form.fulfillmentType === 'DELIVERY' ? 'choice-card selected' : 'choice-card'}><input type="radio" name="fulfillment" value="DELIVERY" checked={form.fulfillmentType === 'DELIVERY'} onChange={event => setForm({ ...form, fulfillmentType: event.target.value })} /><span>🛵</span><b>Delivery</b><small>Según cobertura</small></label>
        </div>
        {form.fulfillmentType === 'DELIVERY' && <div className="checkout-fields">
          <label>Zona<select value={form.deliveryZoneId} onChange={event => setForm({ ...form, deliveryZoneId: event.target.value })}><option value="">Selecciona tu zona</option>{data.deliveryZones.map(item => <option key={item.id} value={item.id}>{item.name} — {money(item.fee)}</option>)}</select></label>
          <label>Dirección<input value={form.deliveryAddress} onChange={event => setForm({ ...form, deliveryAddress: event.target.value })} placeholder="Calle, número y distrito" /></label>
          <label>Referencia<input value={form.deliveryReference} onChange={event => setForm({ ...form, deliveryReference: event.target.value })} placeholder="Frente a..., puerta color..." /></label>
        </div>}

        <div className="schedule-selector">
          {status?.acceptsSameDay && <label className={form.scheduleMode === 'TODAY' ? 'schedule-option selected' : 'schedule-option'}><input type="radio" name="schedule" checked={form.scheduleMode === 'TODAY'} onChange={() => setForm({ ...form, scheduleMode: 'TODAY' })} /><span>⚡</span><div><b>Pedido para hoy</b><small>Dentro del horario de atención</small></div></label>}
          {reservationsEnabled && <label className={form.scheduleMode === 'RESERVATION' ? 'schedule-option selected' : 'schedule-option'}><input type="radio" name="schedule" checked={form.scheduleMode === 'RESERVATION'} onChange={() => setForm({ ...form, scheduleMode: 'RESERVATION' })} /><span>📅</span><div><b>Reservar otro día</b><small>Próxima atención: {tomorrow}</small></div></label>}
        </div>
        {form.scheduleMode === 'RESERVATION' && <div className="checkout-fields two-columns">
          <label>Fecha<input type="date" min={tomorrow} value={form.scheduledDate} onChange={event => setForm({ ...form, scheduledDate: event.target.value })} /></label>
          <label>Horario<select value={form.scheduledSlot} onChange={event => setForm({ ...form, scheduledSlot: event.target.value })}>{slots.map(slot => <option key={slot} value={slot}>{slot}</option>)}</select></label>
        </div>}
      </div>}

      {step === 2 && <div className="checkout-step">
        <h3>Tus datos</h3>
        <p className="checkout-intro">No necesitas crear una cuenta. Solo usaremos estos datos para coordinar tu pedido.</p>
        <div className="checkout-fields">
          <label>Nombre completo<input autoFocus value={form.customerName} onChange={event => setForm({ ...form, customerName: event.target.value })} placeholder="¿A nombre de quién?" /></label>
          <label>Celular / WhatsApp<input inputMode="tel" value={form.customerPhone} onChange={event => setForm({ ...form, customerPhone: event.target.value })} placeholder="9XX XXX XXX" /></label>
          <label>Observaciones<textarea value={form.notes} onChange={event => setForm({ ...form, notes: event.target.value })} placeholder="Tipo de corte, grosor u otra indicación" /></label>
        </div>
      </div>}

      {step === 3 && <div className="checkout-step">
        <h3>Pago y confirmación</h3>
        <div className="choice-grid payment-choice-grid">
          {paymentOptions(business).map(option => <label key={option.value} className={form.paymentMethod === option.value ? 'choice-card selected' : 'choice-card'}><input type="radio" name="payment" value={option.value} checked={form.paymentMethod === option.value} onChange={event => setForm({ ...form, paymentMethod: event.target.value })} /><span>{option.icon}</span><b>{option.label}</b></label>)}
        </div>
        <div className="checkout-summary">
          <h4>Resumen</h4>
          {cart.map(item => <div key={item.id}><span>{item.quantity} × {item.name}</span><b>{money(Number(item.quantity) * Number(item.pricePerUnit))}</b></div>)}
          <div><span>Subtotal</span><b>{money(subtotal)}</b></div>
          <div><span>Delivery</span><b>{money(deliveryFee)}</b></div>
          <div className="checkout-total"><span>Total</span><strong>{money(total)}</strong></div>
        </div>
        <PaymentDetails business={business} method={form.paymentMethod} total={total} />
        {form.paymentMethod !== 'CASH' && <p className="payment-note">Realiza el pago después de que el negocio confirme disponibilidad. Conserva tu comprobante.</p>}
      </div>}

      <div className="checkout-footer">
        {step > 1 ? <button type="button" className="continue-button" onClick={() => { setError(''); setStep(value => value - 1); }}>Atrás</button> : <button type="button" className="continue-button" onClick={onClose}>Cancelar</button>}
        {step < 3
          ? <button type="button" className="checkout-button" onClick={next}>Continuar</button>
          : <button type="button" className="checkout-button" onClick={submit} disabled={saving}>{saving ? 'Registrando...' : 'Confirmar pedido'}</button>}
      </div>
    </section>
  </div>;
}

function ConfirmationModal({ order, form, business, onClose }) {
  const phone = business?.phone || '';
  const confirmationUrl = whatsappUrl(phone, `Hola, deseo confirmar el pedido ${order.code}`);
  return <div className="storefront-modal-backdrop">
    <section className="order-confirmation" role="dialog" aria-modal="true">
      <div className="confirmation-icon">✓</div>
      <small>PEDIDO REGISTRADO</small>
      <h2>¡Gracias, {form.customerName}!</h2>
      <p>Recibimos tu pedido y lo revisaremos para confirmar disponibilidad y preparación.</p>
      <div className="order-code"><span>Código de seguimiento</span><strong>{order.code}</strong></div>
      <div className="confirmation-summary"><span>Total</span><b>{money(order.total)}</b><span>Modalidad</span><b>{order.fulfillmentType === 'DELIVERY' ? 'Delivery' : 'Recojo en tienda'}</b>{order.scheduledFor && <><span>Programado</span><b>{new Date(order.scheduledFor).toLocaleString('es-PE')}</b></>}</div>
      <PaymentDetails business={business} method={order.paymentMethod} total={Number(order.total)} />
      <Link className="checkout-button button-link" to={`/pedido/${encodeURIComponent(order.code)}`}>Ver estado del pedido</Link>
      {confirmationUrl && <a className="continue-button button-link" href={confirmationUrl} target="_blank" rel="noreferrer">Consultar por WhatsApp</a>}
      <button type="button" className="continue-button" onClick={onClose}>Volver al catálogo</button>
    </section>
  </div>;
}

export default function PublicCatalog() {
  const [data, setData] = useState(null);
  const [category, setCategory] = useState('all');
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');
  const [cart, setCart] = useState(readCart);
  const [cartOpen, setCartOpen] = useState(false);
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [confirmation, setConfirmation] = useState(null);

  useEffect(() => {
    api('/public/catalog').then(setData).catch(requestError => setError(requestError.message));
  }, []);

  useEffect(() => { localStorage.setItem(CART_KEY, JSON.stringify(cart)); }, [cart]);

  const products = useMemo(() => {
    if (!data) return [];
    const query = search.trim().toLowerCase();
    return data.products.filter(product => {
      const categoryMatch = category === 'all' || String(product.category.id) === category;
      const searchMatch = !query || `${product.name} ${product.description || ''} ${product.category.name}`.toLowerCase().includes(query);
      return categoryMatch && searchMatch;
    });
  }, [data, category, search]);

  const subtotal = cart.reduce((sum, item) => sum + Number(item.quantity) * Number(item.pricePerUnit), 0);
  const cartCount = cart.length;

  function add(product) {
    setCart(current => {
      const existing = current.find(item => item.id === product.id);
      const minimum = Number(product.minimumQuantity || (product.unit === 'KG' ? 0.5 : 1));
      if (existing) return current.map(item => item.id === product.id ? { ...item, quantity: Number(item.quantity) + minimum } : item);
      return [...current, { ...product, quantity: minimum }];
    });
    setCartOpen(true);
  }

  function changeQuantity(id, delta) {
    setCart(current => current.map(item => {
      if (item.id !== id) return item;
      const minimum = Number(item.minimumQuantity || 1);
      return { ...item, quantity: Math.max(minimum, Number(item.quantity) + delta) };
    }));
  }

  function remove(id) { setCart(current => current.filter(item => item.id !== id)); }

  function checkout() {
    setCartOpen(false);
    setCheckoutOpen(true);
  }

  function success(order, form) {
    setCheckoutOpen(false);
    setCart([]);
    setConfirmation({ order, form });
  }

  if (error) return <div className="public-loading">No se pudo cargar el catálogo: {error}</div>;
  if (!data) return <div className="public-loading">Cargando Carnicería La Herradura...</div>;

  const business = data.business;
  const status = data.businessStatus;
  const mainLocation = data.mainLocation || data.locations?.[0];
  const phone = mainLocation?.whatsappNumber || business.phone || '';
  const heroPhoto = mainLocation?.images?.find(image => ['COVER', 'FACADE'].includes(image.imageType))?.imageUrl || '/cover.png';

  return <div className="storefront-page">
    <StorefrontHeader business={business} cartCount={cartCount} onOpenCart={() => setCartOpen(true)} />

    <main>
      <section className="new-storefront-hero">
        <img className="hero-cover" src={heroPhoto} alt="Carnicería La Herradura y sus cortes de carne" />
        <div className="hero-dark-layer" />
        <div className="hero-content-new">
          <span className={status?.open ? 'hero-status open' : 'hero-status'}>{status?.open ? '● Atendiendo ahora' : '● Reservas disponibles'}</span>
          <h1>Carnes frescas y<br />cortes <em>seleccionados</em></h1>
          <p>{business.welcomeMessage || 'Calidad que se nota, sabor que enamora y atención sin complicaciones.'}</p>
          <div className="hero-buttons">
            <a className="hero-primary" href="#catalogo">🥩 Ver catálogo</a>
            <button className="hero-secondary" type="button" onClick={() => setCartOpen(true)}>🛒 Armar pedido</button>
            <a className="hero-reserve" href="#catalogo">📅 Reservar para mañana</a>
          </div>
          <div className="hero-benefits"><span>✓ Compra sin registrarte</span><span>✓ Delivery o recojo</span><span>✓ Atención por Mashico</span></div>
        </div>
      </section>

      <section className="storefront-trust-row">
        <div><span>🕒</span><b>Horario de atención</b><small>{business.openingHours || `${business.openingTime} - ${business.closingTime}`}</small></div>
        <div><span>🏪</span><b>Recojo en tienda</b><small>Haz tu pedido y recógelo sin esperar.</small></div>
        <div><span>🛵</span><b>Delivery y reservas</b><small>Pedido para hoy o programado.</small></div>
        <div><span>💳</span><b>Métodos de pago</b><small>Efectivo{business.yapeEnabled ? ', Yape' : ''}{business.plinEnabled ? ', Plin' : ''}{business.transferEnabled ? ', transferencia' : ''}.</small></div>
      </section>

      <section id="catalogo" className="modern-catalog-section">
        <div className="storefront-section-heading"><div><span>CATÁLOGO</span><h2>Elige el corte ideal para hoy</h2><p>Busca por nombre, ocasión o categoría y agrégalo en un toque.</p></div><button type="button" onClick={() => setCartOpen(true)}>Ver carrito ({cartCount})</button></div>

        <div className="catalog-tools">
          <label className="catalog-search"><span>⌕</span><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Buscar bistec, parrilla, guiso..." /></label>
          <div className="modern-category-tabs"><button className={category === 'all' ? 'active' : ''} onClick={() => setCategory('all')}>Todos</button>{data.categories.map(item => <button className={category === String(item.id) ? 'active' : ''} onClick={() => setCategory(String(item.id))} key={item.id}>{item.name}</button>)}</div>
        </div>

        <div className="modern-product-grid">
          {products.map(product => <article className="modern-product-card" key={product.id}>
            <div className="modern-product-media">{product.imageUrl ? <img src={product.imageUrl} alt={product.name} /> : <span>🥩</span>}{product.featured && <b>Recomendado</b>}</div>
            <div className="modern-product-body"><small>{product.category.name}</small><h3>{product.name}</h3><p>{product.description}</p><div className="product-price-row"><strong>{money(product.pricePerUnit)}<small>{product.unit === 'KG' ? ' / kg' : ''}</small></strong><span>Stock: {product.stockQuantity}</span></div><button type="button" onClick={() => add(product)}>Agregar al pedido</button></div>
          </article>)}
          {products.length === 0 && <div className="no-products">No encontramos productos con esa búsqueda.</div>}
        </div>
      </section>

      {data.promotions.length > 0 && <section id="promociones" className="modern-promotions-section">
        <div className="storefront-section-heading light"><div><span>PROMOCIONES</span><h2>Combos que rinden más</h2><p>Opciones rápidas para la parrilla, la semana o una reunión familiar.</p></div></div>
        <div className="modern-promo-grid">{data.promotions.map((promotion, index) => <article key={promotion.id}>
          <div className="modern-promo-image">{promotion.imageUrl ? <img src={promotion.imageUrl} alt={promotion.name} /> : <span>🔥</span>}<b>{index === 0 ? 'Más pedido' : 'Promoción'}</b></div>
          <div><h3>{promotion.name}</h3><p>{promotion.description}</p><strong>{promotion.promotionalPrice ? money(promotion.promotionalPrice) : 'Consulta el precio'}</strong>{phone ? <a href={whatsappUrl(phone, `Hola, deseo la promoción ${promotion.name}`)} target="_blank" rel="noreferrer">Pedir promoción</a> : <a href="#catalogo">Ver catálogo</a>}</div>
        </article>)}</div>
      </section>}

      <section className="mashico-public-banner">
        <div className="mashico-avatar">🐮</div><div><span>ASISTENTE DE PEDIDOS</span><h2>Mashico te ayuda a elegir y reservar</h2><p>Pregunta por un producto, el horario, una promoción o la ubicación del negocio.</p></div>{phone && <a href={whatsappUrl(phone, 'Hola Mashico, deseo ver el menú')} target="_blank" rel="noreferrer">Hablar con Mashico</a>}
      </section>

      <div className="location-home-wrapper"><StoreLocationSection location={mainLocation} compact /></div>

      <section id="contacto" className="storefront-contact-section">
        <div><img src="/logo.png" alt="Carnicería La Herradura" /><h2>{business.businessName}</h2><p>Calidad, confianza y tradición en cada corte.</p></div>
        <div><b>Contáctanos</b><p>📞 {phone}</p><p>📍 {mainLocation?.address || business.address}</p></div>
        <div><b>Compra como prefieras</b><Link to="/ubicacion">Cómo llegar al local</Link><Link to="/pedido">Consultar pedido</Link>{phone && <a href={whatsappUrl(phone, 'Hola, deseo hacer un pedido')} target="_blank" rel="noreferrer">Pedir por WhatsApp</a>}</div>
      </section>
    </main>

    <button className="floating-cart" type="button" onClick={() => setCartOpen(true)}>🛒<span>{cartCount}</span></button>
    {phone && <a className="floating-wa-new" href={whatsappUrl(phone, 'Hola, deseo hacer un pedido')} target="_blank" rel="noreferrer">WhatsApp</a>}

    <CartDrawer open={cartOpen} cart={cart} onClose={() => setCartOpen(false)} onChange={changeQuantity} onRemove={remove} subtotal={subtotal} onCheckout={checkout} />
    {checkoutOpen && <CheckoutModal data={data} cart={cart} subtotal={subtotal} onClose={() => setCheckoutOpen(false)} onSuccess={success} />}
    {confirmation && <ConfirmationModal order={confirmation.order} form={confirmation.form} business={business} onClose={() => setConfirmation(null)} />}
  </div>;
}
