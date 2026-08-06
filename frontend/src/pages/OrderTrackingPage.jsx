import React, { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import StorefrontHeader, { whatsappUrl } from '../components/storefront/StorefrontHeader';
import { api, money } from '../services/api';

const statusOrder = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED'];
const statusLabels = {
  PENDING: 'Pedido recibido', CONFIRMED: 'Pedido confirmado', PREPARING: 'En preparación',
  READY: 'Listo para recoger', OUT_FOR_DELIVERY: 'En camino', DELIVERED: 'Entregado', CANCELLED: 'Cancelado'
};

function cleanPhone(value) { return String(value || '').replace(/\D/g, ''); }

export default function OrderTrackingPage() {
  const params = useParams();
  const [code, setCode] = useState(params.code || '');
  const [phone, setPhone] = useState('');
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [business, setBusiness] = useState(null);

  React.useEffect(() => {
    api('/public/catalog').then(data => setBusiness(data.business)).catch(() => {});
  }, []);

  const activeIndex = useMemo(() => order ? statusOrder.indexOf(order.status) : -1, [order]);

  async function search(event) {
    event.preventDefault();
    const normalizedCode = code.trim().toUpperCase();
    const normalizedPhone = cleanPhone(phone);
    setError(''); setOrder(null);
    if (!normalizedCode) { setError('Escribe el código del pedido'); return; }
    if (normalizedPhone.length < 9) { setError('Escribe el celular usado en el pedido'); return; }
    setLoading(true);
    try {
      const result = await api(`/public/orders/${encodeURIComponent(normalizedCode)}?phone=${encodeURIComponent(normalizedPhone)}`);
      setCode(normalizedCode);
      setOrder(result);
    } catch (requestError) {
      setError(requestError.message === 'Pedido no encontrado'
        ? 'No encontramos un pedido con ese código y celular. Revisa los datos.'
        : requestError.message);
    } finally { setLoading(false); }
  }

  const contactUrl = whatsappUrl(business?.phone, order
    ? `Hola, necesito ayuda con el pedido ${order.code}`
    : 'Hola, necesito ayuda para consultar mi pedido');

  return <div className="storefront-page order-tracking-page">
    <StorefrontHeader business={business} />
    <main className="tracking-shell">
      <section className="tracking-intro">
        <span>SEGUIMIENTO</span>
        <h1>Consulta el estado de tu pedido</h1>
        <p>Ingresa el código que recibiste y el mismo celular usado al comprar.</p>
      </section>

      <form className="tracking-form" onSubmit={search}>
        <label>Código del pedido<input value={code} onChange={event => setCode(event.target.value)} placeholder="LH-20260806-XXXXXXXX" autoComplete="off" /></label>
        <label>Celular<input value={phone} onChange={event => setPhone(event.target.value)} placeholder="9XX XXX XXX" inputMode="tel" /></label>
        <button type="submit" disabled={loading}>{loading ? 'Consultando...' : 'Consultar pedido'}</button>
      </form>
      {error && <div className="public-error tracking-error" role="alert">{error}</div>}

      {order && <section className="tracking-result" aria-live="polite">
        <div className="tracking-result-head">
          <div><small>PEDIDO</small><h2>{order.code}</h2></div>
          <span className={`tracking-status ${String(order.status).toLowerCase()}`}>{statusLabels[order.status] || order.status}</span>
        </div>

        {order.status === 'CANCELLED'
          ? <div className="tracking-cancelled">Este pedido fue cancelado. Comunícate con el negocio para más información.</div>
          : <div className="tracking-timeline">
            {statusOrder.map((status, index) => <div key={status} className={index <= activeIndex ? 'done' : ''}>
              <span>{index < activeIndex ? '✓' : index + 1}</span><small>{statusLabels[status]}</small>
            </div>)}
          </div>}

        <div className="tracking-summary-grid">
          <div><small>Modalidad</small><b>{order.fulfillmentType === 'DELIVERY' ? 'Delivery' : 'Recojo en tienda'}</b></div>
          <div><small>Pago</small><b>{String(order.paymentMethod).replaceAll('_', ' ')}</b></div>
          <div><small>Total</small><b>{money(order.total)}</b></div>
          <div><small>Programado</small><b>{order.scheduledFor ? new Date(order.scheduledFor).toLocaleString('es-PE') : 'Pedido para hoy'}</b></div>
        </div>

        <div className="tracking-items">
          <h3>Detalle del pedido</h3>
          {order.items.map((item, index) => <div key={`${item.productName}-${index}`}><span>{item.quantity} × {item.productName}</span><b>{money(item.subtotal)}</b></div>)}
        </div>

        <div className="tracking-actions">
          <Link to="/catalogo">Volver al catálogo</Link>
          {contactUrl && <a href={contactUrl} target="_blank" rel="noreferrer">Consultar por WhatsApp</a>}
        </div>
      </section>}
    </main>
  </div>;
}
