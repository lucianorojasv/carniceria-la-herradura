import React, { useEffect, useState } from 'react';
import { api, money } from '../services/api';
import { PageHeader, Status } from '../components/UI';

const statuses = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const load = () => api('/orders').then(setOrders);

  useEffect(() => { load(); }, []);

  async function change(id, status) {
    await api(`/orders/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status })
    });
    load();
  }

  return <>
    <PageHeader title="Pedidos" subtitle="Pedidos inmediatos y reservas programadas" />
    <div className="panel">
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Pedido</th>
              <th>Cliente</th>
              <th>Detalle</th>
              <th>Modalidad</th>
              <th>Programación</th>
              <th>Total</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {orders.map(order => <tr key={order.id}>
              <td>
                <b>{order.code}</b>
                <small>Creado: {new Date(order.createdAt).toLocaleString('es-PE')}</small>
                {order.source === 'WHATSAPP' && <small>Origen: WhatsApp</small>}
              </td>
              <td>{order.customer?.name}<small>{order.customer?.phone}</small></td>
              <td>{order.items?.map(item => <div key={item.id}>{item.productName} × {item.quantity}</div>)}</td>
              <td>
                {order.fulfillmentType === 'DELIVERY' ? '🛵 Delivery' : '🏪 Recojo'}
                <small>{order.deliveryZone?.name}</small>
                {order.deliveryAddress && <small>{order.deliveryAddress}</small>}
              </td>
              <td>
                {order.scheduledFor
                  ? <span className="scheduled-badge">📅 {new Date(order.scheduledFor).toLocaleString('es-PE')}</span>
                  : <span>Para hoy</span>}
              </td>
              <td><b>{money(order.total)}</b><small>{order.paymentMethod}</small></td>
              <td>
                <Status value={order.status} />
                <select value={order.status} onChange={event => change(order.id, event.target.value)}>
                  {statuses.map(status => <option key={status}>{status}</option>)}
                </select>
              </td>
            </tr>)}
          </tbody>
        </table>
      </div>
    </div>
  </>;
}
