import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { ErrorBox, PageHeader } from '../components/UI';

const weekDays = [
  ['MONDAY', 'Lunes'],
  ['TUESDAY', 'Martes'],
  ['WEDNESDAY', 'Miércoles'],
  ['THURSDAY', 'Jueves'],
  ['FRIDAY', 'Viernes'],
  ['SATURDAY', 'Sábado'],
  ['SUNDAY', 'Domingo']
];

function withDefaults(value) {
  return {
    assistantName: 'Mashico',
    timeZone: 'America/Lima',
    attentionDays: 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY',
    openingTime: '08:00',
    closingTime: '20:00',
    sameDayCutoffTime: '19:30',
    allowNextDayReservations: true,
    reservationSlots: '08:00-10:00;10:00-12:00;12:00-14:00;14:00-16:00;16:00-18:00',
    sendProductImages: true,
    yapeEnabled: false,
    plinEnabled: false,
    transferEnabled: false,
    ...value,
    openingTime: String(value?.openingTime || '08:00').slice(0, 5),
    closingTime: String(value?.closingTime || '20:00').slice(0, 5),
    sameDayCutoffTime: String(value?.sameDayCutoffTime || '19:30').slice(0, 5)
  };
}

export default function Settings() {
  const [settings, setSettings] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState('');
  const [password, setPassword] = useState({ currentPassword: '', newPassword: '' });

  useEffect(() => {
    api('/settings')
      .then(result => setSettings(withDefaults(result)))
      .catch(requestError => setError(requestError.message));
  }, []);

  if (!settings) return <div className="loading">Cargando configuración...</div>;

  async function save(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      const result = await api('/settings', {
        method: 'PUT',
        body: JSON.stringify({
          ...settings,
          minimumDeliveryAmount: Number(settings.minimumDeliveryAmount || 0)
        })
      });
      setSettings(withDefaults(result));
      setMessage('Configuración guardada correctamente');
    } catch (saveError) {
      setError(saveError.message);
    }
  }

  async function changePassword(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await api('/users/me/password', {
        method: 'POST',
        body: JSON.stringify(password)
      });
      setPassword({ currentPassword: '', newPassword: '' });
      setMessage('Contraseña actualizada');
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  function toggleDay(day) {
    const selected = new Set(String(settings.attentionDays || '').split(',').filter(Boolean));
    if (selected.has(day)) selected.delete(day);
    else selected.add(day);
    const ordered = weekDays.map(([value]) => value).filter(value => selected.has(value));
    setSettings({ ...settings, attentionDays: ordered.join(',') });
  }

  async function uploadQr(event, field) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    setUploading(field);
    setError('');
    try {
      const data = new FormData();
      data.append('file', file);
      const result = await api('/media/payment-qr', { method: 'POST', body: data });
      setSettings(current => ({ ...current, [field]: result.url }));
    } catch (uploadError) {
      setError(uploadError.message);
    } finally {
      setUploading('');
    }
  }

  return <>
    <PageHeader
      title="Configuración"
      subtitle="Horario, reservas, pagos y comportamiento de Mashico"
    />

    <form onSubmit={save}>
      <section className="panel form-grid settings-section">
        <h2 className="full">Datos del negocio</h2>
        <label>Nombre comercial
          <input value={settings.businessName || ''} onChange={event => setSettings({ ...settings, businessName: event.target.value })} />
        </label>
        <label>WhatsApp comercial
          <input value={settings.phone || ''} onChange={event => setSettings({ ...settings, phone: event.target.value })} />
        </label>
        <label className="full">Dirección
          <input value={settings.address || ''} onChange={event => setSettings({ ...settings, address: event.target.value })} />
        </label>
        <label>Pedido mínimo para delivery
          <input type="number" step="0.01" min="0" value={settings.minimumDeliveryAmount || 0} onChange={event => setSettings({ ...settings, minimumDeliveryAmount: event.target.value })} />
        </label>
        <label className="check">
          <input type="checkbox" checked={Boolean(settings.deliveryEnabled)} onChange={event => setSettings({ ...settings, deliveryEnabled: event.target.checked })} />
          Delivery habilitado
        </label>
        <label className="full">Mensaje comercial
          <textarea value={settings.welcomeMessage || ''} onChange={event => setSettings({ ...settings, welcomeMessage: event.target.value })} />
        </label>
      </section>

      <section className="panel form-grid settings-section">
        <h2 className="full">Horario y reservas</h2>
        <label>Zona horaria
          <input value={settings.timeZone || 'America/Lima'} onChange={event => setSettings({ ...settings, timeZone: event.target.value })} />
        </label>
        <label>Texto público del horario
          <input value={settings.openingHours || ''} onChange={event => setSettings({ ...settings, openingHours: event.target.value })} placeholder="Lunes a sábado: 8:00 a 20:00" />
        </label>
        <label>Hora de apertura
          <input type="time" value={settings.openingTime} onChange={event => setSettings({ ...settings, openingTime: event.target.value })} />
        </label>
        <label>Hora de cierre
          <input type="time" value={settings.closingTime} onChange={event => setSettings({ ...settings, closingTime: event.target.value })} />
        </label>
        <label>Última hora para pedidos del día
          <input type="time" value={settings.sameDayCutoffTime} onChange={event => setSettings({ ...settings, sameDayCutoffTime: event.target.value })} />
        </label>
        <label className="check">
          <input type="checkbox" checked={Boolean(settings.allowNextDayReservations)} onChange={event => setSettings({ ...settings, allowNextDayReservations: event.target.checked })} />
          Permitir reservas para el próximo día de atención
        </label>
        <div className="full">
          <strong>Días de atención</strong>
          <div className="weekday-grid">
            {weekDays.map(([value, label]) => <label className="check weekday" key={value}>
              <input
                type="checkbox"
                checked={String(settings.attentionDays || '').split(',').includes(value)}
                onChange={() => toggleDay(value)}
              />
              {label}
            </label>)}
          </div>
        </div>
        <label className="full">Franjas para reservas
          <textarea
            value={settings.reservationSlots || ''}
            onChange={event => setSettings({ ...settings, reservationSlots: event.target.value })}
            placeholder="08:00-10:00;10:00-12:00;12:00-14:00"
          />
          <small>Separa cada franja con punto y coma.</small>
        </label>
      </section>

      <section className="panel form-grid settings-section">
        <h2 className="full">Mashico y contenido visual</h2>
        <label>Nombre del asistente
          <input value={settings.assistantName || 'Mashico'} onChange={event => setSettings({ ...settings, assistantName: event.target.value })} />
        </label>
        <label className="check">
          <input type="checkbox" checked={Boolean(settings.sendProductImages)} onChange={event => setSettings({ ...settings, sendProductImages: event.target.checked })} />
          Enviar fotografías de productos por WhatsApp
        </label>
        <p className="full settings-help">
          Mashico enviará la imagen registrada en Productos cuando el cliente seleccione o escriba el nombre de un corte. También enviará la imagen del primer combo activo al consultar promociones.
        </p>
      </section>

      <section className="panel form-grid settings-section">
        <h2 className="full">Datos de pago</h2>

        <div className="full payment-config-card">
          <label className="check payment-toggle">
            <input type="checkbox" checked={Boolean(settings.yapeEnabled)} onChange={event => setSettings({ ...settings, yapeEnabled: event.target.checked })} />
            <strong>Habilitar Yape</strong>
          </label>
          <div className="payment-fields">
            <label>Número Yape
              <input value={settings.yapeNumber || ''} onChange={event => setSettings({ ...settings, yapeNumber: event.target.value })} />
            </label>
            <label>Titular
              <input value={settings.yapeHolder || ''} onChange={event => setSettings({ ...settings, yapeHolder: event.target.value })} />
            </label>
            <QrEditor
              label="QR de Yape"
              value={settings.yapeQrUrl}
              field="yapeQrUrl"
              uploading={uploading}
              onUpload={uploadQr}
              onChange={value => setSettings({ ...settings, yapeQrUrl: value })}
            />
          </div>
        </div>

        <div className="full payment-config-card">
          <label className="check payment-toggle">
            <input type="checkbox" checked={Boolean(settings.plinEnabled)} onChange={event => setSettings({ ...settings, plinEnabled: event.target.checked })} />
            <strong>Habilitar Plin</strong>
          </label>
          <div className="payment-fields">
            <label>Número Plin
              <input value={settings.plinNumber || ''} onChange={event => setSettings({ ...settings, plinNumber: event.target.value })} />
            </label>
            <label>Titular
              <input value={settings.plinHolder || ''} onChange={event => setSettings({ ...settings, plinHolder: event.target.value })} />
            </label>
            <QrEditor
              label="QR de Plin"
              value={settings.plinQrUrl}
              field="plinQrUrl"
              uploading={uploading}
              onUpload={uploadQr}
              onChange={value => setSettings({ ...settings, plinQrUrl: value })}
            />
          </div>
        </div>

        <div className="full payment-config-card">
          <label className="check payment-toggle">
            <input type="checkbox" checked={Boolean(settings.transferEnabled)} onChange={event => setSettings({ ...settings, transferEnabled: event.target.checked })} />
            <strong>Habilitar transferencia bancaria</strong>
          </label>
          <div className="payment-fields bank-fields">
            <label>Banco
              <input value={settings.bankName || ''} onChange={event => setSettings({ ...settings, bankName: event.target.value })} />
            </label>
            <label>Tipo de cuenta
              <input value={settings.bankAccountType || ''} onChange={event => setSettings({ ...settings, bankAccountType: event.target.value })} placeholder="Ahorros" />
            </label>
            <label>Número de cuenta
              <input value={settings.bankAccountNumber || ''} onChange={event => setSettings({ ...settings, bankAccountNumber: event.target.value })} />
            </label>
            <label>CCI
              <input value={settings.bankCci || ''} onChange={event => setSettings({ ...settings, bankCci: event.target.value })} />
            </label>
            <label className="full">Titular
              <input value={settings.bankHolder || ''} onChange={event => setSettings({ ...settings, bankHolder: event.target.value })} />
            </label>
          </div>
        </div>

        <ErrorBox message={error} />
        {message && <div className="success full">{message}</div>}
        <div className="form-actions full">
          <button className="primary" disabled={Boolean(uploading)}>Guardar configuración</button>
        </div>
      </section>
    </form>

    <form className="panel form-grid" onSubmit={changePassword}>
      <h2 className="full">Seguridad</h2>
      <label>Contraseña actual
        <input type="password" required value={password.currentPassword} onChange={event => setPassword({ ...password, currentPassword: event.target.value })} />
      </label>
      <label>Nueva contraseña
        <input type="password" minLength="10" required value={password.newPassword} onChange={event => setPassword({ ...password, newPassword: event.target.value })} />
      </label>
      <div className="form-actions full">
        <button className="primary">Cambiar contraseña</button>
      </div>
    </form>
  </>;
}

function QrEditor({ label, value, field, uploading, onUpload, onChange }) {
  return <div className="qr-editor full">
    <div className="qr-preview">
      {value ? <img src={value} alt={label} /> : <span>QR</span>}
    </div>
    <div>
      <strong>{label}</strong>
      <label className={`upload-button ${uploading === field ? 'disabled' : ''}`}>
        {uploading === field ? 'Subiendo…' : 'Seleccionar imagen QR'}
        <input
          type="file"
          accept="image/jpeg,image/png,image/webp"
          disabled={Boolean(uploading)}
          onChange={event => onUpload(event, field)}
        />
      </label>
      <label>O pega una URL
        <input type="url" value={value || ''} onChange={event => onChange(event.target.value)} placeholder="https://..." />
      </label>
    </div>
  </div>;
}
